package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;

public class LynxKeepAliveCommand extends LynxStandardCommand<LynxAck> {
    boolean initialPing;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_KEEP_ALIVE;
    }

    public void fromPayloadByteArray(byte[] bArr) {
    }

    /* access modifiers changed from: protected */
    public int getMsAwaitInterval() {
        return 500;
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxKeepAliveCommand(LynxModule lynxModule, boolean z) {
        super(lynxModule);
        this.initialPing = z;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    /* access modifiers changed from: protected */
    public void noteAttentionRequired() {
        if (!this.initialPing) {
            super.noteAttentionRequired();
        }
    }
}
