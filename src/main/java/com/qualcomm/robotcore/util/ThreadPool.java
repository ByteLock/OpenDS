package com.qualcomm.robotcore.util;

import android.os.Debug;
import android.os.Process;
import android.util.LongSparseArray;
import androidx.appcompat.widget.ActivityChooserView;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.WeakHashMap;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicInteger;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.Deadline;
import org.firstinspires.inspection.InspectionState;

public class ThreadPool {
    public static final String TAG = "ThreadPool";
    private static ScheduledExecutorService defaultScheduler = null;
    private static ExecutorService defaultSerialThreadPool = null;
    private static ExecutorService defaultThreadPool = null;
    private static Map<ExecutorService, Integer> extantExecutors = new WeakHashMap();
    private static final Object extantExecutorsLock = new Object();
    private static LongSparseArray<Integer> threadIdMap = new LongSparseArray<>();

    public interface ContainerOfThreads extends Iterable<Thread> {
        void noteFinishedThread(Thread thread);

        void noteNewThread(Thread thread);

        void setNameRootForThreads(String str);

        void setPriorityForThreads(Integer num);
    }

    public interface ThreadBorrowable {
        boolean canBorrowThread(Thread thread);
    }

    public static class Singleton<T> {
        public static int INFINITE_TIMEOUT = -1;
        /* access modifiers changed from: private */
        public boolean inFlight = false;
        /* access modifiers changed from: private */
        public final Object lock = new Object();
        private SingletonResult<T> result = null;
        private ExecutorService service = null;

        public void setService(ExecutorService executorService) {
            this.service = executorService;
        }

        public void reset() {
            synchronized (this.lock) {
                this.result = null;
                this.inFlight = false;
            }
        }

        public SingletonResult<T> submit(int i, final Runnable runnable) {
            return submit(i, new Callable<T>() {
                public T call() throws Exception {
                    runnable.run();
                    return null;
                }
            });
        }

        public SingletonResult<T> submit(Runnable runnable) {
            return submit(INFINITE_TIMEOUT, runnable);
        }

