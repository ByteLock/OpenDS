package org.firstinspires.ftc.robotcore.internal.system;

import android.text.TextUtils;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.function.ThrowingRunnable;
import org.firstinspires.inspection.InspectionState;

public class ContinuationSynchronizer<T> {
    public static String TAG = "ContinuationSynchronizer";
    protected final Deadline deadline;
    protected boolean enableTrace;
    protected boolean isFinished;
    protected final CountDownLatch latch;
    protected final Object lock;
    protected Tracer tracer;
    protected T value;

    public ContinuationSynchronizer(long j, TimeUnit timeUnit, boolean z) {
        this(new Deadline(j, timeUnit), z);
    }

    public ContinuationSynchronizer(long j, TimeUnit timeUnit, boolean z, T t) {
        this(new Deadline(j, timeUnit), z, t);
    }

    public ContinuationSynchronizer() {
        this(new Deadline(2147483647L, TimeUnit.SECONDS));
    }

    public ContinuationSynchronizer(Deadline deadline2) {
        this(deadline2, true, (Object) null);
    }

    public ContinuationSynchronizer(Deadline deadline2, boolean z) {
        this(deadline2, z, (Object) null);
    }

    public ContinuationSynchronizer(Deadline deadline2, boolean z, T t) {
        this.enableTrace = false;
        this.lock = new Object();
        this.tracer = Tracer.create(TAG, z);
        this.deadline = deadline2;
        this.latch = new CountDownLatch(1);
        this.isFinished = false;
        this.value = t;
        this.enableTrace = z;
    }

    public T getValue() {
        return this.value;
    }

    public Deadline getDeadline() {
        return this.deadline;
    }

    public boolean isFinished() {
        return this.isFinished;
    }

    public boolean isSuccessful() {
        return this.value != null;
    }

    public void finish(T t) {
        finish(InspectionState.NO_VERSION, t);
    }

    public void finish(String str, final T t) {
        String str2;
        if (TextUtils.isEmpty(str)) {
            str2 = InspectionState.NO_VERSION;
        } else {
            str2 = "\"" + str + "\": ";
        }
        this.tracer.trace("finish(" + str2 + t + ")", (Runnable) new Runnable() {
            public void run() {
                synchronized (ContinuationSynchronizer.this.lock) {
                    if (!ContinuationSynchronizer.this.isFinished) {
                        ContinuationSynchronizer.this.value = t;
                    } else if (ContinuationSynchronizer.this.value == null) {
                        ContinuationSynchronizer.this.value = t;
                    }
                    ContinuationSynchronizer.this.isFinished = true;
                    ContinuationSynchronizer.this.deadline.expire();
                    ContinuationSynchronizer.this.latch.countDown();
                }
            }
        });
    }

    public void await() throws InterruptedException {
        if (!this.deadline.await(this.latch)) {
            this.tracer.traceError("deadline expired during await()", new Object[0]);
        }
    }

    public void await(String str) throws InterruptedException {
        Tracer tracer2 = this.tracer;
        tracer2.trace(tracer2.format("awaiting(%s)", str), new ThrowingRunnable<InterruptedException>() {
            public void run() throws InterruptedException {
                if (!ContinuationSynchronizer.this.deadline.await(ContinuationSynchronizer.this.latch)) {
                    ContinuationSynchronizer.this.tracer.traceError("deadline expired during await()", new Object[0]);
                }
            }
        });
    }
}
