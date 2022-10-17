package com.qualcomm.ftccommon;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import androidx.core.p003os.EnvironmentCompat;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftccommon.configuration.RobotConfigMap;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.hardware.bosch.BHI260IMU;
import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxModuleWarningManager;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.commands.core.LynxFirmwareVersionManager;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.hardware.CachedLynxModulesInfo;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.xmlpull.v1.XmlPullParserException;

public class FtcEventLoop extends FtcEventLoopBase {
    private static volatile boolean appJustLaunched = true;
    protected final OpModeManagerImpl opModeManager;
    protected final AtomicReference<OpMode> opModeStopRequested = new AtomicReference<>();
    protected final Map<String, Long> recentlyAttachedUsbDevices = new ConcurrentHashMap();
    protected UsbModuleAttachmentHandler usbModuleAttachmentHandler = new DefaultUsbModuleAttachmentHandler();
    protected final Utility utility;

    public FtcEventLoop(HardwareFactory hardwareFactory, OpModeRegister opModeRegister, UpdateUI.Callback callback, Activity activity) {
        super(hardwareFactory, opModeRegister, callback, activity);
        this.opModeManager = createOpModeManager(activity);
        this.utility = new Utility(activity);
    }

    protected static OpModeManagerImpl createOpModeManager(Activity activity) {
        return new OpModeManagerImpl(activity, new HardwareMap(activity));
    }

    public OpModeManagerImpl getOpModeManager() {
        return this.opModeManager;
    }

    public UsbModuleAttachmentHandler getUsbModuleAttachmentHandler() {
        return this.usbModuleAttachmentHandler;
    }

    public void setUsbModuleAttachmentHandler(UsbModuleAttachmentHandler usbModuleAttachmentHandler2) {
        this.usbModuleAttachmentHandler = usbModuleAttachmentHandler2;
    }

