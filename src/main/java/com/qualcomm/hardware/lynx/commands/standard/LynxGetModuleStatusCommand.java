package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxResponse;

public class LynxGetModuleStatusCommand extends LynxStandardCommand<LynxGetModuleStatusResponse> {
    private boolean clearStatusAfterResponse;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_GET_MODULE_STATUS;
    }

    public LynxGetModuleStatusCommand(LynxModule lynxModule) {
        super(lynxModule, new LynxGetModuleStatusResponse(lynxModule));
    }

    public LynxGetModuleStatusCommand(LynxModule lynxModule, boolean z) {
        this(lynxModule);
        this.clearStatusAfterResponse = z;
    }

    public boolean getClearStatusAfterResponse() {
        return this.clearStatusAfterResponse;
    }

    public static Class<? extends LynxResponse> getResponseClass() {
        return LynxGetModuleStatusResponse.class;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.clearStatusAfterResponse};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        boolean z = false;
        if (bArr[0] != 0) {
            z = true;
        }
        this.clearStatusAfterResponse = z;
    }
}
