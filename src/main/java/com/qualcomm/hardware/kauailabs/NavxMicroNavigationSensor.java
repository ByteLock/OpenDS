package com.qualcomm.hardware.kauailabs;

import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.IntegratingGyroscope;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtConstants;

@I2cDeviceType
@DeviceProperties(builtIn = true, description = "@string/navx_micro_description", name = "@string/navx_micro_name", xmlTag = "KauaiLabsNavxMicro")
public class NavxMicroNavigationSensor extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynch, Parameters> implements Gyroscope, IntegratingGyroscope, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create7bit(50);
    protected static final I2cDeviceSynch.ReadWindow lowerWindow = newWindow(Register.SENSOR_STATUS_L, Register.LINEAR_ACC_Z_H);
    protected static final I2cDeviceSynch.ReadMode readMode = I2cDeviceSynch.ReadMode.REPEAT;
    protected static final I2cDeviceSynch.ReadWindow upperWindow = newWindow(Register.GYRO_X_L, Register.MAG_Z_H);
    public final int NAVX_WRITE_COMMAND_BIT = 128;
    protected float gyroScaleFactor;

    /* access modifiers changed from: protected */
    public float shortToSignedHundredths(short s) {
        return ((float) s) * 0.01f;
    }

    protected static I2cDeviceSynch.ReadWindow newWindow(Register register, Register register2) {
        return new I2cDeviceSynch.ReadWindow(register.bVal, register2.bVal - register.bVal, readMode);
    }

    public NavxMicroNavigationSensor(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true, new Parameters());
        setReadWindow();
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        registerArmingStateCallback(true);
        ((I2cDeviceSynch) this.deviceClient).engage();
    }

    /* access modifiers changed from: protected */
    public void setReadWindow() {
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(lowerWindow);
    }

    /* access modifiers changed from: protected */
    public boolean internalInitialize(Parameters parameters) {
        this.parameters = parameters.clone();
        write8(Register.UPDATE_RATE_HZ, (byte) parameters.updateRate);
        write8(Register.INTEGRATION_CTL, IntegrationControl.RESET_ALL.bVal);
        this.gyroScaleFactor = ((float) readShort(Register.GYRO_FSR_DPS_L)) / 32768.0f;
        return true;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Other;
    }

    public String getDeviceName() {
        return String.format("Kauai Labs navX-Micro Gyro %s", new Object[]{getFirmwareVersion()});
    }

    public RobotUsbDevice.FirmwareVersion getFirmwareVersion() {
        return new RobotUsbDevice.FirmwareVersion(read8(Register.FW_VER_MAJOR), read8(Register.FW_VER_MINOR));
    }

    /* access modifiers changed from: protected */
    public void ensureReadWindow(I2cDeviceSynch.ReadWindow readWindow) {
        I2cDeviceSynch.ReadWindow readWindow2 = lowerWindow;
        if (!readWindow2.containsWithSameMode(readWindow)) {
            readWindow2 = upperWindow;
            if (!readWindow2.containsWithSameMode(readWindow)) {
                readWindow2 = readWindow;
            }
        }
        ((I2cDeviceSynch) this.deviceClient).ensureReadWindow(readWindow, readWindow2);
    }

    public synchronized TimestampedData readTimeStamped(Register register, int i) {
        ensureReadWindow(new I2cDeviceSynch.ReadWindow(register.bVal, i, readMode));
        return ((I2cDeviceSynch) this.deviceClient).readTimeStamped(register.bVal, i);
    }

    public byte read8(Register register) {
        return readTimeStamped(register, 1).data[0];
    }

    public short readShort(Register register) {
        return TypeConversion.byteArrayToShort(readTimeStamped(register, 2).data, ByteOrder.LITTLE_ENDIAN);
    }

    public float readSignedHundredthsFloat(Register register) {
        return shortToSignedHundredths(readShort(register));
    }

    public void write8(Register register, byte b) {
        ((I2cDeviceSynch) this.deviceClient).write8((int) register.bVal | FtConstants.DCD, (int) b);
    }

    public void writeShort(Register register, short s) {
        ((I2cDeviceSynch) this.deviceClient).write((int) register.bVal | FtConstants.DCD, TypeConversion.shortToByteArray(s, ByteOrder.LITTLE_ENDIAN));
    }

    public boolean isCalibrating() {
        return !((read8(Register.SENSOR_STATUS_H) & CalibrationStatus.IMU_CAL_MASK.bVal) == CalibrationStatus.IMU_CAL_COMPLETE.bVal);
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
        hashSet.add(Axis.X);
        hashSet.add(Axis.Y);
        hashSet.add(Axis.Z);
        return hashSet;
    }

    public Orientation getAngularOrientation(AxesReference axesReference, AxesOrder axesOrder, AngleUnit angleUnit) {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.YAW_L.bVal, 6);
        return new Orientation(AxesReference.INTRINSIC, AxesOrder.ZYX, AngleUnit.DEGREES, -shortToSignedHundredths(TypeConversion.byteArrayToShort(readTimeStamped.data, 0, ByteOrder.LITTLE_ENDIAN)), shortToSignedHundredths(TypeConversion.byteArrayToShort(readTimeStamped.data, 2, ByteOrder.LITTLE_ENDIAN)), shortToSignedHundredths(TypeConversion.byteArrayToShort(readTimeStamped.data, 4, ByteOrder.LITTLE_ENDIAN)), readTimeStamped.nanoTime).toAxesReference(axesReference).toAxesOrder(axesOrder).toAngleUnit(angleUnit);
    }

    public AngularVelocity getAngularVelocity(AngleUnit angleUnit) {
        TimestampedData readTimeStamped = ((I2cDeviceSynch) this.deviceClient).readTimeStamped(Register.GYRO_X_L.bVal, 6);
        return new AngularVelocity(AngleUnit.DEGREES, ((float) TypeConversion.byteArrayToShort(readTimeStamped.data, 0, ByteOrder.LITTLE_ENDIAN)) * this.gyroScaleFactor, ((float) TypeConversion.byteArrayToShort(readTimeStamped.data, 2, ByteOrder.LITTLE_ENDIAN)) * this.gyroScaleFactor, ((float) TypeConversion.byteArrayToShort(readTimeStamped.data, 4, ByteOrder.LITTLE_ENDIAN)) * this.gyroScaleFactor, readTimeStamped.nanoTime).toAngleUnit(angleUnit);
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }

    public static class Parameters implements Cloneable {
        public int updateRate = 50;

        public int realizedUpdateRate() {
            return 200 / (200 / this.updateRate);
        }

        public Parameters clone() {
            try {
                return (Parameters) super.clone();
            } catch (CloneNotSupportedException unused) {
                throw new RuntimeException("internal error: Parameters can't be cloned");
            }
        }
    }

    public enum Register {
        FIRST(0),
        WHOAMI(0),
        HW_REV(1),
        FW_VER_MAJOR(2),
        FW_VER_MINOR(3),
        UPDATE_RATE_HZ(4),
        ACCEL_FSR_G(5),
        GYRO_FSR_DPS_L(6),
        GYRO_FSR_DPS_H(7),
        OP_STATUS(8),
        CAL_STATUS(9),
        SELFTEST_STATUS(10),
        CAPABILITY_FLAGS_L(11),
        CAPABILITY_FLAGS_H(12),
        SENSOR_STATUS_L(16),
        SENSOR_STATUS_H(17),
        TIMESTAMP_L_L(18),
        TIMESTAMP_L_H(19),
        TIMESTAMP_H_L(20),
        TIMESTAMP_H_H(21),
        YAW_L(22),
        YAW_H(23),
        ROLL_L(24),
        ROLL_H(25),
        PITCH_L(26),
        PITCH_H(27),
        HEADING_L(28),
        HEADING_H(29),
        FUSED_HEADING_L(30),
        FUSED_HEADING_H(31),
        ALTITUDE_I_L(32),
        ALTITUDE_I_H(33),
        ALTITUDE_D_L(34),
        ALTITUDE_D_H(35),
        LINEAR_ACC_X_L(36),
        LINEAR_ACC_X_H(37),
        LINEAR_ACC_Y_L(38),
        LINEAR_ACC_Y_H(39),
        LINEAR_ACC_Z_L(40),
        LINEAR_ACC_Z_H(41),
        QUAT_W_L(42),
        QUAT_W_H(43),
        QUAT_X_L(44),
        QUAT_X_H(45),
        QUAT_Y_L(46),
        QUAT_Y_H(47),
        QUAT_Z_L(48),
        QUAT_Z_H(49),
        MPU_TEMP_C_L(50),
        MPU_TEMP_C_H(51),
        GYRO_X_L(52),
        GYRO_X_H(53),
        GYRO_Y_L(54),
        GYRO_Y_H(55),
        GYRO_Z_L(56),
        GYRO_Z_H(57),
        ACC_X_L(58),
        ACC_X_H(59),
        ACC_Y_L(60),
        ACC_Y_H(61),
        ACC_Z_L(62),
        ACC_Z_H(63),
        MAG_X_L(64),
        MAG_X_H(65),
        MAG_Y_L(66),
        MAG_Y_H(67),
        MAG_Z_L(68),
        MAG_Z_H(69),
        PRESSURE_IL(70),
        PRESSURE_IH(71),
        PRESSURE_DL(72),
        PRESSURE_DH(73),
        PRESSURE_TEMP_L(74),
        PRESSURE_TEMP_H(75),
        YAW_OFFSET_L(76),
        YAW_OFFSET_H(77),
        QUAT_OFFSET_W_L(78),
        QUAT_OFFSET_W_H(79),
        QUAT_OFFSET_X_L(80),
        QUAT_OFFSET_X_H(81),
        QUAT_OFFSET_Y_L(82),
        QUAT_OFFSET_Y_H(83),
        QUAT_OFFSET_Z_L(84),
        QUAT_OFFSET_Z_H(85),
        INTEGRATION_CTL(86),
        PAD_UNUSED(87),
        VEL_X_I_L(88),
        VEL_X_I_H(89),
        VEL_X_D_L(90),
        VEL_X_D_H(91),
        VEL_Y_I_L(92),
        VEL_Y_I_H(93),
        VEL_Y_D_L(94),
        VEL_Y_D_H(95),
        VEL_Z_I_L(96),
        VEL_Z_I_H(97),
        VEL_Z_D_L(98),
        VEL_Z_D_H(99),
        DISP_X_I_L(100),
        DISP_X_I_H(101),
        DISP_X_D_L(102),
        DISP_X_D_H(103),
        DISP_Y_I_L(104),
        DISP_Y_I_H(105),
        DISP_Y_D_L(106),
        DISP_Y_D_H(107),
        DISP_Z_I_L(108),
        DISP_Z_I_H(109),
        DISP_Z_D_L(110),
        DISP_Z_D_H(111),
        LAST(r2.bVal),
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

    public enum OpStatus {
        INITIALIZING(0),
        SELFTEST_IN_PROGRESS(1),
        ERROR(2),
        IMU_AUTOCAL_IN_PROGRESS(3),
        NORMAL(4);
        
        public byte bVal;

        private OpStatus(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum SensorStatus {
        MOVING(1),
        YAW_STABLE(2),
        MAG_DISTURBANCE(4),
        ALTITUDE_VALID(8),
        SEALEVEL_PRESS_SET(16),
        FUSED_HEADING_VALID(32);
        
        public byte bVal;

        private SensorStatus(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum CalibrationStatus {
        IMU_CAL_INPROGRESS(0),
        IMU_CAL_ACCUMULATE(1),
        IMU_CAL_COMPLETE(2),
        IMU_CAL_MASK(3),
        MAG_CAL_COMPLETE(4),
        BARO_CAL_COMPLETE(8);
        
        public byte bVal;

        private CalibrationStatus(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum SelfTestStatus {
        COMPLETE(128),
        RESULT_GYRO_PASSED(1),
        RESULT_ACCEL_PASSED(2),
        RESULT_MAG_PASSED(4),
        RESULT_BARO_PASSED(8);
        
        public byte bVal;

        private SelfTestStatus(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum IntegrationControl {
        RESET_VEL_X(1),
        RESET_VEL_Y(2),
        RESET_VEL_Z(4),
        RESET_DISP_X(8),
        RESET_DISP_Y(16),
        RESET_DISP_Z(32),
        RESET_YAW(128),
        RESET_ALL(r0.bVal | r1.bVal | r4.bVal | r6.bVal | r8.bVal | r11.bVal | r12.bVal);
        
        public byte bVal;

        public byte bitor(IntegrationControl integrationControl) {
            return (byte) (integrationControl.bVal | this.bVal);
        }

        public byte bitor(byte b) {
            return (byte) (b | this.bVal);
        }

        private IntegrationControl(int i) {
            this.bVal = (byte) i;
        }
    }
}
