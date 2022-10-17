package com.qualcomm.robotcore.hardware;

public interface PWMOutput extends HardwareDevice {
    int getPulseWidthOutputTime();

    int getPulseWidthPeriod();

    void setPulseWidthOutputTime(int i);

    void setPulseWidthPeriod(int i);
}
