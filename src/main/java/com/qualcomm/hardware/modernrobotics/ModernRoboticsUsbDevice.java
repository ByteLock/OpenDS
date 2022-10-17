package com.qualcomm.hardware.modernrobotics;

import android.content.Context;
import com.qualcomm.hardware.modernrobotics.comm.ReadWriteRunnable;
import com.qualcomm.hardware.modernrobotics.comm.RobotUsbDevicePretendModernRobotics;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.hardware.usb.ArmableUsbDevice;
import org.firstinspires.inspection.InspectionState;

public abstract class ModernRoboticsUsbDevice extends ArmableUsbDevice implements ReadWriteRunnable.Callback {
    protected final CreateReadWriteRunnable createReadWriteRunnable;
    protected volatile ReadWriteRunnable readWriteRunnable;
    protected ExecutorService readWriteService = null;

    public interface CreateReadWriteRunnable {
        ReadWriteRunnable create(RobotUsbDevice robotUsbDevice) throws RobotCoreException, InterruptedException;
    }

    public abstract String getDeviceName();

    public void initializeHardware() {
    }

    public void readComplete() throws InterruptedException {
    }

    public void shutdownComplete() throws InterruptedException {
    }

    public void startupComplete() throws InterruptedException {
    }

    public void writeComplete() throws InterruptedException {
    }

    public ModernRoboticsUsbDevice(Context context, SerialNumber serialNumber, SyncdDevice.Manager manager, ArmableUsbDevice.OpenRobotUsbDevice openRobotUsbDevice, CreateReadWriteRunnable createReadWriteRunnable2) throws RobotCoreException, InterruptedException {
        super(context, serialNumber, manager, openRobotUsbDevice);
        this.createReadWriteRunnable = createReadWriteRunnable2;
        finishConstruction();
    }

    /* access modifiers changed from: protected */
    public RobotUsbDevice getPretendDevice(SerialNumber serialNumber) {
        return new RobotUsbDevicePretendModernRobotics(serialNumber);
    }

    /* access modifiers changed from: protected */
    public void armDevice(RobotUsbDevice robotUsbDevice) throws RobotCoreException, InterruptedException {
        synchronized (this.armingLock) {
            this.robotUsbDevice = robotUsbDevice;
            this.readWriteRunnable = this.createReadWriteRunnable.create(robotUsbDevice);
            if (this.readWriteRunnable != null) {
                Object[] objArr = new Object[2];
                objArr[0] = this.armingState == RobotArmingStateNotifier.ARMINGSTATE.TO_PRETENDING ? "pretend " : InspectionState.NO_VERSION;
                objArr[1] = this.serialNumber;
                RobotLog.m59v("Starting up %sdevice %s", objArr);
                this.readWriteRunnable.setOwner(this);
                this.readWriteRunnable.setCallback(this);
                this.readWriteService = ThreadPool.newSingleThreadExecutor("readWriteService");
                this.readWriteRunnable.executeUsing(this.readWriteService);
                this.syncdDeviceManager.registerSyncdDevice(this.readWriteRunnable);
                this.readWriteRunnable.setAcceptingWrites(true);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void disarmDevice() throws InterruptedException {
        synchronized (this.armingLock) {
            ExecutorService executorService = this.readWriteService;
            if (executorService != null) {
                executorService.shutdown();
            }
            if (this.readWriteRunnable != null) {
                this.readWriteRunnable.setAcceptingWrites(false);
                this.readWriteRunnable.drainPendingWrites();
                this.syncdDeviceManager.unregisterSyncdDevice(this.readWriteRunnable);
                this.readWriteRunnable.close();
                this.readWriteRunnable = null;
            }
            ExecutorService executorService2 = this.readWriteService;
            if (executorService2 != null) {
                ThreadPool.awaitTerminationOrExitApplication(executorService2, 30, TimeUnit.SECONDS, "ReadWriteRunnable for Modern Robotics USB Device", "internal error");
                this.readWriteService = null;
            }
            if (this.robotUsbDevice != null) {
                this.robotUsbDevice.close();
                this.robotUsbDevice = null;
            }
        }
    }

    public ReadWriteRunnable getReadWriteRunnable() {
        return this.readWriteRunnable;
    }

    public ArmableUsbDevice.OpenRobotUsbDevice getOpenRobotUsbDevice() {
        return this.openRobotUsbDevice;
    }

    public CreateReadWriteRunnable getCreateReadWriteRunnable() {
        return this.createReadWriteRunnable;
    }

    public void write8(int i, byte b) {
        write(i, new byte[]{b});
    }

    public void write8(int i, int i2) {
        write(i, new byte[]{(byte) i2});
    }

    public void write8(int i, double d) {
        write(i, new byte[]{(byte) ((int) d)});
    }

    public void write(int i, byte[] bArr) {
        ReadWriteRunnable readWriteRunnable2 = this.readWriteRunnable;
        if (readWriteRunnable2 != null) {
            readWriteRunnable2.write(i, bArr);
        }
    }

    public byte readFromWriteCache(int i) {
        return readFromWriteCache(i, 1)[0];
    }

    public byte[] readFromWriteCache(int i, int i2) {
        ReadWriteRunnable readWriteRunnable2 = this.readWriteRunnable;
        if (readWriteRunnable2 != null) {
            return readWriteRunnable2.readFromWriteCache(i, i2);
        }
        return new byte[i2];
    }

    public byte read8(int i) {
        return read(i, 1)[0];
    }

    public byte[] read(int i, int i2) {
        ReadWriteRunnable readWriteRunnable2 = this.readWriteRunnable;
        if (readWriteRunnable2 != null) {
            return readWriteRunnable2.read(i, i2);
        }
        return new byte[i2];
    }

    private static void logAndThrow(String str) throws RobotCoreException {
        System.err.println(str);
        throw new RobotCoreException(str);
    }
}
