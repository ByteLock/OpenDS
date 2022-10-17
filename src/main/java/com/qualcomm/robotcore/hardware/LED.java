package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DigitalIoDeviceType
@DeviceProperties(builtIn = true, description = "@string/led_description", name = "@string/configTypeLED", xmlTag = "Led")
public class LED implements HardwareDevice, SwitchableLight {
    private final DigitalChannelController controller;
    private final int physicalPort;

    public void close() {
    }

    public int getVersion() {
        return 0;
    }

    public LED(DigitalChannelController digitalChannelController, int i) {
        this.controller = digitalChannelController;
        this.physicalPort = i;
    }

    public void enable(boolean z) {
        this.controller.setDigitalChannelState(this.physicalPort, !z);
    }

    public boolean isLightOn() {
        return this.controller.getDigitalChannelState(this.physicalPort);
    }

    public void enableLight(boolean z) {
        enable(z);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeLED);
    }

    public String getConnectionInfo() {
        return String.format("%s; port %d", new Object[]{this.controller.getConnectionInfo(), Integer.valueOf(this.physicalPort)});
    }

    public void resetDeviceConfigurationForOpMode() {
        this.controller.setDigitalChannelMode(this.physicalPort, DigitalChannel.Mode.OUTPUT);
    }
}
