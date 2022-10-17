package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pDevice;
import android.os.Looper;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.RobocolDatagramSocket;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkConnectionFactory;
import com.qualcomm.robotcore.wifi.NetworkType;
import com.qualcomm.robotcore.wifi.SoftApAssistant;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import java.net.InetAddress;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.internal.network.SendOnceRunnable;
import org.firstinspires.ftc.robotcore.internal.p013ui.RobotCoreGamepadManager;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class NetworkConnectionHandler {
    private static final int IP_ADDRESS_TIMEOUT_SECONDS = 3;
    public static final String TAG = "NetworkConnectionHandler";
    private static final NetworkConnectionHandler theInstance = new NetworkConnectionHandler();
    protected static WifiManager wifiManager = null;
    protected final Object callbackLock;
    protected String connectionOwner;
    protected String connectionOwnerPassword;
    protected Context context;
    private final SendOnceRunnable.DisconnectionCallback disconnectionCallback;
    private boolean isPeerConnected;
    protected final ElapsedTime lastRecvPacket;
    protected NetworkConnection networkConnection;
    private final List<PeerStatusCallback> peerStatusCallbacks;
    private final Object peerStatusLock;
    protected RecvLoopRunnable recvLoopRunnable;
    protected volatile InetAddress remoteAddr;
    protected ScheduledFuture<?> sendLoopFuture;
    protected ScheduledExecutorService sendLoopService;
    protected final SendOnceRunnable sendOnceRunnable;
    protected volatile boolean setupNeeded = true;
    protected volatile NetworkSetupRunnable setupRunnable;
    protected volatile RobocolDatagramSocket socket;
    protected final NetworkConnectionCallbackChainer theNetworkConnectionCallback;
    protected final RecvLoopCallbackChainer theRecvLoopCallback;
    protected final WifiManager.WifiLock wifiLock = newWifiLock();

    public NetworkConnectionHandler() {
        ElapsedTime elapsedTime = new ElapsedTime();
        this.lastRecvPacket = elapsedTime;
        this.sendLoopService = null;
        this.networkConnection = null;
        this.theNetworkConnectionCallback = new NetworkConnectionCallbackChainer();
        this.theRecvLoopCallback = new RecvLoopCallbackChainer();
        this.callbackLock = new Object();
        this.isPeerConnected = false;
        this.peerStatusCallbacks = new CopyOnWriteArrayList();
        this.peerStatusLock = new Object();
        C10921 r1 = new SendOnceRunnable.DisconnectionCallback() {
            public void disconnected() {
                NetworkConnectionHandler.this.updatePeerStatus(false, false);
            }
        };
        this.disconnectionCallback = r1;
        this.sendOnceRunnable = new SendOnceRunnable(r1, elapsedTime);
    }

    public static NetworkConnectionHandler getInstance() {
        return theInstance;
    }

    public static WifiManager getWifiManager() {
        if (wifiManager == null) {
            wifiManager = (WifiManager) AppUtil.getDefContext().getSystemService("wifi");
        }
        return wifiManager;
    }

    protected static WifiManager.WifiLock newWifiLock() {
        WifiManager.WifiLock createWifiLock = getWifiManager().createWifiLock(3, InspectionState.NO_VERSION);
        createWifiLock.setReferenceCounted(false);
        return createWifiLock;
    }

    public static NetworkType getNetworkType(Context context2) {
        if (Device.isRevControlHub()) {
            return NetworkType.RCWIRELESSAP;
        }
        return NetworkType.fromString(PreferenceManager.getDefaultSharedPreferences(context2).getString(context2.getString(C0705R.string.pref_pairing_kind), NetworkType.globalDefaultAsString()));
    }

    public void init(NetworkType networkType, String str, String str2, Context context2, RobotCoreGamepadManager robotCoreGamepadManager) {
        this.connectionOwner = str;
        this.connectionOwnerPassword = str2;
        this.context = context2;
        this.sendOnceRunnable.parameters.gamepadManager = robotCoreGamepadManager;
        shutdown();
        this.networkConnection = null;
        initNetworkConnection(networkType);
        startWifiAndDiscoverConnections();
    }

    public void init(NetworkType networkType, Context context2) {
        this.context = context2;
        initNetworkConnection(networkType);
    }

    private void initNetworkConnection(NetworkType networkType) {
        NetworkConnection networkConnection2 = this.networkConnection;
        if (!(networkConnection2 == null || networkConnection2.getNetworkType() == networkType)) {
            stop();
            shutdown();
            this.networkConnection = null;
        }
        if (this.networkConnection == null) {
            this.networkConnection = NetworkConnectionFactory.getNetworkConnection(networkType, this.context);
            synchronized (this.callbackLock) {
                this.networkConnection.setCallback(this.theNetworkConnectionCallback);
            }
        }
    }

    public void setRecvLoopRunnable(RecvLoopRunnable recvLoopRunnable2) {
        synchronized (this.callbackLock) {
            this.recvLoopRunnable = recvLoopRunnable2;
            recvLoopRunnable2.setCallback(this.theRecvLoopCallback);
        }
    }

    public NetworkConnection getNetworkConnection() {
        return this.networkConnection;
    }

    public NetworkType getNetworkType() {
        NetworkConnection networkConnection2 = this.networkConnection;
        if (networkConnection2 == null) {
            return NetworkType.UNKNOWN_NETWORK_TYPE;
        }
        return networkConnection2.getNetworkType();
    }

    public void startKeepAlives() {
        SendOnceRunnable sendOnceRunnable2 = this.sendOnceRunnable;
        if (sendOnceRunnable2 != null) {
            sendOnceRunnable2.parameters.originateKeepAlives = AppUtil.getInstance().isDriverStation() && Device.phoneImplementsAggressiveWifiScanning();
        }
    }

    public void stopKeepAlives() {
        SendOnceRunnable sendOnceRunnable2 = this.sendOnceRunnable;
        if (sendOnceRunnable2 != null) {
            sendOnceRunnable2.parameters.originateKeepAlives = false;
        }
    }

    public void startWifiAndDiscoverConnections() {
        acquireWifiLock();
        this.networkConnection.enable();
        if (!this.networkConnection.isConnected()) {
            this.networkConnection.discoverPotentialConnections();
        }
    }

    public void startConnection(String str, String str2) {
        this.connectionOwner = str;
        this.connectionOwnerPassword = str2;
        this.networkConnection.connect(str, str2);
    }

    public boolean connectedWithUnexpectedDevice() {
        String str;
        if (getNetworkType() == NetworkType.WIRELESSAP || (str = this.connectionOwner) == null || str.equals(this.networkConnection.getConnectionOwnerMacAddress())) {
            return false;
        }
        RobotLog.m48ee(TAG, "Network Connection - connected to " + this.networkConnection.getConnectionOwnerMacAddress() + ", expected " + this.connectionOwner);
        return true;
    }

    public void acquireWifiLock() {
        this.wifiLock.acquire();
    }

    public boolean isNetworkConnected() {
        return this.networkConnection.isConnected();
    }

    public boolean isWifiDirect() {
        return this.networkConnection.getNetworkType().equals(NetworkType.WIFIDIRECT);
    }

    public void discoverPotentialConnections() {
        this.networkConnection.discoverPotentialConnections();
    }

    public void cancelConnectionSearch() {
        NetworkConnection networkConnection2 = this.networkConnection;
        if (networkConnection2 != null) {
            networkConnection2.cancelPotentialConnections();
        }
    }

    public String getFailureReason() {
        return this.networkConnection.getFailureReason();
    }

    public String getConnectionOwnerName() {
        return this.networkConnection.getConnectionOwnerName();
    }

    public String getExpectedConnectionOwnerName() {
        return this.connectionOwner;
    }

    public String getDeviceName() {
        return this.networkConnection.getDeviceName();
    }

    public void stop() {
        NetworkConnection networkConnection2 = this.networkConnection;
        if (networkConnection2 != null) {
            networkConnection2.disable();
        }
        if (this.wifiLock.isHeld()) {
            this.wifiLock.release();
        }
    }

    public boolean connectingOrConnected() {
        NetworkConnection.ConnectStatus connectStatus = this.networkConnection.getConnectStatus();
        return connectStatus == NetworkConnection.ConnectStatus.CONNECTED || connectStatus == NetworkConnection.ConnectStatus.CONNECTING;
    }

    public boolean connectionMatches(String str) {
        String str2 = this.connectionOwner;
        return str2 != null && str2.equals(str);
    }

    public boolean readyForCommandProcessing() {
        synchronized (this.callbackLock) {
            if (this.recvLoopRunnable == null) {
                return false;
            }
            return true;
        }
    }

    public boolean isPeerConnected() {
        boolean z;
        synchronized (this.peerStatusLock) {
            z = this.isPeerConnected;
        }
        return z;
    }

    public boolean isShutDown() {
        return this.setupNeeded;
    }

    public boolean registerPeerStatusCallback(PeerStatusCallback peerStatusCallback) {
        boolean z;
        synchronized (this.peerStatusLock) {
            this.peerStatusCallbacks.add(peerStatusCallback);
            z = this.isPeerConnected;
        }
        return z;
    }

    /* access modifiers changed from: private */
    public void updatePeerStatus(boolean z, boolean z2) {
        boolean z3;
        synchronized (this.peerStatusLock) {
            z3 = z != this.isPeerConnected;
            this.isPeerConnected = z;
        }
        if (z3 || z2) {
            for (PeerStatusCallback next : this.peerStatusCallbacks) {
                if (this.isPeerConnected) {
                    next.onPeerConnected();
                } else {
                    next.onPeerDisconnected();
                }
            }
            if (!z3) {
                return;
            }
            if (this.isPeerConnected) {
                RobotLog.m60vv(TAG, "Peer connection established");
            } else {
                RobotLog.m60vv(TAG, "Peer connection lost");
            }
        }
    }

    public synchronized CallbackResult handleConnectionInfoAvailable() {
        CallbackResult callbackResult;
        callbackResult = CallbackResult.HANDLED;
        RobotLog.m54ii(TAG, "Handling new network connection infomation, connected: " + this.networkConnection.isConnected() + " setup needed: " + this.setupNeeded);
        this.lastRecvPacket.reset();
        if (this.networkConnection.isConnected() && this.setupNeeded) {
            this.setupNeeded = false;
            if (this.networkConnection.getNetworkType() != NetworkType.WIFIDIRECT) {
                ElapsedTime elapsedTime = new ElapsedTime();
                int ipAddress = getWifiManager().getConnectionInfo().getIpAddress();
                while (ipAddress == 0 && elapsedTime.seconds() < 3.0d && !Thread.currentThread().isInterrupted()) {
                    ipAddress = getWifiManager().getConnectionInfo().getIpAddress();
                    try {
                        Thread.sleep(50);
                    } catch (InterruptedException e) {
                        RobotLog.m50ee(TAG, (Throwable) e, "Thread interrupted while waiting for IP address");
                        Thread.currentThread().interrupt();
                    }
                }
            }
            synchronized (this.callbackLock) {
                this.setupRunnable = new NetworkSetupRunnable(this.theRecvLoopCallback, this.networkConnection, this.lastRecvPacket);
            }
            new Thread(this.setupRunnable).start();
        }
        return callbackResult;
    }

    public synchronized CallbackResult handlePeersAvailable() {
        CallbackResult callbackResult;
        callbackResult = CallbackResult.NOT_HANDLED;
        NetworkType networkType = this.networkConnection.getNetworkType();
        int i = C10943.$SwitchMap$com$qualcomm$robotcore$wifi$NetworkType[networkType.ordinal()];
        if (i == 1) {
            callbackResult = handleWifiDirectPeersAvailable();
        } else if (i == 2) {
            callbackResult = handleSoftAPPeersAvailable();
        } else if (i == 3 || i == 4) {
            RobotLog.m46e("Unhandled peers available event: " + networkType.toString());
        }
        return callbackResult;
    }

    private CallbackResult handleSoftAPPeersAvailable() {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        for (ScanResult next : ((SoftApAssistant) this.networkConnection).getScanResults()) {
            RobotLog.m58v(next.SSID);
            if (next.SSID.equalsIgnoreCase(this.connectionOwner)) {
                this.networkConnection.connect(this.connectionOwner, this.connectionOwnerPassword);
                return CallbackResult.HANDLED;
            }
        }
        return callbackResult;
    }

    private CallbackResult handleWifiDirectPeersAvailable() {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        for (WifiP2pDevice next : ((WifiDirectAssistant) this.networkConnection).getPeers()) {
            if (next.deviceAddress.equalsIgnoreCase(this.connectionOwner)) {
                this.networkConnection.connect(next.deviceAddress);
                return CallbackResult.HANDLED;
            }
        }
        return callbackResult;
    }

    /* JADX WARNING: Code restructure failed: missing block: B:22:0x0087, code lost:
        return r0;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.qualcomm.robotcore.robocol.PeerDiscovery updateConnection(com.qualcomm.robotcore.robocol.RobocolDatagram r11) throws com.qualcomm.robotcore.exception.RobotCoreException, com.qualcomm.robotcore.exception.RobotProtocolException {
        /*
            r10 = this;
            monitor-enter(r10)
            org.firstinspires.ftc.robotcore.internal.network.NetworkSetupRunnable r0 = r10.setupRunnable     // Catch:{ all -> 0x0096 }
            if (r0 == 0) goto L_0x000d
            org.firstinspires.ftc.robotcore.internal.network.NetworkSetupRunnable r0 = r10.setupRunnable     // Catch:{ all -> 0x0096 }
            com.qualcomm.robotcore.robocol.RobocolDatagramSocket r0 = r0.getSocket()     // Catch:{ all -> 0x0096 }
            r10.socket = r0     // Catch:{ all -> 0x0096 }
        L_0x000d:
            com.qualcomm.robotcore.robocol.PeerDiscovery r0 = com.qualcomm.robotcore.robocol.PeerDiscovery.forReceive()     // Catch:{ all -> 0x0096 }
            byte[] r1 = r11.getData()     // Catch:{ all -> 0x0096 }
            r0.fromByteArray(r1)     // Catch:{ all -> 0x0096 }
            com.qualcomm.robotcore.robocol.PeerDiscovery$PeerType r1 = r0.getPeerType()     // Catch:{ all -> 0x0096 }
            com.qualcomm.robotcore.robocol.PeerDiscovery$PeerType r2 = com.qualcomm.robotcore.robocol.PeerDiscovery.PeerType.NOT_CONNECTED_DUE_TO_PREEXISTING_CONNECTION     // Catch:{ all -> 0x0096 }
            if (r1 == r2) goto L_0x0088
            com.qualcomm.robotcore.util.ElapsedTime r1 = r10.lastRecvPacket     // Catch:{ all -> 0x0096 }
            r1.reset()     // Catch:{ all -> 0x0096 }
            java.net.InetAddress r1 = r11.getAddress()     // Catch:{ all -> 0x0096 }
            java.net.InetAddress r2 = r10.remoteAddr     // Catch:{ all -> 0x0096 }
            boolean r1 = r1.equals(r2)     // Catch:{ all -> 0x0096 }
            r2 = 1
            if (r1 == 0) goto L_0x0038
            r11 = 0
            r10.updatePeerStatus(r2, r11)     // Catch:{ all -> 0x0096 }
            monitor-exit(r10)
            return r0
        L_0x0038:
            java.net.InetAddress r11 = r11.getAddress()     // Catch:{ all -> 0x0096 }
            r10.remoteAddr = r11     // Catch:{ all -> 0x0096 }
            java.lang.String r11 = "PeerDiscovery"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0096 }
            r1.<init>()     // Catch:{ all -> 0x0096 }
            java.lang.String r3 = "new remote peer discovered: "
            r1.append(r3)     // Catch:{ all -> 0x0096 }
            java.net.InetAddress r3 = r10.remoteAddr     // Catch:{ all -> 0x0096 }
            java.lang.String r3 = r3.getHostAddress()     // Catch:{ all -> 0x0096 }
            r1.append(r3)     // Catch:{ all -> 0x0096 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0096 }
            com.qualcomm.robotcore.util.RobotLog.m60vv(r11, r1)     // Catch:{ all -> 0x0096 }
            com.qualcomm.robotcore.robocol.RobocolDatagramSocket r11 = r10.socket     // Catch:{ all -> 0x0096 }
            if (r11 == 0) goto L_0x0086
            java.util.concurrent.ScheduledFuture<?> r11 = r10.sendLoopFuture     // Catch:{ all -> 0x0096 }
            if (r11 == 0) goto L_0x0068
            boolean r11 = r11.isDone()     // Catch:{ all -> 0x0096 }
            if (r11 == 0) goto L_0x0083
        L_0x0068:
            java.lang.String r11 = "NetworkConnectionHandler"
            java.lang.String r1 = "starting sending loop"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r11, r1)     // Catch:{ all -> 0x0096 }
            java.util.concurrent.ScheduledExecutorService r3 = java.util.concurrent.Executors.newSingleThreadScheduledExecutor()     // Catch:{ all -> 0x0096 }
            r10.sendLoopService = r3     // Catch:{ all -> 0x0096 }
            org.firstinspires.ftc.robotcore.internal.network.SendOnceRunnable r4 = r10.sendOnceRunnable     // Catch:{ all -> 0x0096 }
            r5 = 0
            r7 = 40
            java.util.concurrent.TimeUnit r9 = java.util.concurrent.TimeUnit.MILLISECONDS     // Catch:{ all -> 0x0096 }
            java.util.concurrent.ScheduledFuture r11 = r3.scheduleAtFixedRate(r4, r5, r7, r9)     // Catch:{ all -> 0x0096 }
            r10.sendLoopFuture = r11     // Catch:{ all -> 0x0096 }
        L_0x0083:
            r10.updatePeerStatus(r2, r2)     // Catch:{ all -> 0x0096 }
        L_0x0086:
            monitor-exit(r10)
            return r0
        L_0x0088:
            com.qualcomm.robotcore.exception.RobotProtocolException r11 = new com.qualcomm.robotcore.exception.RobotProtocolException     // Catch:{ all -> 0x0096 }
            android.content.Context r0 = r10.context     // Catch:{ all -> 0x0096 }
            int r1 = com.qualcomm.robotcore.C0705R.string.anotherDsIsConnectedError     // Catch:{ all -> 0x0096 }
            java.lang.String r0 = r0.getString(r1)     // Catch:{ all -> 0x0096 }
            r11.<init>(r0)     // Catch:{ all -> 0x0096 }
            throw r11     // Catch:{ all -> 0x0096 }
        L_0x0096:
            r11 = move-exception
            monitor-exit(r10)
            throw r11
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler.updateConnection(com.qualcomm.robotcore.robocol.RobocolDatagram):com.qualcomm.robotcore.robocol.PeerDiscovery");
    }

    public boolean removeCommand(Command command) {
        SendOnceRunnable sendOnceRunnable2 = this.sendOnceRunnable;
        return sendOnceRunnable2 != null && sendOnceRunnable2.removeCommand(command);
    }

    public void sendCommand(Command command) {
        SendOnceRunnable sendOnceRunnable2 = this.sendOnceRunnable;
        if (sendOnceRunnable2 != null) {
            sendOnceRunnable2.sendCommand(command);
        }
    }

    public void sendReply(Command command, Command command2) {
        if (wasTransmittedRemotely(command)) {
            sendCommand(command2);
        } else {
            injectReceivedCommand(command2);
        }
    }

    /* access modifiers changed from: protected */
    public boolean wasTransmittedRemotely(Command command) {
        return !command.isInjected();
    }

    public void injectReceivedCommand(Command command) {
        NetworkSetupRunnable networkSetupRunnable = this.setupRunnable;
        if (networkSetupRunnable != null) {
            command.setIsInjected(true);
            networkSetupRunnable.injectReceivedCommand(command);
            RobotLog.m61vv("Robocol", "locally injecting %s", command.getName());
            return;
        }
        RobotLog.m60vv(TAG, "injectReceivedCommand(): setupRunnable==null; command ignored");
    }

    public CallbackResult processAcknowledgments(Command command) throws RobotCoreException {
        if (command.isAcknowledged()) {
            if (SendOnceRunnable.DEBUG) {
                RobotLog.m61vv("Robocol", "received ack: %s(%d)", command.getName(), Integer.valueOf(command.getSequenceNumber()));
            }
            removeCommand(command);
            return CallbackResult.HANDLED;
        }
        command.acknowledge();
        sendCommand(command);
        return CallbackResult.NOT_HANDLED;
    }

    public void sendDataToPeer(RobocolParsable robocolParsable) throws RobotCoreException {
        InetAddress inetAddress = this.remoteAddr;
        if (inetAddress != null) {
            sendDatagram(new RobocolDatagram(robocolParsable, inetAddress));
        }
    }

    public void sendDatagram(final RobocolDatagram robocolDatagram) {
        C10932 r0 = new Runnable() {
            public void run() {
                RobocolDatagramSocket robocolDatagramSocket = NetworkConnectionHandler.this.socket;
                if (robocolDatagramSocket != null) {
                    robocolDatagramSocket.send(robocolDatagram);
                }
            }
        };
        if (Looper.myLooper() == Looper.getMainLooper()) {
            ThreadPool.getDefault().execute(r0);
        } else {
            r0.run();
        }
    }

    public synchronized void clientDisconnect() {
        SendOnceRunnable sendOnceRunnable2 = this.sendOnceRunnable;
        if (sendOnceRunnable2 != null) {
            sendOnceRunnable2.clearCommands();
        }
        this.remoteAddr = null;
    }

    public synchronized void shutdown() {
        if (this.setupRunnable != null) {
            this.setupRunnable.shutdown();
            this.setupRunnable = null;
        }
        ScheduledFuture<?> scheduledFuture = this.sendLoopFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            this.sendLoopFuture = null;
        }
        ScheduledExecutorService scheduledExecutorService = this.sendLoopService;
        if (scheduledExecutorService != null) {
            scheduledExecutorService.shutdown();
            this.sendLoopService = null;
        }
        this.remoteAddr = null;
        this.setupNeeded = true;
    }

    public void stopPeerDiscovery() {
        if (this.setupRunnable != null) {
            this.setupRunnable.stopPeerDiscovery();
        }
    }

    public long getRxDataCount() {
        NetworkSetupRunnable networkSetupRunnable = this.setupRunnable;
        if (networkSetupRunnable != null) {
            return networkSetupRunnable.getRxDataCount();
        }
        return 0;
    }

    public long getTxDataCount() {
        NetworkSetupRunnable networkSetupRunnable = this.setupRunnable;
        if (networkSetupRunnable != null) {
            return networkSetupRunnable.getTxDataCount();
        }
        return 0;
    }

    public long getBytesPerSecond() {
        NetworkSetupRunnable networkSetupRunnable = this.setupRunnable;
        if (networkSetupRunnable != null) {
            return networkSetupRunnable.getBytesPerSecond();
        }
        return 0;
    }

    public int getWifiChannel() {
        return this.networkConnection.getWifiChannel();
    }

    public InetAddress getCurrentPeerAddr() {
        return this.remoteAddr;
    }

    public void pushNetworkConnectionCallback(NetworkConnection.NetworkConnectionCallback networkConnectionCallback) {
        synchronized (this.callbackLock) {
            this.theNetworkConnectionCallback.push(networkConnectionCallback);
        }
    }

    public void removeNetworkConnectionCallback(NetworkConnection.NetworkConnectionCallback networkConnectionCallback) {
        synchronized (this.callbackLock) {
            this.theNetworkConnectionCallback.remove(networkConnectionCallback);
        }
    }

    protected class NetworkConnectionCallbackChainer implements NetworkConnection.NetworkConnectionCallback {
        protected final CopyOnWriteArrayList<NetworkConnection.NetworkConnectionCallback> callbacks = new CopyOnWriteArrayList<>();

        protected NetworkConnectionCallbackChainer() {
        }

        /* access modifiers changed from: package-private */
        public void push(NetworkConnection.NetworkConnectionCallback networkConnectionCallback) {
            synchronized (this.callbacks) {
                remove(networkConnectionCallback);
                if (networkConnectionCallback != null && !this.callbacks.contains(networkConnectionCallback)) {
                    this.callbacks.add(0, networkConnectionCallback);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(NetworkConnection.NetworkConnectionCallback networkConnectionCallback) {
            synchronized (this.callbacks) {
                if (networkConnectionCallback != null) {
                    this.callbacks.remove(networkConnectionCallback);
                }
            }
        }

        public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
            Iterator<NetworkConnection.NetworkConnectionCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().onNetworkConnectionEvent(networkEvent).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }
    }

    public void pushReceiveLoopCallback(RecvLoopRunnable.RecvLoopCallback recvLoopCallback) {
        synchronized (this.callbackLock) {
            this.theRecvLoopCallback.push(recvLoopCallback);
        }
    }

    public void removeReceiveLoopCallback(RecvLoopRunnable.RecvLoopCallback recvLoopCallback) {
        synchronized (this.callbackLock) {
            this.theRecvLoopCallback.remove(recvLoopCallback);
        }
    }

    protected class RecvLoopCallbackChainer implements RecvLoopRunnable.RecvLoopCallback {
        protected final CopyOnWriteArrayList<RecvLoopRunnable.RecvLoopCallback> callbacks = new CopyOnWriteArrayList<>();

        protected RecvLoopCallbackChainer() {
        }

        /* access modifiers changed from: package-private */
        public void push(RecvLoopRunnable.RecvLoopCallback recvLoopCallback) {
            synchronized (this.callbacks) {
                remove(recvLoopCallback);
                if (recvLoopCallback != null && !this.callbacks.contains(recvLoopCallback)) {
                    this.callbacks.add(0, recvLoopCallback);
                }
            }
        }

        /* access modifiers changed from: package-private */
        public void remove(RecvLoopRunnable.RecvLoopCallback recvLoopCallback) {
            synchronized (this.callbacks) {
                if (recvLoopCallback != null) {
                    this.callbacks.remove(recvLoopCallback);
                }
            }
        }

        public CallbackResult packetReceived(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().packetReceived(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().peerDiscoveryEvent(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().heartbeatEvent(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            boolean z = false;
            while (it.hasNext()) {
                CallbackResult commandEvent = it.next().commandEvent(command);
                z = z || commandEvent.isHandled();
                if (commandEvent.stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            if (!z) {
                StringBuilder sb = new StringBuilder();
                Iterator<RecvLoopRunnable.RecvLoopCallback> it2 = this.callbacks.iterator();
                while (it2.hasNext()) {
                    RecvLoopRunnable.RecvLoopCallback next = it2.next();
                    if (sb.length() > 0) {
                        sb.append(",");
                    }
                    sb.append(next.getClass().getSimpleName());
                }
                RobotLog.m61vv("Robocol", "unable to process command %s callbacks=%s", command.getName(), sb.toString());
            }
            return z ? CallbackResult.HANDLED : CallbackResult.NOT_HANDLED;
        }

        public CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().telemetryEvent(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().gamepadEvent(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult emptyEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().emptyEvent(robocolDatagram).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult reportGlobalError(String str, boolean z) {
            Iterator<RecvLoopRunnable.RecvLoopCallback> it = this.callbacks.iterator();
            while (it.hasNext()) {
                if (it.next().reportGlobalError(str, z).stopDispatch()) {
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }
    }
}
