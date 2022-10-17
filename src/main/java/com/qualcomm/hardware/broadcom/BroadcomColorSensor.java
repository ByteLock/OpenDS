package com.qualcomm.hardware.broadcom;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

public interface BroadcomColorSensor extends ColorSensor, NormalizedColorSensor {
    public static final I2cAddr BROADCOM_APDS9151_ADDRESS = I2cAddr.create7bit(82);
    public static final byte BROADCOM_APDS9151_ID = -62;

    byte getDeviceID();

    Parameters getParameters();

    boolean initialize(Parameters parameters);

    byte[] read(Register register, int i);

    byte read8(Register register);

    void write(Register register, byte[] bArr);

    void write8(Register register, int i);

    public static class Parameters implements Cloneable {
        public static LSResolution lightSensorResolution = LSResolution.R16BIT;
        public static PSResolution proximityResolution = PSResolution.R11BIT;
        public int colorSaturation = 65535;
        public int deviceId;
        public Gain gain = Gain.GAIN_3;
        public I2cAddr i2cAddr;
        public LEDCurrent ledCurrent = LEDCurrent.CURRENT_125mA;
        public LSMeasurementRate lightSensorMeasRate = LSMeasurementRate.R100ms;
        public boolean loggingEnabled = false;
        public String loggingTag = "BroadcomColorSensor";
        public PSMeasurementRate proximityMeasRate = PSMeasurementRate.R100ms;
        public int proximityPulseCount = 32;
        public int proximitySaturation = 2047;
        public LEDPulseModulation pulseModulation = LEDPulseModulation.LED_PULSE_60kHz;
        public I2cDeviceSynch.ReadWindow readWindow = new I2cDeviceSynch.ReadWindow(Register.READ_WINDOW_FIRST.bVal, (Register.READ_WINDOW_LAST.bVal - Register.READ_WINDOW_FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT);

        public Parameters(I2cAddr i2cAddr2, int i) {
            this.i2cAddr = i2cAddr2;
            this.deviceId = i;
        }

        public static Parameters createForAPDS9151() {
            return new Parameters(BroadcomColorSensor.BROADCOM_APDS9151_ADDRESS, -62);
        }

        public Parameters clone() {
            try {
                return (Parameters) super.clone();
            } catch (CloneNotSupportedException unused) {
                throw new RuntimeException("internal error: Parameters not cloneable");
            }
        }
    }

    public enum Register {
        MAIN_CTRL(0),
        PS_LED(1),
        PS_PULSES(2),
        PS_MEAS_RATE(3),
        LS_MEAS_RATE(4),
        LS_GAIN(5),
        PART_ID(6),
        MAIN_STATUS(7),
        PS_DATA(8),
        LS_DATA_IR(10),
        LS_DATA_GREEN(13),
        LS_DATA_BLUE(16),
        LS_DATA_RED(19),
        INT_CFG(25),
        INT_PST(26),
        PS_THRES_UP(27),
        PS_THRES_LOW(30),
        PS_CAN(31),
        LS_THRES_UP(33),
        LS_THRES_LOW(36),
        LS_THRES_VAR(39),
        READ_WINDOW_FIRST(r15.bVal),
        READ_WINDOW_LAST(r8.bVal + 1);
        
        public final byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum MainControl {
        RES7(128),
        SAI_PS(64),
        SAI_LS(32),
        SW_RESET(16),
        RES3(8),
        RGB_MODE(4),
        LS_EN(2),
        PS_EN(1),
        OFF(0);
        
        public final byte bVal;

        public byte bitOr(MainControl mainControl) {
            return (byte) (mainControl.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private MainControl(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum MainStatus {
        POWER_ON_STATUS(32),
        LS_INT_STAT(16),
        LS_DATA_STATUS(8),
        PS_LOGIC_SIG_STAT(4),
        PS_INT_STAT(2),
        PS_DATA_STAT(1);
        
        public final byte bVal;

        public byte bitOr(MainStatus mainStatus) {
            return (byte) (mainStatus.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private MainStatus(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Gain {
        UNKNOWN(-1),
        GAIN_1(0),
        GAIN_3(1),
        GAIN_6(2),
        GAIN_9(3),
        GAIN_18(4);
        
        public final byte bVal;

        private Gain(int i) {
            this.bVal = (byte) i;
        }

        public static Gain fromByte(byte b) {
            for (Gain gain : values()) {
                if (gain.bVal == b) {
                    return gain;
                }
            }
            return UNKNOWN;
        }
    }

    public enum LEDCurrent {
        CURRENT_2_5mA(0),
        CURRENT_5mA(1),
        CURRENT_10mA(2),
        CURRENT_25mA(3),
        CURRENT_50mA(4),
        CURRENT_75mA(5),
        CURRENT_100mA(6),
        CURRENT_125mA(7);
        
        public final byte bVal;

        public byte bitOr(LEDCurrent lEDCurrent) {
            return (byte) (lEDCurrent.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private LEDCurrent(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum LEDPulseModulation {
        RES0(0),
        RES1(1),
        RES2(2),
        LED_PULSE_60kHz(3),
        LED_PULSE_70kHz(4),
        LED_PULSE_80kHz(5),
        LED_PULSE_90kHz(6),
        LED_PULSE_100kHz(7);
        
        public final byte bVal;

        public byte bitOr(LEDPulseModulation lEDPulseModulation) {
            return (byte) (lEDPulseModulation.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private LEDPulseModulation(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum PSResolution {
        R8BIT(0),
        R9BIT(1),
        R10BIT(2),
        R11BIT(3);
        
        public final byte bVal;

        public byte bitOr(PSResolution pSResolution) {
            return (byte) (pSResolution.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private PSResolution(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum PSMeasurementRate {
        RES(0),
        R6_25ms(1),
        R12_5ms(2),
        R25ms(3),
        R50ms(4),
        R100ms(5),
        R200ms(6),
        R400ms(7);
        
        public final byte bVal;

        public byte bitOr(PSMeasurementRate pSMeasurementRate) {
            return (byte) (pSMeasurementRate.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private PSMeasurementRate(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum LSResolution {
        R20BIT(0),
        R19BIT(1),
        R18BIT(2),
        R17BIT(3),
        R16BIT(4),
        R13BIT(5),
        RES_1(6),
        RES_2(7);
        
        public final byte bVal;

        public byte bitOr(LSResolution lSResolution) {
            return (byte) (lSResolution.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private LSResolution(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum LSMeasurementRate {
        R25ms(0),
        R50ms(1),
        R100ms(2),
        R200ms(3),
        R500ms(4),
        R1000ms(5),
        R2000ms_1(6),
        R2000ms_2(7);
        
        public final byte bVal;

        public byte bitOr(PSMeasurementRate pSMeasurementRate) {
            return (byte) (pSMeasurementRate.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private LSMeasurementRate(int i) {
            this.bVal = (byte) i;
        }
    }
}
