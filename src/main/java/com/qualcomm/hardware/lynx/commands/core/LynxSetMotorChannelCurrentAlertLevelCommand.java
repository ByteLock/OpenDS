package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;

public class LynxSetMotorChannelCurrentAlertLevelCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public final int cbPayload;
    private short mACurrentLimit;
    private byte motor;

    public LynxSetMotorChannelCurrentAlertLevelCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 3;
    }

    public LynxSetMotorChannelCurrentAlertLevelCommand(LynxModuleIntf lynxModuleIntf, int i, int i2) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
        short s = (short) i2;
        this.mACurrentLimit = s;
        if (TypeConversion.unsignedShortToInt(s) != i2) {
            throw new IllegalArgumentException(String.format("illegal current limit: %d mA", new Object[]{Integer.valueOf(i2)}));
        }
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.putShort(this.mACurrentLimit);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.mACurrentLimit = order.getShort();
    }
}
