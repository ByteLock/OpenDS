package com.qualcomm.ftccommon.configuration;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.File;
import java.text.Collator;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;

public class FtcLoadFileActivity extends EditActivity {
    public static final String TAG = "FtcConfigTag";
    protected final RecvLoopRunnable.RecvLoopCallback commandCallback = new CommandCallback();
    DialogInterface.OnClickListener doNothingAndCloseListener = new DialogInterface.OnClickListener() {
        public void onClick(DialogInterface dialogInterface, int i) {
        }
    };
    /* access modifiers changed from: private */
    public List<RobotConfigFile> fileList = new CopyOnWriteArrayList();
    private final NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();

    public String getTag() {
        return "FtcConfigTag";
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C0470R.C0472id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RobotLog.m60vv("FtcConfigTag", "FtcLoadFileActivity started");
        setContentView(C0470R.layout.activity_load);
        deserialize(EditParameters.fromIntent(this, getIntent()));
        buildInfoButtons();
        if (this.remoteConfigure) {
            this.networkConnectionHandler.pushReceiveLoopCallback(this.commandCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if (!this.remoteConfigure) {
            this.robotConfigFileManager.createConfigFolder();
        }
        if (!this.remoteConfigure) {
            this.fileList = this.robotConfigFileManager.getXMLFiles();
            warnIfNoFiles();
        } else {
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATIONS));
        }
        populate();
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRequestConfigFilesResp(String str) throws RobotCoreException {
        RobotConfigFileManager robotConfigFileManager = this.robotConfigFileManager;
        this.fileList = RobotConfigFileManager.deserializeXMLConfigList(str);
        warnIfNoFiles();
        populate();
        return CallbackResult.HANDLED;
    }

    public void onResume() {
        super.onResume();
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.remoteConfigure) {
            this.networkConnectionHandler.removeReceiveLoopCallback(this.commandCallback);
        }
    }

