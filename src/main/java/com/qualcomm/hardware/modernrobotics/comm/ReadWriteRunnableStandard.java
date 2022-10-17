package com.qualcomm.hardware.modernrobotics.comm;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.hardware.modernrobotics.comm.ReadWriteRunnable;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.TypeConversion;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbDeviceClosedException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbProtocolException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbStuckUsbWriteException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbTimeoutException;

public class ReadWriteRunnableStandard implements ReadWriteRunnable {
    protected static boolean DEBUG_SEGMENTS = false;
    public static final String TAG = "ReadWriteRunnable";
    protected volatile boolean acceptingWrites = false;
    protected final Object acceptingWritesLock = new Object();
    protected byte[] activeBuffer;
    protected TimeWindow activeBufferTimeWindow;
    protected ReadWriteRunnable.Callback callback;
    protected final Context context;
    protected final boolean debugLogging;
    protected volatile boolean fullWriteNeeded;
    protected int ibActiveFirst;
    protected final byte[] localDeviceReadCache = new byte[256];
    protected final byte[] localDeviceWriteCache = new byte[256];
    protected int monitorLength;
    protected RobotUsbModule owner;
    protected boolean pruneBufferAfterRead;
    protected final Object readSupressionLock = new Object();
    protected RobotUsbDevice robotUsbDevice;
    protected volatile boolean running = false;
    protected CountDownLatch runningInterlock = new CountDownLatch(1);
    protected ConcurrentLinkedQueue<Integer> segmentReadQueue = new ConcurrentLinkedQueue<>();
    protected ConcurrentLinkedQueue<Integer> segmentWriteQueue = new ConcurrentLinkedQueue<>();
    protected Map<Integer, ReadWriteRunnableSegment> segments = new HashMap();
    protected final SerialNumber serialNumber;
    protected volatile boolean shutdownComplete = false;
    protected volatile SyncdDevice.ShutdownReason shutdownReason = SyncdDevice.ShutdownReason.NORMAL;
    protected int startAddress;
    protected volatile boolean suppressReads = false;
    protected ModernRoboticsReaderWriter usbHandler;
    private volatile boolean writeNeeded = false;

    public ReadWriteRunnableStandard(Context context2, SerialNumber serialNumber2, RobotUsbDevice robotUsbDevice2, int i, int i2, boolean z) {
        this.context = context2;
        this.serialNumber = serialNumber2;
        this.startAddress = i2;
        this.monitorLength = i;
        this.fullWriteNeeded = false;
        this.pruneBufferAfterRead = true;
        this.debugLogging = z;
        this.callback = new ReadWriteRunnable.EmptyCallback();
        this.owner = null;
        this.robotUsbDevice = robotUsbDevice2;
        this.usbHandler = new ModernRoboticsReaderWriter(robotUsbDevice2);
    }

    public void setCallback(ReadWriteRunnable.Callback callback2) {
        this.callback = callback2;
    }

    public void setOwner(RobotUsbModule robotUsbModule) {
        this.owner = robotUsbModule;
    }

    public RobotUsbModule getOwner() {
        return this.owner;
    }

    public boolean writeNeeded() {
        return this.writeNeeded;
    }

    public void resetWriteNeeded() {
        this.writeNeeded = false;
    }

    public void write(int i, byte[] bArr) {
        synchronized (this.acceptingWritesLock) {
            if (this.acceptingWrites) {
                synchronized (this.localDeviceWriteCache) {
                    System.arraycopy(bArr, 0, this.localDeviceWriteCache, i, bArr.length);
                    this.writeNeeded = true;
                    if (i < this.startAddress) {
                        this.fullWriteNeeded = true;
                    }
                }
            }
        }
    }

    public void suppressReads(boolean z) {
        synchronized (this.readSupressionLock) {
            this.suppressReads = z;
        }
    }

    public byte[] readFromWriteCache(int i, int i2) {
        byte[] copyOfRange;
        synchronized (this.localDeviceWriteCache) {
            copyOfRange = Arrays.copyOfRange(this.localDeviceWriteCache, i, i2 + i);
        }
        return copyOfRange;
    }

    public byte[] read(int i, int i2) {
        byte[] copyOfRange;
        synchronized (this.localDeviceReadCache) {
            copyOfRange = Arrays.copyOfRange(this.localDeviceReadCache, i, i2 + i);
        }
        return copyOfRange;
    }

