package org.firstinspires.ftc.robotcore.internal.system;

import android.content.Intent;
import android.os.Debug;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;

public class AppAliveNotifier {
    private static final Intent ALIVE_NOTIFICATION = new Intent(Intents.ACTION_FTC_NOTIFY_RC_ALIVE);
    private static final int APP_STARTUP_OS_TIMEOUT_VALUE_SECONDS = 20;
    private static final int MIN_NOTIFICATION_PERIOD_MS = 1500;
    private static final int OS_TIMEOUT_VALUE_SECONDS = 10;
    private static final int SECONDS_IN_ONE_YEAR = 31536000;
    private static final String TAG = "AppAliveNotifier";
    private static final AppAliveNotifier instance = new AppAliveNotifier();
    private volatile boolean appFinishedStartup = false;
    private final boolean enabled;
    private volatile boolean previouslyDetectedDebugger = false;
    private final ElapsedTime timeSinceLastAliveNotification;

    public static AppAliveNotifier getInstance() {
        return instance;
    }

    private AppAliveNotifier() {
        boolean hasRcAppWatchdog = AndroidBoard.getInstance().hasRcAppWatchdog();
        this.enabled = hasRcAppWatchdog;
        this.timeSinceLastAliveNotification = hasRcAppWatchdog ? new ElapsedTime() : null;
    }

    public void onAppStartup() {
        if (this.enabled) {
            setOsTimeout(20);
            checkForDebugger();
        }
    }

    public void notifyAppAlive() {
        if (this.enabled) {
            if (!this.appFinishedStartup && !this.previouslyDetectedDebugger) {
                this.appFinishedStartup = true;
                setOsTimeout(10);
            }
            if (this.timeSinceLastAliveNotification.milliseconds() > 1500.0d) {
                AppUtil.getDefContext().sendBroadcast(ALIVE_NOTIFICATION);
                this.timeSinceLastAliveNotification.reset();
            }
            checkForDebugger();
        }
    }

    public void disableAppWatchdogUntilNextAppStart() {
        if (this.enabled) {
            setOsTimeout(SECONDS_IN_ONE_YEAR);
        }
    }

    private void checkForDebugger() {
        if (this.previouslyDetectedDebugger) {
            return;
        }
        if (Debug.waitingForDebugger() || Debug.isDebuggerConnected()) {
            RobotLog.m54ii(TAG, "Debugger detected, setting OS's RC Watchdog timeout to 1 year");
            this.previouslyDetectedDebugger = true;
            setOsTimeout(SECONDS_IN_ONE_YEAR);
        }
    }

    private void setOsTimeout(int i) {
        RobotLog.m55ii(TAG, "Telling the OS to set the RC alive notification timeout to %d seconds", Integer.valueOf(i));
        Intent intent = new Intent(Intents.ACTION_FTC_NOTIFY_RC_ALIVE);
        intent.putExtra(Intents.EXTRA_RC_ALIVE_NOTIFICATION_TIMEOUT_SECONDS, i);
        AppUtil.getDefContext().sendBroadcast(intent);
        this.timeSinceLastAliveNotification.reset();
    }
}
