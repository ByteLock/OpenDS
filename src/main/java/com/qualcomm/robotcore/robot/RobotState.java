package com.qualcomm.robotcore.robot;

import android.content.Context;
import com.qualcomm.robotcore.C0705R;

public enum RobotState {
    UNKNOWN(-1),
    NOT_STARTED(0),
    INIT(1),
    RUNNING(2),
    STOPPED(3),
    EMERGENCY_STOP(4);
    
    private int robotState;

    private RobotState(int i) {
        this.robotState = (byte) i;
    }

    public byte asByte() {
        return (byte) this.robotState;
    }

    public static RobotState fromByte(int i) {
        for (RobotState robotState2 : values()) {
            if (robotState2.robotState == i) {
                return robotState2;
            }
        }
        return UNKNOWN;
    }

    /* renamed from: com.qualcomm.robotcore.robot.RobotState$1 */
    static /* synthetic */ class C07481 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$robot$RobotState = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|14) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.robot.RobotState[] r0 = com.qualcomm.robotcore.robot.RobotState.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$robot$RobotState = r0
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotState     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.NOT_STARTED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotState     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.INIT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotState     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.RUNNING     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotState     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.STOPPED     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotState     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.robotcore.robot.RobotState r1 = com.qualcomm.robotcore.robot.RobotState.EMERGENCY_STOP     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.robot.RobotState.C07481.<clinit>():void");
        }
    }

    public String toString(Context context) {
        switch (C07481.$SwitchMap$com$qualcomm$robotcore$robot$RobotState[ordinal()]) {
            case 1:
                return context.getString(C0705R.string.robotStateUnknown);
            case 2:
                return context.getString(C0705R.string.robotStateNotStarted);
            case 3:
                return context.getString(C0705R.string.robotStateInit);
            case 4:
                return context.getString(C0705R.string.robotStateRunning);
            case 5:
                return context.getString(C0705R.string.robotStateStopped);
            case 6:
                return context.getString(C0705R.string.robotStateEmergencyStop);
            default:
                return context.getString(C0705R.string.robotStateInternalError);
        }
    }
}
