package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DcMotor;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public interface DcMotorEx extends DcMotor {
    double getCurrent(CurrentUnit currentUnit);

    double getCurrentAlert(CurrentUnit currentUnit);

    @Deprecated
    PIDCoefficients getPIDCoefficients(DcMotor.RunMode runMode);

    PIDFCoefficients getPIDFCoefficients(DcMotor.RunMode runMode);

    int getTargetPositionTolerance();

    double getVelocity();

    double getVelocity(AngleUnit angleUnit);

    boolean isMotorEnabled();

    boolean isOverCurrent();

    void setCurrentAlert(double d, CurrentUnit currentUnit);

    void setMotorDisable();

    void setMotorEnable();

    @Deprecated
    void setPIDCoefficients(DcMotor.RunMode runMode, PIDCoefficients pIDCoefficients);

    void setPIDFCoefficients(DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) throws UnsupportedOperationException;

    void setPositionPIDFCoefficients(double d);

    void setTargetPositionTolerance(int i);

    void setVelocity(double d);

    void setVelocity(double d, AngleUnit angleUnit);

    void setVelocityPIDFCoefficients(double d, double d2, double d3, double d4);
}
