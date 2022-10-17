package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "NeveRest 40 Gearmotor", xmlTag = "NeveRest40Gearmotor")
@DistributorInfo(distributor = "AndyMark", model = "am-2964a", url = "http://www.andymark.com/NeveRest-40-Gearmotor-p/am-2964a.htm")
@MotorType(gearing = 40.0d, maxRPM = 160.0d, orientation = Rotation.CCW, ticksPerRev = 1120.0d)
public interface NeveRest40Gearmotor {
}
