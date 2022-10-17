package com.qualcomm.ftcdriverstation;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class ManualKeyInDialog extends AlertDialog.Builder {
    Button doneBtn;
    EditText input;
    InputMethodManager inputMethodManager = ((InputMethodManager) AppUtil.getDefContext().getSystemService("input_method"));
    Listener listener;
    String title;

    public static abstract class Listener {
        public abstract void onInput(String str);
    }

    public ManualKeyInDialog(Context context, String str, Listener listener2) {
        super(context);
        this.title = str;
        this.listener = listener2;
    }

    public AlertDialog show() {
        setTitle(this.title);
        setCancelable(false);
        View inflate = create().getLayoutInflater().inflate(C0648R.layout.custom_input_dialog_layout, (ViewGroup) null, false);
        EditText editText = (EditText) inflate.findViewById(C0648R.C0650id.input);
        this.input = editText;
        editText.requestFocus();
        setView(inflate);
        setPositiveButton(17039370, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ManualKeyInDialog.this.inputMethodManager.hideSoftInputFromWindow(ManualKeyInDialog.this.input.getWindowToken(), 0);
                dialogInterface.dismiss();
                ManualKeyInDialog.this.listener.onInput(ManualKeyInDialog.this.input.getText().toString());
            }
        });
        setNegativeButton(17039360, new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                ManualKeyInDialog.this.inputMethodManager.hideSoftInputFromWindow(ManualKeyInDialog.this.input.getWindowToken(), 0);
                dialogInterface.cancel();
            }
        });
        AlertDialog show = super.show();
        this.doneBtn = show.getButton(-1);
        this.input.post(new Runnable() {
            public void run() {
                ManualKeyInDialog.this.setSoftInputMode(2);
            }
        });
        return show;
    }

    public void setSoftInputMode(int i) {
        this.inputMethodManager.toggleSoftInput(i, 0);
    }
}
