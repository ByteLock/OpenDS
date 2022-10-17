package org.firstinspires.ftc.robotcore.internal.network;

import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.SystemClock;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class PreferenceRemoterDS extends PreferenceRemoter {
    public static final String TAG = "NetDiscover_prefremds";
    protected static PreferenceRemoterDS theInstance;
    protected PreferencesHelper.StringMap mapGroupOwnerToDeviceName;

    public String getTag() {
        return TAG;
    }

    public static synchronized PreferenceRemoterDS getInstance() {
        PreferenceRemoterDS preferenceRemoterDS;
        synchronized (PreferenceRemoterDS.class) {
            if (theInstance == null) {
                theInstance = new PreferenceRemoterDS();
            }
            preferenceRemoterDS = theInstance;
        }
        return preferenceRemoterDS;
    }

    public PreferenceRemoterDS() {
        if (System.currentTimeMillis() - SystemClock.elapsedRealtime() > getTimestampWhenRenameMapWasLastSaved()) {
            RobotLog.m60vv(TAG, "The device has been booted since the last time the Driver Station was running");
            clearRenameMap();
        } else {
            loadRenameMap();
        }
        this.preferencesHelper.remove(this.context.getString(C0705R.string.pref_wifip2p_groupowner_lastconnectedto));
        this.preferencesHelper.remove(this.context.getString(C0705R.string.pref_wifip2p_channel));
        this.preferencesHelper.remove(this.context.getString(C0705R.string.pref_has_independent_phone_battery_rc));
    }

    /* access modifiers changed from: protected */
    public SharedPreferences.OnSharedPreferenceChangeListener makeSharedPrefListener() {
        return new SharedPreferencesListenerDS();
    }

    public void onWifiToggled(boolean z) {
        RobotLog.m61vv(TAG, "onWifiToggled(%s)", Boolean.valueOf(z));
        if (!z) {
            clearRenameMap();
        }
    }

    /* access modifiers changed from: protected */
    public void clearRenameMap() {
        RobotLog.m60vv(TAG, "clearRenameMap()");
        this.mapGroupOwnerToDeviceName = new PreferencesHelper.StringMap();
        saveRenameMap();
    }

    /* access modifiers changed from: protected */
    public void saveRenameMap() {
        this.preferencesHelper.writeStringMapPrefIfDifferent(this.context.getString(C0705R.string.pref_wifip2p_groupowner_map), this.mapGroupOwnerToDeviceName);
        this.preferencesHelper.writeLongPrefIfDifferent(this.context.getString(C0705R.string.pref_wifip2p_groupowner_map_timestamp), System.currentTimeMillis());
    }

    /* access modifiers changed from: protected */
    public void loadRenameMap() {
        this.mapGroupOwnerToDeviceName = this.preferencesHelper.readStringMap(this.context.getString(C0705R.string.pref_wifip2p_groupowner_map), new PreferencesHelper.StringMap());
    }

    /* access modifiers changed from: protected */
    public long getTimestampWhenRenameMapWasLastSaved() {
        return this.preferencesHelper.readLong(this.context.getString(C0705R.string.pref_wifip2p_groupowner_map_timestamp), 0);
    }

    public String getDeviceNameForWifiP2pGroupOwner(String str) {
        String str2 = (String) this.mapGroupOwnerToDeviceName.get(str);
        return str2 != null ? str2 : str;
    }

    public CallbackResult handleCommandRobotControllerPreference(String str) {
        RobotControllerPreference deserialize = RobotControllerPreference.deserialize(str);
        RobotLog.m61vv(getTag(), "handleRobotControllerPreference() pref=%s", deserialize.getPrefName());
        if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_sound_on_off))) {
            if (this.preferencesHelper.readBoolean(this.context.getString(C0705R.string.pref_has_speaker_rc), true)) {
                this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_sound_on_off_rc), deserialize.getValue());
            } else {
                this.preferencesHelper.writeBooleanPrefIfDifferent(this.context.getString(C0705R.string.pref_sound_on_off_rc), false);
            }
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_wifip2p_channel))) {
            int intValue = ((Integer) deserialize.getValue()).intValue();
            RobotLog.m61vv(TAG, "pref_wifip2p_channel: prefChannel = %d", Integer.valueOf(intValue));
            this.preferencesHelper.writeIntPrefIfDifferent(this.context.getString(C0705R.string.pref_wifip2p_channel), intValue);
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_has_speaker))) {
            this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_has_speaker_rc), deserialize.getValue());
            if (!this.preferencesHelper.readBoolean(this.context.getString(C0705R.string.pref_has_speaker_rc), true)) {
                this.preferencesHelper.writeBooleanPrefIfDifferent(this.context.getString(C0705R.string.pref_sound_on_off_rc), false);
            }
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_app_theme))) {
            this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_app_theme_rc), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_device_name))) {
            this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_device_name_rc), deserialize.getValue());
            this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_device_name_rc_display), deserialize.getValue());
            String readString = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_wifip2p_groupowner_connectedto), InspectionState.NO_VERSION);
            if (readString.isEmpty()) {
                readString = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_wifip2p_groupowner_lastconnectedto), InspectionState.NO_VERSION);
            }
            if (!readString.isEmpty()) {
                this.mapGroupOwnerToDeviceName.put(readString, (String) deserialize.getValue());
                saveRenameMap();
            } else {
                RobotLog.m48ee(TAG, "odd: we got a name change from an RC we're not actually connected to");
            }
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_wifip2p_remote_channel_change_works))) {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_has_independent_phone_battery))) {
            this.preferencesHelper.writePrefIfDifferent(this.context.getString(C0705R.string.pref_has_independent_phone_battery_rc), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_warn_about_obsolete_software))) {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_warn_about_mismatched_app_versions))) {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_warn_about_2_4_ghz_band))) {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        } else if (deserialize.getPrefName().equals(this.context.getString(C0705R.string.pref_warn_about_incorrect_clocks))) {
            this.preferencesHelper.writePrefIfDifferent(deserialize.getPrefName(), deserialize.getValue());
        }
        return CallbackResult.HANDLED;
    }

    protected class SharedPreferencesListenerDS implements SharedPreferences.OnSharedPreferenceChangeListener {
        protected SharedPreferencesListenerDS() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            String str2;
            Object readPref;
            RobotLog.m61vv(PreferenceRemoterDS.TAG, "onSharedPreferenceChanged(name=%s, value=%s)", str, PreferenceRemoterDS.this.preferencesHelper.readPref(str));
            if (str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_sound_on_off_rc))) {
                str2 = PreferenceRemoterDS.this.context.getString(C0705R.string.pref_sound_on_off);
            } else if (str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_device_name_rc))) {
                str2 = PreferenceRemoterDS.this.context.getString(C0705R.string.pref_device_name);
            } else if (str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_app_theme_rc))) {
                str2 = PreferenceRemoterDS.this.context.getString(C0705R.string.pref_app_theme);
            } else if (str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_wifip2p_channel))) {
                str2 = PreferenceRemoterDS.this.context.getString(C0705R.string.pref_wifip2p_channel);
            } else {
                str2 = (!str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_warn_about_obsolete_software)) && !str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_warn_about_mismatched_app_versions)) && !str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_warn_about_2_4_ghz_band)) && !str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_warn_about_incorrect_clocks))) ? null : str;
            }
            if (!(str2 == null || (readPref = PreferenceRemoterDS.this.preferencesHelper.readPref(str)) == null)) {
                PreferenceRemoterDS.this.sendPreference(new RobotControllerPreference(str2, readPref));
            }
            if (str.equals(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_wifip2p_groupowner_connectedto))) {
                String readString = PreferenceRemoterDS.this.preferencesHelper.readString(str, InspectionState.NO_VERSION);
                if (!readString.isEmpty()) {
                    PreferenceRemoterDS.this.preferencesHelper.writePrefIfDifferent(PreferenceRemoterDS.this.context.getString(C0705R.string.pref_wifip2p_groupowner_lastconnectedto), readString);
                } else {
                    RobotLog.m61vv(PreferenceRemoterDS.TAG, "%s has been removed", str);
                }
            }
        }
    }

    public void sendInformationalPrefsToRc() {
        try {
            int i = this.context.getPackageManager().getPackageInfo(this.context.getPackageName(), 0).versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        sendPreference(new RobotControllerPreference(this.context.getString(C0705R.string.pref_ds_supports_5_ghz), Boolean.valueOf(WifiUtil.is5GHzAvailable())));
        if (Device.isRevDriverHub()) {
            sendPreference(new RobotControllerPreference(this.context.getString(C0705R.string.pref_dh_os_version_code), Integer.valueOf(LynxConstants.getDriverHubOsVersionCode())));
        }
    }
}
