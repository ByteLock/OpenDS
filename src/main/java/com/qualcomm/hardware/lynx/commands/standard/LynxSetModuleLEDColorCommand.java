package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;

public class LynxSetModuleLEDColorCommand extends LynxStandardCommand<LynxAck> {
    byte blue;
    byte green;
    byte red;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_SET_MODULE_LED_COLOR;
    }

    public LynxSetModuleLEDColorCommand(LynxModule lynxModule) {
        super(lynxModule);
    }

    public LynxSetModuleLEDColorCommand(LynxModule lynxModule, byte b, byte b2, byte b3) {
        this(lynxModule);
        this.red = b;
        this.green = b2;
        this.blue = b3;
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
