package com.qualcomm.robotcore.hardware;

import java.util.concurrent.locks.Lock;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

public interface I2cDevice extends HardwareDevice {
    void clearI2cPortActionFlag();

    void copyBufferIntoWriteBuffer(byte[] bArr);

    void enableI2cReadMode(I2cAddr i2cAddr, int i, int i2);

    void enableI2cWriteMode(I2cAddr i2cAddr, int i, int i2);

    byte[] getCopyOfReadBuffer();

    byte[] getCopyOfWriteBuffer();

    byte[] getI2cReadCache();

    Lock getI2cReadCacheLock();

    TimeWindow getI2cReadCacheTimeWindow();

    byte[] getI2cWriteCache();

    Lock getI2cWriteCacheLock();

    int getMaxI2cWriteLatency();

    boolean isArmed();

    @Deprecated
    boolean isI2cPortActionFlagSet();

    boolean isI2cPortInReadMode();

    boolean isI2cPortInWriteMode();

    void readI2cCacheFromController();

    @Deprecated
    void readI2cCacheFromModule();

    void setI2cPortActionFlag();

    void writeI2cCacheToController();

    @Deprecated
    void writeI2cCacheToModule();

    void writeI2cPortFlagOnlyToController();

    @Deprecated
    void writeI2cPortFlagOnlyToModule();
}
