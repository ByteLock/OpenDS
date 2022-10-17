package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.AnalogSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.AnalogSensorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.util.Range;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

@DeviceProperties(builtIn = true, description = "@string/optical_distance_sensor_description", name = "@string/configTypeOpticalDistanceSensor", xmlTag = "OpticalDistanceSensor")
@AnalogSensorType
public class ModernRoboticsAnalogOpticalDistanceSensor implements OpticalDistanceSensor, AnalogSensor {
    protected static final double apiLevelMax = 1.0d;
    protected static final double apiLevelMin = 0.0d;
    private final AnalogInputController analogInputController;
    private final int physicalPort;

    public void close() {
    }

    public void enableLed(boolean z) {
    }

    public int getVersion() {
        return 0;
    }

    public void resetDeviceConfigurationForOpMode() {
    }

    public ModernRoboticsAnalogOpticalDistanceSensor(AnalogInputController analogInputController2, int i) {
        this.analogInputController = analogInputController2;
        this.physicalPort = i;
    }

    public String toString() {
        return String.format("OpticalDistanceSensor: %1.2f", new Object[]{Double.valueOf(getLightDetected())});
    }

    public double getLightDetected() {
        return Range.clip(Range.scale(getRawLightDetected(), LynxServoController.apiPositionFirst, getRawLightDetectedMax(), LynxServoController.apiPositionFirst, 1.0d), (double) LynxServoController.apiPositionFirst, 1.0d);
    }

    public double getMaxVoltage() {
        return Math.min(5.0d, this.analogInputController.getMaxAnalogInputVoltage());
    }

    public double getRawLightDetected() {
        return Range.clip(readRawVoltage(), (double) LynxServoController.apiPositionFirst, getMaxVoltage());
    }

    public double getRawLightDetectedMax() {
        return getMaxVoltage();
    }

    public double readRawVoltage() {
        return this.analogInputController.getAnalogInputVoltage(this.physicalPort);
    }

    public String status() {
        return String.format("Optical Distance Sensor, connected via device %s, port %d", new Object[]{this.analogInputController.getSerialNumber(), Integer.valueOf(this.physicalPort)});
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeOpticalDistanceSensor);
    }

    public String getConnectionInfo() {
        return this.analogInputController.getConnectionInfo() + "; analog port " + this.physicalPort;
    }
}
