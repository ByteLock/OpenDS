package org.firstinspires.ftc.robotcore.internal.system;

import androidx.appcompat.widget.ActivityChooserView;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.atomic.AtomicInteger;

public class RefCounted {
    public static TraceLevel currentTraceLevel = TraceLevel.Normal;
    public static TraceLevel defaultTraceLevel = TraceLevel.Normal;
    public static boolean traceCtor = true;
    public static boolean traceDtor = true;
    public static boolean traceRefCount = true;
    protected boolean destroyed;
    /* access modifiers changed from: protected */
    public final Object lock;
    protected AtomicInteger refCount;
    protected TraceLevel traceLevel;

    /* access modifiers changed from: protected */
    public void destructor() {
    }

    /* access modifiers changed from: protected */
    public void postDestructor() {
    }

    /* access modifiers changed from: protected */
    public void preDestructor() {
    }

    public String getTag() {
        return getClass().getSimpleName();
    }

    protected RefCounted() {
        this(defaultTraceLevel);
    }

    protected RefCounted(TraceLevel traceLevel2) {
        this.refCount = new AtomicInteger(1);
        this.lock = new Object();
        this.destroyed = false;
        this.traceLevel = traceLevel2;
        if (traceCtor()) {
            RobotLog.m61vv(getTag(), "construct(0x%08x)", Integer.valueOf(hashCode()));
        }
    }

    public void addRef() {
        int incrementAndGet = this.refCount.incrementAndGet();
        if (traceRefCount()) {
            doTraceRefCnt(Misc.formatInvariant("ref:add(after=%d)", Integer.valueOf(incrementAndGet)));
        }
    }

    public int releaseRef() {
        int decrementAndGet = this.refCount.decrementAndGet();
        if (traceRefCount()) {
            doTraceRefCnt(Misc.formatInvariant("ref:release(after=%d)", Integer.valueOf(decrementAndGet)));
        }
        if (decrementAndGet == 0) {
            doLockAndDestruct();
        }
        return decrementAndGet;
    }

    /* access modifiers changed from: protected */
    public void doTraceRefCnt(String str) {
        RobotLog.m60vv(getTag(), AppUtil.getInstance().findCaller(Misc.formatInvariant("%s(%s)", str, getTraceIdentifier()), 1));
    }

    public String getTraceIdentifier() {
        return Misc.formatInvariant("hash=0x%08x", Integer.valueOf(hashCode()));
    }

    /* access modifiers changed from: protected */
    public final void doLockAndDestruct() {
        synchronized (this.lock) {
            if (!this.destroyed) {
                this.destroyed = true;
                preDestructor();
                if (traceDtor()) {
                    RobotLog.m61vv(getTag(), "destroy(%s)", getTraceIdentifier());
                }
                destructor();
                postDestructor();
            }
        }
    }

    public static class TraceLevel {
        public static final TraceLevel None = new TraceLevel(ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED);
        public static final TraceLevel Normal = new TraceLevel(10);
        public static final TraceLevel Verbose = new TraceLevel(20);
        public static final TraceLevel VeryVerbose = new TraceLevel(30);
        public final boolean traceRefCount;
        public final int value;

        public TraceLevel(int i) {
            this(i, false);
        }

        public TraceLevel(int i, boolean z) {
            this.value = i;
            this.traceRefCount = z;
        }

        public TraceLevel traceRefCnt() {
            return new TraceLevel(this.value, true);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isTraceArmed() {
        return this.traceLevel.value <= currentTraceLevel.value && currentTraceLevel.value != TraceLevel.None.value;
    }

    /* access modifiers changed from: protected */
    public boolean traceCtor() {
        return traceCtor && isTraceArmed();
    }

    /* access modifiers changed from: protected */
    public boolean traceDtor() {
        return traceDtor && isTraceArmed();
    }

    /* access modifiers changed from: protected */
    public boolean traceRefCount() {
        return traceRefCount && isTraceArmed() && this.traceLevel.traceRefCount;
    }
}
