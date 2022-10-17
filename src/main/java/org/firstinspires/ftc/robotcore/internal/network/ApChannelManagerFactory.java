package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.util.Device;

public class ApChannelManagerFactory {
    protected static ApChannelManager apChannelManager;

    public static synchronized ApChannelManager getInstance() {
        ApChannelManager apChannelManager2;
        synchronized (ApChannelManagerFactory.class) {
            if (apChannelManager == null) {
                if (Device.isRevControlHub()) {
                    apChannelManager = new ControlHubApChannelManager();
                } else {
                    apChannelManager = new WifiDirectChannelManager();
                }
            }
            apChannelManager2 = apChannelManager;
        }
        return apChannelManager2;
    }

    private ApChannelManagerFactory() {
    }
}
