package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetMotorTargetPositionCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int apiToleranceFirst = 0;
    public static final int apiToleranceLast = 65535;
    public static final int cbPayload = 7;
    private byte motor;
    private int target;
    private short tolerance;

    public LynxSetMotorTargetPositionCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetMotorTargetPositionCommand(LynxModuleIntf lynxModuleIntf, int i, int i2, int i3) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        if (i3 < 0 || i3 > 65535) {
            throw new IllegalArgumentException(String.format("illegal tolerance: %d", new Object[]{Integer.valueOf(i3)}));
        }
        this.motor = (byte) i;
        this.target = i2;
        this.tolerance = (short) i3;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(7).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.putInt(this.target);
        order.putShort(this.tolerance);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.target = order.getInt();
        this.tolerance = order.getShort();
    }
}
