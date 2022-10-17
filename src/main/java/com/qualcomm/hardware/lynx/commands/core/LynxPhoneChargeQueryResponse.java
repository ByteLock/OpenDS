package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxPhoneChargeQueryResponse extends LynxDekaInterfaceResponse {
    public static final int cbPayload = 1;
    private byte chargeEnabled;

    public LynxPhoneChargeQueryResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
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
