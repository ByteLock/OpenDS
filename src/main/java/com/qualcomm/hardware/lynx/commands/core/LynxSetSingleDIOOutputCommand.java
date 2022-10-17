package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import java.nio.ByteBuffer;

public class LynxSetSingleDIOOutputCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public final int cbPayload;
    private byte pin;
    private byte value;

    public LynxSetSingleDIOOutputCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 2;
    }

    public LynxSetSingleDIOOutputCommand(LynxModuleIntf lynxModuleIntf, int i, boolean z) {
        this(lynxModuleIntf);
        this.pin = (byte) i;
        this.value = z ? (byte) 1 : 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.pin);
        order.put(this.value);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.pin = order.get();
        this.value = order.get();
    }
}
