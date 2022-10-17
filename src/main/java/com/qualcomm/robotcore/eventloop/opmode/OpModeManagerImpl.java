package com.qualcomm.robotcore.eventloop.opmode;

import android.app.Activity;
import android.content.Context;
import android.os.Debug;
import android.util.Log;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.LightSensor;
import com.qualcomm.robotcore.hardware.RobotCoreLynxController;
import com.qualcomm.robotcore.hardware.RobotCoreLynxModule;
import com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.util.HashSet;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.AtomicReference;
import org.firstinspires.ftc.robotcore.internal.android.dex.DexFormat;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeServices;
import org.firstinspires.ftc.robotcore.internal.opmode.RegisteredOpModes;
import org.firstinspires.ftc.robotcore.ui.GamepadUser;
import org.firstinspires.inspection.InspectionState;

public class OpModeManagerImpl implements OpModeServices, OpModeManagerNotifier {
    public static final OpMode DEFAULT_OP_MODE = new DefaultOpMode();
    public static final String DEFAULT_OP_MODE_NAME = "$Stop$Robot$";
    public static final String TAG = "OpModeManager";
    protected static final WeakHashMap<Activity, OpModeManagerImpl> mapActivityToOpModeManager = new WeakHashMap<>();
    protected static int matchNumber = 0;
    protected OpMode activeOpMode = DEFAULT_OP_MODE;
    protected String activeOpModeName = "$Stop$Robot$";
    protected boolean callToInitNeeded = false;
    protected boolean callToStartNeeded = false;
    protected Context context;
    protected EventLoopManager eventLoopManager = null;
    protected boolean gamepadResetNeeded = false;
    protected HardwareMap hardwareMap = null;
    protected final WeakReferenceSet<OpModeManagerNotifier.Notifications> listeners = new WeakReferenceSet<>();
    protected AtomicReference<OpModeStateTransition> nextOpModeState = new AtomicReference<>((Object) null);
    protected OpModeState opModeState = OpModeState.INIT;
    protected boolean opModeSwapNeeded = false;
    protected String queuedOpModeName = "$Stop$Robot$";
    protected boolean skipCallToStop = false;
    protected OpModeStuckCodeMonitor stuckMonitor = null;
    protected boolean telemetryClearNeeded = false;

    public static class ForceStopException extends RuntimeException {
    }

    protected enum OpModeState {
        INIT,
        LOOPING
    }

    class OpModeStateTransition {
        Boolean callToInitNeeded = null;
        Boolean callToStartNeeded = null;
        Boolean gamepadResetNeeded = null;
        Boolean opModeSwapNeeded = null;
        String queuedOpModeName = null;
        Boolean telemetryClearNeeded = null;

        OpModeStateTransition() {
        }

        /* access modifiers changed from: package-private */
        public void apply() {
            String str = this.queuedOpModeName;
            if (str != null) {
                OpModeManagerImpl.this.queuedOpModeName = str;
            }
            Boolean bool = this.opModeSwapNeeded;
            if (bool != null) {
                OpModeManagerImpl.this.opModeSwapNeeded = bool.booleanValue();
            }
            Boolean bool2 = this.callToInitNeeded;
            if (bool2 != null) {
                OpModeManagerImpl.this.callToInitNeeded = bool2.booleanValue();
            }
            Boolean bool3 = this.gamepadResetNeeded;
            if (bool3 != null) {
                OpModeManagerImpl.this.gamepadResetNeeded = bool3.booleanValue();
            }
            Boolean bool4 = this.telemetryClearNeeded;
            if (bool4 != null) {
                OpModeManagerImpl.this.telemetryClearNeeded = bool4.booleanValue();
            }
            Boolean bool5 = this.callToStartNeeded;
            if (bool5 != null) {
                OpModeManagerImpl.this.callToStartNeeded = bool5.booleanValue();
            }
        }

        /* access modifiers changed from: package-private */
        public OpModeStateTransition copy() {
            OpModeStateTransition opModeStateTransition = new OpModeStateTransition();
            opModeStateTransition.queuedOpModeName = this.queuedOpModeName;
            opModeStateTransition.opModeSwapNeeded = this.opModeSwapNeeded;
            opModeStateTransition.callToInitNeeded = this.callToInitNeeded;
            opModeStateTransition.gamepadResetNeeded = this.gamepadResetNeeded;
            opModeStateTransition.telemetryClearNeeded = this.telemetryClearNeeded;
            opModeStateTransition.callToStartNeeded = this.callToStartNeeded;
            return opModeStateTransition;
        }
    }

