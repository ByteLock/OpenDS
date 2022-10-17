package com.qualcomm.hardware.bosch;

import android.util.Log;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.Gyroscope;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.IntegratingGyroscope;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.TypeConversion;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.external.navigation.MagneticFlux;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Quaternion;
import org.firstinspires.ftc.robotcore.external.navigation.Temperature;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public abstract class BNO055IMUImpl extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynch, BNO055IMU.Parameters> implements BNO055IMU, Gyroscope, IntegratingGyroscope, I2cAddrConfig, OpModeManagerNotifier.Notifications {
    public static final byte bCHIP_ID_VALUE = -96;
    protected static final I2cDeviceSynch.ReadWindow lowerWindow = newWindow(BNO055IMU.Register.CHIP_ID, BNO055IMU.Register.EUL_H_LSB);
    protected static final int msAwaitChipId = 2000;
    protected static final int msAwaitSelfTest = 2000;
    protected static final int msExtra = 50;
    protected static final I2cDeviceSynch.ReadMode readMode = I2cDeviceSynch.ReadMode.REPEAT;
    protected static final I2cDeviceSynch.ReadWindow upperWindow = newWindow(BNO055IMU.Register.EUL_H_LSB, BNO055IMU.Register.TEMP);
    protected BNO055IMU.AccelerationIntegrator accelerationAlgorithm;
    protected ExecutorService accelerationMananger;
    protected BNO055IMU.SensorMode currentMode;
    protected final Object dataLock = new Object();
    protected float delayScale = 1.0f;
    protected final Object startStopLock = new Object();

    public abstract String getDeviceName();

    /* access modifiers changed from: protected */
    public float getFluxScale() {
        return 1.6E7f;
    }

    public abstract HardwareDevice.Manufacturer getManufacturer();

    public void onOpModePreInit(OpMode opMode) {
    }

    public void onOpModePreStart(OpMode opMode) {
    }

    public /* bridge */ /* synthetic */ BNO055IMU.Parameters getParameters() {
        return (BNO055IMU.Parameters) super.getParameters();
    }

    public /* bridge */ /* synthetic */ boolean initialize(BNO055IMU.Parameters parameters) {
        return super.initialize(parameters);
    }

    public static class ImuNotInitializedException extends RuntimeException {
        public ImuNotInitializedException() {
            super("The IMU was not initialized");
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(7:5|6|7|8|9|10|11) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0025 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static boolean imuIsPresent(com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple r7, boolean r8) {
        /*
            java.lang.String r0 = "BNO055"
            java.lang.String r1 = "Suppressing I2C warnings while we check for a BNO055 IMU"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)
            r1 = 1
            com.qualcomm.robotcore.hardware.I2cWarningManager.suppressNewProblemDeviceWarnings(r1)
            r2 = 0
            com.qualcomm.hardware.bosch.BNO055IMU$Register r3 = com.qualcomm.hardware.bosch.BNO055IMU.Register.CHIP_ID     // Catch:{ all -> 0x0048 }
            byte r3 = r3.bVal     // Catch:{ all -> 0x0048 }
            byte r3 = r7.read8(r3)     // Catch:{ all -> 0x0048 }
            r4 = -96
            if (r3 == r4) goto L_0x0034
            if (r8 == 0) goto L_0x0034
            com.qualcomm.robotcore.hardware.I2cWaitControl r8 = com.qualcomm.robotcore.hardware.I2cWaitControl.WRITTEN     // Catch:{ all -> 0x0048 }
            r7.waitForWriteCompletions(r8)     // Catch:{ all -> 0x0048 }
            r5 = 650(0x28a, double:3.21E-321)
            java.lang.Thread.sleep(r5)     // Catch:{ InterruptedException -> 0x0025 }
            goto L_0x002c
        L_0x0025:
            java.lang.Thread r8 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0048 }
            r8.interrupt()     // Catch:{ all -> 0x0048 }
        L_0x002c:
            com.qualcomm.hardware.bosch.BNO055IMU$Register r8 = com.qualcomm.hardware.bosch.BNO055IMU.Register.CHIP_ID     // Catch:{ all -> 0x0048 }
            byte r8 = r8.bVal     // Catch:{ all -> 0x0048 }
            byte r3 = r7.read8(r8)     // Catch:{ all -> 0x0048 }
        L_0x0034:
            if (r3 != r4) goto L_0x003f
            java.lang.String r7 = "Found BNO055 IMU"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r7)     // Catch:{ all -> 0x0048 }
            com.qualcomm.robotcore.hardware.I2cWarningManager.suppressNewProblemDeviceWarnings(r2)
            return r1
        L_0x003f:
            java.lang.String r7 = "No BNO055 IMU found"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r7)     // Catch:{ all -> 0x0048 }
            com.qualcomm.robotcore.hardware.I2cWarningManager.suppressNewProblemDeviceWarnings(r2)
            return r2
        L_0x0048:
            r7 = move-exception
            com.qualcomm.robotcore.hardware.I2cWarningManager.suppressNewProblemDeviceWarnings(r2)
            throw r7
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.bosch.BNO055IMUImpl.imuIsPresent(com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple, boolean):boolean");
    }

    protected static I2cDeviceSynch.ReadWindow newWindow(BNO055IMU.Register register, BNO055IMU.Register register2) {
        return new I2cDeviceSynch.ReadWindow(register.bVal, register2.bVal - register.bVal, readMode);
    }

    /* access modifiers changed from: protected */
    public void throwIfNotInitialized() {
        if (((BNO055IMU.Parameters) this.parameters).mode == BNO055IMU.SensorMode.DISABLED) {
            throw new ImuNotInitializedException();
        }
    }

    public BNO055IMUImpl(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true, disabledParameters());
        ((I2cDeviceSynch) this.deviceClient).setReadWindow(lowerWindow);
        ((I2cDeviceSynch) this.deviceClient).engage();
        this.currentMode = null;
        this.accelerationAlgorithm = new NaiveAccelerationIntegrator();
        this.accelerationMananger = null;
        registerArmingStateCallback(false);
    }

    protected static BNO055IMU.Parameters disabledParameters() {
        BNO055IMU.Parameters parameters = new BNO055IMU.Parameters();
        parameters.mode = BNO055IMU.SensorMode.DISABLED;
        return parameters;
    }

    public void resetDeviceConfigurationForOpMode() {
        stopAccelerationIntegration();
        this.parameters = disabledParameters();
        super.resetDeviceConfigurationForOpMode();
    }

    public void onOpModePostStop(OpMode opMode) {
        stopAccelerationIntegration();
    }

    public I2cAddr getI2cAddress() {
        return ((BNO055IMU.Parameters) this.parameters).i2cAddr;
    }

    public void setI2cAddress(I2cAddr i2cAddr) {
        ((BNO055IMU.Parameters) this.parameters).i2cAddr = i2cAddr;
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public boolean internalInitialize(BNO055IMU.Parameters parameters) {
        if (parameters.mode == BNO055IMU.SensorMode.DISABLED) {
            return true;
        }
        BNO055IMU.Parameters parameters2 = (BNO055IMU.Parameters) this.parameters;
        this.parameters = parameters.clone();
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(parameters.i2cAddr);
        if (internalInitializeOnce(parameters.mode.isFusionMode() ? BNO055IMU.SystemStatus.RUNNING_FUSION : BNO055IMU.SystemStatus.RUNNING_NO_FUSION)) {
            this.isInitialized = true;
            return true;
        }
        log_e("IMU initialization failed", new Object[0]);
        this.parameters = parameters2;
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean internalInitializeOnce(BNO055IMU.SystemStatus systemStatus) {
        if (BNO055IMU.SensorMode.CONFIG != ((BNO055IMU.Parameters) this.parameters).mode) {
            ElapsedTime elapsedTime = new ElapsedTime();
            if (((BNO055IMU.Parameters) this.parameters).accelerationIntegrationAlgorithm != null) {
                this.accelerationAlgorithm = ((BNO055IMU.Parameters) this.parameters).accelerationIntegrationAlgorithm;
            }
            if (!imuIsPresent(this.deviceClient, true)) {
                log_e("IMU appears to not be present", new Object[0]);
                return false;
            }
            setSensorMode(BNO055IMU.SensorMode.CONFIG);
            I2cWarningManager.suppressNewProblemDeviceWarnings(true);
            try {
                elapsedTime.reset();
                write8(BNO055IMU.Register.SYS_TRIGGER, 32, I2cWaitControl.WRITTEN);
                delay(SoundPlayer.msSoundTransmissionFreshness);
                RobotLog.m60vv("IMU", "Now polling until IMU comes out of reset. It is normal to see I2C failures below");
                do {
                    if (!isStopRequested()) {
                        if (read8(BNO055IMU.Register.CHIP_ID) != -96) {
                            delayExtra(10);
                        }
                    }
                    delayLoreExtra(50);
                    I2cWarningManager.suppressNewProblemDeviceWarnings(false);
                    RobotLog.m60vv("IMU", "IMU has come out of reset. No more I2C failures should occur.");
                    write8(BNO055IMU.Register.PWR_MODE, POWER_MODE.NORMAL.getValue(), I2cWaitControl.WRITTEN);
                    delayLoreExtra(10);
                    write8(BNO055IMU.Register.PAGE_ID, 0);
                    write8(BNO055IMU.Register.UNIT_SEL, (((BNO055IMU.Parameters) this.parameters).pitchMode.bVal << 7) | (((BNO055IMU.Parameters) this.parameters).temperatureUnit.bVal << 4) | (((BNO055IMU.Parameters) this.parameters).angleUnit.bVal << 2) | (((BNO055IMU.Parameters) this.parameters).angleUnit.bVal << 1) | ((BNO055IMU.Parameters) this.parameters).accelUnit.bVal);
                    write8(BNO055IMU.Register.PAGE_ID, 1);
                    write8(BNO055IMU.Register.ACC_CONFIG, ((BNO055IMU.Parameters) this.parameters).accelPowerMode.bVal | ((BNO055IMU.Parameters) this.parameters).accelBandwidth.bVal | ((BNO055IMU.Parameters) this.parameters).accelRange.bVal);
                    write8(BNO055IMU.Register.MAG_CONFIG, ((BNO055IMU.Parameters) this.parameters).magPowerMode.bVal | ((BNO055IMU.Parameters) this.parameters).magOpMode.bVal | ((BNO055IMU.Parameters) this.parameters).magRate.bVal);
                    write8(BNO055IMU.Register.GYR_CONFIG_0, ((BNO055IMU.Parameters) this.parameters).gyroBandwidth.bVal | ((BNO055IMU.Parameters) this.parameters).gyroRange.bVal);
                    write8(BNO055IMU.Register.GYR_CONFIG_1, ((BNO055IMU.Parameters) this.parameters).gyroPowerMode.bVal);
                    write8(BNO055IMU.Register.PAGE_ID, 0);
                    write8(BNO055IMU.Register.SYS_TRIGGER, 0);
                    if (((BNO055IMU.Parameters) this.parameters).calibrationData != null) {
                        writeCalibrationData(((BNO055IMU.Parameters) this.parameters).calibrationData);
                    } else if (((BNO055IMU.Parameters) this.parameters).calibrationDataFile != null) {
                        try {
                            writeCalibrationData(BNO055IMU.CalibrationData.deserialize(ReadWriteFile.readFileOrThrow(AppUtil.getInstance().getSettingsFile(((BNO055IMU.Parameters) this.parameters).calibrationDataFile))));
                        } catch (IOException unused) {
                        }
                    }
                    setSensorMode(((BNO055IMU.Parameters) this.parameters).mode);
                    BNO055IMU.SystemStatus systemStatus2 = getSystemStatus();
                    if (systemStatus2 == systemStatus) {
                        return true;
                    }
                    log_w("IMU initialization failed: system status=%s expected=%s", systemStatus2, systemStatus);
                    return false;
                } while (elapsedTime.milliseconds() <= 2000.0d);
                log_e("failed to retrieve chip id", new Object[0]);
                return false;
            } finally {
                I2cWarningManager.suppressNewProblemDeviceWarnings(false);
            }
        } else {
            throw new IllegalArgumentException("SensorMode.CONFIG illegal for use as initialization mode");
        }
    }

    /* access modifiers changed from: protected */
    public void setSensorMode(BNO055IMU.SensorMode sensorMode) {
        this.currentMode = sensorMode;
        write8(BNO055IMU.Register.OPR_MODE, sensorMode.bVal & 15, I2cWaitControl.WRITTEN);
        if (sensorMode == BNO055IMU.SensorMode.CONFIG) {
            delayExtra(19);
        } else {
            delayExtra(7);
        }
    }

    public synchronized BNO055IMU.SystemStatus getSystemStatus() {
        BNO055IMU.SystemStatus from;
        byte read8 = read8(BNO055IMU.Register.SYS_STAT);
        from = BNO055IMU.SystemStatus.from(read8);
        if (from == BNO055IMU.SystemStatus.UNKNOWN) {
            log_w("unknown system status observed: 0x%08x", Byte.valueOf(read8));
        }
        return from;
    }

    public synchronized BNO055IMU.SystemError getSystemError() {
        BNO055IMU.SystemError from;
        byte read8 = read8(BNO055IMU.Register.SYS_ERR);
        from = BNO055IMU.SystemError.from(read8);
        if (from == BNO055IMU.SystemError.UNKNOWN) {
            log_w("unknown system error observed: 0x%08x", Byte.valueOf(read8));
        }
        return from;
    }

    public synchronized BNO055IMU.CalibrationStatus getCalibrationStatus() {
        return new BNO055IMU.CalibrationStatus(read8(BNO055IMU.Register.CALIB_STAT));
    }

    public void close() {
        stopAccelerationIntegration();
        super.close();
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

    public synchronized AngularVelocity getAngularVelocity(AngleUnit angleUnit) {
        VectorData vector;
        float next;
        throwIfNotInitialized();
        vector = getVector(VECTOR.GYROSCOPE, getAngularScale());
        next = vector.next();
        return new AngularVelocity(((BNO055IMU.Parameters) this.parameters).angleUnit.toAngleUnit(), -vector.next(), -vector.next(), next, vector.data.nanoTime).toAngleUnit(angleUnit);
    }

    public Orientation getAngularOrientation(AxesReference axesReference, AxesOrder axesOrder, AngleUnit angleUnit) {
        return getAngularOrientation().toAxesReference(axesReference).toAxesOrder(axesOrder).toAngleUnit(angleUnit);
    }

    public synchronized boolean isSystemCalibrated() {
        return ((read8(BNO055IMU.Register.CALIB_STAT) >> 6) & 3) == 3;
    }

    public synchronized boolean isGyroCalibrated() {
        return ((read8(BNO055IMU.Register.CALIB_STAT) >> 4) & 3) == 3;
    }

    public synchronized boolean isAccelerometerCalibrated() {
        return ((read8(BNO055IMU.Register.CALIB_STAT) >> 2) & 3) == 3;
    }

    public synchronized boolean isMagnetometerCalibrated() {
        return (read8(BNO055IMU.Register.CALIB_STAT) & 3) == 3;
    }

    public BNO055IMU.CalibrationData readCalibrationData() {
        BNO055IMU.SensorMode sensorMode = this.currentMode;
        if (sensorMode != BNO055IMU.SensorMode.CONFIG) {
            setSensorMode(BNO055IMU.SensorMode.CONFIG);
        }
        BNO055IMU.CalibrationData calibrationData = new BNO055IMU.CalibrationData();
        calibrationData.dxAccel = readShort(BNO055IMU.Register.ACC_OFFSET_X_LSB);
        calibrationData.dyAccel = readShort(BNO055IMU.Register.ACC_OFFSET_Y_LSB);
        calibrationData.dzAccel = readShort(BNO055IMU.Register.ACC_OFFSET_Z_LSB);
        calibrationData.dxMag = readShort(BNO055IMU.Register.MAG_OFFSET_X_LSB);
        calibrationData.dyMag = readShort(BNO055IMU.Register.MAG_OFFSET_Y_LSB);
        calibrationData.dzMag = readShort(BNO055IMU.Register.MAG_OFFSET_Z_LSB);
        calibrationData.dxGyro = readShort(BNO055IMU.Register.GYR_OFFSET_X_LSB);
        calibrationData.dyGyro = readShort(BNO055IMU.Register.GYR_OFFSET_Y_LSB);
        calibrationData.dzGyro = readShort(BNO055IMU.Register.GYR_OFFSET_Z_LSB);
        calibrationData.radiusAccel = readShort(BNO055IMU.Register.ACC_RADIUS_LSB);
        calibrationData.radiusMag = readShort(BNO055IMU.Register.MAG_RADIUS_LSB);
        if (sensorMode != BNO055IMU.SensorMode.CONFIG) {
            setSensorMode(sensorMode);
        }
        return calibrationData;
    }

    public void writeCalibrationData(BNO055IMU.CalibrationData calibrationData) {
        BNO055IMU.SensorMode sensorMode = this.currentMode;
        if (sensorMode != BNO055IMU.SensorMode.CONFIG) {
            setSensorMode(BNO055IMU.SensorMode.CONFIG);
        }
        writeShort(BNO055IMU.Register.ACC_OFFSET_X_LSB, calibrationData.dxAccel);
        writeShort(BNO055IMU.Register.ACC_OFFSET_Y_LSB, calibrationData.dyAccel);
        writeShort(BNO055IMU.Register.ACC_OFFSET_Z_LSB, calibrationData.dzAccel);
        writeShort(BNO055IMU.Register.MAG_OFFSET_X_LSB, calibrationData.dxMag);
        writeShort(BNO055IMU.Register.MAG_OFFSET_Y_LSB, calibrationData.dyMag);
        writeShort(BNO055IMU.Register.MAG_OFFSET_Z_LSB, calibrationData.dzMag);
        writeShort(BNO055IMU.Register.GYR_OFFSET_X_LSB, calibrationData.dxGyro);
        writeShort(BNO055IMU.Register.GYR_OFFSET_Y_LSB, calibrationData.dyGyro);
        writeShort(BNO055IMU.Register.GYR_OFFSET_Z_LSB, calibrationData.dzGyro);
        writeShort(BNO055IMU.Register.ACC_RADIUS_LSB, calibrationData.radiusAccel);
        writeShort(BNO055IMU.Register.MAG_RADIUS_LSB, calibrationData.radiusMag);
        if (sensorMode != BNO055IMU.SensorMode.CONFIG) {
            setSensorMode(sensorMode);
        }
    }

    public synchronized Temperature getTemperature() {
        throwIfNotInitialized();
        return new Temperature(((BNO055IMU.Parameters) this.parameters).temperatureUnit.toTempUnit(), (double) read8(BNO055IMU.Register.TEMP), System.nanoTime());
    }

    public synchronized MagneticFlux getMagneticFieldStrength() {
        VectorData vector;
        throwIfNotInitialized();
        vector = getVector(VECTOR.MAGNETOMETER, getFluxScale());
        return new MagneticFlux((double) vector.next(), (double) vector.next(), (double) vector.next(), vector.data.nanoTime);
    }

    public synchronized Acceleration getOverallAcceleration() {
        VectorData vector;
        throwIfNotInitialized();
        vector = getVector(VECTOR.ACCELEROMETER, getMetersAccelerationScale());
        return new Acceleration(DistanceUnit.METER, (double) vector.next(), (double) vector.next(), (double) vector.next(), vector.data.nanoTime);
    }

    public synchronized Acceleration getLinearAcceleration() {
        VectorData vector;
        throwIfNotInitialized();
        vector = getVector(VECTOR.LINEARACCEL, getMetersAccelerationScale());
        return new Acceleration(DistanceUnit.METER, (double) vector.next(), (double) vector.next(), (double) vector.next(), vector.data.nanoTime);
    }

    public synchronized Acceleration getGravity() {
        VectorData vector;
        throwIfNotInitialized();
        vector = getVector(VECTOR.GRAVITY, getMetersAccelerationScale());
        return new Acceleration(DistanceUnit.METER, (double) vector.next(), (double) vector.next(), (double) vector.next(), vector.data.nanoTime);
    }

    public synchronized AngularVelocity getAngularVelocity() {
        throwIfNotInitialized();
        return getAngularVelocity(((BNO055IMU.Parameters) this.parameters).angleUnit.toAngleUnit());
    }

    public synchronized Orientation getAngularOrientation() {
        VectorData vector;
        AngleUnit angleUnit;
        throwIfNotInitialized();
        vector = getVector(VECTOR.EULER, getAngularScale());
        angleUnit = ((BNO055IMU.Parameters) this.parameters).angleUnit.toAngleUnit();
        return new Orientation(AxesReference.INTRINSIC, AxesOrder.ZYX, angleUnit, angleUnit.normalize(-vector.next()), angleUnit.normalize(vector.next()), angleUnit.normalize(vector.next()), vector.data.nanoTime);
    }

    public synchronized Quaternion getQuaternionOrientation() {
        VectorData vectorData;
        throwIfNotInitialized();
        ((I2cDeviceSynch) this.deviceClient).ensureReadWindow(new I2cDeviceSynch.ReadWindow(BNO055IMU.Register.QUA_DATA_W_LSB.bVal, 8, readMode), upperWindow);
        vectorData = new VectorData(((I2cDeviceSynch) this.deviceClient).readTimeStamped(BNO055IMU.Register.QUA_DATA_W_LSB.bVal, 8), 16384.0f);
        return new Quaternion(vectorData.next(), vectorData.next(), vectorData.next(), vectorData.next(), vectorData.data.nanoTime);
    }

    /* access modifiers changed from: protected */
    public float getAngularScale() {
        return ((BNO055IMU.Parameters) this.parameters).angleUnit == BNO055IMU.AngleUnit.DEGREES ? 16.0f : 900.0f;
    }

    /* access modifiers changed from: protected */
    public float getAccelerationScale() {
        return ((BNO055IMU.Parameters) this.parameters).accelUnit == BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC ? 100.0f : 1.0f;
    }

    /* access modifiers changed from: protected */
    public float getMetersAccelerationScale() {
        if (((BNO055IMU.Parameters) this.parameters).accelUnit == BNO055IMU.AccelUnit.METERS_PERSEC_PERSEC) {
            return getAccelerationScale();
        }
        return getAccelerationScale() * 100.0f;
    }

    /* access modifiers changed from: protected */
    public VectorData getVector(VECTOR vector, float f) {
        ensureReadWindow(new I2cDeviceSynch.ReadWindow(vector.getValue(), 6, readMode));
        return new VectorData(((I2cDeviceSynch) this.deviceClient).readTimeStamped(vector.getValue(), 6), f);
    }

    protected static class VectorData {
        protected ByteBuffer buffer;
        public TimestampedData data;
        public float scale;

        public VectorData(TimestampedData timestampedData, float f) {
            this.data = timestampedData;
            this.scale = f;
            this.buffer = ByteBuffer.wrap(timestampedData.data).order(ByteOrder.LITTLE_ENDIAN);
        }

        public float next() {
            return ((float) this.buffer.getShort()) / this.scale;
        }
    }

    public Acceleration getAcceleration() {
        Acceleration acceleration;
        synchronized (this.dataLock) {
            acceleration = this.accelerationAlgorithm.getAcceleration();
            if (acceleration == null) {
                acceleration = new Acceleration();
            }
        }
        return acceleration;
    }

    public Velocity getVelocity() {
        Velocity velocity;
        synchronized (this.dataLock) {
            velocity = this.accelerationAlgorithm.getVelocity();
            if (velocity == null) {
                velocity = new Velocity();
            }
        }
        return velocity;
    }

    public Position getPosition() {
        Position position;
        synchronized (this.dataLock) {
            position = this.accelerationAlgorithm.getPosition();
            if (position == null) {
                position = new Position();
            }
        }
        return position;
    }

    public void startAccelerationIntegration(Position position, Velocity velocity, int i) {
        synchronized (this.startStopLock) {
            stopAccelerationIntegration();
            this.accelerationAlgorithm.initialize((BNO055IMU.Parameters) this.parameters, position, velocity);
            ExecutorService newSingleThreadExecutor = ThreadPool.newSingleThreadExecutor("imu acceleration");
            this.accelerationMananger = newSingleThreadExecutor;
            newSingleThreadExecutor.execute(new AccelerationManager(i));
        }
    }

    public void stopAccelerationIntegration() {
        synchronized (this.startStopLock) {
            ExecutorService executorService = this.accelerationMananger;
            if (executorService != null) {
                executorService.shutdownNow();
                ThreadPool.awaitTerminationOrExitApplication(this.accelerationMananger, 10, TimeUnit.SECONDS, "IMU acceleration", "unresponsive user acceleration code");
                this.accelerationMananger = null;
            }
        }
    }

    class AccelerationManager implements Runnable {
        protected static final long nsPerMs = 1000000;
        protected final int msPollInterval;

        AccelerationManager(int i) {
            this.msPollInterval = i;
        }

        public void run() {
            while (!BNO055IMUImpl.this.isStopRequested()) {
                try {
                    Acceleration linearAcceleration = BNO055IMUImpl.this.getLinearAcceleration();
                    synchronized (BNO055IMUImpl.this.dataLock) {
                        BNO055IMUImpl.this.accelerationAlgorithm.update(linearAcceleration);
                    }
                    if (this.msPollInterval > 0) {
                        Thread.sleep(Math.max(0, (((long) this.msPollInterval) - ((System.nanoTime() - linearAcceleration.acquisitionTime) / 1000000)) - 5));
                    } else {
                        Thread.yield();
                    }
                } catch (InterruptedException | CancellationException unused) {
                    return;
                }
            }
        }
    }

    /* access modifiers changed from: package-private */
    public boolean isStopRequested() {
        return Thread.currentThread().isInterrupted();
    }

    public synchronized byte read8(BNO055IMU.Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public synchronized byte[] read(BNO055IMU.Register register, int i) {
        return ((I2cDeviceSynch) this.deviceClient).read(register.bVal, i);
    }

    /* access modifiers changed from: protected */
    public short readShort(BNO055IMU.Register register) {
        return TypeConversion.byteArrayToShort(read(register, 2), ByteOrder.LITTLE_ENDIAN);
    }

    public void write8(BNO055IMU.Register register, int i) {
        write8(register, i, I2cWaitControl.ATOMIC);
    }

    public void write8(BNO055IMU.Register register, int i, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write8(register.bVal, i, i2cWaitControl);
    }

    public void write(BNO055IMU.Register register, byte[] bArr) {
        write(register, bArr, I2cWaitControl.ATOMIC);
    }

    public void write(BNO055IMU.Register register, byte[] bArr, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write(register.bVal, bArr, i2cWaitControl);
    }

    /* access modifiers changed from: protected */
    public void writeShort(BNO055IMU.Register register, short s) {
        write(register, TypeConversion.shortToByteArray(s, ByteOrder.LITTLE_ENDIAN));
    }

    /* access modifiers changed from: protected */
    public void waitForWriteCompletions() {
        ((I2cDeviceSynch) this.deviceClient).waitForWriteCompletions(I2cWaitControl.ATOMIC);
    }

    /* access modifiers changed from: protected */
    public String getLoggingTag() {
        return ((BNO055IMU.Parameters) this.parameters).loggingTag;
    }

    /* access modifiers changed from: protected */
    public void log_v(String str, Object... objArr) {
        if (((BNO055IMU.Parameters) this.parameters).loggingEnabled) {
            Log.v(getLoggingTag(), String.format(str, objArr));
        }
    }

    /* access modifiers changed from: protected */
    public void log_d(String str, Object... objArr) {
        if (((BNO055IMU.Parameters) this.parameters).loggingEnabled) {
            Log.d(getLoggingTag(), String.format(str, objArr));
        }
    }

    /* access modifiers changed from: protected */
    public void log_w(String str, Object... objArr) {
        if (((BNO055IMU.Parameters) this.parameters).loggingEnabled) {
            Log.w(getLoggingTag(), String.format(str, objArr));
        }
    }

    /* access modifiers changed from: protected */
    public void log_e(String str, Object... objArr) {
        if (((BNO055IMU.Parameters) this.parameters).loggingEnabled) {
            Log.e(getLoggingTag(), String.format(str, objArr));
        }
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

    /* access modifiers changed from: protected */
    public void delayExtra(int i) {
        delay(i + 50);
    }

    /* access modifiers changed from: protected */
    public void delayLoreExtra(int i) {
        delayLore(i + 50);
    }

    /* access modifiers changed from: protected */
    public void delayLore(int i) {
        delay(i);
    }

    /* access modifiers changed from: protected */
    public void delay(int i) {
        try {
            waitForWriteCompletions();
            Thread.sleep((long) ((int) (((float) i) * this.delayScale)));
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    /* access modifiers changed from: protected */
    public void enterConfigModeFor(Runnable runnable) {
        BNO055IMU.SensorMode sensorMode = this.currentMode;
        setSensorMode(BNO055IMU.SensorMode.CONFIG);
        delayLoreExtra(25);
        try {
            runnable.run();
        } finally {
            setSensorMode(sensorMode);
            delayLoreExtra(20);
        }
    }

    /* access modifiers changed from: protected */
    public <T> T enterConfigModeFor(Func<T> func) {
        BNO055IMU.SensorMode sensorMode = this.currentMode;
        setSensorMode(BNO055IMU.SensorMode.CONFIG);
        delayLoreExtra(25);
        try {
            return func.value();
        } finally {
            setSensorMode(sensorMode);
            delayLoreExtra(20);
        }
    }

    enum VECTOR {
        ACCELEROMETER((String) BNO055IMU.Register.ACC_DATA_X_LSB),
        MAGNETOMETER((String) BNO055IMU.Register.MAG_DATA_X_LSB),
        GYROSCOPE((String) BNO055IMU.Register.GYR_DATA_X_LSB),
        EULER((String) BNO055IMU.Register.EUL_H_LSB),
        LINEARACCEL((String) BNO055IMU.Register.LIA_DATA_X_LSB),
        GRAVITY((String) BNO055IMU.Register.GRV_DATA_X_LSB);
        
        protected byte value;

        private VECTOR(int i) {
            this.value = (byte) i;
        }

        private VECTOR(BNO055IMU.Register register) {
            this(r1, r2, (int) register.bVal);
        }

        public byte getValue() {
            return this.value;
        }
    }

    enum POWER_MODE {
        NORMAL(0),
        LOWPOWER(1),
        SUSPEND(2);
        
        protected byte value;

        private POWER_MODE(int i) {
            this.value = (byte) i;
        }

        public byte getValue() {
            return this.value;
        }
    }
}
