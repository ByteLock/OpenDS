package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.util.TypeConversion;

public class LynxGetModuleLEDColorResponse extends LynxStandardResponse {
    byte blue;
    byte green;
    byte red;

    public LynxGetModuleLEDColorResponse(LynxModule lynxModule) {
        super(lynxModule);
    }

    /* access modifiers changed from: package-private */
    public int getRed() {
        return TypeConversion.unsignedByteToInt(this.red);
    }

    /* access modifiers changed from: package-private */
    public int getGreen() {
        return TypeConversion.unsignedByteToInt(this.green);
    }

    /* access modifiers changed from: package-private */
    public int getBlue() {
        return TypeConversion.unsignedByteToInt(this.blue);
    }

    public static int getStandardCommandNumber() {
        return LynxGetModuleLEDColorCommand.getStandardCommandNumber() | 32768;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.red, this.green, this.blue};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.red = bArr[0];
        this.green = bArr[1];
        this.blue = bArr[2];
    }
}
