package com.qualcomm.robotcore.hardware;

public interface DcMotorSimple extends HardwareDevice {
    Direction getDirection();

    double getPower();

    void setDirection(Direction direction);

    void setPower(double d);

    public enum Direction {
        FORWARD,
        REVERSE;

        public Direction inverted() {
            Direction direction = FORWARD;
            return this == direction ? REVERSE : direction;
        }
    }
}
