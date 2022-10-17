package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxGetSingleDIOInputResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 1;
    private byte value = 0;

    public LynxGetSingleDIOInputResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public boolean getValue() {
        return TypeConversion.unsignedByteToInt(this.value) != 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.value);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.value = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
