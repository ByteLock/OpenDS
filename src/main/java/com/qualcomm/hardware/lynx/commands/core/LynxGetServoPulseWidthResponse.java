package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxGetServoPulseWidthResponse extends LynxDekaInterfaceResponse {
    public static final int cbPayload = 2;
    private short pulseWidth = 0;

    public LynxGetServoPulseWidthResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getPulseWidth() {
        return TypeConversion.unsignedShortToInt(this.pulseWidth);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.pulseWidth);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.pulseWidth = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
