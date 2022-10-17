package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import java.nio.ByteBuffer;

public class LynxGetMotorPIDControlLoopCoefficientsResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 12;

    /* renamed from: d */
    private int f89d = 0;

    /* renamed from: i */
    private int f90i = 0;

    /* renamed from: p */
    private int f91p = 0;

    public LynxGetMotorPIDControlLoopCoefficientsResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getP() {
        return this.f91p;
    }

    public int getI() {
        return this.f90i;
    }

    public int getD() {
        return this.f89d;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(12).order(LynxDatagram.LYNX_ENDIAN);
        order.putInt(this.f91p);
        order.putInt(this.f90i);
        order.putInt(this.f89d);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.f91p = order.getInt();
        this.f90i = order.getInt();
        this.f89d = order.getInt();
    }
}
