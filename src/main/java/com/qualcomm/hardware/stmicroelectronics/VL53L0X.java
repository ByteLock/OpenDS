package com.qualcomm.hardware.stmicroelectronics;

import androidx.core.view.MotionEventCompat;
import com.qualcomm.robotcore.hardware.DistanceSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import org.firstinspires.ftc.robotcore.external.navigation.DistanceUnit;
import org.firstinspires.ftc.robotcore.internal.android.p009dx.p012io.Opcodes;

public class VL53L0X extends I2cDeviceSynchDevice<I2cDeviceSynch> implements DistanceSensor {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(82);
    protected static final int FAKE_DISTANCE_MM = 65535;
    protected String MYTAG = "STMicroVL53L0X: ";
    boolean assume_uninitialized = true;
    boolean did_timeout = false;
    protected ElapsedTime ioElapsedTime;
    protected int io_timeout = 0;
    long measurement_timing_budget_us;
    private byte spad_count;
    private boolean spad_type_is_aperture;
    private byte stop_variable = 0;

    enum vcselPeriodType {
        VcselPeriodPreRange,
        VcselPeriodFinalRange
    }

    /* access modifiers changed from: package-private */
    public int decodeTimeout(int i) {
        return ((i & 255) << ((i & MotionEventCompat.ACTION_POINTER_INDEX_MASK) >> 8)) + 1;
    }

    /* access modifiers changed from: protected */
    public int decodeVcselPeriod(int i) {
        return (i + 1) << 1;
    }

    /* access modifiers changed from: protected */
    public long encodeTimeout(int i) {
        if (i <= 0) {
            return 0;
        }
        long j = (long) (i - 1);
        int i2 = 0;
        while ((-256 & j) > 0) {
            j >>= 1;
            i2++;
        }
        return ((long) (i2 << 8)) | (j & 255);
    }

    public String getDeviceName() {
        return "STMicroelectronics_VL53L0X_Range_Sensor";
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Other;
    }

    public byte getModelID() {
        return readReg(Register.IDENTIFICATION_MODEL_ID);
    }

    public double getDistance(DistanceUnit distanceUnit) {
        double d;
        double readRangeContinuousMillimeters = (double) readRangeContinuousMillimeters();
        if (distanceUnit == DistanceUnit.CM) {
            d = 10.0d;
        } else if (distanceUnit == DistanceUnit.METER) {
            d = 1000.0d;
        } else if (distanceUnit != DistanceUnit.INCH) {
            return readRangeContinuousMillimeters;
        } else {
            d = 25.4d;
        }
        return readRangeContinuousMillimeters / d;
    }

    public boolean didTimeoutOccur() {
        return this.did_timeout;
    }

