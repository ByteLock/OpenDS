package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;

public class LynxFailSafeCommand extends LynxStandardCommand<LynxAck> {
    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_FAIL_SAFE;
    }

    public void fromPayloadByteArray(byte[] bArr) {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxFailSafeCommand(LynxModule lynxModule) {
        super(lynxModule);
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }
}
