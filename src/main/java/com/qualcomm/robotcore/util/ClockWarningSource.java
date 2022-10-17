package com.qualcomm.robotcore.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.BuildConfig;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.robocol.Heartbeat;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.threeten.p014bp.Instant;
import org.threeten.p014bp.ZonedDateTime;
import org.threeten.p014bp.temporal.ChronoUnit;

public class ClockWarningSource implements GlobalWarningSource, PeerStatusCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String CLOCK_WARNING_ENABLED_PREF;
    private static final AppUtil appUtil = AppUtil.getInstance();
    private static final Application context;
    private static final ClockWarningSource instance = new ClockWarningSource();
    private static final SharedPreferences preferences;
    private volatile boolean dsClockIsOlderThanCurrentRelease = false;
    private final boolean examineRcClock = (!Device.isRevControlHub());
    private volatile boolean needToCheckDsClock = true;
    private volatile boolean rcAndDsClocksDifferSignificantly = false;
    private final ZonedDateTime rcBuildTime = ZonedDateTime.parse(BuildConfig.SDK_BUILD_TIME, appUtil.getIso8601DateTimeFormatter());
    private volatile boolean rcClockIsOlderThanCurrentRelease = false;
    private final ElapsedTime timeSinceLastDsClockCheck = new ElapsedTime(0);
    private volatile String warning;

    public void clearGlobalWarning() {
    }

    public void onPeerConnected() {
    }

    public void setGlobalWarning(String str) {
    }

    public boolean shouldTriggerWarningSound() {
        return false;
    }

    public void suppressGlobalWarning(boolean z) {
    }

    static {
        Application defContext = AppUtil.getDefContext();
        context = defContext;
        preferences = PreferenceManager.getDefaultSharedPreferences(defContext);
        CLOCK_WARNING_ENABLED_PREF = defContext.getString(C0705R.string.pref_warn_about_incorrect_clocks);
    }

    public static ClockWarningSource getInstance() {
        return instance;
    }

    private ClockWarningSource() {
        RobotLog.registerGlobalWarningSource(this);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        onPossibleRcClockUpdate();
    }

    public void onPossibleRcClockUpdate() {
        if (this.examineRcClock) {
            this.rcClockIsOlderThanCurrentRelease = ZonedDateTime.now().isBefore(this.rcBuildTime);
            this.needToCheckDsClock = true;
            refreshWarning();
        }
    }

    public void onDsHeartbeatReceived(Heartbeat heartbeat) {
        if (this.needToCheckDsClock && this.timeSinceLastDsClockCheck.seconds() > 5.0d) {
            this.timeSinceLastDsClockCheck.reset();
            Instant ofEpochMilli = Instant.ofEpochMilli(heartbeat.f135t0);
            this.dsClockIsOlderThanCurrentRelease = ofEpochMilli.isBefore(Instant.from(this.rcBuildTime));
            if (this.examineRcClock) {
                this.rcAndDsClocksDifferSignificantly = Math.abs(ChronoUnit.MINUTES.between(ofEpochMilli, Instant.now())) > 60;
            }
            if (!this.dsClockIsOlderThanCurrentRelease && !this.rcAndDsClocksDifferSignificantly) {
                this.needToCheckDsClock = false;
            }
            refreshWarning();
        }
    }

    public String getGlobalWarning() {
        return this.warning;
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        if (CLOCK_WARNING_ENABLED_PREF.equals(str)) {
            refreshWarning();
        }
    }

    public void onPeerDisconnected() {
        this.needToCheckDsClock = true;
        this.dsClockIsOlderThanCurrentRelease = false;
        this.rcAndDsClocksDifferSignificantly = false;
        refreshWarning();
    }

    private void refreshWarning() {
        if (!preferences.getBoolean(CLOCK_WARNING_ENABLED_PREF, true)) {
            this.warning = null;
        } else if (this.rcClockIsOlderThanCurrentRelease && this.dsClockIsOlderThanCurrentRelease) {
            this.warning = context.getString(C0705R.string.warningBothClocksBehind);
        } else if (this.rcClockIsOlderThanCurrentRelease) {
            this.warning = context.getString(C0705R.string.warningRcClockBehind);
        } else if (this.dsClockIsOlderThanCurrentRelease) {
            this.warning = context.getString(C0705R.string.warningDsClockBehind);
        } else if (this.rcAndDsClocksDifferSignificantly) {
            this.warning = context.getString(C0705R.string.warningClocksDiffer);
        } else {
            this.warning = null;
        }
    }
}
