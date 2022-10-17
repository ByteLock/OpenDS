package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;

public class DcMotorImplEx extends DcMotorImpl implements DcMotorEx {
    DcMotorControllerEx controllerEx;
    int targetPositionTolerance;

    public DcMotorImplEx(DcMotorController dcMotorController, int i) {
        this(dcMotorController, i, DcMotorSimple.Direction.FORWARD);
    }

    public DcMotorImplEx(DcMotorController dcMotorController, int i, DcMotorSimple.Direction direction) {
        this(dcMotorController, i, direction, MotorConfigurationType.getUnspecifiedMotorType());
    }

    public DcMotorImplEx(DcMotorController dcMotorController, int i, DcMotorSimple.Direction direction, MotorConfigurationType motorConfigurationType) {
        super(dcMotorController, i, direction, motorConfigurationType);
        this.targetPositionTolerance = 5;
        this.controllerEx = (DcMotorControllerEx) dcMotorController;
    }

    public void setMotorEnable() {
        this.controllerEx.setMotorEnable(getPortNumber());
    }

    public void setMotorDisable() {
        this.controllerEx.setMotorDisable(getPortNumber());
    }

    public boolean isMotorEnabled() {
        return this.controllerEx.isMotorEnabled(getPortNumber());
    }

    public synchronized void setVelocity(double d) {
        this.controllerEx.setMotorVelocity(getPortNumber(), adjustAngularRate(d));
    }

    public synchronized void setVelocity(double d, AngleUnit angleUnit) {
        this.controllerEx.setMotorVelocity(getPortNumber(), adjustAngularRate(d), angleUnit);
    }

    public synchronized double getVelocity() {
        return adjustAngularRate(this.controllerEx.getMotorVelocity(getPortNumber()));
    }

    public synchronized double getVelocity(AngleUnit angleUnit) {
        return adjustAngularRate(this.controllerEx.getMotorVelocity(getPortNumber(), angleUnit));
    }

    /* access modifiers changed from: protected */
    public double adjustAngularRate(double d) {
        return getOperationalDirection() == DcMotorSimple.Direction.REVERSE ? -d : d;
    }

    public void setPIDCoefficients(DcMotor.RunMode runMode, PIDCoefficients pIDCoefficients) {
        this.controllerEx.setPIDCoefficients(getPortNumber(), runMode, pIDCoefficients);
    }

    public void setPIDFCoefficients(DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) {
        this.controllerEx.setPIDFCoefficients(getPortNumber(), runMode, pIDFCoefficients);
    }

    public void setVelocityPIDFCoefficients(double d, double d2, double d3, double d4) {
        setPIDFCoefficients(DcMotor.RunMode.RUN_USING_ENCODER, new PIDFCoefficients(d, d2, d3, d4, MotorControlAlgorithm.PIDF));
    }

    public void setPositionPIDFCoefficients(double d) {
        setPIDFCoefficients(DcMotor.RunMode.RUN_TO_POSITION, new PIDFCoefficients(d, LynxServoController.apiPositionFirst, LynxServoController.apiPositionFirst, LynxServoController.apiPositionFirst, MotorControlAlgorithm.PIDF));
    }

    public PIDCoefficients getPIDCoefficients(DcMotor.RunMode runMode) {
        return this.controllerEx.getPIDCoefficients(getPortNumber(), runMode);
    }

    public PIDFCoefficients getPIDFCoefficients(DcMotor.RunMode runMode) {
        return this.controllerEx.getPIDFCoefficients(getPortNumber(), runMode);
    }

    public int getTargetPositionTolerance() {
        return this.targetPositionTolerance;
    }

    public synchronized void setTargetPositionTolerance(int i) {
        this.targetPositionTolerance = i;
    }

    /* access modifiers changed from: protected */
    public void internalSetTargetPosition(int i) {
        this.controllerEx.setMotorTargetPosition(this.portNumber, i, this.targetPositionTolerance);
    }

    public double getCurrent(CurrentUnit currentUnit) {
        return this.controllerEx.getMotorCurrent(this.portNumber, currentUnit);
    }

    public double getCurrentAlert(CurrentUnit currentUnit) {
        return this.controllerEx.getMotorCurrentAlert(this.portNumber, currentUnit);
    }

    public void setCurrentAlert(double d, CurrentUnit currentUnit) {
        this.controllerEx.setMotorCurrentAlert(this.portNumber, d, currentUnit);
    }

    public boolean isOverCurrent() {
        return this.controllerEx.isMotorOverCurrent(this.portNumber);
    }
}
