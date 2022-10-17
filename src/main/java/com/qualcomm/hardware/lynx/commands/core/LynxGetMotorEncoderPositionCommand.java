package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetMotorEncoderPositionCommand extends LynxDekaInterfaceCommand<LynxGetMotorEncoderPositionResponse> {
    private static final int cbPayload = 1;
    private byte motor;

    public LynxGetMotorEncoderPositionCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetMotorEncoderPositionResponse(lynxModuleIntf));
    }

    public LynxGetMotorEncoderPositionCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetMotorEncoderPositionResponse.class;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.motor = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
