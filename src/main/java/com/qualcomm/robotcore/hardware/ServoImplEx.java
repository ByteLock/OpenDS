package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;

public class ServoImplEx extends ServoImpl implements PwmControl {
    protected ServoControllerEx controllerEx;

    public ServoImplEx(ServoControllerEx servoControllerEx, int i, ServoConfigurationType servoConfigurationType) {
        this(servoControllerEx, i, Servo.Direction.FORWARD, servoConfigurationType);
    }

    public ServoImplEx(ServoControllerEx servoControllerEx, int i, Servo.Direction direction, ServoConfigurationType servoConfigurationType) {
        super(servoControllerEx, i, direction);
        this.controllerEx = servoControllerEx;
        servoControllerEx.setServoType(i, servoConfigurationType);
    }

    public void setPwmRange(PwmControl.PwmRange pwmRange) {
        this.controllerEx.setServoPwmRange(getPortNumber(), pwmRange);
    }

    public PwmControl.PwmRange getPwmRange() {
        return this.controllerEx.getServoPwmRange(getPortNumber());
    }

    public void setPwmEnable() {
        this.controllerEx.setServoPwmEnable(getPortNumber());
    }

    public void setPwmDisable() {
        this.controllerEx.setServoPwmDisable(getPortNumber());
    }

    public boolean isPwmEnabled() {
        return this.controllerEx.isServoPwmEnabled(getPortNumber());
    }
}
