package com.qualcomm.robotcore.eventloop;

import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;

public interface SyncdDevice {
    public static final int msAbnormalReopenInterval = 250;

    public interface Manager {
        void registerSyncdDevice(SyncdDevice syncdDevice);

        void unregisterSyncdDevice(SyncdDevice syncdDevice);
    }

    public enum ShutdownReason {
        NORMAL,
        ABNORMAL,
        ABNORMAL_ATTEMPT_REOPEN
    }

    public interface Syncable {
        void setSyncDeviceManager(Manager manager);
    }

    RobotUsbModule getOwner();

    ShutdownReason getShutdownReason();

    void setOwner(RobotUsbModule robotUsbModule);
}