    public OpModeManagerImpl(Activity activity, HardwareMap hardwareMap2) {
        this.hardwareMap = hardwareMap2;
        initActiveOpMode("$Stop$Robot$");
        this.context = activity;
        WeakHashMap<Activity, OpModeManagerImpl> weakHashMap = mapActivityToOpModeManager;
        synchronized (weakHashMap) {
            weakHashMap.put(activity, this);
        }
    }

    public static OpModeManagerImpl getOpModeManagerOfActivity(Activity activity) {
        OpModeManagerImpl opModeManagerImpl;
        WeakHashMap<Activity, OpModeManagerImpl> weakHashMap = mapActivityToOpModeManager;
        synchronized (weakHashMap) {
            opModeManagerImpl = weakHashMap.get(activity);
        }
        return opModeManagerImpl;
    }

    public void init(EventLoopManager eventLoopManager2) {
        this.stuckMonitor = new OpModeStuckCodeMonitor();
        this.eventLoopManager = eventLoopManager2;
    }

    public void teardown() {
        this.stuckMonitor.shutdown();
    }

    public OpMode registerListener(OpModeManagerNotifier.Notifications notifications) {
        OpMode opMode;
        synchronized (this.listeners) {
            this.listeners.add(notifications);
            opMode = this.activeOpMode;
        }
        return opMode;
    }

    public void unregisterListener(OpModeManagerNotifier.Notifications notifications) {
        synchronized (this.listeners) {
            this.listeners.remove(notifications);
        }
    }

    /* access modifiers changed from: protected */
    public void setActiveOpMode(OpMode opMode, String str) {
        synchronized (this.listeners) {
            this.activeOpMode = opMode;
            this.activeOpModeName = str;
        }
    }

    public void setHardwareMap(HardwareMap hardwareMap2) {
        this.hardwareMap = hardwareMap2;
    }

    public HardwareMap getHardwareMap() {
        return this.hardwareMap;
    }

    public RobotState getRobotState() {
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        if (eventLoopManager2 != null) {
            return eventLoopManager2.state;
        }
        return RobotState.UNKNOWN;
    }

    public String getActiveOpModeName() {
        return this.activeOpModeName;
    }

    public OpMode getActiveOpMode() {
        return this.activeOpMode;
    }

    /* access modifiers changed from: protected */
    public void doMatchLoggingWork(String str) {
        if (!str.equals("$Stop$Robot$")) {
            try {
                RobotLog.startMatchLogging(this.context, str, matchNumber);
            } catch (RobotCoreException e) {
                RobotLog.m48ee(TAG, "Could not start match logging");
                e.printStackTrace();
            }
        } else {
            RobotLog.stopMatchLogging();
        }
    }

    public void setMatchNumber(int i) {
        matchNumber = i;
    }

    public void initActiveOpMode(String str) {
        OpModeStateTransition opModeStateTransition = new OpModeStateTransition();
        opModeStateTransition.queuedOpModeName = str;
        opModeStateTransition.opModeSwapNeeded = true;
        opModeStateTransition.callToInitNeeded = true;
        opModeStateTransition.gamepadResetNeeded = true;
        opModeStateTransition.telemetryClearNeeded = Boolean.valueOf(true ^ str.equals("$Stop$Robot$"));
        opModeStateTransition.callToStartNeeded = false;
        doMatchLoggingWork(str);
        this.nextOpModeState.set(opModeStateTransition);
    }

    public void startActiveOpMode() {
        OpModeStateTransition opModeStateTransition;
        OpModeStateTransition opModeStateTransition2 = null;
        while (true) {
            if (opModeStateTransition2 != null) {
                opModeStateTransition = opModeStateTransition2.copy();
            } else {
                opModeStateTransition = new OpModeStateTransition();
            }
            opModeStateTransition.callToStartNeeded = true;
            if (!this.nextOpModeState.compareAndSet(opModeStateTransition2, opModeStateTransition)) {
                Thread.yield();
                opModeStateTransition2 = this.nextOpModeState.get();
            } else {
                return;
            }
        }
    }

