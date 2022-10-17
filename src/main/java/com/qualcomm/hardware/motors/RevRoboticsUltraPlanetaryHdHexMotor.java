package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "@string/rev_ultraplanetary_hd_hex_name", xmlTag = "RevRoboticsUltraplanetaryHDHexMotor")
@DistributorInfo(distributor = "@string/rev_distributor", model = "REV-41-1600", url = "https://www.revrobotics.com/rev-41-1600/")
@MotorType(gearing = 20.0d, maxRPM = 300.0d, orientation = Rotation.f212CW, ticksPerRev = 560.0d)
public interface RevRoboticsUltraPlanetaryHdHexMotor {
}
