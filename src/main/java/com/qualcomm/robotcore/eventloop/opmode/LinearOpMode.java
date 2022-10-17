package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.opmode.TelemetryInternal;

public abstract class LinearOpMode extends OpMode {
    private ExecutorService executorService = null;
    private LinearOpModeHelper helper = null;
    private volatile boolean isStarted = false;
    private final Object runningNotifier = new Object();
    private volatile boolean stopRequested = false;
    private boolean userMonitoredForStart = false;

    public abstract void runOpMode() throws InterruptedException;

    public void waitForStart() {
        while (!isStarted()) {
            synchronized (this.runningNotifier) {
                try {
                    this.runningNotifier.wait();
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                    return;
                } catch (Throwable th) {
                    throw th;
                }
            }
        }
    }

    public final void idle() {
        Thread.yield();
    }

    public final void sleep(long j) {
        try {
            Thread.sleep(j);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
    }

    public final boolean opModeIsActive() {
        boolean z = !isStopRequested() && isStarted();
        if (z) {
            idle();
        }
        return z;
    }

    public final boolean opModeInInit() {
        return !isStarted() && !isStopRequested();
    }

    public final boolean isStarted() {
        if (this.isStarted) {
            this.userMonitoredForStart = true;
        }
        if (this.isStarted || Thread.currentThread().isInterrupted()) {
            return true;
        }
        return false;
    }

    public final boolean isStopRequested() {
        return this.stopRequested || Thread.currentThread().isInterrupted();
    }

    public final void init() {
        this.executorService = ThreadPool.newSingleThreadExecutor("LinearOpMode");
        this.helper = new LinearOpModeHelper();
        this.isStarted = false;
        this.stopRequested = false;
        this.executorService.execute(this.helper);
    }

    public final void init_loop() {
        handleLoop();
    }

    public final void start() {
        this.stopRequested = false;
        this.isStarted = true;
        synchronized (this.runningNotifier) {
            this.runningNotifier.notifyAll();
        }
    }

    public final void loop() {
        handleLoop();
    }

    public final void stop() {
        LinearOpModeHelper linearOpModeHelper;
        if (!this.stopRequested && (linearOpModeHelper = this.helper) != null) {
            if (!this.userMonitoredForStart && linearOpModeHelper.userMethodReturned) {
                RobotLog.addGlobalWarningMessage("The OpMode which was just initialized ended prematurely as a result of not monitoring for the start condition. Did you forget to call waitForStart()?");
            }
            this.stopRequested = true;
            ExecutorService executorService2 = this.executorService;
            if (executorService2 != null) {
                executorService2.shutdownNow();
                try {
                    ThreadPool.awaitTermination(this.executorService, 100, TimeUnit.DAYS, "user linear op mode");
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void handleLoop() {
        if (this.helper.hasRuntimeException()) {
            throw this.helper.getRuntimeException();
        } else if (!this.helper.hasNoClassDefFoundError()) {
            synchronized (this.runningNotifier) {
                this.runningNotifier.notifyAll();
            }
        } else {
            throw this.helper.getNoClassDefFoundError();
        }
    }

    private class LinearOpModeHelper implements Runnable {
        private static final String TAG = "LinearOpModeHelper";
        protected RuntimeException exception = null;
        protected boolean isShutdown = false;
        protected NoClassDefFoundError noClassDefFoundError = null;
        protected volatile boolean userMethodReturned = false;

        public LinearOpModeHelper() {
        }

        public void run() {
            ThreadPool.logThreadLifeCycle("LinearOpMode main", new Runnable() {
                public void run() {
                    C07131 r1;
                    LinearOpModeHelper.this.exception = null;
                    LinearOpModeHelper.this.noClassDefFoundError = null;
                    LinearOpModeHelper.this.isShutdown = false;
                    try {
                        LinearOpMode.this.runOpMode();
                        LinearOpModeHelper.this.userMethodReturned = true;
                        RobotLog.m42dd(LinearOpModeHelper.TAG, "User runOpModeMethod exited");
                        LinearOpMode.this.requestOpModeStop();
                        r1 = new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        };
                    } catch (InterruptedException unused) {
                        RobotLog.m40d("LinearOpMode received an InterruptedException; shutting down this linear op mode");
                        LinearOpMode.this.requestOpModeStop();
                        r1 = new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        };
                    } catch (CancellationException unused2) {
                        RobotLog.m40d("LinearOpMode received a CancellationException; shutting down this linear op mode");
                        LinearOpMode.this.requestOpModeStop();
                        r1 = new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        };
                    } catch (RuntimeException e) {
                        LinearOpModeHelper.this.exception = e;
                        r1 = new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        };
                    } catch (NoClassDefFoundError e2) {
                        LinearOpModeHelper.this.noClassDefFoundError = e2;
                        r1 = new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        };
                    } catch (Throwable th) {
                        I2cWarningManager.suppressNewProblemDeviceWarningsWhile(new Runnable() {
                            public void run() {
                                if (LinearOpMode.this.telemetry instanceof TelemetryInternal) {
                                    LinearOpMode.this.telemetry.setMsTransmissionInterval(0);
                                    ((TelemetryInternal) LinearOpMode.this.telemetry).tryUpdateIfDirty();
                                }
                            }
                        });
                        LinearOpModeHelper.this.isShutdown = true;
                        throw th;
                    }
                    I2cWarningManager.suppressNewProblemDeviceWarningsWhile(r1);
                    LinearOpModeHelper.this.isShutdown = true;
                }
            });
        }

        public boolean hasRuntimeException() {
            return this.exception != null;
        }

        public RuntimeException getRuntimeException() {
            return this.exception;
        }

        public boolean hasNoClassDefFoundError() {
            return this.noClassDefFoundError != null;
        }

        public NoClassDefFoundError getNoClassDefFoundError() {
            return this.noClassDefFoundError;
        }

        public boolean isShutdown() {
            return this.isShutdown;
        }
    }

    public void internalPostInitLoop() {
        if (this.telemetry instanceof TelemetryInternal) {
            ((TelemetryInternal) this.telemetry).tryUpdateIfDirty();
        }
    }

    public void internalPostLoop() {
        if (this.telemetry instanceof TelemetryInternal) {
            ((TelemetryInternal) this.telemetry).tryUpdateIfDirty();
        }
    }
}
