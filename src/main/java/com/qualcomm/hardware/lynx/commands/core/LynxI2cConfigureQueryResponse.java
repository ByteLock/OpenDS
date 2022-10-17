package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cConfigureChannelCommand;
import java.nio.ByteBuffer;

public class LynxI2cConfigureQueryResponse extends LynxDekaInterfaceResponse {
    public static final int cbPayload = 1;
    private byte speedCode;

    public LynxI2cConfigureQueryResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public static LynxI2cConfigureQueryResponse createDefaultResponse(LynxModuleIntf lynxModuleIntf) {
        LynxI2cConfigureQueryResponse lynxI2cConfigureQueryResponse = new LynxI2cConfigureQueryResponse(lynxModuleIntf);
        lynxI2cConfigureQueryResponse.speedCode = LynxI2cConfigureChannelCommand.SpeedCode.UNKNOWN.bVal;
        return lynxI2cConfigureQueryResponse;
    }

    public LynxI2cConfigureChannelCommand.SpeedCode getSpeedCode() {
        return LynxI2cConfigureChannelCommand.SpeedCode.fromByte(this.speedCode);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.speedCode);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.speedCode = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
