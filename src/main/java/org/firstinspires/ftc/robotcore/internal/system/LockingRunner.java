package org.firstinspires.ftc.robotcore.internal.system;

import java.lang.ref.WeakReference;
import java.util.concurrent.Semaphore;
import org.firstinspires.ftc.robotcore.external.Supplier;
import org.firstinspires.ftc.robotcore.external.ThrowingCallable;

public final class LockingRunner {
    private static final int MAX_CONCURRENT_EXECUTIONS = 1;
    private WeakReference<Thread> lockingThreadReference = null;
    private final Semaphore semaphore = new Semaphore(1, true);

    private static class NeverThrown extends Exception {
        private NeverThrown() {
        }
    }

    public void lockWhile(final Runnable runnable) throws InterruptedException {
        lockWhile(new Supplier<Void>() {
            public Void get() {
                runnable.run();
                return null;
            }
        });
    }

    public <T> T lockWhile(final Supplier<T> supplier) throws InterruptedException {
        try {
            return lockWhile(new ThrowingCallable<T, NeverThrown>() {
                public T call() {
                    return supplier.get();
                }
            });
        } catch (NeverThrown e) {
            throw AppUtil.getInstance().unreachable((Throwable) e);
        }
    }

    public <T, E extends Throwable> T lockWhile(ThrowingCallable<T, E> throwingCallable) throws InterruptedException, Throwable {
        lock();
        try {
            return throwingCallable.call();
        } finally {
            unlock();
        }
    }

    private void lock() throws InterruptedException {
        WeakReference<Thread> weakReference = this.lockingThreadReference;
        if (weakReference == null || !((Thread) weakReference.get()).equals(Thread.currentThread())) {
            this.semaphore.acquire();
            this.lockingThreadReference = new WeakReference<>(Thread.currentThread());
            return;
        }
        throw new RuntimeException("The thread currently holding the lock tried to obtain the lock. This is invalid behavior, as LockingRunner does not (currently) support re-entrant locking, to preserve full compatibility with file-based locking.");
    }

    private void unlock() {
        this.lockingThreadReference = null;
        this.semaphore.release();
    }
}
