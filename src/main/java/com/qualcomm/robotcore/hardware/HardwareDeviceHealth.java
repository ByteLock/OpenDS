package com.qualcomm.robotcore.hardware;

public interface HardwareDeviceHealth {

    public enum HealthStatus {
        UNKNOWN,
        HEALTHY,
        UNHEALTHY,
        CLOSED
    }

    HealthStatus getHealthStatus();

    void setHealthStatus(HealthStatus healthStatus);
}
