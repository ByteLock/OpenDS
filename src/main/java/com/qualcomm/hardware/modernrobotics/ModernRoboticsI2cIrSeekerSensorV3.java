package com.qualcomm.hardware.modernrobotics;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cAddrConfig;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDevice;
import com.qualcomm.robotcore.hardware.IrSeekerSensor;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteOrder;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class ModernRoboticsI2cIrSeekerSensorV3 extends I2cDeviceSynchDevice<I2cDeviceSynch> implements IrSeekerSensor, I2cAddrConfig {
    public static final I2cAddr ADDRESS_I2C_DEFAULT = I2cAddr.create8bit(56);
    public static final double MAX_SENSOR_STRENGTH = 255.0d;
    protected IrSeekerSensor.Mode mode = IrSeekerSensor.Mode.MODE_1200HZ;
    protected double signalDetectedThreshold;

    public int getVersion() {
        return 3;
    }

    public enum Register {
        READ_WINDOW_FIRST(0),
        FIRMWARE_REV(0),
        MANUFACTURE_CODE(1),
        SENSOR_ID(2),
        UNUSED(3),
        DIR_DATA_1200(4),
        SIGNAL_STRENTH_1200(5),
        DIR_DATA_600(6),
        SIGNAL_STRENTH_600(7),
        LEFT_SIDE_DATA_1200(8),
        RIGHT_SIDE_DATA_1200(10),
        LEFT_SIDE_DATA_600(12),
        RIGHT_SIDE_DATA_600(14),
        READ_WINDOW_LAST(r10.bVal + 1),
        UNKNOWN(-1);
        
        public byte bVal;

        private Register(int i) {
            this.bVal = (byte) i;
        }
    }

    public ModernRoboticsI2cIrSeekerSensorV3(I2cDeviceSynch i2cDeviceSynch) {
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
    public boolean doInitialize() {
        setMode(IrSeekerSensor.Mode.MODE_1200HZ);
        this.signalDetectedThreshold = 0.00392156862745098d;
        return true;
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.ModernRobotics;
    }

    public String getDeviceName() {
        return String.format("%s %s", new Object[]{AppUtil.getDefContext().getString(C0705R.string.configTypeIrSeekerV3), new RobotUsbDevice.FirmwareVersion(read8(Register.FIRMWARE_REV))});
    }

    public byte read8(Register register) {
        return ((I2cDeviceSynch) this.deviceClient).read8(register.bVal);
    }

    public void write8(Register register, byte b) {
        ((I2cDeviceSynch) this.deviceClient).write8((int) register.bVal, (int) b);
    }

    /* access modifiers changed from: protected */
    public short readShort(Register register) {
        return TypeConversion.byteArrayToShort(((I2cDeviceSynch) this.deviceClient).read(register.bVal, 2), ByteOrder.LITTLE_ENDIAN);
    }

    public String toString() {
        if (!signalDetected()) {
            return "IR Seeker:  --% signal at  ---.- degrees";
        }
        return String.format("IR Seeker: %3.0f%% signal at %6.1f degrees", new Object[]{Double.valueOf(getStrength() * 100.0d), Double.valueOf(getAngle())});
    }

    public synchronized void setSignalDetectedThreshold(double d) {
        this.signalDetectedThreshold = d;
    }

    public double getSignalDetectedThreshold() {
        return this.signalDetectedThreshold;
    }

    public synchronized void setMode(IrSeekerSensor.Mode mode2) {
        this.mode = mode2;
    }

    public IrSeekerSensor.Mode getMode() {
        return this.mode;
    }

    public boolean signalDetected() {
        return getStrength() > this.signalDetectedThreshold;
    }

    public synchronized double getAngle() {
        return (double) read8(getMode() == IrSeekerSensor.Mode.MODE_1200HZ ? Register.DIR_DATA_1200 : Register.DIR_DATA_600);
    }

    public synchronized double getStrength() {
        return TypeConversion.unsignedByteToDouble(read8(getMode() == IrSeekerSensor.Mode.MODE_1200HZ ? Register.SIGNAL_STRENTH_1200 : Register.SIGNAL_STRENTH_600)) / 255.0d;
    }

    public synchronized IrSeekerSensor.IrSeekerIndividualSensor[] getIndividualSensors() {
        IrSeekerSensor.IrSeekerIndividualSensor[] irSeekerIndividualSensorArr;
        irSeekerIndividualSensorArr = new IrSeekerSensor.IrSeekerIndividualSensor[2];
        irSeekerIndividualSensorArr[0] = new IrSeekerSensor.IrSeekerIndividualSensor(-1.0d, ((double) readShort(getMode() == IrSeekerSensor.Mode.MODE_1200HZ ? Register.LEFT_SIDE_DATA_1200 : Register.LEFT_SIDE_DATA_600)) / 255.0d);
        irSeekerIndividualSensorArr[1] = new IrSeekerSensor.IrSeekerIndividualSensor(1.0d, ((double) readShort(getMode() == IrSeekerSensor.Mode.MODE_1200HZ ? Register.RIGHT_SIDE_DATA_1200 : Register.RIGHT_SIDE_DATA_600)) / 255.0d);
        return irSeekerIndividualSensorArr;
    }

    public synchronized void setI2cAddress(I2cAddr i2cAddr) {
        ((I2cDeviceSynch) this.deviceClient).setI2cAddress(i2cAddr);
    }

    public I2cAddr getI2cAddress() {
        return ((I2cDeviceSynch) this.deviceClient).getI2cAddress();
    }
}
