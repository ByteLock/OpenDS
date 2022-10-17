package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DeviceProperties(builtIn = true, description = "@string/mr_touch_sensor_description", name = "@string/configTypeMRTouchSensor", xmlTag = "ModernRoboticsAnalogTouchSensor")
@AnalogSensorType
public class ModernRoboticsTouchSensor implements TouchSensor {
    private AnalogInputController analogInputController;
    private double analogThreshold;
    private DigitalChannelController digitalController;
    private int physicalPort;

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public ModernRoboticsTouchSensor(DigitalChannelController digitalChannelController, int i) {
        this.analogInputController = null;
        this.digitalController = digitalChannelController;
        this.physicalPort = i;
    }

    public ModernRoboticsTouchSensor(AnalogInputController analogInputController2, int i) {
        this.digitalController = null;
        this.analogInputController = analogInputController2;
        this.physicalPort = i;
        this.analogThreshold = analogInputController2.getMaxAnalogInputVoltage() / 2.0d;
    }

    public boolean isDigital() {
        return this.digitalController != null;
    }

    public boolean isAnalog() {
        return !isDigital();
    }

    public double getAnalogVoltageThreshold() {
        return this.analogThreshold;
    }

    public void setAnalogVoltageThreshold(double d) {
        this.analogThreshold = d;
    }

    public String toString() {
        return String.format("Touch Sensor: %1.2f", new Object[]{Double.valueOf(getValue())});
    }

    public double getValue() {
        if (isPressed()) {
            return 1.0d;
        }
        return LynxServoController.apiPositionFirst;
    }

    public boolean isPressed() {
        if (isDigital()) {
            return this.digitalController.getDigitalChannelState(this.physicalPort);
        }
        return this.analogInputController.getAnalogInputVoltage(this.physicalPort) > getAnalogVoltageThreshold();
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeMRTouchSensor);
    }

    public String getConnectionInfo() {
        if (isDigital()) {
            return this.digitalController.getConnectionInfo() + "; digital port " + this.physicalPort;
        }
        return this.analogInputController.getConnectionInfo() + "; analog port " + this.physicalPort;
    }

    public void resetDeviceConfigurationForOpMode() {
        if (isDigital()) {
            this.digitalController.setDigitalChannelMode(this.physicalPort, DigitalChannel.Mode.INPUT);
        }
    }
}
