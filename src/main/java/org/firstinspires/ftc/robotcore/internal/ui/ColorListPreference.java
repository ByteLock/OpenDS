package org.firstinspires.ftc.robotcore.internal.p013ui;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.graphics.drawable.ColorDrawable;
import android.preference.ListPreference;
import android.util.AttributeSet;
import android.util.Pair;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import com.qualcomm.robotcore.C0705R;
import java.util.ArrayList;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.ColorListPreference */
public class ColorListPreference extends ListPreference {
    protected int clickedDialogEntryIndex;
    protected int[] colors;

    public ColorListPreference(Context context, AttributeSet attributeSet) {
        super(context, attributeSet);
        TypedArray obtainStyledAttributes = context.obtainStyledAttributes(attributeSet, C0705R.styleable.ColorListPreference, 0, 0);
        try {
            this.colors = context.getResources().getIntArray(obtainStyledAttributes.getResourceId(C0705R.styleable.ColorListPreference_colors, 0));
        } finally {
            obtainStyledAttributes.recycle();
        }
    }

    public ColorListPreference(Context context) {
        this(context, (AttributeSet) null);
    }

    private int getValueIndex() {
        return findIndexOfValue(getValue());
    }

    /* access modifiers changed from: protected */
    public void onPrepareDialogBuilder(AlertDialog.Builder builder) {
        if (getEntries() == null || getEntryValues() == null || this.colors == null) {
            throw new IllegalStateException("ColorListPreference: entries, values, and colors required");
        }
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < getEntries().length; i++) {
            arrayList.add(new Pair(getEntries()[i], Integer.valueOf(this.colors[i])));
        }
        this.clickedDialogEntryIndex = getValueIndex();
        final int i2 = C0705R.C0707id.colorSwatch;
        final int i3 = C0705R.layout.color_list_preference_line_item;
        builder.setSingleChoiceItems(new ArrayAdapter<Pair<CharSequence, Integer>>(getContext(), i3, 16908308, arrayList) {
            public View getView(int i, View view, ViewGroup viewGroup) {
                if (view == null) {
                    view = LayoutInflater.from(getContext()).inflate(i3, viewGroup, false);
                }
                Pair pair = (Pair) getItem(i);
                ((TextView) view.findViewById(16908308)).setText((CharSequence) pair.first);
                int i2 = i2;
                if (i2 != 0) {
                    ((ImageView) view.findViewById(i2)).setImageDrawable(new ColorDrawable(((Integer) pair.second).intValue()));
                }
                return view;
            }
        }, this.clickedDialogEntryIndex, new DialogClickListener());
        builder.setPositiveButton((CharSequence) null, (DialogInterface.OnClickListener) null);
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.ui.ColorListPreference$DialogClickListener */
    protected class DialogClickListener implements DialogInterface.OnClickListener {
        protected DialogClickListener() {
        }

        public void onClick(DialogInterface dialogInterface, int i) {
            ColorListPreference.this.clickedDialogEntryIndex = i;
            ColorListPreference.this.onClick(dialogInterface, -1);
            dialogInterface.dismiss();
        }
    }

    /* access modifiers changed from: protected */
    public void onDialogClosed(boolean z) {
        if (z && this.clickedDialogEntryIndex >= 0 && getEntryValues() != null) {
            String charSequence = getEntryValues()[this.clickedDialogEntryIndex].toString();
            if (callChangeListener(charSequence)) {
                setValue(charSequence);
            }
        }
    }
}
