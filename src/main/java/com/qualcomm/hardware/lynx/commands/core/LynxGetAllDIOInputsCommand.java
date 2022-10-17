package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;

public class LynxGetAllDIOInputsCommand extends LynxDekaInterfaceCommand<LynxGetAllDIOInputsResponse> {
    private static final int cbPayload = 0;

    public void fromPayloadByteArray(byte[] bArr) {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxGetAllDIOInputsCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetAllDIOInputsResponse(lynxModuleIntf));
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetSingleDIOInputResponse.class;
    }
}
