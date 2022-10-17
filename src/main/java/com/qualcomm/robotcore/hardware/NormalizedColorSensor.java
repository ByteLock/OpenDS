package com.qualcomm.robotcore.hardware;

public interface NormalizedColorSensor extends HardwareDevice {
    float getGain();

    NormalizedRGBA getNormalizedColors();

    void setGain(float f);
}
