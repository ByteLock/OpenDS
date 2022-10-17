package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetPWMConfigurationCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int apiFramePeriodFirst = 0;
    public static final int apiFramePeriodLast = 65535;
    public static final int cbPayload = 3;
    private byte channel;
    private short framePeriod;

    public LynxSetPWMConfigurationCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetPWMConfigurationCommand(LynxModuleIntf lynxModuleIntf, int i, int i2) {
        this(lynxModuleIntf);
        LynxConstants.validatePwmChannelZ(i);
        if (i2 < 0 || i2 > 65535) {
            throw new IllegalArgumentException(String.format("illegal frame period: %d", new Object[]{Integer.valueOf(i2)}));
        }
        this.channel = (byte) i;
        this.framePeriod = (short) i2;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.channel);
        order.putShort(this.framePeriod);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.channel = order.get();
        this.framePeriod = order.getShort();
    }
}
