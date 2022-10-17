package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import java.nio.ByteBuffer;

public class LynxPhoneChargeControlCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 1;
    private byte chargeEnabled;

    public LynxPhoneChargeControlCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxPhoneChargeControlCommand(LynxModuleIntf lynxModuleIntf, boolean z) {
        this(lynxModuleIntf);
        this.chargeEnabled = z ? (byte) 1 : 0;
    }

    public boolean isChargeEnabled() {
        return this.chargeEnabled != 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.chargeEnabled);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.chargeEnabled = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
