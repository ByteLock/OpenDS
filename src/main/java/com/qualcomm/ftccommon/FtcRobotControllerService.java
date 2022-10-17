package com.qualcomm.ftccommon;

import android.app.AlertDialog;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Binder;
import android.os.IBinder;
import android.view.ContextThemeWrapper;
import androidx.core.view.ViewCompat;
import com.qualcomm.ftccommon.UpdateUI;
import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.eventloop.opmode.EventLoopManagerClient;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.factory.RobotFactory;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.hardware.LightBlinker;
import com.qualcomm.robotcore.hardware.LightMultiplexor;
import com.qualcomm.robotcore.hardware.SwitchableLight;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robot.Robot;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.robot.RobotStatus;
import com.qualcomm.robotcore.util.ClockWarningSource;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SoftwareVersionWarningSource;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.WebServer;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import java.util.ArrayList;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.hardware.android.DragonboardIndicatorLED;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.network.PeerStatus;
import org.firstinspires.ftc.robotcore.internal.network.PreferenceRemoterRC;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;
import org.firstinspires.ftc.robotcore.internal.system.AppAliveNotifier;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.ftc.robotserver.internal.webserver.CoreRobotWebServer;

public class FtcRobotControllerService extends Service implements NetworkConnection.NetworkConnectionCallback, WifiDirectAgent.Callback, EventLoopManagerClient {
    private static final int NETWORK_WAIT = 1000;
    public static final String TAG = "FTCService";
    private static final int USB_WAIT = 5000;
    /* access modifiers changed from: private */
    public AlertDialog alertDialogConnectedAsPeer;
    /* access modifiers changed from: private */
    public AlertDialog alertDialogWifiDirectNameNonPrintableChars;
    private final IBinder binder = new FtcRobotControllerBinder();
    /* access modifiers changed from: private */
    public SwitchableLight bootIndicator = null;
    private Future bootIndicatorOff = null;
    /* access modifiers changed from: private */
    public UpdateUI.Callback callback = null;
    /* access modifiers changed from: private */
    public EventLoop eventLoop;
    /* access modifiers changed from: private */
    public EventLoopManager eventLoopManager;
    /* access modifiers changed from: private */
    public final EventLoopMonitor eventLoopMonitor = new EventLoopMonitor();
    /* access modifiers changed from: private */
    public EventLoop idleEventLoop;
    private LightBlinker livenessIndicatorBlinker = null;
    /* access modifiers changed from: private */
    public NetworkConnection networkConnection;
    private NetworkConnection.NetworkEvent networkConnectionStatus = NetworkConnection.NetworkEvent.UNKNOWN;
    private OnBotJavaHelper onBotJavaHelper;
    private final PreferencesHelper preferencesHelper = new PreferencesHelper(TAG);
    /* access modifiers changed from: private */
    public Robot robot;
    private Future robotSetupFuture = null;
    /* access modifiers changed from: private */
    public volatile boolean robotSetupHasBeenStarted = false;
    private RobotStatus robotStatus = RobotStatus.NONE;
    /* access modifiers changed from: private */
    public WebServer webServer;
    /* access modifiers changed from: private */
    public WifiDirectAgent wifiDirectAgent = WifiDirectAgent.getInstance();
    /* access modifiers changed from: private */
    public final Object wifiDirectCallbackLock = new Object();

    public class FtcRobotControllerBinder extends Binder {
        public FtcRobotControllerBinder() {
        }

        public FtcRobotControllerService getService() {
            return FtcRobotControllerService.this;
        }
    }

    private class EventLoopMonitor implements EventLoopManager.EventLoopMonitor {
        private EventLoopMonitor() {
        }

        public void onStateChange(RobotState robotState) {
            if (FtcRobotControllerService.this.callback != null) {
                FtcRobotControllerService.this.callback.updateRobotState(robotState);
                if (robotState == RobotState.RUNNING) {
                    FtcRobotControllerService.this.updateRobotStatus(RobotStatus.NONE);
                }
            }
        }

