package com.qualcomm.hardware.bosch;

import com.qualcomm.robotcore.hardware.I2cAddr;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.MagneticFlux;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.Temperature;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

public interface BNO055IMU {
    public static final I2cAddr I2CADDR_ALTERNATE = I2cAddr.create7bit(41);
    public static final I2cAddr I2CADDR_DEFAULT = I2cAddr.create7bit(40);
    public static final I2cAddr I2CADDR_UNSPECIFIED = I2cAddr.zero();

    public interface AccelerationIntegrator {
        Acceleration getAcceleration();

        Position getPosition();

        Velocity getVelocity();

        void initialize(Parameters parameters, Position position, Velocity velocity);

        void update(Acceleration acceleration);
    }

    void close();

    Acceleration getAcceleration();

    Orientation getAngularOrientation();

    Orientation getAngularOrientation(AxesReference axesReference, AxesOrder axesOrder, org.firstinspires.ftc.robotcore.external.navigation.AngleUnit angleUnit);

    AngularVelocity getAngularVelocity();

    CalibrationStatus getCalibrationStatus();

    Acceleration getGravity();

    Acceleration getLinearAcceleration();

    MagneticFlux getMagneticFieldStrength();

    Acceleration getOverallAcceleration();

    Parameters getParameters();

    Position getPosition();

    Quaternion getQuaternionOrientation();

    SystemError getSystemError();

    SystemStatus getSystemStatus();

    Temperature getTemperature();

    Velocity getVelocity();

    boolean initialize(Parameters parameters);

    boolean isAccelerometerCalibrated();

    boolean isGyroCalibrated();

    boolean isMagnetometerCalibrated();

    boolean isSystemCalibrated();

    byte[] read(Register register, int i);

    byte read8(Register register);

    CalibrationData readCalibrationData();

    void startAccelerationIntegration(Position position, Velocity velocity, int i);

    void stopAccelerationIntegration();

    void write(Register register, byte[] bArr);

    void write8(Register register, int i);

    void writeCalibrationData(CalibrationData calibrationData);

    public static class Parameters implements Cloneable {
        public AccelBandwidth accelBandwidth = AccelBandwidth.HZ62_5;
        public AccelPowerMode accelPowerMode = AccelPowerMode.NORMAL;
        public AccelRange accelRange = AccelRange.G4;
        public AccelUnit accelUnit = AccelUnit.METERS_PERSEC_PERSEC;
        public AccelerationIntegrator accelerationIntegrationAlgorithm = null;
        public AngleUnit angleUnit = AngleUnit.RADIANS;
        public CalibrationData calibrationData = null;
        public String calibrationDataFile = null;
        public GyroBandwidth gyroBandwidth = GyroBandwidth.HZ32;
        public GyroPowerMode gyroPowerMode = GyroPowerMode.NORMAL;
        public GyroRange gyroRange = GyroRange.DPS2000;
        public I2cAddr i2cAddr = BNO055IMU.I2CADDR_DEFAULT;
        public boolean loggingEnabled = false;
        public String loggingTag = "AdaFruitIMU";
        public MagOpMode magOpMode = MagOpMode.REGULAR;
        public MagPowerMode magPowerMode = MagPowerMode.NORMAL;
        public MagRate magRate = MagRate.HZ10;
        public SensorMode mode = SensorMode.IMU;
        @Deprecated
        public PitchMode pitchMode = PitchMode.ANDROID;
        public TempUnit temperatureUnit = TempUnit.CELSIUS;
        public boolean useExternalCrystal = true;

        public Parameters clone() {
            try {
                Parameters parameters = (Parameters) super.clone();
                CalibrationData calibrationData2 = parameters.calibrationData;
                parameters.calibrationData = calibrationData2 == null ? null : calibrationData2.clone();
                return parameters;
            } catch (CloneNotSupportedException unused) {
                throw new RuntimeException("internal error: Parameters can't be cloned");
            }
        }
    }

    public static class CalibrationData implements Cloneable {
        public short dxAccel;
        public short dxGyro;
        public short dxMag;
        public short dyAccel;
        public short dyGyro;
        public short dyMag;
        public short dzAccel;
        public short dzGyro;
        public short dzMag;
        public short radiusAccel;
        public short radiusMag;

        public String serialize() {
            return SimpleGson.getInstance().toJson((Object) this);
        }

        public static CalibrationData deserialize(String str) {
            return (CalibrationData) SimpleGson.getInstance().fromJson(str, CalibrationData.class);
        }