    public enum Register {
        SYSRANGE_START(0),
        SYSTEM_THRESH_HIGH(12),
        SYSTEM_THRESH_LOW(14),
        SYSTEM_SEQUENCE_CONFIG(1),
        SYSTEM_RANGE_CONFIG(9),
        SYSTEM_INTERMEASUREMENT_PERIOD(4),
        SYSTEM_INTERRUPT_CONFIG_GPIO(10),
        GPIO_HV_MUX_ACTIVE_HIGH(132),
        SYSTEM_INTERRUPT_CLEAR(11),
        RESULT_INTERRUPT_STATUS(19),
        RESULT_RANGE_STATUS(20),
        RESULT_CORE_AMBIENT_WINDOW_EVENTS_RTN(188),
        RESULT_CORE_RANGING_TOTAL_EVENTS_RTN(192),
        RESULT_CORE_AMBIENT_WINDOW_EVENTS_REF(Opcodes.ADD_INT_LIT16),
        RESULT_CORE_RANGING_TOTAL_EVENTS_REF(Opcodes.REM_INT_LIT16),
        RESULT_PEAK_SIGNAL_RATE_REF(182),
        ALGO_PART_TO_PART_RANGE_OFFSET_MM(40),
        I2C_SLAVE_DEVICE_ADDRESS(138),
        MSRC_CONFIG_CONTROL(96),
        PRE_RANGE_CONFIG_MIN_SNR(39),
        PRE_RANGE_CONFIG_VALID_PHASE_LOW(86),
        PRE_RANGE_CONFIG_VALID_PHASE_HIGH(87),
        PRE_RANGE_MIN_COUNT_RATE_RTN_LIMIT(100),
        FINAL_RANGE_CONFIG_MIN_SNR(103),
        FINAL_RANGE_CONFIG_VALID_PHASE_LOW(71),
        FINAL_RANGE_CONFIG_VALID_PHASE_HIGH(72),
        FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT(68),
        PRE_RANGE_CONFIG_SIGMA_THRESH_HI(97),
        PRE_RANGE_CONFIG_SIGMA_THRESH_LO(98),
        PRE_RANGE_CONFIG_VCSEL_PERIOD(80),
        PRE_RANGE_CONFIG_TIMEOUT_MACROP_HI(81),
        PRE_RANGE_CONFIG_TIMEOUT_MACROP_LO(82),
        SYSTEM_HISTOGRAM_BIN(129),
        HISTOGRAM_CONFIG_INITIAL_PHASE_SELECT(51),
        HISTOGRAM_CONFIG_READOUT_CTRL(85),
        FINAL_RANGE_CONFIG_VCSEL_PERIOD(112),
        FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI(113),
        FINAL_RANGE_CONFIG_TIMEOUT_MACROP_LO(114),
        CROSSTALK_COMPENSATION_PEAK_RATE_MCPS(32),
        MSRC_CONFIG_TIMEOUT_MACROP(70),
        SOFT_RESET_GO2_SOFT_RESET_N(191),
        IDENTIFICATION_MODEL_ID(192),
        IDENTIFICATION_REVISION_ID(194),
        OSC_CALIBRATE_VAL(248),
        GLOBAL_CONFIG_VCSEL_WIDTH(50),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_0(176),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_1(177),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_2(178),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_3(179),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_4(180),
        GLOBAL_CONFIG_SPAD_ENABLES_REF_5(181),
        GLOBAL_CONFIG_REF_EN_START_SELECT(182),
        DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD(78),
        DYNAMIC_SPAD_REF_EN_START_OFFSET(79),
        POWER_MANAGEMENT_GO1_POWER_FORCE(128),
        VHV_CONFIG_PAD_SCL_SDA__EXTSUP_HV(137),
        ALGO_PHASECAL_LIM(48),
        ALGO_PHASECAL_CONFIG_TIMEOUT(48);
        
        public int bVal;

        private Register(int i) {
            this.bVal = i;
        }
    }

