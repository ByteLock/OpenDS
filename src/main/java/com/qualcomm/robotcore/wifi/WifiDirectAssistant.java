package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.NetworkInfo;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pGroup;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Looper;
import androidx.core.content.ContextCompat;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;

import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.network.ApChannel;
import org.firstinspires.ftc.robotcore.network.ApChannelManagerFactory;
import org.firstinspires.ftc.robotcore.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.network.InvalidNetworkSettingException;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.network.WifiUtil;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class WifiDirectAssistant extends NetworkConnection {
    public static final String TAG = "WifiDirect";
    private static WifiDirectAssistant wifiDirectAssistant;
    private int clients = 0;
    /* access modifiers changed from: private */
    public NetworkConnection.ConnectStatus connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
    /* access modifiers changed from: private */
    public final Object connectStatusLock = new Object();
    /* access modifiers changed from: private */
    public final WifiDirectConnectionInfoListener connectionListener;
    /* access modifiers changed from: private */
    public String deviceMacAddress = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public String deviceName = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public int failureReason = 0;
    /* access modifiers changed from: private */
    public boolean groupFormed = false;
    /* access modifiers changed from: private */
    public final WifiDirectGroupInfoListener groupInfoListener;
    /* access modifiers changed from: private */
    public String groupInterface = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public String groupNetworkName = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public InetAddress groupOwnerAddress = null;
    /* access modifiers changed from: private */
    public final Object groupOwnerLock = new Object();
    /* access modifiers changed from: private */
    public String groupOwnerMacAddress = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public String groupOwnerName = InspectionState.NO_VERSION;
    private final IntentFilter intentFilter;
    /* access modifiers changed from: private */
    public boolean isWifiP2pEnabled = false;
    private NetworkConnection.NetworkEvent lastEvent = null;
    /* access modifiers changed from: private */
    public String passphrase = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    public final WifiDirectPeerListListener peerListListener;
    /* access modifiers changed from: private */
    public final List<WifiP2pDevice> peers = new ArrayList();
    /* access modifiers changed from: private */
    public PreferencesHelper preferencesHelper;
    private WifiP2pBroadcastReceiver receiver;
    /* access modifiers changed from: private */
    public final WifiP2pManager.Channel wifiP2pChannel;
    /* access modifiers changed from: private */
    public final WifiP2pManager wifiP2pManager;

    public void detectWifiReset() {
    }

    public void onWaitForConnection() {
    }

    private class WifiDirectPeerListListener implements WifiP2pManager.PeerListListener {
        private WifiDirectPeerListListener() {
        }

        public void onPeersAvailable(WifiP2pDeviceList wifiP2pDeviceList) {
            WifiDirectAssistant.this.peers.clear();
            WifiDirectAssistant.this.peers.addAll(wifiP2pDeviceList.getDeviceList());
            RobotLog.m60vv(WifiDirectAssistant.TAG, "peers found: " + WifiDirectAssistant.this.peers.size());
            if (WifiDirectAssistant.this.peers.size() == 0) {
                WifiUtil.doLocationServicesCheck();
            }
            for (WifiP2pDevice wifiP2pDevice : WifiDirectAssistant.this.peers) {
                RobotLog.m60vv(WifiDirectAssistant.TAG, "    peer: " + wifiP2pDevice.deviceAddress + " " + wifiP2pDevice.deviceName);
            }
            WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.PEERS_AVAILABLE);
        }
    }

    private class WifiDirectConnectionInfoListener implements WifiP2pManager.ConnectionInfoListener {
        private WifiDirectConnectionInfoListener() {
        }

        public void onConnectionInfoAvailable(WifiP2pInfo wifiP2pInfo) {
            if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") != -1) {
                WifiDirectAssistant.this.wifiP2pManager.requestGroupInfo(WifiDirectAssistant.this.wifiP2pChannel, WifiDirectAssistant.this.groupInfoListener);
                synchronized (WifiDirectAssistant.this.groupOwnerLock) {
                    InetAddress unused = WifiDirectAssistant.this.groupOwnerAddress = wifiP2pInfo.groupOwnerAddress;
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "group owners address: " + WifiDirectAssistant.this.groupOwnerAddress.toString());
                }
                if (wifiP2pInfo.groupFormed && wifiP2pInfo.isGroupOwner) {
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "group formed, this device is the group owner (GO)");
                    synchronized (WifiDirectAssistant.this.connectStatusLock) {
                        NetworkConnection.ConnectStatus unused2 = WifiDirectAssistant.this.connectStatus = NetworkConnection.ConnectStatus.GROUP_OWNER;
                    }
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.CONNECTED_AS_GROUP_OWNER);
                } else if (wifiP2pInfo.groupFormed) {
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "group formed, this device is a client");
                    synchronized (WifiDirectAssistant.this.connectStatusLock) {
                        NetworkConnection.ConnectStatus unused3 = WifiDirectAssistant.this.connectStatus = NetworkConnection.ConnectStatus.CONNECTED;
                    }
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.CONNECTED_AS_PEER);
                } else {
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "group NOT formed, ERROR: " + wifiP2pInfo.toString());
                    int unused4 = WifiDirectAssistant.this.failureReason = 0;
                    synchronized (WifiDirectAssistant.this.connectStatusLock) {
                        NetworkConnection.ConnectStatus unused5 = WifiDirectAssistant.this.connectStatus = NetworkConnection.ConnectStatus.ERROR;
                    }
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.ERROR);
                }
            } else {
                throw new RuntimeException("We do NOT have permission to access fine location");
            }
        }
    }

    private class WifiDirectGroupInfoListener implements WifiP2pManager.GroupInfoListener {
        private WifiDirectGroupInfoListener() {
        }

        public void onGroupInfoAvailable(WifiP2pGroup wifiP2pGroup) {
            if (wifiP2pGroup != null) {
                if (wifiP2pGroup.isGroupOwner()) {
                    WifiDirectAssistant wifiDirectAssistant = WifiDirectAssistant.this;
                    String unused = wifiDirectAssistant.groupOwnerMacAddress = wifiDirectAssistant.deviceMacAddress;
                    WifiDirectAssistant wifiDirectAssistant2 = WifiDirectAssistant.this;
                    String unused2 = wifiDirectAssistant2.groupOwnerName = wifiDirectAssistant2.deviceName;
                } else {
                    WifiP2pDevice owner = wifiP2pGroup.getOwner();
                    String unused3 = WifiDirectAssistant.this.groupOwnerMacAddress = owner.deviceAddress;
                    String unused4 = WifiDirectAssistant.this.groupOwnerName = owner.deviceName;
                }
                String unused5 = WifiDirectAssistant.this.groupInterface = wifiP2pGroup.getInterface();
                String unused6 = WifiDirectAssistant.this.groupNetworkName = wifiP2pGroup.getNetworkName();
                String unused7 = WifiDirectAssistant.this.passphrase = wifiP2pGroup.getPassphrase();
                WifiDirectAssistant wifiDirectAssistant3 = WifiDirectAssistant.this;
                String unused8 = wifiDirectAssistant3.passphrase = wifiDirectAssistant3.passphrase != null ? WifiDirectAssistant.this.passphrase : InspectionState.NO_VERSION;
                RobotLog.m60vv(WifiDirectAssistant.TAG, "connection information available");
                RobotLog.m60vv(WifiDirectAssistant.TAG, "connection information - groupOwnerName = " + WifiDirectAssistant.this.groupOwnerName);
                RobotLog.m60vv(WifiDirectAssistant.TAG, "connection information - groupOwnerMacAddress = " + WifiDirectAssistant.this.groupOwnerMacAddress);
                RobotLog.m60vv(WifiDirectAssistant.TAG, "connection information - groupInterface = " + WifiDirectAssistant.this.groupInterface);
                RobotLog.m60vv(WifiDirectAssistant.TAG, "connection information - groupNetworkName = " + WifiDirectAssistant.this.groupNetworkName);
                WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
            }
        }
    }

    private class WifiP2pBroadcastReceiver extends BroadcastReceiver {
        private WifiP2pBroadcastReceiver() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            boolean z = true;
            if ("android.net.wifi.p2p.STATE_CHANGED".equals(action)) {
                int intExtra = intent.getIntExtra("wifi_p2p_state", -1);
                WifiDirectAssistant wifiDirectAssistant = WifiDirectAssistant.this;
                if (intExtra != 2) {
                    z = false;
                }
                boolean unused = wifiDirectAssistant.isWifiP2pEnabled = z;
                RobotLog.m42dd(WifiDirectAssistant.TAG, "broadcast: state - enabled: " + WifiDirectAssistant.this.isWifiP2pEnabled);
            } else if ("android.net.wifi.p2p.PEERS_CHANGED".equals(action)) {
                RobotLog.m42dd(WifiDirectAssistant.TAG, "broadcast: peers changed");
                if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") != -1) {
                    WifiDirectAssistant.this.wifiP2pManager.requestPeers(WifiDirectAssistant.this.wifiP2pChannel, WifiDirectAssistant.this.peerListListener);
                    return;
                }
                throw new RuntimeException("We do NOT have permission to access fine location");
            } else if ("android.net.wifi.p2p.CONNECTION_STATE_CHANGE".equals(action)) {
                NetworkInfo networkInfo = (NetworkInfo) intent.getParcelableExtra("networkInfo");
                WifiP2pInfo wifiP2pInfo = (WifiP2pInfo) intent.getParcelableExtra("wifiP2pInfo");
                WifiP2pGroup wifiP2pGroup = (WifiP2pGroup) intent.getParcelableExtra("p2pGroupInfo");
                RobotLog.m43dd(WifiDirectAssistant.TAG, "broadcast: connection changed: connectStatus=%s networkInfo.state=%s", WifiDirectAssistant.this.connectStatus, networkInfo.getState());
                if (!networkInfo.isConnected()) {
                    WifiDirectAssistant.this.preferencesHelper.remove(context.getString(C0705R.string.pref_wifip2p_groupowner_connectedto));
                    synchronized (WifiDirectAssistant.this.connectStatusLock) {
                        NetworkConnection.ConnectStatus unused2 = WifiDirectAssistant.this.connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
                    }
                    if (!WifiDirectAssistant.this.groupFormed) {
                        WifiDirectAssistant.this.discoverPeers();
                    }
                    if (WifiDirectAssistant.this.isConnected()) {
                        RobotLog.m60vv(WifiDirectAssistant.TAG, "disconnecting");
                        WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.DISCONNECTED);
                    }
                    boolean unused3 = WifiDirectAssistant.this.groupFormed = wifiP2pInfo.groupFormed;
                } else if (!WifiDirectAssistant.this.isConnected()) {
                    WifiDirectAssistant.this.preferencesHelper.writeStringPrefIfDifferent(context.getString(C0705R.string.pref_wifip2p_groupowner_connectedto), wifiP2pGroup.getOwner().deviceName);
                    WifiDirectAssistant.this.wifiP2pManager.requestConnectionInfo(WifiDirectAssistant.this.wifiP2pChannel, WifiDirectAssistant.this.connectionListener);
                    WifiDirectAssistant.this.wifiP2pManager.stopPeerDiscovery(WifiDirectAssistant.this.wifiP2pChannel, (WifiP2pManager.ActionListener) null);
                }
            } else if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(action)) {
                RobotLog.m42dd(WifiDirectAssistant.TAG, "broadcast: this device changed");
                WifiDirectAssistant.this.updateLocalDeviceInfo((WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice"));
            } else {
                RobotLog.m43dd(WifiDirectAssistant.TAG, "broadcast: %s", action);
            }
        }
    }

    public static synchronized WifiDirectAssistant getWifiDirectAssistant(Context context) {
        WifiDirectAssistant wifiDirectAssistant2;
        synchronized (WifiDirectAssistant.class) {
            if (wifiDirectAssistant == null) {
                wifiDirectAssistant = new WifiDirectAssistant(context);
            }
            wifiDirectAssistant2 = wifiDirectAssistant;
        }
        return wifiDirectAssistant2;
    }

    private WifiDirectAssistant(Context context) {
        super(context);
        IntentFilter intentFilter2 = new IntentFilter();
        this.intentFilter = intentFilter2;
        intentFilter2.addAction("android.net.wifi.p2p.STATE_CHANGED");
        intentFilter2.addAction("android.net.wifi.p2p.PEERS_CHANGED");
        intentFilter2.addAction("android.net.wifi.p2p.CONNECTION_STATE_CHANGE");
        intentFilter2.addAction("android.net.wifi.p2p.THIS_DEVICE_CHANGED");
        WifiP2pManager wifiP2pManager2 = (WifiP2pManager) context.getSystemService("wifip2p");
        this.wifiP2pManager = wifiP2pManager2;
        this.wifiP2pChannel = wifiP2pManager2.initialize(context, Looper.getMainLooper(), (WifiP2pManager.ChannelListener) null);
        this.receiver = new WifiP2pBroadcastReceiver();
        this.connectionListener = new WifiDirectConnectionInfoListener();
        this.peerListListener = new WifiDirectPeerListListener();
        this.groupInfoListener = new WifiDirectGroupInfoListener();
        PreferencesHelper preferencesHelper2 = new PreferencesHelper(TAG, this.context);
        this.preferencesHelper = preferencesHelper2;
        preferencesHelper2.remove(context.getString(C0705R.string.pref_wifip2p_groupowner_connectedto));
    }

    public NetworkType getNetworkType() {
        return NetworkType.WIFIDIRECT;
    }

    public synchronized void enable() {
        this.clients++;
        RobotLog.m60vv(TAG, "There are " + this.clients + " Wi-Fi Direct Assistant Clients (+)");
        if (Device.isRevDriverHub()) {
            RobotLog.m60vv(TAG, "Disconnecting from normal Wi-Fi access point");
            NetworkConnectionHandler.getWifiManager().disconnect();
        }
        if (this.clients == 1) {
            RobotLog.m60vv(TAG, "Enabling Wi-Fi Direct Assistant");
            if (this.receiver == null) {
                this.receiver = new WifiP2pBroadcastReceiver();
            }
            this.context.registerReceiver(this.receiver, this.intentFilter);
            if (Build.VERSION.SDK_INT >= 29) {
                WifiDirectAssistantAndroid10Extensions.handleRegisterBroadcastReceiver(this.wifiP2pManager, this.wifiP2pChannel, new WifiDirectAssistantAndroid10Extensions.DelegateDeviceInfoListener() {
                    /* access modifiers changed from: package-private */
                    public void onDeviceInfoAvailable(WifiP2pDevice wifiP2pDevice) {
                        if (wifiP2pDevice != null) {
                            WifiDirectAssistant.this.updateLocalDeviceInfo(wifiP2pDevice);
                        }
                    }
                });
            }
        }
        WifiDirectAgent.getInstance().doListen();
    }

    /*  JADX ERROR: IndexOutOfBoundsException in pass: RegionMakerVisitor
        java.lang.IndexOutOfBoundsException: Index 0 out of bounds for length 0
        	at java.base/jdk.internal.util.Preconditions.outOfBounds(Preconditions.java:64)
        	at java.base/jdk.internal.util.Preconditions.outOfBoundsCheckIndex(Preconditions.java:70)
        	at java.base/jdk.internal.util.Preconditions.checkIndex(Preconditions.java:248)
        	at java.base/java.util.Objects.checkIndex(Objects.java:372)
        	at java.base/java.util.ArrayList.get(ArrayList.java:458)
        	at jadx.core.dex.nodes.InsnNode.getArg(InsnNode.java:101)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:611)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverseMonitorExits(RegionMaker.java:619)
        	at jadx.core.dex.visitors.regions.RegionMaker.processMonitorEnter(RegionMaker.java:561)
        	at jadx.core.dex.visitors.regions.RegionMaker.traverse(RegionMaker.java:133)
        	at jadx.core.dex.visitors.regions.RegionMaker.makeRegion(RegionMaker.java:86)
        	at jadx.core.dex.visitors.regions.RegionMaker.processExcHandler(RegionMaker.java:1043)
        	at jadx.core.dex.visitors.regions.RegionMaker.processTryCatchBlocks(RegionMaker.java:975)
        	at jadx.core.dex.visitors.regions.RegionMakerVisitor.visit(RegionMakerVisitor.java:52)
        */
    public synchronized void disable() {
        /*
            r3 = this;
            monitor-enter(r3)
            int r0 = r3.clients     // Catch:{ all -> 0x0055 }
            int r0 = r0 + -1
            r3.clients = r0     // Catch:{ all -> 0x0055 }
            java.lang.String r0 = "WifiDirect"
            java.lang.StringBuilder r1 = new java.lang.StringBuilder     // Catch:{ all -> 0x0055 }
            r1.<init>()     // Catch:{ all -> 0x0055 }
            java.lang.String r2 = "There are "
            r1.append(r2)     // Catch:{ all -> 0x0055 }
            int r2 = r3.clients     // Catch:{ all -> 0x0055 }
            r1.append(r2)     // Catch:{ all -> 0x0055 }
            java.lang.String r2 = " Wi-Fi Direct Assistant Clients (-)"
            r1.append(r2)     // Catch:{ all -> 0x0055 }
            java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x0055 }
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)     // Catch:{ all -> 0x0055 }
            int r0 = r3.clients     // Catch:{ all -> 0x0055 }
            if (r0 != 0) goto L_0x0053
            java.lang.String r0 = "WifiDirect"
            java.lang.String r1 = "Disabling Wi-Fi Direct Assistant"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)     // Catch:{ all -> 0x0055 }
            android.net.wifi.p2p.WifiP2pManager r0 = r3.wifiP2pManager     // Catch:{ all -> 0x0055 }
            android.net.wifi.p2p.WifiP2pManager$Channel r1 = r3.wifiP2pChannel     // Catch:{ all -> 0x0055 }
            r2 = 0
            r0.stopPeerDiscovery(r1, r2)     // Catch:{ all -> 0x0055 }
            android.net.wifi.p2p.WifiP2pManager r0 = r3.wifiP2pManager     // Catch:{ all -> 0x0055 }
            android.net.wifi.p2p.WifiP2pManager$Channel r1 = r3.wifiP2pChannel     // Catch:{ all -> 0x0055 }
            r0.cancelConnect(r1, r2)     // Catch:{ all -> 0x0055 }
            android.content.Context r0 = r3.context     // Catch:{ IllegalArgumentException -> 0x0045 }
            com.qualcomm.robotcore.wifi.WifiDirectAssistant$WifiP2pBroadcastReceiver r1 = r3.receiver     // Catch:{ IllegalArgumentException -> 0x0045 }
            r0.unregisterReceiver(r1)     // Catch:{ IllegalArgumentException -> 0x0045 }
        L_0x0045:
            r3.lastEvent = r2     // Catch:{ all -> 0x0055 }
            java.lang.Object r0 = r3.connectStatusLock     // Catch:{ all -> 0x0055 }
            monitor-enter(r0)     // Catch:{ all -> 0x0055 }
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r1 = com.qualcomm.robotcore.wifi.NetworkConnection.ConnectStatus.NOT_CONNECTED     // Catch:{ all -> 0x0050 }
            r3.connectStatus = r1     // Catch:{ all -> 0x0050 }
            monitor-exit(r0)     // Catch:{ all -> 0x0050 }
            goto L_0x0053
        L_0x0050:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0050 }
            throw r1     // Catch:{ all -> 0x0055 }
        L_0x0053:
            monitor-exit(r3)
            return
        L_0x0055:
            r0 = move-exception
            monitor-exit(r3)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.wifi.WifiDirectAssistant.disable():void");
    }

    public void discoverPotentialConnections() {
        discoverPeers();
    }

    public void createConnection() {
        createGroup();
    }

    public void cancelPotentialConnections() {
        cancelDiscoverPeers();
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        if (isEnabled()) {
            sb.append("Name: ");
            sb.append(getDeviceName());
            if (isGroupOwner()) {
                sb.append("\nIP Address: ");
                sb.append(getGroupOwnerAddress().getHostAddress());
                sb.append("\nPassphrase: ");
                sb.append(getPassphrase());
                sb.append("\nGroup Owner");
            } else if (isConnected()) {
                sb.append("\nGroup Owner: ");
                sb.append(getGroupOwnerName());
                sb.append("\nConnected");
            } else {
                sb.append("\nNo connection information");
            }
        }
        return sb.toString();
    }

    public synchronized boolean isEnabled() {
        return this.clients > 0;
    }

    public NetworkConnection.ConnectStatus getConnectStatus() {
        NetworkConnection.ConnectStatus connectStatus2;
        synchronized (this.connectStatusLock) {
            connectStatus2 = this.connectStatus;
        }
        return connectStatus2;
    }

    public List<WifiP2pDevice> getPeers() {
        return new ArrayList(this.peers);
    }

    public String getDeviceMacAddress() {
        return this.deviceMacAddress;
    }

    public String getDeviceName() {
        return this.deviceName;
    }

    public InetAddress getConnectionOwnerAddress() {
        return getGroupOwnerAddress();
    }

    public InetAddress getGroupOwnerAddress() {
        InetAddress inetAddress;
        synchronized (this.groupOwnerLock) {
            inetAddress = this.groupOwnerAddress;
        }
        return inetAddress;
    }

    public String getConnectionOwnerMacAddress() {
        return getGroupOwnerMacAddress();
    }

    private String getGroupOwnerMacAddress() {
        return this.groupOwnerMacAddress;
    }

    public String getConnectionOwnerName() {
        return getGroupOwnerName();
    }

    public String getGroupOwnerName() {
        return this.groupOwnerName;
    }

    public String getPassphrase() {
        return this.passphrase;
    }

    public String getGroupInterface() {
        return this.groupInterface;
    }

    public String getGroupNetworkName() {
        return this.groupNetworkName;
    }

    public boolean isWifiP2pEnabled() {
        return this.isWifiP2pEnabled;
    }

    public boolean isConnected() {
        boolean z;
        synchronized (this.connectStatusLock) {
            if (this.connectStatus != NetworkConnection.ConnectStatus.CONNECTED) {
                if (this.connectStatus != NetworkConnection.ConnectStatus.GROUP_OWNER) {
                    z = false;
                }
            }
            z = true;
        }
        return z;
    }

    public boolean isGroupOwner() {
        boolean z;
        synchronized (this.connectStatusLock) {
            z = this.connectStatus == NetworkConnection.ConnectStatus.GROUP_OWNER;
        }
        return z;
    }

    public void discoverPeers() {
        if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") != -1) {
            this.wifiP2pManager.discoverPeers(this.wifiP2pChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.DISCOVERING_PEERS);
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "discovering peers");
                }

                public void onFailure(int i) {
                    String failureReasonToString = WifiDirectAssistant.failureReasonToString(i);
                    int unused = WifiDirectAssistant.this.failureReason = i;
                    RobotLog.m66ww(WifiDirectAssistant.TAG, "Wi-Fi Direct failure while trying to discover peers - reason: " + failureReasonToString);
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.ERROR);
                }
            });
            return;
        }
        throw new RuntimeException("We do NOT have permission to access fine location");
    }

    public void cancelDiscoverPeers() {
        RobotLog.m42dd(TAG, "stop discovering peers");
        this.wifiP2pManager.stopPeerDiscovery(this.wifiP2pChannel, (WifiP2pManager.ActionListener) null);
    }

    public void createGroup() {
        if (ContextCompat.checkSelfPermission(AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") != -1) {
            this.wifiP2pManager.createGroup(this.wifiP2pChannel, new WifiP2pManager.ActionListener() {
                public void onSuccess() {
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.GROUP_CREATED);
                    RobotLog.m42dd(WifiDirectAssistant.TAG, "created group");
                }

                public void onFailure(int i) {
                    if (i == 2) {
                        RobotLog.m42dd(WifiDirectAssistant.TAG, "cannot create group, does group already exist?");
                        return;
                    }
                    String failureReasonToString = WifiDirectAssistant.failureReasonToString(i);
                    int unused = WifiDirectAssistant.this.failureReason = i;
                    RobotLog.m66ww(WifiDirectAssistant.TAG, "Wi-Fi Direct failure while trying to create group - reason: " + failureReasonToString);
                    synchronized (WifiDirectAssistant.this.connectStatusLock) {
                        NetworkConnection.ConnectStatus unused2 = WifiDirectAssistant.this.connectStatus = NetworkConnection.ConnectStatus.ERROR;
                    }
                    WifiDirectAssistant.this.sendEvent(NetworkConnection.NetworkEvent.ERROR);
                }
            });
            return;
        }
        throw new RuntimeException("We do NOT have permission to access fine location");
    }

    public void removeGroup() {
        this.wifiP2pManager.removeGroup(this.wifiP2pChannel, (WifiP2pManager.ActionListener) null);
    }

    public void connect(String str, String str2) {
        throw new UnsupportedOperationException("This method is not supported for this class");
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x002b, code lost:
        r0 = new android.net.wifi.p2p.WifiP2pConfig();
        r0.deviceAddress = r5;
        r0.wps.setup = 0;
        r0.groupOwnerIntent = 1;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x0045, code lost:
        if (androidx.core.content.ContextCompat.checkSelfPermission(org.firstinspires.ftc.robotcore.internal.system.AppUtil.getDefContext(), "android.permission.ACCESS_FINE_LOCATION") == -1) goto L_0x0054;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:12:0x0047, code lost:
        r4.wifiP2pManager.connect(r4.wifiP2pChannel, r0, new com.qualcomm.robotcore.wifi.WifiDirectAssistant.C07804(r4));
     */
    /* JADX WARNING: Code restructure failed: missing block: B:13:0x0053, code lost:
        return;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:15:0x005b, code lost:
        throw new java.lang.RuntimeException("We do NOT have permission to access fine location");
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void connect(java.lang.String r5) {
        /*
            r4 = this;
            java.lang.Object r0 = r4.connectStatusLock
            monitor-enter(r0)
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r1 = r4.connectStatus     // Catch:{ all -> 0x0079 }
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r2 = com.qualcomm.robotcore.wifi.NetworkConnection.ConnectStatus.CONNECTING     // Catch:{ all -> 0x0079 }
            if (r1 == r2) goto L_0x005c
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r1 = r4.connectStatus     // Catch:{ all -> 0x0079 }
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r2 = com.qualcomm.robotcore.wifi.NetworkConnection.ConnectStatus.CONNECTED     // Catch:{ all -> 0x0079 }
            if (r1 != r2) goto L_0x0010
            goto L_0x005c
        L_0x0010:
            java.lang.String r1 = "WifiDirect"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0079 }
            r2.<init>()     // Catch:{ all -> 0x0079 }
            java.lang.String r3 = "connecting to "
            r2.append(r3)     // Catch:{ all -> 0x0079 }
            r2.append(r5)     // Catch:{ all -> 0x0079 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0079 }
            com.qualcomm.robotcore.util.RobotLog.m42dd(r1, r2)     // Catch:{ all -> 0x0079 }
            com.qualcomm.robotcore.wifi.NetworkConnection$ConnectStatus r1 = com.qualcomm.robotcore.wifi.NetworkConnection.ConnectStatus.CONNECTING     // Catch:{ all -> 0x0079 }
            r4.connectStatus = r1     // Catch:{ all -> 0x0079 }
            monitor-exit(r0)     // Catch:{ all -> 0x0079 }
            android.net.wifi.p2p.WifiP2pConfig r0 = new android.net.wifi.p2p.WifiP2pConfig
            r0.<init>()
            r0.deviceAddress = r5
            android.net.wifi.WpsInfo r5 = r0.wps
            r1 = 0
            r5.setup = r1
            r5 = 1
            r0.groupOwnerIntent = r5
            android.app.Application r5 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getDefContext()
            java.lang.String r1 = "android.permission.ACCESS_FINE_LOCATION"
            int r5 = androidx.core.content.ContextCompat.checkSelfPermission(r5, r1)
            r1 = -1
            if (r5 == r1) goto L_0x0054
            android.net.wifi.p2p.WifiP2pManager r5 = r4.wifiP2pManager
            android.net.wifi.p2p.WifiP2pManager$Channel r1 = r4.wifiP2pChannel
            com.qualcomm.robotcore.wifi.WifiDirectAssistant$4 r2 = new com.qualcomm.robotcore.wifi.WifiDirectAssistant$4
            r2.<init>()
            r5.connect(r1, r0, r2)
            return
        L_0x0054:
            java.lang.RuntimeException r5 = new java.lang.RuntimeException
            java.lang.String r0 = "We do NOT have permission to access fine location"
            r5.<init>(r0)
            throw r5
        L_0x005c:
            java.lang.String r1 = "WifiDirect"
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0079 }
            r2.<init>()     // Catch:{ all -> 0x0079 }
            java.lang.String r3 = "connection request to "
            r2.append(r3)     // Catch:{ all -> 0x0079 }
            r2.append(r5)     // Catch:{ all -> 0x0079 }
            java.lang.String r5 = " ignored, already connected"
            r2.append(r5)     // Catch:{ all -> 0x0079 }
            java.lang.String r5 = r2.toString()     // Catch:{ all -> 0x0079 }
            com.qualcomm.robotcore.util.RobotLog.m42dd(r1, r5)     // Catch:{ all -> 0x0079 }
            monitor-exit(r0)     // Catch:{ all -> 0x0079 }
            return
        L_0x0079:
            r5 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x0079 }
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.wifi.WifiDirectAssistant.connect(java.lang.String):void");
    }

    /* access modifiers changed from: private */
    public void updateLocalDeviceInfo(WifiP2pDevice wifiP2pDevice) {
        this.deviceName = wifiP2pDevice.deviceName;
        this.deviceMacAddress = wifiP2pDevice.deviceAddress;
        RobotLog.m60vv(TAG, "device information: " + this.deviceName + " " + this.deviceMacAddress);
    }

    public String getFailureReason() {
        return failureReasonToString(this.failureReason);
    }

    public static String failureReasonToString(int i) {
        if (i == 0) {
            return "ERROR";
        }
        if (i == 1) {
            return "P2P_UNSUPPORTED";
        }
        if (i == 2) {
            return "BUSY";
        }
        return "UNKNOWN (reason " + i + ")";
    }

    /* access modifiers changed from: protected */
    public void sendEvent(NetworkConnection.NetworkEvent networkEvent) {
        NetworkConnection.NetworkEvent networkEvent2 = this.lastEvent;
        if (networkEvent2 != networkEvent || networkEvent2 == NetworkConnection.NetworkEvent.PEERS_AVAILABLE) {
            this.lastEvent = networkEvent;
            synchronized (this.callbackLock) {
                if (this.callback != null) {
                    this.callback.onNetworkConnectionEvent(networkEvent);
                }
            }
        }
    }

    public void setNetworkSettings(String str, String str2, ApChannel apChannel) throws InvalidNetworkSettingException {
        if (str != null) {
            DeviceNameManagerFactory.getInstance().setDeviceName(str, true);
        }
        if (apChannel != null) {
            ApChannelManagerFactory.getInstance().setChannel(apChannel, true);
        }
    }
}
