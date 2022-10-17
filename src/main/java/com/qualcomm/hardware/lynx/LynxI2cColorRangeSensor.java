package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.ams.AMSColorSensor;
import com.qualcomm.hardware.ams.AMSColorSensorImpl;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.ColorRangeSensor;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.util.Range;
import java.nio.ByteOrder;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class LynxI2cColorRangeSensor extends AMSColorSensorImpl implements DistanceSensor, OpticalDistanceSensor, ColorRangeSensor {
    protected static final double apiLevelMax = 1.0d;
    protected static final double apiLevelMin = 0.0d;
    public double aParam = 186.347d;
    public double bParam = 30403.5d;
    public double cParam = 0.576649d;

    public LynxI2cColorRangeSensor(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        super(AMSColorSensor.Parameters.createForTMD37821(), i2cDeviceSynchSimple, true);
    }

    public double getDistance(DistanceUnit distanceUnit) {
        return distanceUnit.fromUnit(DistanceUnit.CM, cmFromOptical(rawOptical()));
    }

    /* access modifiers changed from: protected */
    public double cmFromOptical(int i) {
        double d = this.aParam;
        double d2 = this.cParam;
        double d3 = (double) i;
        double d4 = ((-d) * d2) + (d2 * d3);
        double d5 = this.bParam;
        return (d4 - Math.sqrt(((-d) * d5) + (d5 * d3))) / (this.aParam - d3);
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypeLynxColorSensor);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public double getLightDetected() {
        return Range.clip(Range.scale(getRawLightDetected(), LynxServoController.apiPositionFirst, getRawLightDetectedMax(), LynxServoController.apiPositionFirst, 1.0d), (double) LynxServoController.apiPositionFirst, 1.0d);
    }

    public double getRawLightDetected() {
        return (double) rawOptical();
    }

    public double getRawLightDetectedMax() {
        return (double) ((AMSColorSensor.Parameters) this.parameters).proximitySaturation;
    }

    public String status() {
        return String.format(Locale.getDefault(), "%s on %s", new Object[]{getDeviceName(), getConnectionInfo()});
    }

    public int rawOptical() {
        return readUnsignedShort(AMSColorSensor.Register.PDATA, ByteOrder.LITTLE_ENDIAN);
    }
}
