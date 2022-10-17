package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxGetMotorTargetVelocityResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 2;
    private short velocity = 0;

    public LynxGetMotorTargetVelocityResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getVelocity() {
        return this.velocity;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.velocity);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.velocity = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
