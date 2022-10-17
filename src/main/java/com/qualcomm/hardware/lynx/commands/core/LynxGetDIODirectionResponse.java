package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import java.nio.ByteBuffer;

public class LynxGetDIODirectionResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 1;
    private byte direction = 0;

    public LynxGetDIODirectionResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public DigitalChannel.Mode getMode() {
        return this.direction == 0 ? DigitalChannel.Mode.INPUT : DigitalChannel.Mode.OUTPUT;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.direction);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.direction = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