        public void onPeerConnected() {
            updatePeerStatus(PeerStatus.CONNECTED);
            RobotLog.m42dd(FtcRobotControllerService.TAG, "Sending user device types and preferences to driver station");
            ConfigurationTypeManager.getInstance().sendUserDeviceTypes();
            PreferenceRemoterRC.getInstance().sendAllPreferences();
        }

        public void onPeerDisconnected() {
            updatePeerStatus(PeerStatus.DISCONNECTED);
        }

        private void updatePeerStatus(PeerStatus peerStatus) {
            if (FtcRobotControllerService.this.callback != null) {
                FtcRobotControllerService.this.callback.updatePeerStatus(peerStatus);
            }
        }

        public void onTelemetryTransmitted() {
            if (FtcRobotControllerService.this.callback != null) {
                FtcRobotControllerService.this.callback.refreshErrorTextOnUiThread();
            }
        }
    }

    private class RobotSetupRunnable implements Runnable {
        Runnable runOnComplete;

        RobotSetupRunnable(Runnable runnable) {
            this.runOnComplete = runnable;
        }

        /* access modifiers changed from: package-private */
        public void shutdownRobot() {
            if (FtcRobotControllerService.this.robot != null) {
                FtcRobotControllerService.this.robot.shutdown();
                Robot unused = FtcRobotControllerService.this.robot = null;
            }
        }

        /* access modifiers changed from: package-private */
        public void awaitUSB() throws InterruptedException {
            FtcRobotControllerService.this.updateRobotStatus(RobotStatus.SCANNING_USB);
            AppAliveNotifier.getInstance().notifyAppAlive();
            Thread.sleep(5000);
            AppAliveNotifier.getInstance().notifyAppAlive();
        }

        /* access modifiers changed from: package-private */
        public void initializeEventLoopAndRobot() throws RobotCoreException {
            if (FtcRobotControllerService.this.eventLoopManager == null) {
                FtcRobotControllerService ftcRobotControllerService = FtcRobotControllerService.this;
                FtcRobotControllerService ftcRobotControllerService2 = FtcRobotControllerService.this;
                EventLoopManager unused = ftcRobotControllerService.eventLoopManager = new EventLoopManager(ftcRobotControllerService2, ftcRobotControllerService2, ftcRobotControllerService2.idleEventLoop);
            }
            FtcRobotControllerService ftcRobotControllerService3 = FtcRobotControllerService.this;
            Robot unused2 = ftcRobotControllerService3.robot = RobotFactory.createRobot(ftcRobotControllerService3.eventLoopManager);
        }

