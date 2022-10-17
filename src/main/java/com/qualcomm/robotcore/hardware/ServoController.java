package com.qualcomm.robotcore.hardware;

public interface ServoController extends HardwareDevice {

    public enum PwmStatus {
        ENABLED,
        DISABLED,
        MIXED
    }

    PwmStatus getPwmStatus();

    double getServoPosition(int i);

    void pwmDisable();

    void pwmEnable();

    void setServoPosition(int i, double d);
}
