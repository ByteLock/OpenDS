package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "GoBILDA 5201 series", xmlTag = "goBILDA5201SeriesMotor")
@DistributorInfo(distributor = "goBILDA_distributor", model = "goBILDA-5201", url = "https://www.gobilda.com/5201-series-spur-gear-motors/")
@MotorType(gearing = 54.0d, maxRPM = 104.0d, orientation = Rotation.CCW, ticksPerRev = 1500.0d)
public interface GoBILDA5201Series {
}
