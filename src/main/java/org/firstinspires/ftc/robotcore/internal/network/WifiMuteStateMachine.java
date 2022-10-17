package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Activity;
import android.net.wifi.WifiManager;
import android.os.CountDownTimer;
import android.widget.TextView;
import android.widget.Toast;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.external.Event;
import org.firstinspires.ftc.robotcore.external.State;
import org.firstinspires.ftc.robotcore.external.StateMachine;
import org.firstinspires.ftc.robotcore.external.StateTransition;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.CallbackRegistrar;
import org.firstinspires.ftc.robotcore.internal.system.Watchdog;

public class WifiMuteStateMachine extends StateMachine {
    private static final String TAG = "WifiMuteStateMachine";
    private static final int WIFI_MUTE_PERIOD = 1;
    private static final int WIFI_MUTE_TIMEOUT = 600;
    private static final int WIFI_MUTE_WARN = 10;
    /* access modifiers changed from: private */
    public Activity activity;
    private final WifiState blackhole = new WifiState();
    protected final CallbackRegistrar<Callback> callbacks;
    private final TimeoutSuspended timeoutSuspended = new TimeoutSuspended();
    private WifiManager wifiManager;
    /* access modifiers changed from: private */
    public final WifiMuteFragment wifiMuteFragment;
    protected Watchdog wifiMuzzleWatchdog;
    private final WifiOff wifiOff = new WifiOff();
    private final WifiOn wifiOn = new WifiOn();
    private final WifiPendingOff wifiPendingOff = new WifiPendingOff();

    public interface Callback {
        void onPendingCancel();

        void onPendingOn();

        void onWifiOff();

        void onWifiOn();
    }

    private class WifiState implements State {
        private WifiState() {
        }

        public void onEnter(Event event) {
            RobotLog.m54ii(WifiMuteStateMachine.TAG, "Enter State: " + getClass().getSimpleName());
        }

        public void onExit(Event event) {
            RobotLog.m54ii(WifiMuteStateMachine.TAG, "Exit State: " + getClass().getSimpleName());
        }

        public String toString() {
            return getClass().getSimpleName();
        }
    }

    private class WifiOn extends WifiState {
        boolean isEnabled;

        private WifiOn() {
            super();
            this.isEnabled = true;
        }

