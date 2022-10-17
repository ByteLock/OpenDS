package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class WatchdogMonitor {
    public static final String TAG = "WatchdogMonitor";
    protected ExecutorService executorService = ThreadPool.newSingleThreadExecutor(TAG);
    protected Thread monitoredThread = null;
    protected Runner runner = new Runner();
    protected final Object startStopLock = new Object();

    public void close(boolean z) {
        synchronized (this.startStopLock) {
            ExecutorService executorService2 = this.executorService;
            if (executorService2 != null) {
                if (z) {
                    executorService2.shutdownNow();
                    ThreadPool.awaitTerminationOrExitApplication(this.executorService, 1, TimeUnit.SECONDS, TAG, "internal error");
                } else {
                    executorService2.shutdown();
                }
                this.executorService = null;
            }
        }
    }

    public <V> V monitor(Callable<V> callable, Callable<V> callable2, long j, TimeUnit timeUnit) throws ExecutionException, InterruptedException {
        this.monitoredThread = Thread.currentThread();
        Future<V> schedule = schedule(callable2, j, timeUnit);
        try {
            V call = callable.call();
            if (!schedule.cancel(false)) {
                call = schedule.get();
            }
            this.monitoredThread = null;
            return call;
        } catch (Exception e) {
            throw new ExecutionException("exception while monitoring", e);
        } catch (Throwable th) {
            if (!schedule.cancel(false)) {
                schedule.get();
            }
            this.monitoredThread = null;
            throw th;
        }
    }

    public Thread getMonitoredThread() {
        return this.monitoredThread;
    }

    /* access modifiers changed from: protected */
    public <V> Future<V> schedule(Callable<V> callable, long j, TimeUnit timeUnit) {
        try {
            this.runner.await();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        this.runner.initialize(callable, timeUnit.toMillis(j));
        try {
            this.executorService.submit(this.runner);
        } catch (RuntimeException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "executorService.submit() failed");
            this.runner.noteRunComplete();
        }
        return this.runner;
    }

    protected class Runner<V> implements Runnable, Future<V> {
        Callable<V> callable;
        V callableResult;
        final ReusableCountDownLatch cancelInterlock = new ReusableCountDownLatch(0);
        boolean done;
        ExecutionException executionException;
        boolean isCancelled;
        final ReusableCountDownLatch isCancelledAvailable = new ReusableCountDownLatch(0);
        long msTimeout;
        final ReusableCountDownLatch runComplete = new ReusableCountDownLatch(0);

        protected Runner() {
        }

        public void initialize(Callable<V> callable2, long j) {
            this.callable = callable2;
            this.msTimeout = j;
            this.runComplete.reset(1);
            this.cancelInterlock.reset(1);
            this.isCancelledAvailable.reset(1);
            this.callableResult = null;
            this.executionException = null;
            this.isCancelled = false;
            this.done = false;
        }

        /* access modifiers changed from: protected */
        public void noteRunComplete() {
            this.isCancelledAvailable.countDown();
            this.done = true;
            this.runComplete.countDown();
        }

        public void await() throws InterruptedException {
            this.runComplete.await();
        }

        public void await(long j, TimeUnit timeUnit) throws InterruptedException, TimeoutException {
            if (!this.runComplete.await(j, timeUnit)) {
                throw new TimeoutException("timeout awaiting watchdog timer");
            }
        }

        public boolean cancel(boolean z) {
            this.cancelInterlock.countDown();
            try {
                this.isCancelledAvailable.await();
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
            return this.isCancelled;
        }

        public boolean isCancelled() {
            return this.isCancelled;
        }

        public void run() {
            try {
                if (this.cancelInterlock.await(this.msTimeout, TimeUnit.MILLISECONDS)) {
                    this.isCancelled = true;
                    this.isCancelledAvailable.countDown();
                } else {
                    this.isCancelled = false;
                    this.isCancelledAvailable.countDown();
                    try {
                        this.callableResult = this.callable.call();
                    } catch (Exception e) {
                        this.executionException = new ExecutionException("exception during watchdog timer", e);
                    }
                }
            } catch (InterruptedException unused) {
            } catch (Throwable th) {
                noteRunComplete();
                throw th;
            }
            noteRunComplete();
        }

        public boolean isDone() {
            return this.done;
        }

        public V get() throws InterruptedException, ExecutionException {
            WatchdogMonitor.this.runner.await();
            if (WatchdogMonitor.this.runner.executionException == null) {
                return this.callableResult;
            }
            throw WatchdogMonitor.this.runner.executionException;
        }

        public V get(long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            WatchdogMonitor.this.runner.await(j, timeUnit);
            if (WatchdogMonitor.this.runner.executionException == null) {
                return this.callableResult;
            }
            throw WatchdogMonitor.this.runner.executionException;
        }
    }
}
