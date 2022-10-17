package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.util.Device;

public class DeviceNameManagerFactory {

    protected static class InstanceHolder {
        public static final DeviceNameManager theInstance;

        protected InstanceHolder() {
        }

        static {
            DeviceNameManager deviceNameManager;
            if (Device.isRevControlHub()) {
                deviceNameManager = new ControlHubDeviceNameManager();
            } else {
                deviceNameManager = new WifiDirectDeviceNameManager();
            }
            theInstance = deviceNameManager;
        }
    }

    public static DeviceNameManager getInstance() {
        return InstanceHolder.theInstance;
    }
}
