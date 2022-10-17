package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.ElapsedTime;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.Semaphore;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;

public class Deadline extends ElapsedTime {
    protected TimeUnit awaitUnit = TimeUnit.NANOSECONDS;
    protected long msPollInterval = 125;
    protected long nsDeadline;
    protected final long nsDuration;

    public Deadline(long j, TimeUnit timeUnit) {
        long nanos = timeUnit.toNanos(j);
        this.nsDuration = nanos;
        this.nsDeadline = Misc.saturatingAdd(this.nsStartTime, nanos);
    }

    public void reset() {
        super.reset();
        this.nsDeadline = Misc.saturatingAdd(this.nsStartTime, this.nsDuration);
    }

    public void cancel() {
        expire();
    }

    public void expire() {
        this.nsDeadline = this.nsStartTime;
    }

    public long getDuration(TimeUnit timeUnit) {
        return timeUnit.convert(this.nsDuration, TimeUnit.NANOSECONDS);
    }

    public long getDeadline(TimeUnit timeUnit) {
        return timeUnit.convert(this.nsDeadline, TimeUnit.NANOSECONDS);
    }

    public long timeRemaining(TimeUnit timeUnit) {
        return timeUnit.convert(Math.max(0, this.nsDeadline - nsNow()), TimeUnit.NANOSECONDS);
    }

    public boolean hasExpired() {
        return timeRemaining(TimeUnit.NANOSECONDS) <= 0;
    }

    public boolean await(CountDownLatch countDownLatch) throws InterruptedException {
        long min;
        long convert = this.awaitUnit.convert(this.msPollInterval, TimeUnit.MILLISECONDS);
        do {
            min = Math.min(convert, timeRemaining(this.awaitUnit));
            if (min <= 0) {
                return false;
            }
        } while (!countDownLatch.await(min, this.awaitUnit));
        return true;
    }

    public boolean tryLock(Lock lock) throws InterruptedException {
        long min;
        long convert = this.awaitUnit.convert(this.msPollInterval, TimeUnit.MILLISECONDS);
        do {
            min = Math.min(convert, timeRemaining(this.awaitUnit));
            if (min <= 0) {
                return false;
            }
        } while (!lock.tryLock(min, this.awaitUnit));
        return true;
    }

    public boolean tryAcquire(Semaphore semaphore) throws InterruptedException {
        long min;
        long convert = this.awaitUnit.convert(this.msPollInterval, TimeUnit.MILLISECONDS);
        do {
            min = Math.min(convert, timeRemaining(this.awaitUnit));
            if (min <= 0) {
                return false;
            }
        } while (!semaphore.tryAcquire(min, this.awaitUnit));
        return true;
    }
}
