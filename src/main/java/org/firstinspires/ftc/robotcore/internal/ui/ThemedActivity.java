package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.app.Activity;
import android.content.Context;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Handler;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.ThemedActivity */
public abstract class ThemedActivity extends BaseActivity {
    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        appAppThemeToActivity(getTag(), this);
        super.onCreate(bundle);
    }

    public static void appAppThemeToActivity(String str, Activity activity) {
        boolean z;
        PreferencesHelper preferencesHelper = new PreferencesHelper(str, (Context) activity);
        String string = activity.getString(C0705R.string.pref_app_theme);
        String readString = preferencesHelper.readString(string, activity.getString(C0705R.string.tokenThemeRed));
        preferencesHelper.writePrefIfDifferent(string, readString);
        String[] stringArray = activity.getResources().getStringArray(C0705R.array.app_theme_tokens);
        TypedArray obtainTypedArray = activity.getResources().obtainTypedArray(C0705R.array.app_theme_ids);
        int i = 0;
        while (true) {
            if (i >= stringArray.length) {
                z = false;
                break;
            } else if (stringArray[i].equals(readString)) {
                activity.setTheme(obtainTypedArray.getResourceId(i, 0));
                z = true;
                break;
            } else {
                i++;
            }
        }
        if (!z) {
            activity.setTheme(obtainTypedArray.getResourceId(0, 0));
        }
        obtainTypedArray.recycle();
    }

    public void restartForAppThemeChange(int i) {
        restartForAppThemeChange(getTag(), getString(i));
    }

    public static void restartForAppThemeChange(String str, final String str2) {
        final AppUtil instance = AppUtil.getInstance();
        RobotLog.m61vv(str, "app theme changed: restarting app: %s", str2);
        instance.runOnUiThread(new Runnable() {
            public void run() {
                AppUtil.this.showToast(UILocation.BOTH, str2);
                new Handler().postDelayed(new Runnable() {
                    public void run() {
                        AppUtil.this.restartApp(0);
                    }
                }, 1250);
            }
        });
    }
}
