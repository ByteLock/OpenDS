package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.IntegratingGyroscope;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public class ModernRoboticsI2cGyro extends I2cDeviceSynchDevice<I2cDeviceSynch> implements GyroSensor, Gyroscope, IntegratingGyroscope, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(32);
    protected float degreesPerSecondPerDigit = 0.00875f;
    protected float degreesPerZAxisTick;
    protected HeadingMode headingMode = HeadingMode.HEADING_CARTESIAN;

    public enum HeadingMode {
        HEADING_CARTESIAN,
        HEADING_CARDINAL
    }

    @Deprecated
    public enum MeasurementMode {
        GYRO_CALIBRATION_PENDING,
        GYRO_CALIBRATING,
        GYRO_NORMAL
    }

    /* access modifiers changed from: protected */
    public int truncate(float f) {
        return (int) f;
    }

    public enum Register {
        READ_WINDOW_FIRST(0),
        FIRMWARE_REV(0),
        MANUFACTURE_CODE(1),
        SENSOR_ID(2),
        COMMAND(3),
        HEADING_DATA(4),
        INTEGRATED_Z_VALUE(6),
        RAW_X_VAL(8),
        RAW_Y_VAL(10),
        RAW_Z_VAL(12),
        Z_AXIS_OFFSET(14),
        Z_AXIS_SCALE_COEF(16),
        READ_WINDOW_LAST(r8.bVal + 1),
        UNKNOWN(-1);
        
        public byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }

        public static Register fromByte(byte b) {
            for (Register register : values()) {
                if (register.bVal == b) {
                    return register;
                }
            }
            return UNKNOWN;
        }
    }

    public enum Command {
        NORMAL(0),
        CALIBRATE(78),
        RESET_Z_AXIS(82),
        WRITE_EEPROM(87),
        UNKNOWN(-1);
        
        public byte bVal;

        private Command(int i) {
            this.bVal = (byte) i;
        }

        public static Command fromByte(byte b) {
            for (Command command : values()) {
                if (command.bVal == b) {
                    return command;
                }
            }
            return UNKNOWN;
        }
    }

    public ModernRoboticsI2cGyro(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true);
        setOptimalReadWindow();
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        super.registerArmingStateCallback(false);
        ((I2cDeviceSynch) this.deviceClient).engage();
    }

    /* access modifiers changed from: protected */
    public void setOptimalReadWindow() {
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(new I2cDeviceSynch.ReadWindow(Register.READ_WINDOW_FIRST.bVal, (Register.READ_WINDOW_LAST.bVal - Register.READ_WINDOW_FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT));
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        writeCommand(Command.NORMAL);
        resetZAxisIntegrator();
        setZAxisScalingCoefficient(256);
        this.headingMode = HeadingMode.HEADING_CARTESIAN;
        return true;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        RobotUsbDevice.FirmwareVersion firmwareVersion = new RobotUsbDevice.FirmwareVersion(read8(Register.FIRMWARE_REV));
        return String.format(Locale.getDefault(), "Modern Robotics Gyroscope %s", new Object[]{firmwareVersion});
    }

    public byte read8(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public void write8(Register register, byte b) {
        ((I2cDeviceSynch) this.deviceClient).write8((int) register.bVal, (int) b);
    }

    public short readShort(Register register) {
        return TypeConversion.byteArrayToShort(((I2cDeviceSynch) this.deviceClient).read(register.bVal, 2), ByteOrder.LITTLE_ENDIAN);
    }

    public void writeShort(Register register, short s) {
        ((I2cDeviceSynch) this.deviceClient).write((int) register.bVal, TypeConversion.shortToByteArray(s, ByteOrder.LITTLE_ENDIAN));
    }

    public void writeCommand(Command command) {
        ((I2cDeviceSynch) this.deviceClient).waitForWriteCompletions(I2cWaitControl.ATOMIC);
        write8(Register.COMMAND, command.bVal);
    }

    public Command readCommand() {
        return Command.fromByte(read8(Register.COMMAND));
    }

    public Set<Axis> getAngularVelocityAxes() {
        HashSet hashSet = new HashSet();
        hashSet.add(Axis.X);
        hashSet.add(Axis.Y);
        hashSet.add(Axis.Z);
        return hashSet;
    }

    public Set<Axis> getAngularOrientationAxes() {
        HashSet hashSet = new HashSet();
        hashSet.add(Axis.Z);
        return hashSet;
    }

    public AngularVelocity getAngularVelocity(AngleUnit angleUnit) {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.RAW_X_VAL.bVal, 6);
        short byteArrayToShort = TypeConversion.byteArrayToShort(readTimeStamped.data, 0, ByteOrder.LITTLE_ENDIAN);
        short byteArrayToShort2 = TypeConversion.byteArrayToShort(readTimeStamped.data, 2, ByteOrder.LITTLE_ENDIAN);
        short byteArrayToShort3 = TypeConversion.byteArrayToShort(readTimeStamped.data, 4, ByteOrder.LITTLE_ENDIAN);
        float f = this.degreesPerSecondPerDigit;
        return new AngularVelocity(AngleUnit.DEGREES, ((float) byteArrayToShort) * f, ((float) byteArrayToShort2) * f, ((float) byteArrayToShort3) * f, readTimeStamped.nanoTime).toAngleUnit(angleUnit);
    }

    public Orientation getAngularOrientation(AxesReference axesReference, AxesOrder axesOrder, AngleUnit angleUnit) {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.INTEGRATED_Z_VALUE.bVal, 2);
        return new Orientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES, AngleUnit.normalizeDegrees(degreesZFromIntegratedZ(TypeConversion.byteArrayToShort(readTimeStamped.data, ByteOrder.LITTLE_ENDIAN))), 0.0f, 0.0f, readTimeStamped.nanoTime).toAxesReference(axesReference).toAxesOrder(axesOrder).toAngleUnit(angleUnit);
    }

    public synchronized void setHeadingMode(HeadingMode headingMode2) {
        this.headingMode = headingMode2;
    }

    public HeadingMode getHeadingMode() {
        return this.headingMode;
    }

    public int rawX() {
        return readShort(Register.RAW_X_VAL);
    }

    public int rawY() {
        return readShort(Register.RAW_Y_VAL);
    }

    public int rawZ() {
        return readShort(Register.RAW_Z_VAL);
    }

    public int getZAxisOffset() {
        return readShort(Register.Z_AXIS_OFFSET);
    }

    public void setZAxisOffset(short s) {
        writeShort(Register.Z_AXIS_OFFSET, s);
    }

    public int getZAxisScalingCoefficient() {
        return TypeConversion.unsignedShortToInt(readShort(Register.Z_AXIS_SCALE_COEF));
    }

    public void setZAxisScalingCoefficient(int i) {
        writeShort(Register.Z_AXIS_SCALE_COEF, (short) i);
        this.degreesPerZAxisTick = 256.0f / ((float) i);
    }

    public int getIntegratedZValue() {
        return readShort(Register.INTEGRATED_Z_VALUE);
    }

    public synchronized int getHeading() {
        float normalize0359 = normalize0359(degreesZFromIntegratedZ(getIntegratedZValue()));
        if (this.headingMode == HeadingMode.HEADING_CARDINAL) {
            if (normalize0359 != 0.0f) {
                normalize0359 = Math.abs(normalize0359 - 360.0f);
            }
            return truncate(normalize0359);
        }
        return truncate(normalize0359);
    }

    /* access modifiers changed from: protected */
    public float normalize0359(float f) {
        float normalizeDegrees = AngleUnit.normalizeDegrees(f);
        return normalizeDegrees < 0.0f ? normalizeDegrees + 360.0f : normalizeDegrees;
    }

    /* access modifiers changed from: protected */
    public float degreesZFromIntegratedZ(int i) {
        return ((float) i) * this.degreesPerZAxisTick;
    }

    public void resetZAxisIntegrator() {
        writeCommand(Command.RESET_Z_AXIS);
    }

    public String status() {
        return String.format(Locale.getDefault(), "%s on %s", new Object[]{getDeviceName(), getConnectionInfo()});
    }

    public void calibrate() {
        writeCommand(Command.CALIBRATE);
    }

    public boolean isCalibrating() {
        return readCommand() == Command.CALIBRATE;
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }

    @Deprecated
    public double getRotationFraction() {
        notSupported();
        return LynxServoController.apiPositionFirst;
    }

    @Deprecated
    public MeasurementMode getMeasurementMode() {
        if (isCalibrating()) {
            return MeasurementMode.GYRO_CALIBRATING;
        }
        return MeasurementMode.GYRO_NORMAL;
    }

    /* access modifiers changed from: protected */
    public void notSupported() {
        throw new UnsupportedOperationException("This method is not supported for " + getDeviceName());
    }
}