        /* access modifiers changed from: package-private */
        public boolean waitForWifi() throws InterruptedException {
            FtcRobotControllerService.this.updateRobotStatus(RobotStatus.WAITING_ON_WIFI);
            boolean z = false;
            while (true) {
                synchronized (FtcRobotControllerService.this.wifiDirectCallbackLock) {
                    if (FtcRobotControllerService.this.wifiDirectAgent.isWifiEnabled()) {
                        return z;
                    }
                    z = true;
                    FtcRobotControllerService.this.waitForNextWifiDirectCallback();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean waitForWifiDirect() throws InterruptedException {
            FtcRobotControllerService.this.updateRobotStatus(RobotStatus.WAITING_ON_WIFI_DIRECT);
            boolean z = false;
            while (true) {
                synchronized (FtcRobotControllerService.this.wifiDirectCallbackLock) {
                    if (FtcRobotControllerService.this.wifiDirectAgent.isWifiDirectEnabled()) {
                        return z;
                    }
                    z = true;
                    FtcRobotControllerService.this.waitForNextWifiDirectCallback();
                }
            }
        }

        /* access modifiers changed from: package-private */
        public boolean waitForNetworkConnection() throws InterruptedException {
            RobotLog.m60vv(FtcRobotControllerService.TAG, "Waiting for a connection to a Wi-Fi service");
            FtcRobotControllerService.this.updateRobotStatus(RobotStatus.WAITING_ON_NETWORK_CONNECTION);
            boolean z = false;
            while (!FtcRobotControllerService.this.networkConnection.isConnected()) {
                z = true;
                FtcRobotControllerService.this.networkConnection.onWaitForConnection();
                Thread.sleep(1000);
            }
            return z;
        }

        /* access modifiers changed from: package-private */
        public void waitForNetwork() throws InterruptedException {
            if (FtcRobotControllerService.this.networkConnection.getNetworkType() == NetworkType.WIFIDIRECT) {
                waitForWifi();
                waitForWifiDirect();
                FtcRobotControllerService.this.networkConnection.createConnection();
            }
            waitForNetworkConnection();
            FtcRobotControllerService.this.webServer.start();
        }

        /* access modifiers changed from: package-private */
        public void startRobot() throws RobotCoreException {
            FtcRobotControllerService.this.updateRobotStatus(RobotStatus.STARTING_ROBOT);
            FtcRobotControllerService.this.robot.eventLoopManager.setMonitor(FtcRobotControllerService.this.eventLoopMonitor);
            FtcRobotControllerService.this.robot.start(FtcRobotControllerService.this.eventLoop);
        }

        public void run() {
            final boolean z = !FtcRobotControllerService.this.robotSetupHasBeenStarted;
            boolean unused = FtcRobotControllerService.this.robotSetupHasBeenStarted = true;
            ThreadPool.logThreadLifeCycle("RobotSetupRunnable.run()", new Runnable() {
                /* JADX WARNING: Code restructure failed: missing block: B:10:0x0038, code lost:
                    r4.this$1.runOnComplete.run();
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
                    if (r4.this$1.runOnComplete == null) goto L_0x005e;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:17:0x0060, code lost:
                    if (r0 == false) goto L_?;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:18:0x0062, code lost:
                    com.qualcomm.robotcore.util.RobotLog.m42dd(com.qualcomm.ftccommon.FtcRobotControllerService.TAG, "Detecting Wi-Fi reset");
                    com.qualcomm.ftccommon.FtcRobotControllerService.access$800(r4.this$1.this$0).detectWifiReset();
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:23:?, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:24:?, code lost:
                    return;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:4:0x0024, code lost:
                    if (r4.this$1.runOnComplete != null) goto L_0x0038;
                 */
                /* JADX WARNING: Code restructure failed: missing block: B:9:0x0036, code lost:
                    if (r4.this$1.runOnComplete == null) goto L_0x005e;
                 */
                /* JADX WARNING: Missing exception handler attribute for start block: B:6:0x0029 */
                /* Code decompiled incorrectly, please refer to instructions dump. */
                public void run() {
                    /*
                        r4 = this;
                        java.lang.String r0 = "FTCService"
                        java.lang.String r1 = "Processing robot setup"
                        com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        r1.shutdownRobot()     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        r1.awaitUSB()     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        r1.initializeEventLoopAndRobot()     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        r1.waitForNetwork()     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        r1.startRobot()     // Catch:{ RobotCoreException -> 0x0040, InterruptedException -> 0x0029 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        if (r1 == 0) goto L_0x005e
                        goto L_0x0038
                    L_0x0027:
                        r0 = move-exception
                        goto L_0x0073
                    L_0x0029:
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService r1 = com.qualcomm.ftccommon.FtcRobotControllerService.this     // Catch:{ all -> 0x0027 }
                        com.qualcomm.robotcore.robot.RobotStatus r2 = com.qualcomm.robotcore.robot.RobotStatus.ABORT_DUE_TO_INTERRUPT     // Catch:{ all -> 0x0027 }
                        r1.updateRobotStatus(r2)     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        if (r1 == 0) goto L_0x005e
                    L_0x0038:
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        r1.run()
                        goto L_0x005e
                    L_0x0040:
                        r1 = move-exception
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r2 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService r2 = com.qualcomm.ftccommon.FtcRobotControllerService.this     // Catch:{ all -> 0x0027 }
                        com.qualcomm.robotcore.robot.RobotStatus r3 = com.qualcomm.robotcore.robot.RobotStatus.UNABLE_TO_START_ROBOT     // Catch:{ all -> 0x0027 }
                        r2.updateRobotStatus(r3)     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r2 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService r2 = com.qualcomm.ftccommon.FtcRobotControllerService.this     // Catch:{ all -> 0x0027 }
                        int r3 = com.qualcomm.ftccommon.C0470R.string.globalErrorFailedToCreateRobot     // Catch:{ all -> 0x0027 }
                        java.lang.String r2 = r2.getString(r3)     // Catch:{ all -> 0x0027 }
                        com.qualcomm.robotcore.util.RobotLog.setGlobalErrorMsg((com.qualcomm.robotcore.exception.RobotCoreException) r1, (java.lang.String) r2)     // Catch:{ all -> 0x0027 }
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        if (r1 == 0) goto L_0x005e
                        goto L_0x0038
                    L_0x005e:
                        boolean r1 = r0
                        if (r1 == 0) goto L_0x0072
                        java.lang.String r1 = "Detecting Wi-Fi reset"
                        com.qualcomm.robotcore.util.RobotLog.m42dd(r0, r1)
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r0 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        com.qualcomm.ftccommon.FtcRobotControllerService r0 = com.qualcomm.ftccommon.FtcRobotControllerService.this
                        com.qualcomm.robotcore.wifi.NetworkConnection r0 = r0.networkConnection
                        r0.detectWifiReset()
                    L_0x0072:
                        return
                    L_0x0073:
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        if (r1 == 0) goto L_0x0080
                        com.qualcomm.ftccommon.FtcRobotControllerService$RobotSetupRunnable r1 = com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.this
                        java.lang.Runnable r1 = r1.runOnComplete
                        r1.run()
                    L_0x0080:
                        throw r0
                    */
                    throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.FtcRobotControllerService.RobotSetupRunnable.C04651.run():void");
                }
            });
        }
    }

    public void onReceive(Context context, Intent intent) {
        synchronized (this.wifiDirectCallbackLock) {
            this.wifiDirectCallbackLock.notifyAll();
        }
    }

    /* access modifiers changed from: package-private */
    public void waitForNextWifiDirectCallback() throws InterruptedException {
        synchronized (this.wifiDirectCallbackLock) {
            this.wifiDirectCallbackLock.wait();
        }
    }

    public NetworkConnection getNetworkConnection() {
        return this.networkConnection;
    }

    public NetworkConnection.NetworkEvent getNetworkConnectionStatus() {
        return this.networkConnectionStatus;
    }

    public void setOnBotJavaHelper(OnBotJavaHelper onBotJavaHelper2) {
        this.onBotJavaHelper = onBotJavaHelper2;
    }

    public RobotStatus getRobotStatus() {
        return this.robotStatus;
    }

    public Robot getRobot() {
        return this.robot;
    }

    public WebServer getWebServer() {
        return this.webServer;
    }

    public OnBotJavaHelper getOnBotJavaHelper() {
        return this.onBotJavaHelper;
    }

    public void onCreate() {
        super.onCreate();
        RobotLog.m60vv(TAG, "onCreate()");
        this.wifiDirectAgent.registerCallback(this);
        startLEDS();
        SoftwareVersionWarningSource.getInstance();
        ClockWarningSource.getInstance();
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        RobotLog.m60vv(TAG, "onStartCommand()");
        return super.onStartCommand(intent, i, i2);
    }

    public IBinder onBind(Intent intent) {
        RobotLog.m60vv(TAG, "onBind()");
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(C0470R.string.pref_wifip2p_remote_channel_change_works), Device.wifiP2pRemoteChannelChangeWorks());
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(C0470R.string.pref_has_independent_phone_battery), !LynxConstants.isRevControlHub());
        boolean z = !LynxConstants.isRevControlHub();
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(C0470R.string.pref_has_speaker), z);
        if (!z) {
            this.preferencesHelper.writeBooleanPrefIfDifferent(getString(C0470R.string.pref_sound_on_off), false);
        }
        FtcLynxFirmwareUpdateActivity.initializeDirectories();
        NetworkType networkType = (NetworkType) intent.getSerializableExtra(NetworkConnectionFactory.NETWORK_CONNECTION_TYPE);
        this.webServer = new CoreRobotWebServer(networkType);
        NetworkConnection networkConnection2 = NetworkConnectionFactory.getNetworkConnection(networkType, getBaseContext());
        this.networkConnection = networkConnection2;
        if (networkConnection2 == null) {
            RobotLog.setGlobalErrorMsg("Setup failure: A valid network connection type was not found for \"%s\"", networkType);
        } else {
            networkConnection2.setCallback(this);
            this.networkConnection.enable();
            this.networkConnection.createConnection();
        }
        return this.binder;
    }

