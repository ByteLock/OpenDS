package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

public interface DcMotor extends DcMotorSimple {

    public enum ZeroPowerBehavior {
        UNKNOWN,
        BRAKE,
        FLOAT
    }

    DcMotorController getController();

    int getCurrentPosition();

    RunMode getMode();

    MotorConfigurationType getMotorType();

    int getPortNumber();

    boolean getPowerFloat();

    int getTargetPosition();

    ZeroPowerBehavior getZeroPowerBehavior();

    boolean isBusy();

    void setMode(RunMode runMode);

    void setMotorType(MotorConfigurationType motorConfigurationType);

    @Deprecated
    void setPowerFloat();

    void setTargetPosition(int i);

    void setZeroPowerBehavior(ZeroPowerBehavior zeroPowerBehavior);

    /* renamed from: com.qualcomm.robotcore.hardware.DcMotor$1 */
    static /* synthetic */ class C07201 {
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
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODERS     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODERS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RESET_ENCODERS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.DcMotor.C07201.<clinit>():void");
        }
    }

    public enum RunMode {
        RUN_WITHOUT_ENCODER,
        RUN_USING_ENCODER,
        RUN_TO_POSITION,
        STOP_AND_RESET_ENCODER,
        RUN_WITHOUT_ENCODERS,
        RUN_USING_ENCODERS,
        RESET_ENCODERS;

        @Deprecated
        public RunMode migrate() {
            int i = C07201.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[ordinal()];
            if (i == 1) {
                return RUN_WITHOUT_ENCODER;
            }
            if (i == 2) {
                return RUN_USING_ENCODER;
            }
            if (i != 3) {
                return this;
            }
            return STOP_AND_RESET_ENCODER;
        }

        public boolean isPIDMode() {
            return this == RUN_USING_ENCODER || this == RUN_USING_ENCODERS || this == RUN_TO_POSITION;
        }
    }
}
