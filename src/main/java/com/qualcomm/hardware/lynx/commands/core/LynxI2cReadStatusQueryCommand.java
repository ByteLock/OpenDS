package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cReadStatusQueryCommand extends LynxDekaInterfaceCommand<LynxI2cReadStatusQueryResponse> {
    public static final int cbPayload = 1;
    private byte i2cBus;

    public LynxI2cReadStatusQueryCommand(LynxModuleIntf lynxModuleIntf, int i, int i2) {
        super(lynxModuleIntf, new LynxI2cReadStatusQueryResponse(lynxModuleIntf, i2));
        LynxConstants.validateI2cBusZ(i);
        this.i2cBus = (byte) i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxI2cReadStatusQueryResponse.class;
    }

    public void onResponseReceived(LynxMessage lynxMessage) {
        super.onResponseReceived(lynxMessage);
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
