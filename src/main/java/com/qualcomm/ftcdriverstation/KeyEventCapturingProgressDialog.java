package com.qualcomm.ftcdriverstation;

import android.app.ProgressDialog;
import android.content.Context;
import android.view.KeyEvent;

public class KeyEventCapturingProgressDialog extends ProgressDialog {
    Listener listener;

    interface Listener {
        void onKeyDown(KeyEvent keyEvent);
    }

    public boolean onKeyUp(int i, KeyEvent keyEvent) {
        return true;
    }

    public KeyEventCapturingProgressDialog(Context context) {
        super(context);
    }

    public KeyEventCapturingProgressDialog(Context context, int i) {
        super(context, i);
    }

    public void setListener(Listener listener2) {
        this.listener = listener2;
    }

    public boolean onKeyDown(int i, KeyEvent keyEvent) {
        Listener listener2 = this.listener;
        if (listener2 == null) {
            return true;
        }
        listener2.onKeyDown(keyEvent);
        return true;
    }
}
