package com.qualcomm.robotcore.hardware.usb.ftdi;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtDevice;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtDeviceManager;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

public class RobotUsbManagerFtdi implements RobotUsbManager {
    public static final String TAG = "RobotUsbManagerFtdi";
    private FtDeviceManager ftDeviceManager = FtDeviceManager.getInstance();
    private int numberOfDevices;

    public synchronized List<SerialNumber> scanForDevices() throws RobotCoreException {
        ArrayList arrayList;
        this.numberOfDevices = this.ftDeviceManager.createDeviceInfoList();
        arrayList = new ArrayList(this.numberOfDevices);
        for (int i = 0; i < this.numberOfDevices; i++) {
            arrayList.add(getDeviceSerialNumberByIndex(i));
        }
        return arrayList;
    }

    /* access modifiers changed from: protected */
    public SerialNumber getDeviceSerialNumberByIndex(int i) throws RobotCoreException {
        return SerialNumber.fromString(this.ftDeviceManager.getDeviceInfoListDetail(i).serialNumber);
    }

    public static SerialNumber getSerialNumber(FtDevice ftDevice) {
        return SerialNumber.fromString(ftDevice.getDeviceInfo().serialNumber);
    }

    public RobotUsbDevice openBySerialNumber(SerialNumber serialNumber) throws RobotCoreException {
        FtDevice openBySerialNumber = this.ftDeviceManager.openBySerialNumber(serialNumber.getString());
        if (openBySerialNumber != null) {
            try {
                openBySerialNumber.resetDevice();
            } catch (RobotUsbException e) {
                RobotLog.m50ee(TAG, (Throwable) e, "unable to reset FtDevice(%s): ignoring");
            }
            return new RobotUsbDeviceFtdi(openBySerialNumber, serialNumber);
        }
        throw new RobotCoreException("FTDI driver failed to open USB device with serial number " + serialNumber + " (returned null device)");
    }
}
