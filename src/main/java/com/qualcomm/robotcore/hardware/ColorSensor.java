package com.qualcomm.robotcore.hardware;

public interface ColorSensor extends HardwareDevice {
    int alpha();

    int argb();

    int blue();

    void enableLed(boolean z);

    I2cAddr getI2cAddress();

    int green();

    int red();

    void setI2cAddress(I2cAddr i2cAddr);
}
