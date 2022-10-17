package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

public class OpModeSelectionDialogFragment extends DialogFragment {
    /* access modifiers changed from: private */
    public OpModeSelectionDialogListener listener = null;
    /* access modifiers changed from: private */
    public List<OpModeMeta> opModes = new LinkedList();
    private int title = 0;

    public interface OpModeSelectionDialogListener {
        void onOpModeSelectionClick(OpModeMeta opModeMeta);
    }

    public void setOpModes(List<OpModeMeta> list) {
        LinkedList linkedList = new LinkedList(list);
        this.opModes = linkedList;
        Collections.sort(linkedList, new Comparator<OpModeMeta>() {
            public int compare(OpModeMeta opModeMeta, OpModeMeta opModeMeta2) {
                int compareTo = opModeMeta.group.compareTo(opModeMeta2.group);
                return compareTo == 0 ? opModeMeta.name.compareTo(opModeMeta2.name) : compareTo;
            }
        });
    }

    public void setOnSelectionDialogListener(OpModeSelectionDialogListener opModeSelectionDialogListener) {
        this.listener = opModeSelectionDialogListener;
    }

    public void setTitle(int i) {
        this.title = i;
    }

    public Dialog onCreateDialog(Bundle bundle) {
        View inflate = LayoutInflater.from(getActivity()).inflate(C0648R.layout.opmode_dialog_title_bar, (ViewGroup) null);
        ((TextView) inflate.findViewById(C0648R.C0650id.opmodeDialogTitle)).setText(this.title);
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setCustomTitle(inflate);
        OpModeSelectionDialogEntryAdapter opModeSelectionDialogEntryAdapter = new OpModeSelectionDialogEntryAdapter(getActivity(), this.opModes);
        builder.setTitle(this.title);
        builder.setAdapter(opModeSelectionDialogEntryAdapter, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (OpModeSelectionDialogFragment.this.listener != null) {
                    OpModeSelectionDialogFragment.this.listener.onOpModeSelectionClick((OpModeMeta) OpModeSelectionDialogFragment.this.opModes.get(i));
                }
            }
        });
        return builder.create();
    }

    public void onStart() {
        super.onStart();
        Dialog dialog = getDialog();
        dialog.findViewById(dialog.getContext().getResources().getIdentifier("android:id/titleDivider", (String) null, (String) null)).setBackground(dialog.findViewById(C0648R.C0650id.opmodeDialogTitleLine).getBackground());
    }
}
