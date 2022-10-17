package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetDIODirectionCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public final int cbPayload;
    private int direction;
    private int pin;

    public LynxSetDIODirectionCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 2;
    }

    public LynxSetDIODirectionCommand(LynxModuleIntf lynxModuleIntf, int i, DigitalChannel.Mode mode) {
        this(lynxModuleIntf);
        LynxConstants.validateDigitalIOZ(i);
        this.pin = i;
        this.direction = mode == DigitalChannel.Mode.INPUT ? 0 : 1;
    }

    public int getPin() {
        return this.pin;
    }

    public DigitalChannel.Mode getMode() {
        return this.direction == 0 ? DigitalChannel.Mode.INPUT : DigitalChannel.Mode.OUTPUT;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put((byte) this.pin);
        order.put((byte) this.direction);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.pin = order.get();
        this.direction = order.get();
    }
}
