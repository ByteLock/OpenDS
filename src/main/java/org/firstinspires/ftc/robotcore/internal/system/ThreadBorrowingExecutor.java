package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.Executor;

public class ThreadBorrowingExecutor implements Executor, ThreadPool.ThreadBorrowable {
    protected final Executor delegate;

    public boolean canBorrowThread(Thread thread) {
        return true;
    }

    public ThreadBorrowingExecutor(Executor executor) {
        this.delegate = executor;
    }

    public void execute(Runnable runnable) {
        this.delegate.execute(runnable);
    }
}
