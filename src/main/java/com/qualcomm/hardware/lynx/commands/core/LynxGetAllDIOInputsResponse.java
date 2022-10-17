package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetAllDIOInputsResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 1;
    private byte bits = 0;

    public LynxGetAllDIOInputsResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public boolean getPin(int i) {
        LynxConstants.validateDigitalIOZ(i);
        if (((1 << i) & this.bits) != 0) {
            return true;
        }
        return false;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.bits);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.bits = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
