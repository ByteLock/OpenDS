package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.C0660R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.usb.ftdi.RobotUsbDeviceFtdi;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.atomic.AtomicBoolean;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderManager;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderProtocolException;
import org.firstinspires.ftc.robotcore.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

class LynxFirmwareUpdater {
    private static final String TAG = "LynxFirmwareUpdater";
    private static final AtomicBoolean firmwareUpdateInProgress = new AtomicBoolean(false);
    private final LynxUsbDeviceImpl device;
    private final boolean isControlHub;

    public LynxFirmwareUpdater(LynxUsbDeviceImpl lynxUsbDeviceImpl) {
        this.device = lynxUsbDeviceImpl;
        this.isControlHub = lynxUsbDeviceImpl.getSerialNumber().isEmbedded();
    }

    public RobotCoreCommandList.LynxFirmwareUpdateResp updateFirmware(RobotCoreCommandList.FWImage fWImage, String str, Consumer<ProgressParameters> consumer) {
        RobotCoreCommandList.LynxFirmwareUpdateResp lynxFirmwareUpdateResp = new RobotCoreCommandList.LynxFirmwareUpdateResp();
        lynxFirmwareUpdateResp.success = false;
        lynxFirmwareUpdateResp.originatorId = str;
        RobotLog.m61vv(TAG, "updateFirmware() serialNumber=%s, fwimage=%s", this.device.getSerialNumber(), fWImage.getName());
        byte[] readBytes = ReadWriteFile.readBytes(fWImage);
        if (readBytes.length <= 0) {
            lynxFirmwareUpdateResp.errorMessage = AppUtil.getDefContext().getString(C0660R.string.lynxFirmwareFileEmpty);
            RobotLog.m60vv(TAG, "Firmware update file was empty");
            return lynxFirmwareUpdateResp;
        }
        try {
            AtomicBoolean atomicBoolean = firmwareUpdateInProgress;
            if (!atomicBoolean.compareAndSet(false, true)) {
                lynxFirmwareUpdateResp.errorMessage = AppUtil.getDefContext().getString(C0660R.string.lynxFirmwareUpdateAlreadyInProgress);
                RobotLog.m60vv(TAG, "Cannot update firmware: a firmware update is already in progress");
                RobotLog.m61vv(TAG, "reengaging lynx usb device %s", this.device.getSerialNumber());
                this.device.engage();
                atomicBoolean.set(false);
                return lynxFirmwareUpdateResp;
            }
            RobotLog.m61vv(TAG, "disengaging lynx usb device %s", this.device.getSerialNumber());
            this.device.disengage();
            int i = 0;
            while (true) {
                if (i >= 2) {
                    break;
                }
                RobotLog.m61vv(TAG, "trying firmware update: count=%d", Integer.valueOf(i));
                AppAliveNotifier.getInstance().notifyAppAlive();
                if (updateFirmwareOnce(readBytes, consumer)) {
                    lynxFirmwareUpdateResp.success = true;
                    break;
                }
                i++;
            }
            RobotLog.m61vv(TAG, "reengaging lynx usb device %s", this.device.getSerialNumber());
            this.device.engage();
            firmwareUpdateInProgress.set(false);
            return lynxFirmwareUpdateResp;
        } catch (RuntimeException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "RuntimeException in updateLynxFirmware()");
            RobotLog.m61vv(TAG, "reengaging lynx usb device %s", this.device.getSerialNumber());
        } catch (Throwable th) {
            RobotLog.m61vv(TAG, "reengaging lynx usb device %s", this.device.getSerialNumber());
            this.device.engage();
            firmwareUpdateInProgress.set(false);
            throw th;
        }
    }

    private boolean updateFirmwareOnce(byte[] bArr, Consumer<ProgressParameters> consumer) {
        if (enterFirmwareUpdateMode()) {
            try {
                new FlashLoaderManager(this.device.getRobotUsbDevice(), bArr).updateFirmware(consumer);
                return true;
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
                RobotLog.m49ee(TAG, "interrupt while updating firmware: serial=%s", this.device.getSerialNumber());
                return false;
            } catch (FlashLoaderProtocolException e) {
                RobotLog.m51ee(TAG, e, "exception while updating firmware: serial=%s", this.device.getSerialNumber());
                return false;
            }
        } else {
            RobotLog.m48ee(TAG, "failed to enter firmware update mode");
            return false;
        }
    }

    private boolean enterFirmwareUpdateMode() {
        boolean z;
        if (this.isControlHub) {
            RobotLog.m60vv(TAG, "putting embedded lynx into firmware update mode");
            z = enterFirmwareUpdateModeControlHub();
        } else {
            RobotLog.m61vv(TAG, "putting lynx(serial=%s) into firmware update mode", this.device.getSerialNumber());
            z = enterFirmwareUpdateModeUSB();
        }
        try {
            Thread.sleep(100);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        return z;
    }

    private boolean enterFirmwareUpdateModeUSB() {
        RobotLog.m61vv(LynxModule.TAG, "enterFirmwareUpdateModeUSB() serial=%s", this.device.getSerialNumber());
        if (!LynxConstants.isEmbeddedSerialNumber(this.device.getSerialNumber())) {
            RobotUsbDeviceFtdi accessCBus = LynxUsbDeviceImpl.accessCBus(this.device.getRobotUsbDevice());
            if (accessCBus != null) {
                try {
                    accessCBus.cbus_setup(3, 3);
                    long j = (long) 75;
                    Thread.sleep(j);
                    accessCBus.cbus_write(1);
                    Thread.sleep(j);
                    accessCBus.cbus_write(0);
                    Thread.sleep(j);
                    accessCBus.cbus_write(1);
                    Thread.sleep(j);
                    accessCBus.cbus_write(3);
                    Thread.sleep(200);
                    return true;
                } catch (InterruptedException | RobotUsbException e) {
                    LynxUsbDeviceImpl.exceptionHandler.handleException(e);
                }
            } else {
                RobotLog.m48ee(TAG, "enterFirmwareUpdateModeUSB() can't access FTDI device");
            }
        } else {
            RobotLog.m48ee(TAG, "enterFirmwareUpdateModeUSB() issued on Control Hub's embedded Expansion Hub");
        }
        return false;
    }

    public boolean enterFirmwareUpdateModeControlHub() {
        RobotLog.m60vv(LynxModule.TAG, "enterFirmwareUpdateModeControlHub()");
        if (LynxConstants.isRevControlHub()) {
            try {
                boolean state = AndroidBoard.getInstance().getAndroidBoardIsPresentPin().getState();
                RobotLog.m61vv(LynxModule.TAG, "fw update embedded usb device: isPresent: was=%s", Boolean.valueOf(state));
                if (!state) {
                    AndroidBoard.getInstance().getAndroidBoardIsPresentPin().setState(true);
                    Thread.sleep((long) 75);
                }
                AndroidBoard.getInstance().getProgrammingPin().setState(true);
                long j = (long) 75;
                Thread.sleep(j);
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(true);
                Thread.sleep(j);
                AndroidBoard.getInstance().getLynxModuleResetPin().setState(false);
                Thread.sleep(j);
                AndroidBoard.getInstance().getProgrammingPin().setState(false);
                Thread.sleep(j);
                return true;
            } catch (InterruptedException unused) {
                Thread.currentThread().interrupt();
            }
        } else {
            RobotLog.m48ee(TAG, "enterFirmwareUpdateModeControlHub() issued on non-Control Hub");
            return false;
        }
    }
}
