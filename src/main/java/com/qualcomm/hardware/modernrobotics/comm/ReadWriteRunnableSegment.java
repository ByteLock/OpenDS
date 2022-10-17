package com.qualcomm.hardware.modernrobotics.comm;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

public class ReadWriteRunnableSegment {
    private int address;
    private final byte[] bufferRead;
    private final byte[] bufferWrite;
    private int key;
    final Lock lockRead = new ReentrantLock();
    final Lock lockWrite;
    private boolean retryOnReadFailure;
    private TimeWindow timeWindow;

    public ReadWriteRunnableSegment(int i, int i2, int i3) {
        this.key = i;
        this.address = i2;
        this.bufferRead = new byte[i3];
        this.lockWrite = new ReentrantLock();
        this.bufferWrite = new byte[i3];
        this.retryOnReadFailure = true;
        this.timeWindow = new TimeWindow();
    }

    public int getKey() {
        return this.key;
    }

    public int getAddress() {
        return this.address;
    }

    public void setAddress(int i) {
        this.address = i;
    }

    public Lock getReadLock() {
        return this.lockRead;
    }

    public byte[] getReadBuffer() {
        return this.bufferRead;
    }

    public Lock getWriteLock() {
        return this.lockWrite;
    }

    public byte[] getWriteBuffer() {
        return this.bufferWrite;
    }

    public void setRetryOnReadFailure(boolean z) {
        this.retryOnReadFailure = z;
    }

    public boolean getRetryOnReadFailure() {
        return this.retryOnReadFailure;
    }

    public TimeWindow getTimeWindow() {
        return this.timeWindow;
    }

    public String toString() {
        return String.format("Segment - address:%d read:%d write:%d", new Object[]{Integer.valueOf(this.address), Integer.valueOf(this.bufferRead.length), Integer.valueOf(this.bufferWrite.length)});
    }
}
