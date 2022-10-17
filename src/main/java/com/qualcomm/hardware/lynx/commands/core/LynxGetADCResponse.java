package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxGetADCResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 2;
    private short value;

    public LynxGetADCResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getValue() {
        return this.value;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.value);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.value = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
