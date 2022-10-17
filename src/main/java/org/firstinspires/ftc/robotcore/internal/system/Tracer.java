package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.Callable;
import org.firstinspires.ftc.robotcore.external.function.InterruptableThrowingRunnable;
import org.firstinspires.ftc.robotcore.external.function.Supplier;
import org.firstinspires.ftc.robotcore.external.function.ThrowingRunnable;
import org.firstinspires.ftc.robotcore.external.function.ThrowingSupplier;
import org.firstinspires.inspection.InspectionState;

public class Tracer {
    public static boolean DEBUG = true;
    protected boolean enableErrorTrace;
    protected boolean enableTrace;
    public final String tag;

    public String getTag() {
        return this.tag;
    }

    public boolean enableTrace() {
        return this.enableTrace;
    }

    public boolean enableErrorTrace() {
        return this.enableErrorTrace;
    }

    protected Tracer(String str, boolean z, boolean z2) {
        this.tag = str;
        this.enableTrace = z;
        this.enableErrorTrace = z2;
    }

    public static Tracer create(String str, boolean z, boolean z2) {
        return new Tracer(str, z, z2);
    }

    public static Tracer create(String str, boolean z) {
        return create(str, z, z);
    }

    public static Tracer create(String str) {
        return create(str, DEBUG);
    }

    public static Tracer create(Object obj, String str) {
        return create(obj.getClass().getSimpleName() + str);
    }

    public static Tracer create(Object obj) {
        return create(obj.getClass().getSimpleName());
    }

    public static Tracer create(Class cls) {
        return create(cls.getSimpleName());
    }

    public String format(String str, Object... objArr) {
        return this.enableTrace ? Misc.formatInvariant(str, objArr) : InspectionState.NO_VERSION;
    }

    /* access modifiers changed from: protected */
    public void log(String str, Object... objArr) {
        if (this.enableTrace) {
            RobotLog.m43dd(getTag(), str, objArr);
        }
    }

    /* access modifiers changed from: protected */
    public void logError(String str, Object... objArr) {
        if (this.enableErrorTrace) {
            RobotLog.m49ee(getTag(), str, objArr);
        }
    }

    /* access modifiers changed from: protected */
    public void logError(Throwable th, String str, Object... objArr) {
        if (this.enableErrorTrace) {
            RobotLog.m51ee(getTag(), th, str, objArr);
        }
    }

    public void trace(String str, Object... objArr) {
        log(str, objArr);
    }

    public void traceError(String str, Object... objArr) {
        logError(str, objArr);
    }

    public void traceError(Throwable th, String str, Object... objArr) {
        logError(th, str, objArr);
    }

    public void trace(String str, Runnable runnable) {
        log("%s...", str);
        try {
            runnable.run();
        } finally {
            log("...%s", str);
        }
    }

    public <T> T trace(String str, Callable<T> callable) throws Exception {
        log("%s...", str);
        try {
            return callable.call();
        } finally {
            log("...%s", str);
        }
    }

    public <T> T trace(String str, Supplier<T> supplier) {
        log("%s...", str);
        try {
            return supplier.get();
        } finally {
            log("...%s", str);
        }
    }

    public <R> R traceResult(String str, Supplier<R> supplier) {
        log("%s...", str);
        try {
            R r = supplier.get();
            log("...%s: %s", str, r);
            return r;
        } catch (Throwable th) {
            log("...%s: %s", str, null);
            throw th;
        }
    }

    public <R, E extends Throwable> R traceResult(String str, ThrowingSupplier<R, E> throwingSupplier) throws Throwable {
        log("%s...", str);
        try {
            R r = throwingSupplier.get();
            log("...%s: %s", str, r);
            return r;
        } catch (Throwable th) {
            log("...%s: %s", str, null);
            throw th;
        }
    }

    public <T, E extends Throwable> T trace(String str, ThrowingSupplier<T, E> throwingSupplier) throws Throwable {
        log("%s...", str);
        try {
            return throwingSupplier.get();
        } finally {
            log("...%s", str);
        }
    }

    public <E extends Throwable> void trace(String str, ThrowingRunnable<E> throwingRunnable) throws Throwable {
        log("%s...", str);
        try {
            throwingRunnable.run();
        } finally {
            log("...%s", str);
        }
    }

    public <E extends Throwable> void trace(String str, InterruptableThrowingRunnable<E> interruptableThrowingRunnable) throws Throwable, InterruptedException {
        log("%s...", str);
        try {
            interruptableThrowingRunnable.run();
        } finally {
            log("...%s", str);
        }
    }
}
