package com.qualcomm.hardware.ams;

import com.qualcomm.hardware.ams.AMSColorSensor;
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
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtConstants;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;

public abstract class AMSColorSensorImpl extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynchSimple, AMSColorSensor.Parameters> implements AMSColorSensor, I2cAddrConfig, Light {
    public static final String TAG = "AMSColorSensorImpl";
    private float softwareGain = 1.0f;

    public String getDeviceName() {
        return "AMS I2C Color Sensor";
    }

    public boolean isLightOn() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, byte b2, byte b3) {
        return (b & b2) == b3;
    }

    public /* bridge */ /* synthetic */ AMSColorSensor.Parameters getParameters() {
        return (AMSColorSensor.Parameters) super.getParameters();
    }

    public /* bridge */ /* synthetic */ boolean initialize(AMSColorSensor.Parameters parameters) {
        return super.initialize(parameters);
    }

    protected AMSColorSensorImpl(AMSColorSensor.Parameters parameters, I2cDeviceSynchSimple i2cDeviceSynchSimple, boolean z) {
        super(i2cDeviceSynchSimple, z, parameters);
        this.deviceClient.setLogging(((AMSColorSensor.Parameters) this.parameters).loggingEnabled);
        this.deviceClient.setLoggingTag(((AMSColorSensor.Parameters) this.parameters).loggingTag);
        registerArmingStateCallback(true);
        engage();
    }

    /* access modifiers changed from: protected */
    public synchronized boolean internalInitialize(AMSColorSensor.Parameters parameters) {
        String str;
        RobotLog.m60vv(TAG, "internalInitialize()...");
        try {
            if (((AMSColorSensor.Parameters) this.parameters).deviceId == parameters.deviceId) {
                this.parameters = parameters.clone();
                setI2cAddress(parameters.i2cAddr);
                if (!this.deviceClient.isArmed()) {
                    return false;
                }
                byte deviceID = getDeviceID();
                if (deviceID != parameters.deviceId) {
                    RobotLog.m49ee(TAG, "unexpected AMS color sensor chipid: found=%d expected=%d", Byte.valueOf(deviceID), Integer.valueOf(parameters.deviceId));
                    RobotLog.m60vv(TAG, "...internalInitialize()");
                    return false;
                }
                dumpState();
                disable();
                setIntegrationTime(parameters.atime);
                setHardwareGain(parameters.gain);
                setPDrive(parameters.ledDrive);
                if (is3782() && parameters.useProximityIfAvailable) {
                    setProximityPulseCount(parameters.proximityPulseCount);
                }
                enable();
                dumpState();
                if ((this.deviceClient instanceof I2cDeviceSynch) && parameters.readWindow != null) {
                    ((I2cDeviceSynch) this.deviceClient).setReadWindow(parameters.readWindow);
                }
                RobotLog.m60vv(TAG, "...internalInitialize()");
                return true;
            }
            throw new IllegalArgumentException(String.format("can't change device types (modify existing params instead): old=%d new=%d", new Object[]{Integer.valueOf(((AMSColorSensor.Parameters) this.parameters).deviceId), Integer.valueOf(parameters.deviceId)}));
        } finally {
            str = "...internalInitialize()";
            RobotLog.m60vv(TAG, str);
        }
    }

    /* access modifiers changed from: protected */
    public void dumpState() {
        RobotLog.logBytes(TAG, "state", read(AMSColorSensor.Register.ENABLE, 25), 25);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:13:0x0039  */
    /* JADX WARNING: Removed duplicated region for block: B:14:0x003b  */
    /* JADX WARNING: Removed duplicated region for block: B:18:0x0046  */
    /* JADX WARNING: Removed duplicated region for block: B:19:0x0048  */
    /* JADX WARNING: Removed duplicated region for block: B:21:0x004b  */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x0060  */
    /* JADX WARNING: Removed duplicated region for block: B:28:0x0065  */
    /* JADX WARNING: Removed duplicated region for block: B:31:0x0069  */
    /* JADX WARNING: Removed duplicated region for block: B:32:0x006e  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void enable() {
        /*
            r7 = this;
            monitor-enter(r7)
            byte r0 = r7.readEnable()     // Catch:{ all -> 0x0088 }
            java.lang.String r1 = "AMSColorSensorImpl"
            java.lang.String r2 = "enable() enabled=0x%02x..."
            r3 = 1
            java.lang.Object[] r4 = new java.lang.Object[r3]     // Catch:{ all -> 0x0088 }
            java.lang.Byte r5 = java.lang.Byte.valueOf(r0)     // Catch:{ all -> 0x0088 }
            r6 = 0
            r4[r6] = r5     // Catch:{ all -> 0x0088 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r1, (java.lang.String) r2, (java.lang.Object[]) r4)     // Catch:{ all -> 0x0088 }
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r1 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.PON     // Catch:{ all -> 0x0088 }
            boolean r1 = r7.testBits((byte) r0, (com.qualcomm.hardware.ams.AMSColorSensor.Enable) r1)     // Catch:{ all -> 0x0088 }
            r1 = r1 ^ r3
            if (r1 != 0) goto L_0x002a
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r2 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.AEN     // Catch:{ all -> 0x0088 }
            boolean r2 = r7.testBits((byte) r0, (com.qualcomm.hardware.ams.AMSColorSensor.Enable) r2)     // Catch:{ all -> 0x0088 }
            if (r2 != 0) goto L_0x0028
            goto L_0x002a
        L_0x0028:
            r2 = r6
            goto L_0x002b
        L_0x002a:
            r2 = r3
        L_0x002b:
            boolean r4 = r7.is3782()     // Catch:{ all -> 0x0088 }
            if (r4 == 0) goto L_0x003b
            java.lang.Object r4 = r7.parameters     // Catch:{ all -> 0x0088 }
            com.qualcomm.hardware.ams.AMSColorSensor$Parameters r4 = (com.qualcomm.hardware.ams.AMSColorSensor.Parameters) r4     // Catch:{ all -> 0x0088 }
            boolean r4 = r4.useProximityIfAvailable     // Catch:{ all -> 0x0088 }
            if (r4 == 0) goto L_0x003b
            r4 = r3
            goto L_0x003c
        L_0x003b:
            r4 = r6
        L_0x003c:
            if (r4 == 0) goto L_0x0048
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r4 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.PEN     // Catch:{ all -> 0x0088 }
            boolean r0 = r7.testBits((byte) r0, (com.qualcomm.hardware.ams.AMSColorSensor.Enable) r4)     // Catch:{ all -> 0x0088 }
            if (r0 != 0) goto L_0x0048
            r0 = r3
            goto L_0x0049
        L_0x0048:
            r0 = r6
        L_0x0049:
            if (r1 == 0) goto L_0x0052
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r1 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.PON     // Catch:{ all -> 0x0088 }
            byte r1 = r1.bVal     // Catch:{ all -> 0x0088 }
            r7.writeEnable(r1)     // Catch:{ all -> 0x0088 }
        L_0x0052:
            r1 = 3
            r7.delay(r1)     // Catch:{ all -> 0x0088 }
            if (r2 != 0) goto L_0x005a
            if (r0 == 0) goto L_0x0073
        L_0x005a:
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r1 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.PON     // Catch:{ all -> 0x0088 }
            byte r1 = r1.bVal     // Catch:{ all -> 0x0088 }
            if (r2 == 0) goto L_0x0065
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r2 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.AEN     // Catch:{ all -> 0x0088 }
            byte r2 = r2.bVal     // Catch:{ all -> 0x0088 }
            goto L_0x0066
        L_0x0065:
            r2 = r6
        L_0x0066:
            r1 = r1 | r2
            if (r0 == 0) goto L_0x006e
            com.qualcomm.hardware.ams.AMSColorSensor$Enable r0 = com.qualcomm.hardware.ams.AMSColorSensor.Enable.PEN     // Catch:{ all -> 0x0088 }
            byte r0 = r0.bVal     // Catch:{ all -> 0x0088 }
            goto L_0x006f
        L_0x006e:
            r0 = r6
        L_0x006f:
            r0 = r0 | r1
            r7.writeEnable(r0)     // Catch:{ all -> 0x0088 }
        L_0x0073:
            byte r0 = r7.readEnableAfterWrite()     // Catch:{ all -> 0x0088 }
            java.lang.String r1 = "AMSColorSensorImpl"
            java.lang.String r2 = "...enable() enabled=0x%02x"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x0088 }
            java.lang.Byte r0 = java.lang.Byte.valueOf(r0)     // Catch:{ all -> 0x0088 }
            r3[r6] = r0     // Catch:{ all -> 0x0088 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r1, (java.lang.String) r2, (java.lang.Object[]) r3)     // Catch:{ all -> 0x0088 }
            monitor-exit(r7)
            return
        L_0x0088:
            r0 = move-exception
            monitor-exit(r7)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.ams.AMSColorSensorImpl.enable():void");
    }

    /* access modifiers changed from: protected */
    public synchronized void disable() {
        byte readEnable = readEnable();
        RobotLog.m61vv(TAG, "disable() enabled=0x%02x...", Byte.valueOf(readEnable));
        writeEnable(readEnable & (~(AMSColorSensor.Enable.PON.bVal | AMSColorSensor.Enable.AEN.bVal | AMSColorSensor.Enable.PEN.bVal)));
        RobotLog.m61vv(TAG, "...disable() enabled=0x%02x", Byte.valueOf(readEnableAfterWrite()));
    }

    /* access modifiers changed from: protected */
    public boolean isConnectedAndEnabled() {
        return testBits(readEnable(), AMSColorSensor.Enable.PON);
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, byte b2) {
        return testBits(b, b2, b2);
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, AMSColorSensor.Enable enable) {
        return testBits(b, enable, enable);
    }

    /* access modifiers changed from: protected */
    public boolean testBits(byte b, AMSColorSensor.Enable enable, AMSColorSensor.Enable enable2) {
        return testBits(b, enable.bVal, enable2.bVal);
    }

    /* access modifiers changed from: protected */
    public void writeEnable(int i) {
        write8(AMSColorSensor.Register.ENABLE, i & (~((is3782() ? AMSColorSensor.Enable.PIEN.bVal : 0) | AMSColorSensor.Enable.RES7.bVal | AMSColorSensor.Enable.RES6.bVal | AMSColorSensor.Enable.PIEN.bVal | AMSColorSensor.Enable.AIEN.bVal)));
    }

    /* access modifiers changed from: protected */
    public byte readEnable() {
        return read8(AMSColorSensor.Register.ENABLE);
    }

    /* access modifiers changed from: protected */
    public byte readEnableAfterWrite() {
        delay(5);
        return readEnable();
    }

    /* access modifiers changed from: protected */
    public void setIntegrationTime(int i) {
        RobotLog.m61vv(TAG, "setIntegrationTime(0x%02x)", Integer.valueOf(i));
        write8(AMSColorSensor.Register.ATIME, i);
    }

    /* access modifiers changed from: protected */
    public void setProximityPulseCount(int i) {
        RobotLog.m61vv(TAG, "setProximityPulseCount(0x%02x)", Integer.valueOf(i));
        write8(AMSColorSensor.Register.PPLUSE, i);
    }

    /* access modifiers changed from: protected */
    public boolean is3782() {
        return ((AMSColorSensor.Parameters) this.parameters).deviceId == 96 || ((AMSColorSensor.Parameters) this.parameters).deviceId == 105;
    }

    /* access modifiers changed from: protected */
    public void setHardwareGain(AMSColorSensor.Gain gain) {
        RobotLog.m61vv(TAG, "setGain(%s)", gain);
        updateControl(AMSColorSensor.Gain.MASK.bVal, gain.bVal);
    }

    /* access modifiers changed from: protected */
    public void setPDrive(AMSColorSensor.LEDDrive lEDDrive) {
        RobotLog.m61vv(TAG, "setPDrive(%s)", lEDDrive);
        updateControl(AMSColorSensor.LEDDrive.MASK.bVal, lEDDrive.bVal);
    }

    /* access modifiers changed from: protected */
    public void updateControl(int i, int i2) {
        byte read8 = read8(AMSColorSensor.Register.CONTROL);
        if (is3782()) {
            read8 |= 32;
        }
        byte b = i & i2;
        write8(AMSColorSensor.Register.CONTROL, b | (read8 & (~i)));
    }

    public byte getDeviceID() {
        return read8(AMSColorSensor.Register.DEVICE_ID);
    }

    public float getGain() {
        return this.softwareGain;
    }

    public void setGain(float f) {
        this.softwareGain = f;
    }

    public synchronized int red() {
        return normalToUnsignedShort(getNormalizedColors().red);
    }

    public synchronized int green() {
        return normalToUnsignedShort(getNormalizedColors().green);
    }

    public synchronized int blue() {
        return normalToUnsignedShort(getNormalizedColors().blue);
    }

    public synchronized int alpha() {
        return normalToUnsignedShort(getNormalizedColors().alpha);
    }

    /* access modifiers changed from: protected */
    public int normalToUnsignedShort(float f) {
        return (int) (f * ((float) ((AMSColorSensor.Parameters) this.parameters).getMaximumReading()));
    }

    public synchronized int argb() {
        return getNormalizedColors().toColor();
    }

    public NormalizedRGBA getNormalizedColors() {
        Deadline deadline = new Deadline(2, TimeUnit.SECONDS);
        while (true) {
            byte[] read = read(AMSColorSensor.Register.STATUS, AMSColorSensor.Register.PDATA.bVal - AMSColorSensor.Register.STATUS.bVal);
            if (testBits(read[0], AMSColorSensor.Status.AVALID.bVal)) {
                int unsignedShortToInt = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 1, ByteOrder.LITTLE_ENDIAN));
                int unsignedShortToInt2 = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 3, ByteOrder.LITTLE_ENDIAN));
                int unsignedShortToInt3 = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 5, ByteOrder.LITTLE_ENDIAN));
                int unsignedShortToInt4 = TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read, 7, ByteOrder.LITTLE_ENDIAN));
                float maximumReading = 1.0f / ((float) ((AMSColorSensor.Parameters) this.parameters).getMaximumReading());
                NormalizedRGBA normalizedRGBA = new NormalizedRGBA();
                normalizedRGBA.alpha = Range.clip(((float) unsignedShortToInt) * this.softwareGain * maximumReading, 0.0f, 1.0f);
                normalizedRGBA.red = Range.clip(((float) unsignedShortToInt2) * this.softwareGain * maximumReading, 0.0f, 1.0f);
                normalizedRGBA.green = Range.clip(((float) unsignedShortToInt3) * this.softwareGain * maximumReading, 0.0f, 1.0f);
                normalizedRGBA.blue = Range.clip(((float) unsignedShortToInt4) * this.softwareGain * maximumReading, 0.0f, 1.0f);
                return normalizedRGBA;
            } else if (!Thread.currentThread().isInterrupted() && isConnectedAndEnabled() && !deadline.hasExpired()) {
                delay(3);
            }
        }
        return new NormalizedRGBA();
    }

    public synchronized void enableLed(boolean z) {
    }

    public synchronized I2cAddr getI2cAddress() {
        return this.deviceClient.getI2cAddress();
    }

    public synchronized void setI2cAddress(I2cAddr i2cAddr) {
        ((AMSColorSensor.Parameters) this.parameters).i2cAddr = i2cAddr;
        this.deviceClient.setI2cAddress(i2cAddr);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.AMS;
    }

    public void resetDeviceConfigurationForOpMode() {
        super.resetDeviceConfigurationForOpMode();
        this.softwareGain = 1.0f;
    }

    /* access modifiers changed from: protected */
    public int readUnsignedByte(AMSColorSensor.Register register) {
        return TypeConversion.unsignedByteToInt(read8(register));
    }

    /* access modifiers changed from: protected */
    public int readUnsignedShort(AMSColorSensor.Register register, ByteOrder byteOrder) {
        return TypeConversion.unsignedShortToInt(TypeConversion.byteArrayToShort(read(register, 2), 0, byteOrder));
    }

    public synchronized byte read8(AMSColorSensor.Register register) {
        return this.deviceClient.read8(register.bVal | FtConstants.DCD);
    }

    public synchronized byte[] read(AMSColorSensor.Register register, int i) {
        return this.deviceClient.read(register.bVal | FtConstants.DCD, i);
    }

    public synchronized void write8(AMSColorSensor.Register register, int i) {
        this.deviceClient.write8(register.bVal | FtConstants.DCD, i, I2cWaitControl.WRITTEN);
    }

    public void write(AMSColorSensor.Register register, byte[] bArr) {
        this.deviceClient.write(register.bVal | FtConstants.DCD | 32, bArr, I2cWaitControl.WRITTEN);
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
