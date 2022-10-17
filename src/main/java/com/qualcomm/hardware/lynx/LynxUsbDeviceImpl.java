package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.hardware.bosch.BHI260IMU;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.hardware.lynx.commands.core.LynxFirmwareVersionManager;
import com.qualcomm.hardware.lynx.commands.standard.LynxDiscoveryCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxDiscoveryResponse;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.LynxModuleMeta;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.hardware.usb.ftdi.RobotUsbDeviceFtdi;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.IncludedFirmwareFileInfo;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.TypeConversion;
import com.qualcomm.robotcore.util.Util;
import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.hardware.usb.ArmableUsbDevice;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbDeviceClosedException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbFTDIException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbUnspecifiedException;

public class LynxUsbDeviceImpl extends ArmableUsbDevice implements LynxUsbDevice {
    public static boolean DEBUG_LOG_DATAGRAMS = false;
    public static boolean DEBUG_LOG_DATAGRAMS_FINISH = false;
    public static boolean DEBUG_LOG_DATAGRAMS_LOCK = false;
    public static boolean DEBUG_LOG_MESSAGES = false;
    protected static final String SEPARATOR = " / ";
    public static final String TAG = "LynxUsb";
    protected static final int cbusBothAsserted = 0;
    protected static final int cbusMask = 3;
    protected static final int cbusNProg = 2;
    protected static final int cbusNReset = 1;
    protected static final int cbusNeitherAsserted = 3;
    protected static final int cbusProgAsserted = 1;
    protected static final int cbusResetAsserted = 2;
    protected static final LynxCommExceptionHandler exceptionHandler = new LynxCommExceptionHandler(TAG);
    protected static final WeakReferenceSet<LynxUsbDeviceImpl> extantDevices = new WeakReferenceSet<>();
    protected static final int msCbusWiggle = 75;
    protected static final int msNetworkTransmissionLockAcquisitionTimeMax = 500;
    protected static final int msResetRecovery = 200;
    protected final ConcurrentHashMap<Integer, LynxModuleMeta> discoveredModules = new ConcurrentHashMap<>();
    protected final Object engageLock = new Object();
    protected boolean hasShutdownAbnormally = false;
    protected ExecutorService incomingDatagramPoller = null;
    protected boolean isEngaged = true;
    protected boolean isSystemSynthetic = false;
    protected final ConcurrentHashMap<Integer, LynxModule> knownModules = new ConcurrentHashMap<>();
    protected final ConcurrentHashMap<Integer, LynxModule> knownModulesChanging = new ConcurrentHashMap<>();
    protected final LynxFirmwareUpdater lynxFirmwareUpdater = new LynxFirmwareUpdater(this);
    protected final ConcurrentHashMap<Integer, String> missingModules = new ConcurrentHashMap<>();
    protected final MessageKeyedLock networkTransmissionLock = new MessageKeyedLock("lynx xmit lock", msNetworkTransmissionLockAcquisitionTimeMax);
    protected boolean resetAttempted = false;
    protected final Object systemOperationLock = new Object();
    protected boolean wasPollingWhenEngaged = true;

    public LynxUsbDeviceImpl getDelegationTarget() {
        return this;
    }

    public RobotUsbModule getOwner() {
        return this;
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public int getVersion() {
        return 1;
    }

    public void resetDeviceConfigurationForOpMode() {
    }

    public void setOwner(RobotUsbModule robotUsbModule) {
    }

    protected LynxUsbDeviceImpl(Context context, SerialNumber serialNumber, SyncdDevice.Manager manager, ArmableUsbDevice.OpenRobotUsbDevice openRobotUsbDevice) {
        super(context, serialNumber, manager, openRobotUsbDevice);
        extantDevices.add(this);
        finishConstruction();
    }

    public static LynxUsbDevice findOrCreateAndArm(Context context, SerialNumber serialNumber, SyncdDevice.Manager manager, ArmableUsbDevice.OpenRobotUsbDevice openRobotUsbDevice) throws RobotCoreException, InterruptedException {
        WeakReferenceSet<LynxUsbDeviceImpl> weakReferenceSet = extantDevices;
        synchronized (weakReferenceSet) {
            Iterator<LynxUsbDeviceImpl> it = weakReferenceSet.iterator();
            while (it.hasNext()) {
                LynxUsbDeviceImpl next = it.next();
                if (next.getSerialNumber().equals((Object) serialNumber) && next.getArmingState() != RobotArmingStateNotifier.ARMINGSTATE.CLOSED) {
                    next.addRef();
                    RobotLog.m61vv(TAG, "using existing [%s]: 0x%08x", serialNumber, Integer.valueOf(next.hashCode()));
                    LynxUsbDeviceDelegate lynxUsbDeviceDelegate = new LynxUsbDeviceDelegate(next);
                    return lynxUsbDeviceDelegate;
                }
            }
            LynxUsbDeviceImpl lynxUsbDeviceImpl = new LynxUsbDeviceImpl(context, serialNumber, manager, openRobotUsbDevice);
            RobotLog.m61vv(TAG, "creating new [%s]: 0x%08x", serialNumber, Integer.valueOf(lynxUsbDeviceImpl.hashCode()));
            lynxUsbDeviceImpl.armOrPretend();
            LynxUsbDeviceDelegate lynxUsbDeviceDelegate2 = new LynxUsbDeviceDelegate(lynxUsbDeviceImpl);
            return lynxUsbDeviceDelegate2;
        }
    }

    public boolean isSystemSynthetic() {
        return this.isSystemSynthetic;
    }

    public void setSystemSynthetic(boolean z) {
        this.isSystemSynthetic = z;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
        WeakReferenceSet<LynxUsbDeviceImpl> weakReferenceSet = extantDevices;
        synchronized (weakReferenceSet) {
            super.doClose();
            weakReferenceSet.remove(this);
        }
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.moduleDisplayNameLynxUsbDevice);
    }

