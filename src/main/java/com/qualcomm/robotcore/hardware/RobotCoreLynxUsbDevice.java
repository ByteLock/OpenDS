package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.exception.RobotCoreException;

public interface RobotCoreLynxUsbDevice {
    void close();

    LynxModuleMetaList discoverModules(boolean z) throws RobotCoreException, InterruptedException;

    void failSafe();

    void lockNetworkLockAcquisitions();

    void setThrowOnNetworkLockAcquisition(boolean z);
}
