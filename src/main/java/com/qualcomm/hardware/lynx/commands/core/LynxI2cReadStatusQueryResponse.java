package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxI2cReadStatusQueryResponse extends LynxI2cResponse {
    public static final int cbPayload = 2;
    private byte cbRead;
    private byte[] payload;

    public LynxI2cReadStatusQueryResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cReadStatusQueryResponse(LynxModuleIntf lynxModuleIntf, int i) {
        super(lynxModuleIntf);
        this.payload = new byte[i];
    }

    public byte[] getBytes() {
        return this.payload;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cStatus);
        order.put(this.cbRead);
        order.put(this.payload);
        return order.array();
    }

    /* JADX WARNING: type inference failed for: r0v2, types: [int, byte] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fromPayloadByteArray(byte[] r2) {
        /*
            r1 = this;
            java.nio.ByteBuffer r2 = java.nio.ByteBuffer.wrap(r2)
            java.nio.ByteOrder r0 = com.qualcomm.hardware.lynx.commands.LynxDatagram.LYNX_ENDIAN
            java.nio.ByteBuffer r2 = r2.order(r0)
            byte r0 = r2.get()
            r1.i2cStatus = r0
            byte r0 = r2.get()
            r1.cbRead = r0
            byte[] r0 = new byte[r0]
            r1.payload = r0
            r2.get(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.core.LynxI2cReadStatusQueryResponse.fromPayloadByteArray(byte[]):void");
    }
}
