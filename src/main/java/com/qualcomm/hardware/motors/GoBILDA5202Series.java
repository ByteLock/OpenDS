package com.qualcomm.hardware.motors;

import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.MotorType;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@DeviceProperties(builtIn = true, name = "GoBILDA 5202/3/4 series", xmlTag = "goBILDA5202SeriesMotor")
@DistributorInfo(distributor = "goBILDA_distributor", model = "goBILDA-5202", url = "https://www.gobilda.com/5202-series-yellow-jacket-planetary-gear-motors/")
@MotorType(gearing = 99.5d, maxRPM = 60.0d, orientation = Rotation.CCW, ticksPerRev = 2786.0d)
public interface GoBILDA5202Series {
}
