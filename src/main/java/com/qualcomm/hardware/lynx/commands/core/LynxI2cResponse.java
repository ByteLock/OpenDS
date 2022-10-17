package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.robotcore.util.RobotLog;

public abstract class LynxI2cResponse extends LynxDekaInterfaceResponse {
    protected byte i2cStatus = 0;

    public static boolean isAddressAcknowledged(byte b) {
        return (b & 1) == 0;
    }

    public static boolean isArbitrationLost(byte b) {
        return (b & 4) != 0;
    }

    public static boolean isClockTimeout(byte b) {
        return (b & 8) != 0;
    }

    public static boolean isDataAcknowledged(byte b) {
        return (b & 2) == 0;
    }

    public static boolean isStatusOk(byte b) {
        return (b & 15) == 0;
    }

    public LynxI2cResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public byte getI2cStatus() {
        return this.i2cStatus;
    }

    public boolean isAddressAcknowledged() {
        return isAddressAcknowledged(getI2cStatus());
    }

    public boolean isDataAcknowledged() {
        return isDataAcknowledged(getI2cStatus());
    }

    public boolean isArbitrationLost() {
        return isArbitrationLost(getI2cStatus());
    }

    public boolean isClockTimeout() {
        return isClockTimeout(getI2cStatus());
    }

    public boolean isStatusOk() {
        return isStatusOk(getI2cStatus());
    }

    public void logResponse() {
        if (getI2cStatus() != 0) {
            RobotLog.m59v("addr=%s data=%s arb=%s clock=%s", Boolean.valueOf(isAddressAcknowledged()), Boolean.valueOf(isDataAcknowledged()), Boolean.valueOf(isArbitrationLost()), Boolean.valueOf(isClockTimeout()));
        }
    }
}
