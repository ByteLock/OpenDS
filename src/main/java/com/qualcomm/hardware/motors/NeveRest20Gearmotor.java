package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "NeveRest 20 Gearmotor", xmlTag = "NeveRest20Gearmotor")
@DistributorInfo(distributor = "AndyMark", model = "am-3102", url = "http://www.andymark.com/NeveRest-20-12V-Gearmotor-p/am-3102.htm")
@MotorType(gearing = 20.0d, maxRPM = 315.0d, orientation = Rotation.f212CW, ticksPerRev = 560.0d)
public interface NeveRest20Gearmotor {
}
