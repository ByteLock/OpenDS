package com.qualcomm.ftccommon;

import android.app.Activity;
import android.hardware.usb.UsbDevice;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.eventloop.opmode.OpModeRegister;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.concurrent.TimeUnit;

public class FtcEventLoopIdle extends FtcEventLoopBase {
    public static final String TAG = "FtcEventLoopIdle";

    public OpModeManagerImpl getOpModeManager() {
        return null;
    }

    public void handleUsbModuleAttach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
    }

    public void handleUsbModuleDetach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException {
    }

    public void onUsbDeviceAttached(UsbDevice usbDevice) {
    }

    public void pendUsbDeviceAttachment(SerialNumber serialNumber, long j, TimeUnit timeUnit) {
    }

    public void processedRecentlyAttachedUsbDevices() throws RobotCoreException, InterruptedException {
    }

    public void refreshUserTelemetry(TelemetryMessage telemetryMessage, double d) {
    }

    public void requestOpModeStop(OpMode opMode) {
    }

    public FtcEventLoopIdle(HardwareFactory hardwareFactory, OpModeRegister opModeRegister, UpdateUI.Callback callback, Activity activity) {
        super(hardwareFactory, opModeRegister, callback, activity);
    }

    public void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException {
        RobotLog.m54ii(TAG, "------- idle init --------");
        try {
            super.init(eventLoopManager);
        } catch (Exception e) {
            RobotLog.m62vv(TAG, (Throwable) e, "exception in idle event loop init; ignored");
        }
    }

    public void loop() {
        try {
            super.loop();
            checkForChangedOpModes();
        } catch (Exception e) {
            RobotLog.m62vv(TAG, (Throwable) e, "exception in idle event loop loop; ignored");
        }
    }

    public void teardown() throws RobotCoreException, InterruptedException {
        RobotLog.m54ii(TAG, "------- idle teardown ----");
        try {
            super.teardown();
        } catch (Exception e) {
            RobotLog.m62vv(TAG, (Throwable) e, "exception in idle event loop teardown; ignored");
        }
    }
}
