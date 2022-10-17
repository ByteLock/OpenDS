package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxI2cWriteStatusQueryResponse extends LynxI2cResponse {
    public static final int cbPayload = 2;
    private byte cbWritten;

    public LynxI2cWriteStatusQueryResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cStatus);
        order.put(this.cbWritten);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cStatus = order.get();
        this.cbWritten = order.get();
    }
}
