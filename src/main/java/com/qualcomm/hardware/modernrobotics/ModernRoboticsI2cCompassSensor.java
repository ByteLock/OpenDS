package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.robotcore.hardware.CompassSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.MagneticFlux;

@I2cDeviceType
@DeviceProperties(builtIn = true, description = "@string/mr_compass_description", name = "@string/mr_compass_name", xmlTag = "ModernRoboticsI2cCompassSensor")
public class ModernRoboticsI2cCompassSensor extends I2cDeviceSynchDevice<I2cDeviceSynch> implements CompassSensor, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(36);

    public enum Register {
        READ_WINDOW_FIRST(0),
        FIRMWARE_REV(0),
        MANUFACTURE_CODE(1),
        SENSOR_ID(2),
        COMMAND(3),
        HEADING(4),
        ACCELX(6),
        ACCELY(8),
        ACCELZ(10),
        MAGX(12),
        MAGY(14),
        MAGZ(16),
        READ_WINDOW_LAST(r8.bVal + 1),
        ACCELX_OFFSET(18),
        ACCELY_OFFSET(20),
        ACCELZ_OFFSET(22),
        MAGX_OFFSET(24),
        MAGY_OFFSET(26),
        MAGZ_OFFSET(28),
        MAG_TILT_COEFF(30),
        ACCEL_SCALE_COEFF(32),
        MAG_SCALE_COEFF_X(34),
        MAG_SCALE_COEFF_Y(36),
        UNKNOWN(-1);
        
        public byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Command {
        NORMAL(0),
        CALIBRATE_IRON(67),
        ACCEL_NULL_X(88),
        ACCEL_NULL_Y(89),
        ACCEL_NULL_Z(90),
        ACCEL_GAIN_ADJUST(71),
        MEASURE_TILT_UP(85),
        MEASURE_TILT_DOWN(68),
        WRITE_EEPROM(87),
        CALIBRATION_FAILED(70),
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

    public ModernRoboticsI2cCompassSensor(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true);
        setOptimalReadWindow();
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        registerArmingStateCallback(false);
        ((I2cDeviceSynch) this.deviceClient).engage();
    }

    /* access modifiers changed from: protected */
    public void setOptimalReadWindow() {
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(new I2cDeviceSynch.ReadWindow(Register.READ_WINDOW_FIRST.bVal, (Register.READ_WINDOW_LAST.bVal - Register.READ_WINDOW_FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT));
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        setMode(CompassSensor.CompassMode.MEASUREMENT_MODE);
        return true;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        RobotUsbDevice.FirmwareVersion firmwareVersion = new RobotUsbDevice.FirmwareVersion(read8(Register.FIRMWARE_REV));
        return String.format(Locale.getDefault(), "Modern Robotics Compass Sensor %s", new Object[]{firmwareVersion});
    }

    public byte read8(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public void write8(Register register, byte b) {
        ((I2cDeviceSynch) this.deviceClient).write8((int) register.bVal, (int) b);
    }

    public int readShort(Register register) {
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

    public Acceleration getAcceleration() {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.ACCELX.bVal, 6);
        ByteBuffer order = ByteBuffer.wrap(readTimeStamped.data).order(ByteOrder.LITTLE_ENDIAN);
        return Acceleration.fromGravity(((double) order.getShort()) * 0.001d, ((double) order.getShort()) * 0.001d, ((double) order.getShort()) * 0.001d, readTimeStamped.nanoTime);
    }

    public MagneticFlux getMagneticFlux() {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.MAGX.bVal, 6);
        ByteBuffer order = ByteBuffer.wrap(readTimeStamped.data).order(ByteOrder.LITTLE_ENDIAN);
        short s = order.getShort();
        return new MagneticFlux(((double) s) * 1.0E-4d, ((double) order.getShort()) * 1.0E-4d, ((double) order.getShort()) * 1.0E-4d, readTimeStamped.nanoTime);
    }

    public double getDirection() {
        return (double) readShort(Register.HEADING);
    }

    public String status() {
        return String.format(Locale.getDefault(), "%s on %s", new Object[]{getDeviceName(), getConnectionInfo()});
    }

    public boolean isCalibrating() {
        return readCommand() == Command.CALIBRATE_IRON;
    }

    public boolean calibrationFailed() {
        return readCommand() == Command.CALIBRATION_FAILED;
    }

    public void setMode(CompassSensor.CompassMode compassMode) {
        writeCommand(compassMode == CompassSensor.CompassMode.CALIBRATION_MODE ? Command.CALIBRATE_IRON : Command.NORMAL);
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }
}
