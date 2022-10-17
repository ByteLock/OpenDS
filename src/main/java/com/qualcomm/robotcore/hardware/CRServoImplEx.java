package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;

public class CRServoImplEx extends CRServoImpl implements PwmControl {
    protected ServoControllerEx controllerEx;

    public CRServoImplEx(ServoControllerEx servoControllerEx, int i, ServoConfigurationType servoConfigurationType) {
        this(servoControllerEx, i, DcMotorSimple.Direction.FORWARD, servoConfigurationType);
    }

    public CRServoImplEx(ServoControllerEx servoControllerEx, int i, DcMotorSimple.Direction direction, ServoConfigurationType servoConfigurationType) {
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
