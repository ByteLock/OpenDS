package com.qualcomm.robotcore.hardware;

public class TimestampedI2cData extends TimestampedData {
    public I2cAddr i2cAddr;
    public int register;

    public static TimestampedI2cData makeFakeData(I2cAddr i2cAddr2, int i, int i2) {
        TimestampedI2cData timestampedI2cData = new TimestampedI2cData();
        timestampedI2cData.data = new byte[i2];
        timestampedI2cData.nanoTime = System.nanoTime();
        timestampedI2cData.i2cAddr = i2cAddr2;
        timestampedI2cData.register = i;
        return timestampedI2cData;
    }
}
