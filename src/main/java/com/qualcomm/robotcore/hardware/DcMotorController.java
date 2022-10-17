package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

public interface DcMotorController extends HardwareDevice {
    int getMotorCurrentPosition(int i);

    DcMotor.RunMode getMotorMode(int i);

    double getMotorPower(int i);

    boolean getMotorPowerFloat(int i);

    int getMotorTargetPosition(int i);

    MotorConfigurationType getMotorType(int i);

    DcMotor.ZeroPowerBehavior getMotorZeroPowerBehavior(int i);

    boolean isBusy(int i);

    void resetDeviceConfigurationForOpMode(int i);

    void setMotorMode(int i, DcMotor.RunMode runMode);

    void setMotorPower(int i, double d);

    void setMotorTargetPosition(int i, int i2);

    void setMotorType(int i, MotorConfigurationType motorConfigurationType);

    void setMotorZeroPowerBehavior(int i, DcMotor.ZeroPowerBehavior zeroPowerBehavior);
}
