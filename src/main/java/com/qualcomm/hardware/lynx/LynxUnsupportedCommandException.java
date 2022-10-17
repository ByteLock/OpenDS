package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxMessage;

public class LynxUnsupportedCommandException extends Exception {
    private Class<? extends LynxMessage> clazz;
    private int commandNumber;
    private LynxModule lynxModule;

    public LynxUnsupportedCommandException(LynxModule lynxModule2, LynxMessage lynxMessage) {
        this.lynxModule = lynxModule2;
        this.commandNumber = lynxMessage.getCommandNumber();
        this.clazz = lynxMessage.getClass();
    }

    public int getCommandNumber() {
        return this.commandNumber;
    }

    public Class<? extends LynxMessage> getClazz() {
        return this.clazz;
    }

    public LynxModuleIntf getLynxModule() {
        return this.lynxModule;
    }
}
