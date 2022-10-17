package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "Matrix 12v Motor", xmlTag = "Matrix12vMotor")
@DistributorInfo(distributor = "Modern Robotics", model = "50-0120", url = "http://www.modernroboticsinc.com/12v-6mm-motor-kit-2")
@MotorType(gearing = 52.8d, maxRPM = 190.0d, orientation = Rotation.f212CW, ticksPerRev = 1478.4d)
public interface Matrix12vMotor {
}
