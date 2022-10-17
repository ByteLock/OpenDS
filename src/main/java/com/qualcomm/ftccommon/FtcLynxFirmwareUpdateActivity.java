package com.qualcomm.ftccommon;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.USBAccessibleLynxModule;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.collections.MutableReference;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderManager;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Deadline;
import org.firstinspires.inspection.C1275R;
import org.firstinspires.inspection.InspectionState;
import p007fi.iki.elonen.NanoHTTPD;

public class FtcLynxFirmwareUpdateActivity extends ThemedActivity {
    public static final String TAG = "FtcLynxFirmwareUpdateActivity";
    protected BlockingQueue<RobotCoreCommandList.LynxFirmwareUpdateResp> availableFWUpdateResps;
    protected BlockingQueue<RobotCoreCommandList.LynxFirmwareImagesResp> availableLynxImages;
    protected BlockingQueue<RobotCoreCommandList.USBAccessibleLynxModulesResp> availableLynxModules;
    protected boolean cancelUpdate;
    protected boolean enableUpdateButton;
    protected RobotCoreCommandList.FWImage firmwareImageFile;
    protected Map<View, RobotCoreCommandList.FWImage> firmwareImagesMap;
    protected List<USBAccessibleLynxModule> modulesToUpdate;
    protected int msResponseWait;
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected final String originatorId;
    protected RecvLoopRunnable.RecvLoopCallback recvLoopCallback;
    protected boolean remoteConfigure;
    protected View.OnClickListener updateFileClickListener;

    protected enum FwResponseStatus {
        Succeeded,
        TimedOut,
        Cancelled
    }

    public String getTag() {
        return TAG;
    }

