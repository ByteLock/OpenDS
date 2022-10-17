package com.qualcomm.hardware.broadcom;

import com.qualcomm.hardware.broadcom.BroadcomColorSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.Light;
import com.qualcomm.robotcore.hardware.NormalizedRGBA;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteOrder;

public abstract class BroadcomColorSensorImpl extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynchSimple, BroadcomColorSensor.Parameters> implements BroadcomColorSensor, I2cAddrConfig, Light {
    public static final String TAG = "BroadcomColorSensorImpl";
    int alpha = 0;
    int blue = 0;
    NormalizedRGBA colors = new NormalizedRGBA();
    int green = 0;
    int red = 0;
    float softwareGain = 1.0f;

    public String getDeviceName() {
        return "Broadcom I2C Color Sensor";
    }

    public boolean isLightOn() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, byte b2, byte b3) {
        return (b & b2) == b3;
    }

    public /* bridge */ /* synthetic */ BroadcomColorSensor.Parameters getParameters() {
        return (BroadcomColorSensor.Parameters) super.getParameters();
    }

    public /* bridge */ /* synthetic */ boolean initialize(BroadcomColorSensor.Parameters parameters) {
        return super.initialize(parameters);
    }

    protected BroadcomColorSensorImpl(BroadcomColorSensor.Parameters parameters, I2cDeviceSynchSimple i2cDeviceSynchSimple, boolean z) {
        super(i2cDeviceSynchSimple, z, parameters);
        this.deviceClient.setLogging(((BroadcomColorSensor.Parameters) this.parameters).loggingEnabled);
        this.deviceClient.setLoggingTag(((BroadcomColorSensor.Parameters) this.parameters).loggingTag);
        registerArmingStateCallback(true);
        engage();
    }

    /* access modifiers changed from: protected */
    public synchronized boolean internalInitialize(BroadcomColorSensor.Parameters parameters) {
        String str;
        RobotLog.m60vv(TAG, "internalInitialize()...");
        try {
            if (((BroadcomColorSensor.Parameters) this.parameters).deviceId == parameters.deviceId) {
                this.parameters = parameters.clone();
                setI2cAddress(parameters.i2cAddr);
                if (!this.deviceClient.isArmed()) {
                    return false;
                }
                byte deviceID = getDeviceID();
                if (deviceID != parameters.deviceId) {
                    RobotLog.m49ee(TAG, "unexpected Broadcom color sensor chipid: found=%d expected=%d", Byte.valueOf(deviceID), Integer.valueOf(parameters.deviceId));
                    RobotLog.m60vv(TAG, "...internalInitialize()");
                    return false;
                }
                dumpState();
                setHardwareGain(parameters.gain);
                setLEDParameters(parameters.pulseModulation, parameters.ledCurrent);
                setProximityPulseCount(parameters.proximityPulseCount);
                setPSRateAndRes(BroadcomColorSensor.Parameters.proximityResolution, parameters.proximityMeasRate);
                setLSRateAndRes(BroadcomColorSensor.Parameters.lightSensorResolution, parameters.lightSensorMeasRate);
                enable();
                dumpState();
                if ((this.deviceClient instanceof I2cDeviceSynch) && parameters.readWindow != null) {
                    ((I2cDeviceSynch) this.deviceClient).setReadWindow(parameters.readWindow);
                }
                RobotLog.m60vv(TAG, "...internalInitialize()");
                return true;
            }
            throw new IllegalArgumentException(String.format("can't change device types (modify existing params instead): old=%d new=%d", new Object[]{Integer.valueOf(((BroadcomColorSensor.Parameters) this.parameters).deviceId), Integer.valueOf(parameters.deviceId)}));
        } finally {
            str = "...internalInitialize()";
            RobotLog.m60vv(TAG, str);
        }
    }

    /* access modifiers changed from: protected */
    public void dumpState() {
        RobotLog.logBytes(TAG, "state", read(BroadcomColorSensor.Register.MAIN_CTRL, 7), 7);
    }

    /* access modifiers changed from: protected */
    public synchronized void enable() {
        RobotLog.m61vv(TAG, "enable() enabled=0x%02x...", Byte.valueOf(readMainCtrl()));
        write8(BroadcomColorSensor.Register.MAIN_CTRL, BroadcomColorSensor.MainControl.PS_EN.bVal | BroadcomColorSensor.MainControl.LS_EN.bVal | BroadcomColorSensor.MainControl.RGB_MODE.bVal);
        RobotLog.m61vv(TAG, "...enable() enabled=0x%02x", Byte.valueOf(readMainCtrl()));
    }

    /* access modifiers changed from: protected */
    public synchronized void disable() {
        byte readMainCtrl = readMainCtrl();
        RobotLog.m61vv(TAG, "disable() enabled=0x%02x...", Byte.valueOf(readMainCtrl));
        write8(BroadcomColorSensor.Register.MAIN_CTRL, readMainCtrl & (~(BroadcomColorSensor.MainControl.PS_EN.bVal | BroadcomColorSensor.MainControl.LS_EN.bVal | BroadcomColorSensor.MainControl.RGB_MODE.bVal)));
        RobotLog.m61vv(TAG, "...disable() enabled=0x%02x", Byte.valueOf(readMainCtrl()));
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, byte b2) {
        return testBits(b, b2, b2);
    }

    /* access modifiers changed from: protected */
    public byte readMainCtrl() {
        return read8(BroadcomColorSensor.Register.MAIN_CTRL);
    }

    /* access modifiers changed from: protected */
    public void setProximityPulseCount(int i) {
        RobotLog.m61vv(TAG, "setProximityPulseCount(0x%02x)", Integer.valueOf(i));
        write8(BroadcomColorSensor.Register.PS_PULSES, i);
    }

    /* access modifiers changed from: protected */
    public void setHardwareGain(BroadcomColorSensor.Gain gain) {
        RobotLog.m61vv(TAG, "setGain(0x%02x)", Byte.valueOf(gain.bVal));
        write8(BroadcomColorSensor.Register.LS_GAIN, gain.bVal);
    }

    /* access modifiers changed from: protected */
    public void setPDrive(BroadcomColorSensor.LEDCurrent lEDCurrent) {
        RobotLog.m61vv(TAG, "setPDrive(0x%02x)", Byte.valueOf(lEDCurrent.bVal));
        write8(BroadcomColorSensor.Register.PS_LED, lEDCurrent.bVal);
    }

    public byte getDeviceID() {
        return read8(BroadcomColorSensor.Register.PART_ID);
    }

    /* access modifiers changed from: protected */
    public void setLEDParameters(BroadcomColorSensor.LEDPulseModulation lEDPulseModulation, BroadcomColorSensor.LEDCurrent lEDCurrent) {
        byte b = (byte) ((lEDPulseModulation.bVal << 4) | lEDCurrent.bVal);
        RobotLog.m61vv(TAG, "setLEDParameters(0x%02x)", Byte.valueOf(b));
        write8(BroadcomColorSensor.Register.PS_LED, b);
    }

    /* access modifiers changed from: protected */
    public void setPSRateAndRes(BroadcomColorSensor.PSResolution pSResolution, BroadcomColorSensor.PSMeasurementRate pSMeasurementRate) {
        byte b = (byte) ((pSResolution.bVal << 3) | pSMeasurementRate.bVal);
        RobotLog.m61vv(TAG, "setPSMeasRate(0x%02x)", Byte.valueOf(b));
        write8(BroadcomColorSensor.Register.PS_MEAS_RATE, b);
    }

    /* access modifiers changed from: protected */
    public void setLSRateAndRes(BroadcomColorSensor.LSResolution lSResolution, BroadcomColorSensor.LSMeasurementRate lSMeasurementRate) {
        byte b = (byte) ((lSResolution.bVal << 4) | lSMeasurementRate.bVal);
        RobotLog.m61vv(TAG, "setLSMeasRate(0x%02x)", Byte.valueOf(b));
        write8(BroadcomColorSensor.Register.LS_MEAS_RATE, b);
    }

    public synchronized int red() {
        updateColors();
        return this.red;
    }

    public synchronized int green() {
        updateColors();
        return this.green;
    }

    public synchronized int blue() {
        updateColors();
        return this.blue;
    }

    public synchronized int alpha() {
        updateColors();
        return this.alpha;
    }

    public synchronized int argb() {
        return getNormalizedColors().toColor();
    }

    public void setGain(float f) {
        this.softwareGain = f;
    }

    public float getGain() {
        return this.softwareGain;
    }

    private void updateColors() {
        if (testBits(read8(BroadcomColorSensor.Register.MAIN_STATUS), BroadcomColorSensor.MainStatus.LS_DATA_STATUS.bVal)) {
            byte[] read = read(BroadcomColorSensor.Register.LS_DATA_GREEN, 9);
            this.green = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 0, ByteOrder.LITTLE_ENDIAN));
            this.blue = Range.clip((int) (((double) TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 3, ByteOrder.LITTLE_ENDIAN))) * 1.55d), 0, 65535);
            int clip = Range.clip((int) (((double) TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 6, ByteOrder.LITTLE_ENDIAN))) * 1.07d), 0, 65535);
            this.red = clip;
            this.alpha = ((this.green + clip) + this.blue) / 3;
            this.colors.red = Range.clip((((float) clip) * this.softwareGain) / ((float) ((BroadcomColorSensor.Parameters) this.parameters).colorSaturation), 0.0f, 1.0f);
            this.colors.green = Range.clip((((float) this.green) * this.softwareGain) / ((float) ((BroadcomColorSensor.Parameters) this.parameters).colorSaturation), 0.0f, 1.0f);
            this.colors.blue = Range.clip((((float) this.blue) * this.softwareGain) / ((float) ((BroadcomColorSensor.Parameters) this.parameters).colorSaturation), 0.0f, 1.0f);
            this.colors.alpha = (float) ((-(65535.0d / (Math.pow((double) (((float) ((this.red + this.green) + this.blue)) / 3.0f), 2.0d) + 65535.0d))) + 1.0d);
        }
    }

    public NormalizedRGBA getNormalizedColors() {
        updateColors();
        return this.colors;
    }

    public synchronized void enableLed(boolean z) {
    }

    public synchronized I2cAddr getI2cAddress() {
        return this.deviceClient.getI2cAddress();
    }

    public synchronized void setI2cAddress(I2cAddr i2cAddr) {
        ((BroadcomColorSensor.Parameters) this.parameters).i2cAddr = i2cAddr;
        this.deviceClient.setI2cAddress(i2cAddr);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Broadcom;
    }

    public void resetDeviceConfigurationForOpMode() {
        super.resetDeviceConfigurationForOpMode();
        this.softwareGain = 1.0f;
    }

    /* access modifiers changed from: protected */
    public int readUnsignedByte(BroadcomColorSensor.Register register) {
        return TypeConversion.unsignedByteToInt(read8(register));
    }

    /* access modifiers changed from: protected */
    public int readUnsignedShort(BroadcomColorSensor.Register register, ByteOrder byteOrder) {
        return TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read(register, 2), 0, byteOrder));
    }

    public synchronized byte read8(BroadcomColorSensor.Register register) {
        return this.deviceClient.read8(register.bVal);
    }

    public synchronized byte[] read(BroadcomColorSensor.Register register, int i) {
        return this.deviceClient.read(register.bVal, i);
    }

    public synchronized void write8(BroadcomColorSensor.Register register, int i) {
        this.deviceClient.write8(register.bVal, i, I2cWaitControl.WRITTEN);
    }

    public void write(BroadcomColorSensor.Register register, byte[] bArr) {
        this.deviceClient.write(register.bVal, bArr, I2cWaitControl.WRITTEN);
    }

    /* access modifiers changed from: protected */
    public void delay(int i) {
        try {
            Thread.sleep((long) i);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }
}
