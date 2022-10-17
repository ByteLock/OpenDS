package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxResponse;

public class LynxGetModuleLEDPatternCommand extends LynxStandardCommand<LynxGetModuleLEDPatternResponse> {
    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_GET_MODULE_LED_PATTERN;
    }

    public void fromPayloadByteArray(byte[] bArr) {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxGetModuleLEDPatternCommand(LynxModule lynxModule) {
        super(lynxModule, new LynxGetModuleLEDPatternResponse(lynxModule));
    }

    public static Class<? extends LynxResponse> getResponseClass() {
        return LynxGetModuleLEDPatternResponse.class;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }
}
