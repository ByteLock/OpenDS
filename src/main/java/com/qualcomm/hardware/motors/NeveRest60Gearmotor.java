package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "NeveRest 60 Gearmotor", xmlTag = "NeveRest60Gearmotor")
@DistributorInfo(distributor = "AndyMark", model = "am-3103", url = "http://www.andymark.com/NeveRest-60-Gearmotor-p/am-3103.htm")
@MotorType(gearing = 60.0d, maxRPM = 105.0d, orientation = Rotation.CCW, ticksPerRev = 1680.0d)
public interface NeveRest60Gearmotor {
}
