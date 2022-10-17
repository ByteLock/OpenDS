package com.qualcomm.ftccommon.configuration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.robotcore.exception.DuplicateNameException;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxModuleConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxUsbDeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Semaphore;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;

public class FtcConfigurationActivity extends EditActivity {
    protected static final boolean DEBUG = false;
    public static final String TAG = "FtcConfigTag";
    public static final RequestCode requestCode = RequestCode.EDIT_FILE;
    protected final RecvLoopRunnable.RecvLoopCallback commandCallback = new CommandCallback();
    DialogInterface.OnClickListener doNothingAndCloseListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogInterface, int i) {
        }
    };
    protected Semaphore feedbackPosted = new Semaphore(0);
    protected int idFeedbackAnchor = C0470R.C0472id.feedbackAnchor;
    protected long msSaveSplashDelay = 1000;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected final Object robotConfigMapLock = new Object();
    protected ThreadPool.Singleton scanButtonSingleton = new ThreadPool.Singleton();
    protected USBScanManager usbScanManager = null;

    public String getTag() {
        return "FtcConfigTag";
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RobotLog.m60vv("FtcConfigTag", "onCreate()");
        setContentView(C0470R.layout.activity_ftc_configuration);
        try {
            deserialize(EditParameters.fromIntent(this, getIntent()));
            ((Button) findViewById(C0470R.C0472id.scanButton)).setVisibility(0);
            ((Button) findViewById(C0470R.C0472id.doneButton)).setText(C0470R.string.buttonNameSave);
            startExecutorService();
        } catch (RobotCoreException unused) {
            RobotLog.m48ee("FtcConfigTag", "exception thrown during FtcConfigurationActivity.onCreate()");
            finishCancel();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if (this.remoteConfigure) {
            this.networkConnectionHandler.pushReceiveLoopCallback(this.commandCallback);
        }
        if (!this.remoteConfigure) {
            this.robotConfigFileManager.createConfigFolder();
        }
        if (!this.currentCfgFile.isDirty()) {
            ensureConfigFileIsFresh();
        }
    }

    /* access modifiers changed from: protected */
    public void ensureConfigFileIsFresh() {
        if (this.haveRobotConfigMapParameter) {
            populateListAndWarnDevices();
        } else if (this.remoteConfigure) {
            this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION, this.currentCfgFile.toString()));
        } else {
            readFile();
        }
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
    }

    public void onPause() {
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        if (this.remoteConfigure) {
            this.networkConnectionHandler.removeReceiveLoopCallback(this.commandCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        RobotLog.m60vv("FtcConfigTag", "FtcConfigurationActivity.onDestroy()");
        super.onDestroy();
        stopExecutorService();
    }

    public void onDevicesInfoButtonPressed(View view) {
        RobotLog.m60vv("FtcConfigTag", "onDevicesInfoButtonPressed()");
        AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.titleDevices), getString(C0470R.string.msgInfoHowToUse));
        buildBuilder.setPositiveButton(getString(C0470R.string.buttonNameOK), this.doNothingAndCloseListener);
        AlertDialog create = buildBuilder.create();
        create.show();
        ((TextView) create.findViewById(16908299)).setTextSize(14.0f);
    }

    public void onDoneInfoButtonPressed(View view) {
        RobotLog.m60vv("FtcConfigTag", "onDoneInfoButtonPressed()");
        AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.titleSaveConfiguration), getString(C0470R.string.msgInfoSave));
        buildBuilder.setPositiveButton(getString(C0470R.string.buttonNameOK), this.doNothingAndCloseListener);
        AlertDialog create = buildBuilder.create();
        create.show();
        ((TextView) create.findViewById(16908299)).setTextSize(14.0f);
    }

    public void onScanButtonPressed(View view) {
        dirtyCheckThenSingletonUSBScanAndUpdateUI(true);
    }

    /* access modifiers changed from: package-private */
    public void dirtyCheckThenSingletonUSBScanAndUpdateUI(final boolean z) {
        final C05122 r0 = new Runnable() {
            public void run() {
                ThreadPool.logThreadLifeCycle("USB bus scan handler", new Runnable() {
                    public void run() {
                        if (z) {
                            FtcConfigurationActivity.this.synchronouslySetFeedbackWhile(FtcConfigurationActivity.this.getString(C0470R.string.ftcConfigScanning), InspectionState.NO_VERSION, new Runnable() {
                                public void run() {
                                    FtcConfigurationActivity.this.doUSBScanAndUpdateUI();
                                }
                            });
                        } else {
                            FtcConfigurationActivity.this.doUSBScanAndUpdateUI();
                        }
                    }
                });
            }
        };
        if (this.currentCfgFile.isDirty()) {
            AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.titleUnsavedChanges), getString(C0470R.string.msgAlertBeforeScan));
            buildBuilder.setPositiveButton(C0470R.string.buttonNameOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcConfigurationActivity.this.scanButtonSingleton.submit(ThreadPool.Singleton.INFINITE_TIMEOUT, r0);
                }
            });
            buildBuilder.setNegativeButton(C0470R.string.buttonNameCancel, this.doNothingAndCloseListener);
            buildBuilder.show();
            return;
        }
        this.scanButtonSingleton.submit(ThreadPool.Singleton.INFINITE_TIMEOUT, (Runnable) r0);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0058, code lost:
        com.qualcomm.robotcore.util.RobotLog.m60vv("FtcConfigTag", "...doUSBScanAndUpdateUI()");
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x005b, code lost:
        throw r2;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:6:0x004b, code lost:
        r2 = move-exception;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:8:?, code lost:
        java.lang.Thread.currentThread().interrupt();
     */
    /* JADX WARNING: Failed to process nested try/catch */
    /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x004d */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void doUSBScanAndUpdateUI() {
        /*
            r7 = this;
            java.lang.String r0 = "...doUSBScanAndUpdateUI()"
            java.lang.String r1 = "FtcConfigTag"
            java.lang.String r2 = "doUSBScanAndUpdateUI()..."
            com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r2)
            com.qualcomm.ftccommon.configuration.USBScanManager r2 = r7.usbScanManager     // Catch:{ InterruptedException -> 0x004d }
            com.qualcomm.robotcore.util.ThreadPool$SingletonResult r2 = r2.startDeviceScanIfNecessary()     // Catch:{ InterruptedException -> 0x004d }
            java.lang.Object r2 = r2.await()     // Catch:{ InterruptedException -> 0x004d }
            com.qualcomm.robotcore.hardware.ScannedDevices r2 = (com.qualcomm.robotcore.hardware.ScannedDevices) r2     // Catch:{ InterruptedException -> 0x004d }
            if (r2 == 0) goto L_0x0038
            java.lang.String r3 = "scan for devices on USB bus found %d devices"
            r4 = 1
            java.lang.Object[] r4 = new java.lang.Object[r4]     // Catch:{ InterruptedException -> 0x004d }
            r5 = 0
            int r6 = r2.size()     // Catch:{ InterruptedException -> 0x004d }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ InterruptedException -> 0x004d }
            r4[r5] = r6     // Catch:{ InterruptedException -> 0x004d }
            com.qualcomm.robotcore.util.RobotLog.m43dd((java.lang.String) r1, (java.lang.String) r3, (java.lang.Object[]) r4)     // Catch:{ InterruptedException -> 0x004d }
            r7.buildRobotConfigMapFromScanned(r2)     // Catch:{ InterruptedException -> 0x004d }
            org.firstinspires.ftc.robotcore.internal.system.AppUtil r2 = r7.appUtil     // Catch:{ InterruptedException -> 0x004d }
            com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$4 r3 = new com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$4     // Catch:{ InterruptedException -> 0x004d }
            r3.<init>()     // Catch:{ InterruptedException -> 0x004d }
            r2.synchronousRunOnUiThread(r3)     // Catch:{ InterruptedException -> 0x004d }
            goto L_0x0054
        L_0x0038:
            java.lang.String r2 = "scan for devices on USB bus failed"
            com.qualcomm.robotcore.util.RobotLog.m48ee(r1, r2)     // Catch:{ InterruptedException -> 0x004d }
            org.firstinspires.ftc.robotcore.internal.system.AppUtil r2 = r7.appUtil     // Catch:{ InterruptedException -> 0x004d }
            org.firstinspires.ftc.robotcore.internal.ui.UILocation r3 = org.firstinspires.ftc.robotcore.internal.p013ui.UILocation.ONLY_LOCAL     // Catch:{ InterruptedException -> 0x004d }
            int r4 = com.qualcomm.ftccommon.C0470R.string.ftcConfigScanningFailed     // Catch:{ InterruptedException -> 0x004d }
            java.lang.String r4 = r7.getString(r4)     // Catch:{ InterruptedException -> 0x004d }
            r2.showToast(r3, r4)     // Catch:{ InterruptedException -> 0x004d }
            goto L_0x0054
        L_0x004b:
            r2 = move-exception
            goto L_0x0058
        L_0x004d:
            java.lang.Thread r2 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x004b }
            r2.interrupt()     // Catch:{ all -> 0x004b }
        L_0x0054:
            com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r0)
            return
        L_0x0058:
            com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r0)
            throw r2
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.configuration.FtcConfigurationActivity.doUSBScanAndUpdateUI():void");
    }

    private void startExecutorService() throws RobotCoreException {
        USBScanManager uSBScanManager = new USBScanManager(this, this.remoteConfigure);
        this.usbScanManager = uSBScanManager;
        uSBScanManager.startExecutorService();
        this.scanButtonSingleton.reset();
        this.scanButtonSingleton.setService(this.usbScanManager.getExecutorService());
        this.usbScanManager.startDeviceScanIfNecessary();
    }

    private void stopExecutorService() {
        this.usbScanManager.stopExecutorService();
        this.usbScanManager = null;
    }

    private boolean carryOver(SerialNumber serialNumber, RobotConfigMap robotConfigMap) {
        if (robotConfigMap == null || !robotConfigMap.contains(serialNumber)) {
            return false;
        }
        if (!robotConfigMap.get(serialNumber).isSystemSynthetic()) {
            return true;
        }
        RobotLog.m61vv("FtcConfigTag", "not carrying over synthetic controller: serial=%s", serialNumber);
        return false;
    }

    private RobotConfigMap buildRobotConfigMapFromScanned(RobotConfigMap robotConfigMap, ScannedDevices scannedDevices) {
        ControllerConfiguration controllerConfiguration;
        RobotConfigMap robotConfigMap2 = robotConfigMap;
        RobotConfigMap robotConfigMap3 = new RobotConfigMap();
        this.configurationUtility.resetNameUniquifiers();
        for (Map.Entry next : scannedDevices.entrySet()) {
            SerialNumber serialNumber = (SerialNumber) next.getKey();
            DeviceManager.UsbDeviceType usbDeviceType = (DeviceManager.UsbDeviceType) next.getValue();
            if (!carryOver(serialNumber, robotConfigMap2)) {
                controllerConfiguration = this.configurationUtility.buildNewControllerConfiguration(serialNumber, usbDeviceType, this.usbScanManager.getLynxModuleMetaListSupplier(serialNumber));
            } else if (usbDeviceType == DeviceManager.UsbDeviceType.LYNX_USB_DEVICE) {
                RobotLog.m60vv("FtcConfigTag", "Performing Lynx discovery");
                controllerConfiguration = this.configurationUtility.buildNewControllerConfiguration(serialNumber, usbDeviceType, this.usbScanManager.getLynxModuleMetaListSupplier(serialNumber));
                LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration = (LynxUsbDeviceConfiguration) controllerConfiguration;
                for (LynxModuleConfiguration lynxModuleConfiguration : new ArrayList(lynxUsbDeviceConfiguration.getModules())) {
                    for (LynxModuleConfiguration next2 : ((LynxUsbDeviceConfiguration) robotConfigMap2.get(serialNumber)).getModules()) {
                        if (lynxModuleConfiguration.getModuleAddress() == next2.getModuleAddress()) {
                            RobotLog.m61vv("FtcConfigTag", "carrying over %s", next2.getModuleSerialNumber());
                            next2.setIsParent(lynxModuleConfiguration.isParent());
                            lynxUsbDeviceConfiguration.getModules().remove(lynxModuleConfiguration);
                            lynxUsbDeviceConfiguration.getModules().add(next2);
                        }
                    }
                }
            } else {
                RobotLog.m61vv("FtcConfigTag", "carrying over %s", serialNumber);
                controllerConfiguration = robotConfigMap2.get(serialNumber);
            }
            if (controllerConfiguration != null) {
                controllerConfiguration.setKnownToBeAttached(true);
                robotConfigMap3.put(serialNumber, controllerConfiguration);
            }
        }
        return robotConfigMap3;
    }

    private void readFile() {
        try {
            XmlPullParser xml = this.currentCfgFile.getXml();
            if (xml != null) {
                buildControllersFromXMLResults(new ReadXMLFileHandler().parse(xml));
                populateListAndWarnDevices();
                return;
            }
            throw new RobotCoreException("can't access configuration");
        } catch (Exception e) {
            String format = String.format(getString(C0470R.string.errorParsingConfiguration), new Object[]{this.currentCfgFile.getName()});
            RobotLog.m50ee("FtcConfigTag", (Throwable) e, format);
            this.appUtil.showToast(UILocation.ONLY_LOCAL, format);
        }
    }

    /* access modifiers changed from: private */
    public void populateListAndWarnDevices() {
        this.appUtil.runOnUiThread(new Runnable() {
            public void run() {
                FtcConfigurationActivity.this.populateList();
                FtcConfigurationActivity.this.warnIncompleteDevices();
            }
        });
    }

    /* access modifiers changed from: private */
    public void warnIncompleteDevices() {
        String str;
        String str2 = null;
        if (this.scannedDevices.getErrorMessage() != null) {
            str2 = getString(C0470R.string.errorScanningDevicesTitle);
            str = this.scannedDevices.getErrorMessage();
        } else if (!getRobotConfigMap().allControllersAreBound()) {
            str2 = getString(C0470R.string.notAllDevicesFoundTitle);
            str = Misc.formatForUser(C0470R.string.notAllDevicesFoundMessage, getString(C0470R.string.noSerialNumber));
        } else if (getRobotConfigMap().size() == 0) {
            str2 = getString(C0470R.string.noDevicesFoundTitle);
            str = getString(C0470R.string.noDevicesFoundMessage);
            clearDuplicateWarning();
        } else {
            str = null;
        }
        if (str2 == null && str == null) {
            this.utility.hideFeedbackText(this.idFeedbackAnchor);
        } else {
            this.utility.setFeedbackText(str2 == null ? InspectionState.NO_VERSION : str2, str == null ? InspectionState.NO_VERSION : str, this.idFeedbackAnchor, C0470R.layout.feedback, C0470R.C0472id.feedbackText0, C0470R.C0472id.feedbackText1, C0470R.C0472id.feedbackOKButton);
        }
    }

    /* access modifiers changed from: private */
    /* JADX WARNING: Can't wrap try/catch for region: R(9:1|2|3|4|5|6|7|8|9) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001e */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void synchronouslySetFeedbackWhile(final java.lang.String r6, final java.lang.String r7, java.lang.Runnable r8) {
        /*
            r5 = this;
            com.qualcomm.robotcore.hardware.configuration.Utility r0 = r5.utility
            int r1 = r5.idFeedbackAnchor
            int r2 = com.qualcomm.ftccommon.C0470R.layout.feedback
            int r3 = com.qualcomm.ftccommon.C0470R.C0472id.feedbackText0
            int r4 = com.qualcomm.ftccommon.C0470R.C0472id.feedbackText1
            java.lang.CharSequence[] r0 = r0.getFeedbackText(r1, r2, r3, r4)
            org.firstinspires.ftc.robotcore.internal.system.AppUtil r1 = r5.appUtil     // Catch:{ all -> 0x0033 }
            com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$6 r2 = new com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$6     // Catch:{ all -> 0x0033 }
            r2.<init>(r6, r7)     // Catch:{ all -> 0x0033 }
            r1.synchronousRunOnUiThread(r2)     // Catch:{ all -> 0x0033 }
            java.util.concurrent.Semaphore r6 = r5.feedbackPosted     // Catch:{ InterruptedException -> 0x001e }
            r6.acquire()     // Catch:{ InterruptedException -> 0x001e }
            goto L_0x0025
        L_0x001e:
            java.lang.Thread r6 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0033 }
            r6.interrupt()     // Catch:{ all -> 0x0033 }
        L_0x0025:
            r8.run()     // Catch:{ all -> 0x0033 }
            org.firstinspires.ftc.robotcore.internal.system.AppUtil r6 = r5.appUtil
            com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$7 r7 = new com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$7
            r7.<init>(r0)
            r6.runOnUiThread(r7)
            return
        L_0x0033:
            r6 = move-exception
            org.firstinspires.ftc.robotcore.internal.system.AppUtil r7 = r5.appUtil
            com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$7 r8 = new com.qualcomm.ftccommon.configuration.FtcConfigurationActivity$7
            r8.<init>(r0)
            r7.runOnUiThread(r8)
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.configuration.FtcConfigurationActivity.synchronouslySetFeedbackWhile(java.lang.String, java.lang.String, java.lang.Runnable):void");
    }

    /* access modifiers changed from: private */
    public void warnDuplicateNames(String str) {
        this.utility.setFeedbackText("Found " + str, "Please fix and re-save.", C0470R.C0472id.feedbackAnchorDuplicateNames, C0470R.layout.feedback, C0470R.C0472id.feedbackText0, C0470R.C0472id.feedbackText1);
    }

    /* access modifiers changed from: private */
    public void clearDuplicateWarning() {
        LinearLayout linearLayout = (LinearLayout) findViewById(C0470R.C0472id.feedbackAnchorDuplicateNames);
        linearLayout.removeAllViews();
        linearLayout.setVisibility(8);
    }

    /* access modifiers changed from: private */
    public void populateList() {
        ListView listView = (ListView) findViewById(C0470R.C0472id.controllersList);
        try {
            this.scannedDevices = this.usbScanManager.awaitScannedDevices();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        tellControllersAboutAttachment();
        listView.setAdapter(new DeviceInfoAdapter(this, 17367044, new LinkedList(getRobotConfigMap().controllerConfigurations())));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                ControllerConfiguration controllerConfiguration = (ControllerConfiguration) adapterView.getItemAtPosition(i);
                ConfigurationType configurationType = controllerConfiguration.getConfigurationType();
                if (configurationType == BuiltInConfigurationType.LYNX_USB_DEVICE) {
                    FtcConfigurationActivity.this.handleLaunchEdit(EditLynxUsbDeviceActivity.requestCode, EditLynxUsbDeviceActivity.class, FtcConfigurationActivity.this.initParameters(0, LynxModuleConfiguration.class, controllerConfiguration, ((LynxUsbDeviceConfiguration) controllerConfiguration).getDevices()));
                } else if (configurationType == BuiltInConfigurationType.WEBCAM) {
                    FtcConfigurationActivity.this.handleLaunchEdit(EditWebcamActivity.requestCode, EditWebcamActivity.class, FtcConfigurationActivity.this.initParameters(controllerConfiguration));
                }
            }
        });
    }

    /* access modifiers changed from: package-private */
    public <ITEM_T extends DeviceConfiguration> EditParameters initParameters(int i, Class<ITEM_T> cls, ControllerConfiguration controllerConfiguration, List<ITEM_T> list) {
        EditParameters editParameters = new EditParameters((EditActivity) this, (DeviceConfiguration) controllerConfiguration, cls, list);
        editParameters.setInitialPortNumber(i);
        editParameters.setScannedDevices(this.scannedDevices);
        editParameters.setRobotConfigMap(getRobotConfigMap());
        return editParameters;
    }

    /* access modifiers changed from: package-private */
    public <ITEM_T extends DeviceConfiguration> EditParameters initParameters(ControllerConfiguration controllerConfiguration) {
        return initParameters(0, DeviceConfiguration.class, controllerConfiguration, new ArrayList());
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        try {
            logActivityResult(i, i2, intent);
            if (i2 != 0) {
                RequestCode fromValue = RequestCode.fromValue(i);
                EditParameters fromIntent = EditParameters.fromIntent(this, intent);
                RobotLog.m61vv("FtcConfigTag", "onActivityResult(%s)", fromValue.toString());
                synchronized (this.robotConfigMapLock) {
                    deserializeConfigMap(fromIntent);
                }
                this.scannedDevices = this.usbScanManager.awaitScannedDevices();
                this.appUtil.runOnUiThread(new Runnable() {
                    public void run() {
                        FtcConfigurationActivity.this.currentCfgFile.markDirty();
                        FtcConfigurationActivity.this.robotConfigFileManager.updateActiveConfigHeader(FtcConfigurationActivity.this.currentCfgFile);
                        FtcConfigurationActivity.this.populateList();
                    }
                });
            }
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    public void onBackPressed() {
        RobotLog.m60vv("FtcConfigTag", "onBackPressed()");
        doBackOrCancel();
    }

    public void onCancelButtonPressed(View view) {
        RobotLog.m60vv("FtcConfigTag", "onCancelButtonPressed()");
        doBackOrCancel();
    }

    private void doBackOrCancel() {
        if (this.currentCfgFile.isDirty()) {
            C051010 r0 = new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcConfigurationActivity.this.currentCfgFile.markClean();
                    FtcConfigurationActivity.this.robotConfigFileManager.setActiveConfig(FtcConfigurationActivity.this.remoteConfigure, FtcConfigurationActivity.this.currentCfgFile);
                    FtcConfigurationActivity.this.finishCancel();
                }
            };
            AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.saveChangesTitle), getString(C0470R.string.saveChangesMessage));
            buildBuilder.setPositiveButton(C0470R.string.buttonExitWithoutSaving, r0);
            buildBuilder.setNegativeButton(C0470R.string.buttonNameCancel, this.doNothingAndCloseListener);
            buildBuilder.show();
            return;
        }
        finishCancel();
    }

    public void onDoneButtonPressed(View view) {
        RobotLog.m60vv("FtcConfigTag", "onDoneButtonPressed()");
        final String xml = this.robotConfigFileManager.toXml(getRobotConfigMap());
        if (xml != null) {
            String string = getString(C0470R.string.configNamePromptBanter);
            final EditText editText = new EditText(this);
            editText.setText(this.currentCfgFile.isNoConfig() ? InspectionState.NO_VERSION : this.currentCfgFile.getName());
            AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.configNamePromptTitle), string);
            buildBuilder.setView(editText);
            buildBuilder.setPositiveButton(getString(C0470R.string.buttonNameOK), new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    String obj = editText.getText().toString();
                    RobotConfigFileManager.ConfigNameCheckResult isPlausibleConfigName = FtcConfigurationActivity.this.robotConfigFileManager.isPlausibleConfigName(FtcConfigurationActivity.this.currentCfgFile, obj, FtcConfigurationActivity.this.extantRobotConfigurations);
                    if (!isPlausibleConfigName.success) {
                        String format = String.format(isPlausibleConfigName.errorFormat, new Object[]{obj});
                        FtcConfigurationActivity.this.appUtil.showToast(UILocation.ONLY_LOCAL, String.format("%s %s", new Object[]{format, FtcConfigurationActivity.this.getString(C0470R.string.configurationNotSaved)}));
                        return;
                    }
                    try {
                        if (!FtcConfigurationActivity.this.currentCfgFile.getName().equals(obj)) {
                            FtcConfigurationActivity ftcConfigurationActivity = FtcConfigurationActivity.this;
                            ftcConfigurationActivity.currentCfgFile = new RobotConfigFile(ftcConfigurationActivity.robotConfigFileManager, obj);
                        }
                        FtcConfigurationActivity.this.robotConfigFileManager.writeToFile(FtcConfigurationActivity.this.currentCfgFile, FtcConfigurationActivity.this.remoteConfigure, xml);
                        FtcConfigurationActivity.this.robotConfigFileManager.setActiveConfigAndUpdateUI(FtcConfigurationActivity.this.remoteConfigure, FtcConfigurationActivity.this.currentCfgFile);
                        FtcConfigurationActivity.this.clearDuplicateWarning();
                        FtcConfigurationActivity.this.confirmSave();
                        FtcConfigurationActivity.this.pauseAfterSave();
                        FtcConfigurationActivity.this.finishOk();
                    } catch (DuplicateNameException e) {
                        FtcConfigurationActivity.this.warnDuplicateNames(e.getMessage());
                        RobotLog.m48ee("FtcConfigTag", e.getMessage());
                    } catch (RobotCoreException | IOException e2) {
                        FtcConfigurationActivity.this.appUtil.showToast(UILocation.ONLY_LOCAL, e2.getMessage());
                        RobotLog.m48ee("FtcConfigTag", e2.getMessage());
                    }
                }
            });
            buildBuilder.setNegativeButton(getString(C0470R.string.buttonNameCancel), this.doNothingAndCloseListener);
            buildBuilder.show();
        }
    }

    /* access modifiers changed from: private */
    public void confirmSave() {
        Toast makeText = Toast.makeText(this, C0470R.string.toastSaved, 0);
        makeText.setGravity(80, 0, 50);
        makeText.show();
    }

    /* access modifiers changed from: private */
    public void pauseAfterSave() {
        try {
            Thread.sleep(this.msSaveSplashDelay);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    private void buildControllersFromXMLResults(List<ControllerConfiguration> list) {
        synchronized (this.robotConfigMapLock) {
            this.robotConfigMap = new RobotConfigMap((Collection<ControllerConfiguration>) list);
        }
    }

    private void buildRobotConfigMapFromScanned(ScannedDevices scannedDevices) {
        synchronized (this.robotConfigMapLock) {
            this.robotConfigMap = buildRobotConfigMapFromScanned(getRobotConfigMap(), scannedDevices);
        }
    }

    /* access modifiers changed from: protected */
    public RobotConfigMap getRobotConfigMap() {
        RobotConfigMap robotConfigMap;
        synchronized (this.robotConfigMapLock) {
            robotConfigMap = super.getRobotConfigMap();
        }
        return robotConfigMap;
    }

    /* access modifiers changed from: protected */
    public void tellControllersAboutAttachment() {
        for (ControllerConfiguration next : getRobotConfigMap().controllerConfigurations()) {
            next.setKnownToBeAttached(this.scannedDevices.containsKey(next.getSerialNumber()));
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandScanResp(String str) throws RobotCoreException {
        USBScanManager uSBScanManager = this.usbScanManager;
        if (uSBScanManager != null) {
            uSBScanManager.handleCommandScanResponse(str);
            populateListAndWarnDevices();
        }
        return CallbackResult.HANDLED_CONTINUE;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDiscoverLynxModulesResp(String str) throws RobotCoreException {
        USBScanManager uSBScanManager = this.usbScanManager;
        if (uSBScanManager != null) {
            uSBScanManager.handleCommandDiscoverLynxModulesResponse(str);
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRequestParticularConfigurationResp(String str) throws RobotCoreException {
        buildControllersFromXMLResults(new ReadXMLFileHandler().parse((Reader) new StringReader(str)));
        populateListAndWarnDevices();
        return CallbackResult.HANDLED;
    }

    private class CommandCallback extends RecvLoopRunnable.DegenerateCallback {
        private CommandCallback() {
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            CallbackResult handleCommandRequestParticularConfigurationResp;
            CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
            try {
                String name = command.getName();
                String extra = command.getExtra();
                if (name.equals(CommandList.CMD_SCAN_RESP)) {
                    handleCommandRequestParticularConfigurationResp = FtcConfigurationActivity.this.handleCommandScanResp(extra);
                } else if (name.equals(CommandList.CMD_DISCOVER_LYNX_MODULES_RESP)) {
                    handleCommandRequestParticularConfigurationResp = FtcConfigurationActivity.this.handleCommandDiscoverLynxModulesResp(extra);
                } else if (!name.equals(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP)) {
                    return callbackResult;
                } else {
                    handleCommandRequestParticularConfigurationResp = FtcConfigurationActivity.this.handleCommandRequestParticularConfigurationResp(extra);
                }
                return handleCommandRequestParticularConfigurationResp;
            } catch (RobotCoreException e) {
                RobotLog.logStacktrace(e);
                return callbackResult;
            }
        }
    }
}
