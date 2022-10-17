package com.qualcomm.robotcore.hardware;

public interface LightSensor extends HardwareDevice {
    void enableLed(boolean z);

    double getLightDetected();

    double getRawLightDetected();

    double getRawLightDetectedMax();

    String status();
}
