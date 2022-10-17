package com.qualcomm.robotcore.hardware.usb.serial;

import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.File;
import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

public class SerialPort {
    protected static final String TAG = "SerialPort";
    protected int baudRate;
    protected File file;
    protected FileDescriptor fileDescriptor;
    protected FileInputStream fileInputStream;
    protected FileOutputStream fileOutputStream;

    public static native void close(FileDescriptor fileDescriptor2);

    private static native FileDescriptor open(String str, int i, boolean z);

    public SerialPort(File file2, int i) throws IOException {
        this.file = file2;
        ensureReadWriteable(file2);
        this.baudRate = i;
        FileDescriptor open = open(file2.getAbsolutePath(), i, isDragonboard());
        this.fileDescriptor = open;
        if (open != null) {
            this.fileInputStream = new FileInputStream(this.fileDescriptor);
            this.fileOutputStream = new FileOutputStream(this.fileDescriptor);
            return;
        }
        throw new IOException(String.format("SerialPort.SerialPort: failed: path=%s", new Object[]{file2.getAbsolutePath()}));
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        close();
        super.finalize();
    }

    public synchronized void close() {
        FileDescriptor fileDescriptor2 = this.fileDescriptor;
        if (fileDescriptor2 != null) {
            close(fileDescriptor2);
            this.fileDescriptor = null;
        }
    }

    private boolean isDragonboard() {
        return LynxConstants.getControlHubVersion() == 0;
    }

    protected static void ensureReadWriteable(File file2) throws SecurityException {
        if (!file2.canRead() || !file2.canWrite()) {
            RobotLog.m61vv(TAG, "making RW: %s", file2.getAbsolutePath());
            try {
                throw new RuntimeException("incorrect perms on " + file2.getAbsolutePath());
            } catch (Exception e) {
                RobotLog.logStacktrace(e);
                throw new SecurityException(String.format("SerialPort.ensureReadWriteFile: exception: path=%s", new Object[]{file2.getAbsolutePath()}), e);
            }
        }
    }

    public String getName() {
        return this.file.getAbsolutePath();
    }

    public InputStream getInputStream() {
        return this.fileInputStream;
    }

    public OutputStream getOutputStream() {
        return this.fileOutputStream;
    }

    public int getBaudRate() {
        return this.baudRate;
    }

    static {
        System.loadLibrary(RobotLog.TAG);
    }
}
