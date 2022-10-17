package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cWriteReadMultipleBytesCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 4;
    public static final int cbPayloadFirst = 1;
    public static final int cbPayloadLast = 100;
    private byte cbToRead;
    private byte i2cAddr7Bit;
    private byte i2cBus;
    private byte i2cStartAddr;

    public LynxI2cWriteReadMultipleBytesCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cWriteReadMultipleBytesCommand(LynxModuleIntf lynxModuleIntf, int i, I2cAddr i2cAddr, int i2, int i3) {
        this(lynxModuleIntf);
        LynxConstants.validateI2cBusZ(i);
        if (i3 < 1 || i3 > 100) {
            throw new IllegalArgumentException(String.format("illegal payload length: %d", new Object[]{Integer.valueOf(i3)}));
        }
        this.i2cBus = (byte) i;
        this.i2cAddr7Bit = (byte) i2cAddr.get7Bit();
        this.cbToRead = (byte) i3;
        this.i2cStartAddr = (byte) i2;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(4).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        order.put(this.i2cAddr7Bit);
        order.put(this.cbToRead);
        order.put(this.i2cStartAddr);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cBus = order.get();
        this.i2cAddr7Bit = order.get();
        this.cbToRead = order.get();
        this.i2cStartAddr = order.get();
    }
}
