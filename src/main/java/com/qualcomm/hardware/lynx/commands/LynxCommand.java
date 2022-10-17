package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import java.lang.reflect.InvocationTargetException;

public abstract class LynxCommand<RESPONSE extends LynxMessage> extends LynxRespondable<RESPONSE> {
    public LynxCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxCommand(LynxModuleIntf lynxModuleIntf, RESPONSE response) {
        super(lynxModuleIntf, response);
    }

    public static Class<? extends LynxResponse> getResponseClass(Class cls) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        return (Class) invokeStaticNullaryMethod(cls, "getResponseClass");
    }
}
