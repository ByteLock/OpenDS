package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.TypeConversion;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;

@I2cDeviceType
@DeviceProperties(builtIn = true, description = "@string/mr_range_description", name = "@string/mr_range_name", xmlTag = "ModernRoboticsI2cRangeSensor")
public class ModernRoboticsI2cRangeSensor extends I2cDeviceSynchDevice<I2cDeviceSynch> implements DistanceSensor, OpticalDistanceSensor, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(40);
    protected static final double apiLevelMax = 1.0d;
    protected static final double apiLevelMin = 0.0d;
    protected static final int cmUltrasonicMax = 255;
    public double aParam = 5.11595056535567d;
    public double bParam = 457.048400147437d;
    public double cParam = -0.8061002068394054d;
    public double dParam = 0.004048820370701007d;
    public int rawOpticalMinValid = 3;

    public void enableLed(boolean z) {
    }

    public double getRawLightDetectedMax() {
        return 255.0d;
    }

    public enum Register {
        FIRST(0),
        FIRMWARE_REV(0),
        MANUFACTURE_CODE(1),
        SENSOR_ID(2),
        ULTRASONIC(4),
        OPTICAL(5),
        LAST(r9.bVal),
        UNKNOWN(-1);
        
        public byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public ModernRoboticsI2cRangeSensor(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true);
        setOptimalReadWindow();
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        super.registerArmingStateCallback(false);
        ((I2cDeviceSynch) this.deviceClient).engage();
    }

    /* access modifiers changed from: protected */
    public void setOptimalReadWindow() {
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(new I2cDeviceSynch.ReadWindow(Register.FIRST.bVal, (Register.LAST.bVal - Register.FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT));
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        return true;
    }

    public double getDistance(DistanceUnit distanceUnit) {
        double d;
        int rawOptical = rawOptical();
        if (rawOptical >= this.rawOpticalMinValid) {
            d = cmFromOptical(rawOptical);
        } else {
            d = cmUltrasonic();
            if (d == 255.0d) {
                return Double.MAX_VALUE;
            }
        }
        return distanceUnit.fromUnit(DistanceUnit.CM, d);
    }

    /* access modifiers changed from: protected */
    public double cmFromOptical(int i) {
        return (this.dParam + Math.log(((-this.aParam) + ((double) i)) / this.bParam)) / this.cParam;
    }

    public double cmUltrasonic() {
        return (double) rawUltrasonic();
    }

    public double cmOptical() {
        int rawOptical = rawOptical();
        if (rawOptical >= this.rawOpticalMinValid) {
            return cmFromOptical(rawOptical);
        }
        return Double.MAX_VALUE;
    }

    public double getLightDetected() {
        return Range.clip(Range.scale(getRawLightDetected(), 0.0d, getRawLightDetectedMax(), 0.0d, 1.0d), 0.0d, 1.0d);
    }

    public double getRawLightDetected() {
        return (double) rawOptical();
    }

    public String status() {
        return String.format(Locale.getDefault(), "%s on %s", new Object[]{getDeviceName(), getConnectionInfo()});
    }

    public int rawUltrasonic() {
        return readUnsignedByte(Register.ULTRASONIC);
    }

    public int rawOptical() {
        return readUnsignedByte(Register.OPTICAL);
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        return String.format(Locale.getDefault(), "Modern Robotics Range Sensor %s", new Object[]{new RobotUsbDevice.FirmwareVersion(read8(Register.FIRMWARE_REV))});
    }

    public byte read8(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public void write8(Register register, byte b) {
        write8(register, b, I2cWaitControl.NONE);
    }

    public void write8(Register register, byte b, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write8(register.bVal, b, i2cWaitControl);
    }

    /* access modifiers changed from: protected */
    public int readUnsignedByte(Register register) {
        return TypeConversion.unsignedByteToInt(read8(register));
    }
}
