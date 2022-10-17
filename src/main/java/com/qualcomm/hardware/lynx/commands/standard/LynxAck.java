package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxMessage;

public class LynxAck extends LynxMessage {
    private boolean isAttentionRequired;

    public static int getStandardCommandNumber() {
        return 32513;
    }

    public boolean isAck() {
        return true;
    }

    public LynxAck(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxAck(LynxModuleIntf lynxModuleIntf, boolean z) {
        super(lynxModuleIntf);
        this.isAttentionRequired = z;
    }

    public boolean isAttentionRequired() {
        return this.isAttentionRequired;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.isAttentionRequired};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        boolean z = false;
        if (bArr[0] != 0) {
            z = true;
        }
        this.isAttentionRequired = z;
    }
}
