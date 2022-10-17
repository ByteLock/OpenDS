package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import java.nio.ByteBuffer;

public class LynxFtdiResetQueryCommand extends LynxDekaInterfaceCommand<LynxFtdiResetQueryResponse> {
    public static final int cbPayload = 0;

    public LynxFtdiResetQueryCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxFtdiResetQueryResponse(lynxModuleIntf));
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxPhoneChargeQueryResponse.class;
    }

    public byte[] toPayloadByteArray() {
        return ByteBuffer.allocate(0).order(LynxDatagram.LYNX_ENDIAN).array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
    }
}
