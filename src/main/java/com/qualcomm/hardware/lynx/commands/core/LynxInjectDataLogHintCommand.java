package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.firstinspires.inspection.InspectionState;

public class LynxInjectDataLogHintCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public static final int cbFixed = 1;
    public static final int cbMaxText = 100;
    public static final Charset charset = Charset.forName("UTF-8");
    private byte[] payload;

    public LynxInjectDataLogHintCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxInjectDataLogHintCommand(LynxModuleIntf lynxModuleIntf, String str) {
        this(lynxModuleIntf);
        setHintText(str);
    }

    public void setHintText(String str) {
        if (str.length() > 100) {
            str = str.substring(0, 100);
        }
        while (true) {
            byte[] bytes = str.getBytes(charset);
            this.payload = bytes;
            if (bytes.length > 100) {
                str = str.substring(0, str.length() - 1);
            } else {
                return;
            }
        }
    }

    public String getHintText() {
        byte[] bArr = this.payload;
        return bArr != null ? new String(bArr, charset) : InspectionState.NO_VERSION;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(this.payload.length + 1).order(LynxDatagram.LYNX_ENDIAN);
        order.put((byte) this.payload.length);
        order.put(this.payload);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        byte[] bArr2 = new byte[TypeConversion.unsignedByteToInt(order.get())];
        this.payload = bArr2;
        order.get(bArr2);
    }
}
