package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxMessage;

public abstract class LynxResponse<RESPONSE extends LynxMessage> extends LynxRespondable<RESPONSE> {
    public static final int RESPONSE_BIT = 32768;

    public boolean isResponse() {
        return true;
    }

    public LynxResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }
}
