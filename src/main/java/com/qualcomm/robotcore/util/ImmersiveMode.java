package com.qualcomm.robotcore.util;

import android.view.View;

public class ImmersiveMode {
    View decorView;

    public ImmersiveMode(View view) {
        this.decorView = view;
    }

    public void hideSystemUI() {
        this.decorView.setSystemUiVisibility(5894);
    }
}
