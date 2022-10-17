package com.qualcomm.robotcore.hardware;

public interface I2cDeviceSynchSimple extends HardwareDevice, HardwareDeviceHealth, I2cAddrConfig, RobotConfigNameable {
    void enableWriteCoalescing(boolean z);

    @Deprecated
    I2cAddr getI2cAddr();

    boolean getLogging();

    String getLoggingTag();

    boolean isArmed();

    boolean isWriteCoalescingEnabled();

    byte[] read(int i);

    byte[] read(int i, int i2);

    byte read8();

    byte read8(int i);

    TimestampedData readTimeStamped(int i);

    TimestampedData readTimeStamped(int i, int i2);

    @Deprecated
    void setI2cAddr(I2cAddr i2cAddr);

    void setLogging(boolean z);

    void setLoggingTag(String str);

    void waitForWriteCompletions(I2cWaitControl i2cWaitControl);

    void write(int i, byte[] bArr);

    void write(int i, byte[] bArr, I2cWaitControl i2cWaitControl);

    void write(byte[] bArr);

    void write(byte[] bArr, I2cWaitControl i2cWaitControl);

    void write8(int i);

    void write8(int i, int i2);

    void write8(int i, int i2, I2cWaitControl i2cWaitControl);

    void write8(int i, I2cWaitControl i2cWaitControl);
}
