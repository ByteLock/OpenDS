package com.qualcomm.robotcore.hardware;

public interface HardwareDevice {

    public enum Manufacturer {
        Unknown,
        Other,
        Lego,
        HiTechnic,
        ModernRobotics,
        Adafruit,
        Matrix,
        Lynx,
        AMS,
        STMicroelectronics,
        Broadcom
    }

    void close();

    String getConnectionInfo();

    String getDeviceName();

    Manufacturer getManufacturer();

    int getVersion();

    void resetDeviceConfigurationForOpMode();
}
