package com.qualcomm.robotcore.hardware;

public interface RobotConfigNameable extends HardwareDevice {
    String getUserConfiguredName();

    void setUserConfiguredName(String str);
}
