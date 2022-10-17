package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxMessage;

public abstract class LynxInterfaceCommand<RESPONSE extends LynxMessage> extends LynxCommand<RESPONSE> {
    public abstract LynxInterface getInterface();

    public LynxInterfaceCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxInterfaceCommand(LynxModuleIntf lynxModuleIntf, RESPONSE response) {
        super(lynxModuleIntf, response);
    }

    public int getCommandNumber() {
        LynxInterface lynxInterface = getInterface();
        if (lynxInterface == null) {
            return 0;
        }
        return lynxInterface.getBaseCommandNumber().intValue() + lynxInterface.getCommandIndex(getClass());
    }
}
