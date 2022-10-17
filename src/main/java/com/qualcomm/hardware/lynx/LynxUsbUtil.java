package com.qualcomm.hardware.lynx;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

public class LynxUsbUtil {
    private static final String TAG = "LynxUsbUtil";

    public static <T> T makePlaceholderValue(T t) {
        return t;
    }

    public static RobotUsbDevice openUsbDevice(boolean z, RobotUsbManager robotUsbManager, SerialNumber serialNumber) throws RobotCoreException {
        if (z) {
            robotUsbManager.scanForDevices();
        }
        RobotUsbDevice robotUsbDevice = null;
        try {
            robotUsbDevice = robotUsbManager.openBySerialNumber(serialNumber);
        } catch (RobotCoreException e) {
            logMessageAndThrow("unable to open lynx USB device " + serialNumber + ": " + e.getMessage());
        }
        try {
            robotUsbDevice.setBaudRate(460800);
            robotUsbDevice.setDataCharacteristics((byte) 8, (byte) 0, (byte) 0);
            robotUsbDevice.setLatencyTimer(1);
        } catch (RobotUsbException e2) {
            robotUsbDevice.close();
            logMessageAndThrow("Unable to open lynx USB device " + serialNumber + " - " + robotUsbDevice.getProductName() + ": " + e2.getMessage());
        }
        return robotUsbDevice;
    }

    private static void logMessageAndThrow(String str) throws RobotCoreException {
        RobotLog.m48ee(TAG, str);
        throw new RobotCoreException(str);
    }

    public static class Placeholder<T> {
        private boolean logged = false;
        private String message;
        private String tag;

        public Placeholder(String str, String str2, Object... objArr) {
            this.tag = str;
            this.message = String.format("placeholder: %s", new Object[]{String.format(str2, objArr)});
        }

        public synchronized void reset() {
            this.logged = false;
        }

        public synchronized T log(T t) {
            if (!this.logged) {
                RobotLog.m48ee(this.tag, this.message);
                this.logged = true;
            }
            return t;
        }
    }
}
