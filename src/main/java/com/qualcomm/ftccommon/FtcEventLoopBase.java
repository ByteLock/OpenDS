package com.qualcomm.ftccommon;

import android.app.Activity;
import androidx.core.internal.view.SupportMenu;
import androidx.core.view.ViewCompat;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.ftccommon.configuration.USBScanManager;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.hardware.lynx.EmbeddedControlHubModule;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.USBAccessibleLynxModule;
import com.qualcomm.robotcore.hardware.VisuallyIdentifiableHardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.WriteXMLFileHandler;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import java.io.File;
import java.io.FileFilter;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.external.function.Supplier;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamServer;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PreferenceRemoterRC;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.network.WifiDirectGroupName;
import org.firstinspires.ftc.robotcore.network.WifiDirectPersistentGroupManager;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaBuildLocker;
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes;
import org.firstinspires.ftc.robotcore.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParserException;

public abstract class FtcEventLoopBase implements EventLoop {
    public static final String TAG = "FtcEventLoop";
    protected Activity activityContext;
    protected FtcEventLoopHandler ftcEventLoopHandler;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected final RegisteredOpModes registeredOpModes;
    protected RobotConfigFileManager robotCfgFileMgr;
    protected boolean runningOnDriverStation = false;
    protected USBScanManager usbScanManager;
    protected final OpModeRegister userOpmodeRegister;

