package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;

@DeviceProperties(builtIn = true, name = "Unspecified Motor", xmlTag = "Motor")
@MotorType(gearing = 52.0d, maxRPM = 165.0d, ticksPerRev = 1440.0d)
public interface UnspecifiedMotor {
}
