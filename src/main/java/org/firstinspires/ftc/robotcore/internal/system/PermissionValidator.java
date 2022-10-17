package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.List;

public final class PermissionValidator {
    private static final int SOME_RANDOM_NUMBER = 1;
    private static Activity activity;
    private final String TAG = "PermissionValidator";
    private List<String> asked;
    private PermissionListener listener;
    private PreferencesHelper preferencesHelper;

    private enum PermissionState {
        GRANTED,
        DENIED,
        PERMANENTLY_DENIED
    }

    public PermissionValidator(Activity activity2, PermissionListener permissionListener) {
        activity = activity2;
        this.listener = permissionListener;
        this.preferencesHelper = new PreferencesHelper("PermissionValidator");
        this.asked = new ArrayList();
    }

    /* access modifiers changed from: protected */
    public void requestPermission(String str) {
        ActivityCompat.requestPermissions(activity, new String[]{str}, 1);
        this.asked.add(str);
    }

    /* access modifiers changed from: protected */
    public PermissionState getPermissionState(String str) {
        if (ContextCompat.checkSelfPermission(activity, str) == 0) {
            this.preferencesHelper.writeBooleanPrefIfDifferent(str, false);
            return PermissionState.GRANTED;
        } else if (this.preferencesHelper.readBoolean(str, false)) {
            return PermissionState.PERMANENTLY_DENIED;
        } else {
            return PermissionState.DENIED;
        }
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.system.PermissionValidator$2 */
    static /* synthetic */ class C11892 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$system$PermissionValidator$PermissionState */
        static final /* synthetic */ int[] f279xc377418e;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.system.PermissionValidator$PermissionState[] r0 = org.firstinspires.ftc.robotcore.internal.system.PermissionValidator.PermissionState.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f279xc377418e = r0
                org.firstinspires.ftc.robotcore.internal.system.PermissionValidator$PermissionState r1 = org.firstinspires.ftc.robotcore.internal.system.PermissionValidator.PermissionState.GRANTED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f279xc377418e     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.system.PermissionValidator$PermissionState r1 = org.firstinspires.ftc.robotcore.internal.system.PermissionValidator.PermissionState.DENIED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f279xc377418e     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.system.PermissionValidator$PermissionState r1 = org.firstinspires.ftc.robotcore.internal.system.PermissionValidator.PermissionState.PERMANENTLY_DENIED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.system.PermissionValidator.C11892.<clinit>():void");
        }
    }

    public void checkPermission(String str) {
        RobotLog.m54ii("PermissionValidator", "Checking permission for " + str);
        int i = C11892.f279xc377418e[getPermissionState(str).ordinal()];
        if (i == 1) {
            RobotLog.m54ii("PermissionValidator", "    Granted: " + str);
            this.listener.onPermissionGranted(str);
        } else if (i == 2) {
            RobotLog.m54ii("PermissionValidator", "    Denied: " + str);
            requestPermission(str);
        } else if (i == 3) {
            RobotLog.m54ii("PermissionValidator", "    Permanently denied: " + str);
            this.listener.onPermissionPermanentlyDenied(str);
        }
    }

    public void explain(final String str) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setMessage(((PermissionValidatorActivity) activity).mapPermissionToExplanation(str));
        builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                PermissionValidator.this.checkPermission(str);
            }
        });
        builder.create();
        builder.show();
    }

    public void onRequestPermissionsResult(int i, String[] strArr, int[] iArr) {
        for (int i2 = 0; i2 < iArr.length; i2++) {
            if (iArr[i2] != 0) {
                RobotLog.m49ee("PermissionValidator", "You must grant permission to %s.", strArr[i2]);
                if (!ActivityCompat.shouldShowRequestPermissionRationale(activity, strArr[i2])) {
                    RobotLog.m48ee("PermissionValidator", "PR permanently denied: " + strArr[i2]);
                    this.preferencesHelper.writeBooleanPrefIfDifferent(strArr[i2], true);
                }
                this.listener.onPermissionDenied(strArr[i2]);
            } else {
                this.listener.onPermissionGranted(strArr[i2]);
            }
        }
    }
}
