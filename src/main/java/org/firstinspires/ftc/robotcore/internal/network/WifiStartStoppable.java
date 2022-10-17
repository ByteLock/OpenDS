package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.Semaphore;
import java.util.concurrent.locks.ReentrantLock;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;

public abstract class WifiStartStoppable {
    public boolean DEBUG;
    public boolean DEBUG_VERBOSE;
    private final ReentrantLock completionLock;
    private Semaphore completionSemaphore;
    private boolean completionSuccess;
    protected ActionListenerFailure failureReason;
    protected int startCount;
    protected final Object startStopLock;
    protected final WifiDirectAgent wifiDirectAgent;
    protected final StartResult wifiDirectAgentStarted;

    /* access modifiers changed from: protected */
    public abstract boolean doStart() throws InterruptedException;

    /* access modifiers changed from: protected */
    public abstract void doStop() throws InterruptedException;

    public abstract String getTag();

    /* access modifiers changed from: protected */
    public boolean startIsIdempotent() {
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean startIsRefCounted() {
        return true;
    }

    protected WifiStartStoppable(WifiDirectAgent wifiDirectAgent2) {
        this.DEBUG = true;
        this.DEBUG_VERBOSE = false;
        this.startStopLock = new Object();
        this.startCount = 0;
        this.wifiDirectAgentStarted = new StartResult();
        this.completionLock = new ReentrantLock();
        this.completionSuccess = true;
        this.completionSemaphore = new Semaphore(0);
        this.failureReason = ActionListenerFailure.UNKNOWN;
        this.wifiDirectAgent = wifiDirectAgent2;
    }

    protected WifiStartStoppable(int i) {
        this.DEBUG = true;
        this.DEBUG_VERBOSE = false;
        this.startStopLock = new Object();
        this.startCount = 0;
        this.wifiDirectAgentStarted = new StartResult();
        this.completionLock = new ReentrantLock();
        this.completionSuccess = true;
        this.completionSemaphore = new Semaphore(0);
        this.failureReason = ActionListenerFailure.UNKNOWN;
        this.wifiDirectAgent = (WifiDirectAgent) this;
    }

    public WifiDirectAgent getWifiDirectAgent() {
        return this.wifiDirectAgent;
    }

    /* access modifiers changed from: protected */
    public void trace(String str, Runnable runnable) {
        trace(str, true, runnable);
    }

    /* access modifiers changed from: protected */
    public void trace(String str, boolean z, Runnable runnable) {
        if (z) {
            RobotLog.m61vv(getTag(), "%s()...", str);
        }
        try {
            runnable.run();
        } finally {
            if (z) {
                RobotLog.m61vv(getTag(), "...%s()", str);
            }
        }
    }

    /* access modifiers changed from: protected */
    public <T> T trace(String str, boolean z, Func<T> func) {
        if (z) {
            RobotLog.m61vv(getTag(), "%s()...", str);
        }
        try {
            return func.value();
        } finally {
            if (z) {
                RobotLog.m61vv(getTag(), "...%s()", str);
            }
        }
    }

    /* access modifiers changed from: protected */
    public <T> T lockCompletion(T t, Func<T> func) {
        try {
            this.completionLock.lockInterruptibly();
            t = func.value();
            this.completionLock.unlock();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        } catch (Throwable th) {
            this.completionLock.unlock();
            throw th;
        }
        return t;
    }

    /* access modifiers changed from: protected */
    public boolean isCompletionLockHeld() {
        return this.completionLock.isHeldByCurrentThread();
    }

    /* access modifiers changed from: protected */
    public boolean resetCompletion() {
        Assert.assertTrue(isCompletionLockHeld());
        this.completionSuccess = true;
        this.completionSemaphore = new Semaphore(0);
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean waitForCompletion() throws InterruptedException {
        Assert.assertNotNull(this.wifiDirectAgent);
        Assert.assertFalse(this.wifiDirectAgent.isLooperThread());
        Assert.assertTrue(isCompletionLockHeld());
        this.completionSemaphore.acquire();
        return this.completionSuccess;
    }

    /* access modifiers changed from: protected */
    public void releaseCompletion(boolean z) {
        Assert.assertNotNull(this.wifiDirectAgent);
        Assert.assertTrue(this.wifiDirectAgent.isLooperThread());
        Assert.assertFalse(isCompletionLockHeld());
        this.completionSuccess = z;
        this.completionSemaphore.release();
    }

    /* access modifiers changed from: protected */
    public boolean receivedInterrupt(InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean receivedCompletionInterrupt(InterruptedException interruptedException) {
        Assert.assertTrue(isCompletionLockHeld());
        return receivedInterrupt(interruptedException);
    }

    public StartResult start() {
        StartResult startResult = new StartResult();
        if (start(startResult)) {
            return startResult;
        }
        return null;
    }

    /* JADX WARNING: Removed duplicated region for block: B:13:0x001c A[Catch:{ all -> 0x0070 }] */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x0037 A[Catch:{ all -> 0x0070 }] */
    /* JADX WARNING: Removed duplicated region for block: B:23:0x004a A[Catch:{ all -> 0x0070 }] */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0053 A[Catch:{ all -> 0x0070 }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public boolean start(org.firstinspires.ftc.robotcore.internal.network.StartResult r8) {
        /*
            r7 = this;
            java.lang.Object r0 = r7.startStopLock     // Catch:{ all -> 0x0070 }
            monitor-enter(r0)     // Catch:{ all -> 0x0070 }
            org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable r1 = r8.getStartStoppable()     // Catch:{ all -> 0x006d }
            r2 = 0
            r3 = 1
            if (r1 == 0) goto L_0x0014
            org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable r1 = r8.getStartStoppable()     // Catch:{ all -> 0x006d }
            if (r1 != r7) goto L_0x0012
            goto L_0x0014
        L_0x0012:
            r1 = r2
            goto L_0x0015
        L_0x0014:
            r1 = r3
        L_0x0015:
            org.firstinspires.ftc.robotcore.internal.system.Assert.assertTrue(r1)     // Catch:{ all -> 0x006d }
            boolean r1 = r7.DEBUG_VERBOSE     // Catch:{ all -> 0x006d }
            if (r1 == 0) goto L_0x002f
            java.lang.String r1 = r7.getTag()     // Catch:{ all -> 0x006d }
            java.lang.String r4 = "start() count=%d..."
            java.lang.Object[] r5 = new java.lang.Object[r3]     // Catch:{ all -> 0x006d }
            int r6 = r7.startCount     // Catch:{ all -> 0x006d }
            java.lang.Integer r6 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x006d }
            r5[r2] = r6     // Catch:{ all -> 0x006d }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r1, (java.lang.String) r4, (java.lang.Object[]) r5)     // Catch:{ all -> 0x006d }
        L_0x002f:
            int r1 = r7.startCount     // Catch:{ all -> 0x006d }
            int r4 = r1 + 1
            r7.startCount = r4     // Catch:{ all -> 0x006d }
            if (r1 == 0) goto L_0x0040
            boolean r1 = r7.startIsIdempotent()     // Catch:{ all -> 0x006d }
            if (r1 == 0) goto L_0x003e
            goto L_0x0040
        L_0x003e:
            r1 = r3
            goto L_0x0045
        L_0x0040:
            boolean r2 = r7.callDoStart()     // Catch:{ all -> 0x006d }
            r1 = r2
        L_0x0045:
            r8.setStartStoppable(r7)     // Catch:{ all -> 0x006d }
            if (r2 == 0) goto L_0x004d
            r8.incrementStartCount()     // Catch:{ all -> 0x006d }
        L_0x004d:
            boolean r2 = r7.startIsRefCounted()     // Catch:{ all -> 0x006d }
            if (r2 != 0) goto L_0x005e
            int r2 = r8.getStartCount()     // Catch:{ all -> 0x006d }
            int r2 = java.lang.Math.min(r3, r2)     // Catch:{ all -> 0x006d }
            r8.setStartCount(r2)     // Catch:{ all -> 0x006d }
        L_0x005e:
            monitor-exit(r0)     // Catch:{ all -> 0x006d }
            boolean r8 = r7.DEBUG_VERBOSE
            if (r8 == 0) goto L_0x006c
            java.lang.String r8 = r7.getTag()
            java.lang.String r0 = "...start()"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r8, r0)
        L_0x006c:
            return r1
        L_0x006d:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x006d }
            throw r8     // Catch:{ all -> 0x0070 }
        L_0x0070:
            r8 = move-exception
            boolean r0 = r7.DEBUG_VERBOSE
            if (r0 == 0) goto L_0x007e
            java.lang.String r0 = r7.getTag()
            java.lang.String r1 = "...start()"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)
        L_0x007e:
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.start(org.firstinspires.ftc.robotcore.internal.network.StartResult):boolean");
    }

    /* access modifiers changed from: protected */
    public boolean callDoStart() {
        return ((Boolean) trace("doStart", this.DEBUG, new Func<Boolean>() {
            public Boolean value() {
                return (Boolean) WifiStartStoppable.this.lockCompletion(false, new Func<Boolean>() {
                    public Boolean value() {
                        try {
                            return Boolean.valueOf(WifiStartStoppable.this.doStart());
                        } catch (InterruptedException unused) {
                            Thread.currentThread().interrupt();
                            return false;
                        }
                    }
                });
            }
        })).booleanValue();
    }

    public void stop(StartResult startResult) {
        boolean z = false;
        Assert.assertTrue((startResult.getStartStoppable() == null && startResult.getStartCount() == 0) || startResult.getStartStoppable() == this);
        synchronized (this.startStopLock) {
            while (startResult.getStartCount() > 0) {
                startResult.decrementStartCount();
                internalStop();
            }
        }
        if (startResult.getStartCount() == 0) {
            z = true;
        }
        Assert.assertTrue(z);
    }

    public void terminate() {
        synchronized (this.startStopLock) {
            if (this.startCount > 0) {
                this.startCount = 1;
                internalStop();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void stopDueToFailure() {
        internalStop();
    }

    public void internalStop() {
        try {
            synchronized (this.startStopLock) {
                if (this.DEBUG_VERBOSE) {
                    RobotLog.m61vv(getTag(), "stop() count=%d...", Integer.valueOf(this.startCount));
                }
                int i = this.startCount;
                if (i > 0) {
                    int i2 = i - 1;
                    this.startCount = i2;
                    if (i2 == 0) {
                        callDoStop();
                    }
                }
            }
            if (this.DEBUG_VERBOSE) {
                RobotLog.m60vv(getTag(), "...stop()");
            }
        } catch (Throwable th) {
            if (this.DEBUG_VERBOSE) {
                RobotLog.m60vv(getTag(), "...stop()");
            }
            throw th;
        }
    }

    /* access modifiers changed from: protected */
    public void callDoStop() {
        trace("doStop", this.DEBUG, (Runnable) new Runnable() {
            public void run() {
                WifiStartStoppable.this.lockCompletion(null, new Func<Void>() {
                    public Void value() {
                        try {
                            WifiStartStoppable.this.doStop();
                            return null;
                        } catch (InterruptedException unused) {
                            Thread.currentThread().interrupt();
                            return null;
                        }
                    }
                });
            }
        });
    }

    public void restart() {
        if (this.DEBUG) {
            RobotLog.m60vv(getTag(), "restart()...");
        }
        try {
            synchronized (this.startStopLock) {
                if (this.startCount > 0) {
                    callDoStop();
                    callDoStart();
                }
            }
            if (this.DEBUG) {
                RobotLog.m60vv(getTag(), "...restart()");
            }
        } catch (Throwable th) {
            if (this.DEBUG) {
                RobotLog.m60vv(getTag(), "...restart()");
            }
            throw th;
        }
    }

    public ActionListenerFailure getActionListenerFailureReason() {
        return this.failureReason;
    }

    public enum ActionListenerFailure {
        UNKNOWN,
        P2P_UNSUPPORTED,
        ERROR,
        BUSY,
        NO_SERVICE_REQUESTS,
        WIFI_DISABLED;

        public static ActionListenerFailure from(int i, WifiDirectAgent wifiDirectAgent) {
            if (i == 0) {
                return ERROR;
            }
            boolean z = true;
            if (i == 1) {
                return P2P_UNSUPPORTED;
            }
            if (i == 2) {
                WifiState wifiState = wifiDirectAgent != null ? wifiDirectAgent.getWifiState() : WifiState.UNKNOWN;
                if (!(wifiState == WifiState.DISABLED || wifiState == WifiState.DISABLING)) {
                    z = false;
                }
                if (z) {
                    return WIFI_DISABLED;
                }
                return BUSY;
            } else if (i != 3) {
                return UNKNOWN;
            } else {
                return NO_SERVICE_REQUESTS;
            }
        }

        public String toString() {
            Application application = AppUtil.getInstance().getApplication();
            int i = C11293.f271xf13086cf[ordinal()];
            if (i == 1) {
                return application.getString(C0705R.string.actionlistenerfailure_nop2p);
            }
            if (i == 2) {
                return application.getString(C0705R.string.actionlistenerfailure_nowifi);
            }
            if (i == 3) {
                return application.getString(C0705R.string.actionlistenerfailure_busy);
            }
            if (i == 4) {
                return application.getString(C0705R.string.actionlistenerfailure_error);
            }
            if (i != 5) {
                return application.getString(C0705R.string.actionlistenerfailure_unknown);
            }
            return application.getString(C0705R.string.actionlistenerfailure_nosevicerequests);
        }
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$3 */
    static /* synthetic */ class C11293 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$network$WifiStartStoppable$ActionListenerFailure */
        static final /* synthetic */ int[] f271xf13086cf;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure[] r0 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f271xf13086cf = r0
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure r1 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.P2P_UNSUPPORTED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f271xf13086cf     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure r1 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.WIFI_DISABLED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f271xf13086cf     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure r1 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.BUSY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f271xf13086cf     // Catch:{ NoSuchFieldError -> 0x0033 }
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure r1 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.ERROR     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f271xf13086cf     // Catch:{ NoSuchFieldError -> 0x003e }
                org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable$ActionListenerFailure r1 = org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.ActionListenerFailure.NO_SERVICE_REQUESTS     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.WifiStartStoppable.C11293.<clinit>():void");
        }
    }
}