    public boolean onUnbind(Intent intent) {
        RobotLog.m60vv(TAG, "onUnbind()");
        this.networkConnection.disable();
        shutdownRobot();
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        if (eventLoopManager2 == null) {
            return false;
        }
        eventLoopManager2.close();
        this.eventLoopManager = null;
        return false;
    }

    public void onDestroy() {
        super.onDestroy();
        RobotLog.m60vv(TAG, "onDestroy()");
        this.webServer.stop();
        stopLEDS();
        this.wifiDirectAgent.unregisterCallback(this);
    }

    /* access modifiers changed from: protected */
    public void startLEDS() {
        if (LynxConstants.useIndicatorLEDS()) {
            for (int i = 1; i <= 4; i++) {
                DragonboardIndicatorLED.forIndex(i).enableLight(false);
            }
            LightMultiplexor forLight = LightMultiplexor.forLight(DragonboardIndicatorLED.forIndex(4));
            this.bootIndicator = forLight;
            forLight.enableLight(true);
            this.bootIndicatorOff = ThreadPool.getDefaultScheduler().schedule(new Runnable() {
                public void run() {
                    FtcRobotControllerService.this.bootIndicator.enableLight(false);
                }
            }, 10, TimeUnit.SECONDS);
            this.livenessIndicatorBlinker = new LightBlinker(LightMultiplexor.forLight(DragonboardIndicatorLED.forIndex(1)));
            ArrayList arrayList = new ArrayList();
            arrayList.add(new Blinker.Step(-16711936, (long) 4500, TimeUnit.MILLISECONDS));
            arrayList.add(new Blinker.Step(ViewCompat.MEASURED_STATE_MASK, (long) 500, TimeUnit.MILLISECONDS));
            this.livenessIndicatorBlinker.setPattern(arrayList);
        }
    }