    private void buildInfoButtons() {
        ((Button) findViewById(C0470R.C0472id.files_holder).findViewById(C0470R.C0472id.info_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder buildBuilder = FtcLoadFileActivity.this.utility.buildBuilder(FtcLoadFileActivity.this.getString(C0470R.string.availableConfigListCaption), FtcLoadFileActivity.this.getString(C0470R.string.availableConfigsInfoMessage));
                buildBuilder.setPositiveButton(FtcLoadFileActivity.this.getString(C0470R.string.buttonNameOK), FtcLoadFileActivity.this.doNothingAndCloseListener);
                AlertDialog create = buildBuilder.create();
                create.show();
                ((TextView) create.findViewById(16908299)).setTextSize(14.0f);
            }
        });
        ((Button) findViewById(C0470R.C0472id.configureFromTemplateArea).findViewById(C0470R.C0472id.info_btn)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                AlertDialog.Builder buildBuilder = FtcLoadFileActivity.this.utility.buildBuilder(FtcLoadFileActivity.this.getString(C0470R.string.configFromTemplateInfoTitle), FtcLoadFileActivity.this.getString(C0470R.string.configFromTemplateInfoMessage));
                buildBuilder.setPositiveButton(FtcLoadFileActivity.this.getString(C0470R.string.buttonNameOK), FtcLoadFileActivity.this.doNothingAndCloseListener);
                AlertDialog create = buildBuilder.create();
                create.show();
                ((TextView) create.findViewById(16908299)).setTextSize(14.0f);
            }
        });
    }

    private void warnIfNoFiles() {
        if (this.fileList.size() == 0) {
            final String string = getString(C0470R.string.noFilesFoundTitle);
            final String string2 = getString(C0470R.string.noFilesFoundMessage);
            runOnUiThread(new Runnable() {
                public void run() {
                    FtcLoadFileActivity.this.utility.setFeedbackText(string, string2, C0470R.C0472id.empty_filelist, C0470R.layout.feedback, C0470R.C0472id.feedbackText0, C0470R.C0472id.feedbackText1);
                }
            });
            return;
        }
        runOnUiThread(new Runnable() {
            public void run() {
                ViewGroup viewGroup = (ViewGroup) FtcLoadFileActivity.this.findViewById(C0470R.C0472id.empty_filelist);
                viewGroup.removeAllViews();
                viewGroup.setVisibility(8);
            }
        });
    }

    private void populate() {
        runOnUiThread(new Runnable() {
            public void run() {
                View findViewById = FtcLoadFileActivity.this.findViewById(C0470R.C0472id.readOnlyExplanation);
                findViewById.setVisibility(8);
                ViewGroup viewGroup = (ViewGroup) FtcLoadFileActivity.this.findViewById(C0470R.C0472id.inclusionlayout);
                viewGroup.removeAllViews();
                final Collator instance = Collator.getInstance();
                instance.setStrength(0);
                Collections.sort(FtcLoadFileActivity.this.fileList, new Comparator<RobotConfigFile>() {
                    public int compare(RobotConfigFile robotConfigFile, RobotConfigFile robotConfigFile2) {
                        return instance.compare(robotConfigFile.getName(), robotConfigFile2.getName());
                    }
                });
                for (RobotConfigFile robotConfigFile : FtcLoadFileActivity.this.fileList) {
                    View inflate = LayoutInflater.from(FtcLoadFileActivity.this.context).inflate(C0470R.layout.file_info, (ViewGroup) null);
                    viewGroup.addView(inflate);
                    if (robotConfigFile.isReadOnly()) {
                        Button button = (Button) inflate.findViewById(C0470R.C0472id.file_delete_button);
                        button.setEnabled(false);
                        button.setClickable(false);
                        findViewById.setVisibility(0);
                    }
                    TextView textView = (TextView) inflate.findViewById(C0470R.C0472id.filename_editText);
                    textView.setText(robotConfigFile.getName());
                    textView.setTag(robotConfigFile);
                    inflate.findViewById(C0470R.C0472id.configIsReadOnlyFeedback).setVisibility(robotConfigFile.isReadOnly() ? 0 : 8);
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        logActivityResult(i, i2, intent);
        this.currentCfgFile = this.robotConfigFileManager.getActiveConfigAndUpdateUI();
    }

    public void onNewButtonPressed(View view) {
        this.robotConfigFileManager.setActiveConfigAndUpdateUI(this.remoteConfigure, RobotConfigFile.noConfig(this.robotConfigFileManager));
        startActivityForResult(makeEditConfigIntent(FtcNewFileActivity.class, (RobotConfigFile) null), FtcNewFileActivity.requestCode.value);
    }

    public void onFileEditButtonPressed(View view) {
        RobotConfigFile file = getFile(view);
        this.robotConfigFileManager.setActiveConfig(this.remoteConfigure, file);
        startActivityForResult(makeEditConfigIntent(FtcConfigurationActivity.class, file), FtcConfigurationActivity.requestCode.value);
    }

    public void onConfigureFromTemplatePressed(View view) {
        startActivityForResult(makeEditConfigIntent(ConfigureFromTemplateActivity.class, (RobotConfigFile) null), ConfigureFromTemplateActivity.requestCode.value);
    }

    /* access modifiers changed from: package-private */
    public Intent makeEditConfigIntent(Class cls, RobotConfigFile robotConfigFile) {
        EditParameters editParameters = new EditParameters(this);
        editParameters.setExtantRobotConfigurations(this.fileList);
        if (robotConfigFile != null) {
            editParameters.setCurrentCfgFile(robotConfigFile);
        }
        Intent intent = new Intent(this.context, cls);
        editParameters.putIntent(intent);
        return intent;
    }

    public void onFileActivateButtonPressed(View view) {
        RobotConfigFile file = getFile(view);
        this.robotConfigFileManager.setActiveConfigAndUpdateUI(this.remoteConfigure, file);
        if (this.remoteConfigure) {
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_ACTIVATE_CONFIGURATION, file.toString()));
        }
    }

    public void onFileDeleteButtonPressed(View view) {
        final RobotConfigFile file = getFile(view);
        if (file.getLocation() == RobotConfigFile.FileLocation.LOCAL_STORAGE) {
            AlertDialog.Builder buildBuilder = this.utility.buildBuilder(getString(C0470R.string.confirmConfigDeleteTitle), getString(C0470R.string.confirmConfigDeleteMessage));
            buildBuilder.setPositiveButton(C0470R.string.buttonNameOK, new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcLoadFileActivity.this.doDeleteConfiguration(file);
                }
            });
            buildBuilder.setNegativeButton(C0470R.string.buttonNameCancel, this.doNothingAndCloseListener);
            buildBuilder.show();
        }
    }

    /* access modifiers changed from: package-private */
    public void doDeleteConfiguration(RobotConfigFile robotConfigFile) {
        if (this.remoteConfigure) {
            if (robotConfigFile.getLocation() == RobotConfigFile.FileLocation.LOCAL_STORAGE) {
                this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_DELETE_CONFIGURATION, robotConfigFile.toString()));
                this.fileList.remove(robotConfigFile);
                populate();
            }
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_CONFIGURATIONS));
        } else {
            if (robotConfigFile.getLocation() == RobotConfigFile.FileLocation.LOCAL_STORAGE) {
                File fullPath = robotConfigFile.getFullPath();
                if (!fullPath.delete()) {
                    String name = fullPath.getName();
                    this.appUtil.showToast(UILocation.ONLY_LOCAL, String.format(getString(C0470R.string.configToDeleteDoesNotExist), new Object[]{name}));
                    RobotLog.m48ee("FtcConfigTag", "Tried to delete a file that does not exist: " + name);
                }
            }
            this.fileList = this.robotConfigFileManager.getXMLFiles();
            populate();
        }
        this.robotConfigFileManager.setActiveConfigAndUpdateUI(this.remoteConfigure, RobotConfigFile.noConfig(this.robotConfigFileManager));
    }

    private RobotConfigFile getFile(View view) {
        return (RobotConfigFile) ((TextView) ((LinearLayout) ((LinearLayout) view.getParent()).getParent()).findViewById(C0470R.C0472id.filename_editText)).getTag();
    }

    public void onBackPressed() {
        logBackPressed();
        finishOk();
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
                if (name.equals(CommandList.CMD_REQUEST_CONFIGURATIONS_RESP)) {
                    handleCommandNotifyActiveConfig = FtcLoadFileActivity.this.handleCommandRequestConfigFilesResp(extra);
                } else if (!name.equals(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION)) {
                    return callbackResult;
                } else {
                    handleCommandNotifyActiveConfig = FtcLoadFileActivity.this.handleCommandNotifyActiveConfig(extra);
                }
                return handleCommandNotifyActiveConfig;
            } catch (RobotCoreException e) {
                RobotLog.logStacktrace(e);
                return callbackResult;
            }
        }
    }
}
