package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import com.qualcomm.robotcore.C0705R;

public enum NetworkStatus {
    UNKNOWN,
    INACTIVE,
    ACTIVE,
    ENABLED,
    ERROR,
    CREATED_AP_CONNECTION;

    /* renamed from: org.firstinspires.ftc.robotcore.internal.network.NetworkStatus$1 */
    static /* synthetic */ class C10961 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$network$NetworkStatus */
        static final /* synthetic */ int[] f268x2dc93c48 = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|14) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus[] r0 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f268x2dc93c48 = r0
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f268x2dc93c48     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.ACTIVE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f268x2dc93c48     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.INACTIVE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f268x2dc93c48     // Catch:{ NoSuchFieldError -> 0x0033 }
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.ENABLED     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f268x2dc93c48     // Catch:{ NoSuchFieldError -> 0x003e }
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.ERROR     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = f268x2dc93c48     // Catch:{ NoSuchFieldError -> 0x0049 }
                org.firstinspires.ftc.robotcore.internal.network.NetworkStatus r1 = org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.CREATED_AP_CONNECTION     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.NetworkStatus.C10961.<clinit>():void");
        }
    }

    public String toString(Context context, Object... objArr) {
        switch (C10961.f268x2dc93c48[ordinal()]) {
            case 1:
                return context.getString(C0705R.string.networkStatusUnknown);
            case 2:
                return context.getString(C0705R.string.networkStatusActive);
            case 3:
                return context.getString(C0705R.string.networkStatusInactive);
            case 4:
                return context.getString(C0705R.string.networkStatusEnabled);
            case 5:
                return context.getString(C0705R.string.networkStatusError);
            case 6:
                return String.format(context.getString(C0705R.string.networkStatusCreatedAPConnection), objArr);
            default:
                return context.getString(C0705R.string.networkStatusInternalError);
        }
    }
}
