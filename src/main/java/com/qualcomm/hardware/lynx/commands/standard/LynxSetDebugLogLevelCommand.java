package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;

public class LynxSetDebugLogLevelCommand extends LynxStandardCommand<LynxAck> {
    LynxModule.DebugGroup debugGroup;
    LynxModule.DebugVerbosity verbosity;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_DEBUG_LOG_LEVEL;
    }

    public LynxSetDebugLogLevelCommand(LynxModule lynxModule) {
        super(lynxModule);
        this.debugGroup = LynxModule.DebugGroup.MAIN;
        this.verbosity = LynxModule.DebugVerbosity.OFF;
    }

    public LynxSetDebugLogLevelCommand(LynxModule lynxModule, LynxModule.DebugGroup debugGroup2, LynxModule.DebugVerbosity debugVerbosity) {
        this(lynxModule);
        this.debugGroup = debugGroup2;
        this.verbosity = debugVerbosity;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return new byte[]{this.debugGroup.bVal, this.verbosity.bVal};
    }

    public void fromPayloadByteArray(byte[] bArr) {
        this.debugGroup = LynxModule.DebugGroup.fromInt(bArr[0]);
        this.verbosity = LynxModule.DebugVerbosity.fromInt(bArr[1]);
    }
}
