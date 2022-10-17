package com.qualcomm.hardware.rev;

import com.qualcomm.hardware.broadcom.BroadcomColorSensor;
import com.qualcomm.hardware.broadcom.BroadcomColorSensorImpl;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.Range;
import java.nio.ByteOrder;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@DeviceProperties(builtIn = true, compatibleControlSystems = {ControlSystem.REV_HUB}, description = "@string/rev_color_sensor_v3_description", name = "@string/rev_color_sensor_v3_name", xmlTag = "RevColorSensorV3")
@I2cDeviceType
public class RevColorSensorV3 extends BroadcomColorSensorImpl implements DistanceSensor, OpticalDistanceSensor, ColorRangeSensor {
    protected static final double apiLevelMax = 1.0d;
    protected static final double apiLevelMin = 0.0d;
    double aParam = 325.961d;
    double binvParam = -0.75934d;
    double cParam = 26.98d;
    double maxDist = 6.0d;

    public String getDeviceName() {
        return "Rev Color Sensor v3";
    }

    public RevColorSensorV3(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        super(BroadcomColorSensor.Parameters.createForAPDS9151(), i2cDeviceSynchSimple, true);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Broadcom;
    }

    public double getLightDetected() {
        return Range.clip(Range.scale(getRawLightDetected(), LynxServoController.apiPositionFirst, getRawLightDetectedMax(), LynxServoController.apiPositionFirst, 1.0d), (double) LynxServoController.apiPositionFirst, 1.0d);
    }

    public double getRawLightDetected() {
        return (double) rawOptical();
    }

    public double getRawLightDetectedMax() {
        return (double) ((BroadcomColorSensor.Parameters) this.parameters).proximitySaturation;
    }

    public String status() {
        return String.format(Locale.getDefault(), "%s on %s", new Object[]{getDeviceName(), getConnectionInfo()});
    }

    public double getDistance(DistanceUnit distanceUnit) {
        return distanceUnit.fromUnit(DistanceUnit.INCH, inFromOptical(rawOptical()));
    }

    /* access modifiers changed from: protected */
    public double inFromOptical(int i) {
        double d = (double) i;
        double d2 = this.cParam;
        if (d <= d2) {
            return this.maxDist;
        }
        return Math.min(Math.pow((d - d2) / this.aParam, this.binvParam), this.maxDist);
    }

    public int rawOptical() {
        return readUnsignedShort(BroadcomColorSensor.Register.PS_DATA, ByteOrder.LITTLE_ENDIAN) & 2047;
    }
}
