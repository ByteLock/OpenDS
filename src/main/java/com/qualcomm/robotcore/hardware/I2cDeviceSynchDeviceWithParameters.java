package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.util.RobotLog;

public abstract class I2cDeviceSynchDeviceWithParameters<DEVICE_CLIENT extends I2cDeviceSynchSimple, PARAMETERS> extends I2cDeviceSynchDevice<DEVICE_CLIENT> {
    protected PARAMETERS parameters;

    /* access modifiers changed from: protected */
    public abstract boolean internalInitialize(PARAMETERS parameters2);

    protected I2cDeviceSynchDeviceWithParameters(DEVICE_CLIENT device_client, boolean z, PARAMETERS parameters2) {
        super(device_client, z);
        this.parameters = parameters2;
    }

    public PARAMETERS getParameters() {
        return this.parameters;
    }

    /* access modifiers changed from: protected */
    public synchronized boolean doInitialize() {
        return internalInitialize(this.parameters);
    }

    public boolean initialize(PARAMETERS parameters2) {
        this.isInitialized = internalInitialize(parameters2);
        if (this.deviceClientIsOwned) {
            if (this.isInitialized) {
                I2cWarningManager.removeProblemI2cDevice(this.deviceClient);
            } else {
                RobotLog.m47e("Marking I2C device %s %s as unhealthy because initialization failed", getClass().getSimpleName(), getConnectionInfo());
                I2cWarningManager.notifyProblemI2cDevice(this.deviceClient);
            }
        }
        return this.isInitialized;
    }
}
