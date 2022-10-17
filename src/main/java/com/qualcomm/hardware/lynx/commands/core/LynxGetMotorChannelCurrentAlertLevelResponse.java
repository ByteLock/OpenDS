package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxGetMotorChannelCurrentAlertLevelResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 1;
    private short mACurrentLimit;

    public LynxGetMotorChannelCurrentAlertLevelResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getCurrentLimit() {
        return TypeConversion.unsignedShortToInt(this.mACurrentLimit);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.mACurrentLimit);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.mACurrentLimit = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
