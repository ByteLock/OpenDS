package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.hardware.SwitchableLight;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.TypeConversion;

public class ModernRoboticsI2cColorSensor extends I2cDeviceSynchDevice<I2cDeviceSynch> implements ColorSensor, NormalizedColorSensor, SwitchableLight, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(60);
    protected final float colorNormalizationFactor = 1.5258789E-5f;
    protected boolean isLightOn = false;
    private float softwareGain = 1.0f;

    public enum Register {
        FIRMWARE_REV(0),
        MANUFACTURE_CODE(1),
        SENSOR_ID(2),
        COMMAND(3),
        COLOR_NUMBER(4),
        RED(5),
        GREEN(6),
        BLUE(7),
        ALPHA(8),
        COLOR_INDEX(9),
        RED_INDEX(10),
        GREEN_INDEX(11),
        BLUE_INDEX(12),
        RED_READING(14),
        GREEN_READING(16),
        BLUE_READING(18),
        ALPHA_READING(20),
        NORMALIZED_RED_READING(22),
        NORMALIZED_GREEN_READING(24),
        NORMALIZED_BLUE_READING(26),
        NORMALIZED_ALPHA_READING(28),
        READ_WINDOW_FIRST(r9.bVal),
        READ_WINDOW_LAST(r4.bVal + 1);
        
        public byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Command {
        ACTIVE_LED(0),
        PASSIVE_LED(1),
        HZ50(53),
        HZ60(54),
        CALIBRATE_BLACK(66),
        CALIBRATE_WHITE(67);
        
        public byte bVal;

        private Command(int i) {
            this.bVal = (byte) i;
        }
    }

    public ModernRoboticsI2cColorSensor(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true);
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(new I2cDeviceSynch.ReadWindow(Register.READ_WINDOW_FIRST.bVal, (Register.READ_WINDOW_LAST.bVal - Register.READ_WINDOW_FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT));
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        registerArmingStateCallback(false);
        ((I2cDeviceSynch) this.deviceClient).engage();
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        enableLed(true);
        return true;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        return String.format("Modern Robotics I2C Color Sensor %s", new Object[]{new RobotUsbDevice.FirmwareVersion(read8(Register.FIRMWARE_REV))});
    }

    public byte read8(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public void write8(Register register, byte b) {
        ((I2cDeviceSynch) this.deviceClient).write8((int) register.bVal, (int) b);
    }

    public int readUnsignedByte(Register register) {
        return TypeConversion.unsignedByteToInt(read8(register));
    }

    public int readUnsignedShort(Register register) {
        return TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(((I2cDeviceSynch) this.deviceClient).read(register.bVal, 2)));
    }

    public void writeCommand(Command command) {
        ((I2cDeviceSynch) this.deviceClient).waitForWriteCompletions(I2cWaitControl.ATOMIC);
        write8(Register.COMMAND, command.bVal);
    }

    public String toString() {
        return String.format("argb: 0x%08x", new Object[]{Integer.valueOf(argb())});
    }

    public int red() {
        return readUnsignedByte(Register.RED);
    }

    public int green() {
        return readUnsignedByte(Register.GREEN);
    }

    public int blue() {
        return readUnsignedByte(Register.BLUE);
    }

    public int alpha() {
        return readUnsignedByte(Register.ALPHA);
    }

    public int argb() {
        return getNormalizedColors().toColor();
    }

    public NormalizedRGBA getNormalizedColors() {
        NormalizedRGBA normalizedRGBA = new NormalizedRGBA();
        normalizedRGBA.red = Range.clip(this.softwareGain * ((float) readUnsignedShort(Register.NORMALIZED_RED_READING)) * 1.5258789E-5f, 0.0f, 1.0f);
        normalizedRGBA.green = Range.clip(this.softwareGain * ((float) readUnsignedShort(Register.NORMALIZED_GREEN_READING)) * 1.5258789E-5f, 0.0f, 1.0f);
        normalizedRGBA.blue = Range.clip(this.softwareGain * ((float) readUnsignedShort(Register.NORMALIZED_BLUE_READING)) * 1.5258789E-5f, 0.0f, 1.0f);
        normalizedRGBA.alpha = Range.clip(this.softwareGain * ((float) readUnsignedShort(Register.NORMALIZED_ALPHA_READING)) * 1.5258789E-5f, 0.0f, 1.0f);
        return normalizedRGBA;
    }

    public float getGain() {
        return this.softwareGain;
    }

    public void setGain(float f) {
        this.softwareGain = f;
    }

    public synchronized void enableLed(boolean z) {
        writeCommand(z ? Command.ACTIVE_LED : Command.PASSIVE_LED);
        this.isLightOn = z;
    }

    public void enableLight(boolean z) {
        enableLed(z);
    }

    public synchronized boolean isLightOn() {
        return this.isLightOn;
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }

    public void resetDeviceConfigurationForOpMode() {
        super.resetDeviceConfigurationForOpMode();
        this.softwareGain = 1.0f;
    }
}
