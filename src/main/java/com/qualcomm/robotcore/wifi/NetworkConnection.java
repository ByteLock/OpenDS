package com.qualcomm.robotcore.wifi;

import android.content.Context;
import android.net.wifi.WifiManager;
import com.qualcomm.robotcore.util.RobotLog;
import java.net.InetAddress;
import org.firstinspires.ftc.robotcore.internal.network.ApChannel;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.InvalidNetworkSettingException;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public abstract class NetworkConnection {
    private static final int GHZ_24_BASE_FREQ = 2407;
    private static final int GHZ_50_BASE_FREQ = 5000;
    protected NetworkConnectionCallback callback = null;
    protected final Object callbackLock = new Object();
    protected Context context;
    protected NetworkEvent lastEvent = null;
    protected final WifiManager wifiManager = ((WifiManager) AppUtil.getDefContext().getSystemService("wifi"));

    public enum ConnectStatus {
        NOT_CONNECTED,
        CONNECTING,
        CONNECTED,
        GROUP_OWNER,
        ERROR
    }

    public interface NetworkConnectionCallback {
        CallbackResult onNetworkConnectionEvent(NetworkEvent networkEvent);
    }

    public enum NetworkEvent {
        DISCOVERING_PEERS,
        PEERS_AVAILABLE,
        GROUP_CREATED,
        CONNECTING,
        CONNECTED_AS_PEER,
        CONNECTED_AS_GROUP_OWNER,
        DISCONNECTED,
        CONNECTION_INFO_AVAILABLE,
        AP_CREATED,
        ERROR,
        UNKNOWN
    }

    public abstract void cancelPotentialConnections();

    public abstract void connect(String str);

    public abstract void connect(String str, String str2);

    public abstract void createConnection();

    public abstract void detectWifiReset();

    public abstract void disable();

    public abstract void discoverPotentialConnections();

    public abstract void enable();

    public abstract ConnectStatus getConnectStatus();

    public abstract InetAddress getConnectionOwnerAddress();

    public abstract String getConnectionOwnerMacAddress();

    public abstract String getConnectionOwnerName();

    public abstract String getDeviceName();

    public abstract String getFailureReason();

    public abstract String getInfo();

    public abstract NetworkType getNetworkType();

    public abstract String getPassphrase();

    public abstract boolean isConnected();

    public abstract void onWaitForConnection();

    public abstract void setNetworkSettings(String str, String str2, ApChannel apChannel) throws InvalidNetworkSettingException;

    public NetworkConnection(Context context2) {
        this.context = context2 == null ? AppUtil.getDefContext() : context2;
    }

    public static boolean isDeviceNameValid(String str) {
        return str.matches("^\\p{Print}+$");
    }

    public void setCallback(NetworkConnectionCallback networkConnectionCallback) {
        synchronized (this.callbackLock) {
            this.callback = networkConnectionCallback;
        }
    }

    public NetworkConnectionCallback getCallback() {
        NetworkConnectionCallback networkConnectionCallback;
        synchronized (this.callbackLock) {
            networkConnectionCallback = this.callback;
        }
        return networkConnectionCallback;
    }

    public int getWifiChannel() {
        int frequency = this.wifiManager.getConnectionInfo().getFrequency();
        if (frequency >= 5000) {
            return (frequency - 5000) / 5;
        }
        if (frequency >= GHZ_24_BASE_FREQ) {
            return (frequency - GHZ_24_BASE_FREQ) / 5;
        }
        return 0;
    }

    /* access modifiers changed from: protected */
    public void sendEvent(NetworkEvent networkEvent) {
        if (this.lastEvent == networkEvent) {
            RobotLog.m52i("Dropping duplicate network event " + networkEvent.toString());
            return;
        }
        this.lastEvent = networkEvent;
        synchronized (this.callbackLock) {
            NetworkConnectionCallback networkConnectionCallback = this.callback;
            if (networkConnectionCallback != null) {
                networkConnectionCallback.onNetworkConnectionEvent(networkEvent);
            }
        }
    }
}
