package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.util.TypeConversion;
import com.qualcomm.robotcore.util.Util;
import java.nio.ByteBuffer;

public class LynxQueryInterfaceResponse extends LynxStandardResponse {
    short commandNumberFirst = 0;
    short numberOfCommands = 0;

    public LynxQueryInterfaceResponse(LynxModule lynxModule) {
        super(lynxModule);
    }

    public int getCommandNumberFirst() {
        return TypeConversion.unsignedShortToInt(this.commandNumberFirst);
    }

    public int getNumberOfCommands() {
        return TypeConversion.unsignedShortToInt(this.numberOfCommands);
    }

    public static int getStandardCommandNumber() {
        return LynxQueryInterfaceCommand.getStandardCommandNumber() | 32768;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return Util.concatenateByteArrays(TypeConversion.shortToByteArray(this.commandNumberFirst, LynxDatagram.LYNX_ENDIAN), TypeConversion.shortToByteArray(this.numberOfCommands, LynxDatagram.LYNX_ENDIAN));
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        wrap.order(LynxDatagram.LYNX_ENDIAN);
        this.commandNumberFirst = wrap.getShort();
        this.numberOfCommands = wrap.getShort();
    }
}
