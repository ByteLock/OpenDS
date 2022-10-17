package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

public class OpModeSelectionDialogEntryAdapter extends BaseAdapter implements ListAdapter {
    private Activity activity;
    private List<OpModeMeta> opmodes;

    public long getItemId(int i) {
        return 0;
    }

    public OpModeSelectionDialogEntryAdapter(Activity activity2, List<OpModeMeta> list) {
        this.activity = activity2;
        this.opmodes = list;
    }

    public int getCount() {
        return this.opmodes.size();
    }

    public Object getItem(int i) {
        return this.opmodes.get(i);
    }

    public View getView(int i, View view, ViewGroup viewGroup) {
        if (view == null) {
            view = this.activity.getLayoutInflater().inflate(C0648R.layout.opmode_dialog_item, viewGroup, false);
        }
        OpModeMeta opModeMeta = this.opmodes.get(i);
        ((TextView) view.findViewById(C0648R.C0650id.opmodeDialogItemText)).setText(opModeMeta.name);
        ImageView imageView = (ImageView) view.findViewById(C0648R.C0650id.programmingToolIcon);
        if (opModeMeta.source != null) {
            imageView.setVisibility(0);
            int i2 = C06341.f73x5cceead8[opModeMeta.source.ordinal()];
            if (i2 == 1) {
                imageView.setImageDrawable(this.activity.getDrawable(C0648R.C0649drawable.android_head_icon));
            } else if (i2 == 2) {
                imageView.setImageDrawable(this.activity.getDrawable(C0648R.C0649drawable.blockly_icon));
            } else if (i2 == 3) {
                imageView.setImageDrawable(this.activity.getDrawable(C0648R.C0649drawable.obj_icon));
            } else if (i2 == 4) {
                imageView.setImageDrawable(this.activity.getDrawable(C0648R.C0649drawable.extlib_icon));
            }
        } else {
            imageView.setVisibility(8);
        }
        View findViewById = view.findViewById(C0648R.C0650id.opmodeDialogItemTextSeparator);
        if (i >= this.opmodes.size() - 1 || this.opmodes.get(i).group.equals(this.opmodes.get(i + 1).group)) {
            findViewById.setVisibility(4);
        } else {
            findViewById.setVisibility(0);
        }
        return view;
    }

    /* renamed from: com.qualcomm.ftcdriverstation.OpModeSelectionDialogEntryAdapter$1 */
    static /* synthetic */ class C06341 {

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$internal$opmode$OpModeMeta$Source */
        static final /* synthetic */ int[] f73x5cceead8;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta$Source[] r0 = org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Source.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f73x5cceead8 = r0
                org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta$Source r1 = org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Source.ANDROID_STUDIO     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f73x5cceead8     // Catch:{ NoSuchFieldError -> 0x001d }
                org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta$Source r1 = org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Source.BLOCKLY     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f73x5cceead8     // Catch:{ NoSuchFieldError -> 0x0028 }
                org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta$Source r1 = org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Source.ONBOTJAVA     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f73x5cceead8     // Catch:{ NoSuchFieldError -> 0x0033 }
                org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta$Source r1 = org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta.Source.EXTERNAL_LIBRARY     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftcdriverstation.OpModeSelectionDialogEntryAdapter.C06341.<clinit>():void");
        }
    }

    private boolean opModesComeFromMultipleSources() {
        OpModeMeta.Source source = this.opmodes.get(0).source;
        for (int i = 1; i < this.opmodes.size(); i++) {
            if (this.opmodes.get(i).source != source) {
                return true;
            }
        }
        return false;
    }
}
