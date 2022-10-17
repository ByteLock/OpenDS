package com.qualcomm.robotcore.hardware;

import java.util.concurrent.BlockingQueue;

public interface I2cDeviceSynchReadHistory {
    BlockingQueue<TimestampedI2cData> getHistoryQueue();

    int getHistoryQueueCapacity();

    void setHistoryQueueCapacity(int i);
}
