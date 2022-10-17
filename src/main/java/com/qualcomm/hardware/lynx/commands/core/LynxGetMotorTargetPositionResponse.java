package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxGetMotorTargetPositionResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 6;
    private int target;
    private short tolerance;

    public LynxGetMotorTargetPositionResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getTarget() {
        return this.target;
    }

    public int getTolerance() {
        return TypeConversion.unsignedShortToInt(this.tolerance);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(6).order(LynxDatagram.LYNX_ENDIAN);
        order.putInt(this.target);
        order.putShort(this.tolerance);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.target = order.getInt();
        this.tolerance = order.getShort();
    }
}
