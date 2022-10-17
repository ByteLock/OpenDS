package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxGetMotorConstantPowerResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 2;
    private short power = 0;

    public LynxGetMotorConstantPowerResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getPower() {
        return this.power;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.power);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.power = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
