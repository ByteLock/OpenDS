package com.qualcomm.robotcore.hardware;

public final class I2cAddr {
    private final int i2cAddr7Bit;

    public I2cAddr(int i) {
        this.i2cAddr7Bit = i & 127;
    }

    public static I2cAddr zero() {
        return create7bit(0);
    }

    public static I2cAddr create7bit(int i) {
        return new I2cAddr(i);
    }

    public static I2cAddr create8bit(int i) {
        return new I2cAddr(i / 2);
    }

    public int get8Bit() {
        return this.i2cAddr7Bit * 2;
    }

    public int get7Bit() {
        return this.i2cAddr7Bit;
    }
}
