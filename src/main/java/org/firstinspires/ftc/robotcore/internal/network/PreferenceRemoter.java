package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.Map;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public abstract class PreferenceRemoter extends WifiStartStoppable {
    protected Context context;
    protected PreferencesHelper preferencesHelper = new PreferencesHelper(getTag(), this.sharedPreferences);
    private SharedPreferences sharedPreferences;
    protected SharedPreferences.OnSharedPreferenceChangeListener sharedPreferencesListener = makeSharedPrefListener();

    public abstract CallbackResult handleCommandRobotControllerPreference(String str);

    /* access modifiers changed from: protected */
    public abstract SharedPreferences.OnSharedPreferenceChangeListener makeSharedPrefListener();

    public PreferenceRemoter() {
        super(WifiDirectAgent.getInstance());
        Application application = AppUtil.getInstance().getApplication();
        this.context = application;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        dumpAllPrefs();
    }

    /* access modifiers changed from: protected */
    public void dumpAllPrefs() {
        RobotLog.m60vv(getTag(), "----- all preferences -----");
        for (Map.Entry next : this.sharedPreferences.getAll().entrySet()) {
            RobotLog.m61vv(getTag(), "name='%s' value=%s", next.getKey(), next.getValue());
        }
    }

    /* access modifiers changed from: protected */
    public boolean doStart() throws InterruptedException {
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
        return true;
    }

    /* access modifiers changed from: protected */
    public void doStop() throws InterruptedException {
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
    }

    /* access modifiers changed from: protected */
    public void sendPreference(RobotControllerPreference robotControllerPreference) {
        RobotLog.m61vv(getTag(), "sending RC pref name=%s value=%s", robotControllerPreference.getPrefName(), robotControllerPreference.getValue());
        NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_ROBOT_CONTROLLER_PREFERENCE, robotControllerPreference.serialize()));
    }
}
