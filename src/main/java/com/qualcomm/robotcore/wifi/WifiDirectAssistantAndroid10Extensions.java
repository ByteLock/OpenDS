package com.qualcomm.robotcore.wifi;

import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import androidx.core.content.ContextCompat;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class WifiDirectAssistantAndroid10Extensions {

    static abstract class DelegateDeviceInfoListener {
        /* access modifiers changed from: package-private */
        public abstract void onDeviceInfoAvailable(WifiP2pDevice wifiP2pDevice);

        DelegateDeviceInfoListener() {
        }
    }

    public static void handleRegisterBroadcastReceiver(WifiP2pManager wifiP2pManager, WifiP2pManager.Channel channel, final DelegateDeviceInfoListener delegateDeviceInfoListener) {
        if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") != -1) {
            wifiP2pManager.requestDeviceInfo(channel, new WifiP2pManager.DeviceInfoListener() {
                public void onDeviceInfoAvailable(WifiP2pDevice wifiP2pDevice) {
                    DelegateDeviceInfoListener.this.onDeviceInfoAvailable(wifiP2pDevice);
                }
            });
            return;
        }
        throw new RuntimeException("We do NOT have permission to access fine location");
    }
}
