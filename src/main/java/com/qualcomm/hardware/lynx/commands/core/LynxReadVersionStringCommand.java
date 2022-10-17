package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import java.nio.ByteBuffer;

public class LynxReadVersionStringCommand extends LynxDekaInterfaceCommand<LynxReadVersionStringResponse> {
    public static final int cbPayload = 0;

    /* access modifiers changed from: protected */
    public boolean usePretendResponseIfRealModuleDoesntSupport() {
        return true;
    }

    public LynxReadVersionStringCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxReadVersionStringResponse(lynxModuleIntf));
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxReadVersionStringResponse.class;
    }

    public byte[] toPayloadByteArray() {
        return ByteBuffer.allocate(0).order(LynxDatagram.LYNX_ENDIAN).array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
    }
}
