package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetMotorTargetVelocityCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int apiVelocityFirst = -32767;
    public static final int apiVelocityLast = 32767;
    public static final int cbPayload = 3;
    private byte motor;
    private short velocity;

    public LynxSetMotorTargetVelocityCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetMotorTargetVelocityCommand(LynxModuleIntf lynxModuleIntf, int i, int i2) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        if (i2 < -32767 || i2 > 32767) {
            throw new IllegalArgumentException(String.format("illegal velocity: %d", new Object[]{Integer.valueOf(i2)}));
        }
        this.motor = (byte) i;
        this.velocity = (short) i2;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.putShort(this.velocity);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.velocity = order.getShort();
    }
}