        public CalibrationData clone() {
            try {
                return (CalibrationData) super.clone();
            } catch (CloneNotSupportedException unused) {
                throw new RuntimeException("internal error: CalibrationData can't be cloned");
            }
        }
    }

    public enum TempUnit {
        CELSIUS(0),
        FARENHEIT(1);
        
        public final byte bVal;

        private TempUnit(int i) {
            this.bVal = (byte) i;
        }

        public org.firstinspires.ftc.robotcore.external.navigation.TempUnit toTempUnit() {
            if (this == CELSIUS) {
                return org.firstinspires.ftc.robotcore.external.navigation.TempUnit.CELSIUS;
            }
            return org.firstinspires.ftc.robotcore.external.navigation.TempUnit.FARENHEIT;
        }

        public static TempUnit fromTempUnit(org.firstinspires.ftc.robotcore.external.navigation.TempUnit tempUnit) {
            if (tempUnit == org.firstinspires.ftc.robotcore.external.navigation.TempUnit.CELSIUS) {
                return CELSIUS;
            }
            if (tempUnit == org.firstinspires.ftc.robotcore.external.navigation.TempUnit.FARENHEIT) {
                return FARENHEIT;
            }
            throw new UnsupportedOperationException("TempUnit." + tempUnit + " is not supported by BNO055IMU");
        }
    }

    public enum AngleUnit {
        DEGREES(0),
        RADIANS(1);
        
        public final byte bVal;

        private AngleUnit(int i) {
            this.bVal = (byte) i;
        }

        public org.firstinspires.ftc.robotcore.external.navigation.AngleUnit toAngleUnit() {
            if (this == DEGREES) {
                return org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES;
            }
            return org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.RADIANS;
        }

        public static AngleUnit fromAngleUnit(org.firstinspires.ftc.robotcore.external.navigation.AngleUnit angleUnit) {
            if (angleUnit == org.firstinspires.ftc.robotcore.external.navigation.AngleUnit.DEGREES) {
                return DEGREES;
            }
            return RADIANS;
        }
    }

    public enum AccelUnit {
        METERS_PERSEC_PERSEC(0),
        MILLI_EARTH_GRAVITY(1);
        
        public final byte bVal;

