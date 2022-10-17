package com.qualcomm.hardware.lynx;

import android.graphics.Color;
import androidx.core.view.ViewCompat;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterface;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.hardware.lynx.commands.LynxRespondable;
import com.qualcomm.hardware.lynx.commands.LynxResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxDekaInterfaceCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxFtdiResetControlCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxPhoneChargeControlCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxPhoneChargeQueryCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxPhoneChargeQueryResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxReadVersionStringCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxReadVersionStringResponse;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.hardware.lynx.commands.standard.LynxDiscoveryCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxFailSafeCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxGetModuleLEDColorCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxGetModuleStatusCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxGetModuleStatusResponse;
import com.qualcomm.hardware.lynx.commands.standard.LynxKeepAliveCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxNack;
import com.qualcomm.hardware.lynx.commands.standard.LynxQueryInterfaceCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetDebugLogLevelCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetModuleLEDColorCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetModuleLEDPatternCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxSetNewModuleAddressCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxStandardCommand;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import com.qualcomm.robotcore.hardware.RobotConfigNameable;
import com.qualcomm.robotcore.hardware.VisuallyIdentifiableHardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.TypeConversion;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Deque;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.RejectedExecutionException;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.TempUnit;
import org.firstinspires.ftc.robotcore.external.navigation.VoltageUnit;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.Misc;
import org.firstinspires.ftc.robotcore.internal.usb.LynxModuleSerialNumber;
import org.firstinspires.inspection.InspectionState;

public class LynxModule extends LynxCommExceptionHandler implements LynxModuleIntf, RobotArmingStateNotifier, RobotArmingStateNotifier.Callback, Blinker, VisuallyIdentifiableHardwareDevice {
    public static final String TAG = "LynxModule";
    public static BlinkerPolicy blinkerPolicy = new CountModuleAddressBlinkerPolicy();
    protected static final int msInitialContact = 500;
    protected static final int msKeepAliveTimeout = 2500;
    protected static Map<Class<? extends LynxCommand>, MessageClassAndCtor> responseClasses = new HashMap();
    protected static Map<Integer, MessageClassAndCtor> standardMessages = new HashMap();
    protected Future<?> attentionRequiredFuture;
    protected Map<String, List<LynxDekaInterfaceCommand<?>>> bulkCachingHistory;
    protected final Object bulkCachingLock;
    protected BulkCachingMode bulkCachingMode;
    protected final ConcurrentHashMap<Integer, MessageClassAndCtor> commandClasses;
    protected List<LynxController> controllers;
    protected ArrayList<Blinker.Step> currentSteps;
    protected final Object engagementLock = this;
    protected ScheduledExecutorService executor;
    protected boolean ftdiResetWatchdogActive;
    protected boolean ftdiResetWatchdogActiveWhenEngaged;
    protected final Object futureLock;
    protected final Object i2cLock;
    protected final ConcurrentHashMap<String, LynxInterface> interfacesQueried;
    protected boolean isEngaged;
    protected volatile boolean isNotResponding = false;
    protected volatile boolean isOpen;
    protected boolean isParent;
    protected volatile boolean isSystemSynthetic;
    protected volatile boolean isUserModule;
    protected boolean isVisuallyIdentifying;
    protected BulkData lastBulkData;
    protected LynxUsbDevice lynxUsbDevice;
    protected int moduleAddress;
    protected SerialNumber moduleSerialNumber;
    protected AtomicInteger nextMessageNumber;
    protected Future<?> pingFuture;
    protected Deque<ArrayList<Blinker.Step>> previousSteps;
    protected final Object startStopLock;
    protected final Set<Class<? extends LynxCommand>> supportedCommands;
    protected final ConcurrentHashMap<Integer, LynxRespondable> unfinishedCommands;

    public interface BlinkerPolicy {
        List<Blinker.Step> getIdlePattern(LynxModule lynxModule);

        List<Blinker.Step> getVisuallyIdentifyPattern(LynxModule lynxModule);
    }

    public enum BulkCachingMode {
        OFF,
        MANUAL,
        AUTO
    }

    public int getBlinkerPatternMaxLength() {
        return 16;
    }

