package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DeviceProperties(builtIn = true, name = "@string/configTypeAnalogInput", xmlTag = "AnalogInput")
@AnalogSensorType
public class AnalogInput implements HardwareDevice {
    private int channel;
    private AnalogInputController controller;

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public void resetDeviceConfigurationForOpMode() {
    }

    public AnalogInput(AnalogInputController analogInputController, int i) {
        this.controller = analogInputController;
        this.channel = i;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public double getVoltage() {
        return this.controller.getAnalogInputVoltage(this.channel);
    }

    public double getMaxVoltage() {
        return this.controller.getMaxAnalogInputVoltage();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeAnalogInput);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; analog port " + this.channel;
    }
}
