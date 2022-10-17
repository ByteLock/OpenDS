package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetDIODirectionCommand extends LynxDekaInterfaceCommand<LynxGetDIODirectionResponse> {
    private static final int cbPayload = 1;
    private int pin;

    public LynxGetDIODirectionCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetDIODirectionResponse(lynxModuleIntf));
    }

    public LynxGetDIODirectionCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        LynxConstants.validateDigitalIOZ(i);
        this.pin = i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetDIODirectionResponse.class;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put((byte) this.pin);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.pin = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
