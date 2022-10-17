package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxSetMotorPIDFControlLoopCoefficientsCommand extends LynxDekaInterfaceCommand<LynxAck> {
    private static final int cbPayload = 19;

    /* renamed from: d */
    private int f99d;

    /* renamed from: f */
    private int f100f;

    /* renamed from: i */
    private int f101i;
    private byte mode;
    private byte motor;
    private byte motorControlAlgorithm;

    /* renamed from: p */
    private int f102p;

    public enum InternalMotorControlAlgorithm {
        First(0),
        LegacyPID(0),
        PIDF(1),
        Max(2),
        NotSet(255);
        
        private byte value;

        public byte getValue() {
            return this.value;
        }

        private InternalMotorControlAlgorithm(int i) {
            this.value = (byte) i;
        }

        public static InternalMotorControlAlgorithm fromExternal(MotorControlAlgorithm motorControlAlgorithm) {
            int i = C07021.$SwitchMap$com$qualcomm$robotcore$hardware$MotorControlAlgorithm[motorControlAlgorithm.ordinal()];
            if (i == 1) {
                return LegacyPID;
            }
            if (i != 2) {
                return NotSet;
            }
            return PIDF;
        }

        public static InternalMotorControlAlgorithm fromByte(byte b) {
            InternalMotorControlAlgorithm internalMotorControlAlgorithm = LegacyPID;
            if (b == internalMotorControlAlgorithm.getValue()) {
                return internalMotorControlAlgorithm;
            }
            InternalMotorControlAlgorithm internalMotorControlAlgorithm2 = PIDF;
            if (b == internalMotorControlAlgorithm2.getValue()) {
                return internalMotorControlAlgorithm2;
            }
            return NotSet;
        }

        public MotorControlAlgorithm toExternal() {
            int i = C07021.f103x43fd9f57[ordinal()];
            if (i == 1) {
                return MotorControlAlgorithm.LegacyPID;
            }
            if (i != 2) {
                return MotorControlAlgorithm.Unknown;
            }
            return MotorControlAlgorithm.PIDF;
        }
    }

    public LynxSetMotorPIDFControlLoopCoefficientsCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public LynxSetMotorPIDFControlLoopCoefficientsCommand(LynxModuleIntf lynxModuleIntf, int i, DcMotor.RunMode runMode, int i2, int i3, int i4, int i5, InternalMotorControlAlgorithm internalMotorControlAlgorithm) {
        this(lynxModuleIntf);
        LynxConstants.validateMotorZ(i);
        this.motor = (byte) i;
        int i6 = C07021.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[runMode.ordinal()];
        if (i6 == 1) {
            this.mode = 1;
        } else if (i6 == 2) {
            this.mode = 2;
        } else {
            throw new IllegalArgumentException(String.format("illegal mode: %s", new Object[]{runMode.toString()}));
        }
        this.f102p = i2;
        this.f101i = i3;
        this.f99d = i4;
        this.f100f = i5;
        this.motorControlAlgorithm = internalMotorControlAlgorithm.getValue();
    }

    /* renamed from: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand$1 */
    static /* synthetic */ class C07021 {

        /* renamed from: $SwitchMap$com$qualcomm$hardware$lynx$commands$core$LynxSetMotorPIDFControlLoopCoefficientsCommand$InternalMotorControlAlgorithm */
        static final /* synthetic */ int[] f103x43fd9f57;
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode;
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$MotorControlAlgorithm;

        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x002e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0049 */
        static {
            /*
                com.qualcomm.robotcore.hardware.DcMotor$RunMode[] r0 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode = r0
                r1 = 1
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r2 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r3 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand$InternalMotorControlAlgorithm[] r2 = com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                f103x43fd9f57 = r2
                com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand$InternalMotorControlAlgorithm r3 = com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm.LegacyPID     // Catch:{ NoSuchFieldError -> 0x002e }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x002e }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x002e }
            L_0x002e:
                int[] r2 = f103x43fd9f57     // Catch:{ NoSuchFieldError -> 0x0038 }
                com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand$InternalMotorControlAlgorithm r3 = com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm.PIDF     // Catch:{ NoSuchFieldError -> 0x0038 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0038 }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0038 }
            L_0x0038:
                com.qualcomm.robotcore.hardware.MotorControlAlgorithm[] r2 = com.qualcomm.robotcore.hardware.MotorControlAlgorithm.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                $SwitchMap$com$qualcomm$robotcore$hardware$MotorControlAlgorithm = r2
                com.qualcomm.robotcore.hardware.MotorControlAlgorithm r3 = com.qualcomm.robotcore.hardware.MotorControlAlgorithm.LegacyPID     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r1 = $SwitchMap$com$qualcomm$robotcore$hardware$MotorControlAlgorithm     // Catch:{ NoSuchFieldError -> 0x0053 }
                com.qualcomm.robotcore.hardware.MotorControlAlgorithm r2 = com.qualcomm.robotcore.hardware.MotorControlAlgorithm.PIDF     // Catch:{ NoSuchFieldError -> 0x0053 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0053 }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x0053 }
            L_0x0053:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand.C07021.<clinit>():void");
        }
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(19).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.motor);
        order.put(this.mode);
        order.putInt(this.f102p);
        order.putInt(this.f101i);
        order.putInt(this.f99d);
        order.putInt(this.f100f);
        order.put(this.motorControlAlgorithm);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.motor = order.get();
        this.mode = order.get();
        this.f102p = order.getInt();
        this.f101i = order.getInt();
        this.f99d = order.getInt();
        this.f100f = order.getInt();
        this.motorControlAlgorithm = this.motorControlAlgorithm;
    }
}
