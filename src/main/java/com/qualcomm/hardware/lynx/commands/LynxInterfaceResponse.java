package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModuleIntf;

public abstract class LynxInterfaceResponse extends LynxResponse {
    public abstract LynxInterface getInterface();

    public LynxInterfaceResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public int getCommandNumber() {
        LynxInterface lynxInterface = getInterface();
        if (lynxInterface == null) {
            return 0;
        }
        return 32768 | (lynxInterface.getBaseCommandNumber().intValue() + lynxInterface.getResponseIndex(getClass()));
    }
}
