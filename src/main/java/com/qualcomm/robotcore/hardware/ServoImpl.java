package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class ServoImpl implements Servo {
    protected ServoController controller;
    protected Servo.Direction direction;
    protected double limitPositionMax;
    protected double limitPositionMin;
    protected int portNumber;

    private double reverse(double d) {
        return (1.0d - d) + LynxServoController.apiPositionFirst;
    }

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public ServoImpl(ServoController servoController, int i) {
        this(servoController, i, Servo.Direction.FORWARD);
    }

    public ServoImpl(ServoController servoController, int i, Servo.Direction direction2) {
        this.controller = null;
        this.portNumber = -1;
        Servo.Direction direction3 = Servo.Direction.FORWARD;
        this.limitPositionMin = LynxServoController.apiPositionFirst;
        this.limitPositionMax = 1.0d;
        this.direction = direction2;
        this.controller = servoController;
        this.portNumber = i;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeServo);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; port " + this.portNumber;
    }

    public synchronized void resetDeviceConfigurationForOpMode() {
        this.limitPositionMin = LynxServoController.apiPositionFirst;
        this.limitPositionMax = 1.0d;
        this.direction = Servo.Direction.FORWARD;
    }

    public ServoController getController() {
        return this.controller;
    }

    public synchronized void setDirection(Servo.Direction direction2) {
        this.direction = direction2;
    }

    public Servo.Direction getDirection() {
        return this.direction;
    }

    public int getPortNumber() {
        return this.portNumber;
    }

    public synchronized void setPosition(double d) {
        double clip = Range.clip(d, (double) LynxServoController.apiPositionFirst, 1.0d);
        if (this.direction == Servo.Direction.REVERSE) {
            clip = reverse(clip);
        }
        internalSetPosition(Range.scale(clip, LynxServoController.apiPositionFirst, 1.0d, this.limitPositionMin, this.limitPositionMax));
    }

    /* access modifiers changed from: protected */
    public void internalSetPosition(double d) {
        this.controller.setServoPosition(this.portNumber, d);
    }

    public synchronized double getPosition() {
        double clip;
        synchronized (this) {
            double servoPosition = this.controller.getServoPosition(this.portNumber);
            if (this.direction == Servo.Direction.REVERSE) {
                servoPosition = reverse(servoPosition);
            }
            clip = Range.clip(Range.scale(servoPosition, this.limitPositionMin, this.limitPositionMax, LynxServoController.apiPositionFirst, 1.0d), (double) LynxServoController.apiPositionFirst, 1.0d);
        }
        return clip;
    }

    public synchronized void scaleRange(double d, double d2) {
        double clip = Range.clip(d, (double) LynxServoController.apiPositionFirst, 1.0d);
        double clip2 = Range.clip(d2, (double) LynxServoController.apiPositionFirst, 1.0d);
        if (clip < clip2) {
            this.limitPositionMin = clip;
            this.limitPositionMax = clip2;
        } else {
            throw new IllegalArgumentException("min must be less than max");
        }
    }
}
