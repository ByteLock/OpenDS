package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxGetMotorEncoderPositionResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 4;
    private int position;

    public LynxGetMotorEncoderPositionResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getPosition() {
        return this.position;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(4).order(LynxDatagram.LYNX_ENDIAN);
        order.putInt(this.position);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.position = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getInt();
    }
}
