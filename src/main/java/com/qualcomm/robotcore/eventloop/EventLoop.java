package com.qualcomm.robotcore.eventloop;

import android.hardware.usb.UsbDevice;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.CallbackResult;

public interface EventLoop {
    public static final double TELEMETRY_DEFAULT_INTERVAL = Double.NaN;

    OpModeManagerImpl getOpModeManager();

    void handleUsbModuleAttach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException;

    void handleUsbModuleDetach(RobotUsbModule robotUsbModule) throws RobotCoreException, InterruptedException;

    void init(EventLoopManager eventLoopManager) throws RobotCoreException, InterruptedException;

    void loop();

    void onUsbDeviceAttached(UsbDevice usbDevice);

    void pendUsbDeviceAttachment(SerialNumber serialNumber, long j, TimeUnit timeUnit);

    CallbackResult processCommand(Command command) throws InterruptedException, RobotCoreException;

    void processedRecentlyAttachedUsbDevices() throws RobotCoreException, InterruptedException;

    void refreshUserTelemetry(TelemetryMessage telemetryMessage, double d);

    void requestOpModeStop(OpMode opMode);

    void teardown() throws RobotCoreException, InterruptedException;
}
