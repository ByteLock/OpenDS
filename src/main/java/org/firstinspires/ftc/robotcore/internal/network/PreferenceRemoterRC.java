package org.firstinspires.ftc.robotcore.internal.network;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SoftwareVersionWarningSource;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.WifiDirectAssistant;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.network.ApChannel;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class PreferenceRemoterRC extends PreferenceRemoter {
    public static final String TAG = "NetDiscover_prefremrc";
    protected static PreferenceRemoterRC theInstance;
    protected Set<String> rcPrefsOfInterestToDS;
    protected WarningSource warningSource = new WarningSource();

    public String getTag() {
        return TAG;
    }

    public static synchronized PreferenceRemoterRC getInstance() {
        PreferenceRemoterRC preferenceRemoterRC;
        synchronized (PreferenceRemoterRC.class) {
            if (theInstance == null) {
                theInstance = new PreferenceRemoterRC();
            }
            preferenceRemoterRC = theInstance;
        }
        return preferenceRemoterRC;
    }

    public PreferenceRemoterRC() {
        HashSet hashSet = new HashSet();
        this.rcPrefsOfInterestToDS = hashSet;
        hashSet.add(this.context.getString(C0705R.string.pref_device_name));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_app_theme));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_sound_on_off));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_wifip2p_remote_channel_change_works));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_wifip2p_channel));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_has_independent_phone_battery));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_has_speaker));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_warn_about_obsolete_software));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_warn_about_2_4_ghz_band));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_warn_about_mismatched_app_versions));
        this.rcPrefsOfInterestToDS.add(this.context.getString(C0705R.string.pref_warn_about_incorrect_clocks));
    }

    /* access modifiers changed from: protected */
    public SharedPreferences.OnSharedPreferenceChangeListener makeSharedPrefListener() {
        return new SharedPreferencesListenerRC();
    }

    public CallbackResult handleCommandRobotControllerPreference(String str) {
        RobotControllerPreference deserialize = RobotControllerPreference.deserialize(str);
        if (deserialize.getPrefName().equals(AppUtil.getDefContext().getString(C0705R.string.pref_wifip2p_channel))) {
            if (deserialize.getValue() == null || !(deserialize.getValue() instanceof Integer)) {
                RobotLog.m48ee(TAG, "incorrect preference value type: " + deserialize.getValue());
            } else {
                new WifiDirectChannelChanger().changeToChannel(((Integer) deserialize.getValue()).intValue());
            }
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_ds_supports_5_ghz))) {
            boolean unused = this.warningSource.dsSupports5Ghz = ((Boolean) deserialize.getValue()).booleanValue();
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_dh_os_version_code))) {
            SoftwareVersionWarningSource.getInstance().onReceivedDriverHubOsVersionCode(((Integer) deserialize.getValue()).intValue());
        } else {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        }
        return CallbackResult.HANDLED;
    }

    protected class SharedPreferencesListenerRC implements SharedPreferences.OnSharedPreferenceChangeListener {
        protected SharedPreferencesListenerRC() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            RobotLog.m61vv(PreferenceRemoterRC.TAG, "onSharedPreferenceChanged(name=%s, value=%s)", str, PreferenceRemoterRC.this.preferencesHelper.readPref(str));
            if (PreferenceRemoterRC.this.rcPrefsOfInterestToDS.contains(str)) {
                PreferenceRemoterRC.this.sendPreference(str);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void sendPreference(String str) {
        Object readPref = this.preferencesHelper.readPref(str);
        if (readPref != null) {
            sendPreference(new RobotControllerPreference(str, readPref));
        }
    }

    public void sendAllPreferences() {
        RobotLog.m60vv(TAG, "sendAllPreferences()");
        for (String sendPreference : this.rcPrefsOfInterestToDS) {
            sendPreference(sendPreference);
        }
    }

    protected class WarningSource implements GlobalWarningSource, PeerStatusCallback {
        /* access modifiers changed from: private */
        public volatile boolean dsSupports5Ghz = false;
        /* access modifiers changed from: private */
        public final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(AppUtil.getDefContext());
        /* access modifiers changed from: private */
        public volatile String unnecessary2_4GhzUsageWarning;

        public void onPeerConnected() {
        }

        public void setGlobalWarning(String str) {
        }

        public boolean shouldTriggerWarningSound() {
            return false;
        }

        public void suppressGlobalWarning(boolean z) {
        }

        public WarningSource() {
            RobotLog.registerGlobalWarningSource(this);
            NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
            ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable(PreferenceRemoterRC.this) {
                public void run() {
                    boolean z = true;
                    if (!WarningSource.this.sharedPrefs.getBoolean(PreferenceRemoterRC.this.context.getString(C0705R.string.pref_warn_about_2_4_ghz_band), true)) {
                        String unused = WarningSource.this.unnecessary2_4GhzUsageWarning = null;
                        return;
                    }
                    boolean is5GHzAvailable = WifiUtil.is5GHzAvailable();
                    ApChannel currentChannel = ApChannelManagerFactory.getInstance().getCurrentChannel();
                    if (currentChannel == ApChannel.UNKNOWN || currentChannel.band != ApChannel.Band.BAND_2_4_GHZ) {
                        z = false;
                    }
                    if (!WarningSource.this.dsSupports5Ghz || !is5GHzAvailable || !z) {
                        String unused2 = WarningSource.this.unnecessary2_4GhzUsageWarning = null;
                    } else if (!(NetworkConnectionHandler.getInstance().getNetworkConnection() instanceof WifiDirectAssistant) || ApChannelManagerFactory.getInstance().getCurrentChannel() != ApChannel.AUTO_2_4_GHZ) {
                        WarningSource warningSource = WarningSource.this;
                        String unused3 = warningSource.unnecessary2_4GhzUsageWarning = PreferenceRemoterRC.this.context.getString(C0705R.string.warning2_4GhzUnnecessaryUsage);
                    } else {
                        WarningSource warningSource2 = WarningSource.this;
                        String unused4 = warningSource2.unnecessary2_4GhzUsageWarning = PreferenceRemoterRC.this.context.getString(C0705R.string.warning2_4GhzUnnecessaryUsageWiFiDirectAuto);
                    }
                }
            }, 0, 5, TimeUnit.SECONDS);
        }

        public String getGlobalWarning() {
            return this.unnecessary2_4GhzUsageWarning;
        }

        public void clearGlobalWarning() {
            this.unnecessary2_4GhzUsageWarning = null;
        }

        public void onPeerDisconnected() {
            clearGlobalWarning();
            this.dsSupports5Ghz = false;
        }
    }
}
