package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerPositionParams;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerVelocityParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@ExpansionHubMotorControllerVelocityParams(mo10132D = 0.0d, mo10133I = 3.0d, mo10134P = 10.0d)
@MotorType(gearing = 36.25d, maxRPM = 137.0d, orientation = Rotation.CCW, ticksPerRev = 288.0d)
@DeviceProperties(builtIn = true, name = "@string/rev_core_hex_name", xmlTag = "RevRoboticsCoreHexMotor")
@ExpansionHubMotorControllerPositionParams(mo10129D = 0.0d, mo10130I = 0.05d, mo10131P = 10.0d)
@DistributorInfo(distributor = "@string/rev_distributor", model = "REV-41-1300", url = "http://www.revrobotics.com/rev-41-1300")
public interface RevRoboticsCoreHexMotor {
}
