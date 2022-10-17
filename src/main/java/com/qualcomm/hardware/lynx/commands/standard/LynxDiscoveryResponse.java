package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.util.TypeConversion;

public class LynxDiscoveryResponse extends LynxStandardResponse {
    byte parentIndicator;

    public boolean isAckable() {
        return false;
    }

    public LynxDiscoveryResponse() {
        super((LynxModule) null);
    }

    public boolean isParent() {
        return !isChild();
    }

    public boolean isChild() {
        return getParentIndicator() == 0;
    }

    public int getParentIndicator() {
        return TypeConversion.unsignedByteToInt(this.parentIndicator);
    }

    public int getDiscoveredModuleAddress() {
        return this.serialization.getSourceModuleAddress();
    }

    public static int getStandardCommandNumber() {
        return LynxDiscoveryCommand.getStandardCommandNumber() | 32768;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.parentIndicator};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.parentIndicator = bArr[0];
    }
}
