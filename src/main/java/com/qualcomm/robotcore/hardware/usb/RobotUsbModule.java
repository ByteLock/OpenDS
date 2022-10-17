package com.qualcomm.robotcore.hardware.usb;

import com.qualcomm.robotcore.exception.RobotCoreException;

public interface RobotUsbModule extends RobotArmingStateNotifier {
    void arm() throws RobotCoreException, InterruptedException;

    void armOrPretend() throws RobotCoreException, InterruptedException;

    void close();

    void disarm() throws RobotCoreException, InterruptedException;

    void pretend() throws RobotCoreException, InterruptedException;
}
