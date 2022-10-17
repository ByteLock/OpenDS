package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.DigitalChannelController;

public interface DigitalChannel extends HardwareDevice {

    public enum Mode {
        INPUT,
        OUTPUT
    }

    Mode getMode();

    boolean getState();

    void setMode(Mode mode);

    @Deprecated
    void setMode(DigitalChannelController.Mode mode);

    void setState(boolean z);
}
