package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cReadSingleByteCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 2;
    private byte i2cAddr7Bit;
    private byte i2cBus;

    public LynxI2cReadSingleByteCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cReadSingleByteCommand(LynxModuleIntf lynxModuleIntf, int i, I2cAddr i2cAddr) {
        this(lynxModuleIntf);
        LynxConstants.validateI2cBusZ(i);
        this.i2cBus = (byte) i;
        this.i2cAddr7Bit = (byte) i2cAddr.get7Bit();
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        order.put(this.i2cAddr7Bit);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cBus = order.get();
        this.i2cAddr7Bit = order.get();
    }
}
