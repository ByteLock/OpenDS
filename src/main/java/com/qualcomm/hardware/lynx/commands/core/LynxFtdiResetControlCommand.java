package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import java.nio.ByteBuffer;

public class LynxFtdiResetControlCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 1;
    private byte enabled;

    public LynxFtdiResetControlCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxFtdiResetControlCommand(LynxModuleIntf lynxModuleIntf, boolean z) {
        this(lynxModuleIntf);
        this.enabled = z ? (byte) 1 : 0;
    }

    public boolean isEnabled() {
        return this.enabled != 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.enabled);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.enabled = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
