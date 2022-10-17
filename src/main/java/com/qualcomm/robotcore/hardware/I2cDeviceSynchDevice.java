package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.util.RobotLog;

public abstract class I2cDeviceSynchDevice<DEVICE_CLIENT extends I2cDeviceSynchSimple> implements RobotArmingStateNotifier.Callback, HardwareDevice {
    private static String TAG = "I2C";
    protected DEVICE_CLIENT deviceClient;
    protected boolean deviceClientIsOwned;
    protected boolean isInitialized = false;

    /* access modifiers changed from: protected */
    public abstract boolean doInitialize();

    public int getVersion() {
        return 1;
    }

    protected I2cDeviceSynchDevice(DEVICE_CLIENT device_client, boolean z) {
        this.deviceClient = device_client;
        this.deviceClientIsOwned = z;
        device_client.enableWriteCoalescing(false);
    }

    /* access modifiers changed from: protected */
    public void registerArmingStateCallback(boolean z) {
        DEVICE_CLIENT device_client = this.deviceClient;
        if (device_client instanceof RobotArmingStateNotifier) {
            ((RobotArmingStateNotifier) device_client).registerCallback(this, z);
        }
    }

    /* access modifiers changed from: protected */
    public void engage() {
        DEVICE_CLIENT device_client = this.deviceClient;
        if (device_client instanceof Engagable) {
            ((Engagable) device_client).engage();
        }
    }

    /* access modifiers changed from: protected */
    public void disengage() {
        DEVICE_CLIENT device_client = this.deviceClient;
        if (device_client instanceof Engagable) {
            ((Engagable) device_client).disengage();
        }
    }

    public DEVICE_CLIENT getDeviceClient() {
        return this.deviceClient;
    }

    public void onModuleStateChange(RobotArmingStateNotifier robotArmingStateNotifier, RobotArmingStateNotifier.ARMINGSTATE armingstate) {
        if (armingstate == RobotArmingStateNotifier.ARMINGSTATE.ARMED) {
            initializeIfNecessary();
        } else if (armingstate == RobotArmingStateNotifier.ARMINGSTATE.PRETENDING) {
            initializeIfNecessary();
            this.isInitialized = false;
        }
    }

    /* access modifiers changed from: protected */
    public synchronized void initializeIfNecessary() {
        if (!this.isInitialized) {
            RobotLog.m55ii(TAG, "Automatically initializing I2C device %s %s", getClass().getSimpleName(), getConnectionInfo());
            initialize();
        }
    }

    public synchronized boolean initialize() {
        boolean doInitialize = doInitialize();
        this.isInitialized = doInitialize;
        if (this.deviceClientIsOwned) {
            if (doInitialize) {
                I2cWarningManager.removeProblemI2cDevice(this.deviceClient);
            } else {
                RobotLog.m47e("Marking I2C device %s %s as unhealthy because initialization failed", getClass().getSimpleName(), getConnectionInfo());
                I2cWarningManager.notifyProblemI2cDevice(this.deviceClient);
            }
        }
        return this.isInitialized;
    }

    public void resetDeviceConfigurationForOpMode() {
        this.deviceClient.resetDeviceConfigurationForOpMode();
        this.isInitialized = false;
    }

    public void close() {
        if (this.deviceClientIsOwned) {
            this.deviceClient.close();
        }
    }

    public String getConnectionInfo() {
        return this.deviceClient.getConnectionInfo();
    }
}
