package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class Watchdog {
    private static final String TAG = "Watchdog";
    private boolean alreadyGrowled;
    private Runnable bark;
    private Deadline deadline;
    private ScheduledExecutorService executorService;
    private ScheduledFuture<?> futureTask;
    private Runnable growl;
    private int growlTime;
    private int period;
    private long timeout;
    private TimeUnit unit;

    public Watchdog(Runnable runnable, int i, long j, TimeUnit timeUnit) {
        this.period = i;
        this.timeout = j;
        this.growlTime = 0;
        this.unit = timeUnit;
        this.bark = runnable;
        this.growl = null;
        this.deadline = null;
        this.executorService = null;
        this.futureTask = null;
    }

    public Watchdog(Runnable runnable, Runnable runnable2, int i, int i2, long j, TimeUnit timeUnit) {
        this.period = i2;
        this.timeout = j;
        this.unit = timeUnit;
        this.bark = runnable;
        this.growl = runnable2;
        this.growlTime = i;
        this.deadline = null;
        this.executorService = null;
        this.futureTask = null;
    }

    public synchronized void start() {
        if (this.deadline != null) {
            RobotLog.m48ee(TAG, "Don't start the same watchdog twice");
            return;
        }
        this.deadline = new Deadline(this.timeout, this.unit);
        ThreadPool.RecordingScheduledExecutor newScheduledExecutor = ThreadPool.newScheduledExecutor(1, TAG);
        this.executorService = newScheduledExecutor;
        WatchdogPeriodic watchdogPeriodic = new WatchdogPeriodic();
        int i = this.period;
        this.futureTask = newScheduledExecutor.scheduleAtFixedRate(watchdogPeriodic, (long) i, (long) i, this.unit);
        this.alreadyGrowled = false;
    }

    public synchronized void stroke() {
        Deadline deadline2 = this.deadline;
        if (deadline2 != null) {
            deadline2.reset();
        } else {
            RobotLog.m54ii(TAG, "The dog was stroked after it was euthanized.");
            start();
        }
        this.alreadyGrowled = false;
    }

    public synchronized void euthanize() {
        this.deadline = null;
        ScheduledExecutorService scheduledExecutorService = this.executorService;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            this.executorService = null;
        }
    }

    public boolean isRunning() {
        return this.executorService != null;
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Code restructure failed: missing block: B:19:0x003d, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void checkDog() {
        /*
            r4 = this;
            monitor-enter(r4)
            org.firstinspires.ftc.robotcore.internal.system.Deadline r0 = r4.deadline     // Catch:{ all -> 0x003e }
            if (r0 != 0) goto L_0x000e
            java.lang.String r0 = "Watchdog"
            java.lang.String r1 = "Checking a dog that is not alive."
            com.qualcomm.robotcore.util.RobotLog.m66ww(r0, r1)     // Catch:{ all -> 0x003e }
            monitor-exit(r4)
            return
        L_0x000e:
            boolean r0 = r0.hasExpired()     // Catch:{ all -> 0x003e }
            if (r0 == 0) goto L_0x001d
            java.lang.Runnable r0 = r4.bark     // Catch:{ all -> 0x003e }
            r0.run()     // Catch:{ all -> 0x003e }
            r4.euthanize()     // Catch:{ all -> 0x003e }
            goto L_0x003c
        L_0x001d:
            java.lang.Runnable r0 = r4.growl     // Catch:{ all -> 0x003e }
            if (r0 == 0) goto L_0x003c
            boolean r0 = r4.alreadyGrowled     // Catch:{ all -> 0x003e }
            if (r0 != 0) goto L_0x003c
            org.firstinspires.ftc.robotcore.internal.system.Deadline r0 = r4.deadline     // Catch:{ all -> 0x003e }
            java.util.concurrent.TimeUnit r1 = r4.unit     // Catch:{ all -> 0x003e }
            long r0 = r0.timeRemaining(r1)     // Catch:{ all -> 0x003e }
            int r2 = r4.growlTime     // Catch:{ all -> 0x003e }
            long r2 = (long) r2     // Catch:{ all -> 0x003e }
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 > 0) goto L_0x003c
            java.lang.Runnable r0 = r4.growl     // Catch:{ all -> 0x003e }
            r0.run()     // Catch:{ all -> 0x003e }
            r0 = 1
            r4.alreadyGrowled = r0     // Catch:{ all -> 0x003e }
        L_0x003c:
            monitor-exit(r4)
            return
        L_0x003e:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.system.Watchdog.checkDog():void");
    }

    private class WatchdogPeriodic implements Runnable {
        private WatchdogPeriodic() {
        }

        public void run() {
            Watchdog.this.checkDog();
        }
    }
}
