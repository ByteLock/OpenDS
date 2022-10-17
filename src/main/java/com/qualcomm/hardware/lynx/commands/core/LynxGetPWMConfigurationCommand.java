package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetPWMConfigurationCommand extends LynxDekaInterfaceCommand<LynxGetPWMConfigurationResponse> {
    public static final int cbPayload = 1;
    private byte channel;

    public LynxGetPWMConfigurationCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetPWMConfigurationResponse(lynxModuleIntf));
    }

    public LynxGetPWMConfigurationCommand(LynxModuleIntf lynxModuleIntf, int i) {
        this(lynxModuleIntf);
        LynxConstants.validatePwmChannelZ(i);
        this.channel = (byte) i;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetPWMConfigurationResponse.class;
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
