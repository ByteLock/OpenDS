package com.qualcomm.robotcore.eventloop;

import android.content.Context;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.eventloop.opmode.EventLoopManagerClient;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.RobotProtocolException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.PeerDiscovery;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.ClockWarningSource;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SoftwareVersionWarningSource;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.WebServer;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import java.util.Set;
import java.util.concurrent.CancellationException;
import java.util.concurrent.CopyOnWriteArraySet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class EventLoopManager implements RecvLoopRunnable.RecvLoopCallback, NetworkConnection.NetworkConnectionCallback, PeerStatusCallback, SyncdDevice.Manager {
    private static final boolean DEBUG = false;
    private static final int HEARTBEAT_WAIT_DELAY = 500;
    private static final int MAX_COMMAND_CACHE = 8;
    public static final String RC_BATTERY_STATUS_KEY = "$RobotController$Battery$Status$";
    public static final String ROBOT_BATTERY_LEVEL_KEY = "$Robot$Battery$Level$";
    private static final double SECONDS_UNTIL_FORCED_SHUTDOWN = 2.0d;
    public static final String SYSTEM_ERROR_KEY = "$System$Error$";
    public static final String SYSTEM_NONE_KEY = "$System$None$";
    public static final String SYSTEM_WARNING_KEY = "$System$Warning$";
    public static final String TAG = "EventLoopManager";
    private final PeerDiscovery anotherDsConnectedPeerDiscoveryResponse = PeerDiscovery.forTransmission(PeerDiscovery.PeerType.NOT_CONNECTED_DUE_TO_PREEXISTING_CONNECTION);
    private final AppUtil appUtil = AppUtil.getInstance();
    private EventLoopMonitor callback = null;
    private final Command[] commandRecvCache = new Command[8];
    private int commandRecvCachePosition = 0;
    private final Context context;
    private volatile boolean displayingRobocolMismatchError = false;
    /* access modifiers changed from: private */
    public EventLoop eventLoop = null;
    /* access modifiers changed from: private */
    public final Object eventLoopLock = new Object();
    /* access modifiers changed from: private */
    public final EventLoopManagerClient eventLoopManagerClient;
    private ExecutorService executorEventLoop = ThreadPool.newSingleThreadExecutor("executorEventLoop");
    private final Gamepad[] gamepads = {new Gamepad(), new Gamepad()};
    private Heartbeat heartbeat = new Heartbeat();
    /* access modifiers changed from: private */
    public final EventLoop idleEventLoop;
    /* access modifiers changed from: private */
    public ElapsedTime lastHeartbeatReceived = new ElapsedTime();
    private String lastSystemTelemetryKey = null;
    private String lastSystemTelemetryMessage = null;
    private long lastSystemTelemetryNanoTime = 0;
    private final NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    private final PeerDiscovery normalPeerDiscoveryResponse = PeerDiscovery.forTransmission(PeerDiscovery.PeerType.PEER);
    private boolean receivedTimeFromCurrentPeer = false;
    private final Object refreshSystemTelemetryLock = new Object();
    public RobotState state = RobotState.NOT_STARTED;
    /* access modifiers changed from: private */
    public final Set<SyncdDevice> syncdDevices = new CopyOnWriteArraySet();

    public interface EventLoopMonitor {
        void onPeerConnected();

        void onPeerDisconnected();

        void onStateChange(RobotState robotState);

        void onTelemetryTransmitted();
    }

    public EventLoopManager(Context context2, EventLoopManagerClient eventLoopManagerClient2, EventLoop eventLoop2) {
        this.context = context2;
        this.eventLoopManagerClient = eventLoopManagerClient2;
        this.idleEventLoop = eventLoop2;
        this.eventLoop = eventLoop2;
        changeState(RobotState.NOT_STARTED);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
    }

    public WebServer getWebServer() {
        return this.eventLoopManagerClient.getWebServer();
    }

    public void setMonitor(EventLoopMonitor eventLoopMonitor) {
        this.callback = eventLoopMonitor;
        if (NetworkConnectionHandler.getInstance().isPeerConnected()) {
            this.callback.onPeerConnected();
        } else {
            this.callback.onPeerDisconnected();
        }
    }

    public EventLoopMonitor getMonitor() {
        return this.callback;
    }

    public EventLoop getEventLoop() {
        return this.eventLoop;
    }

    public Gamepad getGamepad(int i) {
        Range.throwIfRangeIsInvalid(i, 0, 1);
        return this.gamepads[i];
    }

    public Gamepad[] getGamepads() {
        return this.gamepads;
    }

    public Heartbeat getHeartbeat() {
        return this.heartbeat;
    }

    public CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult reportGlobalError(String str, boolean z) {
        RobotLog.setGlobalErrorMsg(str);
        return CallbackResult.HANDLED;
    }

    public CallbackResult packetReceived(RobocolDatagram robocolDatagram) {
        refreshSystemTelemetry();
        return CallbackResult.NOT_HANDLED;
    }

    private class EventLoopRunnable implements Runnable {
        private EventLoopRunnable() {
        }

        public void run() {
            ThreadPool.logThreadLifeCycle("opmode loop()", new Runnable() {
                public void run() {
                    String str;
                    String str2;
                    try {
                        ElapsedTime elapsedTime = new ElapsedTime();
                        while (!Thread.currentThread().isInterrupted()) {
                            while (elapsedTime.time() < 0.001d) {
                                Thread.sleep(5);
                            }
                            elapsedTime.reset();
                            EventLoopManager.this.refreshSystemTelemetry();
                            if (EventLoopManager.this.lastHeartbeatReceived.startTime() == LynxServoController.apiPositionFirst) {
                                Thread.sleep(500);
                            }
                            for (SyncdDevice syncdDevice : EventLoopManager.this.syncdDevices) {
                                SyncdDevice.ShutdownReason shutdownReason = syncdDevice.getShutdownReason();
                                if (shutdownReason != SyncdDevice.ShutdownReason.NORMAL) {
                                    RobotLog.m59v("event loop: device has shutdown abnormally: %s", shutdownReason);
                                    RobotUsbModule owner = syncdDevice.getOwner();
                                    if (owner != null) {
                                        RobotLog.m61vv(EventLoopManager.TAG, "event loop: detaching device %s", owner.getSerialNumber());
                                        synchronized (EventLoopManager.this.eventLoopLock) {
                                            EventLoopManager.this.eventLoop.handleUsbModuleDetach(owner);
                                            if (shutdownReason == SyncdDevice.ShutdownReason.ABNORMAL_ATTEMPT_REOPEN) {
                                                RobotLog.m61vv(EventLoopManager.TAG, "event loop: auto-reattaching device %s", owner.getSerialNumber());
                                                EventLoopManager.this.eventLoop.pendUsbDeviceAttachment(owner.getSerialNumber(), 250, TimeUnit.MILLISECONDS);
                                            }
                                        }
                                    } else {
                                        continue;
                                    }
                                }
                            }
                            synchronized (EventLoopManager.this.eventLoopLock) {
                                EventLoopManager.this.eventLoop.processedRecentlyAttachedUsbDevices();
                            }
                            try {
                                synchronized (EventLoopManager.this.eventLoopLock) {
                                    EventLoopManager.this.eventLoop.loop();
                                }
                            } catch (Exception e) {
                                RobotLog.m50ee(EventLoopManager.TAG, (Throwable) e, "Event loop threw an exception");
                                StringBuilder sb = new StringBuilder();
                                sb.append(e.getClass().getSimpleName());
                                if (e.getMessage() != null) {
                                    str = " - " + e.getMessage();
                                } else {
                                    str = InspectionState.NO_VERSION;
                                }
                                sb.append(str);
                                String sb2 = sb.toString();
                                RobotLog.setGlobalErrorMsg("System exception: " + sb2);
                                throw new RobotCoreException("EventLoop Exception in loop(): %s", sb2);
                            } catch (NoClassDefFoundError e2) {
                                OnBotJavaHelper onBotJavaHelper = EventLoopManager.this.eventLoopManagerClient.getOnBotJavaHelper();
                                if (onBotJavaHelper == null || !onBotJavaHelper.isExternalLibrariesError(e2)) {
                                    throw e2;
                                }
                                RobotLog.m50ee(EventLoopManager.TAG, (Throwable) e2, "Event loop threw an exception");
                                StringBuilder sb3 = new StringBuilder();
                                sb3.append(e2.getClass().getSimpleName());
                                if (e2.getMessage() != null) {
                                    str2 = " - " + e2.getMessage();
                                } else {
                                    str2 = InspectionState.NO_VERSION;
                                }
                                sb3.append(str2);
                                String sb4 = sb3.toString();
                                RobotLog.setGlobalErrorMsg("System exception: " + sb4);
                                throw new RobotCoreException("EventLoop Exception in loop(): %s", sb4);
                            }
                        }
                    } catch (InterruptedException e3) {
                        RobotLog.m62vv(EventLoopManager.TAG, (Throwable) e3, "EventLoopRunnable interrupted");
                        Thread.currentThread().interrupt();
                        EventLoopManager.this.changeState(RobotState.STOPPED);
                    } catch (CancellationException e4) {
                        RobotLog.m62vv(EventLoopManager.TAG, (Throwable) e4, "EventLoopRunnable cancelled");
                        EventLoopManager.this.changeState(RobotState.STOPPED);
                    } catch (RobotCoreException e5) {
                        RobotLog.m62vv(EventLoopManager.TAG, (Throwable) e5, "RobotCoreException in EventLoopManager");
                        EventLoopManager.this.changeState(RobotState.EMERGENCY_STOP);
                        EventLoopManager.this.refreshSystemTelemetry();
                    }
                    try {
                        synchronized (EventLoopManager.this.eventLoopLock) {
                            EventLoopManager.this.eventLoop.teardown();
                        }
                    } catch (Exception e6) {
                        RobotLog.m68ww(EventLoopManager.TAG, (Throwable) e6, "Caught exception during looper teardown: " + e6.toString());
                        EventLoopManager.this.refreshSystemTelemetry();
                    }
                }
            });
        }
    }

    public void refreshSystemTelemetryNow() {
        this.lastSystemTelemetryNanoTime = 0;
        refreshSystemTelemetry();
    }

    /* JADX WARNING: Removed duplicated region for block: B:18:0x0047  */
    /* JADX WARNING: Removed duplicated region for block: B:24:0x0055  */
    /* JADX WARNING: Removed duplicated region for block: B:26:0x0063  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void refreshSystemTelemetry() {
        /*
            r13 = this;
            java.lang.Object r0 = r13.refreshSystemTelemetryLock
            monitor-enter(r0)
            long r1 = java.lang.System.nanoTime()     // Catch:{ all -> 0x0075 }
            java.lang.String r3 = com.qualcomm.robotcore.util.RobotLog.getGlobalErrorMsg()     // Catch:{ all -> 0x0075 }
            com.qualcomm.robotcore.util.RobotLog$GlobalWarningMessage r4 = com.qualcomm.robotcore.util.RobotLog.getGlobalWarningMessage()     // Catch:{ all -> 0x0075 }
            java.lang.String r4 = r4.message     // Catch:{ all -> 0x0075 }
            boolean r5 = r3.isEmpty()     // Catch:{ all -> 0x0075 }
            if (r5 != 0) goto L_0x001a
            java.lang.String r4 = "$System$Error$"
            goto L_0x002a
        L_0x001a:
            boolean r3 = r4.isEmpty()     // Catch:{ all -> 0x0075 }
            if (r3 != 0) goto L_0x0026
            java.lang.String r3 = "$System$Warning$"
            r12 = r4
            r4 = r3
            r3 = r12
            goto L_0x002a
        L_0x0026:
            java.lang.String r3 = ""
            java.lang.String r4 = "$System$None$"
        L_0x002a:
            r5 = 5000000000(0x12a05f200, double:2.470328229E-314)
            java.lang.String r7 = r13.lastSystemTelemetryMessage     // Catch:{ all -> 0x0075 }
            boolean r7 = r3.equals(r7)     // Catch:{ all -> 0x0075 }
            r8 = 0
            r9 = 1
            if (r7 == 0) goto L_0x0044
            java.lang.String r7 = r13.lastSystemTelemetryKey     // Catch:{ all -> 0x0075 }
            boolean r7 = r4.equals(r7)     // Catch:{ all -> 0x0075 }
            if (r7 != 0) goto L_0x0042
            goto L_0x0044
        L_0x0042:
            r7 = r8
            goto L_0x0045
        L_0x0044:
            r7 = r9
        L_0x0045:
            if (r7 != 0) goto L_0x0052
            long r10 = r13.lastSystemTelemetryNanoTime     // Catch:{ all -> 0x0075 }
            long r10 = r1 - r10
            int r5 = (r10 > r5 ? 1 : (r10 == r5 ? 0 : -1))
            if (r5 <= 0) goto L_0x0050
            goto L_0x0052
        L_0x0050:
            r5 = r8
            goto L_0x0053
        L_0x0052:
            r5 = r9
        L_0x0053:
            if (r7 == 0) goto L_0x0061
            java.lang.String r6 = "system telemetry: key=%s msg=\"%s\""
            r7 = 2
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ all -> 0x0075 }
            r7[r8] = r4     // Catch:{ all -> 0x0075 }
            r7[r9] = r3     // Catch:{ all -> 0x0075 }
            com.qualcomm.robotcore.util.RobotLog.m41d(r6, r7)     // Catch:{ all -> 0x0075 }
        L_0x0061:
            if (r5 == 0) goto L_0x0073
            r13.lastSystemTelemetryMessage = r3     // Catch:{ all -> 0x0075 }
            r13.lastSystemTelemetryKey = r4     // Catch:{ all -> 0x0075 }
            r13.lastSystemTelemetryNanoTime = r1     // Catch:{ all -> 0x0075 }
            r13.buildAndSendTelemetry(r4, r3)     // Catch:{ all -> 0x0075 }
            com.qualcomm.robotcore.eventloop.EventLoopManager$EventLoopMonitor r1 = r13.callback     // Catch:{ all -> 0x0075 }
            if (r1 == 0) goto L_0x0073
            r1.onTelemetryTransmitted()     // Catch:{ all -> 0x0075 }
        L_0x0073:
            monitor-exit(r0)     // Catch:{ all -> 0x0075 }
            return
        L_0x0075:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0075 }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.eventloop.EventLoopManager.refreshSystemTelemetry():void");
    }

    public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        RobotLog.m54ii(TAG, "onNetworkConnectionEvent: " + networkEvent.toString());
        int i = C07102.f107x94151df2[networkEvent.ordinal()];
        if (i == 1) {
            return this.networkConnectionHandler.handlePeersAvailable();
        }
        if (i != 2) {
            return callbackResult;
        }
        RobotLog.m54ii("Robocol", "Received network connection event");
        return this.networkConnectionHandler.handleConnectionInfoAvailable();
    }

    /* renamed from: com.qualcomm.robotcore.eventloop.EventLoopManager$2 */
    static /* synthetic */ class C07102 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent */
        static final /* synthetic */ int[] f107x94151df2;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent[] r0 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f107x94151df2 = r0
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.PEERS_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f107x94151df2     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.eventloop.EventLoopManager.C07102.<clinit>():void");
        }
    }

    public void start(EventLoop eventLoop2) throws RobotCoreException {
        RobotLog.m60vv("Robocol", "EventLoopManager.start()");
        this.networkConnectionHandler.pushNetworkConnectionCallback(this);
        this.networkConnectionHandler.pushReceiveLoopCallback(this);
        this.networkConnectionHandler.init(NetworkConnectionHandler.getNetworkType(this.context), this.context);
        if (this.networkConnectionHandler.isNetworkConnected()) {
            RobotLog.m60vv("Robocol", "Spoofing a Network Connection event...");
            onNetworkConnectionEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
        } else {
            RobotLog.m60vv("Robocol", "Network not yet available, deferring network connection event...");
        }
        setEventLoop(eventLoop2);
    }

    public void shutdown() {
        RobotLog.m60vv("Robocol", "EventLoopManager.shutdown()");
        stopEventLoop();
    }

    public void close() {
        RobotLog.m60vv("Robocol", "EventLoopManager.close()");
        this.networkConnectionHandler.shutdown();
        this.networkConnectionHandler.removeNetworkConnectionCallback(this);
        this.networkConnectionHandler.removeReceiveLoopCallback(this);
    }

    public void registerSyncdDevice(SyncdDevice syncdDevice) {
        this.syncdDevices.add(syncdDevice);
    }

    public void unregisterSyncdDevice(SyncdDevice syncdDevice) {
        this.syncdDevices.remove(syncdDevice);
    }

    public void setEventLoop(EventLoop eventLoop2) throws RobotCoreException {
        stopEventLoop();
        synchronized (this.eventLoopLock) {
            this.eventLoop = eventLoop2;
            RobotLog.m61vv("Robocol", "eventLoop=%s", eventLoop2.getClass().getSimpleName());
        }
        startEventLoop();
    }

    public void sendTelemetryData(TelemetryMessage telemetryMessage) {
        try {
            telemetryMessage.setRobotState(this.state);
            this.networkConnectionHandler.sendDataToPeer(telemetryMessage);
        } catch (RobotCoreException e) {
            RobotLog.m68ww(TAG, (Throwable) e, "Failed to send telemetry data");
        }
        telemetryMessage.clearData();
    }

    private void startEventLoop() throws RobotCoreException {
        try {
            changeState(RobotState.INIT);
            synchronized (this.eventLoopLock) {
                this.eventLoop.init(this);
            }
            this.lastHeartbeatReceived = new ElapsedTime(0);
            changeState(RobotState.RUNNING);
            ExecutorService newSingleThreadExecutor = ThreadPool.newSingleThreadExecutor("executorEventLoop");
            this.executorEventLoop = newSingleThreadExecutor;
            newSingleThreadExecutor.execute(new Runnable() {
                public void run() {
                    boolean z;
                    new EventLoopRunnable().run();
                    EventLoopManager eventLoopManager = EventLoopManager.this;
                    EventLoop unused = eventLoopManager.eventLoop = eventLoopManager.idleEventLoop;
                    if (!Thread.currentThread().isInterrupted()) {
                        RobotLog.m60vv(EventLoopManager.TAG, "switching to idleEventLoop");
                        try {
                            synchronized (EventLoopManager.this.eventLoopLock) {
                                EventLoopManager.this.eventLoop.init(EventLoopManager.this);
                            }
                        } catch (InterruptedException unused2) {
                            Thread.currentThread().interrupt();
                        } catch (RobotCoreException e) {
                            RobotLog.m50ee(EventLoopManager.TAG, (Throwable) e, "internal error");
                            z = false;
                        }
                        z = true;
                        if (z) {
                            new EventLoopRunnable().run();
                        }
                    }
                }
            });
        } catch (Exception e) {
            RobotLog.m68ww(TAG, (Throwable) e, "Caught exception during looper init: " + e.toString());
            changeState(RobotState.EMERGENCY_STOP);
            refreshSystemTelemetry();
            throw new RobotCoreException("Robot failed to start: " + e.getMessage());
        }
    }

    private void stopEventLoop() {
        if (this.eventLoop.getOpModeManager() != null) {
            this.eventLoop.getOpModeManager().stopActiveOpMode();
        }
        this.executorEventLoop.shutdownNow();
        ThreadPool.awaitTerminationOrExitApplication(this.executorEventLoop, 10, TimeUnit.SECONDS, "EventLoop", "possible infinite loop in user code?");
        changeState(RobotState.STOPPED);
        this.eventLoop = this.idleEventLoop;
        this.syncdDevices.clear();
    }

    /* access modifiers changed from: private */
    public void changeState(RobotState robotState) {
        this.state = robotState;
        RobotLog.m58v("EventLoopManager state is " + robotState.toString());
        EventLoopMonitor eventLoopMonitor = this.callback;
        if (eventLoopMonitor != null) {
            eventLoopMonitor.onStateChange(robotState);
        }
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_NOTIFY_ROBOT_STATE, Integer.toString(robotState.asByte())));
    }

    public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
        Gamepad gamepad = new Gamepad();
        gamepad.fromByteArray(robocolDatagram.getData());
        if (gamepad.getUser() == null) {
            RobotLog.m49ee(TAG, "gamepad with user %d received; only users 1 and 2 are valid", Byte.valueOf(gamepad.getUser().f280id));
            return CallbackResult.HANDLED;
        }
        this.gamepads[gamepad.getUser().f280id - 1].copy(gamepad);
        if (gamepad.getGamepadId() == -2) {
            RobotLog.m61vv(TAG, "synthetic gamepad received: id=%d user=%s atRest=%s ", Integer.valueOf(gamepad.getGamepadId()), gamepad.getUser(), Boolean.valueOf(gamepad.atRest()));
            gamepad.setGamepadId(-1);
        }
        return CallbackResult.HANDLED;
    }

    public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
        Heartbeat heartbeat2 = new Heartbeat();
        heartbeat2.fromByteArray(robocolDatagram.getData());
        heartbeat2.setRobotState(this.state);
        ClockWarningSource.getInstance().onDsHeartbeatReceived(heartbeat2);
        if (!this.receivedTimeFromCurrentPeer) {
            long j = heartbeat2.f135t0;
            if (this.appUtil.isSaneWallClockTime(j)) {
                this.receivedTimeFromCurrentPeer = true;
                RobotLog.m60vv(TAG, "Setting authoritative wall clock based on connected DS.");
                this.appUtil.setWallClockTime(j);
                this.appUtil.setTimeZone(heartbeat2.getTimeZoneId());
            }
        }
        heartbeat2.f136t1 = robocolDatagram.getWallClockTimeMsReceived();
        heartbeat2.f137t2 = this.appUtil.getWallClockTime();
        this.networkConnectionHandler.sendDataToPeer(heartbeat2);
        this.lastHeartbeatReceived.reset();
        this.heartbeat = heartbeat2;
        return CallbackResult.HANDLED;
    }

    public void onPeerConnected() {
        EventLoopMonitor eventLoopMonitor = this.callback;
        if (eventLoopMonitor != null) {
            eventLoopMonitor.onPeerConnected();
        }
    }

    public void onPeerDisconnected() {
        EventLoopMonitor eventLoopMonitor = this.callback;
        if (eventLoopMonitor != null) {
            eventLoopMonitor.onPeerDisconnected();
        }
        OpModeManagerImpl opModeManager = this.eventLoop.getOpModeManager();
        if (opModeManager != null) {
            opModeManager.initActiveOpMode("$Stop$Robot$");
            RobotLog.m54ii(TAG, "Lost connection while running op mode: " + opModeManager.getActiveOpModeName());
        } else {
            RobotLog.m54ii(TAG, "Lost connection while main event loop not active");
        }
        this.lastHeartbeatReceived = new ElapsedTime(0);
        this.receivedTimeFromCurrentPeer = false;
    }

    public CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
        PeerDiscovery peerDiscovery;
        if (!this.networkConnectionHandler.isPeerConnected() || robocolDatagram.getAddress().equals(this.networkConnectionHandler.getCurrentPeerAddr())) {
            peerDiscovery = this.normalPeerDiscoveryResponse;
            try {
                SoftwareVersionWarningSource.getInstance().onReceivedPeerDiscoveryFromCurrentPeer(this.networkConnectionHandler.updateConnection(robocolDatagram));
                if (this.displayingRobocolMismatchError) {
                    RobotLog.clearGlobalErrorMsg();
                    this.displayingRobocolMismatchError = false;
                }
            } catch (RobotProtocolException e) {
                if (RobotLog.setGlobalErrorMsg(e.getMessage())) {
                    this.displayingRobocolMismatchError = true;
                }
                RobotLog.m48ee(TAG, e.getMessage());
            }
        } else {
            peerDiscovery = this.anotherDsConnectedPeerDiscoveryResponse;
        }
        this.networkConnectionHandler.sendDatagram(new RobocolDatagram(peerDiscovery, robocolDatagram.getAddress()));
        return CallbackResult.HANDLED;
    }

    public CallbackResult commandEvent(Command command) throws RobotCoreException {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        for (Command command2 : this.commandRecvCache) {
            if (command2 != null && command2.equals(command)) {
                return CallbackResult.HANDLED;
            }
        }
        Command[] commandArr = this.commandRecvCache;
        int i = this.commandRecvCachePosition;
        this.commandRecvCachePosition = i + 1;
        commandArr[i % commandArr.length] = command;
        try {
            synchronized (this.eventLoopLock) {
                callbackResult = this.eventLoop.processCommand(command);
            }
        } catch (Exception e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Event loop threw an exception while processing a command");
        }
        return callbackResult;
    }

    public CallbackResult emptyEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public void buildAndSendTelemetry(String str, String str2) {
        TelemetryMessage telemetryMessage = new TelemetryMessage();
        telemetryMessage.setTag(str);
        telemetryMessage.addData(str, str2);
        sendTelemetryData(telemetryMessage);
    }
}