    /* access modifiers changed from: protected */
    public void stopLEDS() {
        Future future = this.bootIndicatorOff;
        if (future != null) {
            future.cancel(false);
            this.bootIndicatorOff = null;
        }
        SwitchableLight switchableLight = this.bootIndicator;
        if (switchableLight != null) {
            switchableLight.enableLight(false);
            this.bootIndicator = null;
        }
        LightBlinker lightBlinker = this.livenessIndicatorBlinker;
        if (lightBlinker != null) {
            lightBlinker.stopBlinking();
            this.livenessIndicatorBlinker = null;
        }
    }

    public synchronized void setCallback(UpdateUI.Callback callback2) {
        this.callback = callback2;
        callback2.updatePeerStatus(NetworkConnectionHandler.getInstance().isPeerConnected() ? PeerStatus.CONNECTED : PeerStatus.DISCONNECTED);
    }

    public synchronized void setupRobot(EventLoop eventLoop2, EventLoop eventLoop3, Runnable runnable) {
        shutdownRobotSetup();
        this.eventLoop = eventLoop2;
        this.idleEventLoop = eventLoop3;
        this.robotSetupFuture = ThreadPool.getDefault().submit(new RobotSetupRunnable(runnable));
    }

    /* access modifiers changed from: package-private */
    public void shutdownRobotSetup() {
        Future future = this.robotSetupFuture;
        if (future != null) {
            ThreadPool.cancelFutureOrExitApplication(future, 10, TimeUnit.SECONDS, "robot setup", "internal error");
            this.robotSetupFuture = null;
        }
    }

