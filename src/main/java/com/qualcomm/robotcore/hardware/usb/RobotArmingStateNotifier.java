package com.qualcomm.robotcore.hardware.usb;

import com.qualcomm.robotcore.util.SerialNumber;

public interface RobotArmingStateNotifier {

    public enum ARMINGSTATE {
        ARMED,
        PRETENDING,
        DISARMED,
        CLOSED,
        TO_ARMED,
        TO_PRETENDING,
        TO_DISARMED
    }

    public interface Callback {
        void onModuleStateChange(RobotArmingStateNotifier robotArmingStateNotifier, ARMINGSTATE armingstate);
    }

    ARMINGSTATE getArmingState();

    SerialNumber getSerialNumber();

    void registerCallback(Callback callback, boolean z);

    void unregisterCallback(Callback callback);
}
