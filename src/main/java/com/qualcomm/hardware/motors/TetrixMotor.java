package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "Tetrix Motor", xmlTag = "TetrixMotor")
@DistributorInfo(distributor = "Pitsco", model = "W39530", url = "http://www.pitsco.com/TETRIX_DC_Gear_Motor")
@MotorType(gearing = 52.0d, maxRPM = 165.0d, orientation = Rotation.f212CW, ticksPerRev = 1440.0d)
public interface TetrixMotor {
}