    public String getConnectionInfo() {
        return "USB " + getSerialNumber();
    }

    public SyncdDevice.ShutdownReason getShutdownReason() {
        if (this.hasShutdownAbnormally || this.robotUsbDevice == null || !this.robotUsbDevice.isOpen()) {
            return SyncdDevice.ShutdownReason.ABNORMAL;
        }
        return SyncdDevice.ShutdownReason.NORMAL;
    }

    /* access modifiers changed from: protected */
    public boolean hasShutdownAbnormally() {
        return getShutdownReason() != SyncdDevice.ShutdownReason.NORMAL;
    }

    public synchronized void engage() {
        synchronized (this.engageLock) {
            if (!this.isEngaged) {
                if (this.wasPollingWhenEngaged && isArmed()) {
                    startPollingForIncomingDatagrams();
                }
                for (LynxModule engage : getKnownModules()) {
                    engage.engage();
                }
                this.isEngaged = true;
            }
        }
    }

    public synchronized void disengage() {
        synchronized (this.engageLock) {
            if (this.isEngaged) {
                this.isEngaged = false;
                for (LynxModule disengage : getKnownModules()) {
                    disengage.disengage();
                }
                this.wasPollingWhenEngaged = stopPollingForIncomingDatagrams();
            }
        }
    }

