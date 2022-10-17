package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxRespondable;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxResetMotorEncoderCommand extends LynxDekaInterfaceCommand<LynxRespondable> {
    public final int cbPayload;
    private byte motor;

    public LynxResetMotorEncoderCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 1;
    }

    public LynxResetMotorEncoderCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
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
