package com.qualcomm.robotcore.hardware.usb.serial;

import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.File;
import java.io.IOException;
import java.io.InterruptedIOException;
import java.util.ArrayDeque;
import java.util.Queue;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbUnspecifiedException;
import org.firstinspires.inspection.InspectionState;

public class RobotUsbDeviceTty extends RobotUsbDeviceImplBase implements RobotUsbDevice {
    public static boolean DEBUG = false;
    public static final String TAG = "RobotUsbDeviceTTY";
    protected int baudRate;
    protected boolean debugRetainBuffers = false;
    protected final File file;
    protected int msDefaultTimeout = 100;
    protected String productName = InspectionState.NO_VERSION;
    protected Queue<Byte> readAhead = new ArrayDeque();
    protected final Object readLock = new Object();
    protected SerialPort serialPort;
    protected final Object startStopLock = new Object();
    protected RobotUsbDevice.USBIdentifiers usbIdentifiers = new RobotUsbDevice.USBIdentifiers();
    protected final Object writeLock = new Object();

    public String getTag() {
        return TAG;
    }

    public boolean isAttached() {
        return true;
    }

    public boolean mightBeAtUsbPacketStart() {
        return true;
    }

    public void requestReadInterrupt(boolean z) {
    }

    public void resetAndFlushBuffers() {
    }

    public void setBaudRate(int i) throws RobotUsbException {
    }

    public void setBreak(boolean z) throws RobotUsbException {
    }

    public void setDataCharacteristics(byte b, byte b2, byte b3) throws RobotUsbException {
    }

    public void setLatencyTimer(int i) throws RobotUsbException {
    }

    public void skipToLikelyUsbPacketStart() {
    }

    public RobotUsbDeviceTty(SerialPort serialPort2, SerialNumber serialNumber, File file2) {
        super(serialNumber);
        RobotLog.m61vv(TAG, "opening serial=%s file=%s", serialNumber, file2.getPath());
        this.file = file2;
        this.serialPort = serialPort2;
        this.baudRate = serialPort2.getBaudRate();
    }

    public void close() {
        synchronized (this.startStopLock) {
            if (this.serialPort != null) {
                RobotLog.m61vv(TAG, "closing serial=%s file=%s", this.serialNumber, this.file.getPath());
                this.serialPort.close();
                this.serialPort = null;
                removeFromExtantDevices();
            }
        }
    }

    public boolean isOpen() {
        boolean z;
        synchronized (this.startStopLock) {
            z = this.serialPort != null;
        }
        return z;
    }

    public void write(byte[] bArr) throws RobotUsbException {
        synchronized (this.writeLock) {
            try {
                this.serialPort.getOutputStream().write(bArr);
                if (DEBUG) {
                    dumpBytesSent(bArr);
                }
            } catch (IOException e) {
                throw RobotUsbUnspecifiedException.createChained(e, "exception in %s.write()", TAG);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public int read(byte[] bArr, int i, int i2, long j, TimeWindow timeWindow) throws RobotUsbException, InterruptedException {
        InterruptedException interruptedException;
        synchronized (this.readLock) {
            try {
                ElapsedTime elapsedTime = new ElapsedTime();
                int i3 = 0;
                while (i3 < i2 && this.readAhead.size() > 0) {
                    bArr[i3] = this.readAhead.remove().byteValue();
                    i3++;
                }
                while (true) {
                    if (!isOpen() || i3 >= i2) {
                        break;
                    }
                    int read = this.serialPort.getInputStream().read(bArr, i + i3, i2 - i3);
                    if (read == -1) {
                        read = 0;
                    }
                    Assert.assertTrue(read >= 0);
                    i3 += read;
                    if (i3 == i2) {
                        break;
                    } else if (elapsedTime.milliseconds() > ((double) j)) {
                        break;
                    } else if (!Thread.interrupted()) {
                        Thread.yield();
                    } else {
                        throw new InterruptedException();
                    }
                }
                if (i3 == i2) {
                    if (DEBUG) {
                        dumpBytesReceived(bArr, i, i3);
                    }
                    if (timeWindow != null) {
                        timeWindow.clear();
                    }
                    return i3;
                }
                for (int i4 = 0; i4 < i3; i4++) {
                    this.readAhead.add(Byte.valueOf(bArr[i4]));
                }
                RobotLog.m49ee(TAG, "didn't read enough data cbToRead=%d cbRead=%d msTimeout=%d", Integer.valueOf(i2), Integer.valueOf(i3), Long.valueOf(j));
                return 0;
            } catch (InterruptedIOException e) {
                if (e.getCause() instanceof InterruptedException) {
                    interruptedException = (InterruptedException) e.getCause();
                } else {
                    interruptedException = new InterruptedException(e.getMessage());
                }
                throw interruptedException;
            } catch (IOException e2) {
                throw RobotUsbUnspecifiedException.createChained(e2, "exception in %s.read()", TAG);
            } catch (Throwable th) {
                throw th;
            }
        }
    }

    public void setDebugRetainBuffers(boolean z) {
        this.debugRetainBuffers = z;
    }

    public boolean getDebugRetainBuffers() {
        return this.debugRetainBuffers;
    }

    public void logRetainedBuffers(long j, long j2, String str, String str2, Object... objArr) {
        RobotLog.m49ee(str, str2, objArr);
    }

    public void setMsDefaultTimeout(int i) {
        this.msDefaultTimeout = i;
    }

    public int getMsDefaultTimeout() {
        return this.msDefaultTimeout;
    }

    public RobotUsbDevice.USBIdentifiers getUsbIdentifiers() {
        return this.usbIdentifiers;
    }

    public void setUsbIdentifiers(RobotUsbDevice.USBIdentifiers uSBIdentifiers) {
        this.usbIdentifiers = uSBIdentifiers;
    }

    public void setProductName(String str) {
        this.productName = str;
    }

    public String getProductName() {
        return this.productName;
    }
}
