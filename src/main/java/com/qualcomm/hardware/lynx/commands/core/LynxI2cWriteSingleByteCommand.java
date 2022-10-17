package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cWriteSingleByteCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 3;
    private byte bValue;
    private byte i2cAddr7Bit;
    private byte i2cBus;

    public LynxI2cWriteSingleByteCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cWriteSingleByteCommand(LynxModuleIntf lynxModuleIntf, int i, I2cAddr i2cAddr, int i2) {
        this(lynxModuleIntf);
        LynxConstants.validateI2cBusZ(i);
        this.i2cBus = (byte) i;
        this.i2cAddr7Bit = (byte) i2cAddr.get7Bit();
        this.bValue = (byte) i2;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        order.put(this.i2cAddr7Bit);
        order.put(this.bValue);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cBus = order.get();
        this.i2cAddr7Bit = order.get();
        this.bValue = order.get();
    }
}
