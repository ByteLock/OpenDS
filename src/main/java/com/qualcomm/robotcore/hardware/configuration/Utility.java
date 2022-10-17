package com.qualcomm.robotcore.hardware.configuration;

import android.app.Activity;
import android.app.AlertDialog;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.inspection.InspectionState;

public class Utility {
    private Activity activity;

    public Utility(Activity activity2) {
        this.activity = activity2;
    }

    public Activity getActivity() {
        return this.activity;
    }

    public void setFeedbackText(CharSequence[] charSequenceArr, int i, int i2, int i3, int i4) {
        setFeedbackText(charSequenceArr[0], charSequenceArr[1], i, i2, i3, i4);
    }

    public void setFeedbackText(CharSequence charSequence, CharSequence charSequence2, int i, int i2, int i3, int i4) {
        setFeedbackText(charSequence, charSequence2, i, i2, i3, i4, 0);
    }

    public void setFeedbackText(CharSequence charSequence, CharSequence charSequence2, final int i, int i2, int i3, int i4, int i5) {
        LinearLayout linearLayout = (LinearLayout) this.activity.findViewById(i);
        linearLayout.setVisibility(0);
        linearLayout.removeAllViews();
        this.activity.getLayoutInflater().inflate(i2, linearLayout, true);
        TextView textView = (TextView) linearLayout.findViewById(i3);
        TextView textView2 = (TextView) linearLayout.findViewById(i4);
        Button button = (Button) linearLayout.findViewById(i5);
        if (textView != null) {
            textView.setText(charSequence);
        }
        if (textView2 != null) {
            textView2.setText(charSequence2);
        }
        if (button != null) {
            button.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    Utility.this.hideFeedbackText(i);
                }
            });
            button.setVisibility(0);
        }
    }

    public void hideFeedbackText(int i) {
        LinearLayout linearLayout = (LinearLayout) this.activity.findViewById(i);
        linearLayout.removeAllViews();
        linearLayout.setVisibility(8);
    }

    public CharSequence[] getFeedbackText(int i, int i2, int i3, int i4) {
        CharSequence charSequence;
        LinearLayout linearLayout = (LinearLayout) this.activity.findViewById(i);
        Assert.assertTrue(linearLayout != null);
        TextView textView = (TextView) linearLayout.findViewById(i3);
        TextView textView2 = (TextView) linearLayout.findViewById(i4);
        boolean z = textView == null || textView.getText().length() == 0;
        boolean z2 = textView2 == null || textView2.getText().length() == 0;
        if (z && z2) {
            return null;
        }
        CharSequence[] charSequenceArr = new CharSequence[2];
        CharSequence charSequence2 = InspectionState.NO_VERSION;
        if (textView == null) {
            charSequence = charSequence2;
        } else {
            charSequence = textView.getText();
        }
        charSequenceArr[0] = charSequence;
        if (textView2 != null) {
            charSequence2 = textView2.getText();
        }
        charSequenceArr[1] = charSequence2;
        return charSequenceArr;
    }

    public AlertDialog.Builder buildBuilder(String str, String str2) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this.activity);
        builder.setTitle(str).setMessage(str2);
        return builder;
    }
}
