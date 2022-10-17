package com.qualcomm.hardware.rev;

import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;

@DeviceProperties(builtIn = true, compatibleControlSystems = {ControlSystem.REV_HUB}, description = "@string/rev_spark_mini_description", name = "@string/rev_spark_mini_name", xmlTag = "RevSPARKMini")
@ServoType(flavor = ServoFlavor.CONTINUOUS, usPulseFrameRate = 5500.0d, usPulseLower = 500.0d, usPulseUpper = 2500.0d)
public interface RevSPARKMini {
}
