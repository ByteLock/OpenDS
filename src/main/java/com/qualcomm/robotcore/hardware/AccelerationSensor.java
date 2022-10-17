package com.qualcomm.robotcore.hardware;

import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;

public interface AccelerationSensor extends HardwareDevice {
    Acceleration getAcceleration();

    String status();
}
