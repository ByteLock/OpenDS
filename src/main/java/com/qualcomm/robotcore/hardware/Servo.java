package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;

@DeviceProperties(builtIn = true, name = "@string/configTypeServo", xmlTag = "Servo")
@ServoType(flavor = ServoFlavor.STANDARD)
public interface Servo extends HardwareDevice {
    public static final double MAX_POSITION = 1.0d;
    public static final double MIN_POSITION = 0.0d;

    public enum Direction {
        FORWARD,
        REVERSE
    }

    ServoController getController();

    Direction getDirection();

    int getPortNumber();

    double getPosition();

    void scaleRange(double d, double d2);

    void setDirection(Direction direction);

    void setPosition(double d);
}
