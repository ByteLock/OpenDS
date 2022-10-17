package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.util.SerialNumber;

public interface AnalogInputController extends HardwareDevice {
    double getAnalogInputVoltage(int i);

    double getMaxAnalogInputVoltage();

    SerialNumber getSerialNumber();
}
