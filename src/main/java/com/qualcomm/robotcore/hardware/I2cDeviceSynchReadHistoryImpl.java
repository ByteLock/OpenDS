package com.qualcomm.robotcore.hardware;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import org.firstinspires.ftc.robotcore.internal.collections.EvictingBlockingQueue;

public class I2cDeviceSynchReadHistoryImpl implements I2cDeviceSynchReadHistory {
    protected BlockingQueue<TimestampedI2cData> historyQueue;
    protected int historyQueueCapacity;
    protected final Object historyQueueLock = new Object();

    public I2cDeviceSynchReadHistoryImpl() {
        setHistoryQueueCapacity(0);
    }

    public BlockingQueue<TimestampedI2cData> getHistoryQueue() {
        BlockingQueue<TimestampedI2cData> blockingQueue;
        synchronized (this.historyQueueLock) {
            blockingQueue = this.historyQueue;
        }
        return blockingQueue;
    }

    public void setHistoryQueueCapacity(int i) {
        synchronized (this.historyQueueLock) {
            this.historyQueueCapacity = Math.max(0, i);
            if (i <= 0) {
                this.historyQueue = new ArrayBlockingQueue(1);
            } else {
                this.historyQueue = new EvictingBlockingQueue(new ArrayBlockingQueue(i));
            }
        }
    }

    public int getHistoryQueueCapacity() {
        int i;
        synchronized (this.historyQueueLock) {
            i = this.historyQueueCapacity;
        }
        return i;
    }

    public void addToHistoryQueue(TimestampedI2cData timestampedI2cData) {
        synchronized (this.historyQueueLock) {
            if (this.historyQueueCapacity > 0) {
                this.historyQueue.add(timestampedI2cData);
            }
        }
    }
}
