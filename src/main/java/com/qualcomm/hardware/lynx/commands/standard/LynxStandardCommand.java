package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.LynxMessage;

public abstract class LynxStandardCommand<RESPONSE extends LynxMessage> extends LynxCommand<RESPONSE> {
    public static final int COMMAND_NUMBER_ACK = 32513;
    public static final int COMMAND_NUMBER_DEBUG_LOG_LEVEL = 32526;
    public static final int COMMAND_NUMBER_DISCOVERY = 32527;
    public static final int COMMAND_NUMBER_DOWNLOAD_CHUNK = 32521;
    public static final int COMMAND_NUMBER_FAIL_SAFE = 32517;
    public static final int COMMAND_NUMBER_FIRST = 32513;
    public static final int COMMAND_NUMBER_GET_MODULE_LED_COLOR = 32523;
    public static final int COMMAND_NUMBER_GET_MODULE_LED_PATTERN = 32525;
    public static final int COMMAND_NUMBER_GET_MODULE_STATUS = 32515;
    public static final int COMMAND_NUMBER_KEEP_ALIVE = 32516;
    public static final int COMMAND_NUMBER_LAST = 32527;
    public static final int COMMAND_NUMBER_NACK = 32514;
    public static final int COMMAND_NUMBER_QUERY_INTERFACE = 32519;
    public static final int COMMAND_NUMBER_SET_MODULE_LED_COLOR = 32522;
    public static final int COMMAND_NUMBER_SET_MODULE_LED_PATTERN = 32524;
    public static final int COMMAND_NUMBER_SET_NEW_MODULE_ADDRESS = 32518;
    public static final int COMMAND_NUMBER_START_DOWNLOAD = 32520;

    public static boolean isStandardCommandNumber(int i) {
        return 32513 <= i && i <= 32527;
    }

    public static boolean isStandardPacketId(int i) {
        return isStandardCommandNumber(i) || isStandardResponseNumber(i);
    }

    public static boolean isStandardResponseNumber(int i) {
        return (32768 & i) != 0 && isStandardCommandNumber(i & -32769);
    }

    public LynxStandardCommand(LynxModule lynxModule) {
        super(lynxModule);
    }

    public LynxStandardCommand(LynxModule lynxModule, RESPONSE response) {
        super(lynxModule, response);
    }
}
