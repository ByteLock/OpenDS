package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxI2cConfigureChannelCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbPayload = 2;
    private byte i2cBus;
    private byte speedCode;

    public enum SpeedCode {
        UNKNOWN(-1),
        STANDARD_100K(0),
        FAST_400K(1),
        FASTPLUS_1M(2),
        HIGH_3_4M(3);
        
        public byte bVal;

        private SpeedCode(int i) {
            this.bVal = (byte) i;
        }

        public static SpeedCode fromByte(int i) {
            for (SpeedCode speedCode : values()) {
                if (speedCode.bVal == i) {
                    return speedCode;
                }
            }
            return UNKNOWN;
        }
    }

    public LynxI2cConfigureChannelCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxI2cConfigureChannelCommand(LynxModuleIntf lynxModuleIntf, int i, SpeedCode speedCode2) {
        this(lynxModuleIntf);
        LynxConstants.validateI2cBusZ(i);
        this.i2cBus = (byte) i;
        this.speedCode = speedCode2.bVal;
    }

    public SpeedCode getSpeedCode() {
        return SpeedCode.fromByte(this.speedCode);
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.i2cBus);
        order.put(this.speedCode);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.i2cBus = order.get();
        this.speedCode = order.get();
    }
}
