package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;

@DeviceProperties(builtIn = true, name = "@string/configTypeContinuousRotationServo", xmlTag = "ContinuousRotationServo")
@ServoType(flavor = ServoFlavor.CONTINUOUS)
public interface CRServo extends DcMotorSimple {
    ServoController getController();

    int getPortNumber();
}
