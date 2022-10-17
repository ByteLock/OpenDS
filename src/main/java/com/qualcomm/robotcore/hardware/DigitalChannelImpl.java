package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DigitalIoDeviceType
@DeviceProperties(builtIn = true, name = "@string/configTypeDigitalDevice", xmlTag = "DigitalDevice")
public class DigitalChannelImpl implements DigitalChannel {
    private int channel;
    private DigitalChannelController controller;

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public void resetDeviceConfigurationForOpMode() {
    }

    public DigitalChannelImpl(DigitalChannelController digitalChannelController, int i) {
        this.controller = digitalChannelController;
        this.channel = i;
    }

    public DigitalChannel.Mode getMode() {
        return this.controller.getDigitalChannelMode(this.channel);
    }

    public void setMode(DigitalChannel.Mode mode) {
        this.controller.setDigitalChannelMode(this.channel, mode);
    }

    @Deprecated
    public void setMode(DigitalChannelController.Mode mode) {
        this.controller.setDigitalChannelMode(this.channel, mode);
    }

    public boolean getState() {
        return this.controller.getDigitalChannelState(this.channel);
    }

    public void setState(boolean z) {
        this.controller.setDigitalChannelState(this.channel, z);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeDigitalDevice);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; digital port " + this.channel;
    }
}
