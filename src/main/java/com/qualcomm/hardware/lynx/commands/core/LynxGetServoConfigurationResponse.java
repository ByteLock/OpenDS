package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxGetServoConfigurationResponse extends LynxDekaInterfaceResponse {
    public static final int cbPayload = 2;
    private short framePeriod = 0;

    public LynxGetServoConfigurationResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getFramePeriod() {
        return TypeConversion.unsignedShortToInt(this.framePeriod);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.putShort(this.framePeriod);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.framePeriod = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).getShort();
    }
}