        public SingletonResult<T> submit(int i, final Callable<T> callable) {
            SingletonResult<T> singletonResult;
            synchronized (this.lock) {
                if (!this.inFlight) {
                    ExecutorService executorService = this.service;
                    if (executorService != null) {
                        this.inFlight = true;
                        this.result = new SingletonResult<>(i, this, executorService.submit(new Callable<T>() {
                            /* JADX WARNING: Missing exception handler attribute for start block: B:26:0x0034 */
                            /* Code decompiled incorrectly, please refer to instructions dump. */
                            public T call() throws java.lang.Exception {
                                /*
                                    r5 = this;
                                    r0 = 0
                                    r1 = 0
                                    java.util.concurrent.Callable r2 = r6     // Catch:{ InterruptedException -> 0x0034, Exception -> 0x001b }
                                    java.lang.Object r0 = r2.call()     // Catch:{ InterruptedException -> 0x0034, Exception -> 0x001b }
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r2 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this
                                    java.lang.Object r2 = r2.lock
                                    monitor-enter(r2)
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r3 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this     // Catch:{ all -> 0x0016 }
                                    boolean unused = r3.inFlight = r1     // Catch:{ all -> 0x0016 }
                                    monitor-exit(r2)     // Catch:{ all -> 0x0016 }
                                    return r0
                                L_0x0016:
                                    r0 = move-exception
                                    monitor-exit(r2)     // Catch:{ all -> 0x0016 }
                                    throw r0
                                L_0x0019:
                                    r0 = move-exception
                                    goto L_0x004c
                                L_0x001b:
                                    r2 = move-exception
                                    java.lang.String r3 = "ThreadPool"
                                    java.lang.String r4 = "exception thrown during Singleton.submit()"
                                    com.qualcomm.robotcore.util.RobotLog.m50ee((java.lang.String) r3, (java.lang.Throwable) r2, (java.lang.String) r4)     // Catch:{ all -> 0x0019 }
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r2 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this
                                    java.lang.Object r2 = r2.lock
                                    monitor-enter(r2)
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r3 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this     // Catch:{ all -> 0x0031 }
                                    boolean unused = r3.inFlight = r1     // Catch:{ all -> 0x0031 }
                                    monitor-exit(r2)     // Catch:{ all -> 0x0031 }
                                    return r0
                                L_0x0031:
                                    r0 = move-exception
                                    monitor-exit(r2)     // Catch:{ all -> 0x0031 }
                                    throw r0
                                L_0x0034:
                                    java.lang.Thread r2 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0019 }
                                    r2.interrupt()     // Catch:{ all -> 0x0019 }
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r2 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this
                                    java.lang.Object r2 = r2.lock
                                    monitor-enter(r2)
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r3 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this     // Catch:{ all -> 0x0049 }
                                    boolean unused = r3.inFlight = r1     // Catch:{ all -> 0x0049 }
                                    monitor-exit(r2)     // Catch:{ all -> 0x0049 }
                                    return r0
                                L_0x0049:
                                    r0 = move-exception
                                    monitor-exit(r2)     // Catch:{ all -> 0x0049 }
                                    throw r0
                                L_0x004c:
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r2 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this
                                    java.lang.Object r2 = r2.lock
                                    monitor-enter(r2)
                                    com.qualcomm.robotcore.util.ThreadPool$Singleton r3 = com.qualcomm.robotcore.util.ThreadPool.Singleton.this     // Catch:{ all -> 0x005a }
                                    boolean unused = r3.inFlight = r1     // Catch:{ all -> 0x005a }
                                    monitor-exit(r2)     // Catch:{ all -> 0x005a }
                                    throw r0
                                L_0x005a:
                                    r0 = move-exception
                                    monitor-exit(r2)     // Catch:{ all -> 0x005a }
                                    throw r0
                                */
                                throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.util.ThreadPool.Singleton.C07652.call():java.lang.Object");
                            }
                        }));
                    } else {
                        throw new IllegalArgumentException("Singleton service must be set before work is submitted");
                    }
                }
                singletonResult = this.result;
            }
            return singletonResult;
        }

        public SingletonResult<T> submit(Callable<T> callable) {
            return submit(INFINITE_TIMEOUT, callable);
        }

        public SingletonResult<T> getResult() {
            SingletonResult<T> singletonResult;
            synchronized (this.lock) {
                singletonResult = this.result;
            }
            return singletonResult;
        }

        public T await(long j) throws InterruptedException {
            SingletonResult result2 = getResult();
            if (result2 != null) {
                return result2.await(j);
            }
            return null;
        }

        public T await() throws InterruptedException {
            SingletonResult result2 = getResult();
            if (result2 != null) {
                return result2.await();
            }
            return null;
        }
    }

    public static class SingletonResult<T> {
        private Future<T> future;
        private long nsDeadline;
        private Singleton<T> singleton;

        public SingletonResult(int i, Singleton<T> singleton2, Future<T> future2) {
            long j;
            this.singleton = singleton2;
            this.future = future2;
            if (i == Singleton.INFINITE_TIMEOUT) {
                j = -1;
            } else {
                j = System.nanoTime() + (((long) i) * ElapsedTime.MILLIS_IN_NANO);
            }
            this.nsDeadline = j;
        }

        public void setFuture(Future<T> future2) {
            this.future = future2;
        }

        public T await(long j) throws InterruptedException {
            try {
                Future<T> future2 = this.future;
                if (future2 != null) {
                    return future2.get(j, TimeUnit.MILLISECONDS);
                }
                return null;
            } catch (ExecutionException e) {
                RobotLog.m50ee(ThreadPool.TAG, (Throwable) e, "singleton threw ExecutionException");
                return null;
            } catch (TimeoutException unused) {
                long j2 = this.nsDeadline;
                if (j2 <= 0 || j2 >= System.nanoTime()) {
                    return null;
                }
                boolean unused2 = this.singleton.inFlight = false;
                return null;
            }
        }

