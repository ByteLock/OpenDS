package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class CRServoImpl implements CRServo {
    protected static final double apiPowerMax = 1.0d;
    protected static final double apiPowerMin = -1.0d;
    protected static final double apiServoPositionMax = 1.0d;
    protected static final double apiServoPositionMin = 0.0d;
    protected ServoController controller;
    protected DcMotorSimple.Direction direction;
    protected int portNumber;

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public CRServoImpl(ServoController servoController, int i) {
        this(servoController, i, DcMotorSimple.Direction.FORWARD);
    }

    public CRServoImpl(ServoController servoController, int i, DcMotorSimple.Direction direction2) {
        this.controller = null;
        this.portNumber = -1;
        DcMotorSimple.Direction direction3 = DcMotorSimple.Direction.FORWARD;
        this.direction = direction2;
        this.controller = servoController;
        this.portNumber = i;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeContinuousRotationServo);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; port " + this.portNumber;
    }

    public synchronized void resetDeviceConfigurationForOpMode() {
        this.direction = DcMotorSimple.Direction.FORWARD;
    }

    public ServoController getController() {
        return this.controller;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public synchronized void setDirection(DcMotorSimple.Direction direction2) {
        this.direction = direction2;
    }

    public synchronized DcMotorSimple.Direction getDirection() {
        return this.direction;
    }

    public void setPower(double d) {
        this.controller.setServoPosition(this.portNumber, Range.scale(Range.clip(this.direction == DcMotorSimple.Direction.REVERSE ? -d : d, -1.0d, 1.0d), -1.0d, 1.0d, LynxServoController.apiPositionFirst, 1.0d));
    }

    public double getPower() {
        double scale = Range.scale(this.controller.getServoPosition(this.portNumber), LynxServoController.apiPositionFirst, 1.0d, -1.0d, 1.0d);
        return this.direction == DcMotorSimple.Direction.REVERSE ? -scale : scale;
    }
}