    public void stopActiveOpMode() {
        callActiveOpModeStop();
        RobotLog.stopMatchLogging();
        initActiveOpMode("$Stop$Robot$");
    }

    public void runActiveOpMode(Gamepad[] gamepadArr) {
        OpModeStateTransition andSet = this.nextOpModeState.getAndSet((Object) null);
        if (andSet != null) {
            andSet.apply();
        }
        OpMode opMode = this.activeOpMode;
        opMode.time = opMode.getRuntime();
        this.activeOpMode.gamepad1 = gamepadArr[0];
        this.activeOpMode.gamepad2 = gamepadArr[1];
        if (this.gamepadResetNeeded) {
            this.activeOpMode.gamepad1.reset();
            this.activeOpMode.gamepad2.reset();
            this.activeOpMode.gamepad1.setUserForEffects(GamepadUser.ONE.f280id);
            this.activeOpMode.gamepad2.setUserForEffects(GamepadUser.TWO.f280id);
            this.gamepadResetNeeded = false;
        }
        if (this.telemetryClearNeeded && this.eventLoopManager != null) {
            TelemetryMessage telemetryMessage = new TelemetryMessage();
            telemetryMessage.addData(DexFormat.MAGIC_SUFFIX, InspectionState.NO_VERSION);
            this.eventLoopManager.sendTelemetryData(telemetryMessage);
            this.telemetryClearNeeded = false;
            RobotLog.clearGlobalErrorMsg();
            RobotLog.clearGlobalWarningMsg();
        }
        if (this.opModeSwapNeeded) {
            if (!this.skipCallToStop || (this.activeOpMode instanceof LinearOpMode)) {
                callActiveOpModeStop();
            }
            this.skipCallToStop = false;
            performOpModeSwap();
            this.opModeSwapNeeded = false;
        }
        if (this.callToInitNeeded) {
            this.activeOpMode.gamepad1 = gamepadArr[0];
            this.activeOpMode.gamepad2 = gamepadArr[1];
            this.activeOpMode.hardwareMap = this.hardwareMap;
            this.activeOpMode.internalOpModeServices = this;
            if (!this.activeOpModeName.equals("$Stop$Robot$")) {
                resetHardwareForOpMode();
            }
            this.activeOpMode.resetRuntime();
            callActiveOpModeInit();
            this.opModeState = OpModeState.INIT;
            this.callToInitNeeded = false;
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_INIT_OP_MODE, this.activeOpModeName));
        } else if (this.callToStartNeeded) {
            callActiveOpModeStart();
            this.opModeState = OpModeState.LOOPING;
            this.callToStartNeeded = false;
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_RUN_OP_MODE, this.activeOpModeName));
        } else if (this.opModeState == OpModeState.INIT) {
            callActiveOpModeInitLoop();
        } else if (this.opModeState == OpModeState.LOOPING) {
            callActiveOpModeLoop();
        }
    }

    /* access modifiers changed from: protected */
    public void resetHardwareForOpMode() {
        HashSet<HardwareDevice> hashSet = new HashSet<>();
        hashSet.addAll(this.hardwareMap.getAll(RobotCoreLynxModule.class));
        hashSet.addAll(this.hardwareMap.getAll(RobotCoreLynxController.class));
        for (HardwareDevice resetDeviceConfigurationForOpMode : hashSet) {
            resetDeviceConfigurationForOpMode.resetDeviceConfigurationForOpMode();
        }
        for (HardwareDevice next : this.hardwareMap.unsafeIterable()) {
            if (!hashSet.contains(next)) {
                next.resetDeviceConfigurationForOpMode();
            }
        }
    }

    private void performOpModeSwap() {
        RobotLog.m52i("Attempting to switch to op mode " + this.queuedOpModeName);
        OpMode opMode = RegisteredOpModes.getInstance().getOpMode(this.queuedOpModeName);
        if (opMode != null) {
            setActiveOpMode(opMode, this.queuedOpModeName);
        } else {
            failedToSwapOpMode();
        }
    }

    private void failedToSwapOpMode(Exception exc) {
        RobotLog.m50ee(TAG, (Throwable) exc, "Unable to start op mode " + this.activeOpModeName);
        setActiveOpMode(DEFAULT_OP_MODE, "$Stop$Robot$");
    }

    private void failedToSwapOpMode() {
        RobotLog.m48ee(TAG, "Unable to start op mode " + this.activeOpModeName);
        setActiveOpMode(DEFAULT_OP_MODE, "$Stop$Robot$");
    }

    /* access modifiers changed from: protected */
    public void callActiveOpModeStop() {
        try {
            detectStuck(this.activeOpMode.msStuckDetectStop, "stop()", new Runnable() {
                public void run() {
                    OpModeManagerImpl.this.activeOpMode.stop();
                }
            });
        } catch (ForceStopException unused) {
        } catch (Exception e) {
            handleUserCodeException(e);
        }
        synchronized (this.listeners) {
            Iterator<OpModeManagerNotifier.Notifications> it = this.listeners.iterator();
            while (it.hasNext()) {
                it.next().onOpModePostStop(this.activeOpMode);
            }
        }
        for (HardwareDevice next : this.hardwareMap.unsafeIterable()) {
            if (next instanceof OpModeManagerNotifier.Notifications) {
                ((OpModeManagerNotifier.Notifications) next).onOpModePostStop(this.activeOpMode);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void detectStuck(int i, String str, Runnable runnable) {
        detectStuck(i, str, runnable, false);
    }

    /* access modifiers changed from: protected */
    public void detectStuck(int i, String str, Runnable runnable, boolean z) {
        this.stuckMonitor.startMonitoring(i, str, z);
        try {
            runnable.run();
        } finally {
            this.stuckMonitor.stopMonitoring();
            try {
                this.stuckMonitor.acquired.await();
            } catch (InterruptedException e) {
                e.printStackTrace();
                Thread.currentThread().interrupt();
            }
        }
    }

    protected class OpModeStuckCodeMonitor {
        CountDownLatch acquired = null;
        boolean debuggerDetected = false;
        ExecutorService executorService = ThreadPool.newSingleThreadExecutor("OpModeStuckCodeMonitor");
        String method;
        int msTimeout;
        Semaphore stopped = new Semaphore(0);

        protected OpModeStuckCodeMonitor() {
        }

        public void startMonitoring(int i, String str, boolean z) {
            CountDownLatch countDownLatch = this.acquired;
            if (countDownLatch != null) {
                try {
                    countDownLatch.await();
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
            this.msTimeout = i;
            this.method = str;
            this.stopped.drainPermits();
            this.acquired = new CountDownLatch(1);
            this.executorService.execute(new Runner());
            if (z) {
                this.debuggerDetected = false;
            }
        }

        public void stopMonitoring() {
            this.stopped.release();
        }

        public void shutdown() {
            this.executorService.shutdownNow();
        }

        /* access modifiers changed from: protected */
        public boolean checkForDebugger() {
            boolean z = this.debuggerDetected || Debug.isDebuggerConnected();
            this.debuggerDetected = z;
            return z;
        }

        protected class Runner implements Runnable {
            static final String msgForceStoppedCommon = "User OpMode was stuck in %s, but was able to be force stopped without restarting the app. ";
            static final String msgForceStoppedPopupIterative = "User OpMode was stuck in %s, but was able to be force stopped without restarting the app. It appears this was an iterative OpMode; make sure you aren't using your own loops.";
            static final String msgForceStoppedPopupLinear = "User OpMode was stuck in %s, but was able to be force stopped without restarting the app. It appears this was a linear OpMode; make sure you are calling opModeIsActive() in any loops.";

            protected Runner() {
            }

            /* JADX WARNING: Removed duplicated region for block: B:48:0x0168 A[SYNTHETIC, Splitter:B:48:0x0168] */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public void run() {
                /*
                    r7 = this;
                    r0 = 0
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    boolean r1 = r1.checkForDebugger()     // Catch:{ InterruptedException -> 0x0166 }
                    if (r1 == 0) goto L_0x0011
                L_0x0009:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r0 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this
                    java.util.concurrent.CountDownLatch r0 = r0.acquired
                    r0.countDown()
                    return
                L_0x0011:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.concurrent.Semaphore r1 = r1.stopped     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r2 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    int r2 = r2.msTimeout     // Catch:{ InterruptedException -> 0x0166 }
                    long r2 = (long) r2     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.concurrent.TimeUnit r4 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ InterruptedException -> 0x0166 }
                    boolean r1 = r1.tryAcquire(r2, r4)     // Catch:{ InterruptedException -> 0x0166 }
                    if (r1 != 0) goto L_0x0174
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.HardwareMap r1 = r1.hardwareMap     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.Class<com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice> r2 = com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice.class
                    java.util.List r1 = r1.getAll(r2)     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.Iterator r1 = r1.iterator()     // Catch:{ InterruptedException -> 0x0166 }
                L_0x0032:
                    boolean r2 = r1.hasNext()     // Catch:{ InterruptedException -> 0x0166 }
                    r3 = 1
                    if (r2 == 0) goto L_0x0043
                    java.lang.Object r2 = r1.next()     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r2 = (com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice) r2     // Catch:{ InterruptedException -> 0x0166 }
                    r2.setThrowOnNetworkLockAcquisition(r3)     // Catch:{ InterruptedException -> 0x0166 }
                    goto L_0x0032
                L_0x0043:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.concurrent.Semaphore r1 = r1.stopped     // Catch:{ InterruptedException -> 0x0166 }
                    r4 = 100
                    java.util.concurrent.TimeUnit r2 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ InterruptedException -> 0x0166 }
                    boolean r1 = r1.tryAcquire(r4, r2)     // Catch:{ InterruptedException -> 0x0166 }
                    if (r1 == 0) goto L_0x0097
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpMode r1 = r1.activeOpMode     // Catch:{ InterruptedException -> 0x0166 }
                    boolean r1 = r1 instanceof com.qualcomm.robotcore.eventloop.opmode.LinearOpMode     // Catch:{ InterruptedException -> 0x0166 }
                    if (r1 == 0) goto L_0x005e
                    java.lang.String r1 = "User OpMode was stuck in %s, but was able to be force stopped without restarting the app. It appears this was a linear OpMode; make sure you are calling opModeIsActive() in any loops."
                    goto L_0x0060
                L_0x005e:
                    java.lang.String r1 = "User OpMode was stuck in %s, but was able to be force stopped without restarting the app. It appears this was an iterative OpMode; make sure you aren't using your own loops."
                L_0x0060:
                    org.firstinspires.ftc.robotcore.internal.system.AppUtil r2 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getInstance()     // Catch:{ InterruptedException -> 0x0166 }
                    org.firstinspires.ftc.robotcore.internal.ui.UILocation r4 = org.firstinspires.ftc.robotcore.internal.p013ui.UILocation.BOTH     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r5 = "OpMode Force-Stopped"
                    java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r6 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r6 = r6.method     // Catch:{ InterruptedException -> 0x0166 }
                    r3[r0] = r6     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r1 = java.lang.String.format(r1, r3)     // Catch:{ InterruptedException -> 0x0166 }
                    r2.showAlertDialog(r4, r5, r1)     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.HardwareMap r1 = r1.hardwareMap     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.Class<com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice> r2 = com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice.class
                    java.util.List r1 = r1.getAll(r2)     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.Iterator r1 = r1.iterator()     // Catch:{ InterruptedException -> 0x0166 }
                L_0x0087:
                    boolean r2 = r1.hasNext()     // Catch:{ InterruptedException -> 0x0166 }
                    if (r2 == 0) goto L_0x0009
                    java.lang.Object r2 = r1.next()     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r2 = (com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice) r2     // Catch:{ InterruptedException -> 0x0166 }
                    r2.setThrowOnNetworkLockAcquisition(r0)     // Catch:{ InterruptedException -> 0x0166 }
                    goto L_0x0087
                L_0x0097:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.HardwareMap r1 = r1.hardwareMap     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.Class<com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice> r2 = com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice.class
                    java.util.List r1 = r1.getAll(r2)     // Catch:{ InterruptedException -> 0x0166 }
                    java.util.Iterator r1 = r1.iterator()     // Catch:{ InterruptedException -> 0x0166 }
                L_0x00a7:
                    boolean r2 = r1.hasNext()     // Catch:{ InterruptedException -> 0x0166 }
                    if (r2 == 0) goto L_0x00b7
                    java.lang.Object r2 = r1.next()     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r2 = (com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice) r2     // Catch:{ InterruptedException -> 0x0166 }
                    r2.setThrowOnNetworkLockAcquisition(r0)     // Catch:{ InterruptedException -> 0x0166 }
                    goto L_0x00a7
                L_0x00b7:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    android.content.Context r1 = r1.context     // Catch:{ InterruptedException -> 0x0166 }
                    int r2 = com.qualcomm.robotcore.C0705R.string.errorOpModeStuck     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r1 = r1.getString(r2)     // Catch:{ InterruptedException -> 0x0166 }
                    r2 = 2
                    java.lang.Object[] r2 = new java.lang.Object[r2]     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r4 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r4 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r4 = r4.activeOpModeName     // Catch:{ InterruptedException -> 0x0166 }
                    r2[r0] = r4     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r4 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r4 = r4.method     // Catch:{ InterruptedException -> 0x0166 }
                    r2[r3] = r4     // Catch:{ InterruptedException -> 0x0166 }
                    java.lang.String r1 = java.lang.String.format(r1, r2)     // Catch:{ InterruptedException -> 0x0166 }
                    boolean r2 = com.qualcomm.robotcore.util.RobotLog.setGlobalErrorMsg(r1)     // Catch:{ InterruptedException -> 0x0166 }
                    com.qualcomm.robotcore.util.RobotLog.m46e(r1)     // Catch:{ InterruptedException -> 0x0162 }
                    java.util.concurrent.CountDownLatch r1 = new java.util.concurrent.CountDownLatch     // Catch:{ Exception -> 0x0106 }
                    r1.<init>(r3)     // Catch:{ Exception -> 0x0106 }
                    java.lang.Thread r4 = new java.lang.Thread     // Catch:{ Exception -> 0x0106 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor$Runner$1 r5 = new com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor$Runner$1     // Catch:{ Exception -> 0x0106 }
                    r5.<init>(r1)     // Catch:{ Exception -> 0x0106 }
                    r4.<init>(r5)     // Catch:{ Exception -> 0x0106 }
                    r4.start()     // Catch:{ Exception -> 0x0106 }
                    r4 = 250(0xfa, double:1.235E-321)
                    java.util.concurrent.TimeUnit r6 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ Exception -> 0x0106 }
                    boolean r1 = r1.await(r4, r6)     // Catch:{ Exception -> 0x0106 }
                    if (r1 == 0) goto L_0x0101
                    java.lang.String r1 = "Successfully sent failsafe commands to Lynx modules before app restart"
                    com.qualcomm.robotcore.util.RobotLog.m46e(r1)     // Catch:{ Exception -> 0x0106 }
                    goto L_0x0106
                L_0x0101:
                    java.lang.String r1 = "Timed out while sending failsafe commands to Lynx modules before app restart"
                    com.qualcomm.robotcore.util.RobotLog.m46e(r1)     // Catch:{ Exception -> 0x0106 }
                L_0x0106:
                    java.lang.String r1 = "Begin thread dump"
                    com.qualcomm.robotcore.util.RobotLog.m46e(r1)     // Catch:{ InterruptedException -> 0x0162 }
                    java.util.Map r1 = java.lang.Thread.getAllStackTraces()     // Catch:{ InterruptedException -> 0x0162 }
                    java.util.Set r1 = r1.entrySet()     // Catch:{ InterruptedException -> 0x0162 }
                    java.util.Iterator r1 = r1.iterator()     // Catch:{ InterruptedException -> 0x0162 }
                L_0x0117:
                    boolean r4 = r1.hasNext()     // Catch:{ InterruptedException -> 0x0162 }
                    if (r4 == 0) goto L_0x0133
                    java.lang.Object r4 = r1.next()     // Catch:{ InterruptedException -> 0x0162 }
                    java.util.Map$Entry r4 = (java.util.Map.Entry) r4     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.Object r5 = r4.getKey()     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.Thread r5 = (java.lang.Thread) r5     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.Object r4 = r4.getValue()     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.StackTraceElement[] r4 = (java.lang.StackTraceElement[]) r4     // Catch:{ InterruptedException -> 0x0162 }
                    com.qualcomm.robotcore.util.RobotLog.logStackTrace((java.lang.Thread) r5, (java.lang.StackTraceElement[]) r4)     // Catch:{ InterruptedException -> 0x0162 }
                    goto L_0x0117
                L_0x0133:
                    org.firstinspires.ftc.robotcore.internal.system.AppUtil r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getInstance()     // Catch:{ InterruptedException -> 0x0162 }
                    org.firstinspires.ftc.robotcore.internal.ui.UILocation r4 = org.firstinspires.ftc.robotcore.internal.p013ui.UILocation.BOTH     // Catch:{ InterruptedException -> 0x0162 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r5 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0162 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl r5 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.this     // Catch:{ InterruptedException -> 0x0162 }
                    android.content.Context r5 = r5.context     // Catch:{ InterruptedException -> 0x0162 }
                    int r6 = com.qualcomm.robotcore.C0705R.string.toastOpModeStuck     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.String r5 = r5.getString(r6)     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ InterruptedException -> 0x0162 }
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r6 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.String r6 = r6.method     // Catch:{ InterruptedException -> 0x0162 }
                    r3[r0] = r6     // Catch:{ InterruptedException -> 0x0162 }
                    java.lang.String r0 = java.lang.String.format(r5, r3)     // Catch:{ InterruptedException -> 0x0162 }
                    r1.showToast(r4, r0)     // Catch:{ InterruptedException -> 0x0162 }
                    r0 = 1000(0x3e8, double:4.94E-321)
                    java.lang.Thread.sleep(r0)     // Catch:{ InterruptedException -> 0x0162 }
                    org.firstinspires.ftc.robotcore.internal.system.AppUtil r0 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getInstance()     // Catch:{ InterruptedException -> 0x0162 }
                    r1 = -1
                    r0.restartApp(r1)     // Catch:{ InterruptedException -> 0x0162 }
                    goto L_0x0174
                L_0x0162:
                    r0 = r2
                    goto L_0x0166
                L_0x0164:
                    r0 = move-exception
                    goto L_0x016c
                L_0x0166:
                    if (r0 == 0) goto L_0x0174
                    com.qualcomm.robotcore.util.RobotLog.clearGlobalErrorMsg()     // Catch:{ all -> 0x0164 }
                    goto L_0x0174
                L_0x016c:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r1 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this
                    java.util.concurrent.CountDownLatch r1 = r1.acquired
                    r1.countDown()
                    throw r0
                L_0x0174:
                    com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl$OpModeStuckCodeMonitor r0 = com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.this
                    java.util.concurrent.CountDownLatch r0 = r0.acquired
                    r0.countDown()
                    return
                */
                throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl.OpModeStuckCodeMonitor.Runner.run():void");
            }
        }
    }

    /* access modifiers changed from: protected */
    public void callActiveOpModeInit() {
        synchronized (this.listeners) {
            Iterator<OpModeManagerNotifier.Notifications> it = this.listeners.iterator();
            while (it.hasNext()) {
                it.next().onOpModePreInit(this.activeOpMode);
            }
        }
        for (HardwareDevice next : this.hardwareMap.unsafeIterable()) {
            if (next instanceof OpModeManagerNotifier.Notifications) {
                ((OpModeManagerNotifier.Notifications) next).onOpModePreInit(this.activeOpMode);
            }
        }
        this.activeOpMode.internalPreInit();
        try {
            detectStuck(this.activeOpMode.msStuckDetectInit + 2000, "init()", new Runnable() {
                public void run() {
                    OpModeManagerImpl.this.activeOpMode.init();
                }
            }, true);
        } catch (ForceStopException unused) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
        } catch (Exception e) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
            handleUserCodeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void callActiveOpModeStart() {
        synchronized (this.listeners) {
            Iterator<OpModeManagerNotifier.Notifications> it = this.listeners.iterator();
            while (it.hasNext()) {
                it.next().onOpModePreStart(this.activeOpMode);
            }
        }
        for (HardwareDevice next : this.hardwareMap.unsafeIterable()) {
            if (next instanceof OpModeManagerNotifier.Notifications) {
                ((OpModeManagerNotifier.Notifications) next).onOpModePreStart(this.activeOpMode);
            }
        }
        try {
            detectStuck(this.activeOpMode.msStuckDetectStart, "start()", new Runnable() {
                public void run() {
                    OpModeManagerImpl.this.activeOpMode.start();
                }
            });
        } catch (ForceStopException unused) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
        } catch (Exception e) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
            handleUserCodeException(e);
        }
    }

    /* access modifiers changed from: protected */
    public void callActiveOpModeInitLoop() {
        try {
            detectStuck(this.activeOpMode.msStuckDetectInitLoop, "init_loop()", new Runnable() {
                public void run() {
                    OpModeManagerImpl.this.activeOpMode.init_loop();
                }
            });
        } catch (ForceStopException unused) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
        } catch (Exception e) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
            handleUserCodeException(e);
        }
        this.activeOpMode.internalPostInitLoop();
    }

    /* access modifiers changed from: protected */
    public void callActiveOpModeLoop() {
        try {
            detectStuck(this.activeOpMode.msStuckDetectLoop, "loop()", new Runnable() {
                public void run() {
                    OpModeManagerImpl.this.activeOpMode.loop();
                }
            });
        } catch (ForceStopException unused) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
        } catch (Exception e) {
            initActiveOpMode("$Stop$Robot$");
            this.skipCallToStop = true;
            handleUserCodeException(e);
        }
        this.activeOpMode.internalPostLoop();
    }

    /* access modifiers changed from: protected */
    public void handleUserCodeException(Exception exc) {
        RobotLog.m50ee(TAG, (Throwable) exc, "User code threw an uncaught exception");
        handleSendStacktrace(exc);
    }

    /* access modifiers changed from: protected */
    public void handleSendStacktrace(Exception exc) {
        String[] split = Log.getStackTraceString(exc).split("\n");
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < Math.min(split.length, 15); i++) {
            sb.append(split[i]);
            sb.append("\n");
        }
        NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_SHOW_STACKTRACE, sb.toString()));
    }

    public static void updateTelemetryNow(OpMode opMode, TelemetryMessage telemetryMessage) {
        opMode.internalUpdateTelemetryNow(telemetryMessage);
    }

    public void refreshUserTelemetry(TelemetryMessage telemetryMessage, double d) {
        this.eventLoopManager.getEventLoop().refreshUserTelemetry(telemetryMessage, d);
    }

    public void requestOpModeStop(OpMode opMode) {
        this.eventLoopManager.getEventLoop().requestOpModeStop(opMode);
    }

    public static class DefaultOpMode extends OpMode {
        private static final long SAFE_WAIT_NANOS = 100000000;
        private ElapsedTime blinkerTimer;
        private boolean firstTimeRun;
        private long nanoNextSafe;

        public void stop() {
        }

        public DefaultOpMode() {
            this.firstTimeRun = true;
            this.blinkerTimer = new ElapsedTime();
            this.firstTimeRun = true;
        }

        public void init() {
            startSafe();
            this.telemetry.addData("Status", (Object) "Robot is stopping");
        }

        public void init_loop() {
            staySafe();
            this.telemetry.addData("Status", (Object) "Robot is stopped");
        }

        public void loop() {
            staySafe();
            this.telemetry.addData("Status", (Object) "Robot is stopped");
        }

        private boolean isLynxDevice(HardwareDevice hardwareDevice) {
            return hardwareDevice.getManufacturer() == HardwareDevice.Manufacturer.Lynx;
        }

        private boolean isLynxDevice(Object obj) {
            return isLynxDevice((HardwareDevice) obj);
        }

        private void startSafe() {
            for (DcMotorSimple next : this.hardwareMap.getAll(DcMotorSimple.class)) {
                if (next.getPower() != LynxServoController.apiPositionFirst) {
                    next.setPower(LynxServoController.apiPositionFirst);
                }
            }
            if (this.firstTimeRun) {
                this.firstTimeRun = false;
                this.nanoNextSafe = System.nanoTime();
                this.blinkerTimer.reset();
                return;
            }
            this.nanoNextSafe = System.nanoTime() + SAFE_WAIT_NANOS;
        }

        private void staySafe() {
            if (System.nanoTime() > this.nanoNextSafe) {
                for (RobotCoreLynxUsbDevice failSafe : this.hardwareMap.getAll(RobotCoreLynxUsbDevice.class)) {
                    failSafe.failSafe();
                }
                for (ServoController next : this.hardwareMap.getAll(ServoController.class)) {
                    if (!isLynxDevice((HardwareDevice) next)) {
                        next.pwmDisable();
                    }
                }
                for (DcMotor next2 : this.hardwareMap.getAll(DcMotor.class)) {
                    if (!isLynxDevice((HardwareDevice) next2)) {
                        next2.setPower(LynxServoController.apiPositionFirst);
                        next2.setMode(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
                    }
                }
                for (LightSensor enableLed : this.hardwareMap.getAll(LightSensor.class)) {
                    enableLed.enableLed(false);
                }
                this.nanoNextSafe = System.nanoTime() + SAFE_WAIT_NANOS;
            }
        }
    }
}
