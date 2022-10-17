package com.qualcomm.hardware.ams;

import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.NormalizedColorSensor;

public interface AMSColorSensor extends ColorSensor, NormalizedColorSensor {
    public static final int AMS_COLOR_COMMAND_BIT = 128;
    public static final int AMS_COLOR_COMMAND_TYPE_AUTO_INCREMENT = 32;
    public static final int AMS_COLOR_COMMAND_TYPE_REPEATED_BYTE = 0;
    public static final int AMS_COLOR_COMMAND_TYPE_RESERVED = 512;
    public static final int AMS_COLOR_COMMAND_TYPE_SPECIAL = 544;
    public static final I2cAddr AMS_TCS34725_ADDRESS = I2cAddr.create7bit(41);
    public static final byte AMS_TCS34725_ID = 68;
    public static final I2cAddr AMS_TMD37821_ADDRESS = I2cAddr.create7bit(57);
    public static final byte AMS_TMD37821_ID = 96;
    public static final byte AMS_TMD37823_ID = 105;

    byte getDeviceID();

    Parameters getParameters();

    boolean initialize(Parameters parameters);

    byte[] read(Register register, int i);

    byte read8(Register register);

    void write(Register register, byte[] bArr);

    void write8(Register register, int i);

    public static class Parameters implements Cloneable {
        public int atime = atimeFromMs(24.0f);
        public int deviceId;
        public Gain gain = Gain.GAIN_4;
        public I2cAddr i2cAddr;
        public LEDDrive ledDrive = LEDDrive.Percent12_5;
        public boolean loggingEnabled = false;
        public String loggingTag = "AMSColorSensor";
        public int proximityPulseCount = 8;
        public int proximitySaturation = 1023;
        public I2cDeviceSynch.ReadWindow readWindow = new I2cDeviceSynch.ReadWindow(Register.READ_WINDOW_FIRST.bVal, (Register.READ_WINDOW_LAST.bVal - Register.READ_WINDOW_FIRST.bVal) + 1, I2cDeviceSynch.ReadMode.REPEAT);
        public boolean useProximityIfAvailable = true;

        public static int atimeFromMs(float f) {
            return Math.max(0, 256 - ((int) Math.ceil((double) (f / 2.4f))));
        }

        public int integrationCycles() {
            return 256 - this.atime;
        }

        public int getMaximumReading() {
            return Math.min(65535, integrationCycles() * 1024);
        }

        public float msAccumulationInterval() {
            return ((float) integrationCycles()) * 2.4f;
        }

        public Parameters(I2cAddr i2cAddr2, int i) {
            this.i2cAddr = i2cAddr2;
            this.deviceId = i;
        }

        public static Parameters createForTCS34725() {
            return new Parameters(AMSColorSensor.AMS_TCS34725_ADDRESS, 68);
        }

        public static Parameters createForTMD37821() {
            return new Parameters(AMSColorSensor.AMS_TMD37821_ADDRESS, 96);
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
        ENABLE(0),
        ATIME(1),
        REGISTER2(2),
        WTIME(3),
        AILT(4),
        AIHT(6),
        PERS(12),
        CONFIGURATION(13),
        PPLUSE(14),
        CONTROL(15),
        DEVICE_ID(18),
        STATUS(19),
        ALPHA(20),
        RED(22),
        GREEN(24),
        BLUE(26),
        PDATA(28),
        READ_WINDOW_FIRST(r5.bVal),
        READ_WINDOW_LAST(r6.bVal + 1);
        
        public final byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Enable {
        RES7(128),
        RES6(64),
        PIEN(32),
        AIEN(16),
        WEN(8),
        PEN(4),
        AEN(2),
        PON(1),
        OFF(0),
        UNKNOWN(-1);
        
        public final byte bVal;

        public byte bitOr(Enable enable) {
            return (byte) (enable.bVal | this.bVal);
        }

        public byte bitOr(byte b) {
            return (byte) (b | this.bVal);
        }

        private Enable(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Wait {
        MS_2_4(255),
        MS_204(171),
        MS_614(0),
        UNKNOWN(-2);
        
        public final byte bVal;

        private Wait(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Pers {
        CYCLE_NONE(0),
        CYCLE_1(1),
        CYCLE_2(2),
        CYCLE_3(3),
        CYCLE_5(4),
        CYCLE_10(5),
        CYCLE_15(6),
        CYCLE_20(7),
        CYCLE_25(8),
        CYCLE_30(9),
        CYCLE_35(10),
        CYCLE_40(11),
        CYCLE_45(12),
        CYCLE_50(13),
        CYCLE_55(14),
        CYCLE_60(15),
        UNKNOWN(-1);
        
        public final byte bVal;

        private Pers(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Config {
        NORMAL(0),
        LONG_WAIT(2);
        
        public final byte bVal;

        private Config(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Gain {
        UNKNOWN(-1),
        GAIN_1(0),
        GAIN_4(1),
        GAIN_16(2),
        GAIN_64(3),
        MASK(3);
        
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

    public enum LEDDrive {
        Percent100(0),
        Percent50(64),
        Percent25(1024),
        Percent12_5(1088),
        MASK(192);
        
        public final byte bVal;

        private LEDDrive(int i) {
            this.bVal = (byte) i;
        }
    }

    public enum Status {
        PINT(32),
        AINT(16),
        PVALID(2),
        AVALID(1);
        
        public final byte bVal;

        private Status(int i) {
            this.bVal = (byte) i;
        }
    }
}
