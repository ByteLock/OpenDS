package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;
import java.util.Arrays;

public class LynxI2cWriteMultipleBytesCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbFixed = 3;
    public static final int cbPayloadFirst = 1;
    public static final int cbPayloadLast = 100;
    private byte i2cAddr7Bit;
    private byte i2cBus;
    private byte[] payload;

    public LynxI2cWriteMultipleBytesCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cWriteMultipleBytesCommand(LynxModuleIntf lynxModuleIntf, int i, I2cAddr i2cAddr, byte[] bArr) {
        this(lynxModuleIntf);
        LynxConstants.validateI2cBusZ(i);
        if (bArr.length < 1 || bArr.length > 100) {
            throw new IllegalArgumentException(String.format("illegal payload length: %d", new Object[]{Integer.valueOf(bArr.length)}));
        }
        this.i2cBus = (byte) i;
        this.i2cAddr7Bit = (byte) i2cAddr.get7Bit();
        this.payload = Arrays.copyOf(bArr, bArr.length);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(this.payload.length + 3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        order.put(this.i2cAddr7Bit);
        order.put((byte) this.payload.length);
        order.put(this.payload);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cBus = order.get();
        this.i2cAddr7Bit = order.get();
        byte[] bArr2 = new byte[TypeConversion.unsignedByteToInt(order.get())];
        this.payload = bArr2;
        order.get(bArr2);
    }
}
