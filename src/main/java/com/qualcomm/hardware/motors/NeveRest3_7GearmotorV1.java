package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "NeveRest 3.7 v1 Gearmotor", xmlTag = "NeveRest3.7v1Gearmotor")
@DistributorInfo(distributor = "AndyMark", model = "am-3461")
@MotorType(gearing = 3.7d, maxRPM = 1784.0d, orientation = Rotation.CCW, ticksPerRev = 44.4d)
public interface NeveRest3_7GearmotorV1 {
}
