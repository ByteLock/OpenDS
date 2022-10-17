package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;

public class LynxSetNewModuleAddressCommand extends LynxStandardCommand<LynxAck> {
    byte moduleAddress;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_SET_NEW_MODULE_ADDRESS;
    }

    public LynxSetNewModuleAddressCommand(LynxModule lynxModule) {
        super(lynxModule);
    }

    public LynxSetNewModuleAddressCommand(LynxModule lynxModule, byte b) {
        this(lynxModule);
        this.moduleAddress = b;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.moduleAddress};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.moduleAddress = bArr[0];
    }
}
