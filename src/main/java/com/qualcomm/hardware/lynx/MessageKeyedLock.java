package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.hardware.lynx.commands.standard.LynxKeepAliveCommand;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class MessageKeyedLock {
    private final Lock acquisitionsLock;
    private final Condition condition;
    private final Lock lock;
    private long lockAquisitionTime;
    private int lockCount;
    private volatile LynxMessage lockOwner;
    private final String name;
    private long nanoLockAquisitionTimeMax;
    private volatile boolean throwOnAcquisitionAttempt;
    private volatile boolean tryingToHangAcquisitions;

    public MessageKeyedLock(String str) {
        this(str, 500);
    }

    public MessageKeyedLock(String str, int i) {
        this.acquisitionsLock = new ReentrantLock(true);
        this.tryingToHangAcquisitions = false;
        ReentrantLock reentrantLock = new ReentrantLock();
        this.lock = reentrantLock;
        this.name = str;
        this.condition = reentrantLock.newCondition();
        this.lockOwner = null;
        this.lockCount = 0;
        this.lockAquisitionTime = 0;
        this.nanoLockAquisitionTimeMax = ((long) i) * ElapsedTime.MILLIS_IN_NANO;
    }

    private void logv(String str, Object... objArr) {
        RobotLog.m59v("%s: %s", this.name, String.format(str, objArr));
    }

    private void loge(String str, Object... objArr) {
        RobotLog.m47e("%s: %s", this.name, String.format(str, objArr));
    }

    public void reset() throws InterruptedException {
        this.lock.lockInterruptibly();
        try {
            this.lockOwner = null;
            this.lockCount = 0;
            this.lockAquisitionTime = 0;
            this.condition.signalAll();
        } finally {
            this.lock.unlock();
        }
    }

    public void acquire(LynxMessage lynxMessage) throws InterruptedException {
        if (lynxMessage == null) {
            throw new IllegalArgumentException("MessageKeyedLock.acquire: null message");
        } else if (!this.throwOnAcquisitionAttempt || (lynxMessage instanceof LynxKeepAliveCommand)) {
            if (this.tryingToHangAcquisitions) {
                this.acquisitionsLock.lock();
            } else {
                this.acquisitionsLock.lockInterruptibly();
            }
            try {
                this.lock.lockInterruptibly();
                try {
                    if (this.lockOwner != lynxMessage) {
                        while (true) {
                            if (this.lockOwner == null) {
                                break;
                            }
                            long nanoTime = System.nanoTime() - this.lockAquisitionTime;
                            long j = this.nanoLockAquisitionTimeMax;
                            if (nanoTime > j) {
                                loge("#### abandoning lock: old=%s(%d)", this.lockOwner.getClass().getSimpleName(), Integer.valueOf(this.lockOwner.getMessageNumber()));
                                loge("                      new=%s(%d)", lynxMessage.getClass().getSimpleName(), Integer.valueOf(lynxMessage.getMessageNumber()));
                                break;
                            }
                            this.condition.await(j / 4, TimeUnit.NANOSECONDS);
                        }
                        this.lockCount = 0;
                        this.lockAquisitionTime = System.nanoTime();
                        this.lockOwner = lynxMessage;
                        if (LynxUsbDeviceImpl.DEBUG_LOG_DATAGRAMS_LOCK) {
                            logv("lock %s msg#=%d", this.lockOwner.getClass().getSimpleName(), Integer.valueOf(this.lockOwner.getMessageNumber()));
                        }
                    } else {
                        logv("lock recursively acquired", new Object[0]);
                    }
                    this.lockCount++;
                } finally {
                    this.lock.unlock();
                    this.acquisitionsLock.unlock();
                }
            } catch (Exception e) {
                this.acquisitionsLock.unlock();
                throw e;
            }
        } else {
            throw new OpModeManagerImpl.ForceStopException();
        }
    }

    public void release(LynxMessage lynxMessage) throws InterruptedException {
        if (lynxMessage != null) {
            this.lock.lockInterruptibly();
            try {
                if (this.lockOwner == lynxMessage) {
                    int i = this.lockCount - 1;
                    this.lockCount = i;
                    if (i == 0) {
                        if (LynxUsbDeviceImpl.DEBUG_LOG_DATAGRAMS_LOCK) {
                            logv("unlock %s msg#=%d", this.lockOwner.getClass().getSimpleName(), Integer.valueOf(this.lockOwner.getMessageNumber()));
                        }
                        this.lockOwner = null;
                        this.condition.signalAll();
                    } else {
                        logv("lock recursively released", new Object[0]);
                    }
                } else if (this.lockOwner != null) {
                    loge("#### incorrect owner releasing message keyed lock: ignored: old=%s(%d:%d)", this.lockOwner.getClass().getSimpleName(), Integer.valueOf(this.lockOwner.getModuleAddress()), Integer.valueOf(this.lockOwner.getMessageNumber()));
                    loge("                                                            new=%s(%d:%d)", lynxMessage.getClass().getSimpleName(), Integer.valueOf(lynxMessage.getModuleAddress()), Integer.valueOf(lynxMessage.getMessageNumber()));
                } else {
                    loge("#### releasing ownerless message keyed lock: ignored: %s", lynxMessage.getClass().getSimpleName());
                }
            } finally {
                this.lock.unlock();
            }
        } else {
            throw new IllegalArgumentException("MessageKeyedLock.release: null message");
        }
    }

    public void lockAcquisitions() {
        if (LynxUsbDeviceImpl.DEBUG_LOG_DATAGRAMS_LOCK) {
            logv("***ALL FUTURE ACQUISITION ATTEMPTS FROM THREADS OTHER THAN %s WILL NOW HANG!***", Thread.currentThread().getName());
        }
        this.acquisitionsLock.lock();
        this.tryingToHangAcquisitions = true;
    }

    public void throwOnLockAcquisitions(boolean z) {
        this.throwOnAcquisitionAttempt = z;
    }
}
