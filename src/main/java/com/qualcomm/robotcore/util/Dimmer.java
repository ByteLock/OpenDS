package com.qualcomm.robotcore.util;

import android.app.Activity;
import android.os.Handler;
import android.view.WindowManager;
import com.qualcomm.ftcdriverstation.FtcDriverStationActivity;

public class Dimmer {
    public static final int DEFAULT_DIM_TIME = 30000;
    public static final int LONG_BRIGHT_TIME = 60000;
    public static final float MAXIMUM_BRIGHTNESS = 1.0f;
    public static final float MINIMUM_BRIGHTNESS = 0.05f;
    Activity activity;
    Handler handler;
    final WindowManager.LayoutParams layoutParams;
    float userBrightness;
    long waitTime;

    public Dimmer(Activity activity2) {
        this(FtcDriverStationActivity.OpModeCountDownTimer.MS_COUNTDOWN_INTERVAL, activity2);
    }

    public Dimmer(long j, Activity activity2) {
        this.handler = new Handler();
        this.userBrightness = 1.0f;
        this.waitTime = j;
        this.activity = activity2;
        WindowManager.LayoutParams attributes = activity2.getWindow().getAttributes();
        this.layoutParams = attributes;
        this.userBrightness = attributes.screenBrightness;
    }

    /* access modifiers changed from: private */
    public float percentageDim() {
        float f = this.userBrightness * 0.05f;
        if (f < 0.05f) {
            return 0.05f;
        }
        return f;
    }

    public void handleDimTimer() {
        sendToUIThread(this.userBrightness);
        this.handler.removeCallbacks((Runnable) null);
        this.handler.postDelayed(new Runnable() {
            public void run() {
                Dimmer dimmer = Dimmer.this;
                dimmer.sendToUIThread(dimmer.percentageDim());
            }
        }, this.waitTime);
    }

    /* access modifiers changed from: private */
    public void sendToUIThread(float f) {
        this.layoutParams.screenBrightness = f;
        this.activity.runOnUiThread(new Runnable() {
            public void run() {
                Dimmer.this.activity.getWindow().setAttributes(Dimmer.this.layoutParams);
            }
        });
    }

    public void longBright() {
        sendToUIThread(this.userBrightness);
        C07543 r0 = new Runnable() {
            public void run() {
                Dimmer dimmer = Dimmer.this;
                dimmer.sendToUIThread(dimmer.percentageDim());
            }
        };
        this.handler.removeCallbacksAndMessages((Object) null);
        this.handler.postDelayed(r0, 60000);
    }
}
