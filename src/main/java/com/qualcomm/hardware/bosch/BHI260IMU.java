package com.qualcomm.hardware.bosch;

import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchDeviceWithParameters;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.EnumSet;
import org.firstinspires.ftc.robotcore.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.SystemProperties;

@I2cDeviceType
@DeviceProperties(builtIn = true, description = "@string/lynx_embedded_imu_description", name = "@string/lynx_embedded_bhi260ap_imu_name", xmlTag = "ControlHubImuBHI260AP")
public class BHI260IMU extends I2cDeviceSynchDeviceWithParameters<I2cDeviceSynchSimple, InterimParameterClassDoNotUse> {
    private static final int BOOT_FROM_FLASH_TIMEOUT_MS = 2000;
    private static final int BUNDLED_FW_VERSION = 20;
    private static final int COMMAND_ERROR_RESPONSE = 15;
    private static final int COMMAND_ERROR_STATUS = 15;
    private static final int COMMAND_HEADER_LENGTH = 4;
    private static final boolean DEBUG_FW_FLASHING = false;
    private static final int ERASE_FLASH_TIMEOUT_MS = 14000;
    private static final int FW_RESOURCE = C0660R.raw.rev_bhi260_ap_fw_20;
    private static final int FW_START_ADDRESS = 8068;
    private static final I2cAddr I2C_ADDR = I2cAddr.create7bit(40);
    private static final int MAX_READ_I2C_BYTES = 100;
    private static final int MAX_SEND_I2C_BYTES_NO_REGISTER = 100;
    private static final int MAX_SEND_I2C_BYTES_WITH_REGISTER = 99;
    private static final int MAX_WRITE_FLASH_FIRMWARE_AND_PADDING_BYTES = 91;
    private static final int MAX_WRITE_FLASH_FIRMWARE_BYTES = 88;
    private static final int PRODUCT_ID = 137;
    private static final double QUATERNION_SCALE_FACTOR = Math.pow(2.0d, -14.0d);
    private static final String TAG = "BHI260IMU";
    private static final boolean USE_FREEZE_PIN = true;
    private static final int WRITE_FLASH_COMMAND_HEADER_LENGTH = 8;
    private static final int WRITE_FLASH_RESPONSE_TIMEOUT_MS = 1000;
    private int fwVersion = 0;

    private enum BootStatusFlag {
        FLASH_DETECTED,
        FLASH_VERIFY_DONE,
        FLASH_VERIFY_ERROR,
        NO_FLASH,
        HOST_INTERFACE_READY,
        FIRMWARE_VERIFY_DONE,
        FIRMWARE_VERIFY_ERROR,
        FIRMWARE_HALTED
    }

    @Deprecated
    public static class InterimParameterClassDoNotUse {
    }

    private enum InterruptStatusFlag {
        HOST_INTERRUPT_ASSERTED,
        WAKE_UP_FIFO_STATUS_1,
        WAKE_UP_FIFO_STATUS_2,
        NON_WAKE_UP_FIFO_STATUS_1,
        NON_WAKE_UP_FIFO_STATUS_2,
        STATUS_STATUS,
        DEBUG_STATUS,
        RESET_OR_FAULT
    }

    /* access modifiers changed from: protected */
    public boolean internalInitialize(InterimParameterClassDoNotUse interimParameterClassDoNotUse) {
        return true;
    }

    public static boolean imuIsPresent(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        i2cDeviceSynchSimple.setI2cAddress(I2C_ADDR);
        RobotLog.m60vv(TAG, "Suppressing I2C warnings while we check for a BHI260AP IMU");
        I2cWarningManager.suppressNewProblemDeviceWarnings(true);
        try {
            if (read8(i2cDeviceSynchSimple, Register.PRODUCT_IDENTIFIER) == 137) {
                RobotLog.m60vv(TAG, "Found BHI260AP IMU");
                return true;
            }
            RobotLog.m60vv(TAG, "No BHI260AP IMU found");
            I2cWarningManager.suppressNewProblemDeviceWarnings(false);
            return false;
        } finally {
            I2cWarningManager.suppressNewProblemDeviceWarnings(false);
        }
    }

