package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.FileNotFoundException;
import java.io.StringReader;
import java.text.Collator;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Deque;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.inspection.C1275R;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class ConfigureFromTemplateActivity extends EditActivity {
    public static final String TAG = "ConfigFromTemplate";
    public static final RequestCode requestCode = RequestCode.CONFIG_FROM_TEMPLATE;
    protected final RecvLoopRunnable.RecvLoopCallback commandCallback = new CommandCallback();
    protected List<RobotConfigFile> configurationList = new CopyOnWriteArrayList();
    protected ViewGroup feedbackAnchor;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected final Deque<StringProcessor> receivedConfigProcessors = new LinkedList();
    protected Map<String, String> remoteTemplates = new ConcurrentHashMap();
    protected List<RobotConfigFile> templateList = new CopyOnWriteArrayList();
    protected USBScanManager usbScanManager;

    protected interface StringProcessor {
        void processString(String str);
    }

    protected interface TemplateProcessor {
        void processTemplate(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser);
    }

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_configure_from_template);
        deserialize(EditParameters.fromIntent(this, getIntent()));
        if (this.remoteConfigure) {
            this.networkConnectionHandler.pushReceiveLoopCallback(this.commandCallback);
        }
        USBScanManager uSBScanManager = new USBScanManager(this.context, this.remoteConfigure);
        this.usbScanManager = uSBScanManager;
        uSBScanManager.startExecutorService();
        this.usbScanManager.startDeviceScanIfNecessary();
        this.feedbackAnchor = (ViewGroup) findViewById(C0470R.C0472id.feedbackAnchor);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.robotConfigFileManager.updateActiveConfigHeader(this.currentCfgFile);
        if (!this.remoteConfigure) {
            this.configurationList = this.robotConfigFileManager.getXMLFiles();
            this.templateList = this.robotConfigFileManager.getXMLTemplates();
            warnIfNoTemplates();
        } else {
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATIONS));
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATION_TEMPLATES));
        }
        populate();
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRequestConfigurationsResp(String str) throws RobotCoreException {
        RobotConfigFileManager robotConfigFileManager = this.robotConfigFileManager;
        this.configurationList = RobotConfigFileManager.deserializeXMLConfigList(str);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRequestTemplatesResp(String str) throws RobotCoreException {
        RobotConfigFileManager robotConfigFileManager = this.robotConfigFileManager;
        this.templateList = RobotConfigFileManager.deserializeXMLConfigList(str);
        warnIfNoTemplates();
        populate();
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.usbScanManager.stopExecutorService();
        this.usbScanManager = null;
        if (this.remoteConfigure) {
            this.networkConnectionHandler.removeReceiveLoopCallback(this.commandCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void warnIfNoTemplates() {
        if (this.templateList.size() == 0) {
            this.feedbackAnchor.setVisibility(4);
            final String string = getString(C0470R.string.noTemplatesFoundTitle);
            final String string2 = getString(C0470R.string.noTemplatesFoundMessage);
            runOnUiThread(new Runnable() {
                public void run() {
                    ConfigureFromTemplateActivity.this.utility.setFeedbackText(string, string2, C0470R.C0472id.feedbackAnchor, C0470R.layout.feedback, C0470R.C0472id.feedbackText0, C0470R.C0472id.feedbackText1);
                }
            });
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                ConfigureFromTemplateActivity.this.feedbackAnchor.removeAllViews();
                ConfigureFromTemplateActivity.this.feedbackAnchor.setVisibility(8);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void populate() {
        runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup viewGroup = (ViewGroup) ConfigureFromTemplateActivity.this.findViewById(C0470R.C0472id.templateList);
                viewGroup.removeAllViews();
                final Collator instance = Collator.getInstance();
                instance.setStrength(0);
                Collections.sort(ConfigureFromTemplateActivity.this.templateList, new Comparator<RobotConfigFile>() {
                    public int compare(RobotConfigFile robotConfigFile, RobotConfigFile robotConfigFile2) {
                        return instance.compare(robotConfigFile.getName(), robotConfigFile2.getName());
                    }
                });
                for (RobotConfigFile next : ConfigureFromTemplateActivity.this.templateList) {
                    View inflate = LayoutInflater.from(ConfigureFromTemplateActivity.this.context).inflate(C0470R.layout.template_info, (ViewGroup) null);
                    viewGroup.addView(inflate);
                    TextView textView = (TextView) inflate.findViewById(C0470R.C0472id.templateNameText);
                    textView.setText(next.getName());
                    textView.setTag(next);
                }
            }
        });
    }

    public void onConfigureButtonPressed(View view) {
        getTemplateAndThen(getTemplateMeta(view), new TemplateProcessor() {
            public void processTemplate(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser) {
                ConfigureFromTemplateActivity.this.configureFromTemplate(robotConfigFile, xmlPullParser);
            }
        });
    }

    /* access modifiers changed from: package-private */
    public void configureFromTemplate(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser) {
        try {
            RobotConfigMap instantiateTemplate = instantiateTemplate(robotConfigFile, xmlPullParser);
            awaitScannedDevices();
            EditParameters editParameters = new EditParameters(this);
            editParameters.setRobotConfigMap(instantiateTemplate);
            editParameters.setExtantRobotConfigurations(this.configurationList);
            editParameters.setScannedDevices(this.scannedDevices);
            Intent intent = new Intent(this.context, FtcConfigurationActivity.class);
            editParameters.putIntent(intent);
            this.robotConfigFileManager.setActiveConfig(RobotConfigFile.noConfig(this.robotConfigFileManager));
            startActivityForResult(intent, FtcConfigurationActivity.requestCode.value);
        } catch (RobotCoreException unused) {
        }
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i == FtcConfigurationActivity.requestCode.value) {
            this.currentCfgFile = this.robotConfigFileManager.getActiveConfigAndUpdateUI();
        }
    }

    public void onInfoButtonPressed(View view) {
        getTemplateAndThen(getTemplateMeta(view), new TemplateProcessor() {
            public void processTemplate(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser) {
                ConfigureFromTemplateActivity.this.showInfo(robotConfigFile, xmlPullParser);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showInfo(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser) {
        String indent = indent(3, this.robotConfigFileManager.getRobotConfigDescription(xmlPullParser));
        final String string = getString(C0470R.string.templateConfigureConfigurationInstructionsTitle);
        final String format = String.format(getString(C0470R.string.templateConfigurationInstructions), new Object[]{robotConfigFile.getName(), indent});
        runOnUiThread(new Runnable() {
            public void run() {
                ConfigureFromTemplateActivity.this.utility.setFeedbackText(string, format.trim(), C0470R.C0472id.feedbackAnchor, C0470R.layout.feedback, C0470R.C0472id.feedbackText0, C0470R.C0472id.feedbackText1, C0470R.C0472id.feedbackOKButton);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void getTemplateAndThen(final RobotConfigFile robotConfigFile, final TemplateProcessor templateProcessor) {
        if (this.remoteConfigure) {
            String str = this.remoteTemplates.get(robotConfigFile.getName());
            if (str != null) {
                templateProcessor.processTemplate(robotConfigFile, xmlPullParserFromString(str));
                return;
            }
            synchronized (this.receivedConfigProcessors) {
                this.receivedConfigProcessors.addLast(new StringProcessor() {
                    public void processString(String str) {
                        ConfigureFromTemplateActivity.this.remoteTemplates.put(robotConfigFile.getName(), str);
                        templateProcessor.processTemplate(robotConfigFile, ConfigureFromTemplateActivity.this.xmlPullParserFromString(str));
                    }
                });
                this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION, robotConfigFile.toString()));
            }
            return;
        }
        try {
            templateProcessor.processTemplate(robotConfigFile, robotConfigFile.getXml());
        } catch (FileNotFoundException | XmlPullParserException e) {
            RobotLog.m50ee(TAG, e, "Failed to get template XML parser");
        }
    }

    /* access modifiers changed from: protected */
    public XmlPullParser xmlPullParserFromString(String str) {
        return ReadXMLFileHandler.xmlPullParserFromReader(new StringReader(str));
    }

    /* access modifiers changed from: protected */
    public RobotConfigFile getTemplateMeta(View view) {
        return (RobotConfigFile) ((TextView) ((ViewGroup) view.getParent()).findViewById(C0470R.C0472id.templateNameText)).getTag();
    }

    /* access modifiers changed from: protected */
    public ScannedDevices awaitScannedDevices() {
        try {
            this.scannedDevices = this.usbScanManager.awaitScannedDevices();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        return this.scannedDevices;
    }

    /* access modifiers changed from: package-private */
    public RobotConfigMap instantiateTemplate(RobotConfigFile robotConfigFile, XmlPullParser xmlPullParser) throws RobotCoreException {
        awaitScannedDevices();
        RobotConfigMap robotConfigMap = new RobotConfigMap((Collection<ControllerConfiguration>) new ReadXMLFileHandler().parse(xmlPullParser));
        robotConfigMap.bindUnboundControllers(this.scannedDevices);
        return robotConfigMap;
    }

    private String indent(int i, String str) {
        String str2 = InspectionState.NO_VERSION;
        for (int i2 = 0; i2 < i; i2++) {
            str2 = str2 + " ";
        }
        return str2 + str.replace("\n", "\n" + str2);
    }

    private class CommandCallback extends RecvLoopRunnable.DegenerateCallback {
        private CommandCallback() {
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            CallbackResult handleCommandNotifyActiveConfig;
            CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
            try {
                String name = command.getName();
                String extra = command.getExtra();
                if (name.equals(CommandList.CMD_SCAN_RESP)) {
                    handleCommandNotifyActiveConfig = ConfigureFromTemplateActivity.this.handleCommandScanResp(extra);
                } else if (name.equals(CommandList.CMD_REQUEST_CONFIGURATIONS_RESP)) {
                    handleCommandNotifyActiveConfig = ConfigureFromTemplateActivity.this.handleCommandRequestConfigurationsResp(extra);
                } else if (name.equals(CommandList.CMD_REQUEST_CONFIGURATION_TEMPLATES_RESP)) {
                    handleCommandNotifyActiveConfig = ConfigureFromTemplateActivity.this.handleCommandRequestTemplatesResp(extra);
                } else if (name.equals(RobotCoreCommandList.CMD_REQUEST_PARTICULAR_CONFIGURATION_RESP)) {
                    handleCommandNotifyActiveConfig = ConfigureFromTemplateActivity.this.handleCommandRequestParticularConfigurationResp(extra);
                } else if (!name.equals(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION)) {
                    return callbackResult;
                } else {
                    handleCommandNotifyActiveConfig = ConfigureFromTemplateActivity.this.handleCommandNotifyActiveConfig(extra);
                }
                return handleCommandNotifyActiveConfig;
            } catch (RobotCoreException e) {
                RobotLog.logStackTrace(e);
                return callbackResult;
            }
        }
    }

    /* access modifiers changed from: private */
    public CallbackResult handleCommandScanResp(String str) throws RobotCoreException {
        Assert.assertTrue(this.remoteConfigure);
        this.usbScanManager.handleCommandScanResponse(str);
        return CallbackResult.HANDLED_CONTINUE;
    }

    /* access modifiers changed from: private */
    public CallbackResult handleCommandRequestParticularConfigurationResp(String str) throws RobotCoreException {
        StringProcessor pollFirst;
        synchronized (this.receivedConfigProcessors) {
            pollFirst = this.receivedConfigProcessors.pollFirst();
        }
        if (pollFirst != null) {
            pollFirst.processString(str);
        }
        return CallbackResult.HANDLED;
    }
}