    public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
        RobotLog.m54ii(FtcEventLoopBase.TAG, "======= INIT START =======");
        super.init(eventLoopManager);
        this.opModeManager.init(eventLoopManager);
        this.registeredOpModes.registerAllOpModes(this.userOpmodeRegister);
        sendActiveConfig();
        ConfigurationTypeManager.getInstance().sendUserDeviceTypes();
        sendOpModeList();
        this.ftcEventLoopHandler.init(eventLoopManager);
        LynxUsbDevice lynxUsbDevice = null;
        try {
            if (LynxConstants.isRevControlHub()) {
                lynxUsbDevice = ensureEmbeddedControlHubModuleIsSetUp();
                if (appJustLaunched) {
                    appJustLaunched = false;
                    lynxUsbDevice.performSystemOperationOnConnectedModule(173, true, new Consumer<LynxModule>() {
                        public void accept(LynxModule lynxModule) {
                            LynxI2cDeviceSynch createLynxI2cDeviceSynch = LynxFirmwareVersionManager.createLynxI2cDeviceSynch(AppUtil.getDefContext(), lynxModule, 0);
                            if (BHI260IMU.imuIsPresent(createLynxI2cDeviceSynch)) {
                                BHI260IMU.flashFirmwareIfNecessary(createLynxI2cDeviceSynch);
                            }
                            createLynxI2cDeviceSynch.close();
                        }
                    });
                }
            }
            HardwareMap hardwareMap = this.ftcEventLoopHandler.getHardwareMap();
            this.opModeManager.setHardwareMap(hardwareMap);
            hardwareMap.logDevices();
            CachedLynxModulesInfo.setLynxModulesInfo(compileLynxModulesInfo(hardwareMap));
            LynxModuleWarningManager.getInstance().init(this.opModeManager, hardwareMap);
            I2cWarningManager.clearI2cWarnings();
            RobotLog.m54ii(FtcEventLoopBase.TAG, "======= INIT FINISH =======");
        } finally {
            if (lynxUsbDevice != null) {
                lynxUsbDevice.close();
            }
        }
    }

    public void loop() {
        super.loop();
        OpMode andSet = this.opModeStopRequested.getAndSet((Object) null);
        if (andSet != null) {
            processOpModeStopRequest(andSet);
        }
        checkForChangedOpModes();
        this.ftcEventLoopHandler.displayGamePadInfo(this.opModeManager.getActiveOpModeName());
        Gamepad[] gamepads = this.ftcEventLoopHandler.getGamepads();
        this.ftcEventLoopHandler.gamepadEffects();
        this.opModeManager.runActiveOpMode(gamepads);
    }

    public void refreshUserTelemetry(TelemetryMessage telemetryMessage, double d) {
        this.ftcEventLoopHandler.refreshUserTelemetry(telemetryMessage, d);
    }

    public void teardown() throws RobotCoreException, InterruptedException {
        RobotLog.m54ii(FtcEventLoopBase.TAG, "======= TEARDOWN =======");
        super.teardown();
        this.opModeManager.stopActiveOpMode();
        this.opModeManager.teardown();
        RobotLog.m54ii(FtcEventLoopBase.TAG, "======= TEARDOWN COMPLETE =======");
    }

    public CallbackResult processCommand(Command command) throws InterruptedException, RobotCoreException {
        this.ftcEventLoopHandler.sendBatteryInfo();
        CallbackResult processCommand = super.processCommand(command);
        if (processCommand.stopDispatch()) {
            return processCommand;
        }
        CallbackResult callbackResult = CallbackResult.HANDLED;
        String name = command.getName();
        String extra = command.getExtra();
        if (name.equals(CommandList.CMD_INIT_OP_MODE)) {
            handleCommandInitOpMode(extra);
        } else if (name.equals(CommandList.CMD_RUN_OP_MODE)) {
            handleCommandRunOpMode(extra);
        } else if (name.equals(CommandList.CMD_SET_MATCH_NUMBER)) {
            handleCommandSetMatchNumber(extra);
        } else {
            callbackResult = CallbackResult.NOT_HANDLED;
        }
        return callbackResult == CallbackResult.HANDLED ? callbackResult : processCommand;
    }

    /* access modifiers changed from: protected */
    public void sendOpModeList() {
        super.sendOpModeList();
        EventLoopManager eventLoopManager = this.ftcEventLoopHandler.getEventLoopManager();
        if (eventLoopManager != null) {
            eventLoopManager.refreshSystemTelemetryNow();
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandSetMatchNumber(String str) {
        try {
            this.opModeManager.setMatchNumber(Integer.parseInt(str));
        } catch (NumberFormatException e) {
            RobotLog.logStackTrace(e);
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandInitOpMode(String str) {
        this.opModeManager.initActiveOpMode(this.ftcEventLoopHandler.getOpMode(str));
    }

    /* access modifiers changed from: protected */
    public void handleCommandRunOpMode(String str) {
        String opMode = this.ftcEventLoopHandler.getOpMode(str);
        if (!this.opModeManager.getActiveOpModeName().equals(opMode)) {
            this.opModeManager.initActiveOpMode(opMode);
        }
        this.opModeManager.startActiveOpMode();
    }

    public void requestOpModeStop(OpMode opMode) {
        this.opModeStopRequested.set(opMode);
    }

    private void processOpModeStopRequest(OpMode opMode) {
        if (opMode != null && this.opModeManager.getActiveOpMode() == opMode) {
            RobotLog.m55ii(FtcEventLoopBase.TAG, "auto-stopping OpMode '%s'", this.opModeManager.getActiveOpModeName());
            this.opModeManager.stopActiveOpMode();
        }
    }

    public void onUsbDeviceAttached(UsbDevice usbDevice) {
        SerialNumber serialNumberOfUsbDevice = getSerialNumberOfUsbDevice(usbDevice);
        if (serialNumberOfUsbDevice != null) {
            pendUsbDeviceAttachment(serialNumberOfUsbDevice, 0, TimeUnit.MILLISECONDS);
            return;
        }
        RobotLog.m49ee(FtcEventLoopBase.TAG, "ignoring: unable get serial number of attached UsbDevice vendor=0x%04x, product=0x%04x device=0x%04x name=%s", Integer.valueOf(usbDevice.getVendorId()), Integer.valueOf(usbDevice.getProductId()), Integer.valueOf(usbDevice.getDeviceId()), usbDevice.getDeviceName());
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x002c, code lost:
        if (r1 == null) goto L_0x002f;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:7:0x001f, code lost:
        if (r1 != null) goto L_0x0021;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:0x0021, code lost:
        r1.close();
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.qualcomm.robotcore.util.SerialNumber getSerialNumberOfUsbDevice(android.hardware.usb.UsbDevice r4) {
        /*
            r3 = this;
            java.lang.String r0 = r4.getSerialNumber()
            com.qualcomm.robotcore.util.SerialNumber r0 = com.qualcomm.robotcore.util.SerialNumber.fromStringOrNull(r0)
            if (r0 != 0) goto L_0x002f
            r1 = 0
            org.firstinspires.ftc.robotcore.internal.ftdi.FtDeviceManager r2 = org.firstinspires.ftc.robotcore.internal.ftdi.FtDeviceManager.getInstance()     // Catch:{ RuntimeException -> 0x002c, all -> 0x0025 }
            org.firstinspires.ftc.robotcore.internal.ftdi.FtDevice r1 = r2.openByUsbDevice(r4)     // Catch:{ RuntimeException -> 0x002c, all -> 0x0025 }
            if (r1 == 0) goto L_0x001f
            org.firstinspires.ftc.robotcore.internal.ftdi.FtDeviceInfo r2 = r1.getDeviceInfo()     // Catch:{ RuntimeException -> 0x002c, all -> 0x0025 }
            java.lang.String r2 = r2.serialNumber     // Catch:{ RuntimeException -> 0x002c, all -> 0x0025 }
            com.qualcomm.robotcore.util.SerialNumber r0 = com.qualcomm.robotcore.util.SerialNumber.fromStringOrNull(r2)     // Catch:{ RuntimeException -> 0x002c, all -> 0x0025 }
        L_0x001f:
            if (r1 == 0) goto L_0x002f
        L_0x0021:
            r1.close()
            goto L_0x002f
        L_0x0025:
            r4 = move-exception
            if (r1 == 0) goto L_0x002b
            r1.close()
        L_0x002b:
            throw r4
        L_0x002c:
            if (r1 == 0) goto L_0x002f
            goto L_0x0021
        L_0x002f:
            if (r0 != 0) goto L_0x003f
            org.firstinspires.ftc.robotcore.external.ClassFactory r1 = org.firstinspires.ftc.robotcore.external.ClassFactory.getInstance()     // Catch:{ RuntimeException -> 0x003f }
            org.firstinspires.ftc.robotcore.external.hardware.camera.CameraManager r1 = r1.getCameraManager()     // Catch:{ RuntimeException -> 0x003f }
            org.firstinspires.ftc.robotcore.internal.camera.CameraManagerInternal r1 = (org.firstinspires.ftc.robotcore.internal.camera.CameraManagerInternal) r1     // Catch:{ RuntimeException -> 0x003f }
            com.qualcomm.robotcore.util.SerialNumber r0 = r1.getRealOrVendorProductSerialNumber(r4)     // Catch:{ RuntimeException -> 0x003f }
        L_0x003f:
            return r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.FtcEventLoop.getSerialNumberOfUsbDevice(android.hardware.usb.UsbDevice):com.qualcomm.robotcore.util.SerialNumber");
    }

    public void pendUsbDeviceAttachment(SerialNumber serialNumber, long j, TimeUnit timeUnit) {
        long j2 = 0;
        if (j != 0) {
            j2 = System.nanoTime() + timeUnit.toNanos(j);
        }
        this.recentlyAttachedUsbDevices.put(serialNumber.getString(), Long.valueOf(j2));
    }

    public void processedRecentlyAttachedUsbDevices() throws RobotCoreException, InterruptedException {
        boolean z;
        HashSet hashSet = new HashSet();
        long nanoTime = System.nanoTime();
        for (Map.Entry next : this.recentlyAttachedUsbDevices.entrySet()) {
            if (((Long) next.getValue()).longValue() <= nanoTime) {
                hashSet.add((String) next.getKey());
                this.recentlyAttachedUsbDevices.remove(next.getKey());
            }
        }
        if (this.usbModuleAttachmentHandler != null && !hashSet.isEmpty()) {
            List<RobotUsbModule> all = this.ftcEventLoopHandler.getHardwareMap().getAll(RobotUsbModule.class);
            Iterator it = new ArrayList(hashSet).iterator();
            while (it.hasNext()) {
                String str = (String) it.next();
                SerialNumber fromString = SerialNumber.fromString(str);
                Iterator<RobotUsbModule> it2 = all.iterator();
                while (true) {
                    if (!it2.hasNext()) {
                        z = false;
                        break;
                    }
                    RobotUsbModule next2 = it2.next();
                    if (fromString.matches(next2.getSerialNumber()) && next2.getArmingState() != RobotArmingStateNotifier.ARMINGSTATE.ARMED) {
                        hashSet.remove(str);
                        handleUsbModuleAttach(next2);
                        z = true;
                        break;
                    }
                }
                if (!z) {
                    RobotLog.m61vv(FtcEventLoopBase.TAG, "processedRecentlyAttachedUsbDevices(): %s not in hwmap; ignoring", fromString);
                }
            }
        }
    }

    public void handleUsbModuleDetach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
        UsbModuleAttachmentHandler usbModuleAttachmentHandler2 = this.usbModuleAttachmentHandler;
        if (usbModuleAttachmentHandler2 != null) {
            usbModuleAttachmentHandler2.handleUsbModuleDetach(robotUsbModule);
        }
    }

    public void handleUsbModuleAttach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
        UsbModuleAttachmentHandler usbModuleAttachmentHandler2 = this.usbModuleAttachmentHandler;
        if (usbModuleAttachmentHandler2 != null) {
            usbModuleAttachmentHandler2.handleUsbModuleAttach(robotUsbModule);
        }
    }

    private LynxUsbDevice ensureEmbeddedControlHubModuleIsSetUp() throws RobotCoreException, InterruptedException {
        RobotLog.m60vv(FtcEventLoopBase.TAG, "Ensuring that the embedded Control Hub module is set up correctly");
        LynxUsbDevice lynxUsbDevice = (LynxUsbDevice) startUsbScanMangerIfNecessary().getDeviceManager().createLynxUsbDevice(SerialNumber.createEmbedded(), (String) null);
        if (!lynxUsbDevice.setupControlHubEmbeddedModule()) {
            return lynxUsbDevice;
        }
        updateEditableConfigFilesWithNewControlHubAddress();
        lynxUsbDevice.close();
        return (LynxUsbDevice) startUsbScanMangerIfNecessary().getDeviceManager().createLynxUsbDevice(SerialNumber.createEmbedded(), (String) null);
    }

    private void updateEditableConfigFilesWithNewControlHubAddress() throws RobotCoreException {
        RobotLog.m60vv(FtcEventLoopBase.TAG, "We just auto-changed the Control Hub's address. Now auto-updating configuration files.");
        ReadXMLFileHandler readXMLFileHandler = new ReadXMLFileHandler(startUsbScanMangerIfNecessary().getDeviceManager());
        RobotConfigFileManager robotConfigFileManager = new RobotConfigFileManager();
        Iterator<RobotConfigFile> it = robotConfigFileManager.getXMLFiles().iterator();
        while (it.hasNext()) {
            RobotConfigFile next = it.next();
            if (!next.isReadOnly()) {
                RobotLog.m61vv(FtcEventLoopBase.TAG, "Updating \"%s\" config file", next.getName());
                try {
                    robotConfigFileManager.writeToFile(next, false, robotConfigFileManager.toXml(new RobotConfigMap((Collection<ControllerConfiguration>) readXMLFileHandler.parse(next.getXml()))));
                } catch (IOException | XmlPullParserException e) {
                    RobotLog.m50ee(FtcEventLoopBase.TAG, e, String.format(Locale.ENGLISH, "Failed to auto-update config file %s after automatically changing embedded Control Hub module address. This is OK.", new Object[]{next.getName()}));
                }
            }
        }
    }

    private List<CachedLynxModulesInfo.LynxModuleInfo> compileLynxModulesInfo(HardwareMap hardwareMap) {
        String str;
        String str2;
        String str3;
        ArrayList arrayList = new ArrayList();
        for (LynxModule next : hardwareMap.getAll(LynxModule.class)) {
            try {
                str = hardwareMap.getNamesOf(next).iterator().next();
            } catch (RuntimeException unused) {
                str = "Expansion Hub " + next.getModuleAddress();
            }
            String str4 = str;
            String nullableFirmwareVersionString = next.getNullableFirmwareVersionString();
            if (nullableFirmwareVersionString == null) {
                str2 = EnvironmentCompat.MEDIA_UNKNOWN;
            } else {
                LynxI2cDeviceSynch createLynxI2cDeviceSynch = LynxFirmwareVersionManager.createLynxI2cDeviceSynch(AppUtil.getDefContext(), next, 0);
                createLynxI2cDeviceSynch.setI2cAddress(BNO055IMU.I2CADDR_DEFAULT);
                if (BNO055IMUImpl.imuIsPresent(createLynxI2cDeviceSynch, false)) {
                    str3 = "BNO055";
                } else {
                    str3 = BHI260IMU.imuIsPresent(createLynxI2cDeviceSynch) ? "BHI260AP" : "none";
                }
                createLynxI2cDeviceSynch.close();
                str2 = str3;
            }
            arrayList.add(new CachedLynxModulesInfo.LynxModuleInfo(str4, nullableFirmwareVersionString, next.getSerialNumber().toString(), next.getModuleAddress(), str2));
        }
        Collections.sort(arrayList, new Comparator<CachedLynxModulesInfo.LynxModuleInfo>() {
            public int compare(CachedLynxModulesInfo.LynxModuleInfo lynxModuleInfo, CachedLynxModulesInfo.LynxModuleInfo lynxModuleInfo2) {
                return lynxModuleInfo.name.compareTo(lynxModuleInfo2.name);
            }
        });
        return Collections.unmodifiableList(arrayList);
    }

    public class DefaultUsbModuleAttachmentHandler implements UsbModuleAttachmentHandler {
        public DefaultUsbModuleAttachmentHandler() {
        }

        public void handleUsbModuleAttach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
            String nameOfUsbModule = nameOfUsbModule(robotUsbModule);
            RobotLog.m55ii(FtcEventLoopBase.TAG, "vv===== MODULE ATTACH: disarm %s=====vv", nameOfUsbModule);
            robotUsbModule.disarm();
            RobotLog.m55ii(FtcEventLoopBase.TAG, "======= MODULE ATTACH: arm or pretend %s=======", nameOfUsbModule);
            robotUsbModule.armOrPretend();
            RobotLog.m55ii(FtcEventLoopBase.TAG, "^^===== MODULE ATTACH: complete %s=====^^", nameOfUsbModule);
        }

        public void handleUsbModuleDetach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
            String nameOfUsbModule = nameOfUsbModule(robotUsbModule);
            RobotLog.m55ii(FtcEventLoopBase.TAG, "vv===== MODULE DETACH RECOVERY: disarm %s=====vv", nameOfUsbModule);
            robotUsbModule.disarm();
            RobotLog.m55ii(FtcEventLoopBase.TAG, "======= MODULE DETACH RECOVERY: pretend %s=======", nameOfUsbModule);
            robotUsbModule.pretend();
            RobotLog.m55ii(FtcEventLoopBase.TAG, "^^===== MODULE DETACH RECOVERY: complete %s=====^^", nameOfUsbModule);
        }

        /* access modifiers changed from: package-private */
        public String nameOfUsbModule(RobotUsbModule robotUsbModule) {
            return HardwareFactory.getDeviceDisplayName(FtcEventLoop.this.activityContext, robotUsbModule.getSerialNumber());
        }
    }
}
