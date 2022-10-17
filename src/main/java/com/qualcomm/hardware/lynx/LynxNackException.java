package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxRespondable;
import com.qualcomm.hardware.lynx.commands.standard.LynxNack;
import com.qualcomm.robotcore.exception.RobotCoreException;
import java.util.Locale;

public class LynxNackException extends Exception {
    private LynxRespondable command;

    public LynxNackException(LynxRespondable lynxRespondable, String str) {
        super(str);
        this.command = lynxRespondable;
    }

    public LynxNackException(LynxRespondable lynxRespondable, String str, Object... objArr) {
        super(String.format(Locale.getDefault(), "(%s #%d) %s", new Object[]{lynxRespondable.getModule().getSerialNumber(), Integer.valueOf(lynxRespondable.getModuleAddress()), String.format(Locale.getDefault(), str, objArr)}));
        this.command = lynxRespondable;
    }

    public RobotCoreException wrap() {
        return RobotCoreException.createChained(this, getMessage(), new Object[0]);
    }

    public LynxRespondable getCommand() {
        return this.command;
    }

    public LynxNack getNack() {
        return this.command.getNackReceived();
    }
}