    public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
    }

    protected FtcEventLoopBase(HardwareFactory hardwareFactory, OpModeRegister opModeRegister, UpdateUI.Callback callback, Activity activity) {
        this.userOpmodeRegister = opModeRegister;
        this.registeredOpModes = RegisteredOpModes.getInstance();
        this.activityContext = activity;
        this.robotCfgFileMgr = new RobotConfigFileManager(activity);
        this.ftcEventLoopHandler = new FtcEventLoopHandler(hardwareFactory, callback, activity);
        this.usbScanManager = null;
    }

    /* access modifiers changed from: protected */
    public USBScanManager startUsbScanMangerIfNecessary() {
        USBScanManager uSBScanManager = this.usbScanManager;
        if (uSBScanManager != null) {
            return uSBScanManager;
        }
        USBScanManager uSBScanManager2 = new USBScanManager(this.activityContext, false);
        this.usbScanManager = uSBScanManager2;
        uSBScanManager2.startExecutorService();
        return uSBScanManager2;
    }

    public void teardown() throws RobotCoreException, InterruptedException {
        USBScanManager uSBScanManager = this.usbScanManager;
        if (uSBScanManager != null) {
            uSBScanManager.stopExecutorService();
            this.usbScanManager = null;
        }
        this.ftcEventLoopHandler.close();
    }

    public CallbackResult processCommand(Command command) throws InterruptedException, RobotCoreException {
        CallbackResult callbackResult = CallbackResult.HANDLED;
        String name = command.getName();
        String extra = command.getExtra();
        if (name.equals(CommandList.CMD_RESTART_ROBOT)) {
            handleCommandRestartRobot();
            return callbackResult;
        } else if (name.equals(CommandList.CMD_REQUEST_CONFIGURATIONS)) {
            handleCommandRequestConfigurations();
            return callbackResult;
        } else if (name.equals(CommandList.CMD_REQUEST_REMEMBERED_GROUPS)) {
            handleCommandRequestRememberedGroups();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_CLEAR_REMEMBERED_GROUPS)) {
            handleCommandClearRememberedGroups();
            return callbackResult;
        } else if (name.equals(CommandList.CMD_SCAN)) {
            handleCommandScan(extra);
            return callbackResult;
        } else if (name.equals(CommandList.CMD_DISCOVER_LYNX_MODULES)) {
            handleCommandDiscoverLynxModules(extra);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_LYNX_FIRMWARE_UPDATE)) {
            handleCommandLynxFirmwareUpdate(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES)) {
            handleCommandGetUSBAccessibleLynxModules(command);
            return callbackResult;
        } else if (name.equals(CommandList.CMD_LYNX_ADDRESS_CHANGE)) {
            handleCommandLynxChangeModuleAddresses(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES)) {
            handleCommandGetCandidateLynxFirmwareImages(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_INSPECTION_REPORT)) {
            handleCommandRequestInspectionReport();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_DISABLE_BLUETOOTH)) {
            handleCommandDisableBluetooth();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_ABOUT_INFO)) {
            handleCommandRequestAboutInfo(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_DISCONNECT_FROM_WIFI_DIRECT)) {
            handleCommandDisconnectWifiDirect();
            return callbackResult;
        } else if (name.equals(CommandList.CMD_REQUEST_CONFIGURATION_TEMPLATES)) {
            handleCommandRequestConfigurationTemplates();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION)) {
            handleCommandRequestParticularConfiguration(extra);
            return callbackResult;
        } else if (name.equals(CommandList.CMD_ACTIVATE_CONFIGURATION)) {
            handleCommandActivateConfiguration(extra);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_USER_DEVICE_TYPES)) {
            ConfigurationTypeManager.getInstance().sendUserDeviceTypes();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_ACTIVE_CONFIG)) {
            sendActiveConfig();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_OP_MODE_LIST)) {
            sendOpModeList();
            return callbackResult;
        } else if (name.equals(CommandList.CMD_SAVE_CONFIGURATION)) {
            handleCommandSaveConfiguration(extra);
            return callbackResult;
        } else if (name.equals(CommandList.CMD_DELETE_CONFIGURATION)) {
            handleCommandDeleteConfiguration(extra);
            return callbackResult;
        } else if (name.equals(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE)) {
            handleCommandStartDriverStationProgramAndManage();
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_SHOW_TOAST)) {
            handleCommandShowToast(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_SHOW_DIALOG)) {
            handleCommandShowDialog(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_DISMISS_DIALOG)) {
            handleCommandDismissDialog(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_DISMISS_ALL_DIALOGS)) {
            handleCommandDismissAllDialogs(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_SHOW_PROGRESS)) {
            handleCommandShowProgress(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_DISMISS_PROGRESS)) {
            handleCommandDismissProgress(command);
            return callbackResult;
        } else if (name.equals(RobotCoreCommandList.CMD_ROBOT_CONTROLLER_PREFERENCE)) {
            return PreferenceRemoterRC.getInstance().handleCommandRobotControllerPreference(extra);
        } else {
            if (name.equals(CommandList.CmdPlaySound.Command)) {
                return SoundPlayer.getInstance().handleCommandPlaySound(extra);
            }
            if (name.equals(CommandList.CmdRequestSound.Command)) {
                return SoundPlayer.getInstance().handleCommandRequestSound(command);
            }
            if (name.equals(CommandList.CmdStopPlayingSounds.Command)) {
                return SoundPlayer.getInstance().handleCommandStopPlayingSounds(command);
            }
            if (name.equals(RobotCoreCommandList.CMD_REQUEST_FRAME)) {
                return CameraStreamServer.getInstance().handleRequestFrame();
            }
            if (name.equals(CommandList.CmdVisuallyIdentify.Command)) {
                return handleCommandVisuallyIdentify(command);
            }
            if (name.equals(RobotCoreCommandList.CMD_VISUALLY_CONFIRM_WIFI_RESET)) {
                return handleCommandVisuallyConfirmWifiReset();
            }
            if (name.equals(RobotCoreCommandList.CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH)) {
                return handleCommandVisuallyConfirmWifiBandSwitch(command);
            }
            return CallbackResult.NOT_HANDLED;
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandActivateConfiguration(String str) {
        this.robotCfgFileMgr.setActiveConfigAndUpdateUI(this.runningOnDriverStation, this.robotCfgFileMgr.getConfigFromString(str));
    }

    /* access modifiers changed from: protected */
    public void sendActiveConfig() {
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION, this.robotCfgFileMgr.getActiveConfig().toString()));
    }

    /* access modifiers changed from: protected */
    public void sendOpModeList() {
        this.registeredOpModes.waitOpModesRegistered();
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_OP_MODE_LIST, SimpleGson.getInstance().toJson((Object) this.registeredOpModes.getOpModes())));
    }

    /* access modifiers changed from: protected */
    public void checkForChangedOpModes() {
        boolean z;
        boolean z2 = true;
        if (this.registeredOpModes.getOnBotJavaChanged()) {
            OnBotJavaBuildLocker.lockBuildExclusiveWhile(new Runnable() {
                public void run() {
                    FtcEventLoopBase.this.registeredOpModes.clearOnBotJavaChanged();
                    FtcEventLoopBase.this.registeredOpModes.registerOnBotJavaOpModes();
                }
            });
            z = true;
        } else {
            z = false;
        }
        if (this.registeredOpModes.getExternalLibrariesChanged()) {
            this.registeredOpModes.clearExternalLibrariesChanged();
            this.registeredOpModes.registerExternalLibrariesOpModes();
            z = true;
        }
        if (this.registeredOpModes.getBlocksOpModesChanged()) {
            this.registeredOpModes.clearBlocksOpModesChanged();
            this.registeredOpModes.registerInstanceOpModes();
        } else {
            z2 = z;
        }
        if (z2) {
            sendOpModeList();
            ConfigurationTypeManager.getInstance().sendUserDeviceTypes();
        }
    }

    public void loop() {
        AppAliveNotifier.getInstance().notifyAppAlive();
    }

    /* access modifiers changed from: protected */
    public void handleCommandRestartRobot() {
        this.ftcEventLoopHandler.restartRobot();
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestParticularConfiguration(String str) {
        RobotConfigFile configFromString = this.robotCfgFileMgr.getConfigFromString(str);
        ReadXMLFileHandler readXMLFileHandler = new ReadXMLFileHandler();
        if (!configFromString.isNoConfig()) {
            try {
                String xml = new WriteXMLFileHandler().toXml((ArrayList) readXMLFileHandler.parse(configFromString.getXml()));
                RobotLog.m60vv("FtcConfigTag", "FtcEventLoop: handleCommandRequestParticularConfigFile, data: " + xml);
                this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP, xml));
            } catch (RobotCoreException | FileNotFoundException | XmlPullParserException e) {
                RobotLog.m50ee(TAG, e, "Failed to get and/or parse the requested configuration file");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandDeleteConfiguration(String str) {
        RobotConfigFile configFromString = this.robotCfgFileMgr.getConfigFromString(str);
        if (!RobotConfigFileManager.getFullPath(configFromString.getName()).delete()) {
            RobotLog.m48ee(TAG, "Tried to delete a file that does not exist: " + configFromString.getName());
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandSaveConfiguration(String str) {
        String[] split = str.split(RobotConfigFileManager.FILE_LIST_COMMAND_DELIMITER);
        try {
            RobotConfigFile configFromString = this.robotCfgFileMgr.getConfigFromString(split[0]);
            this.robotCfgFileMgr.writeToFile(configFromString, false, split[1]);
            this.robotCfgFileMgr.setActiveConfigAndUpdateUI(false, configFromString);
        } catch (RobotCoreException | IOException e) {
            e.printStackTrace();
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestConfigurations() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATIONS_RESP, RobotConfigFileManager.serializeXMLConfigList(this.robotCfgFileMgr.getXMLFiles())));
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestRememberedGroups() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_REMEMBERED_GROUPS_RESP, WifiDirectGroupName.serializeNames(new WifiDirectPersistentGroupManager(WifiDirectAgent.getInstance()).getPersistentGroups())));
    }

    /* access modifiers changed from: protected */
    public void handleCommandClearRememberedGroups() {
        new WifiDirectPersistentGroupManager(WifiDirectAgent.getInstance()).deleteAllPersistentGroups();
        AppUtil.getInstance().showToast(UILocation.BOTH, AppUtil.getDefContext().getString(C0470R.string.toastWifiP2pRememberedGroupsCleared));
    }

    /* access modifiers changed from: protected */
    public void handleCommandScan(String str) throws RobotCoreException, InterruptedException {
        RobotLog.m60vv("FtcConfigTag", "handling command SCAN");
        final USBScanManager startUsbScanMangerIfNecessary = startUsbScanMangerIfNecessary();
        final ThreadPool.SingletonResult<ScannedDevices> startDeviceScanIfNecessary = startUsbScanMangerIfNecessary.startDeviceScanIfNecessary();
        ThreadPool.getDefault().execute(new Runnable() {
            public void run() {
                try {
                    ScannedDevices scannedDevices = (ScannedDevices) startDeviceScanIfNecessary.await();
                    if (scannedDevices == null) {
                        scannedDevices = new ScannedDevices();
                    }
                    String packageCommandResponse = startUsbScanMangerIfNecessary.packageCommandResponse(scannedDevices);
                    RobotLog.m61vv("FtcConfigTag", "handleCommandScan data='%s'", packageCommandResponse);
                    FtcEventLoopBase.this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_SCAN_RESP, packageCommandResponse));
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void handleCommandDiscoverLynxModules(String str) throws RobotCoreException {
        RobotLog.m60vv("FtcConfigTag", "handling command DiscoverLynxModules");
        final SerialNumber fromString = SerialNumber.fromString(str);
        final USBScanManager startUsbScanMangerIfNecessary = startUsbScanMangerIfNecessary();
        final ThreadPool.SingletonResult<LynxModuleMetaList> startLynxModuleEnumerationIfNecessary = this.usbScanManager.startLynxModuleEnumerationIfNecessary(fromString);
        ThreadPool.getDefault().execute(new Runnable() {
            public void run() {
                try {
                    LynxModuleMetaList lynxModuleMetaList = (LynxModuleMetaList) startLynxModuleEnumerationIfNecessary.await();
                    if (lynxModuleMetaList == null) {
                        lynxModuleMetaList = new LynxModuleMetaList(fromString);
                    }
                    String packageCommandResponse = startUsbScanMangerIfNecessary.packageCommandResponse(lynxModuleMetaList);
                    RobotLog.m61vv("FtcConfigTag", "DiscoverLynxModules data='%s'", packageCommandResponse);
                    FtcEventLoopBase.this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_DISCOVER_LYNX_MODULES_RESP, packageCommandResponse));
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void handleCommandLynxFirmwareUpdate(final Command command) {
        RobotLog.m60vv(TAG, "handleCommandLynxFirmwareUpdate received");
        final RobotCoreCommandList.LynxFirmwareUpdate deserialize = RobotCoreCommandList.LynxFirmwareUpdate.deserialize(command.getExtra());
        ThreadPool.getDefault().submit(new Runnable() {
            public void run() {
                FtcEventLoopBase.this.networkConnectionHandler.sendReply(command, new Command(RobotCoreCommandList.CMD_LYNX_FIRMWARE_UPDATE_RESP, FtcEventLoopBase.this.updateLynxFirmware(deserialize.serialNumber, deserialize.firmwareImageFile, deserialize.originatorId).serialize()));
            }
        });
    }

    /* access modifiers changed from: protected */
    public RobotCoreCommandList.LynxFirmwareUpdateResp updateLynxFirmware(final SerialNumber serialNumber, final RobotCoreCommandList.FWImage fWImage, String str) {
        LynxUsbDevice lynxUsbDeviceForFirmwareUpdate;
        RobotCoreCommandList.LynxFirmwareUpdateResp lynxFirmwareUpdateResp = new RobotCoreCommandList.LynxFirmwareUpdateResp();
        lynxFirmwareUpdateResp.success = false;
        lynxFirmwareUpdateResp.originatorId = str;
        final boolean isEmbedded = serialNumber.isEmbedded();
        C04465 r3 = new Consumer<ProgressParameters>() {
            Double prevPercentComplete = null;

            public void accept(ProgressParameters progressParameters) {
                String str;
                double round = (double) Math.round(progressParameters.fractionComplete() * 100.0d);
                Double d = this.prevPercentComplete;
                if (d == null || d.doubleValue() != round) {
                    this.prevPercentComplete = Double.valueOf(round);
                    if (isEmbedded) {
                        str = String.format(FtcEventLoopBase.this.activityContext.getString(C0470R.string.controlHubFirmwareUpdateMessage), new Object[]{fWImage.getName()});
                    } else {
                        str = String.format(FtcEventLoopBase.this.activityContext.getString(C0470R.string.expansionHubFirmwareUpdateMessage), new Object[]{serialNumber, fWImage.getName()});
                    }
                    AppUtil.getInstance().showProgress(UILocation.BOTH, str, progressParameters.fractionComplete(), 100);
                }
            }
        };
        try {
            r3.accept(new ProgressParameters(0, 1));
            lynxUsbDeviceForFirmwareUpdate = getLynxUsbDeviceForFirmwareUpdate(serialNumber);
            if (lynxUsbDeviceForFirmwareUpdate != null) {
                lynxFirmwareUpdateResp = lynxUsbDeviceForFirmwareUpdate.updateFirmware(fWImage, str, r3);
                lynxUsbDeviceForFirmwareUpdate.close();
            } else {
                RobotLog.m49ee(TAG, "unable to obtain lynx usb device for fw update: %s", serialNumber);
            }
            AppUtil.getInstance().dismissProgress(UILocation.BOTH);
            RobotLog.m61vv(TAG, "updateLynxFirmware(%s, %s): result=%s", serialNumber, fWImage.getName(), lynxFirmwareUpdateResp.serialize());
            return lynxFirmwareUpdateResp;
        } catch (Throwable th) {
            AppUtil.getInstance().dismissProgress(UILocation.BOTH);
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void handleCommandGetUSBAccessibleLynxModules(final Command command) {
        ThreadPool.getDefault().execute(new Runnable() {
            public void run() {
                RobotCoreCommandList.USBAccessibleLynxModulesRequest deserialize = RobotCoreCommandList.USBAccessibleLynxModulesRequest.deserialize(command.getExtra());
                ArrayList<USBAccessibleLynxModule> arrayList = new ArrayList<>();
                try {
                    arrayList.addAll(FtcEventLoopBase.this.getUSBAccessibleLynxDevices(deserialize.forFirmwareUpdate));
                } catch (RobotCoreException unused) {
                }
                Collections.sort(arrayList, new Comparator<USBAccessibleLynxModule>() {
                    public int compare(USBAccessibleLynxModule uSBAccessibleLynxModule, USBAccessibleLynxModule uSBAccessibleLynxModule2) {
                        return uSBAccessibleLynxModule.getSerialNumber().getString().compareTo(uSBAccessibleLynxModule2.getSerialNumber().getString());
                    }
                });
                RobotCoreCommandList.USBAccessibleLynxModulesResp uSBAccessibleLynxModulesResp = new RobotCoreCommandList.USBAccessibleLynxModulesResp();
                uSBAccessibleLynxModulesResp.modules = arrayList;
                FtcEventLoopBase.this.networkConnectionHandler.sendReply(command, new Command(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP, uSBAccessibleLynxModulesResp.serialize()));
            }
        });
    }

    /* access modifiers changed from: protected */
    public LynxUsbDevice getLynxUsbDeviceForFirmwareUpdate(SerialNumber serialNumber) {
        try {
            return (LynxUsbDevice) startUsbScanMangerIfNecessary().getDeviceManager().createLynxUsbDevice(serialNumber, (String) null);
        } catch (RobotCoreException e) {
            RobotLog.m51ee(TAG, e, "getLynxUsbDeviceForFirmwareUpdate(): exception opening lynx usb device: %s", serialNumber);
            return null;
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
            RobotLog.m48ee(TAG, "Thread interrupted in getLynxUsbDeviceForFirmwareUpdate");
            return null;
        }
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:61:0x0154, code lost:
        r14 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:63:?, code lost:
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:65:0x0165, code lost:
        return new java.util.ArrayList();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:66:0x0166, code lost:
        com.qualcomm.robotcore.util.RobotLog.m60vv(TAG, "...getUSBAccessibleLynxDevices()");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:67:0x0169, code lost:
        throw r14;
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:62:0x0156 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public java.util.List<com.qualcomm.robotcore.hardware.USBAccessibleLynxModule> getUSBAccessibleLynxDevices(boolean r14) throws com.qualcomm.robotcore.exception.RobotCoreException {
        /*
            r13 = this;
            java.lang.String r0 = "...getUSBAccessibleLynxDevices()"
            r1 = 1
            java.lang.Object[] r2 = new java.lang.Object[r1]
            java.lang.Boolean r3 = java.lang.Boolean.valueOf(r14)
            r4 = 0
            r2[r4] = r3
            java.lang.String r3 = "FtcEventLoop"
            java.lang.String r5 = "getUSBAccessibleLynxDevices(includeModuleAddresses=%s)..."
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r3, (java.lang.String) r5, (java.lang.Object[]) r2)
            com.qualcomm.ftccommon.configuration.USBScanManager r2 = r13.startUsbScanMangerIfNecessary()
            com.qualcomm.robotcore.util.ThreadPool$SingletonResult r5 = r2.startDeviceScanIfNecessary()
            java.lang.Object r5 = r5.await()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.ScannedDevices r5 = (com.qualcomm.robotcore.hardware.ScannedDevices) r5     // Catch:{ InterruptedException -> 0x0156 }
            java.util.ArrayList r6 = new java.util.ArrayList     // Catch:{ InterruptedException -> 0x0156 }
            r6.<init>()     // Catch:{ InterruptedException -> 0x0156 }
            java.util.Set r5 = r5.entrySet()     // Catch:{ InterruptedException -> 0x0156 }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ InterruptedException -> 0x0156 }
        L_0x002e:
            boolean r7 = r5.hasNext()     // Catch:{ InterruptedException -> 0x0156 }
            if (r7 == 0) goto L_0x005a
            java.lang.Object r7 = r5.next()     // Catch:{ InterruptedException -> 0x0156 }
            java.util.Map$Entry r7 = (java.util.Map.Entry) r7     // Catch:{ InterruptedException -> 0x0156 }
            java.lang.Object r8 = r7.getValue()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r9 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.LYNX_USB_DEVICE     // Catch:{ InterruptedException -> 0x0156 }
            if (r8 != r9) goto L_0x002e
            java.lang.Object r7 = r7.getKey()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r7 = (com.qualcomm.robotcore.util.SerialNumber) r7     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.USBAccessibleLynxModule r8 = new com.qualcomm.robotcore.hardware.USBAccessibleLynxModule     // Catch:{ InterruptedException -> 0x0156 }
            boolean r9 = r7.isEmbedded()     // Catch:{ InterruptedException -> 0x0156 }
            if (r9 != 0) goto L_0x0052
            r9 = r1
            goto L_0x0053
        L_0x0052:
            r9 = r4
        L_0x0053:
            r8.<init>(r7, r9)     // Catch:{ InterruptedException -> 0x0156 }
            r6.add(r8)     // Catch:{ InterruptedException -> 0x0156 }
            goto L_0x002e
        L_0x005a:
            boolean r5 = com.qualcomm.robotcore.hardware.configuration.LynxConstants.isRevControlHub()     // Catch:{ InterruptedException -> 0x0156 }
            if (r5 == 0) goto L_0x008b
            java.util.Iterator r5 = r6.iterator()     // Catch:{ InterruptedException -> 0x0156 }
        L_0x0064:
            boolean r7 = r5.hasNext()     // Catch:{ InterruptedException -> 0x0156 }
            if (r7 == 0) goto L_0x007e
            java.lang.Object r7 = r5.next()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.USBAccessibleLynxModule r7 = (com.qualcomm.robotcore.hardware.USBAccessibleLynxModule) r7     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r7 = r7.getSerialNumber()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r8 = com.qualcomm.robotcore.hardware.configuration.LynxConstants.SERIAL_NUMBER_EMBEDDED     // Catch:{ InterruptedException -> 0x0156 }
            boolean r7 = r7.equals((java.lang.Object) r8)     // Catch:{ InterruptedException -> 0x0156 }
            if (r7 == 0) goto L_0x0064
            r5 = r1
            goto L_0x007f
        L_0x007e:
            r5 = r4
        L_0x007f:
            if (r5 != 0) goto L_0x008b
            com.qualcomm.robotcore.hardware.USBAccessibleLynxModule r5 = new com.qualcomm.robotcore.hardware.USBAccessibleLynxModule     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r7 = com.qualcomm.robotcore.hardware.configuration.LynxConstants.SERIAL_NUMBER_EMBEDDED     // Catch:{ InterruptedException -> 0x0156 }
            r5.<init>(r7, r4)     // Catch:{ InterruptedException -> 0x0156 }
            r6.add(r5)     // Catch:{ InterruptedException -> 0x0156 }
        L_0x008b:
            java.util.Iterator r5 = r6.iterator()     // Catch:{ InterruptedException -> 0x0156 }
        L_0x008f:
            boolean r7 = r5.hasNext()     // Catch:{ InterruptedException -> 0x0156 }
            if (r7 == 0) goto L_0x00a9
            java.lang.Object r7 = r5.next()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.USBAccessibleLynxModule r7 = (com.qualcomm.robotcore.hardware.USBAccessibleLynxModule) r7     // Catch:{ InterruptedException -> 0x0156 }
            java.lang.String r8 = "getUSBAccessibleLynxDevices: found serial=%s"
            java.lang.Object[] r9 = new java.lang.Object[r1]     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r7 = r7.getSerialNumber()     // Catch:{ InterruptedException -> 0x0156 }
            r9[r4] = r7     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r3, (java.lang.String) r8, (java.lang.Object[]) r9)     // Catch:{ InterruptedException -> 0x0156 }
            goto L_0x008f
        L_0x00a9:
            if (r14 == 0) goto L_0x013f
            java.lang.String r14 = "finding module addresses and current firmware versions"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r3, r14)     // Catch:{ InterruptedException -> 0x0156 }
            r14 = r4
        L_0x00b1:
            int r5 = r6.size()     // Catch:{ InterruptedException -> 0x0156 }
            if (r14 >= r5) goto L_0x013f
            java.lang.Object r5 = r6.get(r14)     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.USBAccessibleLynxModule r5 = (com.qualcomm.robotcore.hardware.USBAccessibleLynxModule) r5     // Catch:{ InterruptedException -> 0x0156 }
            java.lang.String r7 = "getUSBAccessibleLynxDevices: finding module address for usbModule %s"
            java.lang.Object[] r8 = new java.lang.Object[r1]     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r9 = r5.getSerialNumber()     // Catch:{ InterruptedException -> 0x0156 }
            r8[r4] = r9     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r3, (java.lang.String) r7, (java.lang.Object[]) r8)     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.DeviceManager r7 = r2.getDeviceManager()     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.SerialNumber r8 = r5.getSerialNumber()     // Catch:{ InterruptedException -> 0x0156 }
            r9 = 0
            com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r7 = r7.createLynxUsbDevice(r8, r9)     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.hardware.lynx.LynxUsbDevice r7 = (com.qualcomm.hardware.lynx.LynxUsbDevice) r7     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.hardware.LynxModuleMetaList r8 = r7.discoverModules(r4)     // Catch:{ all -> 0x0138 }
            r5.setModuleAddress(r4)     // Catch:{ all -> 0x0138 }
            java.util.Iterator r8 = r8.iterator()     // Catch:{ all -> 0x0138 }
            r9 = r4
        L_0x00e5:
            boolean r10 = r8.hasNext()     // Catch:{ all -> 0x0138 }
            if (r10 == 0) goto L_0x0115
            java.lang.Object r10 = r8.next()     // Catch:{ all -> 0x0138 }
            com.qualcomm.robotcore.hardware.LynxModuleMeta r10 = (com.qualcomm.robotcore.hardware.LynxModuleMeta) r10     // Catch:{ all -> 0x0138 }
            java.lang.String r11 = "assessing %s"
            java.lang.Object[] r12 = new java.lang.Object[r1]     // Catch:{ all -> 0x0138 }
            r12[r4] = r10     // Catch:{ all -> 0x0138 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r3, (java.lang.String) r11, (java.lang.Object[]) r12)     // Catch:{ all -> 0x0138 }
            int r11 = r10.getModuleAddress()     // Catch:{ all -> 0x0138 }
            if (r11 != 0) goto L_0x0106
            java.lang.String r10 = "ignoring module with address zero"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r3, r10)     // Catch:{ all -> 0x0138 }
            goto L_0x00e5
        L_0x0106:
            boolean r11 = r10.isParent()     // Catch:{ all -> 0x0138 }
            if (r11 == 0) goto L_0x00e5
            int r9 = r10.getModuleAddress()     // Catch:{ all -> 0x0138 }
            r5.setModuleAddress(r9)     // Catch:{ all -> 0x0138 }
            r9 = r1
            goto L_0x00e5
        L_0x0115:
            java.lang.String r8 = ""
            r5.setFirmwareVersionString(r8)     // Catch:{ all -> 0x0138 }
            if (r9 == 0) goto L_0x012f
            int r8 = r5.getModuleAddress()     // Catch:{ RobotCoreException -> 0x0129 }
            com.qualcomm.ftccommon.FtcEventLoopBase$7 r9 = new com.qualcomm.ftccommon.FtcEventLoopBase$7     // Catch:{ RobotCoreException -> 0x0129 }
            r9.<init>(r5)     // Catch:{ RobotCoreException -> 0x0129 }
            r7.performSystemOperationOnConnectedModule(r8, r1, r9)     // Catch:{ RobotCoreException -> 0x0129 }
            goto L_0x012f
        L_0x0129:
            r5 = move-exception
            java.lang.String r8 = "exception retrieving fw version; ignoring"
            com.qualcomm.robotcore.util.RobotLog.m50ee((java.lang.String) r3, (java.lang.Throwable) r5, (java.lang.String) r8)     // Catch:{ all -> 0x0138 }
        L_0x012f:
            int r14 = r14 + 1
            if (r7 == 0) goto L_0x00b1
            r7.close()     // Catch:{ InterruptedException -> 0x0156 }
            goto L_0x00b1
        L_0x0138:
            r14 = move-exception
            if (r7 == 0) goto L_0x013e
            r7.close()     // Catch:{ InterruptedException -> 0x0156 }
        L_0x013e:
            throw r14     // Catch:{ InterruptedException -> 0x0156 }
        L_0x013f:
            java.lang.String r14 = "getUSBAccessibleLynxDevices(): %d modules found"
            java.lang.Object[] r1 = new java.lang.Object[r1]     // Catch:{ InterruptedException -> 0x0156 }
            int r2 = r6.size()     // Catch:{ InterruptedException -> 0x0156 }
            java.lang.Integer r2 = java.lang.Integer.valueOf(r2)     // Catch:{ InterruptedException -> 0x0156 }
            r1[r4] = r2     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r3, (java.lang.String) r14, (java.lang.Object[]) r1)     // Catch:{ InterruptedException -> 0x0156 }
            com.qualcomm.robotcore.util.RobotLog.m60vv(r3, r0)
            return r6
        L_0x0154:
            r14 = move-exception
            goto L_0x0166
        L_0x0156:
            java.lang.Thread r14 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0154 }
            r14.interrupt()     // Catch:{ all -> 0x0154 }
            java.util.ArrayList r14 = new java.util.ArrayList     // Catch:{ all -> 0x0154 }
            r14.<init>()     // Catch:{ all -> 0x0154 }
            com.qualcomm.robotcore.util.RobotLog.m60vv(r3, r0)
            return r14
        L_0x0166:
            com.qualcomm.robotcore.util.RobotLog.m60vv(r3, r0)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.FtcEventLoopBase.getUSBAccessibleLynxDevices(boolean):java.util.List");
    }

    /* access modifiers changed from: protected */
    public void handleCommandLynxChangeModuleAddresses(final Command command) {
        ThreadPool.getDefault().execute(new Runnable() {
            public void run() {
                AppUtil instance;
                UILocation uILocation;
                Activity activity;
                int i;
                LynxUsbDevice lynxUsbDevice;
                boolean z = true;
                try {
                    CommandList.LynxAddressChangeRequest deserialize = CommandList.LynxAddressChangeRequest.deserialize(command.getExtra());
                    DeviceManager deviceManager = FtcEventLoopBase.this.startUsbScanMangerIfNecessary().getDeviceManager();
                    Iterator<CommandList.LynxAddressChangeRequest.AddressChange> it = deserialize.modulesToChange.iterator();
                    while (it.hasNext()) {
                        final CommandList.LynxAddressChangeRequest.AddressChange next = it.next();
                        lynxUsbDevice = (LynxUsbDevice) deviceManager.createLynxUsbDevice(next.serialNumber, (String) null);
                        try {
                            lynxUsbDevice.performSystemOperationOnConnectedModule(next.oldAddress, true, new Consumer<LynxModule>() {
                                public void accept(LynxModule lynxModule) {
                                    RobotLog.m61vv(FtcEventLoopBase.TAG, "lynx module %s: change address %d -> %d", next.serialNumber, Integer.valueOf(next.oldAddress), Integer.valueOf(next.newAddress));
                                    lynxModule.setNewModuleAddress(next.newAddress);
                                }
                            });
                            if (lynxUsbDevice != null) {
                                lynxUsbDevice.close();
                            }
                        } catch (RobotCoreException e) {
                            RobotLog.m50ee(FtcEventLoopBase.TAG, (Throwable) e, "failure during module address change");
                            AppUtil.getInstance().showToast(UILocation.BOTH, FtcEventLoopBase.this.activityContext.getString(C0470R.string.toastLynxAddressChangeFailed, new Object[]{next.serialNumber}));
                            throw e;
                        } catch (Throwable th) {
                            th = th;
                            z = false;
                        }
                    }
                    instance = AppUtil.getInstance();
                    uILocation = UILocation.BOTH;
                    activity = FtcEventLoopBase.this.activityContext;
                    i = C0470R.string.toastLynxAddressChangeComplete;
                } catch (RobotCoreException unused) {
                    if (z) {
                        instance = AppUtil.getInstance();
                        uILocation = UILocation.BOTH;
                        activity = FtcEventLoopBase.this.activityContext;
                        i = C0470R.string.toastLynxAddressChangeComplete;
                    } else {
                        return;
                    }
                } catch (InterruptedException unused2) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable th2) {
                    if (z) {
                        AppUtil.getInstance().showToast(UILocation.BOTH, FtcEventLoopBase.this.activityContext.getString(C0470R.string.toastLynxAddressChangeComplete));
                    }
                    throw th2;
                }
                instance.showToast(uILocation, activity.getString(i));
                return;
                if (lynxUsbDevice != null) {
                    lynxUsbDevice.close();
                }
                throw th;
            }
        });
    }

    /* access modifiers changed from: protected */
    public void handleCommandGetCandidateLynxFirmwareImages(Command command) {
        final Pattern compile = Pattern.compile("(?i).*\\.bin");
        File file = AppUtil.LYNX_FIRMWARE_UPDATE_DIR;
        File[] listFiles = file.listFiles(new FileFilter() {
            public boolean accept(File file) {
                Assert.assertTrue(file.isAbsolute());
                return compile.matcher(file.getName()).matches();
            }
        });
        RobotCoreCommandList.LynxFirmwareImagesResp lynxFirmwareImagesResp = new RobotCoreCommandList.LynxFirmwareImagesResp();
        lynxFirmwareImagesResp.firstFolder = AppUtil.FIRST_FOLDER;
        for (File fWImage : listFiles) {
            lynxFirmwareImagesResp.firmwareImages.add(new RobotCoreCommandList.FWImage(fWImage, false));
        }
        try {
            File file2 = new File(file.getParentFile().getName(), file.getName());
            for (String str : this.activityContext.getAssets().list(file2.getPath())) {
                if (compile.matcher(str).matches()) {
                    File file3 = new File(file2, str);
                    Assert.assertTrue(!file3.isAbsolute());
                    lynxFirmwareImagesResp.firmwareImages.add(new RobotCoreCommandList.FWImage(file3, true));
                }
            }
        } catch (IOException unused) {
        }
        this.networkConnectionHandler.sendReply(command, new Command(RobotCoreCommandList.CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP, lynxFirmwareImagesResp.serialize()));
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestConfigurationTemplates() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATION_TEMPLATES_RESP, RobotConfigFileManager.serializeXMLConfigList(this.robotCfgFileMgr.getXMLTemplates())));
    }

    /* access modifiers changed from: protected */
    public void handleCommandStartDriverStationProgramAndManage() {
        EventLoopManager eventLoopManager = this.ftcEventLoopHandler.getEventLoopManager();
        if (eventLoopManager != null) {
            String json = eventLoopManager.getWebServer().getConnectionInformation().toJson();
            RobotLog.m61vv(TAG, "sending p&m resp: %s", json);
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE_RESP, json));
            return;
        }
        RobotLog.m60vv(TAG, "handleCommandStartDriverStationProgramAndManage() with null EventLoopManager; ignored");
    }

    /* access modifiers changed from: protected */
    public void handleCommandShowDialog(Command command) {
        RobotCoreCommandList.ShowDialog deserialize = RobotCoreCommandList.ShowDialog.deserialize(command.getExtra());
        AppUtil.DialogParams dialogParams = new AppUtil.DialogParams(UILocation.ONLY_LOCAL, deserialize.title, deserialize.message);
        dialogParams.uuidString = deserialize.uuidString;
        AppUtil.getInstance().showDialog(dialogParams);
    }

    /* access modifiers changed from: protected */
    public void handleCommandDismissDialog(Command command) {
        AppUtil.getInstance().dismissDialog(UILocation.ONLY_LOCAL, RobotCoreCommandList.DismissDialog.deserialize(command.getExtra()));
    }

    /* access modifiers changed from: protected */
    public void handleCommandDismissAllDialogs(Command command) {
        AppUtil.getInstance().dismissAllDialogs(UILocation.ONLY_LOCAL);
    }

    /* access modifiers changed from: protected */
    public void handleCommandShowProgress(Command command) {
        RobotCoreCommandList.ShowProgress deserialize = RobotCoreCommandList.ShowProgress.deserialize(command.getExtra());
        AppUtil.getInstance().showProgress(UILocation.ONLY_LOCAL, deserialize.message, (ProgressParameters) deserialize);
    }

    /* access modifiers changed from: protected */
    public void handleCommandDismissProgress(Command command) {
        AppUtil.getInstance().dismissProgress(UILocation.ONLY_LOCAL);
    }

    /* access modifiers changed from: protected */
    public void handleCommandShowToast(Command command) {
        RobotCoreCommandList.ShowToast deserialize = RobotCoreCommandList.ShowToast.deserialize(command.getExtra());
        AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, deserialize.message, deserialize.duration);
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestInspectionReport() {
        InspectionState inspectionState = new InspectionState();
        inspectionState.initializeLocal();
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_INSPECTION_REPORT_RESP, inspectionState.serialize()));
    }

    /* access modifiers changed from: protected */
    public void handleCommandDisableBluetooth() {
        AppUtil.getInstance().setBluetoothEnabled(false);
    }

    /* access modifiers changed from: protected */
    public void handleCommandRequestAboutInfo(Command command) {
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_ABOUT_INFO_RESP, FtcAboutActivity.getLocalAboutInfo().serialize()));
    }

    /* access modifiers changed from: protected */
    public void handleCommandDisconnectWifiDirect() {
        if (WifiDirectAgent.getInstance().disconnectFromWifiDirect()) {
            AppUtil.getInstance().showToast(UILocation.BOTH, AppUtil.getDefContext().getString(C0470R.string.toastDisconnectedFromWifiDirect));
        } else {
            AppUtil.getInstance().showToast(UILocation.BOTH, AppUtil.getDefContext().getString(C0470R.string.toastErrorDisconnectingFromWifiDirect));
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandVisuallyIdentify(Command command) {
        final CommandList.CmdVisuallyIdentify deserialize = CommandList.CmdVisuallyIdentify.deserialize(command.getExtra());
        ThreadPool.getDefaultSerial().execute(new Runnable() {
            public void run() {
                VisuallyIdentifiableHardwareDevice visuallyIdentifiableHardwareDevice = (VisuallyIdentifiableHardwareDevice) FtcEventLoopBase.this.ftcEventLoopHandler.getHardwareDevice(VisuallyIdentifiableHardwareDevice.class, deserialize.serialNumber, new Supplier<USBScanManager>() {
                    public USBScanManager get() {
                        return FtcEventLoopBase.this.startUsbScanMangerIfNecessary();
                    }
                });
                if (visuallyIdentifiableHardwareDevice != null) {
                    visuallyIdentifiableHardwareDevice.visuallyIdentify(deserialize.shouldIdentify);
                }
            }
        });
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandVisuallyConfirmWifiReset() {
        if (!LynxConstants.isRevControlHub()) {
            return CallbackResult.HANDLED;
        }
        ThreadPool.getDefaultSerial().execute(new Runnable() {
            public void run() {
                LynxModule lynxModule = EmbeddedControlHubModule.get();
                if (lynxModule != null) {
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new Blinker.Step(-65281, 100, TimeUnit.MILLISECONDS));
                    arrayList.add(new Blinker.Step(-256, 100, TimeUnit.MILLISECONDS));
                    arrayList.add(new Blinker.Step(-16711681, 100, TimeUnit.MILLISECONDS));
                    arrayList.add(new Blinker.Step(SupportMenu.CATEGORY_MASK, 100, TimeUnit.MILLISECONDS));
                    lynxModule.pushPattern(arrayList);
                    try {
                        Thread.sleep(4000);
                    } catch (InterruptedException e) {
                        RobotLog.m50ee(FtcEventLoopBase.TAG, (Throwable) e, "Thread interrupted while visually confirming Wi-Fi reset");
                        Thread.currentThread().interrupt();
                    }
                    lynxModule.popPattern();
                }
            }
        });
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandVisuallyConfirmWifiBandSwitch(Command command) {
        if (!LynxConstants.isRevControlHub()) {
            return CallbackResult.HANDLED;
        }
        final int parseInt = Integer.parseInt(command.getExtra());
        ThreadPool.getDefaultSerial().execute(new Runnable() {
            public void run() {
                LynxModule lynxModule = EmbeddedControlHubModule.get();
                if (lynxModule != null) {
                    int i = parseInt == 1 ? -65281 : -256;
                    ArrayList arrayList = new ArrayList();
                    arrayList.add(new Blinker.Step(i, 200, TimeUnit.MILLISECONDS));
                    arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, 100, TimeUnit.MILLISECONDS));
                    lynxModule.pushPattern(arrayList);
                    try {
                        Thread.sleep(6000);
                    } catch (InterruptedException e) {
                        RobotLog.m50ee(FtcEventLoopBase.TAG, (Throwable) e, "Thread interrupted while visually confirming Wi-Fi band switch");
                        Thread.currentThread().interrupt();
                    }
                    lynxModule.popPattern();
                }
            }
        });
        return CallbackResult.HANDLED;
    }
}
