package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeServices;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryImpl;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryInternal;
import p007fi.iki.elonen.NanoHTTPD;

public abstract class OpMode {
    public Gamepad gamepad1;
    public Gamepad gamepad2;
    public HardwareMap hardwareMap;
    public OpModeServices internalOpModeServices;
    public int msStuckDetectInit;
    public int msStuckDetectInitLoop;
    public int msStuckDetectLoop;
    public int msStuckDetectStart;
    public int msStuckDetectStop;
    private long startTime;
    public Telemetry telemetry;
    public double time;

    public abstract void init();

    public void init_loop() {
    }

    public abstract void loop();

    public void start() {
    }

    public void stop() {
    }

    public OpMode() {
        this.gamepad1 = null;
        this.gamepad2 = null;
        this.telemetry = new TelemetryImpl(this);
        this.hardwareMap = null;
        this.time = LynxServoController.apiPositionFirst;
        this.startTime = 0;
        this.msStuckDetectInit = NanoHTTPD.SOCKET_READ_TIMEOUT;
        this.msStuckDetectInitLoop = NanoHTTPD.SOCKET_READ_TIMEOUT;
        this.msStuckDetectStart = NanoHTTPD.SOCKET_READ_TIMEOUT;
        this.msStuckDetectLoop = NanoHTTPD.SOCKET_READ_TIMEOUT;
        this.msStuckDetectStop = 900;
        this.internalOpModeServices = null;
        this.startTime = System.nanoTime();
    }

    public final void requestOpModeStop() {
        this.internalOpModeServices.requestOpModeStop(this);
    }

    public final void terminateOpModeNow() {
        throw new OpModeManagerImpl.ForceStopException();
    }

    public double getRuntime() {
        return ((double) (System.nanoTime() - this.startTime)) / ((double) TimeUnit.SECONDS.toNanos(1));
    }

    public void resetRuntime() {
        this.startTime = System.nanoTime();
    }

    public void updateTelemetry(Telemetry telemetry2) {
        telemetry2.update();
    }

    public void internalPreInit() {
        Telemetry telemetry2 = this.telemetry;
        if (telemetry2 instanceof TelemetryInternal) {
            ((TelemetryInternal) telemetry2).resetTelemetryForOpMode();
        }
    }

    public void internalPostInitLoop() {
        this.telemetry.update();
    }

    public void internalPostLoop() {
        this.telemetry.update();
    }

    public final void internalUpdateTelemetryNow(TelemetryMessage telemetryMessage) {
        this.internalOpModeServices.refreshUserTelemetry(telemetryMessage, LynxServoController.apiPositionFirst);
    }
}
