package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Device;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.BaseActivity */
public abstract class BaseActivity extends Activity {
    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return null;
    }

    public abstract String getTag();

    private void hideBackBar() {
        FrameLayout backBar = getBackBar();
        if (backBar != null) {
            ViewGroup.LayoutParams layoutParams = backBar.getLayoutParams();
            layoutParams.height = 0;
            backBar.setLayoutParams(layoutParams);
        }
    }

    private void setupBackButton() {
        if (getBackBar() != null) {
            ((ImageButton) findViewById(C0705R.C0707id.backButton)).setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    BaseActivity.this.onBackPressed();
                }
            });
        }
    }

    public void setContentView(int i) {
        super.setContentView(i);
        if (Device.deviceHasBackButton()) {
            hideBackBar();
        } else {
            setupBackButton();
        }
    }
}
