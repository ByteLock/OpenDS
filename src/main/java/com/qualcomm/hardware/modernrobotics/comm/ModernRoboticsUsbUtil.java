package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.ModernRoboticsConstants;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

public class ModernRoboticsUsbUtil {
    public static RobotUsbDevice openRobotUsbDevice(boolean z, RobotUsbManager robotUsbManager, SerialNumber serialNumber) throws RobotCoreException {
        if (z) {
            robotUsbManager.scanForDevices();
        }
        try {
            RobotUsbDevice openBySerialNumber = robotUsbManager.openBySerialNumber(serialNumber);
            try {
                openBySerialNumber.setBaudRate(ModernRoboticsConstants.USB_BAUD_RATE);
                openBySerialNumber.setDataCharacteristics((byte) 8, (byte) 0, (byte) 0);
                openBySerialNumber.setLatencyTimer(1);
                try {
                    Thread.sleep(400);
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
                return openBySerialNumber;
            } catch (Exception e) {
                openBySerialNumber.close();
                throw RobotCoreException.createChained(e, "Unable to parameterize USB device " + serialNumber + " - " + openBySerialNumber.getProductName() + ": " + e.getMessage(), new Object[0]);
            }
        } catch (Exception e2) {
            throw RobotCoreException.createChained(e2, "Unable to open USB device " + serialNumber + ": " + e2.getMessage(), new Object[0]);
        }
    }

    public static byte[] getUsbDeviceHeader(RobotUsbDevice robotUsbDevice) throws RobotUsbException {
        byte[] bArr = new byte[3];
        try {
            new ModernRoboticsReaderWriter(robotUsbDevice).read(true, 0, bArr, (TimeWindow) null);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        return bArr;
    }
}
