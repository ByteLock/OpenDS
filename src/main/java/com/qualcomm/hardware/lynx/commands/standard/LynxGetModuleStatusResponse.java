package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.TypeConversion;

public class LynxGetModuleStatusResponse extends LynxStandardResponse {
    public static final int bitBatteryLow = 16;
    public static final int bitControllerOverTemp = 8;
    public static final int bitDeviceReset = 2;
    public static final int bitFailSafe = 4;
    public static final int bitHIBFault = 32;
    public static final int bitKeepAliveTimeout = 1;
    byte motorAlerts;
    byte status;

    public LynxGetModuleStatusResponse(LynxModule lynxModule) {
        super(lynxModule);
    }

    public String toString() {
        StringBuilder sb = new StringBuilder();
        appendBit(sb, 1, "KeepAliveTimeout");
        appendBit(sb, 2, "Reset");
        appendBit(sb, 4, "FailSafe");
        appendBit(sb, 8, "Temp");
        appendBit(sb, 16, "Battery");
        appendBit(sb, 32, "HIB Fault");
        String sb2 = sb.toString();
        if (sb2.length() > 0) {
            sb2 = ": " + sb2;
        }
        return String.format("LynxGetModuleStatusResponse(status=0x%02x alerts=0x%02x%s)", new Object[]{Byte.valueOf(this.status), Byte.valueOf(this.motorAlerts), sb2});
    }

    /* access modifiers changed from: protected */
    public void appendBit(StringBuilder sb, int i, String str) {
        if (testBitsOn(i)) {
            if (sb.length() > 0) {
                sb.append("|");
            }
            sb.append(str);
        }
    }

    public boolean isKeepAliveTimeout() {
        return testBitsOn(1);
    }

    public boolean isDeviceReset() {
        return testBitsOn(2);
    }

    public boolean isFailSafe() {
        return testBitsOn(4);
    }

    public boolean isControllerOverTemp() {
        return testBitsOn(8);
    }

    public boolean isBatteryLow() {
        return testBitsOn(16);
    }

    public boolean isHIBFault() {
        return testBitsOn(32);
    }

    public int getStatus() {
        return TypeConversion.unsignedByteToInt(this.status);
    }

    public boolean testBitsOn(int i) {
        return (getStatus() & i) == i;
    }

    public boolean testAnyBits(int i) {
        return (i & getStatus()) != 0;
    }

    public int getMotorAlerts() {
        return TypeConversion.unsignedByteToInt(this.motorAlerts);
    }

    public boolean hasMotorLostCounts(int i) {
        LynxConstants.validateMotorZ(i);
        int i2 = 1 << i;
        if ((getMotorAlerts() & i2) == i2) {
            return true;
        }
        return false;
    }

    public boolean isMotorBridgeOverTemp(int i) {
        LynxConstants.validateMotorZ(i);
        int i2 = 1 << (i + 4);
        if ((getMotorAlerts() & i2) == i2) {
            return true;
        }
        return false;
    }

    public static int getStandardCommandNumber() {
        return LynxGetModuleStatusCommand.getStandardCommandNumber() | 32768;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.status, this.motorAlerts};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.status = bArr[0];
        this.motorAlerts = bArr[1];
    }
}
