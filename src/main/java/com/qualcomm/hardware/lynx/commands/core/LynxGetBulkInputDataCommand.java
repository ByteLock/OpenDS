package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;

public class LynxGetBulkInputDataCommand extends LynxDekaInterfaceCommand<LynxGetBulkInputDataResponse> {
    public void fromPayloadByteArray(byte[] bArr) {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxGetBulkInputDataCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetBulkInputDataResponse(lynxModuleIntf));
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetBulkInputDataResponse.class;
    }
}
