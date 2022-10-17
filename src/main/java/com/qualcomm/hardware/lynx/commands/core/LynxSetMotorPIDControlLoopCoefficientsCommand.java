package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetMotorPIDControlLoopCoefficientsCommand extends LynxDekaInterfaceCommand<LynxAck> {
    private static final int cbPayload = 14;

    /* renamed from: d */
    private int f96d;

    /* renamed from: i */
    private int f97i;
    private byte mode;
    private byte motor;

    /* renamed from: p */
    private int f98p;

    public static double externalCoefficientFromInternal(int i) {
        return ((double) i) / 65536.0d;
    }

    public LynxSetMotorPIDControlLoopCoefficientsCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetMotorPIDControlLoopCoefficientsCommand(LynxModuleIntf lynxModuleIntf, int i, DcMotor.RunMode runMode, int i2, int i3, int i4) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
        int i5 = C07011.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[runMode.ordinal()];
        if (i5 == 1) {
            this.mode = 1;
        } else if (i5 == 2) {
            this.mode = 2;
        } else {
            throw new IllegalArgumentException(String.format("illegal mode: %s", new Object[]{runMode.toString()}));
        }
        this.f98p = i2;
        this.f97i = i3;
        this.f96d = i4;
    }

    /* renamed from: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDControlLoopCoefficientsCommand$1 */
    static /* synthetic */ class C07011 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.qualcomm.robotcore.hardware.DcMotor$RunMode[] r0 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode = r0
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDControlLoopCoefficientsCommand.C07011.<clinit>():void");
        }
    }

    public static int internalCoefficientFromExternal(double d) {
        return ((int) ((Math.abs(d) * 65536.0d) + 0.5d)) * ((int) Math.signum(d));
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(14).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.put(this.mode);
        order.putInt(this.f98p);
        order.putInt(this.f97i);
        order.putInt(this.f96d);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.mode = order.get();
        this.f98p = order.getInt();
        this.f97i = order.getInt();
        this.f96d = order.getInt();
    }
}
