package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import com.qualcomm.robotcore.util.RobotLog;

public class WifiAssistant {
    private final Context context;
    private final IntentFilter intentFilter;
    private final WifiStateBroadcastReceiver receiver;

    public interface WifiAssistantCallback {
        void wifiEventCallback(WifiState wifiState);
    }

    public enum WifiState {
        CONNECTED,
        NOT_CONNECTED
    }

    private static class WifiStateBroadcastReceiver extends BroadcastReceiver {
        private final WifiAssistantCallback callback;
        private WifiState state = null;

        public WifiStateBroadcastReceiver(WifiAssistantCallback wifiAssistantCallback) {
            this.callback = wifiAssistantCallback;
        }

        public void onReceive(Context context, Intent intent) {
            if (!intent.getAction().equals("android.net.wifi.STATE_CHANGE")) {
                return;
            }
            if (((NetworkInfo) intent.getParcelableExtra("networkInfo")).isConnected()) {
                notify(WifiState.CONNECTED);
            } else {
                notify(WifiState.NOT_CONNECTED);
            }
        }

        private void notify(WifiState wifiState) {
            if (this.state != wifiState) {
                this.state = wifiState;
                WifiAssistantCallback wifiAssistantCallback = this.callback;
                if (wifiAssistantCallback != null) {
                    wifiAssistantCallback.wifiEventCallback(wifiState);
                }
            }
        }
    }

    public WifiAssistant(Context context2, WifiAssistantCallback wifiAssistantCallback) {
        this.context = context2;
        if (wifiAssistantCallback == null) {
            RobotLog.m58v("WifiAssistantCallback is null");
        }
        this.receiver = new WifiStateBroadcastReceiver(wifiAssistantCallback);
        IntentFilter intentFilter2 = new IntentFilter();
        this.intentFilter = intentFilter2;
        intentFilter2.addAction("android.net.wifi.STATE_CHANGE");
    }

    public void enable() {
        this.context.registerReceiver(this.receiver, this.intentFilter);
    }

    public void disable() {
        this.context.unregisterReceiver(this.receiver);
    }
}
