package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;

public interface IrSeekerSensor extends HardwareDevice {

    public enum Mode {
        MODE_600HZ,
        MODE_1200HZ
    }

    double getAngle();

    I2cAddr getI2cAddress();

    IrSeekerIndividualSensor[] getIndividualSensors();

    Mode getMode();

    double getSignalDetectedThreshold();

    double getStrength();

    void setI2cAddress(I2cAddr i2cAddr);

    void setMode(Mode mode);

    void setSignalDetectedThreshold(double d);

    boolean signalDetected();

    public static class IrSeekerIndividualSensor {
        private double angle;
        private double strength;

        public IrSeekerIndividualSensor() {
            this(LynxServoController.apiPositionFirst, LynxServoController.apiPositionFirst);
        }

        public IrSeekerIndividualSensor(double d, double d2) {
            this.angle = d;
            this.strength = d2;
        }

        public double getSensorAngle() {
            return this.angle;
        }

        public double getSensorStrength() {
            return this.strength;
        }

        public String toString() {
            return String.format("IR Sensor: %3.1f degrees at %3.1f%% power", new Object[]{Double.valueOf(this.angle), Double.valueOf(this.strength * 100.0d)});
        }
    }
}
