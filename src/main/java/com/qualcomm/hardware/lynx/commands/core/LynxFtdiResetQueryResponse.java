package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxFtdiResetQueryResponse extends LynxDekaInterfaceResponse {
    public static final int cbPayload = 1;
    private byte enabled = 0;

    public LynxFtdiResetQueryResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public boolean isEnabled() {
        return this.enabled != 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.enabled);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.enabled = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
