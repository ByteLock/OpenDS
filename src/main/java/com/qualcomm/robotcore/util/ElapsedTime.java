package com.qualcomm.robotcore.util;

import java.util.concurrent.TimeUnit;

public class ElapsedTime {
    public static final long MILLIS_IN_NANO = 1000000;
    public static final long SECOND_IN_NANO = 1000000000;
    protected volatile long nsStartTime;
    protected final double resolution;

    public enum Resolution {
        SECONDS,
        MILLISECONDS
    }

    public ElapsedTime() {
        reset();
        this.resolution = 1.0E9d;
    }

    public ElapsedTime(long j) {
        this.nsStartTime = j;
        this.resolution = 1.0E9d;
    }

    public ElapsedTime(Resolution resolution2) {
        reset();
        if (C07551.$SwitchMap$com$qualcomm$robotcore$util$ElapsedTime$Resolution[resolution2.ordinal()] != 2) {
            this.resolution = 1.0E9d;
        } else {
            this.resolution = 1000000.0d;
        }
    }

    /* renamed from: com.qualcomm.robotcore.util.ElapsedTime$1 */
    static /* synthetic */ class C07551 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$util$ElapsedTime$Resolution;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.qualcomm.robotcore.util.ElapsedTime$Resolution[] r0 = com.qualcomm.robotcore.util.ElapsedTime.Resolution.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$util$ElapsedTime$Resolution = r0
                com.qualcomm.robotcore.util.ElapsedTime$Resolution r1 = com.qualcomm.robotcore.util.ElapsedTime.Resolution.SECONDS     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$util$ElapsedTime$Resolution     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.util.ElapsedTime$Resolution r1 = com.qualcomm.robotcore.util.ElapsedTime.Resolution.MILLISECONDS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.util.ElapsedTime.C07551.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public long nsNow() {
        return System.nanoTime();
    }

    public long now(TimeUnit timeUnit) {
        return timeUnit.convert(nsNow(), TimeUnit.NANOSECONDS);
    }

    public void reset() {
        this.nsStartTime = nsNow();
    }

    public double startTime() {
        return ((double) this.nsStartTime) / this.resolution;
    }

    public long startTimeNanoseconds() {
        return this.nsStartTime;
    }

    public double time() {
        return ((double) (nsNow() - this.nsStartTime)) / this.resolution;
    }

    public long time(TimeUnit timeUnit) {
        return timeUnit.convert(nanoseconds(), TimeUnit.NANOSECONDS);
    }

    public double seconds() {
        return ((double) nanoseconds()) / 1.0E9d;
    }

    public double milliseconds() {
        return seconds() * 1000.0d;
    }

    public long nanoseconds() {
        return nsNow() - this.nsStartTime;
    }

    public Resolution getResolution() {
        if (this.resolution == 1000000.0d) {
            return Resolution.MILLISECONDS;
        }
        return Resolution.SECONDS;
    }

    private String resolutionStr() {
        double d = this.resolution;
        if (d == 1.0E9d) {
            return "seconds";
        }
        return d == 1000000.0d ? "milliseconds" : "unknown units";
    }

    public void log(String str) {
        RobotLog.m58v(String.format("TIMER: %20s - %1.3f %s", new Object[]{str, Double.valueOf(time()), resolutionStr()}));
    }

    public String toString() {
        return String.format("%1.4f %s", new Object[]{Double.valueOf(time()), resolutionStr()});
    }
}
