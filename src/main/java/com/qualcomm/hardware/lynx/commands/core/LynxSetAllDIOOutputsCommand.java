package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import java.nio.ByteBuffer;

public class LynxSetAllDIOOutputsCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public final int cbPayload;
    private int values;

    public LynxSetAllDIOOutputsCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 1;
    }

    public LynxSetAllDIOOutputsCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        this.values = i;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put((byte) this.values);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.values = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
