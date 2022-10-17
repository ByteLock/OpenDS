package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetPWMPulseWidthCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int apiPulseWidthFirst = 0;
    public static final int apiPulseWidthLast = 65535;
    public static final int cbPayload = 3;
    private byte channel;
    private short pulseWidth;

    public LynxSetPWMPulseWidthCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetPWMPulseWidthCommand(LynxModuleIntf lynxModuleIntf, int i, int i2) {
        this(lynxModuleIntf);
        LynxConstants.validatePwmChannelZ(i);
        if (i2 < 0 || i2 > 65535) {
            throw new IllegalArgumentException(String.format("illegal pulse width: %d", new Object[]{Integer.valueOf(i2)}));
        }
        this.channel = (byte) i;
        this.pulseWidth = (short) i2;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.channel);
        order.putShort(this.pulseWidth);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.channel = order.get();
        this.pulseWidth = order.getShort();
    }
}
