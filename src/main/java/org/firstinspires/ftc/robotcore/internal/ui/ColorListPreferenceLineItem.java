package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.Checkable;
import android.widget.CheckedTextView;
import android.widget.LinearLayout;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.ColorListPreferenceLineItem */
public class ColorListPreferenceLineItem extends LinearLayout implements Checkable {
    public ColorListPreferenceLineItem(Context context, AttributeSet attributeSet, int i) {
        super(context, attributeSet, i);
    }

    public ColorListPreferenceLineItem(Context context, AttributeSet attributeSet) {
        this(context, attributeSet, 0);
    }

    public ColorListPreferenceLineItem(Context context) {
        this(context, (AttributeSet) null);
    }

    /* access modifiers changed from: protected */
    public CheckedTextView getCheckedTextView() {
        return (CheckedTextView) findViewById(16908308);
    }

    public void setChecked(boolean z) {
        getCheckedTextView().setChecked(z);
    }

    public boolean isChecked() {
        return getCheckedTextView().isChecked();
    }

    public void toggle() {
        getCheckedTextView().toggle();
    }
}
