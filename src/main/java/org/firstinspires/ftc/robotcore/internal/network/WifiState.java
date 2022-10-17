package org.firstinspires.ftc.robotcore.internal.network;

public enum WifiState {
    DISABLING(0),
    DISABLED(1),
    ENABLING(2),
    ENABLED(3),
    UNKNOWN(4),
    FAILED(5);
    
    public int state;

    private WifiState(int i) {
        this.state = i;
    }

    public static WifiState from(int i) {
        for (WifiState wifiState : values()) {
            if (wifiState.state == i) {
                return wifiState;
            }
        }
        return UNKNOWN;
    }

    public boolean isEnabled() {
        return this == ENABLED;
    }
}
