package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetServoConfigurationCommand extends LynxDekaInterfaceCommand<LynxGetServoConfigurationResponse> {
    public static final int cbPayload = 1;
    private byte channel;

    public LynxGetServoConfigurationCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetServoConfigurationResponse(lynxModuleIntf));
    }

    public LynxGetServoConfigurationCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        LynxConstants.validateServoChannelZ(i);
        this.channel = (byte) i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetServoConfigurationResponse.class;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(1).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.channel);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.channel = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN).get();
    }
}