    public static void flashFirmwareIfNecessary(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        i2cDeviceSynchSimple.setI2cAddress(I2C_ADDR);
        try {
            waitForHostInterface(i2cDeviceSynchSimple);
            checkForFlashPresence(i2cDeviceSynchSimple);
            boolean waitForFlashVerification = waitForFlashVerification(i2cDeviceSynchSimple);
            int read16 = waitForFlashVerification ? read16(i2cDeviceSynchSimple, Register.USER_VERSION) : 0;
            RobotLog.m61vv(TAG, "flashFirmwareIfNecessary() alreadyFlashed=%b firmwareVersion=%d", Boolean.valueOf(waitForFlashVerification), Integer.valueOf(read16));
            if (read16 != 20) {
                try {
                    if (SystemProperties.getBoolean("persist.bhi260.flash400khz", false)) {
                        RobotLog.m60vv(TAG, "Setting I2C bus speed to 400KHz for firmware flashing");
                        ((LynxI2cDeviceSynch) i2cDeviceSynchSimple).setBusSpeed(LynxI2cDeviceSynch.BusSpeed.FAST_400K);
                    } else {
                        RobotLog.m60vv(TAG, "Setting I2C bus speed to 100KHz for firmware flashing");
                        ((LynxI2cDeviceSynch) i2cDeviceSynchSimple).setBusSpeed(LynxI2cDeviceSynch.BusSpeed.STANDARD_100K);
                    }
                    RobotLog.m55ii(TAG, "Flashing IMU firmware version %d", 20);
                    ElapsedTime elapsedTime = new ElapsedTime();
                    ByteBuffer wrap = ByteBuffer.wrap(ReadWriteFile.readRawResourceBytesOrThrow(FW_RESOURCE));
                    int remaining = wrap.remaining();
                    RobotLog.m60vv(TAG, "Resetting IMU");
                    write8(i2cDeviceSynchSimple, Register.RESET_REQUEST, 1, I2cWaitControl.WRITTEN);
                    waitForHostInterface(i2cDeviceSynchSimple);
                    setStatusFifoToSynchronousMode(i2cDeviceSynchSimple);
                    RobotLog.m43dd(TAG, "Flash device's JDEC manufacturer ID: 0x%X", Byte.valueOf(i2cDeviceSynchSimple.read8(50)));
                    RobotLog.m60vv(TAG, "Wiping IMU flash memory");
                    AppUtil.getInstance().showProgress(UILocation.BOTH, AppUtil.getDefContext().getString(C0660R.string.flashingControlHubImu), new ProgressParameters(0, remaining));
                    ByteBuffer order = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN);
                    int i = FW_START_ADDRESS;
                    sendCommandAndWaitForResponse(i2cDeviceSynchSimple, CommandType.ERASE_FLASH, order.putInt(FW_START_ADDRESS).putInt(remaining + FW_START_ADDRESS).array(), ERASE_FLASH_TIMEOUT_MS);
                    AppAliveNotifier.getInstance().notifyAppAlive();
                    RobotLog.m54ii(TAG, "Sending firmware data");
                    int i2 = 0;
                    while (wrap.hasRemaining()) {
                        int min = Math.min(wrap.remaining(), 88);
                        sendWriteFlashCommandAndWaitForResponse(i2cDeviceSynchSimple, i, min, wrap);
                        i += min;
                        i2 += min;
                        AppUtil.getInstance().showProgress(UILocation.BOTH, AppUtil.getDefContext().getString(C0660R.string.flashingControlHubImu), new ProgressParameters(i2, remaining));
                    }
                    RobotLog.m60vv(TAG, "Booting into newly-flashed firmware");
                    sendCommand(i2cDeviceSynchSimple, CommandType.BOOT_FLASH, (byte[]) null);
                    waitForHostInterface(i2cDeviceSynchSimple);
                    checkForFlashPresence(i2cDeviceSynchSimple);
                    if (waitForFlashVerification(i2cDeviceSynchSimple)) {
                        RobotLog.m61vv(TAG, "Successfully flashed Control Hub IMU firmware in %.2f seconds", Double.valueOf(elapsedTime.seconds()));
                        AppUtil.getInstance().dismissProgress(UILocation.BOTH);
                        return;
                    }
                    RobotLog.m48ee(TAG, "IMU flash verification failed after flashing firmware");
                    throw new InitException();
                } catch (IOException e) {
                    RobotLog.m50ee(TAG, (Throwable) e, "Failed to read IMU firmware file");
                    throw new InitException();
                } catch (CommandFailureException e2) {
                    RobotLog.m50ee(TAG, (Throwable) e2, "IMU flash erase failed");
                    throw new InitException();
                } catch (CommandFailureException e3) {
                    RobotLog.m50ee(TAG, (Throwable) e3, "Write Flash command failed");
                    throw new InitException();
                } catch (Throwable th) {
                    AppUtil.getInstance().dismissProgress(UILocation.BOTH);
                    throw th;
                }
            }
        } catch (InitException unused) {
            RobotLog.addGlobalWarningMessage(AppUtil.getDefContext().getString(C0660R.string.controlHubImuFwFlashFailed));
        }
    }

    public BHI260IMU(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        super(i2cDeviceSynchSimple, true, new InterimParameterClassDoNotUse());
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0660R.string.lynx_embedded_bhi260ap_imu_name);
    }

    public String getConnectionInfo() {
        return String.format("BHI260 IMU on %s", new Object[]{this.deviceClient.getConnectionInfo()});
    }

    public int getFirmwareVersion() {
        return this.fwVersion;
    }

    private static int read8(I2cDeviceSynchSimple i2cDeviceSynchSimple, Register register) {
        return TypeConversion.unsignedByteToInt(i2cDeviceSynchSimple.read8(register.address));
    }

    private static int read16(I2cDeviceSynchSimple i2cDeviceSynchSimple, Register register) {
        return TypeConversion.byteArrayToShort(i2cDeviceSynchSimple.read(register.address, 2), ByteOrder.LITTLE_ENDIAN);
    }

    private static <T extends Enum<T>> EnumSet<T> read8Flags(I2cDeviceSynchSimple i2cDeviceSynchSimple, Register register, Class<T> cls) {
        return convertIntToEnumSet(read8(i2cDeviceSynchSimple, register), cls);
    }

    private static EnumSet<BootStatusFlag> readBootStatusFlags(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        return read8Flags(i2cDeviceSynchSimple, Register.BOOT_STATUS, BootStatusFlag.class);
    }

    private static void write8(I2cDeviceSynchSimple i2cDeviceSynchSimple, Register register, int i, I2cWaitControl i2cWaitControl) {
        i2cDeviceSynchSimple.write8(register.address, i, i2cWaitControl);
    }

    private static void waitForHostInterface(I2cDeviceSynchSimple i2cDeviceSynchSimple) throws InitException {
        ElapsedTime elapsedTime = new ElapsedTime();
        EnumSet<BootStatusFlag> readBootStatusFlags = readBootStatusFlags(i2cDeviceSynchSimple);
        while (!readBootStatusFlags.contains(BootStatusFlag.HOST_INTERFACE_READY) && elapsedTime.milliseconds() < 2000.0d) {
            try {
                Thread.sleep(10);
                readBootStatusFlags = readBootStatusFlags(i2cDeviceSynchSimple);
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
                throw new InitException();
            }
        }
        if (!readBootStatusFlags.contains(BootStatusFlag.HOST_INTERFACE_READY)) {
            RobotLog.m48ee(TAG, "Timeout expired while waiting for IMU host interface to become ready");
            throw new InitException();
        }
    }

    private static void checkForFlashPresence(I2cDeviceSynchSimple i2cDeviceSynchSimple) throws InitException {
        EnumSet<BootStatusFlag> readBootStatusFlags = readBootStatusFlags(i2cDeviceSynchSimple);
        if (!readBootStatusFlags.contains(BootStatusFlag.FLASH_DETECTED) || readBootStatusFlags.contains(BootStatusFlag.NO_FLASH)) {
            RobotLog.m48ee(TAG, "IMU did not detect flash chip");
            throw new InitException();
        }
    }

    private static boolean waitForFlashVerification(I2cDeviceSynchSimple i2cDeviceSynchSimple) throws InitException {
        ElapsedTime elapsedTime = new ElapsedTime();
        EnumSet<BootStatusFlag> readBootStatusFlags = readBootStatusFlags(i2cDeviceSynchSimple);
        AppAliveNotifier.getInstance().notifyAppAlive();
        while (!readBootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_DONE)) {
            try {
                if (elapsedTime.milliseconds() >= 1500.0d) {
                    break;
                } else if (readBootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_ERROR)) {
                    RobotLog.m48ee(TAG, "Error verifying IMU firmware");
                    return false;
                } else {
                    Thread.sleep(10);
                    readBootStatusFlags = readBootStatusFlags(i2cDeviceSynchSimple);
                }
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
                throw new InitException();
            }
        }
        if (!readBootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_DONE)) {
            RobotLog.m66ww(TAG, "Timeout expired while waiting for IMU to load its firmware from flash");
            return false;
        } else if (!readBootStatusFlags.contains(BootStatusFlag.FLASH_VERIFY_ERROR)) {
            return true;
        } else {
            RobotLog.m48ee(TAG, "Error verifying IMU firmware");
            return false;
        }
    }

    private static void sendCommand(I2cDeviceSynchSimple i2cDeviceSynchSimple, CommandType commandType, byte[] bArr) {
        int i = 0;
        if (bArr == null) {
            bArr = new byte[0];
        }
        int length = bArr.length + 4;
        if (length % 4 != 0) {
            i = 4 - (bArr.length % 4);
            length += i;
        }
        if (length <= 99) {
            ByteBuffer put = ByteBuffer.allocate(length).order(ByteOrder.LITTLE_ENDIAN).putShort((short) commandType.f78id).putShort((short) (length - 4)).put(bArr);
            if (i > 0) {
                put.put(new byte[i]);
            }
            i2cDeviceSynchSimple.write(Register.COMMAND_INPUT.address, put.array());
            return;
        }
        throw new IllegalArgumentException("sendCommand() called with too large of a payload. Update sendCommand() to break into multiple I2C writes");
    }

    private static StatusPacket sendCommandAndWaitForResponse(I2cDeviceSynchSimple i2cDeviceSynchSimple, CommandType commandType, byte[] bArr, int i) throws CommandFailureException {
        sendCommand(i2cDeviceSynchSimple, commandType, bArr);
        return waitForCommandResponse(i2cDeviceSynchSimple, commandType, i);
    }

    private static void sendWriteFlashCommandAndWaitForResponse(I2cDeviceSynchSimple i2cDeviceSynchSimple, int i, int i2, ByteBuffer byteBuffer) throws CommandFailureException {
        int i3;
        if (i2 <= 88) {
            AppAliveNotifier.getInstance().notifyAppAlive();
            int i4 = i2 + 8;
            int i5 = 0;
            if (i4 % 4 != 0) {
                i5 = 4 - (i2 % 4);
                i3 = i4 + i5;
            } else {
                i3 = i4;
            }
            ByteBuffer putShort = ByteBuffer.allocate(i3).order(ByteOrder.LITTLE_ENDIAN).putShort((short) CommandType.WRITE_FLASH.f78id);
            putShort.putShort((short) (i3 - 4));
            putShort.putInt(i);
            byteBuffer.get(putShort.array(), putShort.position(), i2);
            putShort.position(i4);
            if (i5 > 0) {
                putShort.put(new byte[i5]);
            }
            i2cDeviceSynchSimple.write(Register.COMMAND_INPUT.address, putShort.array());
            waitForCommandResponse(i2cDeviceSynchSimple, CommandType.WRITE_FLASH, 1000);
            return;
        }
        throw new IllegalArgumentException("Tried to write too many bytes in a single Write Flash command");
    }

    /* JADX WARNING: type inference failed for: r7v7, types: [com.qualcomm.hardware.bosch.BHI260IMU$CommandError] */
    /* JADX WARNING: Failed to insert additional move for type inference */
    /* JADX WARNING: Multi-variable type inference failed */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private static com.qualcomm.hardware.bosch.BHI260IMU.StatusPacket waitForCommandResponse(com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple r17, com.qualcomm.hardware.bosch.BHI260IMU.CommandType r18, int r19) throws com.qualcomm.hardware.bosch.BHI260IMU.CommandFailureException {
        /*
            r0 = r17
            r1 = r18
            com.qualcomm.robotcore.util.ElapsedTime r2 = new com.qualcomm.robotcore.util.ElapsedTime
            r2.<init>()
            com.qualcomm.robotcore.util.ElapsedTime r3 = new com.qualcomm.robotcore.util.ElapsedTime
            r3.<init>()
            org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier r4 = org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier.getInstance()
            r4.notifyAppAlive()
        L_0x0015:
            double r4 = r3.seconds()
            r6 = 4620693217682128896(0x4020000000000000, double:8.0)
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 <= 0) goto L_0x0029
            org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier r4 = org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier.getInstance()
            r4.notifyAppAlive()
            r3.reset()
        L_0x0029:
            double r4 = r2.milliseconds()
            r8 = r19
            double r9 = (double) r8
            int r4 = (r4 > r9 ? 1 : (r4 == r9 ? 0 : -1))
            java.lang.String r5 = "%dms timeout expired while waiting for response"
            r11 = 1
            r12 = 0
            if (r4 >= 0) goto L_0x01b1
            com.qualcomm.hardware.bosch.BHI260IMU$Register r4 = com.qualcomm.hardware.bosch.BHI260IMU.Register.INTERRUPT_STATUS
            java.lang.Class<com.qualcomm.hardware.bosch.BHI260IMU$InterruptStatusFlag> r13 = com.qualcomm.hardware.bosch.BHI260IMU.InterruptStatusFlag.class
            java.util.EnumSet r4 = read8Flags(r0, r4, r13)
            com.qualcomm.hardware.bosch.BHI260IMU$InterruptStatusFlag r13 = com.qualcomm.hardware.bosch.BHI260IMU.InterruptStatusFlag.RESET_OR_FAULT
            boolean r13 = r4.contains(r13)
            java.lang.String r14 = "BHI260IMU"
            if (r13 == 0) goto L_0x0073
            com.qualcomm.hardware.bosch.BHI260IMU$CommandType r13 = com.qualcomm.hardware.bosch.BHI260IMU.CommandType.ERASE_FLASH
            if (r1 == r13) goto L_0x0073
            java.lang.Object[] r13 = new java.lang.Object[r11]
            r13[r12] = r1
            java.lang.String r15 = "Reset or Fault interrupt status was set while waiting for %s response"
            com.qualcomm.robotcore.util.RobotLog.m67ww((java.lang.String) r14, (java.lang.String) r15, (java.lang.Object[]) r13)
            java.lang.Object[] r13 = new java.lang.Object[r11]
            r13[r12] = r4
            java.lang.String r15 = "Interrupt status: %s"
            com.qualcomm.robotcore.util.RobotLog.m67ww((java.lang.String) r14, (java.lang.String) r15, (java.lang.Object[]) r13)
            java.lang.Object[] r13 = new java.lang.Object[r11]
            com.qualcomm.hardware.bosch.BHI260IMU$Register r15 = com.qualcomm.hardware.bosch.BHI260IMU.Register.ERROR_VALUE
            int r15 = read8(r0, r15)
            java.lang.Integer r15 = java.lang.Integer.valueOf(r15)
            r13[r12] = r15
            java.lang.String r15 = "Error value: 0x%X"
            com.qualcomm.robotcore.util.RobotLog.m67ww((java.lang.String) r14, (java.lang.String) r15, (java.lang.Object[]) r13)
        L_0x0073:
            com.qualcomm.hardware.bosch.BHI260IMU$InterruptStatusFlag r13 = com.qualcomm.hardware.bosch.BHI260IMU.InterruptStatusFlag.STATUS_STATUS
            boolean r4 = r4.contains(r13)
            if (r4 == 0) goto L_0x0015
        L_0x007b:
            double r15 = r3.seconds()
            int r4 = (r15 > r6 ? 1 : (r15 == r6 ? 0 : -1))
            if (r4 <= 0) goto L_0x008d
            org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier r4 = org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier.getInstance()
            r4.notifyAppAlive()
            r3.reset()
        L_0x008d:
            double r15 = r2.milliseconds()
            int r4 = (r15 > r9 ? 1 : (r15 == r9 ? 0 : -1))
            if (r4 >= 0) goto L_0x019d
            com.qualcomm.hardware.bosch.BHI260IMU$Register r4 = com.qualcomm.hardware.bosch.BHI260IMU.Register.STATUS_AND_DEBUG_FIFO_OUTPUT
            int r4 = r4.address
            r13 = 3
            byte[] r4 = r0.read(r4, r13)
            java.nio.ByteBuffer r4 = java.nio.ByteBuffer.wrap(r4)
            java.nio.ByteOrder r15 = java.nio.ByteOrder.LITTLE_ENDIAN
            java.nio.ByteBuffer r4 = r4.order(r15)
            short r15 = r4.getShort()
            int r15 = com.qualcomm.robotcore.util.TypeConversion.unsignedShortToInt(r15)
            byte r4 = r4.get()
            int r4 = com.qualcomm.robotcore.util.TypeConversion.unsignedByteToInt(r4)
            r6 = 100
            if (r4 > r6) goto L_0x0187
            if (r4 != 0) goto L_0x00c3
            byte[] r4 = new byte[r12]
            goto L_0x00c7
        L_0x00c3:
            byte[] r4 = r0.read(r4)
        L_0x00c7:
            int r6 = r18.successStatusCode
            r7 = 0
            if (r15 == r6) goto L_0x0181
            if (r15 != 0) goto L_0x00d8
            java.lang.String r4 = "Received status code 0, trying again"
            com.qualcomm.robotcore.util.RobotLog.m66ww(r14, r4)
            r6 = 4620693217682128896(0x4020000000000000, double:8.0)
            goto L_0x007b
        L_0x00d8:
            r0 = 15
            r2 = 2
            if (r15 != r0) goto L_0x0167
            int r0 = r4.length
            r3 = -1
            if (r0 < r13) goto L_0x0100
            java.nio.ByteBuffer r0 = java.nio.ByteBuffer.wrap(r4)
            java.nio.ByteOrder r3 = java.nio.ByteOrder.LITTLE_ENDIAN
            java.nio.ByteBuffer r0 = r0.order(r3)
            short r3 = r0.getShort()
            int r3 = com.qualcomm.robotcore.util.TypeConversion.unsignedShortToInt(r3)
            byte r0 = r0.get()
            int r0 = com.qualcomm.robotcore.util.TypeConversion.unsignedByteToInt(r0)
            com.qualcomm.hardware.bosch.BHI260IMU$CommandError r7 = com.qualcomm.hardware.bosch.BHI260IMU.CommandError.fromInt(r0)
            goto L_0x0101
        L_0x0100:
            r0 = r3
        L_0x0101:
            int r4 = r18.f78id
            if (r3 != r4) goto L_0x011d
            java.lang.StringBuilder r3 = new java.lang.StringBuilder
            r3.<init>()
            java.lang.String r4 = r18.toString()
            r3.append(r4)
            java.lang.String r4 = " command"
            r3.append(r4)
            java.lang.String r3 = r3.toString()
            goto L_0x0144
        L_0x011d:
            com.qualcomm.hardware.bosch.BHI260IMU$CommandType r4 = com.qualcomm.hardware.bosch.BHI260IMU.CommandType.findById(r3)
            if (r4 != 0) goto L_0x0136
            java.util.Locale r4 = java.util.Locale.US
            java.lang.Object[] r5 = new java.lang.Object[r2]
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)
            r5[r12] = r3
            r5[r11] = r1
            java.lang.String r3 = "unknown command 0x%4X (just sent %s command)"
            java.lang.String r3 = java.lang.String.format(r4, r3, r5)
            goto L_0x0144
        L_0x0136:
            java.util.Locale r3 = java.util.Locale.US
            java.lang.Object[] r5 = new java.lang.Object[r2]
            r5[r12] = r4
            r5[r11] = r1
            java.lang.String r4 = "%s command (just sent %s command)"
            java.lang.String r3 = java.lang.String.format(r3, r4, r5)
        L_0x0144:
            if (r7 != 0) goto L_0x0159
            java.util.Locale r4 = java.util.Locale.US
            java.lang.Object[] r5 = new java.lang.Object[r2]
            java.lang.Integer r0 = java.lang.Integer.valueOf(r0)
            r5[r12] = r0
            r5[r11] = r3
            java.lang.String r0 = "Received unknown Command Error code 0x%2X in response to %s"
            java.lang.String r7 = java.lang.String.format(r4, r0, r5)
            goto L_0x0167
        L_0x0159:
            java.util.Locale r0 = java.util.Locale.US
            java.lang.Object[] r4 = new java.lang.Object[r2]
            r4[r12] = r7
            r4[r11] = r3
            java.lang.String r3 = "Received Command Error %s in response to %s"
            java.lang.String r7 = java.lang.String.format(r0, r3, r4)
        L_0x0167:
            if (r7 != 0) goto L_0x017b
            java.util.Locale r0 = java.util.Locale.US
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.Integer r3 = java.lang.Integer.valueOf(r15)
            r2[r12] = r3
            r2[r11] = r1
            java.lang.String r1 = "Received unexpected response status 0x%X for %s command"
            java.lang.String r7 = java.lang.String.format(r0, r1, r2)
        L_0x017b:
            com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException r0 = new com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException
            r0.<init>(r7)
            throw r0
        L_0x0181:
            com.qualcomm.hardware.bosch.BHI260IMU$StatusPacket r0 = new com.qualcomm.hardware.bosch.BHI260IMU$StatusPacket
            r0.<init>(r15, r4)
            return r0
        L_0x0187:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.Object[] r2 = new java.lang.Object[r11]
            java.lang.Integer r3 = java.lang.Integer.valueOf(r4)
            r2[r12] = r3
            java.lang.String r3 = "IMU sent payload that was too long (%d bytes)"
            java.lang.String r1 = java.lang.String.format(r1, r3, r2)
            r0.<init>(r1)
            throw r0
        L_0x019d:
            com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException r0 = new com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.Object[] r2 = new java.lang.Object[r11]
            java.lang.Integer r3 = java.lang.Integer.valueOf(r19)
            r2[r12] = r3
            java.lang.String r1 = java.lang.String.format(r1, r5, r2)
            r0.<init>(r1)
            throw r0
        L_0x01b1:
            com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException r0 = new com.qualcomm.hardware.bosch.BHI260IMU$CommandFailureException
            java.util.Locale r1 = java.util.Locale.ENGLISH
            java.lang.Object[] r2 = new java.lang.Object[r11]
            java.lang.Integer r3 = java.lang.Integer.valueOf(r19)
            r2[r12] = r3
            java.lang.String r1 = java.lang.String.format(r1, r5, r2)
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.bosch.BHI260IMU.waitForCommandResponse(com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple, com.qualcomm.hardware.bosch.BHI260IMU$CommandType, int):com.qualcomm.hardware.bosch.BHI260IMU$StatusPacket");
    }

    private void configureSensor(Sensor sensor, float f, int i) {
        if (i <= 16777215) {
            ByteBuffer putFloat = ByteBuffer.allocate(8).order(ByteOrder.LITTLE_ENDIAN).put((byte) sensor.f79id).putFloat(f);
            putFloat.put(TypeConversion.intToByteArray(i, ByteOrder.LITTLE_ENDIAN), 0, 3);
            sendCommand(this.deviceClient, CommandType.CONFIGURE_SENSOR, putFloat.array());
            return;
        }
        throw new IllegalArgumentException("Sensor latency must be less than 1,6777,215 milliseconds");
    }

    private static void setStatusFifoToSynchronousMode(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        write8(i2cDeviceSynchSimple, Register.HOST_INTERFACE_CONTROL, 0, I2cWaitControl.ATOMIC);
    }

    private static <T extends Enum<T>> EnumSet<T> convertIntToEnumSet(int i, Class<T> cls) {
        EnumSet<T> noneOf = EnumSet.noneOf(cls);
        for (Enum enumR : (Enum[]) cls.getEnumConstants()) {
            if (((1 << enumR.ordinal()) & i) == (1 << enumR.ordinal())) {
                noneOf.add(enumR);
            }
        }
        return noneOf;
    }

    private enum Register {
        COMMAND_INPUT(0),
        WAKE_UP_FIFO_OUTPUT(1),
        NON_WAKE_UP_FIFO_OUTPUT(2),
        STATUS_AND_DEBUG_FIFO_OUTPUT(3),
        CHIP_CONTROL(5),
        HOST_INTERFACE_CONTROL(6),
        HOST_INTERRUPT_CONTROL(7),
        RESET_REQUEST(20),
        TIMESTAMP_EVENT_REQUEST(21),
        HOST_CONTROL(22),
        HOST_STATUS(23),
        PRODUCT_IDENTIFIER(28),
        REVISION_IDENTIFIER(29),
        ROM_VERSION(30),
        KERNEL_VERSION(32),
        USER_VERSION(34),
        FEATURE_STATUS(36),
        BOOT_STATUS(37),
        CHIP_ID(43),
        INTERRUPT_STATUS(45),
        ERROR_VALUE(46),
        QUATERNION_OUTPUT(50);
        
        /* access modifiers changed from: private */
        public final int address;

        private Register(int i) {
            this.address = i;
        }
    }

    private enum CommandType {
        ERASE_FLASH(4, 10),
        WRITE_FLASH(5, 11),
        BOOT_FLASH(6, 0),
        CONFIGURE_SENSOR(13, 0);
        
        /* access modifiers changed from: private */

        /* renamed from: id */
        public final int f78id;
        /* access modifiers changed from: private */
        public final int successStatusCode;

        private CommandType(int i, int i2) {
            this.f78id = i;
            this.successStatusCode = i2;
        }

        public static CommandType findById(int i) {
            for (CommandType commandType : values()) {
                if (commandType.f78id == i) {
                    return commandType;
                }
            }
            return null;
        }
    }

    private enum CommandError {
        INCORRECT_LENGTH(1),
        TOO_LONG(2),
        PARAM_WRITE_ERROR(3),
        PARAM_READ_ERROR(4),
        INVALID_COMMAND(5),
        INVALID_PARAM(6),
        COMMAND_FAILED(255);
        
        private final int value;

        private CommandError(int i) {
            this.value = i;
        }

        public static CommandError fromInt(int i) {
            for (CommandError commandError : values()) {
                if (i == commandError.value) {
                    return commandError;
                }
            }
            return null;
        }
    }

    private enum Sensor {
        GAME_ROTATION_VECTOR_WAKE_UP(38),
        GAME_ROTATION_VECTOR_VIA_REGISTER(176);
        
        /* access modifiers changed from: private */

        /* renamed from: id */
        public final int f79id;

        private Sensor(int i) {
            this.f79id = i;
        }
    }

    private static class InitException extends Exception {
        private InitException() {
        }
    }

    private static class CommandFailureException extends Exception {
        public CommandFailureException(String str) {
            super(str);
        }
    }

    private static class StatusPacket {
        public final byte[] payload;
        public final int statusCode;

        private StatusPacket(int i, byte[] bArr) {
            this.statusCode = i;
            this.payload = bArr;
        }
    }

    public static void printByteBuffer(String str, ByteBuffer byteBuffer) {
        int position = byteBuffer.position();
        byteBuffer.position(0);
        int remaining = byteBuffer.remaining();
        StringBuilder sb = new StringBuilder(String.format("%X", new Object[]{Byte.valueOf(byteBuffer.get())}));
        while (byteBuffer.hasRemaining()) {
            sb.append(String.format("-%X", new Object[]{Byte.valueOf(byteBuffer.get())}));
        }
        RobotLog.m43dd(TAG, "%s (%d bytes): %s", str, Integer.valueOf(remaining), sb);
        byteBuffer.position(position);
    }
}
