package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.LynxUnsupportedCommandException;

public class LynxDiscoveryCommand extends LynxStandardCommand<LynxAck> {
    public static int getStandardCommandNumber() {
        return 32527;
    }

    public void acquireNetworkLock() throws InterruptedException {
    }

    public void fromPayloadByteArray(byte[] bArr) {
    }

    public int getDestModuleAddress() {
        return 255;
    }

    public boolean isAckable() {
        return false;
    }

    /* access modifiers changed from: protected */
    public void noteAttentionRequired() {
    }

    public void releaseNetworkLock() throws InterruptedException {
    }

    public byte[] toPayloadByteArray() {
        return new byte[0];
    }

    public LynxDiscoveryCommand(LynxModule lynxModule) {
        super(lynxModule);
    }

    public void send() throws LynxNackException, InterruptedException {
        try {
            this.module.sendCommand(this);
        } catch (LynxUnsupportedCommandException e) {
            throwNackForUnsupportedCommand(e);
        }
    }

    public LynxAck sendReceive() throws LynxNackException, InterruptedException {
        try {
            this.module.sendCommand(this);
            return null;
        } catch (LynxUnsupportedCommandException e) {
            throwNackForUnsupportedCommand(e);
            return null;
        }
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }
}
