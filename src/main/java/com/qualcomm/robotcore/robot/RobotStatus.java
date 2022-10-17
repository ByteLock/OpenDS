package com.qualcomm.robotcore.robot;

import android.content.Context;
import com.qualcomm.robotcore.C0705R;
import org.firstinspires.inspection.InspectionState;

public enum RobotStatus {
    UNKNOWN,
    NONE,
    SCANNING_USB,
    ABORT_DUE_TO_INTERRUPT,
    WAITING_ON_WIFI,
    WAITING_ON_WIFI_DIRECT,
    WAITING_ON_NETWORK_CONNECTION,
    NETWORK_TIMED_OUT,
    STARTING_ROBOT,
    UNABLE_TO_START_ROBOT;

    /* renamed from: com.qualcomm.robotcore.robot.RobotStatus$1 */
    static /* synthetic */ class C07491 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|(3:17|18|20)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.robot.RobotStatus[] r0 = com.qualcomm.robotcore.robot.RobotStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus = r0
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.NONE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.SCANNING_USB     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.WAITING_ON_WIFI     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.WAITING_ON_WIFI_DIRECT     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.WAITING_ON_NETWORK_CONNECTION     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.NETWORK_TIMED_OUT     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.STARTING_ROBOT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$robot$RobotStatus     // Catch:{ NoSuchFieldError -> 0x006c }
                com.qualcomm.robotcore.robot.RobotStatus r1 = com.qualcomm.robotcore.robot.RobotStatus.UNABLE_TO_START_ROBOT     // Catch:{ NoSuchFieldError -> 0x006c }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r2 = 9
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.robot.RobotStatus.C07491.<clinit>():void");
        }
    }

    public String toString(Context context) {
        switch (C07491.$SwitchMap$com$qualcomm$robotcore$robot$RobotStatus[ordinal()]) {
            case 1:
                return context.getString(C0705R.string.robotStatusUnknown);
            case 2:
                return InspectionState.NO_VERSION;
            case 3:
                return context.getString(C0705R.string.robotStatusScanningUSB);
            case 4:
                return context.getString(C0705R.string.robotStatusWaitingOnWifi);
            case 5:
                return context.getString(C0705R.string.robotStatusWaitingOnWifiDirect);
            case 6:
                return context.getString(C0705R.string.robotStatusWaitingOnNetworkConnection);
            case 7:
                return context.getString(C0705R.string.robotStatusNetworkTimedOut);
            case 8:
                return context.getString(C0705R.string.robotStatusStartingRobot);
            case 9:
                return context.getString(C0705R.string.robotStatusUnableToStartRobot);
            default:
                return context.getString(C0705R.string.robotStatusInternalError);
        }
    }
}
