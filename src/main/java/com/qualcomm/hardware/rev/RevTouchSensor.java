package com.qualcomm.hardware.rev;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.DigitalIoDeviceType;

@DigitalIoDeviceType
@DeviceProperties(builtIn = true, compatibleControlSystems = {ControlSystem.REV_HUB}, description = "@string/rev_touch_sensor_description", name = "@string/configTypeRevTouchSensor", xmlTag = "RevTouchSensor")
public class RevTouchSensor implements TouchSensor {
    private final DigitalChannelController digitalChannelController;
    private final int physicalPort;

    public void close() {
    }

    public String getDeviceName() {
        return "REV Touch Sensor";
    }

    public int getVersion() {
        return 1;
    }

    public RevTouchSensor(DigitalChannelController digitalChannelController2, int i) {
        this.digitalChannelController = digitalChannelController2;
        this.physicalPort = i;
    }

    public double getValue() {
        if (isPressed()) {
            return 1.0d;
        }
        return LynxServoController.apiPositionFirst;
    }

    public boolean isPressed() {
        return !this.digitalChannelController.getDigitalChannelState(this.physicalPort);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public String getConnectionInfo() {
        return this.digitalChannelController.getConnectionInfo() + "; digital channel " + this.physicalPort;
    }

    public void resetDeviceConfigurationForOpMode() {
        this.digitalChannelController.setDigitalChannelMode(this.physicalPort, DigitalChannel.Mode.INPUT);
    }
}
