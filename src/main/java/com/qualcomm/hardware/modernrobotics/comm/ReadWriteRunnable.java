package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.robotcore.eventloop.SyncdDevice;
import java.util.concurrent.ExecutorService;

public interface ReadWriteRunnable extends Runnable, SyncdDevice {
    public static final int MAX_BUFFER_SIZE = 256;

    public enum BlockingState {
        BLOCKING,
        WAITING
    }

    public interface Callback {
        void readComplete() throws InterruptedException;

        void shutdownComplete() throws InterruptedException;

        void startupComplete() throws InterruptedException;

        void writeComplete() throws InterruptedException;
    }

    public static class EmptyCallback implements Callback {
        public void readComplete() throws InterruptedException {
        }

        public void shutdownComplete() throws InterruptedException {
        }

        public void startupComplete() throws InterruptedException {
        }

        public void writeComplete() throws InterruptedException {
        }
    }

    void close();

    ReadWriteRunnableSegment createSegment(int i, int i2, int i3);

    void destroySegment(int i);

    void drainPendingWrites();

    void executeUsing(ExecutorService executorService);

    boolean getAcceptingWrites();

    ReadWriteRunnableSegment getSegment(int i);

    void queueSegmentRead(int i);

    void queueSegmentWrite(int i);

    byte[] read(int i, int i2);

    byte[] readFromWriteCache(int i, int i2);

    void resetWriteNeeded();

    void run();

    void setAcceptingWrites(boolean z);

    void setCallback(Callback callback);

    void suppressReads(boolean z);

    void write(int i, byte[] bArr);

    boolean writeNeeded();
}
