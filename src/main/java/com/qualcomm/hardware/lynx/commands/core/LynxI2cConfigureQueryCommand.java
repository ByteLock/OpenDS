package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cConfigureQueryCommand extends LynxDekaInterfaceCommand<LynxI2cConfigureQueryResponse> {
    public static final int cbPayload = 1;
    private byte i2cBus;

    public LynxI2cConfigureQueryCommand(LynxModuleIntf lynxModuleIntf, int i) {
        super(lynxModuleIntf, LynxI2cConfigureQueryResponse.createDefaultResponse(lynxModuleIntf));
        LynxConstants.validateI2cBusZ(i);
        this.i2cBus = (byte) i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxI2cConfigureQueryResponse.class;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.i2cBus = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
