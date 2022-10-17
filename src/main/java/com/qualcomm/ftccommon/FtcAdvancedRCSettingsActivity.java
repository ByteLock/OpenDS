package com.qualcomm.ftccommon;

import android.content.Context;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.wifi.NetworkType;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.p013ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.C1275R;

public class FtcAdvancedRCSettingsActivity extends ThemedActivity {
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    public static final String TAG = "FtcAdvancedRCSettingsActivity";

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    public static class SettingsFragment extends PreferenceFragment {
        protected boolean clientConnected = false;
        protected boolean controlHubConnectionMode;
        protected PreferencesHelper preferencesHelper;
        protected boolean remoteConfigure = AppUtil.getInstance().isDriverStation();

        public SettingsFragment() {
            boolean z = false;
            this.controlHubConnectionMode = NetworkConnectionHandler.getNetworkType(AppUtil.getDefContext()) == NetworkType.WIRELESSAP ? true : z;
            this.preferencesHelper = new PreferencesHelper(FtcAdvancedRCSettingsActivity.TAG);
        }

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.clientConnected = getArguments().getBoolean(FtcAdvancedRCSettingsActivity.CLIENT_CONNECTED);
            addPreferencesFromResource(C0470R.xml.advanced_rc_settings);
            Preference findPreference = findPreference(getString(C0470R.string.pref_launch_wifi_remembered_groups_edit));
            Preference findPreference2 = findPreference(getString(C0470R.string.pref_launch_wifi_channel_edit));
            Preference findPreference3 = findPreference(getString(C0470R.string.pref_launch_lynx_firmware_update));
            if (LynxConstants.isRevControlHub() || this.controlHubConnectionMode) {
                findPreference3.setSummary(C0470R.string.summaryLynxFirmwareUpdateCH);
            }
            if (!this.clientConnected) {
                for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                    getPreferenceScreen().getPreference(i).setEnabled(false);
                }
            }
            boolean z = (this.remoteConfigure && LynxConstants.isRevControlHub()) || (this.clientConnected && !this.preferencesHelper.readBoolean(getString(C0470R.string.pref_wifip2p_remote_channel_change_works), false));
            findPreference2.setEnabled(!z);
            findPreference.setEnabled(!z);
            RobotLog.m61vv(FtcAdvancedRCSettingsActivity.TAG, "clientConnected=%s", Boolean.valueOf(this.clientConnected));
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_generic_settings);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CLIENT_CONNECTED, new PreferencesHelper(TAG, (Context) this).readBoolean(getString(C0470R.string.pref_rc_connected), false));
        settingsFragment.setArguments(bundle2);
        getFragmentManager().beginTransaction().replace(C0470R.C0472id.container, settingsFragment).commit();
    }
}