    public void executeUsing(ExecutorService executorService) {
        synchronized (this) {
            executorService.execute(this);
            awaitRunning();
        }
    }

    public void close() {
        synchronized (this) {
            if (this.running) {
                this.robotUsbDevice.requestReadInterrupt(true);
                this.running = false;
                while (!this.shutdownComplete) {
                    Thread.yield();
                }
            }
        }
    }

    public ReadWriteRunnableSegment createSegment(int i, int i2, int i3) {
        ReadWriteRunnableSegment readWriteRunnableSegment = new ReadWriteRunnableSegment(i, i2, i3);
        this.segments.put(Integer.valueOf(i), readWriteRunnableSegment);
        return readWriteRunnableSegment;
    }

    public void destroySegment(int i) {
        this.segments.remove(Integer.valueOf(i));
    }

    public ReadWriteRunnableSegment getSegment(int i) {
        return this.segments.get(Integer.valueOf(i));
    }

    public void queueSegmentRead(int i) {
        queueIfNotAlreadyQueued(i, this.segmentReadQueue);
    }

    public void queueSegmentWrite(int i) {
        synchronized (this.acceptingWritesLock) {
            if (this.acceptingWrites) {
                queueIfNotAlreadyQueued(i, this.segmentWriteQueue);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void awaitRunning() {
        try {
            this.runningInterlock.await();
        } catch (InterruptedException unused) {
            while (this.runningInterlock.getCount() != 0) {
                Thread.yield();
            }
            Thread.currentThread().interrupt();
        }
    }

    /* access modifiers changed from: protected */
    public void setFullActive() {
        this.ibActiveFirst = 0;
        this.activeBuffer = new byte[(this.monitorLength + this.startAddress)];
        this.activeBufferTimeWindow = new TimeWindow();
    }

    /* access modifiers changed from: protected */
    public boolean isFullActive() {
        return this.ibActiveFirst == 0 && this.startAddress > 0;
    }

    /* access modifiers changed from: protected */
    public void setSuffixActive() {
        this.ibActiveFirst = this.startAddress;
        this.activeBuffer = new byte[this.monitorLength];
        this.activeBufferTimeWindow = new TimeWindow();
    }

    public void run() {
        if (!this.shutdownComplete) {
            ThreadPool.logThreadLifeCycle(String.format("r/w loop: %s", new Object[]{HardwareFactory.getDeviceDisplayName(this.context, this.serialNumber)}), new Runnable() {
                public void run() {
                    ReadWriteRunnable.Callback callback;
                    ReadWriteRunnableStandard.this.fullWriteNeeded = false;
                    ReadWriteRunnableStandard.this.pruneBufferAfterRead = true;
                    ReadWriteRunnableStandard.this.setFullActive();
                    ElapsedTime elapsedTime = new ElapsedTime();
                    String str = "Device " + ReadWriteRunnableStandard.this.serialNumber;
                    ReadWriteRunnableStandard.this.running = true;
                    try {
                        ReadWriteRunnableStandard.this.callback.startupComplete();
                    } catch (InterruptedException unused) {
                        ReadWriteRunnableStandard.this.running = false;
                    }
                    ReadWriteRunnableStandard.this.runningInterlock.countDown();
                    while (ReadWriteRunnableStandard.this.running) {
                        try {
                            if (ReadWriteRunnableStandard.this.debugLogging) {
                                elapsedTime.log(str);
                                elapsedTime.reset();
                            }
                            ReadWriteRunnableStandard.this.doReadCycle();
                            ReadWriteRunnableStandard.this.doWriteCycle();
                            ReadWriteRunnableStandard.this.usbHandler.throwIfTooManySequentialCommErrors();
                            if (!ReadWriteRunnableStandard.this.robotUsbDevice.isOpen()) {
                                throw new RobotUsbDeviceClosedException("%s: closed", ReadWriteRunnableStandard.this.robotUsbDevice.getSerialNumber());
                            }
                        } catch (InterruptedException e) {
                            RobotLog.logExceptionHeader(ReadWriteRunnableStandard.TAG, e, "thread interrupt while communicating with %s", HardwareFactory.getDeviceDisplayName(ReadWriteRunnableStandard.this.context, ReadWriteRunnableStandard.this.serialNumber));
                            ReadWriteRunnableStandard.this.usbHandler.close();
                            ReadWriteRunnableStandard.this.running = false;
                            callback = ReadWriteRunnableStandard.this.callback;
                        } catch (RuntimeException | RobotUsbException e2) {
                            if (e2.getClass() != RobotUsbDeviceClosedException.class) {
                                RobotLog.m51ee(ReadWriteRunnableStandard.TAG, e2, "exception while communicating with %s", HardwareFactory.getDeviceDisplayName(ReadWriteRunnableStandard.this.context, ReadWriteRunnableStandard.this.serialNumber));
                            }
                            String string = ReadWriteRunnableStandard.this.context.getString(ReadWriteRunnableStandard.this.robotUsbDevice.isAttached() ? C0660R.string.warningProblemCommunicatingWithUSBDevice : C0660R.string.warningUSBDeviceDetached);
                            ReadWriteRunnableStandard readWriteRunnableStandard = ReadWriteRunnableStandard.this;
                            readWriteRunnableStandard.setOwnerWarningMessage(string, HardwareFactory.getDeviceDisplayName(readWriteRunnableStandard.context, ReadWriteRunnableStandard.this.serialNumber));
                            if (e2.getClass() != RobotUsbTimeoutException.class) {
                                if (e2.getClass() != RobotUsbStuckUsbWriteException.class) {
                                    ReadWriteRunnableStandard.this.shutdownReason = SyncdDevice.ShutdownReason.ABNORMAL;
                                    ReadWriteRunnableStandard.this.usbHandler.close();
                                    ReadWriteRunnableStandard.this.running = false;
                                    callback = ReadWriteRunnableStandard.this.callback;
                                }
                            }
                            ReadWriteRunnableStandard.this.shutdownReason = SyncdDevice.ShutdownReason.ABNORMAL_ATTEMPT_REOPEN;
                            ReadWriteRunnableStandard.this.usbHandler.close();
                            ReadWriteRunnableStandard.this.running = false;
                            callback = ReadWriteRunnableStandard.this.callback;
                        } catch (Throwable th) {
                            ReadWriteRunnableStandard.this.usbHandler.close();
                            ReadWriteRunnableStandard.this.running = false;
                            try {
                                ReadWriteRunnableStandard.this.callback.shutdownComplete();
                            } catch (InterruptedException unused2) {
                            }
                            ReadWriteRunnableStandard.this.shutdownComplete = true;
                            throw th;
                        }
                    }
                    ReadWriteRunnableStandard.this.usbHandler.close();
                    ReadWriteRunnableStandard.this.running = false;
                    try {
                        callback = ReadWriteRunnableStandard.this.callback;
                        callback.shutdownComplete();
                    } catch (InterruptedException unused3) {
                    }
                    ReadWriteRunnableStandard.this.shutdownComplete = true;
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void doReadCycle() throws InterruptedException, RobotUsbException {
        ReadWriteRunnableSegment readWriteRunnableSegment;
        synchronized (this.readSupressionLock) {
            if (!this.suppressReads) {
                try {
                    this.usbHandler.read(!isFullActive(), this.ibActiveFirst, this.activeBuffer, this.activeBufferTimeWindow);
                    while (!this.segmentReadQueue.isEmpty()) {
                        readWriteRunnableSegment = this.segments.get(this.segmentReadQueue.remove());
                        byte[] bArr = new byte[readWriteRunnableSegment.getReadBuffer().length];
                        this.usbHandler.read(readWriteRunnableSegment.getRetryOnReadFailure(), readWriteRunnableSegment.getAddress(), bArr, readWriteRunnableSegment.getTimeWindow());
                        readWriteRunnableSegment.getReadLock().lock();
                        System.arraycopy(bArr, 0, readWriteRunnableSegment.getReadBuffer(), 0, readWriteRunnableSegment.getReadBuffer().length);
                        if (DEBUG_SEGMENTS) {
                            dumpBuffers("segment " + readWriteRunnableSegment.getAddress() + " read", bArr);
                        }
                        readWriteRunnableSegment.getReadLock().unlock();
                    }
                } catch (RobotUsbProtocolException e) {
                    RobotLog.m64w(String.format("could not read %s: %s", new Object[]{HardwareFactory.getDeviceDisplayName(this.context, this.serialNumber), e.getMessage()}));
                } catch (Throwable th) {
                    readWriteRunnableSegment.getReadLock().unlock();
                    throw th;
                }
                synchronized (this.localDeviceReadCache) {
                    byte[] bArr2 = this.activeBuffer;
                    System.arraycopy(bArr2, 0, this.localDeviceReadCache, this.ibActiveFirst, bArr2.length);
                }
            }
        }
        if (this.debugLogging) {
            dumpBuffers("read", this.localDeviceReadCache);
        }
        this.callback.readComplete();
    }

    /* access modifiers changed from: protected */
    public void doWriteCycle() throws InterruptedException, RobotUsbException {
        ReadWriteRunnableSegment readWriteRunnableSegment;
        synchronized (this.localDeviceWriteCache) {
            if (this.fullWriteNeeded) {
                setFullActive();
                this.fullWriteNeeded = false;
                this.pruneBufferAfterRead = true;
            } else if (this.pruneBufferAfterRead) {
                setSuffixActive();
                this.pruneBufferAfterRead = false;
            }
            byte[] bArr = this.localDeviceWriteCache;
            int i = this.ibActiveFirst;
            byte[] bArr2 = this.activeBuffer;
            System.arraycopy(bArr, i, bArr2, 0, bArr2.length);
        }
        try {
            if (writeNeeded()) {
                this.usbHandler.write(this.ibActiveFirst, this.activeBuffer);
                resetWriteNeeded();
            }
            while (!this.segmentWriteQueue.isEmpty()) {
                readWriteRunnableSegment = this.segments.get(this.segmentWriteQueue.remove());
                readWriteRunnableSegment.getWriteLock().lock();
                byte[] copyOf = Arrays.copyOf(readWriteRunnableSegment.getWriteBuffer(), readWriteRunnableSegment.getWriteBuffer().length);
                readWriteRunnableSegment.getWriteLock().unlock();
                this.usbHandler.write(readWriteRunnableSegment.getAddress(), copyOf);
            }
        } catch (RobotUsbProtocolException e) {
            RobotLog.m64w(String.format("could not write to %s: %s", new Object[]{HardwareFactory.getDeviceDisplayName(this.context, this.serialNumber), e.getMessage()}));
        } catch (Throwable th) {
            readWriteRunnableSegment.getWriteLock().unlock();
            throw th;
        }
        if (this.debugLogging) {
            dumpBuffers("write", this.localDeviceWriteCache);
        }
        this.callback.writeComplete();
    }

    /* access modifiers changed from: package-private */
    public void setOwnerWarningMessage(String str, Object... objArr) {
        String format = String.format(str, objArr);
        RobotUsbModule robotUsbModule = this.owner;
        if (robotUsbModule == null || !(robotUsbModule instanceof GlobalWarningSource)) {
            RobotLog.addGlobalWarningMessage(format);
        } else {
            ((GlobalWarningSource) robotUsbModule).setGlobalWarning(format);
        }
    }

    public SyncdDevice.ShutdownReason getShutdownReason() {
        return this.shutdownReason;
    }

    /* access modifiers changed from: package-private */
    public boolean hasPendingWrites() {
        return this.writeNeeded || !this.segmentWriteQueue.isEmpty();
    }

    public void drainPendingWrites() {
        while (this.running && hasPendingWrites()) {
            Thread.yield();
        }
    }

    public void setAcceptingWrites(boolean z) {
        synchronized (this.acceptingWritesLock) {
            this.acceptingWrites = z;
        }
    }

    public boolean getAcceptingWrites() {
        return this.acceptingWrites;
    }

    /* access modifiers changed from: protected */
    public void dumpBuffers(String str, byte[] bArr) {
        RobotLog.m58v("Dumping " + str + " buffers for " + this.serialNumber);
        StringBuilder sb = new StringBuilder(1024);
        int i = 0;
        while (i < this.startAddress + this.monitorLength) {
            sb.append(String.format(" %02x", new Object[]{Integer.valueOf(TypeConversion.unsignedByteToInt(bArr[i]))}));
            i++;
            if (i % 16 == 0) {
                sb.append("\n");
            }
        }
        RobotLog.m58v(sb.toString());
    }

    /* access modifiers changed from: protected */
    public void queueIfNotAlreadyQueued(int i, ConcurrentLinkedQueue<Integer> concurrentLinkedQueue) {
        if (!concurrentLinkedQueue.contains(Integer.valueOf(i))) {
            concurrentLinkedQueue.add(Integer.valueOf(i));
        }
    }
}
