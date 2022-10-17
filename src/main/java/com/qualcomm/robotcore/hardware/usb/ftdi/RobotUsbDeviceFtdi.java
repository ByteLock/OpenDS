package com.qualcomm.robotcore.hardware.usb.ftdi;

import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.File;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtDevice;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbDeviceClosedException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbFTDIException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbUnspecifiedException;

public class RobotUsbDeviceFtdi extends RobotUsbDeviceImplBase implements RobotUsbDevice {
    public static boolean DEBUG = false;
    public static final String TAG = "RobotUsbDeviceFtdi";
    private int cbus_mask = 0;
    private int cbus_outputs = 0;
    private final FtDevice device;

    protected interface RunnableWithRobotUsbCommException {
        void run() throws RobotUsbException;
    }

    public String getTag() {
        return TAG;
    }

    public boolean supportsCbusBitbang() {
        return true;
    }

    public RobotUsbDeviceFtdi(FtDevice ftDevice, SerialNumber serialNumber) {
        super(serialNumber);
        this.device = ftDevice;
        this.firmwareVersion = new RobotUsbDevice.FirmwareVersion();
    }

    public void setDebugRetainBuffers(boolean z) {
        this.device.setDebugRetainBuffers(z);
    }

    public boolean getDebugRetainBuffers() {
        return this.device.getDebugRetainBuffers();
    }

    public void logRetainedBuffers(long j, long j2, String str, String str2, Object... objArr) {
        this.device.logRetainedBuffers(j, j2, str, str2, objArr);
    }

    public void setBaudRate(int i) throws RobotUsbException {
        this.device.setBaudRate(i);
    }

    public void setDataCharacteristics(byte b, byte b2, byte b3) throws RobotUsbException {
        this.device.setDataCharacteristics(b, b2, b3);
    }

    public void setLatencyTimer(int i) throws RobotUsbException {
        this.device.setLatencyTimer((byte) i);
    }

    public void setBreak(boolean z) throws RobotUsbException {
        if (z) {
            this.device.setBreakOn();
        } else {
            this.device.setBreakOff();
        }
    }

    public void resetAndFlushBuffers() throws RobotUsbException {
        this.device.resetDevice();
        this.device.flushBuffers();
    }

    public void write(byte[] bArr) throws InterruptedException, RobotUsbException {
        this.device.write(bArr);
    }

    public int read(byte[] bArr, int i, int i2, long j, TimeWindow timeWindow) throws RobotUsbException, InterruptedException {
        if (i2 > 0) {
            try {
                int read = this.device.read(bArr, i, i2, j, timeWindow);
                if (read == i2) {
                    if (DEBUG) {
                        dumpBytesReceived(bArr, i, read);
                    }
                    return read;
                } else if (read < 0) {
                    if (read == -3) {
                        throw new RobotUsbUnspecifiedException("error: illegal state");
                    } else if (read == -2) {
                        throw new IllegalArgumentException("illegal argument passed to RobotUsbDevice.read()");
                    } else if (read == -1) {
                        Throwable deviceClosedReason = this.device.getDeviceClosedReason();
                        if (deviceClosedReason == null) {
                            deviceClosedReason = new RobotUsbDeviceClosedException("error: closed: FT_Device.read()==RC_DEVICE_CLOSED");
                        }
                        throw deviceClosedReason;
                    } else {
                        throw new RobotUsbUnspecifiedException("error: FT_Device.read()=%d", Integer.valueOf(read));
                    }
                } else if (read != 0) {
                    throw new RobotUsbUnspecifiedException("unexpected result %d from FT_Device_.read()", Integer.valueOf(read));
                }
            } catch (RuntimeException e) {
                throw RobotUsbFTDIException.createChained(e, "runtime exception during read() of %d bytes on %s", Integer.valueOf(i2), this.serialNumber);
            }
        }
        return 0;
    }

    public boolean mightBeAtUsbPacketStart() {
        return this.device.mightBeAtUsbPacketStart();
    }

    public void skipToLikelyUsbPacketStart() {
        this.device.skipToLikelyUsbPacketStart();
    }

    public void requestReadInterrupt(boolean z) {
        this.device.requestReadInterrupt(z);
    }

    public synchronized void close() {
        RobotLog.m61vv(TAG, "closing %s", this.serialNumber);
        this.device.close();
        removeFromExtantDevices();
    }

    public boolean isOpen() {
        return FtDevice.isOpen(this.device);
    }

    public boolean isAttached() {
        return new File(this.device.getUsbDevice().getDeviceName()).exists();
    }

    public RobotUsbDevice.USBIdentifiers getUsbIdentifiers() {
        RobotUsbDevice.USBIdentifiers uSBIdentifiers = new RobotUsbDevice.USBIdentifiers();
        int i = this.device.getDeviceInfo().f265id;
        uSBIdentifiers.vendorId = (i >> 16) & 65535;
        uSBIdentifiers.productId = i & 65535;
        uSBIdentifiers.bcdDevice = this.device.getDeviceInfo().bcdDevice;
        return uSBIdentifiers;
    }

    public String getProductName() {
        return this.device.getDeviceInfo().productName;
    }

    public void cbus_setup(int i, int i2) throws InterruptedException, RobotUsbException {
        int i3 = i & 15;
        this.cbus_mask = i3;
        int i4 = i2 & 15;
        this.cbus_outputs = i4;
        cbus_setBitMode((i3 & i4) | (i3 << 4), 32);
    }

    public void cbus_write(int i) throws InterruptedException, RobotUsbException {
        int i2 = i & 15;
        this.cbus_outputs = i2;
        int i3 = this.cbus_mask;
        cbus_setBitMode((i2 & i3) | (i3 << 4), 32);
    }

    private void cbus_setBitMode(int i, int i2) throws InterruptedException, RobotUsbException {
        if (!this.device.setBitMode((byte) i, (byte) i2)) {
            throw new RobotUsbUnspecifiedException("setBitMode(0x%02x 0x02x) failed", Integer.valueOf(i), Integer.valueOf(i2));
        }
    }
}
