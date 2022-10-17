package com.qualcomm.robotcore.hardware.usb;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.List;

public class RobotUsbManagerCombining implements RobotUsbManager {
    public static final String TAG = "RobotUsbManagerCombining";
    protected List<ManagerInfo> managers = new ArrayList();

    protected class ManagerInfo {
        public RobotUsbManager manager;
        public int scanCount;

        protected ManagerInfo() {
        }
    }

    public void addManager(RobotUsbManager robotUsbManager) {
        ManagerInfo managerInfo = new ManagerInfo();
        managerInfo.manager = robotUsbManager;
        managerInfo.scanCount = 0;
        this.managers.add(managerInfo);
    }

    public synchronized List<SerialNumber> scanForDevices() throws RobotCoreException {
        ArrayList arrayList;
        arrayList = new ArrayList();
        for (ManagerInfo managerInfo : this.managers) {
            try {
                arrayList.addAll(managerInfo.manager.scanForDevices());
            } catch (RobotCoreException unused) {
            }
        }
        return arrayList;
    }

    public synchronized RobotUsbDevice openBySerialNumber(SerialNumber serialNumber) throws RobotCoreException {
        RobotUsbDevice robotUsbDevice;
        robotUsbDevice = null;
        for (ManagerInfo managerInfo : this.managers) {
            try {
                robotUsbDevice = managerInfo.manager.openBySerialNumber(serialNumber);
                if (robotUsbDevice != null) {
                    break;
                }
            } catch (RobotCoreException e) {
                RobotLog.m60vv(TAG, e.getMessage());
            }
        }
        if (robotUsbDevice == null) {
            throw new RobotCoreException("Combiner unable to open device with serialNumber = " + serialNumber);
        }
        return robotUsbDevice;
    }
}
