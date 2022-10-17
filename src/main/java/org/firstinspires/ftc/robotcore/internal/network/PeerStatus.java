package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import com.qualcomm.robotcore.C0705R;

public enum PeerStatus {
    UNKNOWN(-1),
    DISCONNECTED(0),
    CONNECTED(1);
    
    public final byte bVal;

    private PeerStatus(int i) {
        this.bVal = (byte) i;
    }

    public static PeerStatus fromByte(byte b) {
        for (PeerStatus peerStatus : values()) {
            if (peerStatus.bVal == b) {
                return peerStatus;
            }
        }
        return UNKNOWN;
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.network.PeerStatus$1 */
    static /* synthetic */ class C10971 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$network$PeerStatus */
        static final /* synthetic */ int[] f269xbc7a286c = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.network.PeerStatus[] r0 = org.firstinspires.ftc.robotcore.internal.network.PeerStatus.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f269xbc7a286c = r0
                org.firstinspires.ftc.robotcore.internal.network.PeerStatus r1 = org.firstinspires.ftc.robotcore.internal.network.PeerStatus.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f269xbc7a286c     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.network.PeerStatus r1 = org.firstinspires.ftc.robotcore.internal.network.PeerStatus.CONNECTED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f269xbc7a286c     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.network.PeerStatus r1 = org.firstinspires.ftc.robotcore.internal.network.PeerStatus.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.PeerStatus.C10971.<clinit>():void");
        }
    }

    public String toString(Context context) {
        int i = C10971.f269xbc7a286c[ordinal()];
        if (i == 1) {
            return context.getString(C0705R.string.networkStatusUnknown);
        }
        if (i == 2) {
            return context.getString(C0705R.string.peerStatusConnected);
        }
        if (i != 3) {
            return context.getString(C0705R.string.networkStatusInternalError);
        }
        return context.getString(C0705R.string.peerStatusDisconnected);
    }
}