    /* access modifiers changed from: protected */
    public int getMsModulePingInterval() {
        return 1950;
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public int getVersion() {
        return 1;
    }

    static {
        addStandardMessage(LynxAck.class);
        addStandardMessage(LynxNack.class);
        addStandardMessage(LynxKeepAliveCommand.class);
        addStandardMessage(LynxGetModuleStatusCommand.class);
        addStandardMessage(LynxFailSafeCommand.class);
        addStandardMessage(LynxSetNewModuleAddressCommand.class);
        addStandardMessage(LynxQueryInterfaceCommand.class);
        addStandardMessage(LynxSetNewModuleAddressCommand.class);
        addStandardMessage(LynxSetModuleLEDColorCommand.class);
        addStandardMessage(LynxGetModuleLEDColorCommand.class);
        correlateStandardResponse(LynxGetModuleStatusCommand.class);
        correlateStandardResponse(LynxQueryInterfaceCommand.class);
        correlateStandardResponse(LynxGetModuleLEDColorCommand.class);
    }

    protected static class MessageClassAndCtor {
        public Class<? extends LynxMessage> clazz;
        public Constructor<? extends LynxMessage> ctor;

        protected MessageClassAndCtor() {
        }

        /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0011 */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public void assignCtor() throws java.lang.NoSuchMethodException {
            /*
                r5 = this;
                r0 = 0
                r1 = 1
                java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxMessage> r2 = r5.clazz     // Catch:{ NoSuchMethodException -> 0x0011 }
                java.lang.Class[] r3 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0011 }
                java.lang.Class<com.qualcomm.hardware.lynx.LynxModule> r4 = com.qualcomm.hardware.lynx.LynxModule.class
                r3[r0] = r4     // Catch:{ NoSuchMethodException -> 0x0011 }
                java.lang.reflect.Constructor r2 = r2.getConstructor(r3)     // Catch:{ NoSuchMethodException -> 0x0011 }
                r5.ctor = r2     // Catch:{ NoSuchMethodException -> 0x0011 }
                goto L_0x0023
            L_0x0011:
                java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxMessage> r2 = r5.clazz     // Catch:{ NoSuchMethodException -> 0x0020 }
                java.lang.Class[] r1 = new java.lang.Class[r1]     // Catch:{ NoSuchMethodException -> 0x0020 }
                java.lang.Class<com.qualcomm.hardware.lynx.LynxModuleIntf> r3 = com.qualcomm.hardware.lynx.LynxModuleIntf.class
                r1[r0] = r3     // Catch:{ NoSuchMethodException -> 0x0020 }
                java.lang.reflect.Constructor r0 = r2.getConstructor(r1)     // Catch:{ NoSuchMethodException -> 0x0020 }
                r5.ctor = r0     // Catch:{ NoSuchMethodException -> 0x0020 }
                goto L_0x0023
            L_0x0020:
                r0 = 0
                r5.ctor = r0
            L_0x0023:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxModule.MessageClassAndCtor.assignCtor():void");
        }
    }

    protected static void addStandardMessage(Class<? extends LynxMessage> cls) {
        try {
            Integer num = (Integer) LynxMessage.invokeStaticNullaryMethod(cls, "getStandardCommandNumber");
            Assert.assertTrue((num.intValue() & 32768) == 0);
            MessageClassAndCtor messageClassAndCtor = new MessageClassAndCtor();
            messageClassAndCtor.clazz = cls;
            messageClassAndCtor.assignCtor();
            standardMessages.put(num, messageClassAndCtor);
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            RobotLog.m49ee(TAG, "error registering %s", cls.getSimpleName());
        }
    }

    protected static void correlateStandardResponse(Class<? extends LynxCommand> cls) {
        try {
            correlateResponse(cls, LynxCommand.getResponseClass(cls));
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException unused) {
            RobotLog.m49ee(TAG, "error registering response to %s", cls.getSimpleName());
        }
    }

    public static void correlateResponse(Class<? extends LynxCommand> cls, Class<? extends LynxResponse> cls2) throws NoSuchMethodException {
        MessageClassAndCtor messageClassAndCtor = new MessageClassAndCtor();
        messageClassAndCtor.clazz = cls2;
        messageClassAndCtor.assignCtor();
        responseClasses.put(cls, messageClassAndCtor);
    }

    public LynxModule(LynxUsbDevice lynxUsbDevice2, int i, boolean z, boolean z2) {
        this.lynxUsbDevice = lynxUsbDevice2;
        this.controllers = new CopyOnWriteArrayList();
        this.moduleAddress = i;
        this.moduleSerialNumber = new LynxModuleSerialNumber(lynxUsbDevice2.getSerialNumber(), i);
        this.isParent = z;
        this.isSystemSynthetic = false;
        this.isEngaged = true;
        this.isUserModule = z2;
        this.isOpen = true;
        this.startStopLock = new Object();
        this.nextMessageNumber = new AtomicInteger(0);
        ConcurrentHashMap<Integer, MessageClassAndCtor> concurrentHashMap = new ConcurrentHashMap<>(standardMessages);
        this.commandClasses = concurrentHashMap;
        this.supportedCommands = new HashSet();
        for (MessageClassAndCtor next : concurrentHashMap.values()) {
            if (ClassUtil.inheritsFrom(next.clazz, LynxCommand.class)) {
                this.supportedCommands.add(next.clazz);
            }
        }
        this.interfacesQueried = new ConcurrentHashMap<>();
        this.unfinishedCommands = new ConcurrentHashMap<>();
        this.i2cLock = new Object();
        this.currentSteps = new ArrayList<>();
        this.previousSteps = new ArrayDeque();
        this.isVisuallyIdentifying = false;
        this.executor = null;
        this.pingFuture = null;
        this.attentionRequiredFuture = null;
        this.futureLock = new Object();
        this.ftdiResetWatchdogActive = false;
        this.ftdiResetWatchdogActiveWhenEngaged = false;
        this.bulkCachingMode = BulkCachingMode.OFF;
        this.bulkCachingHistory = new HashMap();
        this.bulkCachingLock = new Object();
        startExecutor();
        this.lynxUsbDevice.registerCallback(this, false);
    }

    public String toString() {
        return Misc.formatForUser("LynxModule(mod#=%d, serial=%s)", Integer.valueOf(this.moduleAddress), getSerialNumber());
    }

    public void close() {
        synchronized (this.startStopLock) {
            if (this.isOpen) {
                stopFtdiResetWatchdog();
                this.isOpen = false;
                RobotLog.m61vv(TAG, "close(#%d)", Integer.valueOf(this.moduleAddress));
                unregisterCallback(this);
                this.lynxUsbDevice.removeConfiguredModule(this);
                stopAttentionRequired();
                stopPingTimer(true);
                stopExecutor();
            }
        }
    }

    public boolean isOpen() {
        return this.isOpen;
    }

    public boolean isUserModule() {
        return this.isUserModule;
    }

    public void setUserModule(boolean z) {
        warnIfClosed();
        this.isUserModule = z;
    }

    public boolean isSystemSynthetic() {
        return this.isSystemSynthetic;
    }

    public void setSystemSynthetic(boolean z) {
        warnIfClosed();
        this.isSystemSynthetic = z;
    }

    public void noteController(LynxController lynxController) {
        warnIfClosed();
        this.controllers.add(lynxController);
    }

    public int getModuleAddress() {
        return this.moduleAddress;
    }

    public void setNewModuleAddress(final int i) {
        warnIfClosed();
        if (i != getModuleAddress()) {
            this.lynxUsbDevice.changeModuleAddress(this, i, new Runnable() {
                public void run() {
                    LynxSetNewModuleAddressCommand lynxSetNewModuleAddressCommand;
                    try {
                        lynxSetNewModuleAddressCommand = new LynxSetNewModuleAddressCommand(LynxModule.this, (byte) i);
                        lynxSetNewModuleAddressCommand.acquireNetworkLock();
                        lynxSetNewModuleAddressCommand.send();
                        LynxModule.this.moduleAddress = i;
                        lynxSetNewModuleAddressCommand.releaseNetworkLock();
                    } catch (LynxNackException | InterruptedException e) {
                        LynxModule.this.handleException(e);
                    } catch (Throwable th) {
                        lynxSetNewModuleAddressCommand.releaseNetworkLock();
                        throw th;
                    }
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public byte getNewMessageNumber() {
        while (true) {
            byte andIncrement = (byte) this.nextMessageNumber.getAndIncrement();
            int unsignedByteToInt = TypeConversion.unsignedByteToInt(andIncrement);
            if (andIncrement != 0 && !this.unfinishedCommands.containsKey(Integer.valueOf(unsignedByteToInt))) {
                return andIncrement;
            }
        }
    }

    public void noteAttentionRequired() {
        warnIfClosed();
        if (isUserModule()) {
            synchronized (this.futureLock) {
                if (this.isOpen) {
                    Future<?> future = this.attentionRequiredFuture;
                    if (future != null) {
                        future.cancel(false);
                    }
                    this.attentionRequiredFuture = this.executor.submit(new Runnable() {
                        public void run() {
                            if (LynxModule.this.isOpen) {
                                LynxModule.this.sendGetModuleStatusAndProcessResponse(true);
                            }
                        }
                    });
                }
            }
            forgetLastKnown();
        }
    }

    /* access modifiers changed from: protected */
    public void noteDatagramReceived() {
        warnIfClosed();
        if (this.isNotResponding) {
            this.isNotResponding = false;
            RobotLog.m61vv(TAG, "REV Hub #%d has reconnected", Integer.valueOf(this.moduleAddress));
        }
    }

    public void noteNotResponding() {
        warnIfClosed();
        this.isNotResponding = true;
    }

    public boolean isNotResponding() {
        warnIfClosed();
        return this.isNotResponding;
    }

    /* access modifiers changed from: protected */
    public void warnIfClosed() {
        if (!isOpen()) {
            RobotLog.m68ww(TAG, (Throwable) new RuntimeException(), "Attempted use of a closed LynxModule instance");
        }
    }

    /* access modifiers changed from: protected */
    public void stopAttentionRequired() {
        synchronized (this.futureLock) {
            Future<?> future = this.attentionRequiredFuture;
            if (future != null) {
                future.cancel(true);
                ThreadPool.awaitFuture(this.attentionRequiredFuture, 250, TimeUnit.MILLISECONDS);
                this.attentionRequiredFuture = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendGetModuleStatusAndProcessResponse(boolean z) {
        try {
            LynxGetModuleStatusResponse lynxGetModuleStatusResponse = (LynxGetModuleStatusResponse) new LynxGetModuleStatusCommand(this, z).sendReceive();
            if (lynxGetModuleStatusResponse.testAnyBits(-21)) {
                RobotLog.m61vv(TAG, "received status: %s", lynxGetModuleStatusResponse.toString());
            }
            if (lynxGetModuleStatusResponse.isKeepAliveTimeout()) {
                resendCurrentPattern();
            }
            if (lynxGetModuleStatusResponse.isDeviceReset()) {
                LynxModuleWarningManager.getInstance().reportModuleReset(this);
                resendCurrentPattern();
            }
            if (lynxGetModuleStatusResponse.isBatteryLow()) {
                LynxModuleWarningManager.getInstance().reportModuleLowBattery(this);
            }
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void forgetLastKnown() {
        for (LynxController forgetLastKnown : this.controllers) {
            forgetLastKnown.forgetLastKnown();
        }
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public String getDeviceName() {
        return String.format("%s (%s)", new Object[]{AppUtil.getDefContext().getString(C0660R.string.expansionHubDisplayName), getFirmwareVersionString()});
    }

    public String getFirmwareVersionString() {
        String nullableFirmwareVersionString = getNullableFirmwareVersionString();
        return nullableFirmwareVersionString == null ? AppUtil.getDefContext().getString(C0705R.string.lynxUnavailableFWVersionString) : nullableFirmwareVersionString;
    }

    public String getNullableFirmwareVersionString() {
        warnIfClosed();
        try {
            return ((LynxReadVersionStringResponse) new LynxReadVersionStringCommand(this).sendReceive()).getNullableVersionString();
        } catch (LynxNackException | InterruptedException e) {
            handleException(e);
            return null;
        }
    }

    public String getConnectionInfo() {
        return String.format("%s; module %d", new Object[]{this.lynxUsbDevice.getConnectionInfo(), Integer.valueOf(getModuleAddress())});
    }

    public void resetDeviceConfigurationForOpMode() {
        warnIfClosed();
        setBulkCachingMode(BulkCachingMode.OFF);
    }

    public List<String> getGlobalWarnings() {
        warnIfClosed();
        ArrayList arrayList = new ArrayList();
        for (LynxController healthStatusWarningMessage : this.controllers) {
            String healthStatusWarningMessage2 = getHealthStatusWarningMessage(healthStatusWarningMessage);
            if (!healthStatusWarningMessage2.isEmpty()) {
                arrayList.add(healthStatusWarningMessage2);
            }
        }
        return arrayList;
    }

    public static String getHealthStatusWarningMessage(HardwareDeviceHealth hardwareDeviceHealth) {
        if (C06904.f86xce28df9d[hardwareDeviceHealth.getHealthStatus().ordinal()] != 1) {
            return InspectionState.NO_VERSION;
        }
        String str = null;
        if ((hardwareDeviceHealth instanceof RobotConfigNameable) && (str = ((RobotConfigNameable) hardwareDeviceHealth).getUserConfiguredName()) != null) {
            str = AppUtil.getDefContext().getString(C0660R.string.quotes, new Object[]{str});
        }
        if (str == null && (hardwareDeviceHealth instanceof HardwareDevice)) {
            HardwareDevice hardwareDevice = (HardwareDevice) hardwareDeviceHealth;
            String deviceName = hardwareDevice.getDeviceName();
            String connectionInfo = hardwareDevice.getConnectionInfo();
            str = AppUtil.getDefContext().getString(C0660R.string.hwDeviceDescriptionAndConnection, new Object[]{deviceName, connectionInfo});
        }
        if (str == null) {
            str = AppUtil.getDefContext().getString(C0660R.string.hwPoorlyNamedDevice);
        }
        return AppUtil.getDefContext().getString(C0660R.string.unhealthyDevice, new Object[]{str});
    }

    public SerialNumber getModuleSerialNumber() {
        return this.moduleSerialNumber;
    }

    public SerialNumber getSerialNumber() {
        return this.lynxUsbDevice.getSerialNumber();
    }

    public RobotArmingStateNotifier.ARMINGSTATE getArmingState() {
        return this.lynxUsbDevice.getArmingState();
    }

    public void registerCallback(RobotArmingStateNotifier.Callback callback, boolean z) {
        this.lynxUsbDevice.registerCallback(callback, z);
    }

    public void unregisterCallback(RobotArmingStateNotifier.Callback callback) {
        this.lynxUsbDevice.unregisterCallback(callback);
    }

    /* renamed from: com.qualcomm.hardware.lynx.LynxModule$4 */
    static /* synthetic */ class C06904 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$HardwareDeviceHealth$HealthStatus */
        static final /* synthetic */ int[] f86xce28df9d;

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$usb$RobotArmingStateNotifier$ARMINGSTATE */
        static final /* synthetic */ int[] f87x44709aa9;

        static {
            int[] iArr = new int[RobotArmingStateNotifier.ARMINGSTATE.values().length];
            f87x44709aa9 = iArr;
            try {
                iArr[RobotArmingStateNotifier.ARMINGSTATE.DISARMED.ordinal()] = 1;
            } catch (NoSuchFieldError unused) {
            }
            int[] iArr2 = new int[HardwareDeviceHealth.HealthStatus.values().length];
            f86xce28df9d = iArr2;
            try {
                iArr2[HardwareDeviceHealth.HealthStatus.UNHEALTHY.ordinal()] = 1;
            } catch (NoSuchFieldError unused2) {
            }
        }
    }

    public void onModuleStateChange(RobotArmingStateNotifier robotArmingStateNotifier, RobotArmingStateNotifier.ARMINGSTATE armingstate) {
        int i = C06904.f87x44709aa9[armingstate.ordinal()];
    }

    public void engage() {
        warnIfClosed();
        synchronized (this.engagementLock) {
            if (!this.isEngaged) {
                RobotLog.m61vv(TAG, "engaging lynx module #%d", Integer.valueOf(getModuleAddress()));
                for (LynxController engage : this.controllers) {
                    engage.engage();
                }
                this.isEngaged = true;
                if (this.ftdiResetWatchdogActiveWhenEngaged) {
                    startFtdiResetWatchdog();
                }
            }
        }
    }

    public void disengage() {
        warnIfClosed();
        synchronized (this.engagementLock) {
            if (this.isEngaged) {
                RobotLog.m61vv(TAG, "disengaging lynx module #%d", Integer.valueOf(getModuleAddress()));
                stopFtdiResetWatchdog(true);
                this.isEngaged = false;
                nackUnfinishedCommands();
                for (LynxController disengage : this.controllers) {
                    disengage.disengage();
                }
                nackUnfinishedCommands();
            }
        }
    }

    public boolean isEngaged() {
        boolean z;
        warnIfClosed();
        synchronized (this.engagementLock) {
            z = this.isEngaged;
        }
        return z;
    }

    public void visuallyIdentify(boolean z) {
        warnIfClosed();
        synchronized (this) {
            boolean z2 = this.isVisuallyIdentifying;
            if (z2 != z) {
                if (!z2) {
                    internalPushPattern(blinkerPolicy.getVisuallyIdentifyPattern(this));
                } else {
                    popPattern();
                }
                this.isVisuallyIdentifying = z;
            }
        }
    }

    public void setConstant(int i) {
        warnIfClosed();
        Blinker.Step step = new Blinker.Step(i, 1, TimeUnit.SECONDS);
        ArrayList arrayList = new ArrayList();
        arrayList.add(step);
        setPattern(arrayList);
    }

    public void stopBlinking() {
        warnIfClosed();
        setConstant(ViewCompat.MEASURED_STATE_MASK);
    }

    public synchronized void setPattern(Collection<Blinker.Step> collection) {
        ArrayList<Blinker.Step> arrayList = collection == null ? new ArrayList<>() : new ArrayList<>(collection);
        this.currentSteps = arrayList;
        sendLEDPatternSteps(arrayList);
    }

    public synchronized Collection<Blinker.Step> getPattern() {
        warnIfClosed();
        return new ArrayList(this.currentSteps);
    }

    /* access modifiers changed from: protected */
    public void resendCurrentPattern() {
        RobotLog.m60vv(TAG, "resendCurrentPattern()");
        sendLEDPatternSteps(this.currentSteps);
    }

    public synchronized void pushPattern(Collection<Blinker.Step> collection) {
        visuallyIdentify(false);
        internalPushPattern(collection);
    }

    /* access modifiers changed from: protected */
    public void internalPushPattern(Collection<Blinker.Step> collection) {
        warnIfClosed();
        this.previousSteps.push(this.currentSteps);
        setPattern(collection);
    }

    public synchronized boolean patternStackNotEmpty() {
        warnIfClosed();
        return this.previousSteps.size() > 0;
    }

    public synchronized boolean popPattern() {
        warnIfClosed();
        try {
            setPattern(this.previousSteps.pop());
        } catch (NoSuchElementException unused) {
            setPattern((Collection<Blinker.Step>) null);
            return false;
        }
        return true;
    }

    /* access modifiers changed from: package-private */
    public void sendLEDPatternSteps(Collection<Blinker.Step> collection) {
        warnIfClosed();
        RobotLog.m61vv(TAG, "sendLEDPatternSteps(): steps=%s", collection);
        ping();
        LynxSetModuleLEDPatternCommand.Steps steps = new LynxSetModuleLEDPatternCommand.Steps();
        for (Blinker.Step add : collection) {
            steps.add(add);
        }
        try {
            new LynxSetModuleLEDPatternCommand(this, steps).sendReceive();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }

    public static class BreathingBlinkerPolicy implements BlinkerPolicy {
        public List<Blinker.Step> getIdlePattern(LynxModule lynxModule) {
            float[] fArr = {0.0f, 0.0f, 0.0f};
            Color.colorToHSV(-16711681, fArr);
            final float f = fArr[0];
            final ArrayList arrayList = new ArrayList();
            C06911 r2 = new Consumer<Integer>() {
                public void accept(Integer num) {
                    arrayList.add(new Blinker.Step(Color.HSVToColor(new float[]{f, 1.0f, 1.0f - ((float) Math.sqrt((double) (1.0f - (((((float) num.intValue()) / 8.0f) * 0.95f) + 0.05f))))}), 125, TimeUnit.MILLISECONDS));
                }
            };
            for (int i = 0; i <= 8; i++) {
                r2.accept(Integer.valueOf(i));
            }
            for (int i2 = 7; i2 > 0; i2--) {
                r2.accept(Integer.valueOf(i2));
            }
            return arrayList;
        }

        public List<Blinker.Step> getVisuallyIdentifyPattern(LynxModule lynxModule) {
            ArrayList arrayList = new ArrayList();
            long j = (long) 150;
            arrayList.add(new Blinker.Step(-16711681, j, TimeUnit.MILLISECONDS));
            long j2 = (long) 75;
            arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, j2, TimeUnit.MILLISECONDS));
            arrayList.add(new Blinker.Step(-65281, j, TimeUnit.MILLISECONDS));
            arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, j2, TimeUnit.MILLISECONDS));
            return arrayList;
        }
    }

    public static class CountModuleAddressBlinkerPolicy extends BreathingBlinkerPolicy {
        public List<Blinker.Step> getIdlePattern(LynxModule lynxModule) {
            ArrayList arrayList = new ArrayList();
            if (lynxModule.getModuleAddress() == 173) {
                arrayList.add(new Blinker.Step(-16711936, 1, TimeUnit.SECONDS));
                return arrayList;
            }
            arrayList.add(new Blinker.Step(-16711936, (long) 4500, TimeUnit.MILLISECONDS));
            long j = (long) LynxModule.msInitialContact;
            arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, j, TimeUnit.MILLISECONDS));
            int min = Math.min(lynxModule.getModuleAddress(), (16 - arrayList.size()) / 2);
            for (int i = 0; i < min; i++) {
                arrayList.add(new Blinker.Step(-16776961, j, TimeUnit.MILLISECONDS));
                arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, j, TimeUnit.MILLISECONDS));
            }
            return arrayList;
        }
    }

    public boolean isParent() {
        warnIfClosed();
        return this.isParent;
    }

    public void pingAndQueryKnownInterfacesAndEtc() throws RobotCoreException, InterruptedException {
        warnIfClosed();
        RobotLog.m61vv(TAG, "pingAndQueryKnownInterfaces mod=%d", Integer.valueOf(getModuleAddress()));
        pingInitialContact();
        queryInterface(LynxDekaInterfaceCommand.createDekaInterface());
        startFtdiResetWatchdog();
        initializeDebugLogging();
        initializeLEDS();
        if (isParent() && LynxConstants.isEmbeddedSerialNumber(getSerialNumber())) {
            RobotLog.m61vv(TAG, "setAsControlHubEmbeddedModule(mod=%d)", Integer.valueOf(getModuleAddress()));
            EmbeddedControlHubModule.set(this);
        }
    }

    /* access modifiers changed from: protected */
    public void initializeLEDS() {
        setPattern(blinkerPolicy.getIdlePattern(this));
    }

    /* access modifiers changed from: protected */
    public void initializeDebugLogging() throws RobotCoreException, InterruptedException {
        setDebug(DebugGroup.MODULELED, DebugVerbosity.HIGH);
    }

    /* access modifiers changed from: protected */
    public void pingInitialContact() throws RobotCoreException, InterruptedException {
        ElapsedTime elapsedTime = new ElapsedTime();
        while (elapsedTime.milliseconds() < 500.0d) {
            try {
                ping(true);
                return;
            } catch (LynxNackException | RobotCoreException | RuntimeException unused) {
                RobotLog.m61vv(TAG, "retrying ping mod=%d", Integer.valueOf(getModuleAddress()));
            }
        }
        throw new RobotCoreException("initial ping contact failed: mod=%d", Integer.valueOf(getModuleAddress()));
    }

    public void validateCommand(LynxMessage lynxMessage) throws LynxUnsupportedCommandException {
        warnIfClosed();
        synchronized (this.interfacesQueried) {
            if (this.lynxUsbDevice.getArmingState() == RobotArmingStateNotifier.ARMINGSTATE.ARMED) {
                int commandNumber = lynxMessage.getCommandNumber();
                if (!LynxStandardCommand.isStandardCommandNumber(commandNumber)) {
                    if (!this.commandClasses.containsKey(Integer.valueOf(commandNumber))) {
                        throw new LynxUnsupportedCommandException(this, lynxMessage);
                    }
                }
            }
        }
    }

    public boolean isCommandSupported(Class<? extends LynxCommand> cls) {
        boolean z;
        warnIfClosed();
        synchronized (this.interfacesQueried) {
            if (this.moduleAddress == 0) {
                z = cls == LynxDiscoveryCommand.class;
            } else {
                z = this.supportedCommands.contains(cls);
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0100, code lost:
        r0 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
        com.qualcomm.robotcore.util.RobotLog.m51ee(TAG, r0, "exception registering %s", r13.getSimpleName());
     */
    /* JADX WARNING: Code restructure failed: missing block: B:31:0x0136, code lost:
        com.qualcomm.robotcore.util.RobotLog.m61vv(TAG, "mod#=%d queryInterface(): interface %s is not supported", java.lang.Integer.valueOf(getModuleAddress()), r17.getInterfaceName());
        r2.setWasNacked(true);
        r1.interfacesQueried.put(r17.getInterfaceName(), r2);
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Removed duplicated region for block: B:32:? A[ExcHandler: LynxNackException (unused com.qualcomm.hardware.lynx.LynxNackException), SYNTHETIC, Splitter:B:5:0x0016] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean queryInterface(com.qualcomm.hardware.lynx.commands.LynxInterface r17) throws java.lang.InterruptedException {
        /*
            r16 = this;
            r1 = r16
            r2 = r17
            r16.warnIfClosed()
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.qualcomm.hardware.lynx.commands.LynxInterface> r3 = r1.interfacesQueried
            monitor-enter(r3)
            com.qualcomm.hardware.lynx.commands.standard.LynxQueryInterfaceCommand r0 = new com.qualcomm.hardware.lynx.commands.standard.LynxQueryInterfaceCommand     // Catch:{ all -> 0x015e }
            java.lang.String r4 = r17.getInterfaceName()     // Catch:{ all -> 0x015e }
            r0.<init>(r1, r4)     // Catch:{ all -> 0x015e }
            r4 = 2
            r5 = 1
            r6 = 0
            com.qualcomm.hardware.lynx.commands.LynxMessage r0 = r0.sendReceive()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r7 = r0
            com.qualcomm.hardware.lynx.commands.standard.LynxQueryInterfaceResponse r7 = (com.qualcomm.hardware.lynx.commands.standard.LynxQueryInterfaceResponse) r7     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r2.setWasNacked(r6)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r0 = r7.getCommandNumberFirst()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r2.setBaseCommandNumber(r0)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.String r0 = "LynxModule"
            java.lang.String r8 = "mod#=%d queryInterface(%s)=%d commands starting at %d"
            r9 = 4
            java.lang.Object[] r10 = new java.lang.Object[r9]     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r11 = r16.getModuleAddress()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r10[r6] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.String r11 = r17.getInterfaceName()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r10[r5] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r11 = r7.getNumberOfCommands()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r10[r4] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r11 = r7.getCommandNumberFirst()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r12 = 3
            r10[r12] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r8, (java.lang.Object[]) r10)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.List r8 = r17.getCommandClasses()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor> r0 = r1.commandClasses     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.Set r0 = r0.entrySet()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.Iterator r0 = r0.iterator()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
        L_0x0068:
            boolean r10 = r0.hasNext()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            if (r10 == 0) goto L_0x0099
            java.lang.Object r10 = r0.next()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.Map$Entry r10 = (java.util.Map.Entry) r10     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Object r11 = r10.getValue()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor r11 = (com.qualcomm.hardware.lynx.LynxModule.MessageClassAndCtor) r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxMessage> r11 = r11.clazz     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            boolean r11 = r8.contains(r11)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            if (r11 == 0) goto L_0x0068
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor> r11 = r1.commandClasses     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Object r13 = r10.getKey()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r11.remove(r13)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.util.Set<java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxCommand>> r11 = r1.supportedCommands     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Object r10 = r10.getValue()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor r10 = (com.qualcomm.hardware.lynx.LynxModule.MessageClassAndCtor) r10     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxMessage> r10 = r10.clazz     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r11.remove(r10)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            goto L_0x0068
        L_0x0099:
            java.util.Iterator r10 = r8.iterator()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r11 = r6
        L_0x009e:
            boolean r0 = r10.hasNext()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            if (r0 == 0) goto L_0x0116
            java.lang.Object r0 = r10.next()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r13 = r0
            java.lang.Class r13 = (java.lang.Class) r13     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r0 = r7.getNumberOfCommands()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            if (r11 < r0) goto L_0x00df
            java.lang.String r0 = "LynxModule"
            java.lang.String r10 = "mod#=%d intf=%s: expected %d commands; found %d"
            java.lang.Object[] r9 = new java.lang.Object[r9]     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r11 = r16.getModuleAddress()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r11)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r9[r6] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.String r11 = r17.getInterfaceName()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r9[r5] = r11     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r8 = r8.size()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r9[r4] = r8     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            int r7 = r7.getNumberOfCommands()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.Integer r7 = java.lang.Integer.valueOf(r7)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r9[r12] = r7     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r10, (java.lang.Object[]) r9)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            goto L_0x0116
        L_0x00df:
            if (r13 != 0) goto L_0x00e2
            goto L_0x0112
        L_0x00e2:
            int r0 = r7.getCommandNumberFirst()     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            int r0 = r0 + r11
            com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor r14 = new com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            r14.<init>()     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            r14.clazz = r13     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            r14.assignCtor()     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            java.util.concurrent.ConcurrentHashMap<java.lang.Integer, com.qualcomm.hardware.lynx.LynxModule$MessageClassAndCtor> r15 = r1.commandClasses     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            r15.put(r0, r14)     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            java.util.Set<java.lang.Class<? extends com.qualcomm.hardware.lynx.commands.LynxCommand>> r0 = r1.supportedCommands     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            r0.add(r13)     // Catch:{ NoSuchMethodException -> 0x0102, RuntimeException -> 0x0100, LynxNackException -> 0x0136 }
            goto L_0x0112
        L_0x0100:
            r0 = move-exception
            goto L_0x0103
        L_0x0102:
            r0 = move-exception
        L_0x0103:
            java.lang.String r14 = "LynxModule"
            java.lang.String r15 = "exception registering %s"
            java.lang.Object[] r9 = new java.lang.Object[r5]     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.String r13 = r13.getSimpleName()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r9[r6] = r13     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            com.qualcomm.robotcore.util.RobotLog.m51ee(r14, r0, r15, r9)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
        L_0x0112:
            int r11 = r11 + 1
            r9 = 4
            goto L_0x009e
        L_0x0116:
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.qualcomm.hardware.lynx.commands.LynxInterface> r0 = r1.interfacesQueried     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            java.lang.String r7 = r17.getInterfaceName()     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            r0.put(r7, r2)     // Catch:{ LynxNackException -> 0x0136, RuntimeException -> 0x0120 }
            goto L_0x015c
        L_0x0120:
            r0 = move-exception
            java.lang.String r4 = "LynxModule"
            java.lang.String r7 = "exception during queryInterface(%s)"
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ all -> 0x015e }
            java.lang.String r2 = r17.getInterfaceName()     // Catch:{ all -> 0x015e }
            r5[r6] = r2     // Catch:{ all -> 0x015e }
            com.qualcomm.robotcore.util.RobotLog.m51ee(r4, r0, r7, r5)     // Catch:{ all -> 0x015e }
            java.lang.String r0 = "REV Hub interface query failed"
            com.qualcomm.robotcore.util.RobotLog.setGlobalErrorMsg(r0)     // Catch:{ all -> 0x015e }
            goto L_0x015b
        L_0x0136:
            java.lang.String r0 = "LynxModule"
            java.lang.String r7 = "mod#=%d queryInterface(): interface %s is not supported"
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ all -> 0x015e }
            int r8 = r16.getModuleAddress()     // Catch:{ all -> 0x015e }
            java.lang.Integer r8 = java.lang.Integer.valueOf(r8)     // Catch:{ all -> 0x015e }
            r4[r6] = r8     // Catch:{ all -> 0x015e }
            java.lang.String r8 = r17.getInterfaceName()     // Catch:{ all -> 0x015e }
            r4[r5] = r8     // Catch:{ all -> 0x015e }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r7, (java.lang.Object[]) r4)     // Catch:{ all -> 0x015e }
            r2.setWasNacked(r5)     // Catch:{ all -> 0x015e }
            java.util.concurrent.ConcurrentHashMap<java.lang.String, com.qualcomm.hardware.lynx.commands.LynxInterface> r0 = r1.interfacesQueried     // Catch:{ all -> 0x015e }
            java.lang.String r4 = r17.getInterfaceName()     // Catch:{ all -> 0x015e }
            r0.put(r4, r2)     // Catch:{ all -> 0x015e }
        L_0x015b:
            r5 = r6
        L_0x015c:
            monitor-exit(r3)     // Catch:{ all -> 0x015e }
            return r5
        L_0x015e:
            r0 = move-exception
            monitor-exit(r3)     // Catch:{ all -> 0x015e }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxModule.queryInterface(com.qualcomm.hardware.lynx.commands.LynxInterface):boolean");
    }

    public LynxInterface getInterface(String str) {
        LynxInterface lynxInterface;
        warnIfClosed();
        synchronized (this.interfacesQueried) {
            lynxInterface = this.interfacesQueried.get(str);
            if (lynxInterface == null) {
                RobotLog.m49ee(TAG, "interface \"%s\" has not been successfully queried for %s", str, this);
            } else if (lynxInterface.wasNacked()) {
                RobotLog.m49ee(TAG, "interface \"%s\" not supported on %s", str, this);
            }
        }
        return lynxInterface;
    }

    /* access modifiers changed from: protected */
    public void ping() {
        try {
            ping(false);
        } catch (LynxNackException | RobotCoreException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void ping(boolean z) throws RobotCoreException, InterruptedException, LynxNackException {
        warnIfClosed();
        new LynxKeepAliveCommand(this, z).send();
    }

    public void resetPingTimer(LynxMessage lynxMessage) {
        warnIfClosed();
        startPingTimer();
    }

    /* access modifiers changed from: protected */
    public void startPingTimer() {
        warnIfClosed();
        synchronized (this.futureLock) {
            stopPingTimer(false);
            if (this.isOpen) {
                try {
                    this.pingFuture = this.executor.schedule(new Runnable() {
                        public void run() {
                            if (LynxModule.this.isOpen) {
                                LynxModule.this.ping();
                            }
                        }
                    }, (long) getMsModulePingInterval(), TimeUnit.MILLISECONDS);
                } catch (RejectedExecutionException unused) {
                    RobotLog.m61vv(TAG, "mod#=%d: scheduling of ping rejected: ignored", Integer.valueOf(getModuleAddress()));
                    this.pingFuture = null;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopPingTimer(boolean z) {
        synchronized (this.futureLock) {
            Future<?> future = this.pingFuture;
            if (future != null) {
                future.cancel(false);
                if (z && !ThreadPool.awaitFuture(this.pingFuture, 250, TimeUnit.MILLISECONDS)) {
                    RobotLog.m61vv(TAG, "mod#=%d: unable to await ping future cancellation", Integer.valueOf(getModuleAddress()));
                }
                this.pingFuture = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startFtdiResetWatchdog() {
        synchronized (this.engagementLock) {
            if (!this.ftdiResetWatchdogActive) {
                this.ftdiResetWatchdogActive = true;
                setFtdiResetWatchdog(true);
            }
            if (this.isEngaged) {
                this.ftdiResetWatchdogActiveWhenEngaged = this.ftdiResetWatchdogActive;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopFtdiResetWatchdog() {
        stopFtdiResetWatchdog(false);
    }

    /* access modifiers changed from: protected */
    public void stopFtdiResetWatchdog(boolean z) {
        synchronized (this.engagementLock) {
            if (this.ftdiResetWatchdogActive) {
                this.ftdiResetWatchdogActive = false;
                setFtdiResetWatchdog(false);
            }
            if (this.isEngaged && !z) {
                this.ftdiResetWatchdogActiveWhenEngaged = this.ftdiResetWatchdogActive;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void setFtdiResetWatchdog(boolean z) {
        if (isCommandSupported(LynxFtdiResetControlCommand.class)) {
            boolean interrupted = Thread.interrupted();
            RobotLog.m61vv(TAG, "sending LynxFtdiResetControlCommand(%s) wasInterrupted=%s", Boolean.valueOf(z), Boolean.valueOf(interrupted));
            try {
                new LynxFtdiResetControlCommand(this, z).sendReceive();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
            if (interrupted) {
                Thread.currentThread().interrupt();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startExecutor() {
        if (this.executor == null) {
            this.executor = ThreadPool.newScheduledExecutor(1, "lynx module executor");
        }
    }

    /* access modifiers changed from: protected */
    public void stopExecutor() {
        ScheduledExecutorService scheduledExecutorService = this.executor;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            try {
                ThreadPool.awaitTermination(this.executor, 2, TimeUnit.SECONDS, "lynx module executor");
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        }
    }

    public static class BulkData {
        private final boolean fake;
        private final LynxGetBulkInputDataResponse resp;

        private BulkData(LynxGetBulkInputDataResponse lynxGetBulkInputDataResponse, boolean z) {
            this.resp = lynxGetBulkInputDataResponse;
            this.fake = z;
        }

        public boolean getDigitalChannelState(int i) {
            return this.resp.getDigitalInput(i);
        }

        public int getMotorCurrentPosition(int i) {
            return this.resp.getEncoder(i);
        }

        public int getMotorVelocity(int i) {
            return this.resp.getVelocity(i);
        }

        public boolean isMotorBusy(int i) {
            return !this.resp.isAtTarget(i);
        }

        public boolean isMotorOverCurrent(int i) {
            return this.resp.isOverCurrent(i);
        }

        public double getAnalogInputVoltage(int i) {
            return getAnalogInputVoltage(i, VoltageUnit.VOLTS);
        }

        public double getAnalogInputVoltage(int i, VoltageUnit voltageUnit) {
            return voltageUnit.convert((double) this.resp.getAnalogInput(i), VoltageUnit.MILLIVOLTS);
        }

        public boolean isFake() {
            return this.fake;
        }
    }

    public BulkData getBulkData() {
        warnIfClosed();
        synchronized (this.bulkCachingLock) {
            clearBulkCache();
            try {
                BulkData bulkData = new BulkData((LynxGetBulkInputDataResponse) new LynxGetBulkInputDataCommand(this).sendReceive(), false);
                this.lastBulkData = bulkData;
                return bulkData;
            } catch (InterruptedException e) {
                e = e;
                handleException(e);
                BulkData bulkData2 = (BulkData) LynxUsbUtil.makePlaceholderValue(new BulkData(new LynxGetBulkInputDataResponse(this), true));
                this.lastBulkData = bulkData2;
                return bulkData2;
            } catch (RuntimeException e2) {
                e = e2;
                handleException(e);
                BulkData bulkData22 = (BulkData) LynxUsbUtil.makePlaceholderValue(new BulkData(new LynxGetBulkInputDataResponse(this), true));
                this.lastBulkData = bulkData22;
                return bulkData22;
            } catch (LynxNackException e3) {
                e = e3;
                handleException(e);
                BulkData bulkData222 = (BulkData) LynxUsbUtil.makePlaceholderValue(new BulkData(new LynxGetBulkInputDataResponse(this), true));
                this.lastBulkData = bulkData222;
                return bulkData222;
            }
        }
    }

    public BulkCachingMode getBulkCachingMode() {
        warnIfClosed();
        return this.bulkCachingMode;
    }

    public void setBulkCachingMode(BulkCachingMode bulkCachingMode2) {
        warnIfClosed();
        synchronized (this.bulkCachingLock) {
            if (bulkCachingMode2 == BulkCachingMode.OFF) {
                clearBulkCache();
            }
            this.bulkCachingMode = bulkCachingMode2;
        }
    }

    public void clearBulkCache() {
        warnIfClosed();
        synchronized (this.bulkCachingLock) {
            for (List<LynxDekaInterfaceCommand<?>> clear : this.bulkCachingHistory.values()) {
                clear.clear();
            }
            this.lastBulkData = null;
        }
    }

    /* access modifiers changed from: package-private */
    public BulkData recordBulkCachingCommandIntent(LynxDekaInterfaceCommand<?> lynxDekaInterfaceCommand) {
        warnIfClosed();
        return recordBulkCachingCommandIntent(lynxDekaInterfaceCommand, InspectionState.NO_VERSION);
    }

    /* access modifiers changed from: package-private */
    public BulkData recordBulkCachingCommandIntent(LynxDekaInterfaceCommand<?> lynxDekaInterfaceCommand, String str) {
        BulkData bulkData;
        warnIfClosed();
        synchronized (this.bulkCachingLock) {
            List list = this.bulkCachingHistory.get(str);
            if (this.bulkCachingMode == BulkCachingMode.AUTO) {
                if (list == null) {
                    list = new ArrayList();
                    this.bulkCachingHistory.put(str, list);
                }
                Iterator it = list.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    LynxDekaInterfaceCommand lynxDekaInterfaceCommand2 = (LynxDekaInterfaceCommand) it.next();
                    if (lynxDekaInterfaceCommand2.getDestModuleAddress() == lynxDekaInterfaceCommand.getDestModuleAddress() && lynxDekaInterfaceCommand2.getCommandNumber() == lynxDekaInterfaceCommand.getCommandNumber() && Arrays.equals(lynxDekaInterfaceCommand2.toPayloadByteArray(), lynxDekaInterfaceCommand.toPayloadByteArray())) {
                        clearBulkCache();
                        break;
                    }
                }
            }
            if (this.lastBulkData == null) {
                getBulkData();
            }
            if (this.bulkCachingMode == BulkCachingMode.AUTO) {
                list.add(lynxDekaInterfaceCommand);
            }
            bulkData = this.lastBulkData;
        }
        return bulkData;
    }

    public void failSafe() throws RobotCoreException, InterruptedException, LynxNackException {
        warnIfClosed();
        new LynxFailSafeCommand(this).send();
        forgetLastKnown();
    }

    public void enablePhoneCharging(boolean z) throws RobotCoreException, InterruptedException, LynxNackException {
        warnIfClosed();
        new LynxPhoneChargeControlCommand(this, z).send();
    }

    public boolean isPhoneChargingEnabled() throws RobotCoreException, InterruptedException, LynxNackException {
        warnIfClosed();
        return ((LynxPhoneChargeQueryResponse) new LynxPhoneChargeQueryCommand(this).sendReceive()).isChargeEnabled();
    }

    public double getCurrent(CurrentUnit currentUnit) {
        warnIfClosed();
        try {
            return currentUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.BATTERY_CURRENT, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), CurrentUnit.MILLIAMPS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getGpioBusCurrent(CurrentUnit currentUnit) {
        warnIfClosed();
        try {
            return currentUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.GPIO_CURRENT, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), CurrentUnit.MILLIAMPS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getI2cBusCurrent(CurrentUnit currentUnit) {
        warnIfClosed();
        try {
            return currentUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.I2C_BUS_CURRENT, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), CurrentUnit.MILLIAMPS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getInputVoltage(VoltageUnit voltageUnit) {
        warnIfClosed();
        try {
            return voltageUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.BATTERY_MONITOR, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), VoltageUnit.MILLIVOLTS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getAuxiliaryVoltage(VoltageUnit voltageUnit) {
        warnIfClosed();
        try {
            return voltageUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.FIVE_VOLT_MONITOR, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), VoltageUnit.MILLIVOLTS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getTemperature(TempUnit tempUnit) {
        warnIfClosed();
        try {
            return tempUnit.fromCelsius(((double) ((LynxGetADCResponse) new LynxGetADCCommand(this, LynxGetADCCommand.Channel.CONTROLLER_TEMPERATURE, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue()) / 10.0d);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public enum DebugGroup {
        NONE(0),
        MAIN(1),
        TOHOST(2),
        FROMHOST(3),
        ADC(4),
        PWMSERVO(5),
        MODULELED(6),
        DIGITALIO(7),
        I2C(8),
        MOTOR0(9),
        MOTOR1(10),
        MOTOR2(11),
        MOTOR3(12);
        
        public final byte bVal;

        private DebugGroup(int i) {
            this.bVal = (byte) i;
        }

        public static DebugGroup fromInt(int i) {
            for (DebugGroup debugGroup : values()) {
                if (debugGroup.bVal == ((byte) i)) {
                    return debugGroup;
                }
            }
            return NONE;
        }
    }

    public enum DebugVerbosity {
        OFF(0),
        LOW(1),
        MEDIUM(2),
        HIGH(3);
        
        public final byte bVal;

        private DebugVerbosity(int i) {
            this.bVal = (byte) i;
        }

        public static DebugVerbosity fromInt(int i) {
            for (DebugVerbosity debugVerbosity : values()) {
                if (debugVerbosity.bVal == ((byte) i)) {
                    return debugVerbosity;
                }
            }
            return OFF;
        }
    }

    public void setDebug(DebugGroup debugGroup, DebugVerbosity debugVerbosity) throws InterruptedException {
        warnIfClosed();
        try {
            new LynxSetDebugLogLevelCommand(this, debugGroup, debugVerbosity).send();
        } catch (LynxNackException | RuntimeException e) {
            handleException(e);
        }
    }

    public <T> T acquireI2cLockWhile(Supplier<T> supplier) throws InterruptedException, RobotCoreException, LynxNackException {
        T t;
        warnIfClosed();
        synchronized (this.i2cLock) {
            t = supplier.get();
        }
        return t;
    }

    public void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        warnIfClosed();
        this.lynxUsbDevice.acquireNetworkTransmissionLock(lynxMessage);
    }

    public void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        warnIfClosed();
        this.lynxUsbDevice.releaseNetworkTransmissionLock(lynxMessage);
    }

    public void sendCommand(LynxMessage lynxMessage) throws InterruptedException, LynxUnsupportedCommandException {
        warnIfClosed();
        lynxMessage.setMessageNumber(getNewMessageNumber());
        int messageNumber = lynxMessage.getMessageNumber();
        lynxMessage.setSerialization(new LynxDatagram(lynxMessage));
        boolean z = lynxMessage.isAckable() || lynxMessage.isResponseExpected();
        this.unfinishedCommands.put(Integer.valueOf(messageNumber), (LynxRespondable) lynxMessage);
        this.lynxUsbDevice.transmit(lynxMessage);
        if (!z) {
            finishedWithMessage(lynxMessage);
        }
    }

    public void retransmit(LynxMessage lynxMessage) throws InterruptedException {
        warnIfClosed();
        RobotLog.m61vv(TAG, "retransmitting: mod=%d cmd=0x%02x msg#=%d ref#=%d ", Integer.valueOf(getModuleAddress()), Integer.valueOf(lynxMessage.getCommandNumber()), Integer.valueOf(lynxMessage.getMessageNumber()), Integer.valueOf(lynxMessage.getReferenceNumber()));
        this.lynxUsbDevice.transmit(lynxMessage);
    }

    public void finishedWithMessage(LynxMessage lynxMessage) {
        if (LynxUsbDeviceImpl.DEBUG_LOG_DATAGRAMS_FINISH) {
            RobotLog.m61vv(TAG, "finishing mod=%d msg#=%d", Integer.valueOf(lynxMessage.getModuleAddress()), Integer.valueOf(lynxMessage.getMessageNumber()));
        }
        this.unfinishedCommands.remove(Integer.valueOf(lynxMessage.getMessageNumber()));
        lynxMessage.forgetSerialization();
    }

    public void pretendFinishExtantCommands() throws InterruptedException {
        warnIfClosed();
        for (LynxRespondable pretendFinish : this.unfinishedCommands.values()) {
            pretendFinish.pretendFinish();
        }
    }

    public void onIncomingDatagramReceived(LynxDatagram lynxDatagram) {
        warnIfClosed();
        noteDatagramReceived();
        try {
            MessageClassAndCtor messageClassAndCtor = this.commandClasses.get(Integer.valueOf(lynxDatagram.getCommandNumber()));
            if (messageClassAndCtor != null) {
                if (lynxDatagram.isResponse()) {
                    messageClassAndCtor = responseClasses.get(messageClassAndCtor.clazz);
                }
                if (messageClassAndCtor != null) {
                    LynxMessage lynxMessage = (LynxMessage) messageClassAndCtor.ctor.newInstance(new Object[]{this});
                    lynxMessage.setSerialization(lynxDatagram);
                    lynxMessage.loadFromSerialization();
                    if (LynxUsbDeviceImpl.DEBUG_LOG_MESSAGES) {
                        RobotLog.m61vv(TAG, "rec'd: mod=%d cmd=0x%02x(%s) msg#=%d ref#=%d", Integer.valueOf(lynxDatagram.getSourceModuleAddress()), Integer.valueOf(lynxDatagram.getPacketId()), lynxMessage.getClass().getSimpleName(), Integer.valueOf(lynxMessage.getMessageNumber()), Integer.valueOf(lynxMessage.getReferenceNumber()));
                    }
                    if (!lynxMessage.isAck()) {
                        if (!lynxMessage.isNack()) {
                            LynxRespondable lynxRespondable = this.unfinishedCommands.get(Integer.valueOf(lynxDatagram.getReferenceNumber()));
                            if (lynxRespondable != null) {
                                Assert.assertTrue(lynxMessage.isResponse());
                                lynxRespondable.onResponseReceived((LynxResponse) lynxMessage);
                                finishedWithMessage(lynxRespondable);
                                return;
                            }
                            RobotLog.m49ee(TAG, "unable to find originating command for packetid=0x%04x msg#=%d ref#=%d", Integer.valueOf(lynxDatagram.getPacketId()), Integer.valueOf(lynxDatagram.getMessageNumber()), Integer.valueOf(lynxDatagram.getReferenceNumber()));
                            return;
                        }
                    }
                    LynxRespondable lynxRespondable2 = this.unfinishedCommands.get(Integer.valueOf(lynxDatagram.getReferenceNumber()));
                    if (lynxRespondable2 != null) {
                        if (lynxMessage.isNack()) {
                            lynxRespondable2.onNackReceived((LynxNack) lynxMessage);
                        } else {
                            lynxRespondable2.onAckReceived((LynxAck) lynxMessage);
                        }
                        finishedWithMessage(lynxRespondable2);
                        return;
                    }
                    RobotLog.m49ee(TAG, "unable to find originating LynxRespondable for mod=%d msg#=%d ref#=%d", Integer.valueOf(lynxDatagram.getSourceModuleAddress()), Integer.valueOf(lynxDatagram.getMessageNumber()), Integer.valueOf(lynxDatagram.getReferenceNumber()));
                    return;
                }
                return;
            }
            RobotLog.m49ee(TAG, "no command class known for command=0x%02x", Integer.valueOf(lynxDatagram.getCommandNumber()));
        } catch (IllegalAccessException | InstantiationException | RuntimeException | InvocationTargetException e) {
            RobotLog.m50ee(TAG, e, "internal error in LynxModule.noteIncomingDatagramReceived()");
        }
    }

    public void abandonUnfinishedCommands() {
        warnIfClosed();
        this.unfinishedCommands.clear();
    }

    /* access modifiers changed from: protected */
    public void nackUnfinishedCommands() {
        warnIfClosed();
        while (!this.unfinishedCommands.isEmpty()) {
            for (LynxRespondable next : this.unfinishedCommands.values()) {
                RobotLog.m61vv(TAG, "force-nacking unfinished command=%s mod=%d msg#=%d", next.getClass().getSimpleName(), Integer.valueOf(next.getModuleAddress()), Integer.valueOf(next.getMessageNumber()));
                next.onNackReceived(new LynxNack((LynxModuleIntf) this, (LynxNack.ReasonCode) next.isResponseExpected() ? LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_RESPONSE : LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_ACK));
                finishedWithMessage(next);
            }
        }
    }
}