        private AccelUnit(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum PitchMode {
        WINDOWS(0),
        ANDROID(1);
        
        public final byte bVal;

        private PitchMode(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum GyroRange {
        DPS2000(0),
        DPS1000(1),
        DPS500(2),
        DPS250(3),
        DPS125(4);
        
        public final byte bVal;

        private GyroRange(int i) {
            this.bVal = (byte) (i << 0);
        }
    }

    public enum GyroBandwidth {
        HZ523(0),
        HZ230(1),
        HZ116(2),
        HZ47(3),
        HZ23(4),
        HZ12(5),
        HZ64(6),
        HZ32(7);
        
        public final byte bVal;

        private GyroBandwidth(int i) {
            this.bVal = (byte) (i << 3);
        }
    }

    public enum GyroPowerMode {
        NORMAL(0),
        FAST(1),
        DEEP(2),
        SUSPEND(3),
        ADVANCED(4);
        
        public final byte bVal;

        private GyroPowerMode(int i) {
            this.bVal = (byte) (i << 0);
        }
    }

    public enum AccelRange {
        G2(0),
        G4(1),
        G8(2),
        G16(3);
        
        public final byte bVal;

        private AccelRange(int i) {
            this.bVal = (byte) (i << 0);
        }
    }

    public enum AccelBandwidth {
        HZ7_81(0),
        HZ15_63(1),
        HZ31_25(2),
        HZ62_5(3),
        HZ125(4),
        HZ250(5),
        HZ500(6),
        HZ1000(7);
        
        public final byte bVal;

        private AccelBandwidth(int i) {
            this.bVal = (byte) (i << 2);
        }
    }

    public enum AccelPowerMode {
        NORMAL(0),
        SUSPEND(1),
        LOW1(2),
        STANDBY(3),
        LOW2(4),
        DEEP(5);
        
        public final byte bVal;

        private AccelPowerMode(int i) {
            this.bVal = (byte) (i << 5);
        }
    }

    public enum MagRate {
        HZ2(0),
        HZ6(1),
        HZ8(2),
        HZ10(3),
        HZ15(4),
        HZ20(5),
        HZ25(6),
        HZ30(7);
        
        public final byte bVal;

        private MagRate(int i) {
            this.bVal = (byte) (i << 0);
        }
    }

    public enum MagOpMode {
        LOW(0),
        REGULAR(1),
        ENHANCED(2),
        HIGH(3);
        
        public final byte bVal;

        private MagOpMode(int i) {
            this.bVal = (byte) (i << 3);
        }
    }

    public enum MagPowerMode {
        NORMAL(0),
        SLEEP(1),
        SUSPEND(2),
        FORCE(3);
        
        public final byte bVal;

        private MagPowerMode(int i) {
            this.bVal = (byte) (i << 5);
        }
    }

    public enum SystemStatus {
        UNKNOWN(-1),
        IDLE(0),
        SYSTEM_ERROR(1),
        INITIALIZING_PERIPHERALS(2),
        SYSTEM_INITIALIZATION(3),
        SELF_TEST(4),
        RUNNING_FUSION(5),
        RUNNING_NO_FUSION(6);
        
        public final byte bVal;

        private SystemStatus(int i) {
            this.bVal = (byte) i;
        }

        public static SystemStatus from(int i) {
            for (SystemStatus systemStatus : values()) {
                if (systemStatus.bVal == i) {
                    return systemStatus;
                }
            }
            return UNKNOWN;
        }

        public String toShortString() {
            switch (C06651.$SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus[ordinal()]) {
                case 1:
                    return "idle";
                case 2:
                    return "syserr";
                case 3:
                    return "periph";
                case 4:
                    return "sysinit";
                case 5:
                    return "selftest";
                case 6:
                    return "fusion";
                case 7:
                    return "running";
                default:
                    return "unk";
            }
        }
    }

    public enum SystemError {
        UNKNOWN(-1),
        NO_ERROR(0),
        PERIPHERAL_INITIALIZATION_ERROR(1),
        SYSTEM_INITIALIZATION_ERROR(2),
        SELF_TEST_FAILED(3),
        REGISTER_MAP_OUT_OF_RANGE(4),
        REGISTER_MAP_ADDRESS_OUT_OF_RANGE(5),
        REGISTER_MAP_WRITE_ERROR(6),
        LOW_POWER_MODE_NOT_AVAILABLE(7),
        ACCELEROMETER_POWER_MODE_NOT_AVAILABLE(8),
        FUSION_CONFIGURATION_ERROR(9),
        SENSOR_CONFIGURATION_ERROR(10);
        
        public final byte bVal;

        private SystemError(int i) {
            this.bVal = (byte) i;
        }

        public static SystemError from(int i) {
            for (SystemError systemError : values()) {
                if (systemError.bVal == i) {
                    return systemError;
                }
            }
            return UNKNOWN;
        }
    }

    public static class CalibrationStatus {
        public final byte calibrationStatus;

        public CalibrationStatus(int i) {
            this.calibrationStatus = (byte) i;
        }

        public String toString() {
            return String.format(Locale.getDefault(), "s%d", new Object[]{Integer.valueOf((this.calibrationStatus >> 6) & 3)}) + " " + String.format(Locale.getDefault(), "g%d", new Object[]{Integer.valueOf((this.calibrationStatus >> 4) & 3)}) + " " + String.format(Locale.getDefault(), "a%d", new Object[]{Integer.valueOf((this.calibrationStatus >> 2) & 3)}) + " " + String.format(Locale.getDefault(), "m%d", new Object[]{Integer.valueOf((this.calibrationStatus >> 0) & 3)});
        }
    }

    public enum SensorMode {
        CONFIG(0),
        ACCONLY(1),
        MAGONLY(2),
        GYRONLY(3),
        ACCMAG(4),
        ACCGYRO(5),
        MAGGYRO(6),
        AMG(7),
        IMU(8),
        COMPASS(9),
        M4G(10),
        NDOF_FMC_OFF(11),
        NDOF(12),
        DISABLED(-1);
        
        public final byte bVal;

        private SensorMode(int i) {
            this.bVal = (byte) i;
        }

        public boolean isFusionMode() {
            int i = C06651.$SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode[ordinal()];
            return i == 1 || i == 2 || i == 3 || i == 4 || i == 5;
        }
    }

    /* renamed from: com.qualcomm.hardware.bosch.BNO055IMU$1 */
    static /* synthetic */ class C06651 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode;
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus;

        /* JADX WARNING: Can't wrap try/catch for region: R(26:0|(2:1|2)|3|(2:5|6)|7|(2:9|10)|11|(2:13|14)|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(27:0|(2:1|2)|3|(2:5|6)|7|9|10|11|(2:13|14)|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(29:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|(2:17|18)|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(30:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Can't wrap try/catch for region: R(31:0|1|2|3|5|6|7|9|10|11|13|14|15|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Code restructure failed: missing block: B:37:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x004f */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0059 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0063 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x006d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0077 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0082 */
        static {
            /*
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode[] r0 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode = r0
                r1 = 1
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode r2 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.IMU     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode r3 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.COMPASS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode r4 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.M4G     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                r3 = 4
                int[] r4 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode r5 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.NDOF_FMC_OFF     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4[r5] = r3     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                r4 = 5
                int[] r5 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SensorMode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.hardware.bosch.BNO055IMU$SensorMode r6 = com.qualcomm.hardware.bosch.BNO055IMU.SensorMode.NDOF     // Catch:{ NoSuchFieldError -> 0x003e }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r5[r6] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus[] r5 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.values()
                int r5 = r5.length
                int[] r5 = new int[r5]
                $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus = r5
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r6 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.IDLE     // Catch:{ NoSuchFieldError -> 0x004f }
                int r6 = r6.ordinal()     // Catch:{ NoSuchFieldError -> 0x004f }
                r5[r6] = r1     // Catch:{ NoSuchFieldError -> 0x004f }
            L_0x004f:
                int[] r1 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x0059 }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r5 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.SYSTEM_ERROR     // Catch:{ NoSuchFieldError -> 0x0059 }
                int r5 = r5.ordinal()     // Catch:{ NoSuchFieldError -> 0x0059 }
                r1[r5] = r0     // Catch:{ NoSuchFieldError -> 0x0059 }
            L_0x0059:
                int[] r0 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x0063 }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r1 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.INITIALIZING_PERIPHERALS     // Catch:{ NoSuchFieldError -> 0x0063 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0063 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0063 }
            L_0x0063:
                int[] r0 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x006d }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r1 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.SYSTEM_INITIALIZATION     // Catch:{ NoSuchFieldError -> 0x006d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006d }
                r0[r1] = r3     // Catch:{ NoSuchFieldError -> 0x006d }
            L_0x006d:
                int[] r0 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x0077 }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r1 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.SELF_TEST     // Catch:{ NoSuchFieldError -> 0x0077 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0077 }
                r0[r1] = r4     // Catch:{ NoSuchFieldError -> 0x0077 }
            L_0x0077:
                int[] r0 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x0082 }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r1 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.RUNNING_FUSION     // Catch:{ NoSuchFieldError -> 0x0082 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0082 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0082 }
            L_0x0082:
                int[] r0 = $SwitchMap$com$qualcomm$hardware$bosch$BNO055IMU$SystemStatus     // Catch:{ NoSuchFieldError -> 0x008d }
                com.qualcomm.hardware.bosch.BNO055IMU$SystemStatus r1 = com.qualcomm.hardware.bosch.BNO055IMU.SystemStatus.RUNNING_NO_FUSION     // Catch:{ NoSuchFieldError -> 0x008d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x008d }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x008d }
            L_0x008d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.bosch.BNO055IMU.C06651.<clinit>():void");
        }
    }

    public enum Register {
        PAGE_ID(7),
        CHIP_ID(0),
        ACC_ID(1),
        MAG_ID(2),
        GYR_ID(3),
        SW_REV_ID_LSB(4),
        SW_REV_ID_MSB(5),
        BL_REV_ID(6),
        ACC_DATA_X_LSB(8),
        ACC_DATA_X_MSB(9),
        ACC_DATA_Y_LSB(10),
        ACC_DATA_Y_MSB(11),
        ACC_DATA_Z_LSB(12),
        ACC_DATA_Z_MSB(13),
        MAG_DATA_X_LSB(14),
        MAG_DATA_X_MSB(15),
        MAG_DATA_Y_LSB(16),
        MAG_DATA_Y_MSB(17),
        MAG_DATA_Z_LSB(18),
        MAG_DATA_Z_MSB(19),
        GYR_DATA_X_LSB(20),
        GYR_DATA_X_MSB(21),
        GYR_DATA_Y_LSB(22),
        GYR_DATA_Y_MSB(23),
        GYR_DATA_Z_LSB(24),
        GYR_DATA_Z_MSB(25),
        EUL_H_LSB(26),
        EUL_H_MSB(27),
        EUL_R_LSB(28),
        EUL_R_MSB(29),
        EUL_P_LSB(30),
        EUL_P_MSB(31),
        QUA_DATA_W_LSB(32),
        QUA_DATA_W_MSB(33),
        QUA_DATA_X_LSB(34),
        QUA_DATA_X_MSB(35),
        QUA_DATA_Y_LSB(36),
        QUA_DATA_Y_MSB(37),
        QUA_DATA_Z_LSB(38),
        QUA_DATA_Z_MSB(39),
        LIA_DATA_X_LSB(40),
        LIA_DATA_X_MSB(41),
        LIA_DATA_Y_LSB(42),
        LIA_DATA_Y_MSB(43),
        LIA_DATA_Z_LSB(44),
        LIA_DATA_Z_MSB(45),
        GRV_DATA_X_LSB(46),
        GRV_DATA_X_MSB(47),
        GRV_DATA_Y_LSB(48),
        GRV_DATA_Y_MSB(49),
        GRV_DATA_Z_LSB(50),
        GRV_DATA_Z_MSB(51),
        TEMP(52),
        CALIB_STAT(53),
        SELFTEST_RESULT(54),
        INTR_STAT(55),
        SYS_CLK_STAT(56),
        SYS_STAT(57),
        SYS_ERR(58),
        UNIT_SEL(59),
        DATA_SELECT(60),
        OPR_MODE(61),
        PWR_MODE(62),
        SYS_TRIGGER(63),
        TEMP_SOURCE(64),
        AXIS_MAP_CONFIG(65),
        AXIS_MAP_SIGN(66),
        SIC_MATRIX_0_LSB(67),
        SIC_MATRIX_0_MSB(68),
        SIC_MATRIX_1_LSB(69),
        SIC_MATRIX_1_MSB(70),
        SIC_MATRIX_2_LSB(71),
        SIC_MATRIX_2_MSB(72),
        SIC_MATRIX_3_LSB(73),
        SIC_MATRIX_3_MSB(74),
        SIC_MATRIX_4_LSB(75),
        SIC_MATRIX_4_MSB(76),
        SIC_MATRIX_5_LSB(77),
        SIC_MATRIX_5_MSB(78),
        SIC_MATRIX_6_LSB(79),
        SIC_MATRIX_6_MSB(80),
        SIC_MATRIX_7_LSB(81),
        SIC_MATRIX_7_MSB(82),
        SIC_MATRIX_8_LSB(83),
        SIC_MATRIX_8_MSB(84),
        ACC_OFFSET_X_LSB(85),
        ACC_OFFSET_X_MSB(86),
        ACC_OFFSET_Y_LSB(87),
        ACC_OFFSET_Y_MSB(88),
        ACC_OFFSET_Z_LSB(89),
        ACC_OFFSET_Z_MSB(90),
        MAG_OFFSET_X_LSB(91),
        MAG_OFFSET_X_MSB(92),
        MAG_OFFSET_Y_LSB(93),
        MAG_OFFSET_Y_MSB(94),
        MAG_OFFSET_Z_LSB(95),
        MAG_OFFSET_Z_MSB(96),
        GYR_OFFSET_X_LSB(97),
        GYR_OFFSET_X_MSB(98),
        GYR_OFFSET_Y_LSB(99),
        GYR_OFFSET_Y_MSB(100),
        GYR_OFFSET_Z_LSB(101),
        GYR_OFFSET_Z_MSB(102),
        ACC_RADIUS_LSB(103),
        ACC_RADIUS_MSB(104),
        MAG_RADIUS_LSB(105),
        MAG_RADIUS_MSB(106),
        ACC_CONFIG(8),
        MAG_CONFIG(9),
        GYR_CONFIG_0(10),
        GYR_CONFIG_1(11),
        ACC_SLEEP_CONFIG(12),
        GYR_SLEEP_CONFIG(13),
        INT_MSK(15),
        INT_EN(16),
        ACC_AM_THRES(17),
        ACC_INT_SETTINGS(18),
        ACC_HG_DURATION(19),
        ACC_HG_THRES(20),
        ACC_NM_THRES(21),
        ACC_NM_SET(22),
        GRYO_INT_SETTING(23),
        GRYO_HR_X_SET(24),
        GRYO_DUR_X(25),
        GRYO_HR_Y_SET(26),
        GRYO_DUR_Y(27),
        GRYO_HR_Z_SET(28),
        GRYO_DUR_Z(29),
        GRYO_AM_THRES(30),
        GRYO_AM_SET(31),
        UNIQUE_ID_FIRST(80),
        UNIQUE_ID_LAST(95);
        
        public final byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }
}
