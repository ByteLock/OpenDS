package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetMotorChannelModeCommand extends LynxDekaInterfaceCommand<LynxAck> {
    public final int cbPayload;
    private byte floatAtZero;
    private byte mode;
    private byte motor;

    public LynxSetMotorChannelModeCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.cbPayload = 3;
    }

    public LynxSetMotorChannelModeCommand(LynxModuleIntf lynxModuleIntf, int i, DcMotor.RunMode runMode, DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
        int i2 = C07001.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[runMode.ordinal()];
        byte b = 0;
        if (i2 == 1) {
            this.mode = 0;
        } else if (i2 == 2) {
            this.mode = 1;
        } else if (i2 == 3) {
            this.mode = 2;
        } else {
            throw new IllegalArgumentException(String.format("illegal mode %s", new Object[]{runMode.toString()}));
        }
        this.floatAtZero = zeroPowerBehavior != DcMotor.ZeroPowerBehavior.BRAKE ? 1 : b;
    }

    /* renamed from: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand$1 */
    static /* synthetic */ class C07001 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.robotcore.hardware.DcMotor$RunMode[] r0 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode = r0
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand.C07001.<clinit>():void");
        }
    }

    public DcMotor.RunMode getMode() {
        byte b = this.mode;
        if (b == 1) {
            return DcMotor.RunMode.RUN_USING_ENCODER;
        }
        if (b != 2) {
            return DcMotor.RunMode.RUN_WITHOUT_ENCODER;
        }
        return DcMotor.RunMode.RUN_TO_POSITION;
    }

    public DcMotor.ZeroPowerBehavior getZeroPowerBehavior() {
        return this.floatAtZero == 0 ? DcMotor.ZeroPowerBehavior.BRAKE : DcMotor.ZeroPowerBehavior.FLOAT;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(3).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.put(this.mode);
        order.put(this.floatAtZero);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.mode = order.get();
        this.floatAtZero = order.get();
    }
}
