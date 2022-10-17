package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.util.SerialNumber;

public interface RobotCoreLynxModule extends HardwareDevice {
    String getFirmwareVersionString();

    int getModuleAddress();

    String getNullableFirmwareVersionString();

    SerialNumber getSerialNumber();

    boolean isParent();
}
