package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.ApChannel;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.WifiUtil;
import org.firstinspires.ftc.robotcore.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class DriverStationAccessPointAssistant extends AccessPointAssistant {
    private static final boolean DEBUG = false;
    private static final int DEFAULT_SECONDS_BETWEEN_WIFI_SCANS = 15;
    private static final String TAG = "DSAccessPointAssistant";
    /* access modifiers changed from: private */
    public static final Object listenersLock = new Object();
    private static DriverStationAccessPointAssistant wirelessAPAssistant;
    private volatile NetworkConnection.ConnectStatus connectStatus;
    private ConnectivityManager connectivityManager;
    protected volatile boolean doContinuousScans;
    private final Object enableDisableLock = new Object();
    /* access modifiers changed from: private */
    public ArrayList<ConnectedNetworkHealthListener> healthListeners = new ArrayList<>();
    private IntentFilter intentFilter;
    private NetworkHealthPollerThread networkHealthPollerThread;
    private BroadcastReceiver receiver;
    protected ScheduledFuture<?> resetTimeBetweenWiFiScansFuture;
    protected final Object resetTimeBetweenWiFiScansFutureLock = new Object();
    private final List<ScanResult> scanResults = new ArrayList();
    private volatile int secondsBetweenWifiScans = 15;
    protected final WiFiScanRunnable wiFiScanRunnable = new WiFiScanRunnable();
    private ConnectivityManager.NetworkCallback wifiNetworkCallback;
    protected ScheduledFuture<?> wifiScanFuture;
    protected final Object wifiScanFutureLock = new Object();

    public interface ConnectedNetworkHealthListener {
        void onNetworkHealthUpdate(int i, int i2);
    }

    private DriverStationAccessPointAssistant(Context context) {
        super(context);
        this.connectivityManager = (ConnectivityManager) context.getSystemService("connectivity");
        this.connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
        this.doContinuousScans = false;
        IntentFilter intentFilter2 = new IntentFilter();
        this.intentFilter = intentFilter2;
        intentFilter2.addAction("android.net.wifi.SCAN_RESULTS");
    }

    public static synchronized DriverStationAccessPointAssistant getDriverStationAccessPointAssistant(Context context) {
        DriverStationAccessPointAssistant driverStationAccessPointAssistant;
        synchronized (DriverStationAccessPointAssistant.class) {
            if (wirelessAPAssistant == null) {
                wirelessAPAssistant = new DriverStationAccessPointAssistant(context);
            }
            driverStationAccessPointAssistant = wirelessAPAssistant;
        }
        return driverStationAccessPointAssistant;
    }

    public void enable() {
        synchronized (this.enableDisableLock) {
            if (this.receiver == null) {
                this.receiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        if ("android.net.wifi.SCAN_RESULTS".equals(intent.getAction())) {
                            DriverStationAccessPointAssistant.this.handleScanResultsAvailable(intent);
                        }
                    }
                };
                this.context.registerReceiver(this.receiver, this.intentFilter);
            }
            if (this.wifiNetworkCallback == null) {
                try {
                    this.wifiNetworkCallback = new ConnectivityManager.NetworkCallback() {
                        public void onAvailable(Network network) {
                            DriverStationAccessPointAssistant.this.onWiFiNetworkConnected(network);
                        }

                        public void onLost(Network network) {
                            DriverStationAccessPointAssistant.this.onWiFiNetworkDisconnected(network);
                        }

                        public void onUnavailable() {
                            RobotLog.m48ee(DriverStationAccessPointAssistant.TAG, "connectivityManager.requestNetwork() failed");
                        }
                    };
                    this.connectivityManager.requestNetwork(new NetworkRequest.Builder().addTransportType(1).build(), this.wifiNetworkCallback);
                } catch (RuntimeException e) {
                    this.wifiNetworkCallback = null;
                    throw e;
                }
            }
        }
    }

    public void disable() {
        synchronized (this.enableDisableLock) {
            if (this.receiver != null) {
                this.context.unregisterReceiver(this.receiver);
                this.receiver = null;
            }
            if (this.wifiNetworkCallback != null) {
                this.connectivityManager.bindProcessToNetwork((Network) null);
                try {
                    this.connectivityManager.unregisterNetworkCallback(this.wifiNetworkCallback);
                } catch (RuntimeException unused) {
                    RobotLog.m66ww(TAG, "Unable to unregister network callback (it may have never been registered)");
                }
                this.wifiNetworkCallback = null;
            }
        }
    }

    public String getConnectionOwnerName() {
        return WifiUtil.getConnectedSsid();
    }

    public void setNetworkSettings(String str, String str2, ApChannel apChannel) {
        RobotLog.m48ee(TAG, "setNetworkProperties not supported on Driver Station");
    }

    public void discoverPotentialConnections() {
        RobotLog.m60vv(TAG, "Starting scans for the most recently connected Control Hub Wi-Fi network");
        this.doContinuousScans = true;
        synchronized (this.wifiScanFutureLock) {
            ScheduledFuture<?> scheduledFuture = this.wifiScanFuture;
            if (scheduledFuture == null || scheduledFuture.isDone() || this.wifiScanFuture.isCancelled()) {
                this.wifiScanFuture = ThreadPool.getDefaultScheduler().schedule(this.wiFiScanRunnable, 1000, TimeUnit.MILLISECONDS);
            }
        }
    }

    public void cancelPotentialConnections() {
        RobotLog.m60vv(TAG, "Stopping scans for the most recently connected Control Hub Wi-Fi network");
        this.doContinuousScans = false;
        resetSecondsBetweenWiFiScansToDefault();
    }

    /* access modifiers changed from: protected */
    public boolean lookForKnownAccessPoint(String str, String str2, List<ScanResult> list) {
        if (str != null && str2 != null) {
            Iterator<ScanResult> it = list.iterator();
            while (true) {
                if (!it.hasNext()) {
                    break;
                }
                ScanResult next = it.next();
                if (next.SSID.equals(str) && next.BSSID.equals(str2)) {
                    if (connectToAccessPoint(next.SSID)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public void handleScanResultsAvailable(Intent intent) {
        PreferencesHelper preferencesHelper = new PreferencesHelper(TAG, this.context);
        String str = (String) preferencesHelper.readPref(this.context.getString(C0705R.string.pref_last_known_ssid));
        String str2 = (String) preferencesHelper.readPref(this.context.getString(C0705R.string.pref_last_known_macaddr));
        this.scanResults.clear();
        this.scanResults.addAll(this.wifiManager.getScanResults());
        if (!this.doContinuousScans) {
            return;
        }
        if (!lookForKnownAccessPoint(str, str2, this.scanResults)) {
            synchronized (this.wifiScanFutureLock) {
                ScheduledFuture<?> scheduledFuture = this.wifiScanFuture;
                if (scheduledFuture == null || scheduledFuture.isDone() || this.wifiScanFuture.isCancelled()) {
                    this.wifiScanFuture = ThreadPool.getDefaultScheduler().schedule(this.wiFiScanRunnable, (long) this.secondsBetweenWifiScans, TimeUnit.SECONDS);
                }
            }
            return;
        }
        cancelPotentialConnections();
    }

    public void registerNetworkHealthListener(ConnectedNetworkHealthListener connectedNetworkHealthListener) {
        synchronized (listenersLock) {
            this.healthListeners.add(connectedNetworkHealthListener);
        }
    }

    public void unregisterNetworkHealthListener(ConnectedNetworkHealthListener connectedNetworkHealthListener) {
        synchronized (listenersLock) {
            this.healthListeners.remove(connectedNetworkHealthListener);
        }
    }

    public void temporarilySetSecondsBetweenWifiScans(int i, int i2) {
        RobotLog.m43dd(TAG, "Setting the number of seconds between Wi-Fi scans to %d. This will reset to %d seconds after %d seconds.", Integer.valueOf(i), 15, Integer.valueOf(i2));
        synchronized (this.resetTimeBetweenWiFiScansFutureLock) {
            ScheduledFuture<?> scheduledFuture = this.resetTimeBetweenWiFiScansFuture;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
            }
            this.resetTimeBetweenWiFiScansFuture = ThreadPool.getDefaultScheduler().schedule(new Runnable() {
                public void run() {
                    DriverStationAccessPointAssistant.this.resetSecondsBetweenWiFiScansToDefault();
                }
            }, (long) i2, TimeUnit.SECONDS);
        }
        this.secondsBetweenWifiScans = i;
    }

    public void resetSecondsBetweenWiFiScansToDefault() {
        if (this.secondsBetweenWifiScans != 15) {
            RobotLog.m43dd(TAG, "Resetting time between Wi-Fi scans to the default value of %d seconds", 15);
            this.secondsBetweenWifiScans = 15;
        }
        synchronized (this.resetTimeBetweenWiFiScansFutureLock) {
            ScheduledFuture<?> scheduledFuture = this.resetTimeBetweenWiFiScansFuture;
            if (scheduledFuture != null) {
                scheduledFuture.cancel(false);
                this.resetTimeBetweenWiFiScansFuture = null;
            }
        }
    }

    public class NetworkHealthPollerThread extends Thread {
        public NetworkHealthPollerThread() {
        }

        public void run() {
            while (!Thread.currentThread().isInterrupted()) {
                WifiInfo connectionInfo = DriverStationAccessPointAssistant.this.wifiManager.getConnectionInfo();
                int rssi = connectionInfo.getRssi();
                int linkSpeed = connectionInfo.getLinkSpeed();
                synchronized (DriverStationAccessPointAssistant.listenersLock) {
                    Iterator it = DriverStationAccessPointAssistant.this.healthListeners.iterator();
                    while (it.hasNext()) {
                        ((ConnectedNetworkHealthListener) it.next()).onNetworkHealthUpdate(rssi, linkSpeed);
                    }
                }
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                }
            }
        }
    }

    private void startHealthPoller() {
        NetworkHealthPollerThread networkHealthPollerThread2 = this.networkHealthPollerThread;
        if (networkHealthPollerThread2 == null || networkHealthPollerThread2.isInterrupted()) {
            NetworkHealthPollerThread networkHealthPollerThread3 = new NetworkHealthPollerThread();
            this.networkHealthPollerThread = networkHealthPollerThread3;
            networkHealthPollerThread3.start();
        }
    }

    private void killHealthPoller() {
        NetworkHealthPollerThread networkHealthPollerThread2 = this.networkHealthPollerThread;
        if (networkHealthPollerThread2 != null) {
            networkHealthPollerThread2.interrupt();
            this.networkHealthPollerThread = null;
        }
    }

    /* access modifiers changed from: protected */
    public void onWiFiNetworkConnected(Network network) {
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        RobotLog.m54ii(TAG, "onWiFiConnected() called. wifiInfo: " + connectionInfo);
        RobotLog.m60vv(TAG, "Binding app to new Wi-Fi network.\nNOTE: Communication that can be transmitted over a different network (such as Ethernet)should use sockets explicitly bound to a different android.net.Network instance.");
        this.connectivityManager.bindProcessToNetwork(network);
        if (this.connectStatus == NetworkConnection.ConnectStatus.NOT_CONNECTED) {
            startHealthPoller();
            this.connectStatus = NetworkConnection.ConnectStatus.CONNECTED;
            saveConnectionInfo(connectionInfo);
        }
        sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
    }

    /* access modifiers changed from: protected */
    public void onWiFiNetworkDisconnected(Network network) {
        RobotLog.m54ii(TAG, "onWiFiDisconnected() called. Unbinding app.");
        this.connectivityManager.bindProcessToNetwork((Network) null);
        if (this.connectStatus == NetworkConnection.ConnectStatus.CONNECTED) {
            this.connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
            handleWifiDisconnect();
            killHealthPoller();
        }
    }

    /* access modifiers changed from: protected */
    public boolean connectToAccessPoint(String str) {
        RobotLog.m60vv(TAG, "Attempting to auto-connect to " + str);
        List<WifiConfiguration> configuredNetworks = this.wifiManager.getConfiguredNetworks();
        if (configuredNetworks == null) {
            RobotLog.m48ee(TAG, "Wi-Fi is likely off");
            return false;
        }
        Iterator<WifiConfiguration> it = configuredNetworks.iterator();
        while (true) {
            if (!it.hasNext()) {
                break;
            }
            WifiConfiguration next = it.next();
            if (next.SSID != null) {
                String str2 = next.SSID;
                if (str2.equals("\"" + str + "\"")) {
                    if (!this.wifiManager.enableNetwork(next.networkId, true)) {
                        RobotLog.m66ww(TAG, "Could not enable " + str);
                        return false;
                    } else if (!this.wifiManager.reconnect()) {
                        RobotLog.m66ww(TAG, "Could not reconnect to " + str);
                        return false;
                    }
                }
            }
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public String getIpAddress() {
        return getIpAddressAsString(this.wifiManager.getConnectionInfo().getIpAddress());
    }

    private void handleWifiDisconnect() {
        RobotLog.m60vv(TAG, "Handling Wi-Fi disconnect");
        this.connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
        sendEvent(NetworkConnection.NetworkEvent.DISCONNECTED);
        NetworkConnectionHandler.getInstance().shutdown();
    }

    private void saveConnectionInfo(WifiInfo wifiInfo) {
        String replace = wifiInfo.getSSID().replace("\"", InspectionState.NO_VERSION);
        String bssid = wifiInfo.getBSSID();
        PreferencesHelper preferencesHelper = new PreferencesHelper(TAG, this.context);
        preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_last_known_ssid), replace);
        preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_last_known_macaddr), bssid);
    }

    private static String getIpAddressAsString(int i) {
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255)});
    }

    private class WiFiScanRunnable implements Runnable {
        private WiFiScanRunnable() {
        }

        public void run() {
            if (DriverStationAccessPointAssistant.this.doContinuousScans) {
                DriverStationAccessPointAssistant.this.wifiManager.startScan();
            }
        }
    }
}