        public void onEnter(Event event) {
            super.onEnter(event);
            if (WifiMuteStateMachine.this.wifiMuzzleWatchdog.isRunning()) {
                WifiMuteStateMachine.this.wifiMuzzleWatchdog.stroke();
            } else {
                WifiMuteStateMachine.this.wifiMuzzleWatchdog.start();
            }
            if (!WifiMuteStateMachine.this.isWifiEnabled()) {
                AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, AppUtil.getDefContext().getString(C0705R.string.toastEnableWifi));
                WifiMuteStateMachine.this.enableWifi(true);
                WifiMuteStateMachine.this.notifyWifiOn();
            }
            WifiMuteStateMachine.this.activity.getFragmentManager().beginTransaction().hide(WifiMuteStateMachine.this.wifiMuteFragment).commit();
        }
    }

    private class WifiOff extends WifiState {
        public void onExit(Event event) {
        }

        private WifiOff() {
            super();
        }

        public void onEnter(Event event) {
            super.onEnter(event);
            WifiMuteStateMachine.this.wifiMuteFragment.displayDisabledMessage();
            if (WifiMuteStateMachine.this.wifiMuzzleWatchdog.isRunning()) {
                WifiMuteStateMachine.this.wifiMuzzleWatchdog.euthanize();
            }
            if (WifiMuteStateMachine.this.isWifiEnabled()) {
                AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, AppUtil.getDefContext().getString(C0705R.string.toastDisableWifi));
                WifiMuteStateMachine.this.enableWifi(false);
                WifiMuteStateMachine.this.notifyWifiOff();
            }
        }
    }

    private class WifiPendingOff extends WifiState {
        private final String msg;
        CountDownTimer wifiOffNotificationTimer;

        private WifiPendingOff() {
            super();
            this.msg = AppUtil.getDefContext().getString(C0705R.string.toastDisableWifiWarn);
            this.wifiOffNotificationTimer = new CountDownTimer(TimeUnit.SECONDS.toMillis(10), TimeUnit.SECONDS.toMillis(1)) {
                public void onFinish() {
                }

                public void onTick(long j) {
                    WifiMuteStateMachine.this.wifiMuteFragment.setCountdownNumber(j / TimeUnit.SECONDS.toMillis(1));
                }
            };
        }

        public void onEnter(Event event) {
            super.onEnter(event);
            WifiMuteStateMachine.this.wifiMuteFragment.reset();
            WifiMuteStateMachine.this.activity.getFragmentManager().beginTransaction().show(WifiMuteStateMachine.this.wifiMuteFragment).commit();
            WifiMuteStateMachine.this.notifyPendingOn();
            this.wifiOffNotificationTimer.start();
        }

        public void onExit(Event event) {
            super.onExit(event);
            WifiMuteStateMachine.this.notifyPendingCancel();
            this.wifiOffNotificationTimer.cancel();
        }
    }

    private class TimeoutSuspended extends WifiState {
        private TimeoutSuspended() {
            super();
        }

        public void onEnter(Event event) {
            super.onEnter(event);
            if (WifiMuteStateMachine.this.wifiMuzzleWatchdog.isRunning()) {
                WifiMuteStateMachine.this.wifiMuzzleWatchdog.euthanize();
            }
        }

        public void onExit(Event event) {
            super.onExit(event);
            if (!WifiMuteStateMachine.this.wifiMuzzleWatchdog.isRunning()) {
                WifiMuteStateMachine.this.wifiMuzzleWatchdog.start();
            }
        }
    }

    public void registerCallback(Callback callback) {
        this.callbacks.registerCallback(callback);
    }

    public void unregisterCallback(Callback callback) {
        this.callbacks.unregisterCallback(callback);
    }

    /* access modifiers changed from: protected */
    public void notifyWifiOn() {
        this.callbacks.callbacksDo(new Consumer<Callback>() {
            public void accept(Callback callback) {
                callback.onWifiOn();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void notifyWifiOff() {
        this.callbacks.callbacksDo(new Consumer<Callback>() {
            public void accept(Callback callback) {
                callback.onWifiOff();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void notifyPendingOn() {
        this.callbacks.callbacksDo(new Consumer<Callback>() {
            public void accept(Callback callback) {
                callback.onPendingOn();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void notifyPendingCancel() {
        this.callbacks.callbacksDo(new Consumer<Callback>() {
            public void accept(Callback callback) {
                callback.onPendingCancel();
            }
        });
    }

    public WifiMuteStateMachine() {
        WifiMuteFragment wifiMuteFragment2 = new WifiMuteFragment();
        this.wifiMuteFragment = wifiMuteFragment2;
        this.wifiMuzzleWatchdog = new Watchdog(new WifiMuteRunnable(), new WifiGrowlRunnable(), 10, 1, 600, TimeUnit.SECONDS);
        this.callbacks = new CallbackRegistrar<>();
        wifiMuteFragment2.setStateMachine(this);
        this.wifiManager = (WifiManager) AppUtil.getDefContext().getApplicationContext().getSystemService("wifi");
        Activity activity2 = AppUtil.getInstance().getActivity();
        this.activity = activity2;
        activity2.getFragmentManager().beginTransaction().add(16908290, wifiMuteFragment2).hide(wifiMuteFragment2).commit();
    }

    public void start() {
        super.start(this.blackhole);
        this.wifiMuzzleWatchdog.start();
    }

    public void stop() {
        if (this.wifiMuzzleWatchdog.isRunning()) {
            this.wifiMuzzleWatchdog.euthanize();
        }
    }

    public void initialize() {
        addTransition(new StateTransition(this.wifiOn, WifiMuteEvent.USER_ACTIVITY, this.wifiOn));
        addTransition(new StateTransition(this.wifiOn, WifiMuteEvent.WATCHDOG_WARNING, this.wifiPendingOff));
        addTransition(new StateTransition(this.wifiOn, WifiMuteEvent.RUNNING_OPMODE, this.timeoutSuspended));
        addTransition(new StateTransition(this.wifiOn, WifiMuteEvent.ACTIVITY_STOP, this.wifiOff));
        addTransition(new StateTransition(this.wifiOn, WifiMuteEvent.ACTIVITY_OTHER, this.timeoutSuspended));
        addTransition(new StateTransition(this.wifiPendingOff, WifiMuteEvent.USER_ACTIVITY, this.wifiOn));
        addTransition(new StateTransition(this.wifiPendingOff, WifiMuteEvent.WATCHDOG_TIMEOUT, this.wifiOff));
        addTransition(new StateTransition(this.timeoutSuspended, WifiMuteEvent.STOPPED_OPMODE, this.wifiOn));
        addTransition(new StateTransition(this.timeoutSuspended, WifiMuteEvent.ACTIVITY_START, this.wifiOn));
        addTransition(new StateTransition(this.wifiOff, WifiMuteEvent.USER_ACTIVITY, this.wifiOn));
        addTransition(new StateTransition(this.wifiOff, WifiMuteEvent.ACTIVITY_START, this.wifiOn));
        RobotLog.m54ii(TAG, "State Machine " + toString());
    }

    /* access modifiers changed from: protected */
    public void enableWifi(boolean z) {
        RobotLog.m54ii(TAG, "Set Wi-Fi enable " + z);
        if (z) {
            AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, AppUtil.getDefContext().getString(C0705R.string.toastEnableWifi));
        } else {
            AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, AppUtil.getDefContext().getString(C0705R.string.toastDisableWifi));
        }
        this.wifiManager.setWifiEnabled(z);
    }

    /* access modifiers changed from: protected */
    public boolean isWifiEnabled() {
        return this.wifiManager.isWifiEnabled();
    }

    /* access modifiers changed from: protected */
    public Toast makeToast(Activity activity2, String str) {
        Toast makeText = Toast.makeText(activity2.getApplicationContext(), str, 0);
        ((TextView) makeText.getView().findViewById(16908299)).setTextSize(18.0f);
        makeText.show();
        return makeText;
    }

    private class WifiMuteRunnable implements Runnable {
        private WifiMuteRunnable() {
        }

        public void run() {
            RobotLog.m54ii(WifiMuteStateMachine.TAG, "Watchdog barked");
            WifiMuteStateMachine.this.consumeEvent(WifiMuteEvent.WATCHDOG_TIMEOUT);
        }
    }

    private class WifiGrowlRunnable implements Runnable {
        private WifiGrowlRunnable() {
        }

        public void run() {
            RobotLog.m54ii(WifiMuteStateMachine.TAG, "Watchdog growled");
            WifiMuteStateMachine.this.consumeEvent(WifiMuteEvent.WATCHDOG_WARNING);
        }
    }
}
