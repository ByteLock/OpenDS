package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.content.Context;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class GamepadIndicator {
    protected ImageView activeView;
    protected ImageView baseView;
    protected final Context context;
    protected final int idActive;
    protected final int idBase;
    protected State state = State.INVISIBLE;

    public enum State {
        INVISIBLE,
        VISIBLE,
        INDICATE
    }

    public GamepadIndicator(Activity activity, int i, int i2) {
        this.context = activity;
        this.idActive = i;
        this.idBase = i2;
        initialize(activity);
    }

    public void initialize(Activity activity) {
        this.activeView = (ImageView) activity.findViewById(this.idActive);
        this.baseView = (ImageView) activity.findViewById(this.idBase);
    }

    public void setState(final State state2) {
        this.state = state2;
        AppUtil.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                int i = C06293.$SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State[state2.ordinal()];
                if (i == 1) {
                    GamepadIndicator.this.activeView.setVisibility(4);
                    GamepadIndicator.this.baseView.setVisibility(4);
                } else if (i == 2) {
                    GamepadIndicator.this.activeView.setVisibility(4);
                    GamepadIndicator.this.baseView.setVisibility(0);
                } else if (i == 3) {
                    GamepadIndicator.this.indicate();
                }
            }
        });
    }

    /* renamed from: com.qualcomm.ftcdriverstation.GamepadIndicator$3 */
    static /* synthetic */ class C06293 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.ftcdriverstation.GamepadIndicator$State[] r0 = com.qualcomm.ftcdriverstation.GamepadIndicator.State.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State = r0
                com.qualcomm.ftcdriverstation.GamepadIndicator$State r1 = com.qualcomm.ftcdriverstation.GamepadIndicator.State.INVISIBLE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.ftcdriverstation.GamepadIndicator$State r1 = com.qualcomm.ftcdriverstation.GamepadIndicator.State.VISIBLE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$ftcdriverstation$GamepadIndicator$State     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.ftcdriverstation.GamepadIndicator$State r1 = com.qualcomm.ftcdriverstation.GamepadIndicator.State.INDICATE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftcdriverstation.GamepadIndicator.C06293.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void indicate() {
        Animation loadAnimation = AnimationUtils.loadAnimation(this.context, C0648R.anim.fadeout);
        this.activeView.setImageResource(C0648R.C0649drawable.icon_controlleractive);
        loadAnimation.setAnimationListener(new Animation.AnimationListener() {
            public void onAnimationStart(Animation animation) {
            }

            public void onAnimationEnd(Animation animation) {
                GamepadIndicator.this.activeView.setImageResource(C0648R.C0649drawable.icon_controller);
            }

            public void onAnimationRepeat(Animation animation) {
                GamepadIndicator.this.activeView.setImageResource(C0648R.C0649drawable.icon_controller);
            }
        });
        this.activeView.startAnimation(loadAnimation);
    }
}
