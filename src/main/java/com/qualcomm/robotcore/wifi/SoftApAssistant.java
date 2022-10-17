package com.qualcomm.robotcore.wifi;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.ScanResult;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import com.qualcomm.robotcore.util.ReadWriteFile;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import java.io.File;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.network.ApChannel;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class SoftApAssistant extends NetworkConnection {
    private static String DEFAULT_PASSWORD = "password";
    private static String DEFAULT_SSID = "FTC-1234";
    private static final String NETWORK_PASSWORD_FILE = "FTC_RobotController_password.txt";
    private static final String NETWORK_SSID_FILE = "FTC_RobotController_SSID.txt";
    public static final String TAG = "SoftApAssistant";
    private static IntentFilter intentFilter;
    private static SoftApAssistant softApAssistant;
    private Context context = null;
    String password = DEFAULT_PASSWORD;
    private BroadcastReceiver receiver;
    /* access modifiers changed from: private */
    public final List<ScanResult> scanResults = new ArrayList();
    String ssid = DEFAULT_SSID;

    public void cancelPotentialConnections() {
    }

    public void detectWifiReset() {
    }

    public String getFailureReason() {
        return null;
    }

    public void onWaitForConnection() {
    }

    public void setNetworkSettings(String str, String str2, ApChannel apChannel) {
    }

    public static synchronized SoftApAssistant getSoftApAssistant(Context context2) {
        SoftApAssistant softApAssistant2;
        synchronized (SoftApAssistant.class) {
            if (softApAssistant == null) {
                softApAssistant = new SoftApAssistant(context2);
            }
            IntentFilter intentFilter2 = new IntentFilter();
            intentFilter = intentFilter2;
            intentFilter2.addAction("android.net.wifi.SCAN_RESULTS");
            intentFilter.addAction("android.net.wifi.NETWORK_IDS_CHANGED");
            intentFilter.addAction("android.net.wifi.STATE_CHANGE");
            intentFilter.addAction("android.net.wifi.supplicant.STATE_CHANGE");
            intentFilter.addAction("android.net.wifi.supplicant.CONNECTION_CHANGE");
            intentFilter.addAction("android.net.wifi.WIFI_STATE_CHANGED");
            softApAssistant2 = softApAssistant;
        }
        return softApAssistant2;
    }

    private SoftApAssistant(Context context2) {
        super(context2);
    }

    public List<ScanResult> getScanResults() {
        return this.scanResults;
    }

    public NetworkType getNetworkType() {
        return NetworkType.SOFTAP;
    }

    public void enable() {
        if (this.receiver == null) {
            this.receiver = new BroadcastReceiver() {
                public void onReceive(Context context, Intent intent) {
                    String action = intent.getAction();
                    WifiInfo connectionInfo = SoftApAssistant.this.wifiManager.getConnectionInfo();
                    RobotLog.m58v("onReceive(), action: " + action + ", wifiInfo: " + connectionInfo);
                    if (connectionInfo.getSSID().equals(SoftApAssistant.this.ssid) && connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        SoftApAssistant.this.sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
                    }
                    if ("android.net.wifi.SCAN_RESULTS".equals(action)) {
                        SoftApAssistant.this.scanResults.clear();
                        SoftApAssistant.this.scanResults.addAll(SoftApAssistant.this.wifiManager.getScanResults());
                        RobotLog.m58v("Soft AP scanResults found: " + SoftApAssistant.this.scanResults.size());
                        for (ScanResult scanResult : SoftApAssistant.this.scanResults) {
                            RobotLog.m58v("    scanResult: " + scanResult.SSID);
                        }
                        SoftApAssistant.this.sendEvent(NetworkConnection.NetworkEvent.PEERS_AVAILABLE);
                    }
                    if ("android.net.wifi.supplicant.STATE_CHANGE".equals(action) && connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
                        SoftApAssistant.this.sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
                    }
                }
            };
        }
        this.context.registerReceiver(this.receiver, intentFilter);
    }

    public void disable() {
        try {
            this.context.unregisterReceiver(this.receiver);
        } catch (IllegalArgumentException unused) {
        }
    }

    public void discoverPotentialConnections() {
        this.wifiManager.startScan();
    }

    private WifiConfiguration buildConfig(String str, String str2) {
        WifiConfiguration wifiConfiguration = new WifiConfiguration();
        wifiConfiguration.SSID = str;
        wifiConfiguration.preSharedKey = str2;
        RobotLog.m58v("Setting up network, myConfig.SSID: " + wifiConfiguration.SSID + ", password: " + wifiConfiguration.preSharedKey);
        wifiConfiguration.status = 2;
        wifiConfiguration.allowedAuthAlgorithms.set(0);
        wifiConfiguration.allowedKeyManagement.set(1);
        wifiConfiguration.allowedProtocols.set(1);
        wifiConfiguration.allowedProtocols.set(0);
        wifiConfiguration.allowedGroupCiphers.set(2);
        wifiConfiguration.allowedGroupCiphers.set(3);
        wifiConfiguration.allowedPairwiseCiphers.set(1);
        wifiConfiguration.allowedPairwiseCiphers.set(2);
        return wifiConfiguration;
    }

    public void createConnection() {
        Boolean bool;
        if (this.wifiManager.isWifiEnabled()) {
            this.wifiManager.setWifiEnabled(false);
        }
        File file = AppUtil.FIRST_FOLDER;
        File file2 = new File(file, NETWORK_SSID_FILE);
        if (!file2.exists()) {
            ReadWriteFile.writeFile(file, NETWORK_SSID_FILE, DEFAULT_SSID);
        }
        File file3 = new File(file, NETWORK_PASSWORD_FILE);
        if (!file3.exists()) {
            ReadWriteFile.writeFile(file, NETWORK_PASSWORD_FILE, DEFAULT_PASSWORD);
        }
        String readFile = ReadWriteFile.readFile(file2);
        String readFile2 = ReadWriteFile.readFile(file3);
        if (readFile.isEmpty() || readFile.length() >= 15) {
            ReadWriteFile.writeFile(file, NETWORK_SSID_FILE, DEFAULT_SSID);
        }
        if (readFile2.isEmpty()) {
            ReadWriteFile.writeFile(file, NETWORK_PASSWORD_FILE, DEFAULT_PASSWORD);
        }
        this.ssid = ReadWriteFile.readFile(file2);
        String readFile3 = ReadWriteFile.readFile(file3);
        this.password = readFile3;
        WifiConfiguration buildConfig = buildConfig(this.ssid, readFile3);
        RobotLog.m58v("Advertising SSID: " + this.ssid + ", password: " + this.password);
        try {
            Boolean.valueOf(false);
            if (isSoftAccessPoint()) {
                bool = true;
            } else {
                this.wifiManager.getClass().getMethod("setWifiApConfiguration", new Class[]{WifiConfiguration.class}).invoke(this.wifiManager, new Object[]{buildConfig});
                Method method = this.wifiManager.getClass().getMethod("setWifiApEnabled", new Class[]{WifiConfiguration.class, Boolean.TYPE});
                method.invoke(this.wifiManager, new Object[]{null, false});
                bool = (Boolean) method.invoke(this.wifiManager, new Object[]{buildConfig, true});
            }
            if (bool.booleanValue()) {
                sendEvent(NetworkConnection.NetworkEvent.AP_CREATED);
            }
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            RobotLog.m46e(e.getMessage());
            e.printStackTrace();
        }
    }

    public void connect(String str, String str2) {
        this.ssid = str;
        this.password = str2;
        WifiConfiguration buildConfig = buildConfig(String.format("\"%s\"", new Object[]{str}), String.format("\"%s\"", new Object[]{str2}));
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        RobotLog.m58v("Connecting to SoftAP, SSID: " + buildConfig.SSID + ", supplicant state: " + connectionInfo.getSupplicantState());
        if (connectionInfo.getSSID().equals(buildConfig.SSID) && connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            sendEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
        }
        if (!connectionInfo.getSSID().equals(buildConfig.SSID) || connectionInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            int addNetwork = this.wifiManager.addNetwork(buildConfig);
            this.wifiManager.saveConfiguration();
            if (addNetwork != -1) {
                for (WifiConfiguration next : this.wifiManager.getConfiguredNetworks()) {
                    if (next.SSID != null) {
                        String str3 = next.SSID;
                        if (str3.equals("\"" + str + "\"")) {
                            this.wifiManager.disconnect();
                            this.wifiManager.enableNetwork(next.networkId, true);
                            this.wifiManager.reconnect();
                            return;
                        }
                    }
                }
            }
        }
    }

    public void connect(String str) {
        connect(str, DEFAULT_PASSWORD);
    }

    public InetAddress getConnectionOwnerAddress() {
        try {
            return InetAddress.getByName("192.168.43.1");
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getConnectionOwnerName() {
        RobotLog.m58v("ssid in softap assistant: " + this.ssid);
        return this.ssid.replace("\"", InspectionState.NO_VERSION);
    }

    public String getConnectionOwnerMacAddress() {
        return this.ssid.replace("\"", InspectionState.NO_VERSION);
    }

    public boolean isConnected() {
        if (isSoftAccessPoint()) {
            return true;
        }
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        RobotLog.m58v("isConnected(), current supplicant state: " + connectionInfo.getSupplicantState().toString());
        if (connectionInfo.getSupplicantState() == SupplicantState.COMPLETED) {
            return true;
        }
        return false;
    }

    public String getDeviceName() {
        return this.ssid;
    }

    private boolean isSoftAccessPoint() {
        try {
            return ((Boolean) this.wifiManager.getClass().getMethod("isWifiApEnabled", new Class[0]).invoke(this.wifiManager, new Object[0])).booleanValue();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            return false;
        } catch (InvocationTargetException e2) {
            e2.printStackTrace();
            return false;
        } catch (IllegalAccessException e3) {
            e3.printStackTrace();
            return false;
        }
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        sb.append("Name: ");
        sb.append(getDeviceName());
        if (isSoftAccessPoint()) {
            sb.append("\nAccess Point SSID: ");
            sb.append(getConnectionOwnerName());
            sb.append("\nPassphrase: ");
            sb.append(getPassphrase());
            sb.append("\nAdvertising");
        } else if (isConnected()) {
            sb.append("\nIP Address: ");
            sb.append(getIpAddressAsString(connectionInfo.getIpAddress()));
            sb.append("\nAccess Point SSID: ");
            sb.append(getConnectionOwnerName());
            sb.append("\nPassphrase: ");
            sb.append(getPassphrase());
        } else {
            sb.append("\nNo connection information");
        }
        return sb.toString();
    }

    private String getIpAddressAsString(int i) {
        return String.format("%d.%d.%d.%d", new Object[]{Integer.valueOf(i & 255), Integer.valueOf((i >> 8) & 255), Integer.valueOf((i >> 16) & 255), Integer.valueOf((i >> 24) & 255)});
    }

    public String getPassphrase() {
        return this.password;
    }

    /* renamed from: com.qualcomm.robotcore.wifi.SoftApAssistant$2 */
    static /* synthetic */ class C07762 {
        static final /* synthetic */ int[] $SwitchMap$android$net$wifi$SupplicantState;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                android.net.wifi.SupplicantState[] r0 = android.net.wifi.SupplicantState.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$android$net$wifi$SupplicantState = r0
                android.net.wifi.SupplicantState r1 = android.net.wifi.SupplicantState.ASSOCIATING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$android$net$wifi$SupplicantState     // Catch:{ NoSuchFieldError -> 0x001d }
                android.net.wifi.SupplicantState r1 = android.net.wifi.SupplicantState.COMPLETED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$android$net$wifi$SupplicantState     // Catch:{ NoSuchFieldError -> 0x0028 }
                android.net.wifi.SupplicantState r1 = android.net.wifi.SupplicantState.SCANNING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.wifi.SoftApAssistant.C07762.<clinit>():void");
        }
    }

    public NetworkConnection.ConnectStatus getConnectStatus() {
        int i = C07762.$SwitchMap$android$net$wifi$SupplicantState[this.wifiManager.getConnectionInfo().getSupplicantState().ordinal()];
        if (i == 1) {
            return NetworkConnection.ConnectStatus.CONNECTING;
        }
        if (i == 2) {
            return NetworkConnection.ConnectStatus.CONNECTED;
        }
        if (i != 3) {
            return NetworkConnection.ConnectStatus.NOT_CONNECTED;
        }
        return NetworkConnection.ConnectStatus.NOT_CONNECTED;
    }
}