    public synchronized boolean isEngaged() {
        boolean z;
        synchronized (this.engageLock) {
            z = this.isEngaged;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void doPretend() throws RobotCoreException, InterruptedException {
        RobotLog.m61vv(TAG, "doPretend() serial=%s", this.serialNumber);
    }

    /* access modifiers changed from: protected */
    public void armDevice(RobotUsbDevice robotUsbDevice) throws RobotCoreException, InterruptedException {
        synchronized (this.armingLock) {
            RobotLog.m61vv(TAG, "armDevice() serial=%s...", this.serialNumber);
            Assert.assertTrue(robotUsbDevice != null);
            this.robotUsbDevice = robotUsbDevice;
            if (!this.resetAttempted) {
                this.resetAttempted = true;
                resetDevice(this.robotUsbDevice);
            }
            this.hasShutdownAbnormally = false;
            if (this.syncdDeviceManager != null) {
                this.syncdDeviceManager.registerSyncdDevice(this);
            }
            resetNetworkTransmissionLock();
            startPollingForIncomingDatagrams();
            pingAndQueryKnownInterfaces();
            startRegularPinging();
            RobotLog.m60vv(TAG, "...done armDevice()");
        }
    }

    /* access modifiers changed from: protected */
    public void disarmDevice() throws InterruptedException {
        synchronized (this.armingLock) {
            RobotLog.m61vv(TAG, "disarmDevice() serial=%s...", this.serialNumber);
            Assert.assertFalse(isArmedOrArming());
            pretendFinishExtantCommands();
            abandonUnfinishedCommands();
            stopRegularPinging();
            stopPollingForIncomingDatagrams();
            if (this.robotUsbDevice != null) {
                this.robotUsbDevice.close();
                this.robotUsbDevice = null;
            }
            resetNetworkTransmissionLock();
            if (this.syncdDeviceManager != null) {
                this.syncdDeviceManager.unregisterSyncdDevice(this);
            }
            RobotLog.m60vv(TAG, "...done disarmDevice()");
        }
    }

    /* access modifiers changed from: protected */
    public void doCloseFromArmed() throws RobotCoreException, InterruptedException {
        failSafe();
        closeModules();
        super.doCloseFromArmed();
    }

    /* access modifiers changed from: protected */
    public void doCloseFromOther() throws RobotCoreException, InterruptedException {
        closeModules();
        super.doCloseFromOther();
    }

    /* access modifiers changed from: protected */
    public void closeModules() {
        for (LynxModule close : getKnownModules()) {
            close.close();
        }
    }

    public void failSafe() {
        for (LynxModule next : getKnownModules()) {
            try {
                if (next.isUserModule()) {
                    next.failSafe();
                }
            } catch (LynxNackException | RobotCoreException | InterruptedException e) {
                exceptionHandler.handleException(e);
            }
        }
    }

    /* access modifiers changed from: protected */
    public Collection<LynxModule> getKnownModules() {
        Collection<LynxModule> values;
        synchronized (this.knownModules) {
            values = this.knownModules.values();
        }
        return values;
    }

    /* access modifiers changed from: protected */
    public LynxModule findKnownModule(int i) {
        LynxModule lynxModule;
        synchronized (this.knownModules) {
            lynxModule = this.knownModules.get(Integer.valueOf(i));
            if (lynxModule == null) {
                lynxModule = this.knownModulesChanging.get(Integer.valueOf(i));
            }
        }
        return lynxModule;
    }

    public List<String> getAllModuleFirmwareVersions() {
        ArrayList arrayList = new ArrayList();
        for (LynxModule next : getKnownModules()) {
            next.getFirmwareVersionString();
            arrayList.add(next.getModuleAddress() + SEPARATOR + next.getFirmwareVersionString());
        }
        return arrayList;
    }

    public void changeModuleAddress(LynxModule lynxModule, int i, Runnable runnable) {
        int moduleAddress = lynxModule.getModuleAddress();
        if (i != moduleAddress) {
            synchronized (this.knownModules) {
                this.knownModulesChanging.put(Integer.valueOf(i), lynxModule);
            }
            runnable.run();
            synchronized (this.knownModules) {
                this.knownModules.put(Integer.valueOf(i), lynxModule);
                this.knownModules.remove(Integer.valueOf(moduleAddress));
                this.knownModulesChanging.remove(Integer.valueOf(i));
            }
        }
    }

    public void noteMissingModule(LynxModule lynxModule, String str) {
        this.missingModules.put(Integer.valueOf(lynxModule.getModuleAddress()), str);
        RobotLog.m49ee(TAG, "module #%d did not connect at startup: skip adding its hardware items to the hardwareMap", Integer.valueOf(lynxModule.getModuleAddress()));
    }

    /* access modifiers changed from: protected */
    public String composeGlobalWarning() {
        ArrayList arrayList = new ArrayList();
        String composeGlobalWarning = super.composeGlobalWarning();
        arrayList.add(composeGlobalWarning);
        if (composeGlobalWarning.isEmpty()) {
            for (String str : this.missingModules.values()) {
                arrayList.add(AppUtil.getDefContext().getString(C0660R.string.errorExpansionHubIsMissing, new Object[]{str}));
            }
            for (LynxModule globalWarnings : getKnownModules()) {
                arrayList.addAll(globalWarnings.getGlobalWarnings());
            }
        }
        return RobotLog.combineGlobalWarnings(arrayList);
    }

    public LynxModule addConfiguredModule(LynxModule lynxModule) throws InterruptedException, RobotCoreException {
        boolean z;
        LynxModule lynxModule2;
        synchronized (this.systemOperationLock) {
            RobotLog.m61vv(TAG, "addConfiguredModule() module#=%d", Integer.valueOf(lynxModule.getModuleAddress()));
            synchronized (this.knownModules) {
                if (!this.knownModules.containsKey(Integer.valueOf(lynxModule.getModuleAddress()))) {
                    this.knownModules.put(Integer.valueOf(lynxModule.getModuleAddress()), lynxModule);
                    lynxModule2 = lynxModule;
                    z = true;
                } else {
                    lynxModule2 = this.knownModules.get(Integer.valueOf(lynxModule.getModuleAddress()));
                    RobotLog.m61vv(TAG, "addConfiguredModule() module#=%d: already exists", Integer.valueOf(lynxModule.getModuleAddress()));
                    if (lynxModule.isUserModule() && !lynxModule2.isUserModule()) {
                        RobotLog.m61vv(TAG, "Converting module #%d to a user module", Integer.valueOf(lynxModule2.getModuleAddress()));
                        lynxModule2.setUserModule(true);
                    }
                    if (lynxModule.isUserModule() && lynxModule.isSystemSynthetic() && !lynxModule2.isSystemSynthetic()) {
                        lynxModule2.setSystemSynthetic(true);
                    }
                    if (lynxModule.isParent() != lynxModule2.isParent()) {
                        RobotLog.m67ww(TAG, "addConfiguredModule(): The active configuration file may be incorrect about whether Expansion Hub %d is the parent", Integer.valueOf(lynxModule.getModuleAddress()));
                    }
                    z = false;
                }
            }
            if (z) {
                try {
                    lynxModule.pingAndQueryKnownInterfacesAndEtc();
                } catch (RobotCoreException | InterruptedException | RuntimeException e) {
                    RobotLog.logExceptionHeader(TAG, e, "addConfiguredModule() module#=%d", Integer.valueOf(lynxModule.getModuleAddress()));
                    RobotLog.m49ee(TAG, "Unable to communicate with REV Hub #%d at robot startup. A Robot Restart will be required to use this hub.", Integer.valueOf(lynxModule.getModuleAddress()));
                    lynxModule.close();
                    synchronized (this.knownModules) {
                        this.knownModules.remove(Integer.valueOf(lynxModule.getModuleAddress()));
                        throw e;
                    }
                }
            }
        }
        return lynxModule2;
    }

    public LynxModule getConfiguredModule(int i) {
        LynxModule lynxModule;
        synchronized (this.knownModules) {
            lynxModule = this.knownModules.get(Integer.valueOf(i));
        }
        return lynxModule;
    }

    public void removeConfiguredModule(LynxModule lynxModule) {
        synchronized (this.knownModules) {
            if (lynxModule.getModuleAddress() != 0 && this.knownModules.remove(Integer.valueOf(lynxModule.getModuleAddress())) == null) {
                RobotLog.m49ee(TAG, "removeConfiguredModule(): mod#=%d wasn't there", Integer.valueOf(lynxModule.getModuleAddress()));
            }
        }
    }

    public void performSystemOperationOnConnectedModule(int i, boolean z, Consumer<LynxModule> consumer) throws RobotCoreException, InterruptedException {
        synchronized (this.systemOperationLock) {
            LynxModule addConfiguredModule = addConfiguredModule(new LynxModule(this, i, z, false));
            if (consumer != null) {
                try {
                    consumer.accept(addConfiguredModule);
                } catch (Throwable th) {
                    if (!addConfiguredModule.isUserModule) {
                        addConfiguredModule.close();
                    }
                    throw th;
                }
            }
            if (!addConfiguredModule.isUserModule) {
                addConfiguredModule.close();
            }
        }
    }

    public LynxModuleMetaList discoverModules(boolean z) throws RobotCoreException, InterruptedException {
        RobotLog.m60vv(TAG, "lynx discovery beginning...transmitting LynxDiscoveryCommand()...");
        this.discoveredModules.clear();
        LynxModule lynxModule = new LynxModule(this, 0, false, false);
        try {
            new LynxDiscoveryCommand(lynxModule).send();
            long j = (((long) 254) * 3000000) + 50000000 + 200000000;
            long j2 = j / ElapsedTime.MILLIS_IN_NANO;
            Long.signum(j2);
            long j3 = j - (ElapsedTime.MILLIS_IN_NANO * j2);
            RobotLog.m61vv(TAG, "discovery waiting %dms and %dns", Long.valueOf(j2), Long.valueOf(j3));
            Thread.sleep(j2, (int) j3);
            RobotLog.m61vv(TAG, "discovery waiting complete: #modules=%d", Integer.valueOf(this.discoveredModules.size()));
            if (z) {
                RobotLog.m60vv(TAG, "Checking if discovered modules have onboard IMUs");
                for (final LynxModuleMeta next : this.discoveredModules.values()) {
                    performSystemOperationOnConnectedModule(next.getModuleAddress(), next.isParent(), new Consumer<LynxModule>() {
                        public void accept(LynxModule lynxModule) {
                            LynxI2cDeviceSynch createLynxI2cDeviceSynch = LynxFirmwareVersionManager.createLynxI2cDeviceSynch(AppUtil.getDefContext(), lynxModule, 0);
                            LynxModuleMeta.ImuType imuType = LynxModuleMeta.ImuType.NONE;
                            if (LynxUsbDeviceImpl.this.serialNumber.isEmbedded() && lynxModule.isParent() && BHI260IMU.imuIsPresent(createLynxI2cDeviceSynch)) {
                                imuType = LynxModuleMeta.ImuType.BHI260;
                            }
                            if (imuType == LynxModuleMeta.ImuType.NONE) {
                                createLynxI2cDeviceSynch.setI2cAddress(BNO055IMU.I2CADDR_DEFAULT);
                                if (BNO055IMUImpl.imuIsPresent(createLynxI2cDeviceSynch, false)) {
                                    imuType = LynxModuleMeta.ImuType.BNO055;
                                }
                            }
                            next.setImuType(imuType);
                        }
                    });
                }
            }
            lynxModule.close();
            LynxModuleMetaList lynxModuleMetaList = new LynxModuleMetaList(this.serialNumber, this.discoveredModules.values());
            RobotLog.m60vv(TAG, "...lynx discovery completed");
            return lynxModuleMetaList;
        } catch (LynxNackException e) {
            throw e.wrap();
        } catch (Throwable th) {
            lynxModule.close();
            throw th;
        }
    }

    public boolean setupControlHubEmbeddedModule() throws InterruptedException, RobotCoreException {
        if (!getSerialNumber().isEmbedded()) {
            RobotLog.m66ww(TAG, "setupControlHubEmbeddedModule() called on non-embedded USB device");
            return false;
        }
        try {
            performSystemOperationOnConnectedModule(173, true, (Consumer<LynxModule>) null);
            RobotLog.m60vv(TAG, "Verified that the embedded Control Hub module has the correct address");
            return false;
        } catch (RobotCoreException unused) {
            return handleEmbeddedModuleNotFoundAtExpectedAddress();
        }
    }

    private boolean handleEmbeddedModuleNotFoundAtExpectedAddress() throws RobotCoreException, InterruptedException {
        RobotLog.m67ww(TAG, "Unable to find embedded Control Hub module at address %d. Attempting to resolve automatically.", 173);
        LynxModuleMeta parent = discoverModules(false).getParent();
        if (parent == null) {
            RobotLog.m48ee(TAG, "Unable to communicate with internal Expansion Hub. Attempting to re-flash firmware.");
            autoReflashControlHubFirmware();
            parent = discoverModules(false).getParent();
            if (parent == null) {
                RobotLog.setGlobalErrorMsg(AppUtil.getDefContext().getString(C0660R.string.controlHubNotAbleToCommunicateWithInternalHub));
                return false;
            }
            RobotLog.m54ii(TAG, "Successfully un-bricked the Control Hub's embedded module");
            if (parent.getModuleAddress() == 173) {
                RobotLog.m54ii(TAG, "The embedded module already has the correct address");
                return false;
            }
        }
        setControlHubModuleAddress(parent);
        return true;
    }

    private void autoReflashControlHubFirmware() {
        updateFirmware(IncludedFirmwareFileInfo.FW_IMAGE, "autoFirmwareUpdate", new Consumer<ProgressParameters>() {
            public void accept(ProgressParameters progressParameters) {
                AppAliveNotifier.getInstance().notifyAppAlive();
            }
        });
        resetDevice(this.robotUsbDevice);
    }

    private void setControlHubModuleAddress(LynxModuleMeta lynxModuleMeta) throws InterruptedException, RobotCoreException {
        int moduleAddress = lynxModuleMeta.getModuleAddress();
        RobotLog.m61vv(TAG, "Found embedded module at address %d", Integer.valueOf(moduleAddress));
        performSystemOperationOnConnectedModule(moduleAddress, true, new Consumer<LynxModule>() {
            public void accept(LynxModule lynxModule) {
                RobotLog.m61vv(LynxUsbDeviceImpl.TAG, "Setting embedded module address to %d", 173);
                lynxModule.setNewModuleAddress(173);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onLynxDiscoveryResponseReceived(LynxDatagram lynxDatagram) {
        LynxDiscoveryResponse lynxDiscoveryResponse = new LynxDiscoveryResponse();
        lynxDiscoveryResponse.setSerialization(lynxDatagram);
        lynxDiscoveryResponse.loadFromSerialization();
        RobotLog.m61vv(TAG, "onLynxDiscoveryResponseReceived()... module#=%d isParent=%s", Integer.valueOf(lynxDiscoveryResponse.getDiscoveredModuleAddress()), Boolean.toString(lynxDiscoveryResponse.isParent()));
        try {
            synchronized (this.discoveredModules) {
                if (!this.discoveredModules.containsKey(Integer.valueOf(lynxDatagram.getSourceModuleAddress()))) {
                    RobotLog.m61vv(TAG, "discovered lynx module#=%d isParent=%s", Integer.valueOf(lynxDiscoveryResponse.getDiscoveredModuleAddress()), Boolean.toString(lynxDiscoveryResponse.isParent()));
                    LynxModuleMeta lynxModuleMeta = new LynxModuleMeta(lynxDiscoveryResponse.getDiscoveredModuleAddress(), lynxDiscoveryResponse.isParent());
                    this.discoveredModules.put(Integer.valueOf(lynxModuleMeta.getModuleAddress()), lynxModuleMeta);
                }
            }
            RobotLog.m60vv(TAG, "...onLynxDiscoveryResponseReceived()");
        } catch (Throwable th) {
            RobotLog.m60vv(TAG, "...onLynxDiscoveryResponseReceived()");
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void pingAndQueryKnownInterfaces() throws RobotCoreException, InterruptedException {
        for (LynxModule next : getKnownModules()) {
            if (next.isParent()) {
                next.pingAndQueryKnownInterfacesAndEtc();
            }
        }
        for (LynxModule next2 : getKnownModules()) {
            if (!next2.isParent()) {
                next2.pingAndQueryKnownInterfacesAndEtc();
            }
        }
    }

    public void lockNetworkLockAcquisitions() {
        this.networkTransmissionLock.lockAcquisitions();
    }

    public void setThrowOnNetworkLockAcquisition(boolean z) {
        this.networkTransmissionLock.throwOnLockAcquisitions(z);
    }

    /* access modifiers changed from: protected */
    public void resetNetworkTransmissionLock() throws InterruptedException {
        this.networkTransmissionLock.reset();
    }

    public void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        this.networkTransmissionLock.acquire(lynxMessage);
    }

    public void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        this.networkTransmissionLock.release(lynxMessage);
    }

    /* access modifiers changed from: protected */
    public void startPollingForIncomingDatagrams() {
        if (this.incomingDatagramPoller == null) {
            ExecutorService newSingleThreadExecutor = ThreadPool.newSingleThreadExecutor("lynx dg poller");
            this.incomingDatagramPoller = newSingleThreadExecutor;
            newSingleThreadExecutor.execute(new IncomingDatagramPoller());
        }
    }

    /* access modifiers changed from: protected */
    public boolean stopPollingForIncomingDatagrams() {
        boolean z = this.incomingDatagramPoller != null;
        if (this.robotUsbDevice != null) {
            this.robotUsbDevice.requestReadInterrupt(true);
        }
        if (this.incomingDatagramPoller != null) {
            RobotLog.m60vv(TAG, "shutting down incoming datagrams");
            this.incomingDatagramPoller.shutdownNow();
            ThreadPool.awaitTerminationOrExitApplication(this.incomingDatagramPoller, 5, TimeUnit.SECONDS, "Lynx incoming datagram poller", "internal error");
            this.incomingDatagramPoller = null;
        }
        if (this.robotUsbDevice != null) {
            this.robotUsbDevice.requestReadInterrupt(false);
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public void startRegularPinging() {
        for (LynxModule startPingTimer : getKnownModules()) {
            startPingTimer.startPingTimer();
        }
    }

    /* access modifiers changed from: package-private */
    public void stopRegularPinging() {
        for (LynxModule stopPingTimer : getKnownModules()) {
            stopPingTimer.stopPingTimer(true);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:30:0x008d, code lost:
        r8.noteHasBeenTransmitted();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0090, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void transmit(com.qualcomm.hardware.lynx.commands.LynxMessage r8) throws java.lang.InterruptedException {
        /*
            r7 = this;
            java.lang.Object r0 = r7.engageLock
            monitor-enter(r0)
            boolean r1 = r7.isArmedOrArming()     // Catch:{ all -> 0x0091 }
            if (r1 == 0) goto L_0x0089
            boolean r1 = r7.hasShutdownAbnormally()     // Catch:{ all -> 0x0091 }
            if (r1 != 0) goto L_0x0089
            boolean r1 = r7.isEngaged     // Catch:{ all -> 0x0091 }
            if (r1 == 0) goto L_0x0089
            com.qualcomm.hardware.lynx.commands.LynxDatagram r1 = r8.getSerialization()     // Catch:{ all -> 0x0091 }
            if (r1 == 0) goto L_0x0085
            boolean r2 = DEBUG_LOG_DATAGRAMS     // Catch:{ all -> 0x0091 }
            if (r2 != 0) goto L_0x0021
            boolean r2 = DEBUG_LOG_MESSAGES     // Catch:{ all -> 0x0091 }
            if (r2 == 0) goto L_0x0062
        L_0x0021:
            java.lang.String r2 = "LynxUsb"
            java.lang.String r3 = "xmit'ing: mod=%d cmd=0x%02x(%s) msg#=%d ref#=%d "
            r4 = 5
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ all -> 0x0091 }
            r5 = 0
            int r6 = r8.getModuleAddress()     // Catch:{ all -> 0x0091 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0091 }
            r4[r5] = r6     // Catch:{ all -> 0x0091 }
            r5 = 1
            int r6 = r8.getCommandNumber()     // Catch:{ all -> 0x0091 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0091 }
            r4[r5] = r6     // Catch:{ all -> 0x0091 }
            r5 = 2
            java.lang.Class r6 = r8.getClass()     // Catch:{ all -> 0x0091 }
            java.lang.String r6 = r6.getSimpleName()     // Catch:{ all -> 0x0091 }
            r4[r5] = r6     // Catch:{ all -> 0x0091 }
            r5 = 3
            int r6 = r8.getMessageNumber()     // Catch:{ all -> 0x0091 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0091 }
            r4[r5] = r6     // Catch:{ all -> 0x0091 }
            r5 = 4
            int r6 = r8.getReferenceNumber()     // Catch:{ all -> 0x0091 }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0091 }
            r4[r5] = r6     // Catch:{ all -> 0x0091 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r2, (java.lang.String) r3, (java.lang.Object[]) r4)     // Catch:{ all -> 0x0091 }
        L_0x0062:
            byte[] r1 = r1.toByteArray()     // Catch:{ all -> 0x0091 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r2 = r7.robotUsbDevice     // Catch:{ RobotUsbException -> 0x0078, RuntimeException -> 0x0076 }
            r2.write(r1)     // Catch:{ RobotUsbException -> 0x0078, RuntimeException -> 0x0076 }
            long r1 = java.lang.System.nanoTime()     // Catch:{ all -> 0x0091 }
            r8.setNanotimeLastTransmit(r1)     // Catch:{ all -> 0x0091 }
            r8.resetModulePingTimer()     // Catch:{ all -> 0x0091 }
            goto L_0x008c
        L_0x0076:
            r8 = move-exception
            goto L_0x0079
        L_0x0078:
            r8 = move-exception
        L_0x0079:
            r7.shutdownAbnormally()     // Catch:{ all -> 0x0091 }
            java.lang.String r1 = "LynxUsb"
            java.lang.String r2 = "exception thrown in LynxUsbDevice.transmit"
            com.qualcomm.robotcore.util.RobotLog.m50ee((java.lang.String) r1, (java.lang.Throwable) r8, (java.lang.String) r2)     // Catch:{ all -> 0x0091 }
            monitor-exit(r0)     // Catch:{ all -> 0x0091 }
            return
        L_0x0085:
            r8.onPretendTransmit()     // Catch:{ all -> 0x0091 }
            goto L_0x008c
        L_0x0089:
            r8.onPretendTransmit()     // Catch:{ all -> 0x0091 }
        L_0x008c:
            monitor-exit(r0)     // Catch:{ all -> 0x0091 }
            r8.noteHasBeenTransmitted()
            return
        L_0x0091:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0091 }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxUsbDeviceImpl.transmit(com.qualcomm.hardware.lynx.commands.LynxMessage):void");
    }

    /* access modifiers changed from: protected */
    public void shutdownAbnormally() {
        this.hasShutdownAbnormally = true;
        setGlobalWarning(String.format(this.context.getString(this.robotUsbDevice.isAttached() ? C0660R.string.warningProblemCommunicatingWithUSBDevice : C0660R.string.warningUSBDeviceDetached), new Object[]{HardwareFactory.getDeviceDisplayName(this.context, this.serialNumber)}));
    }

    /* access modifiers changed from: protected */
    public void pretendFinishExtantCommands() throws InterruptedException {
        for (LynxModule pretendFinishExtantCommands : getKnownModules()) {
            pretendFinishExtantCommands.pretendFinishExtantCommands();
        }
    }

    /* access modifiers changed from: protected */
    public void abandonUnfinishedCommands() {
        for (LynxModule abandonUnfinishedCommands : getKnownModules()) {
            abandonUnfinishedCommands.abandonUnfinishedCommands();
        }
    }

    class IncomingDatagramPoller implements Runnable {
        boolean isSynchronized = false;
        byte[] prefix = new byte[4];
        byte[] scratch = new byte[2];
        boolean stopRequested = false;

        IncomingDatagramPoller() {
        }

        public void run() {
            ThreadPool.logThreadLifeCycle("lynx incoming datagrams", new Runnable() {
                public void run() {
                    Thread.currentThread().setPriority(6);
                    while (!IncomingDatagramPoller.this.stopRequested && !Thread.currentThread().isInterrupted() && !LynxUsbDeviceImpl.this.hasShutdownAbnormally()) {
                        LynxDatagram pollForIncomingDatagram = IncomingDatagramPoller.this.pollForIncomingDatagram();
                        if (pollForIncomingDatagram != null) {
                            if (pollForIncomingDatagram.getPacketId() == LynxDiscoveryResponse.getStandardCommandNumber()) {
                                LynxUsbDeviceImpl.this.onLynxDiscoveryResponseReceived(pollForIncomingDatagram);
                            } else {
                                LynxModule findKnownModule = LynxUsbDeviceImpl.this.findKnownModule(pollForIncomingDatagram.getSourceModuleAddress());
                                if (findKnownModule != null) {
                                    findKnownModule.onIncomingDatagramReceived(pollForIncomingDatagram);
                                }
                            }
                        }
                    }
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void readIncomingBytes(byte[] bArr, int i, TimeWindow timeWindow) throws InterruptedException, RobotUsbException {
            int read = LynxUsbDeviceImpl.this.robotUsbDevice.read(bArr, 0, i, 2147483647L, timeWindow);
            if (read != i) {
                if (read == 0) {
                    RobotLog.m49ee(LynxUsbDeviceImpl.TAG, "readIncomingBytes() cbToRead=%d cbRead=%d: throwing InterruptedException", Integer.valueOf(i), Integer.valueOf(read));
                    throw new InterruptedException("interrupt during robotUsbDevice.read()");
                }
                RobotLog.m49ee(LynxUsbDeviceImpl.TAG, "readIncomingBytes() cbToRead=%d cbRead=%d: throwing RobotCoreException", Integer.valueOf(i), Integer.valueOf(read));
                throw new RobotUsbUnspecifiedException("readIncomingBytes() cbToRead=%d cbRead=%d", Integer.valueOf(i), Integer.valueOf(read));
            }
        }

        /* access modifiers changed from: package-private */
        public byte readSingleByte(byte[] bArr) throws InterruptedException, RobotUsbException {
            readIncomingBytes(bArr, 1, (TimeWindow) null);
            return bArr[0];
        }

        /* access modifiers changed from: package-private */
        public LynxDatagram pollForIncomingDatagram() {
            while (!this.stopRequested && !Thread.currentThread().isInterrupted() && !LynxUsbDeviceImpl.this.hasShutdownAbnormally()) {
                try {
                    if (this.isSynchronized) {
                        readIncomingBytes(this.prefix, 4, (TimeWindow) null);
                        if (!LynxDatagram.beginsWithFraming(this.prefix)) {
                            RobotLog.m61vv(LynxUsbDeviceImpl.TAG, "synchronization lost: serial=%s", LynxUsbDeviceImpl.this.serialNumber);
                            this.isSynchronized = false;
                        }
                    } else if (readSingleByte(this.scratch) == LynxDatagram.frameBytes[0]) {
                        if (readSingleByte(this.scratch) == LynxDatagram.frameBytes[1]) {
                            readIncomingBytes(this.scratch, 2, (TimeWindow) null);
                            System.arraycopy(LynxDatagram.frameBytes, 0, this.prefix, 0, 2);
                            System.arraycopy(this.scratch, 0, this.prefix, 2, 2);
                            RobotLog.m61vv(LynxUsbDeviceImpl.TAG, "synchronization gained: serial=%s", LynxUsbDeviceImpl.this.serialNumber);
                            this.isSynchronized = true;
                        }
                    }
                    int unsignedShortToInt = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(this.prefix, 2, LynxDatagram.LYNX_ENDIAN)) - 4;
                    byte[] bArr = new byte[unsignedShortToInt];
                    TimeWindow timeWindow = new TimeWindow();
                    readIncomingBytes(bArr, unsignedShortToInt, timeWindow);
                    byte[] concatenateByteArrays = Util.concatenateByteArrays(this.prefix, bArr);
                    LynxDatagram lynxDatagram = new LynxDatagram();
                    lynxDatagram.setPayloadTimeWindow(timeWindow);
                    lynxDatagram.fromByteArray(concatenateByteArrays);
                    if (lynxDatagram.isChecksumValid()) {
                        if (LynxUsbDeviceImpl.DEBUG_LOG_DATAGRAMS) {
                            RobotLog.m61vv(LynxUsbDeviceImpl.TAG, "rec'd: mod=%d cmd=0x%02x msg#=%d ref#=%d ", Integer.valueOf(lynxDatagram.getSourceModuleAddress()), Integer.valueOf(lynxDatagram.getPacketId()), Integer.valueOf(lynxDatagram.getMessageNumber()), Integer.valueOf(lynxDatagram.getReferenceNumber()));
                        }
                        return lynxDatagram;
                    }
                    RobotLog.m48ee(LynxUsbDeviceImpl.TAG, "invalid checksum received; message ignored");
                } catch (RuntimeException | RobotUsbDeviceClosedException | RobotUsbFTDIException unused) {
                    RobotLog.m60vv(LynxUsbDeviceImpl.TAG, "device closed in incoming datagram loop");
                    LynxUsbDeviceImpl.this.shutdownAbnormally();
                    LynxUsbDeviceImpl.this.robotUsbDevice.close();
                    try {
                        LynxUsbDeviceImpl.this.pretendFinishExtantCommands();
                    } catch (InterruptedException unused2) {
                        this.stopRequested = true;
                    }
                } catch (RobotCoreException | RobotUsbException e) {
                    RobotLog.m62vv(LynxUsbDeviceImpl.TAG, e, "exception thrown in incoming datagram loop; ignored");
                } catch (InterruptedException unused3) {
                    this.stopRequested = true;
                }
            }
            return null;
        }
    }

    protected static RobotUsbDeviceFtdi accessCBus(RobotUsbDevice robotUsbDevice) {
        if (robotUsbDevice instanceof RobotUsbDeviceFtdi) {
            RobotUsbDeviceFtdi robotUsbDeviceFtdi = (RobotUsbDeviceFtdi) robotUsbDevice;
            if (robotUsbDeviceFtdi.supportsCbusBitbang()) {
                return robotUsbDeviceFtdi;
            }
        }
        RobotLog.m48ee(TAG, "accessCBus() unexpectedly failed; ignoring");
        return null;
    }

    public static void resetDevice(RobotUsbDevice robotUsbDevice) {
        RobotLog.m61vv(TAG, "resetDevice() serial=%s", robotUsbDevice.getSerialNumber());
        try {
            if (LynxConstants.isEmbeddedSerialNumber(robotUsbDevice.getSerialNumber())) {
                boolean state = AndroidBoard.getInstance().getAndroidBoardIsPresentPin().getState();
                RobotLog.m61vv(LynxModule.TAG, "resetting embedded usb device: isPresent: was=%s", Boolean.valueOf(state));
                if (!state) {
                    AndroidBoard.getInstance().getAndroidBoardIsPresentPin().setState(true);
                    Thread.sleep((long) 75);
                }
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(true);
                long j = (long) 75;
                Thread.sleep(j);
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(false);
                Thread.sleep(j);
            } else {
                RobotUsbDeviceFtdi accessCBus = accessCBus(robotUsbDevice);
                if (accessCBus != null) {
                    accessCBus.cbus_setup(3, 3);
                    long j2 = (long) 75;
                    Thread.sleep(j2);
                    accessCBus.cbus_write(2);
                    Thread.sleep(j2);
                    accessCBus.cbus_write(3);
                }
            }
            Thread.sleep(200);
        } catch (InterruptedException | RobotUsbException e) {
            exceptionHandler.handleException(e);
        }
    }

    public RobotCoreCommandList.LynxFirmwareUpdateResp updateFirmware(RobotCoreCommandList.FWImage fWImage, String str, Consumer<ProgressParameters> consumer) {
        return this.lynxFirmwareUpdater.updateFirmware(fWImage, str, consumer);
    }
}
