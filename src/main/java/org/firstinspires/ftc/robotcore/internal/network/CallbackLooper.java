package org.firstinspires.ftc.robotcore.internal.network;

import android.os.Handler;
import android.os.Looper;
import java.util.concurrent.ExecutorService;

public class CallbackLooper {
    public static final String TAG = "CallbackLooper";
    protected static final ThreadLocal<CallbackLooper> tls = new ThreadLocal<>();
    protected ExecutorService executorService = null;
    protected Handler handler = null;
    protected Looper looper = null;
    protected Thread thread = null;

    protected static class InstanceHolder {
        public static final CallbackLooper theInstance;

        protected InstanceHolder() {
        }

        static {
            CallbackLooper callbackLooper = new CallbackLooper();
            theInstance = callbackLooper;
            callbackLooper.start();
        }
    }

    public static CallbackLooper getDefault() {
        return InstanceHolder.theInstance;
    }

    public synchronized void post(Runnable runnable) {
        this.handler.post(runnable);
    }

    public synchronized Looper getLooper() {
        return this.looper;
    }

    public synchronized Handler getHandler() {
        return this.handler;
    }

    public static boolean isLooperThread() {
        return tls.get() != null;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(5:4|5|6|7|8) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0021 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void start() {
        /*
            r3 = this;
            monitor-enter(r3)
            java.util.concurrent.ExecutorService r0 = r3.executorService     // Catch:{ all -> 0x002a }
            if (r0 != 0) goto L_0x0028
            java.lang.String r0 = "CallbackLooper"
            java.util.concurrent.ExecutorService r0 = com.qualcomm.robotcore.util.ThreadPool.newSingleThreadExecutor(r0)     // Catch:{ all -> 0x002a }
            r3.executorService = r0     // Catch:{ all -> 0x002a }
            java.util.concurrent.CountDownLatch r0 = new java.util.concurrent.CountDownLatch     // Catch:{ all -> 0x002a }
            r1 = 1
            r0.<init>(r1)     // Catch:{ all -> 0x002a }
            java.util.concurrent.ExecutorService r1 = r3.executorService     // Catch:{ all -> 0x002a }
            org.firstinspires.ftc.robotcore.internal.network.CallbackLooper$1 r2 = new org.firstinspires.ftc.robotcore.internal.network.CallbackLooper$1     // Catch:{ all -> 0x002a }
            r2.<init>(r0)     // Catch:{ all -> 0x002a }
            r1.submit(r2)     // Catch:{ all -> 0x002a }
            r0.await()     // Catch:{ InterruptedException -> 0x0021 }
            goto L_0x0028
        L_0x0021:
            java.lang.Thread r0 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x002a }
            r0.interrupt()     // Catch:{ all -> 0x002a }
        L_0x0028:
            monitor-exit(r3)
            return
        L_0x002a:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.CallbackLooper.start():void");
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(6:4|5|6|7|8|9) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0014 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void stop() {
        /*
            r5 = this;
            monitor-enter(r5)
            java.util.concurrent.ExecutorService r0 = r5.executorService     // Catch:{ all -> 0x0026 }
            if (r0 == 0) goto L_0x0024
            r0.shutdownNow()     // Catch:{ all -> 0x0026 }
            java.util.concurrent.ExecutorService r0 = r5.executorService     // Catch:{ InterruptedException -> 0x0014 }
            r1 = 3
            java.util.concurrent.TimeUnit r3 = java.util.concurrent.TimeUnit.SECONDS     // Catch:{ InterruptedException -> 0x0014 }
            java.lang.String r4 = "CallbackLooper"
            com.qualcomm.robotcore.util.ThreadPool.awaitTermination(r0, r1, r3, r4)     // Catch:{ InterruptedException -> 0x0014 }
            goto L_0x001b
        L_0x0014:
            java.lang.Thread r0 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0026 }
            r0.interrupt()     // Catch:{ all -> 0x0026 }
        L_0x001b:
            r0 = 0
            r5.executorService = r0     // Catch:{ all -> 0x0026 }
            r5.looper = r0     // Catch:{ all -> 0x0026 }
            r5.handler = r0     // Catch:{ all -> 0x0026 }
            r5.thread = r0     // Catch:{ all -> 0x0026 }
        L_0x0024:
            monitor-exit(r5)
            return
        L_0x0026:
            r0 = move-exception
            monitor-exit(r5)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.CallbackLooper.stop():void");
    }
}
