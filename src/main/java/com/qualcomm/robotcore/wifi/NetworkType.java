package com.qualcomm.robotcore.wifi;

public enum NetworkType {
    WIFIDIRECT,
    LOOPBACK,
    SOFTAP,
    WIRELESSAP,
    RCWIRELESSAP,
    UNKNOWN_NETWORK_TYPE;

    public static NetworkType fromString(String str) {
        try {
            return valueOf(str.toUpperCase());
        } catch (Exception unused) {
            return UNKNOWN_NETWORK_TYPE;
        }
    }

    public static NetworkType globalDefault() {
        return WIFIDIRECT;
    }

    public static String globalDefaultAsString() {
        return globalDefault().toString();
    }
}
