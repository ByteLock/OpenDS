package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetPWMEnableCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 2;
    private byte channel;
    private byte enable;

    public LynxSetPWMEnableCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetPWMEnableCommand(LynxModuleIntf lynxModuleIntf, int i, boolean z) {
        this(lynxModuleIntf);
        LynxConstants.validatePwmChannelZ(i);
        this.channel = (byte) i;
        this.enable = z ? (byte) 1 : 0;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.channel);
        order.put(this.enable);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.channel = order.get();
        this.enable = order.get();
    }
}
