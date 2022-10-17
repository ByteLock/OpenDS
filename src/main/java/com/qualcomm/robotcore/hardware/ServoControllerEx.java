package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;

public interface ServoControllerEx extends ServoController {
    PwmControl.PwmRange getServoPwmRange(int i);

    boolean isServoPwmEnabled(int i);

    void setServoPwmDisable(int i);

    void setServoPwmEnable(int i);

    void setServoPwmRange(int i, PwmControl.PwmRange pwmRange);

    void setServoType(int i, ServoConfigurationType servoConfigurationType);
}
