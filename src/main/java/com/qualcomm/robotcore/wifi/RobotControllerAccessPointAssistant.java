package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;

import java.lang.reflect.InvocationTargetException;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.network.ApChannel;
import org.firstinspires.ftc.robotcore.network.ApChannelManager;
import org.firstinspires.ftc.robotcore.network.ApChannelManagerFactory;
import org.firstinspires.ftc.robotcore.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.network.InvalidNetworkSettingException;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PasswordManager;
import org.firstinspires.ftc.robotcore.network.PasswordManagerFactory;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class RobotControllerAccessPointAssistant extends AccessPointAssistant {
    private static final String TAG = "RobotControllerAccessPointAssistant";
    public static final int WIFI_AP_STATE_DISABLED = 11;
    public static final int WIFI_AP_STATE_ENABLED = 13;
    private static RobotControllerAccessPointAssistant robotControllerAccessPointAssistant;
    private ApChannelManager apChannelManager = ApChannelManagerFactory.getInstance();
    private NetworkConnection.ConnectStatus connectStatus;
    private final Object enableDisableLock = new Object();
    private IntentFilter intentFilter;
    private DeviceNameManager nameManager = DeviceNameManagerFactory.getInstance();
    private PasswordManager passwordManager = PasswordManagerFactory.getInstance();
    private BroadcastReceiver receiver;

    public void cancelPotentialConnections() {
    }

    public void discoverPotentialConnections() {
    }

    private RobotControllerAccessPointAssistant(Context context) {
        super(context);
        IntentFilter intentFilter2 = new IntentFilter();
        this.intentFilter = intentFilter2;
        intentFilter2.addAction(Intents.ANDROID_ACTION_WIFI_AP_STATE_CHANGED);
        this.intentFilter.addAction(Intents.ACTION_FTC_WIFI_FACTORY_RESET);
        this.intentFilter.addAction(Intents.ACTION_FTC_AP_NOTIFY_BAND_CHANGE);
    }

    public static synchronized RobotControllerAccessPointAssistant getRobotControllerAccessPointAssistant(Context context) {
        RobotControllerAccessPointAssistant robotControllerAccessPointAssistant2;
        synchronized (RobotControllerAccessPointAssistant.class) {
            if (robotControllerAccessPointAssistant == null) {
                robotControllerAccessPointAssistant = new RobotControllerAccessPointAssistant(context);
            }
            robotControllerAccessPointAssistant2 = robotControllerAccessPointAssistant;
        }
        return robotControllerAccessPointAssistant2;
    }

    /* access modifiers changed from: private */
    public void handleWifiStateChange(Intent intent) {
        int intExtra = intent.getIntExtra("wifi_state", 0);
        RobotLog.m54ii(TAG, "Wi-Fi state change:, wifiApState: " + intExtra);
        if (intExtra == 11) {
            this.connectStatus = NetworkConnection.ConnectStatus.NOT_CONNECTED;
            sendEvent(NetworkConnection.NetworkEvent.DISCONNECTED);
        } else if (intExtra == 13) {
            this.connectStatus = NetworkConnection.ConnectStatus.CONNECTED;
            sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
        }
    }

    /* access modifiers changed from: private */
    public void handleFactoryReset() {
        RobotLog.m66ww(TAG, "Received request to do access point factory reset");
        AppUtil.getInstance().showToast(UILocation.BOTH, "Resetting access point to default name and password", 1);
        NetworkConnectionHandler.getInstance().injectReceivedCommand(new Command(RobotCoreCommandList.CMD_VISUALLY_CONFIRM_WIFI_RESET));
        try {
            Thread.sleep(400);
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        try {
            setNetworkSettings(this.nameManager.resetDeviceName(false), this.passwordManager.resetPassword(false), this.apChannelManager.resetChannel(false));
        } catch (InvalidNetworkSettingException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Default name, password, or channel rejected during reset attempt");
        }
    }

    /* access modifiers changed from: private */
    public void handleBandChangeViaButton(Intent intent) {
        int intExtra = intent.getIntExtra(Intents.EXTRA_AP_BAND, -1);
        if (intExtra == 0) {
            RobotLog.m54ii(TAG, "Received notification that the band has been switched to 2.4 GHz");
        } else if (intExtra == 1) {
            RobotLog.m54ii(TAG, "Received notification that the band has been switched to 5 GHz");
        } else {
            RobotLog.m66ww(TAG, "Received band switch notification with invalid band " + intExtra);
        }
        NetworkConnectionHandler.getInstance().injectReceivedCommand(new Command(RobotCoreCommandList.CMD_VISUALLY_CONFIRM_WIFI_BAND_SWITCH, Integer.toString(intExtra)));
    }

    public NetworkType getNetworkType() {
        return NetworkType.RCWIRELESSAP;
    }

    public void enable() {
        synchronized (this.enableDisableLock) {
            if (this.receiver == null) {
                RobotLog.m54ii(TAG, "Enabling network services");
                this.receiver = new BroadcastReceiver() {
                    public void onReceive(Context context, Intent intent) {
                        if (intent.getAction().equals(Intents.ANDROID_ACTION_WIFI_AP_STATE_CHANGED)) {
                            RobotControllerAccessPointAssistant.this.handleWifiStateChange(intent);
                        } else if (intent.getAction().equals(Intents.ACTION_FTC_WIFI_FACTORY_RESET)) {
                            RobotControllerAccessPointAssistant.this.handleFactoryReset();
                        } else if (intent.getAction().equals(Intents.ACTION_FTC_AP_NOTIFY_BAND_CHANGE)) {
                            RobotControllerAccessPointAssistant.this.handleBandChangeViaButton(intent);
                        }
                    }
                };
                this.context.registerReceiver(this.receiver, this.intentFilter);
            }
        }
    }

    public void disable() {
        synchronized (this.enableDisableLock) {
            if (this.receiver != null) {
                this.context.unregisterReceiver(this.receiver);
                this.receiver = null;
            }
        }
    }

    public boolean isConnected() {
        return getConnectStatus() == NetworkConnection.ConnectStatus.CONNECTED;
    }

    /* access modifiers changed from: protected */
    public boolean isWifiApEnabled() {
        try {
            return ((Boolean) this.wifiManager.getClass().getMethod("isWifiApEnabled", new Class[0]).invoke(this.wifiManager, new Object[0])).booleanValue();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            e.printStackTrace();
            return false;
        }
    }

    public NetworkConnection.ConnectStatus getConnectStatus() {
        if (isWifiApEnabled()) {
            RobotLog.m54ii(TAG, "Wi-Fi AP is enabled");
            return NetworkConnection.ConnectStatus.CONNECTED;
        }
        RobotLog.m54ii(TAG, "Wi-Fi AP is not enabled");
        return NetworkConnection.ConnectStatus.NOT_CONNECTED;
    }

    /* access modifiers changed from: protected */
    public String getIpAddress() {
        return getConnectionOwnerAddress().getHostAddress();
    }

    public void createConnection() {
        RobotLog.m54ii(TAG, "Sending SSID and password to AP service");
        try {
            setNetworkSettings(this.nameManager.getDeviceName(), this.passwordManager.getPassword(), (ApChannel) null);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Currently stored name or password is now being rejected");
        }
    }

    public void detectWifiReset() {
        RobotLog.m43dd(TAG, "detectWifiReset button=%b", Boolean.valueOf(AndroidBoard.getInstance().getUserButtonPin().getState()));
        if (LynxConstants.isRevControlHub() && AndroidBoard.getInstance().getUserButtonPin().getState()) {
            RobotLog.m54ii(TAG, "Wi-Fi settings reset requested through the Control Hub button");
            this.context.sendBroadcast(new Intent(Intents.ACTION_FTC_WIFI_FACTORY_RESET));
        }
    }

    public String getConnectionOwnerName() {
        return this.nameManager.getDeviceName();
    }

    public String getPassphrase() {
        return this.passwordManager.getPassword();
    }

    public void onWaitForConnection() {
        createConnection();
    }

    public void setNetworkSettings(String str, String str2, ApChannel apChannel) throws InvalidNetworkSettingException {
        boolean z = !AndroidBoard.getInstance().supportsBulkNetworkSettings();
        RobotLog.m43dd(TAG, "setNetworkProperties(deviceName=%s, password=%s, ApChannel=%s) sendSettingsIndividually=%b", str, str2, apChannel, Boolean.valueOf(z));
        if (str != null) {
            this.nameManager.setDeviceName(str, z);
        }
        if (str2 != null) {
            this.passwordManager.setPassword(str2, z);
        }
        if (apChannel != null) {
            this.apChannelManager.setChannel(apChannel, z);
        }
        if (!z) {
            Intent intent = new Intent(Intents.ACTION_FTC_AP_SETTINGS_CHANGE);
            intent.putExtra(Intents.EXTRA_AP_NAME, str);
            intent.putExtra(Intents.EXTRA_AP_PASSWORD, str2);
            if (!(apChannel == null || apChannel == ApChannel.UNKNOWN)) {
                intent.putExtra(Intents.EXTRA_AP_BAND, apChannel.band.androidInternalValue);
                intent.putExtra(Intents.EXTRA_AP_CHANNEL, apChannel.channelNum);
            }
            RobotLog.m42dd(TAG, "Sending bulk settings broadcast intent");
            AppUtil.getDefContext().sendBroadcast(intent);
        }
    }
}