    public VL53L0X(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch, true);
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(ADDRESS_I2C_DEFAULT);
        super.registerArmingStateCallback(false);
        ((I2cDeviceSynch) this.deviceClient).engage();
        this.ioElapsedTime = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
        this.did_timeout = false;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        RobotLog.m42dd(this.MYTAG, "Checking to see if it's really a VL53L0X sensor...");
        byte read8 = ((I2cDeviceSynch) this.deviceClient).read8(192);
        RobotLog.m43dd(this.MYTAG, "Reg 0xC0 = %x (should be 0xEE)", Byte.valueOf(read8));
        byte read82 = ((I2cDeviceSynch) this.deviceClient).read8(193);
        RobotLog.m43dd(this.MYTAG, "Reg 0xC1 = %x (should be 0xAA)", Byte.valueOf(read82));
        byte read83 = ((I2cDeviceSynch) this.deviceClient).read8(194);
        RobotLog.m43dd(this.MYTAG, "Reg 0xC2 = %x (should be 0x10)", Byte.valueOf(read83));
        byte read84 = ((I2cDeviceSynch) this.deviceClient).read8(81);
        RobotLog.m43dd(this.MYTAG, "Reg 0x51 = %x (should be 0x0099)", Byte.valueOf(read84));
        byte read85 = ((I2cDeviceSynch) this.deviceClient).read8(97);
        RobotLog.m43dd(this.MYTAG, "Reg 0x61 = %x (should be 0x0000)", Byte.valueOf(read85));
        return initVL53L0X(false);
    }

    private boolean initVL53L0X(boolean z) {
        if (z) {
            writeReg(Register.VHV_CONFIG_PAD_SCL_SDA__EXTSUP_HV, (byte) (readReg(Register.VHV_CONFIG_PAD_SCL_SDA__EXTSUP_HV) | 1));
        }
        writeReg(136, 0);
        writeReg(128, 1);
        writeReg(255, 1);
        writeReg(0, 0);
        this.stop_variable = ((I2cDeviceSynch) this.deviceClient).read8(145);
        writeReg(0, 1);
        writeReg(255, 0);
        writeReg(128, 0);
        writeReg(Register.MSRC_CONFIG_CONTROL, (byte) (readReg(Register.MSRC_CONFIG_CONTROL) | 18));
        RobotLog.m43dd(this.MYTAG, "initial sig rate lim (MCPS) %.06f", Float.valueOf(getSignalRateLimit()));
        setSignalRateLimit(0.25f);
        RobotLog.m43dd(this.MYTAG, "adjusted sig rate lim (MCPS) %.06f", Float.valueOf(getSignalRateLimit()));
        writeReg(Register.SYSTEM_SEQUENCE_CONFIG, (byte) -1);
        if (!getSpadInfo()) {
            return false;
        }
        byte[] read = ((I2cDeviceSynch) this.deviceClient).read(Register.GLOBAL_CONFIG_SPAD_ENABLES_REF_0.bVal, 6);
        writeReg(255, 1);
        writeReg(Register.DYNAMIC_SPAD_REF_EN_START_OFFSET.bVal, 0);
        writeReg(Register.DYNAMIC_SPAD_NUM_REQUESTED_REF_SPAD.bVal, 44);
        writeReg(255, 0);
        writeReg(Register.GLOBAL_CONFIG_REF_EN_START_SELECT.bVal, 180);
        byte b = (byte) (this.spad_type_is_aperture ? 12 : 0);
        byte b2 = 0;
        for (byte b3 = 0; b3 < 48; b3 = (byte) (b3 + 1)) {
            if (b3 < b || b2 == this.spad_count) {
                int i = b3 / 8;
                read[i] = (byte) (read[i] & (~(1 << (b3 % 8))));
            } else if (((read[b3 / 8] >> (b3 % 8)) & 1) != 0) {
                b2 = (byte) (b2 + 1);
            }
        }
        ((I2cDeviceSynch) this.deviceClient).write(Register.GLOBAL_CONFIG_SPAD_ENABLES_REF_0.bVal, read);
        writeReg(255, 1);
        writeReg(0, 0);
        writeReg(255, 0);
        writeReg(9, 0);
        writeReg(16, 0);
        writeReg(17, 0);
        writeReg(36, 1);
        writeReg(37, 255);
        writeReg(117, 0);
        writeReg(255, 1);
        writeReg(78, 44);
        writeReg(72, 0);
        writeReg(48, 32);
        writeReg(255, 0);
        writeReg(48, 9);
        writeReg(84, 0);
        writeReg(49, 4);
        writeReg(50, 3);
        writeReg(64, 131);
        writeReg(70, 37);
        writeReg(96, 0);
        writeReg(39, 0);
        writeReg(80, 6);
        writeReg(81, 0);
        writeReg(82, 150);
        writeReg(86, 8);
        writeReg(87, 48);
        writeReg(97, 0);
        writeReg(98, 0);
        writeReg(100, 0);
        writeReg(101, 0);
        writeReg(102, 160);
        writeReg(255, 1);
        writeReg(34, 50);
        writeReg(71, 20);
        writeReg(73, 255);
        writeReg(74, 0);
        writeReg(255, 0);
        writeReg(122, 10);
        writeReg(123, 0);
        writeReg(120, 33);
        writeReg(255, 1);
        writeReg(35, 52);
        writeReg(66, 0);
        writeReg(68, 255);
        writeReg(69, 38);
        writeReg(70, 5);
        writeReg(64, 64);
        writeReg(14, 6);
        writeReg(32, 26);
        writeReg(67, 64);
        writeReg(255, 0);
        writeReg(52, 3);
        writeReg(53, 68);
        writeReg(255, 1);
        writeReg(49, 4);
        writeReg(75, 9);
        writeReg(76, 5);
        writeReg(77, 4);
        writeReg(255, 0);
        writeReg(68, 0);
        writeReg(69, 32);
        writeReg(71, 8);
        writeReg(72, 40);
        writeReg(103, 0);
        writeReg(112, 4);
        writeReg(113, 1);
        writeReg(114, 254);
        writeReg(118, 0);
        writeReg(119, 0);
        writeReg(255, 1);
        writeReg(13, 1);
        writeReg(255, 0);
        writeReg(128, 1);
        writeReg(1, 248);
        writeReg(255, 1);
        writeReg(142, 1);
        writeReg(0, 1);
        writeReg(255, 0);
        writeReg(128, 0);
        writeReg(Register.SYSTEM_INTERRUPT_CONFIG_GPIO.bVal, 4);
        writeReg(Register.GPIO_HV_MUX_ACTIVE_HIGH.bVal, (int) readReg(Register.GPIO_HV_MUX_ACTIVE_HIGH) & -17);
        writeReg(Register.SYSTEM_INTERRUPT_CLEAR.bVal, 1);
        this.measurement_timing_budget_us = getMeasurementTimingBudget();
        writeReg(Register.SYSTEM_SEQUENCE_CONFIG.bVal, 232);
        setMeasurementTimingBudget(this.measurement_timing_budget_us);
        writeReg(Register.SYSTEM_SEQUENCE_CONFIG.bVal, 1);
        if (!performSingleRefCalibration(64)) {
            return false;
        }
        writeReg(Register.SYSTEM_SEQUENCE_CONFIG.bVal, 2);
        if (!performSingleRefCalibration(0)) {
            return false;
        }
        writeReg(Register.SYSTEM_SEQUENCE_CONFIG.bVal, 232);
        this.assume_uninitialized = false;
        setTimeout(200);
        startContinuous();
        return true;
    }

    private boolean setSignalRateLimit(float f) {
        if (f < 0.0f || ((double) f) > 511.99d) {
            return false;
        }
        writeShort(Register.FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT, (short) ((int) (f * 128.0f)));
        return true;
    }

    private float getSignalRateLimit() {
        return ((float) readShort(Register.FINAL_RANGE_CONFIG_MIN_COUNT_RATE_RTN_LIMIT)) / 128.0f;
    }

    private boolean getSpadInfo() {
        writeReg(128, 1);
        writeReg(255, 1);
        writeReg(0, 0);
        writeReg(255, 6);
        writeReg(131, (int) (byte) (((I2cDeviceSynch) this.deviceClient).read8(131) | 4));
        writeReg(255, 7);
        writeReg(129, 1);
        writeReg(128, 1);
        writeReg(148, 107);
        writeReg(131, 0);
        writeReg(131, 1);
        byte readReg = readReg(146);
        this.spad_count = (byte) (readReg & Byte.MAX_VALUE);
        this.spad_type_is_aperture = ((readReg >> 7) & 1) != 0;
        writeReg(129, 0);
        writeReg(255, 6);
        writeReg(131, (int) readReg(131) & -5);
        writeReg(255, 1);
        writeReg(0, 1);
        writeReg(255, 0);
        writeReg(128, 0);
        return true;
    }

    /* access modifiers changed from: package-private */
    public long getMeasurementTimingBudget() {
        SequenceStepEnables sequenceStepEnables = new SequenceStepEnables();
        SequenceStepTimeouts sequenceStepTimeouts = new SequenceStepTimeouts();
        getSequenceStepEnables(sequenceStepEnables);
        getSequenceStepTimeouts(sequenceStepEnables, sequenceStepTimeouts);
        long j = 2870;
        if (sequenceStepEnables.tcc) {
            j = 2870 + sequenceStepTimeouts.msrc_dss_tcc_us + 590;
        }
        if (sequenceStepEnables.dss) {
            j += (sequenceStepTimeouts.msrc_dss_tcc_us + 690) * 2;
        } else if (sequenceStepEnables.msrc) {
            j += sequenceStepTimeouts.msrc_dss_tcc_us + 660;
        }
        if (sequenceStepEnables.pre_range) {
            j += sequenceStepTimeouts.pre_range_us + 660;
        }
        if (sequenceStepEnables.final_range) {
            j += sequenceStepTimeouts.final_range_us + 550;
        }
        this.measurement_timing_budget_us = j;
        return j;
    }

    protected class SequenceStepEnables {
        boolean dss;
        boolean final_range;
        boolean msrc;
        boolean pre_range;
        boolean tcc;

        protected SequenceStepEnables() {
        }
    }

    protected class SequenceStepTimeouts {
        int final_range_mclks;
        long final_range_us;
        int final_range_vcsel_period_pclks;
        int msrc_dss_tcc_mclks;
        long msrc_dss_tcc_us;
        int pre_range_mclks;
        long pre_range_us;
        int pre_range_vcsel_period_pclks;

        protected SequenceStepTimeouts() {
        }
    }

    /* access modifiers changed from: protected */
    public void getSequenceStepEnables(SequenceStepEnables sequenceStepEnables) {
        byte readReg = readReg(Register.SYSTEM_SEQUENCE_CONFIG);
        boolean z = true;
        sequenceStepEnables.tcc = ((readReg >> 4) & 1) != 0;
        sequenceStepEnables.dss = ((readReg >> 3) & 1) != 0;
        sequenceStepEnables.msrc = ((readReg >> 2) & 1) != 0;
        sequenceStepEnables.pre_range = ((readReg >> 6) & 1) != 0;
        if (((readReg >> 7) & 1) == 0) {
            z = false;
        }
        sequenceStepEnables.final_range = z;
    }

    /* access modifiers changed from: protected */
    public void getSequenceStepTimeouts(SequenceStepEnables sequenceStepEnables, SequenceStepTimeouts sequenceStepTimeouts) {
        sequenceStepTimeouts.pre_range_vcsel_period_pclks = getVcselPulsePeriod(vcselPeriodType.VcselPeriodPreRange);
        sequenceStepTimeouts.msrc_dss_tcc_mclks = readReg(Register.MSRC_CONFIG_TIMEOUT_MACROP) + 1;
        sequenceStepTimeouts.msrc_dss_tcc_us = timeoutMclksToMicroseconds(sequenceStepTimeouts.msrc_dss_tcc_mclks, sequenceStepTimeouts.pre_range_vcsel_period_pclks);
        sequenceStepTimeouts.pre_range_mclks = decodeTimeout(readShort(Register.PRE_RANGE_CONFIG_TIMEOUT_MACROP_HI));
        sequenceStepTimeouts.pre_range_us = timeoutMclksToMicroseconds(sequenceStepTimeouts.pre_range_mclks, sequenceStepTimeouts.pre_range_vcsel_period_pclks);
        sequenceStepTimeouts.final_range_vcsel_period_pclks = getVcselPulsePeriod(vcselPeriodType.VcselPeriodFinalRange);
        sequenceStepTimeouts.final_range_mclks = decodeTimeout(readShort(Register.FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI));
        if (sequenceStepEnables.pre_range) {
            sequenceStepTimeouts.final_range_mclks -= sequenceStepTimeouts.pre_range_mclks;
        }
        sequenceStepTimeouts.final_range_us = timeoutMclksToMicroseconds(sequenceStepTimeouts.final_range_mclks, sequenceStepTimeouts.final_range_vcsel_period_pclks);
    }

    /* access modifiers changed from: protected */
    public int getVcselPulsePeriod(vcselPeriodType vcselperiodtype) {
        if (vcselperiodtype == vcselPeriodType.VcselPeriodPreRange) {
            return decodeVcselPeriod(readReg(Register.PRE_RANGE_CONFIG_VCSEL_PERIOD));
        }
        if (vcselperiodtype == vcselPeriodType.VcselPeriodFinalRange) {
            return decodeVcselPeriod(readReg(Register.FINAL_RANGE_CONFIG_VCSEL_PERIOD));
        }
        return 255;
    }

    /* access modifiers changed from: protected */
    public long timeoutMclksToMicroseconds(int i, int i2) {
        long calcMacroPeriod = calcMacroPeriod(i2);
        return ((((long) i) * calcMacroPeriod) + (calcMacroPeriod / 2)) / 1000;
    }

    /* access modifiers changed from: protected */
    public long calcMacroPeriod(int i) {
        return (((((long) i) * 2304) * 1655) + 500) / 1000;
    }

    /* access modifiers changed from: protected */
    public boolean setMeasurementTimingBudget(long j) {
        long j2 = j;
        SequenceStepEnables sequenceStepEnables = new SequenceStepEnables();
        SequenceStepTimeouts sequenceStepTimeouts = new SequenceStepTimeouts();
        if (j2 < 20000) {
            return false;
        }
        long j3 = 2280;
        getSequenceStepEnables(sequenceStepEnables);
        getSequenceStepTimeouts(sequenceStepEnables, sequenceStepTimeouts);
        if (sequenceStepEnables.tcc) {
            j3 = 2280 + sequenceStepTimeouts.msrc_dss_tcc_us + 590;
        }
        if (sequenceStepEnables.dss) {
            j3 += (sequenceStepTimeouts.msrc_dss_tcc_us + 690) * 2;
        } else if (sequenceStepEnables.msrc) {
            j3 += sequenceStepTimeouts.msrc_dss_tcc_us + 660;
        }
        if (sequenceStepEnables.pre_range) {
            j3 += sequenceStepTimeouts.pre_range_us + 660;
        }
        if (!sequenceStepEnables.final_range) {
            return true;
        }
        long j4 = j3 + 550;
        if (j4 > j2) {
            return false;
        }
        long timeoutMicrosecondsToMclks = timeoutMicrosecondsToMclks(j2 - j4, sequenceStepTimeouts.final_range_vcsel_period_pclks);
        if (sequenceStepEnables.pre_range) {
            timeoutMicrosecondsToMclks += (long) sequenceStepTimeouts.pre_range_mclks;
        }
        writeShort(Register.FINAL_RANGE_CONFIG_TIMEOUT_MACROP_HI, (short) ((int) encodeTimeout((int) timeoutMicrosecondsToMclks)));
        this.measurement_timing_budget_us = j2;
        return true;
    }

    /* access modifiers changed from: protected */
    public long timeoutMicrosecondsToMclks(long j, int i) {
        long calcMacroPeriod = calcMacroPeriod(i);
        return ((j * 1000) + (calcMacroPeriod / 2)) / calcMacroPeriod;
    }

    /* access modifiers changed from: protected */
    public boolean performSingleRefCalibration(int i) {
        writeReg(Register.SYSRANGE_START.bVal, i | 1);
        writeReg(Register.SYSTEM_INTERRUPT_CLEAR.bVal, 1);
        writeReg(Register.SYSRANGE_START.bVal, 0);
        return true;
    }

    /* access modifiers changed from: protected */
    public void startContinuous() {
        startContinuous(0);
    }

    /* access modifiers changed from: protected */
    public void startContinuous(int i) {
        writeReg(128, 1);
        writeReg(255, 1);
        writeReg(0, 0);
        writeReg(145, (int) this.stop_variable);
        writeReg(0, 1);
        writeReg(255, 0);
        writeReg(128, 0);
        if (i != 0) {
            short readShort = readShort(Register.OSC_CALIBRATE_VAL);
            if (readShort != 0) {
                i *= readShort;
            }
            ((I2cDeviceSynch) this.deviceClient).write(Register.SYSTEM_INTERMEASUREMENT_PERIOD.bVal, TypeConversion.intToByteArray(i));
            writeReg(Register.SYSRANGE_START.bVal, 4);
            return;
        }
        writeReg(Register.SYSRANGE_START.bVal, 2);
    }

    /* access modifiers changed from: protected */
    public void stopContinuous() {
        writeReg(Register.SYSRANGE_START.bVal, 1);
        writeReg(255, 1);
        writeReg(0, 0);
        writeReg(145, 0);
        writeReg(0, 1);
        writeReg(255, 0);
    }

    /* access modifiers changed from: protected */
    public void setTimeout(int i) {
        this.io_timeout = i;
    }

    /* access modifiers changed from: protected */
    public int getTimeout() {
        return this.io_timeout;
    }

    /* access modifiers changed from: protected */
    public int readRangeContinuousMillimeters() {
        if (this.io_timeout > 0) {
            this.ioElapsedTime.reset();
        }
        if (this.assume_uninitialized) {
            return 65535;
        }
        while ((readReg(Register.RESULT_INTERRUPT_STATUS) & 7) == 0) {
            if ((this.did_timeout && ((I2cDeviceSynch) this.deviceClient).getHealthStatus() == HardwareDeviceHealth.HealthStatus.UNHEALTHY) || Thread.currentThread().isInterrupted()) {
                return 65535;
            }
            if (this.ioElapsedTime.milliseconds() > ((double) this.io_timeout)) {
                this.did_timeout = true;
                if (((I2cDeviceSynch) this.deviceClient).getHealthStatus() == HardwareDeviceHealth.HealthStatus.HEALTHY) {
                    this.assume_uninitialized = true;
                }
                return 65535;
            }
        }
        this.did_timeout = false;
        short byteArrayToShort = TypeConversion.byteArrayToShort(((I2cDeviceSynch) this.deviceClient).read(Register.RESULT_RANGE_STATUS.bVal + 10, 2));
        writeReg(Register.SYSTEM_INTERRUPT_CLEAR.bVal, 1);
        return byteArrayToShort;
    }

    /* access modifiers changed from: protected */
    public byte readReg(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    /* access modifiers changed from: protected */
    public byte readReg(byte b) {
        return ((I2cDeviceSynch) this.deviceClient).read8(b);
    }

    /* access modifiers changed from: protected */
    public byte readReg(int i) {
        return ((I2cDeviceSynch) this.deviceClient).read8((byte) i);
    }

    /* access modifiers changed from: protected */
    public void writeReg(Register register, byte b) {
        writeReg(register, b, I2cWaitControl.NONE);
    }

    /* access modifiers changed from: protected */
    public void writeReg(byte b, byte b2) {
        writeReg(b, b2, I2cWaitControl.NONE);
    }

    /* access modifiers changed from: protected */
    public void writeReg(int i, int i2) {
        writeReg((byte) i, (byte) i2, I2cWaitControl.NONE);
    }

    /* access modifiers changed from: protected */
    public void writeReg(Register register, byte b, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write8(register.bVal, b, i2cWaitControl);
    }

    /* access modifiers changed from: protected */
    public void writeReg(byte b, byte b2, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write8(b, b2, i2cWaitControl);
    }

    /* access modifiers changed from: protected */
    public void writeReg(int i, int i2, I2cWaitControl i2cWaitControl) {
        ((I2cDeviceSynch) this.deviceClient).write8((byte) i, (byte) i2, i2cWaitControl);
    }

    /* access modifiers changed from: protected */
    public int readUnsignedByte(Register register) {
        return TypeConversion.unsignedByteToInt(readReg(register));
    }

    /* access modifiers changed from: protected */
    public void writeShort(Register register, short s) {
        ((I2cDeviceSynch) this.deviceClient).write(register.bVal, TypeConversion.shortToByteArray(s));
    }

    /* access modifiers changed from: protected */
    public short readShort(Register register) {
        return TypeConversion.byteArrayToShort(((I2cDeviceSynch) this.deviceClient).read(register.bVal, 2));
    }
}