        public T await() throws InterruptedException {
            if (this.nsDeadline >= 0) {
                return await(Math.max(0, this.nsDeadline - System.nanoTime()) / ElapsedTime.MILLIS_IN_NANO);
            }
            try {
                Future<T> future2 = this.future;
                if (future2 != null) {
                    return future2.get();
                }
                return null;
            } catch (ExecutionException e) {
                RobotLog.m50ee(ThreadPool.TAG, (Throwable) e, "singleton threw ExecutionException");
                return null;
            }
        }
    }

    public static ExecutorService getDefault() {
        ExecutorService executorService;
        synchronized (ThreadPool.class) {
            if (defaultThreadPool == null) {
                defaultThreadPool = newCachedThreadPool("default threadpool");
            }
            executorService = defaultThreadPool;
        }
        return executorService;
    }

    public static ExecutorService getDefaultSerial() {
        ExecutorService executorService;
        synchronized (ThreadPool.class) {
            if (defaultSerialThreadPool == null) {
                defaultSerialThreadPool = newSingleThreadExecutor("default serial threadpool");
            }
            executorService = defaultSerialThreadPool;
        }
        return executorService;
    }

    public static ScheduledExecutorService getDefaultScheduler() {
        ScheduledExecutorService scheduledExecutorService;
        synchronized (ThreadPool.class) {
            if (defaultScheduler == null) {
                defaultScheduler = newScheduledExecutor(24, "default scheduler");
            }
            scheduledExecutorService = defaultScheduler;
        }
        return scheduledExecutorService;
    }

