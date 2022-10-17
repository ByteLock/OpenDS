package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class DcMotorImpl implements DcMotor {
    protected DcMotorController controller;
    protected DcMotorSimple.Direction direction;
    protected MotorConfigurationType motorType;
    protected int portNumber;

    public int getVersion() {
        return 1;
    }

    public DcMotorImpl(DcMotorController dcMotorController, int i) {
        this(dcMotorController, i, DcMotorSimple.Direction.FORWARD);
    }

    public DcMotorImpl(DcMotorController dcMotorController, int i, DcMotorSimple.Direction direction2) {
        this(dcMotorController, i, direction2, MotorConfigurationType.getUnspecifiedMotorType());
    }

    public DcMotorImpl(DcMotorController dcMotorController, int i, DcMotorSimple.Direction direction2, MotorConfigurationType motorConfigurationType) {
        this.controller = null;
        this.portNumber = -1;
        DcMotorSimple.Direction direction3 = DcMotorSimple.Direction.FORWARD;
        this.controller = dcMotorController;
        this.portNumber = i;
        this.direction = direction2;
        this.motorType = motorConfigurationType;
        RobotLog.m59v("DcMotorImpl(type=%s)", motorConfigurationType.getXmlTag());
        dcMotorController.setMotorType(i, motorConfigurationType.clone());
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeMotor);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; port " + this.portNumber;
    }

    public void resetDeviceConfigurationForOpMode() {
        setDirection(DcMotorSimple.Direction.FORWARD);
        this.controller.resetDeviceConfigurationForOpMode(this.portNumber);
    }

    public void close() {
        setPowerFloat();
    }

    public MotorConfigurationType getMotorType() {
        return this.controller.getMotorType(this.portNumber);
    }

    public void setMotorType(MotorConfigurationType motorConfigurationType) {
        this.controller.setMotorType(this.portNumber, motorConfigurationType);
    }

    public DcMotorController getController() {
        return this.controller;
    }

    public synchronized void setDirection(DcMotorSimple.Direction direction2) {
        this.direction = direction2;
    }

    public DcMotorSimple.Direction getDirection() {
        return this.direction;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public synchronized void setPower(double d) {
        double d2;
        if (getMode() == DcMotor.RunMode.RUN_TO_POSITION) {
            d2 = Math.abs(d);
        } else {
            d2 = adjustPower(d);
        }
        internalSetPower(d2);
    }

    /* access modifiers changed from: protected */
    public void internalSetPower(double d) {
        this.controller.setMotorPower(this.portNumber, d);
    }

    public synchronized double getPower() {
        double d;
        double motorPower = this.controller.getMotorPower(this.portNumber);
        if (getMode() == DcMotor.RunMode.RUN_TO_POSITION) {
            d = Math.abs(motorPower);
        } else {
            d = adjustPower(motorPower);
        }
        return d;
    }

    public boolean isBusy() {
        return this.controller.isBusy(this.portNumber);
    }

    public synchronized void setZeroPowerBehavior(DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        this.controller.setMotorZeroPowerBehavior(this.portNumber, zeroPowerBehavior);
    }

    public synchronized DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
        return this.controller.getMotorZeroPowerBehavior(this.portNumber);
    }

    @Deprecated
    public synchronized void setPowerFloat() {
        setZeroPowerBehavior(DcMotor.ZeroPowerBehavior.FLOAT);
        setPower(LynxServoController.apiPositionFirst);
    }

    public synchronized boolean getPowerFloat() {
        return getZeroPowerBehavior() == DcMotor.ZeroPowerBehavior.FLOAT && getPower() == LynxServoController.apiPositionFirst;
    }

    public synchronized void setTargetPosition(int i) {
        internalSetTargetPosition(adjustPosition(i));
    }

    /* access modifiers changed from: protected */
    public void internalSetTargetPosition(int i) {
        this.controller.setMotorTargetPosition(this.portNumber, i);
    }

    public synchronized int getTargetPosition() {
        return adjustPosition(this.controller.getMotorTargetPosition(this.portNumber));
    }

    public synchronized int getCurrentPosition() {
        return adjustPosition(this.controller.getMotorCurrentPosition(this.portNumber));
    }

    /* access modifiers changed from: protected */
    public int adjustPosition(int i) {
        return getOperationalDirection() == DcMotorSimple.Direction.REVERSE ? -i : i;
    }

    /* access modifiers changed from: protected */
    public double adjustPower(double d) {
        return getOperationalDirection() == DcMotorSimple.Direction.REVERSE ? -d : d;
    }

    /* access modifiers changed from: protected */
    public DcMotorSimple.Direction getOperationalDirection() {
        return this.motorType.getOrientation() == Rotation.CCW ? this.direction.inverted() : this.direction;
    }

    public synchronized void setMode(DcMotor.RunMode runMode) {
        internalSetMode(runMode.migrate());
    }

    /* access modifiers changed from: protected */
    public void internalSetMode(DcMotor.RunMode runMode) {
        this.controller.setMotorMode(this.portNumber, runMode);
    }

    public DcMotor.RunMode getMode() {
        return this.controller.getMotorMode(this.portNumber);
    }
}
