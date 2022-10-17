package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxInterface;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;

public abstract class LynxDekaInterfaceResponse extends LynxInterfaceResponse {
    public LynxDekaInterfaceResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxInterface getInterface() {
        return this.module.getInterface(LynxDekaInterfaceCommand.dekaInterfaceName);
    }
}
