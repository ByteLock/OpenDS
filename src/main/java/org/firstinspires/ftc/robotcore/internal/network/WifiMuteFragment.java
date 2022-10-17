package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.qualcomm.robotcore.C0705R;

public class WifiMuteFragment extends Fragment {
    TextView description;
    TextView nofication;
    WifiMuteStateMachine stateMachine;
    TextView timer;

    public View onCreateView(LayoutInflater layoutInflater, ViewGroup viewGroup, Bundle bundle) {
        View inflate = layoutInflater.inflate(C0705R.layout.fragment_wifi_mute, viewGroup, false);
        this.timer = (TextView) inflate.findViewById(C0705R.C0707id.countdownNumber);
        this.description = (TextView) inflate.findViewById(C0705R.C0707id.countdownDescription);
        this.nofication = (TextView) inflate.findViewById(C0705R.C0707id.wifiDisabledNotification);
        inflate.setOnTouchListener(new View.OnTouchListener() {
            public boolean onTouch(View view, MotionEvent motionEvent) {
                if (motionEvent.getAction() == 1) {
                    WifiMuteFragment.this.stateMachine.consumeEvent(WifiMuteEvent.USER_ACTIVITY);
                }
                return true;
            }
        });
        return inflate;
    }

    public void setStateMachine(WifiMuteStateMachine wifiMuteStateMachine) {
        this.stateMachine = wifiMuteStateMachine;
    }

    public void setCountdownNumber(final long j) {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WifiMuteFragment.this.timer.setText(String.valueOf(j));
            }
        });
    }

    public void displayDisabledMessage() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WifiMuteFragment.this.description.setVisibility(8);
                WifiMuteFragment.this.timer.setVisibility(8);
                WifiMuteFragment.this.nofication.setVisibility(0);
            }
        });
    }

    public void reset() {
        getActivity().runOnUiThread(new Runnable() {
            public void run() {
                WifiMuteFragment.this.description.setVisibility(0);
                WifiMuteFragment.this.timer.setVisibility(0);
                WifiMuteFragment.this.nofication.setVisibility(8);
            }
        });
    }
}
