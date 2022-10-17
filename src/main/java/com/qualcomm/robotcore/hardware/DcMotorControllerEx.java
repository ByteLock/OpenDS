package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public interface DcMotorControllerEx extends DcMotorController {
    double getMotorCurrent(int i, CurrentUnit currentUnit);

    double getMotorCurrentAlert(int i, CurrentUnit currentUnit);

    double getMotorVelocity(int i);

    double getMotorVelocity(int i, AngleUnit angleUnit);

    @Deprecated
    PIDCoefficients getPIDCoefficients(int i, DcMotor.RunMode runMode);

    PIDFCoefficients getPIDFCoefficients(int i, DcMotor.RunMode runMode);

    boolean isMotorEnabled(int i);

    boolean isMotorOverCurrent(int i);

    void setMotorCurrentAlert(int i, double d, CurrentUnit currentUnit);

    void setMotorDisable(int i);

    void setMotorEnable(int i);

    void setMotorTargetPosition(int i, int i2, int i3);

    void setMotorVelocity(int i, double d);

    void setMotorVelocity(int i, double d, AngleUnit angleUnit);

    @Deprecated
    void setPIDCoefficients(int i, DcMotor.RunMode runMode, PIDCoefficients pIDCoefficients);

    void setPIDFCoefficients(int i, DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) throws UnsupportedOperationException;
}