    public static ExecutorService newSingleThreadExecutor(String str) {
        RecordingThreadPool recordingThreadPool = new RecordingThreadPool(1, 1, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        if (str != null) {
            recordingThreadPool.setNameRootForThreads(str);
        }
        noteNewExecutor(recordingThreadPool);
        return recordingThreadPool;
    }

    public static ExecutorService newFixedThreadPool(int i, String str) {
        RecordingThreadPool recordingThreadPool = new RecordingThreadPool(i, i, 0, TimeUnit.MILLISECONDS, new LinkedBlockingQueue());
        if (str != null) {
            recordingThreadPool.setNameRootForThreads(str);
        }
        noteNewExecutor(recordingThreadPool);
        return recordingThreadPool;
    }

    public static ExecutorService newCachedThreadPool(String str) {
        RecordingThreadPool recordingThreadPool = new RecordingThreadPool(0, ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED, 30, TimeUnit.SECONDS, new SynchronousQueue());
        if (str != null) {
            recordingThreadPool.setNameRootForThreads(str);
        }
        noteNewExecutor(recordingThreadPool);
        return recordingThreadPool;
    }

    public static RecordingScheduledExecutor newScheduledExecutor(int i, String str) {
        RecordingScheduledExecutor recordingScheduledExecutor = new RecordingScheduledExecutor(i);
        if (str != null) {
            recordingScheduledExecutor.setNameRootForThreads(str);
        }
        noteNewExecutor(recordingScheduledExecutor);
        return recordingScheduledExecutor;
    }

    private static void noteNewExecutor(ExecutorService executorService) {
        synchronized (extantExecutorsLock) {
            extantExecutors.put(executorService, 1);
        }
    }

    /* access modifiers changed from: private */
    public static void noteTID(Thread thread, int i) {
        synchronized (ThreadPool.class) {
            threadIdMap.put(thread.getId(), Integer.valueOf(i));
        }
    }

    /* access modifiers changed from: private */
    public static void removeTID(Thread thread) {
        synchronized (ThreadPool.class) {
            threadIdMap.remove(thread.getId());
        }
    }

    public static int getTID(Thread thread) {
        return getTID(thread.getId());
    }

    public static int getTID(long j) {
        int intValue;
        synchronized (ThreadPool.class) {
            intValue = threadIdMap.get(j, 0).intValue();
        }
        return intValue;
    }

    public static boolean awaitTermination(ExecutorService executorService, long j, TimeUnit timeUnit, String str) throws InterruptedException {
        boolean isTerminated;
        verifyNotOnExecutorThread(executorService);
        Deadline deadline = new Deadline(j, timeUnit);
        int i = 0;
        while (true) {
            isTerminated = executorService.isTerminated();
            if (isTerminated) {
                break;
            }
            RobotLog.m61vv(TAG, "waiting for service %s", str);
            if (executorService.awaitTermination(Math.min((long) 2500, deadline.timeRemaining(TimeUnit.MILLISECONDS)), TimeUnit.MILLISECONDS)) {
                Assert.assertTrue(executorService.isTerminated());
                RobotLog.m61vv(TAG, "service %s terminated in awaitTermination()", str);
                isTerminated = true;
                break;
            } else if (deadline.hasExpired()) {
                RobotLog.m49ee(TAG, "deadline expired waiting for service termination: %s", str);
                break;
            } else {
                i++;
                RobotLog.m61vv(TAG, "awaiting shutdown: thread pool=\"%s\" attempt=%d", str, Integer.valueOf(i));
                logThreadStacks(executorService);
                interruptThreads(executorService);
            }
        }
        if (isTerminated) {
            RobotLog.m61vv(TAG, "executive service %s(0x%08x) is terminated", str, Integer.valueOf(executorService.hashCode()));
        } else {
            RobotLog.m61vv(TAG, "executive service %s(0x%08x) is NOT terminated", str, Integer.valueOf(executorService.hashCode()));
            synchronized (extantExecutorsLock) {
                System.gc();
                for (ExecutorService logThreadStacks : extantExecutors.keySet()) {
                    logThreadStacks(logThreadStacks);
                }
            }
        }
        return isTerminated;
    }

    private static void logThreadStacks(ExecutorService executorService) {
        if (executorService instanceof ContainerOfThreads) {
            for (Thread thread : (ContainerOfThreads) executorService) {
                if (thread.isAlive()) {
                    RobotLog.logStackTrace(thread, InspectionState.NO_VERSION, new Object[0]);
                }
            }
        }
    }

    private static void interruptThreads(ExecutorService executorService) {
        if (executorService instanceof ContainerOfThreads) {
            for (Thread thread : (ContainerOfThreads) executorService) {
                if (thread.isAlive()) {
                    if (thread.getId() == Thread.currentThread().getId()) {
                        RobotLog.m60vv(TAG, "interrupting current thread");
                    }
                    thread.interrupt();
                }
            }
        }
    }

    private static void verifyNotOnExecutorThread(ExecutorService executorService) {
        if (executorService instanceof ContainerOfThreads) {
            for (Thread thread : (ContainerOfThreads) executorService) {
                if (thread == Thread.currentThread()) {
                    Assert.assertFailed();
                }
            }
        }
    }

    public static void awaitTerminationOrExitApplication(ExecutorService executorService, long j, TimeUnit timeUnit, String str, String str2) {
        try {
            if (!awaitTermination(executorService, j, timeUnit, str)) {
                exitApplication(str, str2);
            }
        } catch (InterruptedException unused) {
            RobotLog.m61vv(TAG, "awaitTerminationOrExitApplication %s; interrupt thrown", str);
            try {
                Thread.sleep(100);
            } catch (InterruptedException unused2) {
                Thread.currentThread().interrupt();
            }
            if (!executorService.isTerminated()) {
                RobotLog.m61vv(TAG, "awaitTerminationOrExitApplication %s; exiting application after interrupt", str);
                exitApplication(str, str2);
            }
        }
    }

    public static boolean awaitFuture(Future future, long j, TimeUnit timeUnit) {
        try {
            future.get(j, timeUnit);
        } catch (CancellationException | ExecutionException unused) {
        } catch (TimeoutException unused2) {
            return false;
        } catch (InterruptedException unused3) {
            Thread.currentThread().interrupt();
            return false;
        }
        return true;
    }

    public static void cancelFutureOrExitApplication(Future future, long j, TimeUnit timeUnit, String str, String str2) {
        try {
            future.cancel(true);
            future.get(j, timeUnit);
        } catch (CancellationException unused) {
        } catch (ExecutionException e) {
            RobotLog.logExceptionHeader(e, "exception thrown in future; ignoring", new Object[0]);
        } catch (TimeoutException unused2) {
            exitApplication(str, str2);
        } catch (InterruptedException unused3) {
            Thread.currentThread().interrupt();
        }
    }

    public static void exitApplication(String str, String str2) {
        RobotLog.m48ee(TAG, "*****************************************************************");
        RobotLog.m49ee(TAG, "%s took too long to exit; emergency killing app.", str);
        RobotLog.m49ee(TAG, "%s", str2);
        RobotLog.m48ee(TAG, "*****************************************************************");
        while (Debug.isDebuggerConnected()) {
            Thread.yield();
        }
        AppUtil.getInstance().exitApplication(-1);
    }

    /* JADX INFO: finally extract failed */
    public static void logThreadLifeCycle(String str, Runnable runnable) {
        try {
            Thread.currentThread().setName(str);
            RobotLog.m58v(String.format("thread: '%s' starting...", new Object[]{str}));
            runnable.run();
            RobotLog.m58v(String.format("thread: ...terminating '%s'", new Object[]{str}));
        } catch (Throwable th) {
            RobotLog.m58v(String.format("thread: ...terminating '%s'", new Object[]{str}));
            throw th;
        }
    }

    static class ThreadFactoryImpl implements ThreadFactory {
        final ContainerOfThreads container;
        final ThreadFactory threadFactory = Executors.defaultThreadFactory();

        ThreadFactoryImpl(ContainerOfThreads containerOfThreads) {
            this.container = containerOfThreads;
        }

        public Thread newThread(final Runnable runnable) {
            Thread newThread = this.threadFactory.newThread(new Runnable() {
                public void run() {
                    ThreadPool.noteTID(Thread.currentThread(), Process.myTid());
                    try {
                        runnable.run();
                    } finally {
                        ThreadFactoryImpl.this.container.noteFinishedThread(Thread.currentThread());
                        ThreadPool.removeTID(Thread.currentThread());
                    }
                }
            });
            this.container.noteNewThread(newThread);
            return newThread;
        }
    }

    static class ContainerOfThreadsRecorder implements ContainerOfThreads {
        String nameRootForThreads = null;
        Integer priorityForThreads = null;
        AtomicInteger threadCount = new AtomicInteger(0);
        Queue<Thread> threads = new ConcurrentLinkedQueue();

        ContainerOfThreadsRecorder() {
        }

        public void setNameRootForThreads(String str) {
            this.nameRootForThreads = str;
        }

        public void setPriorityForThreads(Integer num) {
            this.priorityForThreads = num;
        }

        public void noteNewThread(Thread thread) {
            this.threads.add(thread);
            String str = this.nameRootForThreads;
            if (str != null) {
                thread.setName(String.format("%s-#%d", new Object[]{str, Integer.valueOf(this.threadCount.getAndIncrement())}));
            }
            Integer num = this.priorityForThreads;
            if (num != null) {
                thread.setPriority(num.intValue());
            }
            logThread(thread, "added");
        }

        public void noteFinishedThread(Thread thread) {
            logThread(thread, "removed");
            this.threads.remove(thread);
        }

        /* access modifiers changed from: protected */
        public void logThread(Thread thread, String str) {
            String str2;
            Object[] objArr = new Object[6];
            objArr[0] = Integer.valueOf(hashCode());
            if (this.nameRootForThreads == null) {
                str2 = InspectionState.NO_VERSION;
            } else {
                str2 = ": " + this.nameRootForThreads;
            }
            objArr[1] = str2;
            objArr[2] = str;
            objArr[3] = Long.valueOf(thread.getId());
            objArr[4] = Integer.valueOf(ThreadPool.getTID(thread));
            objArr[5] = Integer.valueOf(this.threads.size());
            RobotLog.m61vv(ThreadPool.TAG, "container(0x%08x%s) %s id=%d TID=%d count=%d", objArr);
        }

        public Iterator<Thread> iterator() {
            return this.threads.iterator();
        }
    }

    protected static Throwable retrieveUserException(Runnable runnable, Throwable th) {
        if (th != null || !(runnable instanceof Future)) {
            return th;
        }
        try {
            if (!((Future) runnable).isDone()) {
                return th;
            }
            ((Future) runnable).get();
            return th;
        } catch (CancellationException unused) {
            return null;
        } catch (ExecutionException e) {
            return e.getCause();
        } catch (InterruptedException unused2) {
            Thread.currentThread().interrupt();
            return th;
        }
    }

    public static class RecordingThreadPool extends ContainerOfThreadsRecorder implements ExecutorService {
        ThreadPoolExecutor executor;

        public /* bridge */ /* synthetic */ Iterator iterator() {
            return super.iterator();
        }

        public /* bridge */ /* synthetic */ void noteFinishedThread(Thread thread) {
            super.noteFinishedThread(thread);
        }

        public /* bridge */ /* synthetic */ void noteNewThread(Thread thread) {
            super.noteNewThread(thread);
        }

        public /* bridge */ /* synthetic */ void setNameRootForThreads(String str) {
            super.setNameRootForThreads(str);
        }

        public /* bridge */ /* synthetic */ void setPriorityForThreads(Integer num) {
            super.setPriorityForThreads(num);
        }

        RecordingThreadPool(int i, int i2, long j, TimeUnit timeUnit, BlockingQueue<Runnable> blockingQueue) {
            this.executor = new ThreadPoolExecutor(i, i2, j, timeUnit, blockingQueue, new ThreadFactoryImpl(this)) {
                /* access modifiers changed from: protected */
                public void afterExecute(Runnable runnable, Throwable th) {
                    super.afterExecute(runnable, th);
                    Throwable retrieveUserException = ThreadPool.retrieveUserException(runnable, th);
                    if (retrieveUserException != null) {
                        RobotLog.m50ee(ThreadPool.TAG, retrieveUserException, "exception thrown in thread pool; ignored");
                    }
                }
            };
        }

        public void execute(Runnable runnable) {
            this.executor.execute(runnable);
        }

        public void shutdown() {
            this.executor.shutdown();
        }

        public List<Runnable> shutdownNow() {
            return this.executor.shutdownNow();
        }

        public boolean isShutdown() {
            return this.executor.isShutdown();
        }

        public boolean isTerminated() {
            return this.executor.isTerminated();
        }

        public boolean awaitTermination(long j, TimeUnit timeUnit) throws InterruptedException {
            return this.executor.awaitTermination(j, timeUnit);
        }

        public <T> Future<T> submit(Callable<T> callable) {
            return this.executor.submit(callable);
        }

        public <T> Future<T> submit(Runnable runnable, T t) {
            return this.executor.submit(runnable, t);
        }

        public Future<?> submit(Runnable runnable) {
            return this.executor.submit(runnable);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
            return this.executor.invokeAll(collection);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException {
            return this.executor.invokeAll(collection, j, timeUnit);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
            return this.executor.invokeAny(collection);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.executor.invokeAny(collection, j, timeUnit);
        }
    }

    public static class RecordingScheduledExecutor extends ContainerOfThreadsRecorder implements ScheduledExecutorService {
        protected ScheduledThreadPoolExecutor executor;

        public /* bridge */ /* synthetic */ Iterator iterator() {
            return super.iterator();
        }

        public /* bridge */ /* synthetic */ void noteFinishedThread(Thread thread) {
            super.noteFinishedThread(thread);
        }

        public /* bridge */ /* synthetic */ void noteNewThread(Thread thread) {
            super.noteNewThread(thread);
        }

        public /* bridge */ /* synthetic */ void setNameRootForThreads(String str) {
            super.setNameRootForThreads(str);
        }

        public /* bridge */ /* synthetic */ void setPriorityForThreads(Integer num) {
            super.setPriorityForThreads(num);
        }

        RecordingScheduledExecutor(int i) {
            C07621 r0 = new ScheduledThreadPoolExecutor(i, new ThreadFactoryImpl(this)) {
                /* access modifiers changed from: protected */
                public void afterExecute(Runnable runnable, Throwable th) {
                    super.afterExecute(runnable, th);
                    Throwable retrieveUserException = ThreadPool.retrieveUserException(runnable, th);
                    if (retrieveUserException != null) {
                        RobotLog.m50ee(ThreadPool.TAG, retrieveUserException, "exception thrown in thread pool; ignored");
                    }
                }
            };
            this.executor = r0;
            r0.setRemoveOnCancelPolicy(true);
        }

        public void setKeepAliveTime(long j, TimeUnit timeUnit) {
            this.executor.setKeepAliveTime(j, timeUnit);
        }

        public void allowCoreThreadTimeOut(boolean z) {
            this.executor.allowCoreThreadTimeOut(z);
        }

        public void execute(Runnable runnable) {
            this.executor.execute(runnable);
        }

        public void shutdown() {
            this.executor.shutdown();
        }

        public List<Runnable> shutdownNow() {
            return this.executor.shutdownNow();
        }

        public boolean isShutdown() {
            return this.executor.isShutdown();
        }

        public boolean isTerminated() {
            return this.executor.isTerminated();
        }

        public boolean awaitTermination(long j, TimeUnit timeUnit) throws InterruptedException {
            return this.executor.awaitTermination(j, timeUnit);
        }

        public <T> Future<T> submit(Callable<T> callable) {
            return this.executor.submit(callable);
        }

        public <T> Future<T> submit(Runnable runnable, T t) {
            return this.executor.submit(runnable, t);
        }

        public Future<?> submit(Runnable runnable) {
            return this.executor.submit(runnable);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection) throws InterruptedException {
            return this.executor.invokeAll(collection);
        }

        public <T> List<Future<T>> invokeAll(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException {
            return this.executor.invokeAll(collection, j, timeUnit);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> collection) throws InterruptedException, ExecutionException {
            return this.executor.invokeAny(collection);
        }

        public <T> T invokeAny(Collection<? extends Callable<T>> collection, long j, TimeUnit timeUnit) throws InterruptedException, ExecutionException, TimeoutException {
            return this.executor.invokeAny(collection, j, timeUnit);
        }

        public ScheduledFuture<?> schedule(Runnable runnable, long j, TimeUnit timeUnit) {
            return this.executor.schedule(runnable, j, timeUnit);
        }

        public <V> ScheduledFuture<V> schedule(Callable<V> callable, long j, TimeUnit timeUnit) {
            return this.executor.schedule(callable, j, timeUnit);
        }

        public ScheduledFuture<?> scheduleAtFixedRate(Runnable runnable, long j, long j2, TimeUnit timeUnit) {
            return this.executor.scheduleAtFixedRate(runnable, j, j2, timeUnit);
        }

        public ScheduledFuture<?> scheduleWithFixedDelay(Runnable runnable, long j, long j2, TimeUnit timeUnit) {
            return this.executor.scheduleWithFixedDelay(runnable, j, j2, timeUnit);
        }
    }
}
