package com.qualcomm.robotcore.wifi;

import android.content.Context;
import com.qualcomm.robotcore.util.RobotLog;

public class NetworkConnectionFactory {
    public static final String NETWORK_CONNECTION_TYPE = "NETWORK_CONNECTION_TYPE";

    /* renamed from: com.qualcomm.robotcore.wifi.NetworkConnectionFactory$1 */
    static /* synthetic */ class C07731 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType;

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
                com.qualcomm.robotcore.wifi.NetworkType[] r0 = com.qualcomm.robotcore.wifi.NetworkType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType = r0
                com.qualcomm.robotcore.wifi.NetworkType r1 = com.qualcomm.robotcore.wifi.NetworkType.WIFIDIRECT     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.wifi.NetworkType r1 = com.qualcomm.robotcore.wifi.NetworkType.LOOPBACK     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.wifi.NetworkType r1 = com.qualcomm.robotcore.wifi.NetworkType.SOFTAP     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.wifi.NetworkType r1 = com.qualcomm.robotcore.wifi.NetworkType.WIRELESSAP     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$wifi$NetworkType     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.wifi.NetworkType r1 = com.qualcomm.robotcore.wifi.NetworkType.RCWIRELESSAP     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.wifi.NetworkConnectionFactory.C07731.<clinit>():void");
        }
    }

    public static NetworkConnection getNetworkConnection(NetworkType networkType, Context context) {
        RobotLog.m58v("Getting network assistant of type: " + networkType);
        int i = C07731.$SwitchMap$com$qualcomm$robotcore$wifi$NetworkType[networkType.ordinal()];
        if (i == 1) {
            return WifiDirectAssistant.getWifiDirectAssistant(context);
        }
        if (i == 3) {
            return SoftApAssistant.getSoftApAssistant(context);
        }
        if (i == 4) {
            return DriverStationAccessPointAssistant.getDriverStationAccessPointAssistant(context);
        }
        if (i != 5) {
            return null;
        }
        return RobotControllerAccessPointAssistant.getRobotControllerAccessPointAssistant(context);
    }

    public static NetworkType getTypeFromString(String str) {
        return NetworkType.fromString(str);
    }
}
