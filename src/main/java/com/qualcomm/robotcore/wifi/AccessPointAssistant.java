package com.qualcomm.robotcore.wifi;

import android.content.Context;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiInfo;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import java.net.InetAddress;
import java.net.UnknownHostException;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectAgent;

public abstract class AccessPointAssistant extends NetworkConnection {
    private static final String DEFAULT_TETHERING_IP_ADDR = "192.168.43.1";
    private static final String TAG = "AccessPointAssistant";

    public void connect(String str) {
    }

    public void connect(String str, String str2) {
    }

    public void createConnection() {
    }

    public void detectWifiReset() {
    }

    public String getFailureReason() {
        return null;
    }

    /* access modifiers changed from: protected */
    public abstract String getIpAddress();

    public String getPassphrase() {
        return null;
    }

    public void onWaitForConnection() {
    }

    public AccessPointAssistant(Context context) {
        super(context);
        WifiDirectAgent.getInstance().doNotListen();
    }

    public NetworkType getNetworkType() {
        return NetworkType.WIRELESSAP;
    }

    public InetAddress getConnectionOwnerAddress() {
        try {
            return InetAddress.getByName(DEFAULT_TETHERING_IP_ADDR);
        } catch (UnknownHostException e) {
            e.printStackTrace();
            return null;
        }
    }

    public String getConnectionOwnerMacAddress() {
        return getConnectionOwnerName();
    }

    public boolean isConnected() {
        WifiInfo connectionInfo = this.wifiManager.getConnectionInfo();
        if (connectionInfo == null || connectionInfo.getSupplicantState() != SupplicantState.COMPLETED) {
            return false;
        }
        return true;
    }

    public String getDeviceName() {
        return getConnectionOwnerName();
    }

    public String getInfo() {
        StringBuilder sb = new StringBuilder();
        sb.append("Name: ");
        sb.append(getDeviceName());
        sb.append("\nIP Address: ");
        sb.append(getIpAddress());
        sb.append("\nAccess Point SSID: ");
        sb.append(getConnectionOwnerName());
        if (getPassphrase() != null) {
            sb.append("\nPassphrase: ");
            sb.append(getPassphrase());
        }
        return sb.toString();
    }

    /* renamed from: com.qualcomm.robotcore.wifi.AccessPointAssistant$1 */
    static /* synthetic */ class C07691 {
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
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.wifi.AccessPointAssistant.C07691.<clinit>():void");
        }
    }

    public NetworkConnection.ConnectStatus getConnectStatus() {
        int i = C07691.$SwitchMap$android$net$wifi$SupplicantState[this.wifiManager.getConnectionInfo().getSupplicantState().ordinal()];
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
