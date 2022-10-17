package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.Context;
import android.net.wifi.p2p.WifiP2pManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class WifiDirectChannelChanger {
    public static final String TAG = "WifiDirectChannelChanger";
    /* access modifiers changed from: private */
    public int channel = 0;
    /* access modifiers changed from: private */
    public Context context;
    private volatile boolean isChangingChannels = false;
    /* access modifiers changed from: private */
    public int listenChannel = 0;
    /* access modifiers changed from: private */
    public PreferencesHelper preferencesHelper;
    /* access modifiers changed from: private */
    public WifiDirectAgent wifiDirectAgent = WifiDirectAgent.getInstance();

    public WifiDirectChannelChanger() {
        Application defContext = AppUtil.getDefContext();
        this.context = defContext;
        this.preferencesHelper = new PreferencesHelper(TAG, (Context) defContext);
    }

    private void issueSuccessToast() {
        AppUtil.getInstance().showToast(UILocation.BOTH, this.context.getString(C0705R.string.setWifiChannelSuccess, new Object[]{WifiDirectChannelAndDescription.getDescription(this.channel)}), 1);
    }

    private void issueFailureToast() {
        AppUtil.getInstance().showToast(UILocation.BOTH, this.context.getString(C0705R.string.setWifiChannelFailure, new Object[]{WifiDirectChannelAndDescription.getDescription(this.channel)}), 1);
    }

    private void startChannelChange(int i) {
        RobotLog.m61vv(TAG, "startChannelChange() channel=%d", Integer.valueOf(i));
        this.isChangingChannels = true;
        this.channel = i;
    }

    /* access modifiers changed from: private */
    public void finishChannelChange(boolean z) {
        RobotLog.m61vv(TAG, "finishChannelChange() channel=%d success=%s", Integer.valueOf(this.channel), Boolean.valueOf(z));
        if (z) {
            issueSuccessToast();
        } else {
            issueFailureToast();
        }
        this.isChangingChannels = false;
    }

    public boolean isBusy() {
        return this.isChangingChannels;
    }

    public void changeToChannel(int i) {
        RobotLog.m61vv(TAG, "changeToChannel() channel=%d", Integer.valueOf(i));
        startChannelChange(i);
        AppUtil.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                if (WifiDirectChannelChanger.this.channel > 11) {
                    int unused = WifiDirectChannelChanger.this.listenChannel = 0;
                } else {
                    WifiDirectChannelChanger wifiDirectChannelChanger = WifiDirectChannelChanger.this;
                    int unused2 = wifiDirectChannelChanger.listenChannel = wifiDirectChannelChanger.channel;
                }
                WifiDirectChannelChanger.this.wifiDirectAgent.setWifiP2pChannels(WifiDirectChannelChanger.this.listenChannel, WifiDirectChannelChanger.this.channel, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        RobotLog.m60vv(WifiDirectChannelChanger.TAG, "callSetWifiP2pChannels() success");
                        WifiDirectChannelChanger.this.preferencesHelper.writePrefIfDifferent(WifiDirectChannelChanger.this.context.getString(C0705R.string.pref_wifip2p_channel), Integer.valueOf(WifiDirectChannelChanger.this.channel));
                        RobotLog.m61vv(WifiDirectChannelChanger.TAG, "Channel %d saved as preference \"pref_wifip2p_channel\".", Integer.valueOf(WifiDirectChannelChanger.this.channel));
                        WifiDirectChannelChanger.this.finishChannelChange(true);
                    }

                    public void onFailure(int i) {
                        if (i == 0) {
                            RobotLog.m60vv(WifiDirectChannelChanger.TAG, "callSetWifiP2pChannels() failure (ERROR)");
                        } else if (i == 1) {
                            RobotLog.m60vv(WifiDirectChannelChanger.TAG, "callSetWifiP2pChannels() failure (P2P_UNSUPPORTED)");
                        } else if (i != 2) {
                            RobotLog.m60vv(WifiDirectChannelChanger.TAG, "callSetWifiP2pChannels() failure (unknown reason)");
                        } else {
                            RobotLog.m60vv(WifiDirectChannelChanger.TAG, "callSetWifiP2pChannels() failure (BUSY)");
                        }
                        WifiDirectChannelChanger.this.finishChannelChange(false);
                    }
                });
            }
        });
    }
}
