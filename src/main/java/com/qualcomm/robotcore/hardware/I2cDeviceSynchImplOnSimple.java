package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.Arrays;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class I2cDeviceSynchImplOnSimple extends I2cDeviceSynchReadHistoryImpl implements I2cDeviceSynch {
    protected final Object concurrentClientLock = new Object();
    protected int cregReadLast;
    protected final Object engagementLock = new Object();
    protected I2cDeviceSynch.HeartbeatAction heartbeatAction;
    protected ScheduledExecutorService heartbeatExecutor;
    protected I2cDeviceSynchSimple i2cDeviceSynchSimple;
    protected I2cDeviceSynchReadHistory i2cDeviceSynchSimpleHistory;
    protected int iregReadLast;
    protected int iregWriteLast;
    protected boolean isClosing;
    protected boolean isEngaged;
    protected boolean isHooked;
    protected boolean isSimpleOwned;
    protected int msHeartbeatInterval;
    protected byte[] rgbWriteLast;

    public void ensureReadWindow(I2cDeviceSynch.ReadWindow readWindow, I2cDeviceSynch.ReadWindow readWindow2) {
    }

    public I2cDeviceSynch.ReadWindow getReadWindow() {
        return null;
    }

    public void setReadWindow(I2cDeviceSynch.ReadWindow readWindow) {
    }

    public I2cDeviceSynchImplOnSimple(I2cDeviceSynchSimple i2cDeviceSynchSimple2, boolean z) {
        this.i2cDeviceSynchSimple = i2cDeviceSynchSimple2;
        this.i2cDeviceSynchSimpleHistory = i2cDeviceSynchSimple2 instanceof I2cDeviceSynchReadHistory ? (I2cDeviceSynchReadHistory) i2cDeviceSynchSimple2 : null;
        this.isSimpleOwned = z;
        this.msHeartbeatInterval = 0;
        this.heartbeatAction = null;
        this.heartbeatExecutor = null;
        this.cregReadLast = 0;
        this.rgbWriteLast = null;
        this.isEngaged = false;
        this.isHooked = false;
        this.isClosing = false;
    }

    public void setUserConfiguredName(String str) {
        this.i2cDeviceSynchSimple.setUserConfiguredName(str);
    }

    public String getUserConfiguredName() {
        return this.i2cDeviceSynchSimple.getUserConfiguredName();
    }

    public void setLogging(boolean z) {
        this.i2cDeviceSynchSimple.setLogging(z);
    }

    public boolean getLogging() {
        return this.i2cDeviceSynchSimple.getLogging();
    }

    public void setLoggingTag(String str) {
        this.i2cDeviceSynchSimple.setLoggingTag(str);
    }

    public String getLoggingTag() {
        return this.i2cDeviceSynchSimple.getLoggingTag();
    }

    public void engage() {
        synchronized (this.engagementLock) {
            this.isEngaged = true;
            adjustHooking();
        }
    }

    /* access modifiers changed from: protected */
    public void hook() {
        synchronized (this.engagementLock) {
            if (!this.isHooked) {
                startHeartBeat();
                this.isHooked = true;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void adjustHooking() {
        synchronized (this.engagementLock) {
            boolean z = this.isHooked;
            if (!z && this.isEngaged) {
                hook();
            } else if (z && !this.isEngaged) {
                unhook();
            }
        }
    }

    public boolean isEngaged() {
        return this.isEngaged;
    }

    public boolean isArmed() {
        synchronized (this.engagementLock) {
            if (!this.isHooked) {
                return false;
            }
            boolean isArmed = this.i2cDeviceSynchSimple.isArmed();
            return isArmed;
        }
    }

    public void disengage() {
        synchronized (this.engagementLock) {
            this.isEngaged = false;
            adjustHooking();
        }
    }

    /* access modifiers changed from: protected */
    public void unhook() {
        synchronized (this.engagementLock) {
            if (this.isHooked) {
                stopHeartBeat();
                synchronized (this.concurrentClientLock) {
                    waitForWriteCompletions(I2cWaitControl.ATOMIC);
                    this.isHooked = false;
                }
            }
        }
    }

    public void setHeartbeatInterval(int i) {
        synchronized (this.concurrentClientLock) {
            this.msHeartbeatInterval = Math.max(0, this.msHeartbeatInterval);
            stopHeartBeat();
            startHeartBeat();
        }
    }

    public int getHeartbeatInterval() {
        return this.msHeartbeatInterval;
    }

    public void setHeartbeatAction(I2cDeviceSynch.HeartbeatAction heartbeatAction2) {
        this.heartbeatAction = heartbeatAction2;
    }

    public I2cDeviceSynch.HeartbeatAction getHeartbeatAction() {
        return this.heartbeatAction;
    }

    /* access modifiers changed from: package-private */
    public void startHeartBeat() {
        if (this.msHeartbeatInterval > 0) {
            ThreadPool.RecordingScheduledExecutor newScheduledExecutor = ThreadPool.newScheduledExecutor(1, "I2cDeviceSyncImplOnSimple heartbeat");
            this.heartbeatExecutor = newScheduledExecutor;
            newScheduledExecutor.scheduleAtFixedRate(new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:28:?, code lost:
                    return;
                 */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                        r4 = this;
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r0 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this
                        com.qualcomm.robotcore.hardware.I2cDeviceSynch$HeartbeatAction r0 = r0.getHeartbeatAction()
                        if (r0 == 0) goto L_0x0055
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r1 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this
                        java.lang.Object r1 = r1.concurrentClientLock
                        monitor-enter(r1)
                        boolean r2 = r0.rereadLastRead     // Catch:{ all -> 0x0052 }
                        if (r2 == 0) goto L_0x0024
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r2 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        int r2 = r2.cregReadLast     // Catch:{ all -> 0x0052 }
                        if (r2 == 0) goto L_0x0024
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r0 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        int r2 = r0.iregReadLast     // Catch:{ all -> 0x0052 }
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r3 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        int r3 = r3.cregReadLast     // Catch:{ all -> 0x0052 }
                        r0.read(r2, r3)     // Catch:{ all -> 0x0052 }
                        monitor-exit(r1)     // Catch:{ all -> 0x0052 }
                        return
                    L_0x0024:
                        boolean r2 = r0.rewriteLastWritten     // Catch:{ all -> 0x0052 }
                        if (r2 == 0) goto L_0x003b
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r2 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        byte[] r2 = r2.rgbWriteLast     // Catch:{ all -> 0x0052 }
                        if (r2 == 0) goto L_0x003b
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r0 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        int r2 = r0.iregWriteLast     // Catch:{ all -> 0x0052 }
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r3 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        byte[] r3 = r3.rgbWriteLast     // Catch:{ all -> 0x0052 }
                        r0.write((int) r2, (byte[]) r3)     // Catch:{ all -> 0x0052 }
                        monitor-exit(r1)     // Catch:{ all -> 0x0052 }
                        return
                    L_0x003b:
                        com.qualcomm.robotcore.hardware.I2cDeviceSynch$ReadWindow r2 = r0.heartbeatReadWindow     // Catch:{ all -> 0x0052 }
                        if (r2 == 0) goto L_0x0050
                        com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple r2 = com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.this     // Catch:{ all -> 0x0052 }
                        com.qualcomm.robotcore.hardware.I2cDeviceSynch$ReadWindow r3 = r0.heartbeatReadWindow     // Catch:{ all -> 0x0052 }
                        int r3 = r3.getRegisterFirst()     // Catch:{ all -> 0x0052 }
                        com.qualcomm.robotcore.hardware.I2cDeviceSynch$ReadWindow r0 = r0.heartbeatReadWindow     // Catch:{ all -> 0x0052 }
                        int r0 = r0.getRegisterCount()     // Catch:{ all -> 0x0052 }
                        r2.read(r3, r0)     // Catch:{ all -> 0x0052 }
                    L_0x0050:
                        monitor-exit(r1)     // Catch:{ all -> 0x0052 }
                        goto L_0x0055
                    L_0x0052:
                        r0 = move-exception
                        monitor-exit(r1)     // Catch:{ all -> 0x0052 }
                        throw r0
                    L_0x0055:
                        return
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple.C07241.run():void");
                }
            }, 0, (long) this.msHeartbeatInterval, TimeUnit.MILLISECONDS);
        }
    }

    /* access modifiers changed from: package-private */
    public void stopHeartBeat() {
        ScheduledExecutorService scheduledExecutorService = this.heartbeatExecutor;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdownNow();
            ThreadPool.awaitTerminationOrExitApplication(this.heartbeatExecutor, 2, TimeUnit.SECONDS, "heartbeat executor", "internal error");
            this.heartbeatExecutor = null;
        }
    }

    public TimestampedData readTimeStamped(int i, int i2, I2cDeviceSynch.ReadWindow readWindow, I2cDeviceSynch.ReadWindow readWindow2) {
        return readTimeStamped(i, i2);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.i2cDeviceSynchSimple.getManufacturer();
    }

    public String getDeviceName() {
        return this.i2cDeviceSynchSimple.getDeviceName();
    }

    public String getConnectionInfo() {
        return this.i2cDeviceSynchSimple.getConnectionInfo();
    }

    public int getVersion() {
        return this.i2cDeviceSynchSimple.getVersion();
    }

    public void resetDeviceConfigurationForOpMode() {
        this.i2cDeviceSynchSimple.resetDeviceConfigurationForOpMode();
    }

    public void close() {
        this.isClosing = true;
        disengage();
        if (this.isSimpleOwned) {
            this.i2cDeviceSynchSimple.close();
        }
    }

    public void setHealthStatus(HardwareDeviceHealth.HealthStatus healthStatus) {
        this.i2cDeviceSynchSimple.setHealthStatus(healthStatus);
    }

    public HardwareDeviceHealth.HealthStatus getHealthStatus() {
        return this.i2cDeviceSynchSimple.getHealthStatus();
    }

    public BlockingQueue<TimestampedI2cData> getHistoryQueue() {
        I2cDeviceSynchReadHistory i2cDeviceSynchReadHistory = this.i2cDeviceSynchSimpleHistory;
        if (i2cDeviceSynchReadHistory == null) {
            return super.getHistoryQueue();
        }
        return i2cDeviceSynchReadHistory.getHistoryQueue();
    }

    public void setHistoryQueueCapacity(int i) {
        I2cDeviceSynchReadHistory i2cDeviceSynchReadHistory = this.i2cDeviceSynchSimpleHistory;
        if (i2cDeviceSynchReadHistory == null) {
            super.setHistoryQueueCapacity(i);
        } else {
            i2cDeviceSynchReadHistory.setHistoryQueueCapacity(i);
        }
    }

    public int getHistoryQueueCapacity() {
        I2cDeviceSynchReadHistory i2cDeviceSynchReadHistory = this.i2cDeviceSynchSimpleHistory;
        if (i2cDeviceSynchReadHistory == null) {
            return super.getHistoryQueueCapacity();
        }
        return i2cDeviceSynchReadHistory.getHistoryQueueCapacity();
    }

    public void addToHistoryQueue(TimestampedI2cData timestampedI2cData) {
        if (this.i2cDeviceSynchSimpleHistory == null) {
            super.addToHistoryQueue(timestampedI2cData);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isOpenForReading() {
        return this.isHooked && newReadsAndWritesAllowed();
    }

    /* access modifiers changed from: protected */
    public boolean isOpenForWriting() {
        return this.isHooked && newReadsAndWritesAllowed();
    }

    /* access modifiers changed from: protected */
    public boolean newReadsAndWritesAllowed() {
        return !this.isClosing;
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        setI2cAddr(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return getI2cAddr();
    }

    public void setI2cAddr(I2cAddr i2cAddr) {
        this.i2cDeviceSynchSimple.setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddr() {
        return this.i2cDeviceSynchSimple.getI2cAddress();
    }

    public byte read8() {
        return read(1)[0];
    }

    public synchronized byte read8(int i) {
        return read(i, 1)[0];
    }

    public byte[] read(int i) {
        return readTimeStamped(i).data;
    }

    public byte[] read(int i, int i2) {
        return readTimeStamped(i, i2).data;
    }

    public TimestampedData readTimeStamped(int i) {
        synchronized (this.concurrentClientLock) {
            if (!isOpenForReading()) {
                TimestampedI2cData makeFakeData = TimestampedI2cData.makeFakeData(getI2cAddress(), 0, i);
                return makeFakeData;
            }
            this.cregReadLast = i;
            TimestampedI2cData timestampedI2cData = new TimestampedI2cData();
            timestampedI2cData.i2cAddr = getI2cAddress();
            TimestampedData readTimeStamped = this.i2cDeviceSynchSimple.readTimeStamped(i);
            timestampedI2cData.data = readTimeStamped.data;
            timestampedI2cData.nanoTime = readTimeStamped.nanoTime;
            addToHistoryQueue(timestampedI2cData);
            return timestampedI2cData;
        }
    }

    public TimestampedData readTimeStamped(int i, int i2) {
        synchronized (this.concurrentClientLock) {
            if (!isOpenForReading()) {
                TimestampedI2cData makeFakeData = TimestampedI2cData.makeFakeData(getI2cAddress(), i, i2);
                return makeFakeData;
            }
            this.iregReadLast = i;
            this.cregReadLast = i2;
            TimestampedI2cData timestampedI2cData = new TimestampedI2cData();
            timestampedI2cData.i2cAddr = getI2cAddress();
            timestampedI2cData.register = i;
            TimestampedData readTimeStamped = this.i2cDeviceSynchSimple.readTimeStamped(i, i2);
            timestampedI2cData.data = readTimeStamped.data;
            timestampedI2cData.nanoTime = readTimeStamped.nanoTime;
            addToHistoryQueue(timestampedI2cData);
            return timestampedI2cData;
        }
    }

    public void write8(int i) {
        write8(i, I2cWaitControl.ATOMIC);
    }

    public void write8(int i, int i2) {
        write8(i, i2, I2cWaitControl.ATOMIC);
    }

    public void write8(int i, int i2, I2cWaitControl i2cWaitControl) {
        write(i, new byte[]{(byte) i2}, i2cWaitControl);
    }

    public void write8(int i, I2cWaitControl i2cWaitControl) {
        write(new byte[]{(byte) i}, i2cWaitControl);
    }

    public void write(int i, byte[] bArr) {
        write(i, bArr, I2cWaitControl.ATOMIC);
    }

    public void write(byte[] bArr) {
        write(bArr, I2cWaitControl.ATOMIC);
    }

    public void write(int i, byte[] bArr, I2cWaitControl i2cWaitControl) {
        synchronized (this.concurrentClientLock) {
            if (isOpenForWriting()) {
                this.iregWriteLast = i;
                this.rgbWriteLast = Arrays.copyOf(bArr, bArr.length);
                this.i2cDeviceSynchSimple.write(i, bArr, i2cWaitControl);
            }
        }
    }

    public void write(byte[] bArr, I2cWaitControl i2cWaitControl) {
        synchronized (this.concurrentClientLock) {
            if (isOpenForWriting()) {
                this.rgbWriteLast = Arrays.copyOf(bArr, bArr.length);
                this.i2cDeviceSynchSimple.write(bArr, i2cWaitControl);
            }
        }
    }

    public void waitForWriteCompletions(I2cWaitControl i2cWaitControl) {
        this.i2cDeviceSynchSimple.waitForWriteCompletions(i2cWaitControl);
    }

    public void enableWriteCoalescing(boolean z) {
        this.i2cDeviceSynchSimple.enableWriteCoalescing(z);
    }

    public boolean isWriteCoalescingEnabled() {
        return this.i2cDeviceSynchSimple.isWriteCoalescingEnabled();
    }
}
