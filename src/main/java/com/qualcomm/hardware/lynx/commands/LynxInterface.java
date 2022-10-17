package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModule;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.system.Assert;

public class LynxInterface {
    public static final int ERRONEOUS_COMMAND_NUMBER = 0;
    public static final int ERRONEOUS_INDEX = 0;
    private Integer baseCommandNumber = 0;
    private Map<Class<? extends LynxInterfaceCommand>, Integer> commandIndices;
    private Class<? extends LynxInterfaceCommand>[] commands;
    private String interfaceName;
    private Map<Class<? extends LynxInterfaceResponse>, Integer> responseIndices;
    private boolean wasNacked;

    public LynxInterface(String str, Class<? extends LynxInterfaceCommand>... clsArr) {
        this.interfaceName = str;
        this.commands = clsArr;
        this.commandIndices = new HashMap();
        this.responseIndices = new HashMap();
        this.wasNacked = false;
        int i = 0;
        while (true) {
            Class<? extends LynxInterfaceCommand>[] clsArr2 = this.commands;
            if (i < clsArr2.length) {
                Class<? extends LynxInterfaceCommand> cls = clsArr2[i];
                if (cls != null) {
                    this.commandIndices.put(cls, Integer.valueOf(i));
                    try {
                        Class<? extends LynxResponse> responseClass = LynxCommand.getResponseClass(cls);
                        Assert.assertTrue(responseClass != null);
                        this.responseIndices.put(responseClass, Integer.valueOf(i));
                        LynxModule.correlateResponse(cls, responseClass);
                    } catch (Exception unused) {
                    }
                }
                i++;
            } else {
                return;
            }
        }
    }

    public String getInterfaceName() {
        return this.interfaceName;
    }

    public int getCommandCount() {
        return this.commands.length;
    }

    public void setBaseCommandNumber(Integer num) {
        this.baseCommandNumber = num;
    }

    public void setWasNacked(boolean z) {
        this.wasNacked = z;
    }

    public boolean wasNacked() {
        return this.wasNacked;
    }

    public Integer getBaseCommandNumber() {
        return this.baseCommandNumber;
    }

    public int getCommandIndex(Class<? extends LynxInterfaceCommand> cls) {
        return this.commandIndices.get(cls).intValue();
    }

    public int getResponseIndex(Class<? extends LynxInterfaceResponse> cls) {
        return this.responseIndices.get(cls).intValue();
    }

    public List<Class<? extends LynxInterfaceCommand>> getCommandClasses() {
        return Arrays.asList(this.commands);
    }
}
