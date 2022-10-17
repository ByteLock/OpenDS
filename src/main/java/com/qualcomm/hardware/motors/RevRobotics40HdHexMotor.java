package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "@string/rev_40_hd_hex_name", xmlTag = "RevRobotics40HDHexMotor", xmlTagAliases = {"RevRoboticsHDHexMotor"})
@DistributorInfo(distributor = "@string/rev_distributor", model = "REV-41-1301", url = "http://www.revrobotics.com/rev-41-1301")
@MotorType(gearing = 40.0d, maxRPM = 150.0d, orientation = Rotation.CCW, ticksPerRev = 1120.0d)
public interface RevRobotics40HdHexMotor {
}
