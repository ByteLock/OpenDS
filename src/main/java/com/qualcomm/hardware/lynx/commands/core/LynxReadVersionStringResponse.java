package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;

public class LynxReadVersionStringResponse extends LynxDekaInterfaceResponse {
    private byte cbText = 0;
    private byte[] rgbText = null;

    public LynxReadVersionStringResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public String getNullableVersionString() {
        byte[] bArr = this.rgbText;
        if (bArr == null) {
            return null;
        }
        return new String(bArr, Charset.forName("UTF-8"));
    }

    public byte[] toPayloadByteArray() {
        byte[] bArr = this.rgbText;
        ByteBuffer order = ByteBuffer.allocate((bArr == null ? 0 : bArr.length) + 1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.cbText);
        byte[] bArr2 = this.rgbText;
        if (bArr2 != null) {
            order.put(bArr2);
        }
        return order.array();
    }

    /* JADX WARNING: type inference failed for: r0v1, types: [int, byte] */
    /* JADX WARNING: Unknown variable types count: 1 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void fromPayloadByteArray(byte[] r2) {
        /*
            r1 = this;
            java.nio.ByteBuffer r2 = java.nio.ByteBuffer.wrap(r2)
            java.nio.ByteOrder r0 = com.qualcomm.hardware.lynx.commands.LynxDatagram.LYNX_ENDIAN
            java.nio.ByteBuffer r2 = r2.order(r0)
            byte r0 = r2.get()
            r1.cbText = r0
            byte[] r0 = new byte[r0]
            r1.rgbText = r0
            r2.get(r0)
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.core.LynxReadVersionStringResponse.fromPayloadByteArray(byte[]):void");
    }
}
