package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.LayoutInflater;
import android.widget.ArrayAdapter;
import android.widget.FrameLayout;
import android.widget.Spinner;
import com.qualcomm.robotcore.hardware.Gamepad;

public class SelectGamepadMappingDialog extends AlertDialog.Builder {
    private ArrayAdapter<CharSequence> fieldTypeAdapter;
    /* access modifiers changed from: private */
    public Spinner fieldTypeSpinner;
    /* access modifiers changed from: private */
    public Listener listener;

    interface Listener {
        void onOk(Gamepad.Type type);
    }

    public SelectGamepadMappingDialog(Context context) {
        super(context);
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
    }

    public AlertDialog show() {
        setTitle("Choose Mapping");
        LayoutInflater layoutInflater = create().getLayoutInflater();
        FrameLayout frameLayout = new FrameLayout(getContext());
        setView(frameLayout);
        layoutInflater.inflate(C0648R.layout.dialog_gamepad_mapping_type, frameLayout);
        this.fieldTypeSpinner = (Spinner) frameLayout.findViewById(C0648R.C0650id.gamepadMappingSpinner);
        setupTypeSpinner();
        setPositiveButton("OK", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                if (SelectGamepadMappingDialog.this.listener != null) {
                    SelectGamepadMappingDialog.this.listener.onOk(Gamepad.Type.valueOf((String) SelectGamepadMappingDialog.this.fieldTypeSpinner.getSelectedItem()));
                }
            }
        });
        setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
            }
        });
        return super.show();
    }

    private void setupTypeSpinner() {
        this.fieldTypeAdapter = new ArrayAdapter<>(getContext(), 17367048);
        for (Gamepad.Type type : Gamepad.Type.values()) {
            this.fieldTypeAdapter.add(type.toString());
        }
        this.fieldTypeAdapter.setDropDownViewResource(17367049);
        this.fieldTypeSpinner.setAdapter(this.fieldTypeAdapter);
    }
}
