package org.firstinspires.ftc.robotcore.internal.network;

public interface DeviceNameManager {
    String getDeviceName();

    void initializeDeviceNameIfNecessary();

    void registerCallback(DeviceNameListener deviceNameListener);

    String resetDeviceName(boolean z);

    void setDeviceName(String str, boolean z) throws InvalidNetworkSettingException;

    boolean start(StartResult startResult);

    void stop(StartResult startResult);

    void unregisterCallback(DeviceNameListener deviceNameListener);
}
