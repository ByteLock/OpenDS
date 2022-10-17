package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import java.util.concurrent.Callable;

public class HardwareDeviceHealthImpl implements HardwareDeviceHealth {
    protected HardwareDeviceHealth.HealthStatus healthStatus;
    protected Callable<HardwareDeviceHealth.HealthStatus> override;
    protected String tag;

    public HardwareDeviceHealthImpl(String str) {
        this(str, (Callable<HardwareDeviceHealth.HealthStatus>) null);
    }

    public HardwareDeviceHealthImpl(String str, Callable<HardwareDeviceHealth.HealthStatus> callable) {
        this.tag = str;
        this.healthStatus = HardwareDeviceHealth.HealthStatus.UNKNOWN;
        this.override = callable;
    }

    public void close() {
        setHealthStatus(HardwareDeviceHealth.HealthStatus.CLOSED);
    }

    public void setHealthStatus(HardwareDeviceHealth.HealthStatus healthStatus2) {
        synchronized (this) {
            if (this.healthStatus != HardwareDeviceHealth.HealthStatus.CLOSED) {
                this.healthStatus = healthStatus2;
            }
        }
    }

    public HardwareDeviceHealth.HealthStatus getHealthStatus() {
        synchronized (this) {
            Callable<HardwareDeviceHealth.HealthStatus> callable = this.override;
            if (callable != null) {
                try {
                    HardwareDeviceHealth.HealthStatus call = callable.call();
                    if (call != HardwareDeviceHealth.HealthStatus.UNKNOWN) {
                        return call;
                    }
                } catch (Exception unused) {
                }
            }
            HardwareDeviceHealth.HealthStatus healthStatus2 = this.healthStatus;
            return healthStatus2;
        }
    }
}
