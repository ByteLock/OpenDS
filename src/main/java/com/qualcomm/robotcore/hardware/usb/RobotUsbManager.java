package com.qualcomm.robotcore.hardware.usb;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.List;

public interface RobotUsbManager {
    RobotUsbDevice openBySerialNumber(SerialNumber serialNumber) throws RobotCoreException;

    List<SerialNumber> scanForDevices() throws RobotCoreException;
}
