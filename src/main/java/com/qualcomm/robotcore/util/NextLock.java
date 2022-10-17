package com.qualcomm.robotcore.util;

public class NextLock {
    protected long count = 0;
    protected final Object lock = this;

    public class Waiter {
        long nextCount;

        Waiter(long j) {
            this.nextCount = j;
        }

        public void awaitNext() throws InterruptedException {
            synchronized (NextLock.this.lock) {
                do {
                    NextLock.this.lock.wait();
                } while (NextLock.this.count < this.nextCount);
            }
        }
    }

    public Waiter getNextWaiter() {
        Waiter waiter;
        synchronized (this.lock) {
            waiter = new Waiter(this.count + 1);
        }
        return waiter;
    }

    public void advanceNext() {
        synchronized (this.lock) {
            this.count++;
            this.lock.notifyAll();
        }
    }
}