    public synchronized void shutdownRobot() {
        shutdownRobotSetup();
        Robot robot2 = this.robot;
        if (robot2 != null) {
            robot2.shutdown();
        }
        this.robot = null;
        updateRobotStatus(RobotStatus.NONE);
    }

    public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        RobotLog.m54ii(TAG, "onNetworkConnectionEvent: " + networkEvent.toString());
        int i = C04644.f64x94151df2[networkEvent.ordinal()];
        if (i == 1) {
            RobotLog.m54ii(TAG, "Wi-Fi Direct - connected as group owner");
            if (!NetworkConnection.isDeviceNameValid(this.networkConnection.getDeviceName())) {
                RobotLog.m48ee(TAG, "Network Connection device name contains non-printable characters");
                showWifiDirectNameUnprintableCharsDialog();
                callbackResult = CallbackResult.HANDLED;
            }
        } else if (i == 2) {
            RobotLog.m48ee(TAG, "Wi-Fi Direct - connected as peer, was expecting Group Owner");
            showWifiDirectConnectedAsPeerDialog();
            callbackResult = CallbackResult.HANDLED;
        } else if (i == 3) {
            RobotLog.m54ii(TAG, "Network Connection Passphrase: " + this.networkConnection.getPassphrase());
            this.webServer.start();
        } else if (i == 4) {
            RobotLog.m48ee(TAG, "Network Connection Error: " + this.networkConnection.getFailureReason());
        } else if (i == 5) {
            RobotLog.m54ii(TAG, "Network Connection created: " + this.networkConnection.getConnectionOwnerName());
        }
        updateNetworkConnectionStatus(networkEvent);
        return callbackResult;
    }

    /* renamed from: com.qualcomm.ftccommon.FtcRobotControllerService$4 */
    static /* synthetic */ class C04644 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent */
        static final /* synthetic */ int[] f64x94151df2;

        /* JADX WARNING: Can't wrap try/catch for region: R(12:0|1|2|3|4|5|6|7|8|9|10|12) */
        /* JADX WARNING: Code restructure failed: missing block: B:13:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent[] r0 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f64x94151df2 = r0
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_GROUP_OWNER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f64x94151df2     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_PEER     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f64x94151df2     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f64x94151df2     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.ERROR     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f64x94151df2     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.AP_CREATED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.FtcRobotControllerService.C04644.<clinit>():void");
        }
    }

    private void showWifiDirectNameUnprintableCharsDialog() {
        if (this.alertDialogWifiDirectNameNonPrintableChars == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 16973935));
            builder.setTitle(getString(C0470R.string.title_p2p_unprintable_chars));
            builder.setMessage(getString(C0470R.string.msg_p2p_unprintable_chars));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog unused = FtcRobotControllerService.this.alertDialogWifiDirectNameNonPrintableChars = null;
                }
            });
            this.alertDialogWifiDirectNameNonPrintableChars = builder.show();
        }
    }

    private void showWifiDirectConnectedAsPeerDialog() {
        if (this.alertDialogConnectedAsPeer == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 16973935));
            builder.setTitle(getString(C0470R.string.title_p2p_misconfigured));
            builder.setMessage(getString(C0470R.string.msg_rc_p2p_misconfigured));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    AlertDialog unused = FtcRobotControllerService.this.alertDialogConnectedAsPeer = null;
                }
            });
            this.alertDialogConnectedAsPeer = builder.show();
        }
    }

    private void updateNetworkConnectionStatus(NetworkConnection.NetworkEvent networkEvent) {
        this.networkConnectionStatus = networkEvent;
        UpdateUI.Callback callback2 = this.callback;
        if (callback2 != null) {
            callback2.networkConnectionUpdate(networkEvent);
        }
    }

    /* access modifiers changed from: private */
    public void updateRobotStatus(RobotStatus robotStatus2) {
        this.robotStatus = robotStatus2;
        UpdateUI.Callback callback2 = this.callback;
        if (callback2 != null) {
            callback2.updateRobotStatus(robotStatus2);
        }
    }
}