    public FtcLynxFirmwareUpdateActivity() {
        String uuid = UUID.randomUUID().toString();
        this.originatorId = uuid;
        this.recvLoopCallback = new ReceiveLoopCallback(uuid);
        this.remoteConfigure = AppUtil.getInstance().isDriverStation();
        this.msResponseWait = NanoHTTPD.SOCKET_READ_TIMEOUT;
        this.firmwareImagesMap = new HashMap();
        this.firmwareImageFile = new RobotCoreCommandList.FWImage(new File(InspectionState.NO_VERSION), false);
        this.modulesToUpdate = new ArrayList();
        this.enableUpdateButton = true;
        this.cancelUpdate = false;
        this.availableLynxImages = new ArrayBlockingQueue(1);
        this.availableLynxModules = new ArrayBlockingQueue(1);
        this.availableFWUpdateResps = new ArrayBlockingQueue(1);
        this.updateFileClickListener = new View.OnClickListener() {
            public void onClick(View view) {
                FtcLynxFirmwareUpdateActivity ftcLynxFirmwareUpdateActivity = FtcLynxFirmwareUpdateActivity.this;
                ftcLynxFirmwareUpdateActivity.firmwareImageFile = ftcLynxFirmwareUpdateActivity.firmwareImagesMap.get(view);
            }
        };
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    public static void initializeDirectories() {
        AppUtil.getInstance().ensureDirectoryExists(AppUtil.LYNX_FIRMWARE_UPDATE_DIR);
        ReadWriteFile.writeFile(AppUtil.LYNX_FIRMWARE_UPDATE_DIR, "readme.txt", AppUtil.getDefContext().getString(C0470R.string.lynxFirmwareUpdateReadme));
        ReadWriteFile.writeFile(AppUtil.RC_APP_UPDATE_DIR, "readme.txt", AppUtil.getDefContext().getString(C0470R.string.robotControllerAppUpdateReadme));
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_ftc_lynx_fw_update);
        this.networkConnectionHandler.pushReceiveLoopCallback(this.recvLoopCallback);
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        String str;
        String str2;
        String str3;
        super.onStart();
        TextView textView = (TextView) findViewById(C0470R.C0472id.lynxFirmwareFilesHeader);
        RadioGroup radioGroup = (RadioGroup) findViewById(C0470R.C0472id.lynxFirmwareAvailableFilesGroup);
        TextView textView2 = (TextView) findViewById(C0470R.C0472id.lynxFirmwareHubsHeader);
        LinearLayout linearLayout = (LinearLayout) findViewById(C0470R.C0472id.lynxFirmwareModuleList);
        TextView textView3 = (TextView) findViewById(C0470R.C0472id.lynxFirmwareInstructionsPost);
        Button button = (Button) findViewById(C0470R.C0472id.lynxFirmwareUpdateButton);
        RobotCoreCommandList.LynxFirmwareImagesResp candidateLynxFirmwareImages = getCandidateLynxFirmwareImages();
        if (candidateLynxFirmwareImages.firmwareImages.isEmpty()) {
            textView2.setText(getString(C0470R.string.lynx_fw_instructions_no_binary, new Object[]{AppUtil.getInstance().getRelativePath(candidateLynxFirmwareImages.firstFolder.getParentFile(), AppUtil.LYNX_FIRMWARE_UPDATE_DIR)}));
            textView.setVisibility(8);
            radioGroup.setVisibility(8);
            linearLayout.setVisibility(8);
            textView3.setVisibility(8);
            button.setEnabled(false);
            return;
        }
        Collections.sort(candidateLynxFirmwareImages.firmwareImages, new Comparator<RobotCoreCommandList.FWImage>() {
            public int compare(RobotCoreCommandList.FWImage fWImage, RobotCoreCommandList.FWImage fWImage2) {
                return -fWImage.getName().compareTo(fWImage2.getName());
            }
        });
        radioGroup.removeAllViews();
        this.firmwareImagesMap.clear();
        Iterator<RobotCoreCommandList.FWImage> it = candidateLynxFirmwareImages.firmwareImages.iterator();
        boolean z = true;
        while (it.hasNext()) {
            RobotCoreCommandList.FWImage next = it.next();
            RadioButton radioButton = new RadioButton(this);
            String name = next.getName();
            if (next.isAsset) {
                name = name + " (bundled)";
            }
            radioButton.setText(name);
            radioButton.setOnClickListener(this.updateFileClickListener);
            radioGroup.addView(radioButton);
            this.firmwareImagesMap.put(radioButton, next);
            if (z) {
                this.firmwareImageFile = next;
                radioButton.toggle();
                z = false;
            }
        }
        List<USBAccessibleLynxModule> lynxModulesForFirmwareUpdate = getLynxModulesForFirmwareUpdate();
        this.modulesToUpdate = lynxModulesForFirmwareUpdate;
        if (lynxModulesForFirmwareUpdate.isEmpty()) {
            textView2.setText(C0470R.string.lynx_fw_instructions_no_devices);
            linearLayout.setVisibility(8);
            textView3.setVisibility(8);
            button.setEnabled(false);
            return;
        }
        textView2.setText(C0470R.string.lynx_fw_instructions_update);
        for (USBAccessibleLynxModule next2 : this.modulesToUpdate) {
            if (next2.getSerialNumber().isEmbedded()) {
                str = AppUtil.getDefContext().getString(C0470R.string.lynx_fw_instructions_controlhub_item_title);
            } else {
                str = AppUtil.getDefContext().getString(C0470R.string.lynx_fw_instructions_exhub_item_title);
            }
            String string = getString(C0470R.string.lynx_fw_instructions_serial, new Object[]{next2.getSerialNumber()});
            if (next2.getModuleAddress() == 0) {
                str2 = getString(C0470R.string.lynx_fw_instructions_module_address_unavailable);
            } else {
                str2 = getString(C0470R.string.lynx_fw_instructions_module_address, new Object[]{Integer.valueOf(next2.getModuleAddress())});
            }
            String string2 = getString(C0470R.string.lynx_fw_instructions_firmware_version, new Object[]{next2.getFinishedFirmwareVersionString()});
            if (next2.getSerialNumber().isEmbedded()) {
                str3 = string + "\n" + string2;
            } else {
                str3 = string + "\n" + str2 + "\n" + string2;
            }
            View inflate = LayoutInflater.from(this).inflate(17367044, (ViewGroup) null);
            TextView textView4 = (TextView) inflate.findViewById(16908308);
            textView4.setText(str);
            textView4.setTextSize(18.0f);
            ((TextView) inflate.findViewById(16908309)).setText(str3);
            linearLayout.addView(inflate);
        }
        button.setEnabled(this.enableUpdateButton);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        this.networkConnectionHandler.removeReceiveLoopCallback(this.recvLoopCallback);
    }

