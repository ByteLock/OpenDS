package com.qualcomm.hardware.lynx;

import com.qualcomm.robotcore.exception.RobotCoreException;

public interface Supplier<T> {
    T get() throws InterruptedException, RobotCoreException, LynxNackException;
}
