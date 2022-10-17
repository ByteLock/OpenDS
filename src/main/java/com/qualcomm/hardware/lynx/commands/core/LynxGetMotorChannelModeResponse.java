package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.hardware.DcMotor;
import java.nio.ByteBuffer;

public class LynxGetMotorChannelModeResponse extends LynxDekaInterfaceResponse {
    private static final int cbPayload = 2;
    private byte floatAtZero;
    private byte mode;

    public LynxGetMotorChannelModeResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public DcMotor.RunMode getMode() {
        byte b = this.mode;
        if (b == 0) {
            return DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        }
        if (b == 1) {
            return DcMotor.RunMode.RUN_USING_ENCODER;
        }
        if (b == 2) {
            return DcMotor.RunMode.RUN_TO_POSITION;
        }
        throw new IllegalStateException(String.format("illegal mode byte: 0x%02x", new Object[]{Byte.valueOf(this.mode)}));
    }

    public DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
        return this.floatAtZero == 0 ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.mode);
        order.put(this.floatAtZero);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.mode = order.get();
        this.floatAtZero = order.get();
    }
}
