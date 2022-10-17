package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxResponse;

public class LynxGetModuleLEDColorCommand extends LynxStandardCommand<LynxGetModuleLEDColorResponse> {
    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_GET_MODULE_LED_COLOR;
    }

    public void fromPayloadByteArray(byte[] bArr) {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxGetModuleLEDColorCommand(LynxModule lynxModule) {
        super(lynxModule, new LynxGetModuleLEDColorResponse(lynxModule));
    }

    public static Class<? extends LynxResponse> getResponseClass() {
        return LynxGetModuleLEDColorResponse.class;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }
}
