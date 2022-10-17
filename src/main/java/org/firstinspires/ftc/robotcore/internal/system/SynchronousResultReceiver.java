package org.firstinspires.ftc.robotcore.internal.system;

import android.os.Bundle;
import android.os.Handler;
import android.os.ResultReceiver;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public abstract class SynchronousResultReceiver<T> extends ResultReceiver {
    private final BlockingQueue<T> resultQueue;
    private final String tag;

    /* access modifiers changed from: protected */
    public abstract T provideResult(int i, Bundle bundle);

    public SynchronousResultReceiver(int i, String str, Handler handler) {
        super(handler);
        this.resultQueue = new ArrayBlockingQueue(i);
        this.tag = str;
    }

    public final T awaitResult(long j, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
        T poll = this.resultQueue.poll(j, timeUnit);
        if (poll != null) {
            return poll;
        }
        throw new TimeoutException();
    }

    /* access modifiers changed from: protected */
    public final void onReceiveResult(int i, Bundle bundle) {
        if (!this.resultQueue.offer(provideResult(i, bundle))) {
            RobotLog.m66ww(this.tag, "The queue is full! Ignoring the result we just received.");
        }
    }
}
