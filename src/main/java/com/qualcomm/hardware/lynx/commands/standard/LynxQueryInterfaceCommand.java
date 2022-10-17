package com.qualcomm.hardware.lynx.commands.standard;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.LynxResponse;
import java.nio.charset.Charset;
import org.firstinspires.ftc.robotcore.internal.android.dex.DexFormat;

public class LynxQueryInterfaceCommand extends LynxStandardCommand<LynxQueryInterfaceResponse> {
    private String interfaceName;

    public static int getStandardCommandNumber() {
        return LynxStandardCommand.COMMAND_NUMBER_QUERY_INTERFACE;
    }

    public LynxQueryInterfaceCommand(LynxModule lynxModule) {
        super(lynxModule, new LynxQueryInterfaceResponse(lynxModule));
    }

    public LynxQueryInterfaceCommand(LynxModule lynxModule, String str) {
        this(lynxModule);
        this.interfaceName = str;
    }

    /* access modifiers changed from: package-private */
    public void setInterfaceName(String str) {
        this.interfaceName = str;
        if (str != null && str.length() > 0) {
            String str2 = this.interfaceName;
            if (str2.charAt(str2.length() - 1) == 0) {
                String str3 = this.interfaceName;
                this.interfaceName = str3.substring(0, str3.length() - 1);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public String getInterfaceName() {
        return this.interfaceName;
    }

    /* access modifiers changed from: package-private */
    public String getNullTerminatedInterfaceName() {
        return getInterfaceName() + DexFormat.MAGIC_SUFFIX;
    }

    public static Class<? extends LynxResponse> getResponseClass() {
        return LynxQueryInterfaceResponse.class;
    }

    public int getCommandNumber() {
        return getStandardCommandNumber();
    }

    public byte[] toPayloadByteArray() {
        return getNullTerminatedInterfaceName().getBytes(Charset.forName("UTF-8"));
    }

    public void fromPayloadByteArray(byte[] bArr) {
        setInterfaceName(new String(bArr, Charset.forName("UTF-8")));
    }
}
