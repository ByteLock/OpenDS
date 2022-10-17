package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand;
import java.nio.ByteBuffer;

public class LynxGetMotorPIDFControlLoopCoefficientsResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 17;

    /* renamed from: d */
    private int f92d = 0;

    /* renamed from: f */
    private int f93f = 0;

    /* renamed from: i */
    private int f94i = 0;
    private byte motorControlAlgorithm = 0;

    /* renamed from: p */
    private int f95p = 0;

    public LynxGetMotorPIDFControlLoopCoefficientsResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getP() {
        return this.f95p;
    }

    public int getI() {
        return this.f94i;
    }

    public int getD() {
        return this.f92d;
    }

    public int getF() {
        return this.f93f;
    }

    public LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm getInternalMotorControlAlgorithm() {
        return LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm.fromByte(this.motorControlAlgorithm);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(17).order(LynxDatagram.LYNX_ENDIAN);
        order.putInt(this.f95p);
        order.putInt(this.f94i);
        order.putInt(this.f92d);
        order.putInt(this.f93f);
        order.put(this.motorControlAlgorithm);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.f95p = order.getInt();
        this.f94i = order.getInt();
        this.f92d = order.getInt();
        this.f93f = order.getInt();
        this.motorControlAlgorithm = order.get();
    }
}
