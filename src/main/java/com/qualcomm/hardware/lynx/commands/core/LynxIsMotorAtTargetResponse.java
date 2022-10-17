package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxIsMotorAtTargetResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 1;
    private byte atTarget;

    public LynxIsMotorAtTargetResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public boolean isAtTarget() {
        return this.atTarget != 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.atTarget);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.atTarget = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
