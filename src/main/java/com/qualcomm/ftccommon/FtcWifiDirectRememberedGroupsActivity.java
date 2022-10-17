package com.qualcomm.ftccommon;

import android.content.Context;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.network.WifiDirectGroupName;
import org.firstinspires.ftc.robotcore.network.WifiDirectPersistentGroupManager;
import org.firstinspires.ftc.robotcore.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.inspection.C1275R;

public class FtcWifiDirectRememberedGroupsActivity extends ThemedActivity {
    public static final String TAG = "FtcWifiDirectRememberedGroupsActivity";
    private final NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    private WifiDirectPersistentGroupManager persistentGroupManager;
    private final RecvLoopCallback recvLoopCallback = new RecvLoopCallback();
    private final boolean remoteConfigure = AppUtil.getInstance().isDriverStation();
    /* access modifiers changed from: private */
    public Future requestGroupsFuture = null;
    /* access modifiers changed from: private */
    public final Object requestGroupsFutureLock = new Object();

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_ftc_wifi_remembered_groups);
        if (!this.remoteConfigure) {
            this.persistentGroupManager = new WifiDirectPersistentGroupManager(WifiDirectAgent.getInstance());
        } else {
            this.networkConnectionHandler.pushReceiveLoopCallback(this.recvLoopCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        if (!this.remoteConfigure) {
            loadLocalGroups();
        } else {
            requestRememberedGroups();
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        if (this.remoteConfigure) {
            this.networkConnectionHandler.removeReceiveLoopCallback(this.recvLoopCallback);
        }
    }

    /* access modifiers changed from: protected */
    public void loadLocalGroups() {
        loadGroupList(getLocalGroupList());
    }

    /* access modifiers changed from: protected */
    public void requestRememberedGroups() {
        RobotLog.m60vv(TAG, "requestRememberedGroups()");
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_REQUEST_REMEMBERED_GROUPS));
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRequestRememberedGroupsResp(String str) throws RobotCoreException {
        RobotLog.m60vv(TAG, "handleCommandRequestRememberedGroupsResp()");
        loadGroupList(WifiDirectGroupName.deserializeNames(str));
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleRememberedGroupsChanged() {
        synchronized (this.requestGroupsFutureLock) {
            Future future = this.requestGroupsFuture;
            if (future != null) {
                future.cancel(false);
                this.requestGroupsFuture = null;
            }
            this.requestGroupsFuture = ThreadPool.getDefaultScheduler().schedule(new Callable() {
                public Object call() throws Exception {
                    synchronized (FtcWifiDirectRememberedGroupsActivity.this.requestGroupsFutureLock) {
                        FtcWifiDirectRememberedGroupsActivity.this.requestRememberedGroups();
                        Future unused = FtcWifiDirectRememberedGroupsActivity.this.requestGroupsFuture = null;
                    }
                    return null;
                }
            }, 250, TimeUnit.MILLISECONDS);
        }
        return CallbackResult.HANDLED_CONTINUE;
    }

    /* access modifiers changed from: protected */
    public List<WifiDirectGroupName> getLocalGroupList() {
        return WifiDirectGroupName.namesFromGroups(this.persistentGroupManager.getPersistentGroups());
    }

    /* access modifiers changed from: protected */
    public void loadGroupList(final List<WifiDirectGroupName> list) {
        AppUtil.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                ListView listView = (ListView) FtcWifiDirectRememberedGroupsActivity.this.findViewById(C0470R.C0472id.groupList);
                Collections.sort(list);
                if (list.isEmpty()) {
                    list.add(new WifiDirectGroupName(FtcWifiDirectRememberedGroupsActivity.this.getString(C0470R.string.noRememberedGroupsFound)));
                }
                listView.setAdapter(new WifiP2pGroupItemAdapter(AppUtil.getInstance().getActivity(), 17367049, list));
            }
        });
    }

    protected class WifiP2pGroupItemAdapter extends ArrayAdapter<WifiDirectGroupName> {
        public WifiP2pGroupItemAdapter(Context context, int i, List<WifiDirectGroupName> list) {
            super(context, i, list);
        }
    }

    public void onClearRememberedGroupsClicked(View view) {
        RobotLog.m60vv(TAG, "onClearRememberedGroupsClicked()");
        if (!this.remoteConfigure) {
            this.persistentGroupManager.deleteAllPersistentGroups();
            AppUtil.getInstance().showToast(UILocation.BOTH, getString(C0470R.string.toastWifiP2pRememberedGroupsCleared));
            loadLocalGroups();
            return;
        }
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_CLEAR_REMEMBERED_GROUPS));
    }

    protected class RecvLoopCallback extends RecvLoopRunnable.DegenerateCallback {
        protected RecvLoopCallback() {
        }

        public CallbackResult commandEvent(Command command) {
            CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
            try {
                String name = command.getName();
                char c = 65535;
                int hashCode = name.hashCode();
                if (hashCode != -1830844033) {
                    if (hashCode == 45075229) {
                        if (name.equals(CommandList.CMD_REQUEST_REMEMBERED_GROUPS_RESP)) {
                            c = 0;
                        }
                    }
                } else if (name.equals(RobotCoreCommandList.CMD_NOTIFY_WIFI_DIRECT_REMEMBERED_GROUPS_CHANGED)) {
                    c = 1;
                }
                if (c == 0) {
                    return FtcWifiDirectRememberedGroupsActivity.this.handleCommandRequestRememberedGroupsResp(command.getExtra());
                }
                if (c != 1) {
                    return callbackResult;
                }
                FtcWifiDirectRememberedGroupsActivity.this.handleRememberedGroupsChanged();
                return callbackResult;
            } catch (RobotCoreException e) {
                RobotLog.logStacktrace(e);
                return callbackResult;
            }
        }
    }
}