    public void onUpdateLynxFirmwareClicked(View view) {
        this.enableUpdateButton = false;
        view.setEnabled(false);
        ThreadPool.getDefault().execute(new Runnable() {
            public void run() {
                String str;
                String str2;
                Iterator<USBAccessibleLynxModule> it = FtcLynxFirmwareUpdateActivity.this.modulesToUpdate.iterator();
                while (true) {
                    if (!it.hasNext()) {
                        break;
                    }
                    USBAccessibleLynxModule next = it.next();
                    if (FtcLynxFirmwareUpdateActivity.this.cancelUpdate) {
                        break;
                    }
                    FtcLynxFirmwareUpdateActivity.this.availableFWUpdateResps.clear();
                    RobotLog.m61vv(FtcLynxFirmwareUpdateActivity.TAG, "updating %s with %s", next.getSerialNumber(), FtcLynxFirmwareUpdateActivity.this.firmwareImageFile.getName());
                    RobotCoreCommandList.LynxFirmwareUpdate lynxFirmwareUpdate = new RobotCoreCommandList.LynxFirmwareUpdate();
                    lynxFirmwareUpdate.serialNumber = next.getSerialNumber();
                    lynxFirmwareUpdate.firmwareImageFile = FtcLynxFirmwareUpdateActivity.this.firmwareImageFile;
                    lynxFirmwareUpdate.originatorId = FtcLynxFirmwareUpdateActivity.this.originatorId;
                    FtcLynxFirmwareUpdateActivity.this.sendOrInject(new Command(RobotCoreCommandList.CMD_LYNX_FIRMWARE_UPDATE, SimpleGson.getInstance().toJson((Object) lynxFirmwareUpdate)));
                    MutableReference mutableReference = new MutableReference(FwResponseStatus.Succeeded);
                    FtcLynxFirmwareUpdateActivity ftcLynxFirmwareUpdateActivity = FtcLynxFirmwareUpdateActivity.this;
                    RobotCoreCommandList.LynxFirmwareUpdateResp lynxFirmwareUpdateResp = (RobotCoreCommandList.LynxFirmwareUpdateResp) ftcLynxFirmwareUpdateActivity.awaitResponse(ftcLynxFirmwareUpdateActivity.availableFWUpdateResps, null, (long) FlashLoaderManager.secondsFirmwareUpdateTimeout, TimeUnit.SECONDS, mutableReference);
                    if (next.getSerialNumber().isEmbedded()) {
                        str = AppUtil.getDefContext().getString(C0470R.string.controlHubDisplayName);
                    } else {
                        str = AppUtil.getDefContext().getString(C0470R.string.expansionHubDisplayName) + " " + next.getSerialNumber();
                    }
                    if (lynxFirmwareUpdateResp != null && lynxFirmwareUpdateResp.success) {
                        String string = FtcLynxFirmwareUpdateActivity.this.getString(C0470R.string.toastLynxFirmwareUpdateSuccessful, new Object[]{str});
                        RobotLog.m61vv(FtcLynxFirmwareUpdateActivity.TAG, "%s", string);
                        AppUtil.getInstance().showToast(UILocation.BOTH, string);
                    } else if (mutableReference.getValue() != FwResponseStatus.Cancelled) {
                        if (lynxFirmwareUpdateResp == null) {
                            str2 = FtcLynxFirmwareUpdateActivity.this.getString(C0470R.string.alertLynxFirmwareUpdateTimedout, new Object[]{str});
                        } else {
                            String str3 = lynxFirmwareUpdateResp.errorMessage;
                            if (str3 == null || str3.isEmpty()) {
                                str2 = FtcLynxFirmwareUpdateActivity.this.getString(C0470R.string.alertLynxFirmwareUpdateFailed, new Object[]{str});
                            } else {
                                str2 = FtcLynxFirmwareUpdateActivity.this.getString(C0470R.string.alertLynxFirmwareUpdateFailedWithReason, new Object[]{str, str3});
                            }
                        }
                        RobotLog.m49ee(FtcLynxFirmwareUpdateActivity.TAG, "%s", str2);
                        try {
                            AppUtil.getInstance().showAlertDialog(UILocation.BOTH, FtcLynxFirmwareUpdateActivity.this.getString(C0470R.string.alertLynxFirmwareUpdateFailedTitle), str2).dismissed.await();
                        } catch (InterruptedException unused) {
                            Thread.currentThread().interrupt();
                        }
                    }
                }
                FtcLynxFirmwareUpdateActivity.this.finish();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        this.cancelUpdate = true;
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        AppUtil.getInstance().dismissProgress(UILocation.BOTH);
    }

    protected class ReceiveLoopCallback extends RecvLoopRunnable.DegenerateCallback {
        final String originatorId;

        public ReceiveLoopCallback(String str) {
            this.originatorId = str;
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            String name = command.getName();
            name.hashCode();
            char c = 65535;
            switch (name.hashCode()) {
                case -60637871:
                    if (name.equals(RobotCoreCommandList.CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES_RESP)) {
                        c = 0;
                        break;
                    }
                    break;
                case 349178181:
                    if (name.equals(RobotCoreCommandList.CMD_LYNX_FIRMWARE_UPDATE_RESP)) {
                        c = 1;
                        break;
                    }
                    break;
                case 1474679152:
                    if (name.equals(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES_RESP)) {
                        c = 2;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    FtcLynxFirmwareUpdateActivity.this.availableLynxImages.offer(RobotCoreCommandList.LynxFirmwareImagesResp.deserialize(command.getExtra()));
                    return CallbackResult.HANDLED;
                case 1:
                    RobotCoreCommandList.LynxFirmwareUpdateResp deserialize = RobotCoreCommandList.LynxFirmwareUpdateResp.deserialize(command.getExtra());
                    if (deserialize.originatorId == null || deserialize.originatorId.equals(this.originatorId)) {
                        FtcLynxFirmwareUpdateActivity.this.availableFWUpdateResps.offer(deserialize);
                        return CallbackResult.HANDLED;
                    }
                case 2:
                    FtcLynxFirmwareUpdateActivity.this.availableLynxModules.offer(RobotCoreCommandList.USBAccessibleLynxModulesResp.deserialize(command.getExtra()));
                    return CallbackResult.HANDLED_CONTINUE;
            }
            return super.commandEvent(command);
        }
    }

    /* access modifiers changed from: protected */
    public RobotCoreCommandList.LynxFirmwareImagesResp getCandidateLynxFirmwareImages() {
        RobotCoreCommandList.LynxFirmwareImagesResp lynxFirmwareImagesResp = new RobotCoreCommandList.LynxFirmwareImagesResp();
        this.availableLynxImages.clear();
        sendOrInject(new Command(RobotCoreCommandList.CMD_GET_CANDIDATE_LYNX_FIRMWARE_IMAGES));
        RobotCoreCommandList.LynxFirmwareImagesResp lynxFirmwareImagesResp2 = (RobotCoreCommandList.LynxFirmwareImagesResp) awaitResponse(this.availableLynxImages, lynxFirmwareImagesResp);
        RobotLog.m61vv(TAG, "found %d lynx firmware images", Integer.valueOf(lynxFirmwareImagesResp2.firmwareImages.size()));
        return lynxFirmwareImagesResp2;
    }

    /* access modifiers changed from: protected */
    public List<USBAccessibleLynxModule> getLynxModulesForFirmwareUpdate() {
        RobotCoreCommandList.USBAccessibleLynxModulesRequest uSBAccessibleLynxModulesRequest = new RobotCoreCommandList.USBAccessibleLynxModulesRequest();
        RobotCoreCommandList.USBAccessibleLynxModulesResp uSBAccessibleLynxModulesResp = new RobotCoreCommandList.USBAccessibleLynxModulesResp();
        this.availableLynxModules.clear();
        uSBAccessibleLynxModulesRequest.forFirmwareUpdate = true;
        sendOrInject(new Command(RobotCoreCommandList.CMD_GET_USB_ACCESSIBLE_LYNX_MODULES, uSBAccessibleLynxModulesRequest.serialize()));
        RobotCoreCommandList.USBAccessibleLynxModulesResp uSBAccessibleLynxModulesResp2 = (RobotCoreCommandList.USBAccessibleLynxModulesResp) awaitResponse(this.availableLynxModules, uSBAccessibleLynxModulesResp);
        RobotLog.m61vv(TAG, "found %d lynx modules", Integer.valueOf(uSBAccessibleLynxModulesResp2.modules.size()));
        return uSBAccessibleLynxModulesResp2.modules;
    }

    /* access modifiers changed from: protected */
    public void sendOrInject(Command command) {
        if (this.remoteConfigure) {
            NetworkConnectionHandler.getInstance().sendCommand(command);
        } else {
            NetworkConnectionHandler.getInstance().injectReceivedCommand(command);
        }
    }

    /* access modifiers changed from: protected */
    public <T> T awaitResponse(BlockingQueue<T> blockingQueue, T t) {
        return awaitResponse(blockingQueue, t, (long) this.msResponseWait, TimeUnit.MILLISECONDS, new MutableReference(FwResponseStatus.Succeeded));
    }

    /* access modifiers changed from: protected */
    public <T> T awaitResponse(BlockingQueue<T> blockingQueue, T t, long j, TimeUnit timeUnit, MutableReference<FwResponseStatus> mutableReference) {
        try {
            Deadline deadline = new Deadline(j, timeUnit);
            mutableReference.setValue(FwResponseStatus.TimedOut);
            while (true) {
                if (!deadline.hasExpired()) {
                    T poll = blockingQueue.poll(100, TimeUnit.MILLISECONDS);
                    if (poll == null) {
                        if (this.cancelUpdate) {
                            mutableReference.setValue(FwResponseStatus.Cancelled);
                            break;
                        }
                    } else {
                        mutableReference.setValue(FwResponseStatus.Succeeded);
                        return poll;
                    }
                } else {
                    break;
                }
            }
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        return t;
    }
}
