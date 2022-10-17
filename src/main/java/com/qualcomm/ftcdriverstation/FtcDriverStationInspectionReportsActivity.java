package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;
import java.util.ArrayList;
import org.firstinspires.directgamepadaccess.core.UsbGamepad;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.p013ui.BaseActivity;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.RcInspectionActivity;

public class FtcDriverStationInspectionReportsActivity extends BaseActivity {
    protected static final String CLIENT_CONNECTED = "CLIENT_CONNECTED";
    public static final String TAG = "FtcDriverStationInspectionReportsActivity";

    public String getTag() {
        return TAG;
    }

    public static class SettingsFragment extends PreferenceFragment {
        protected boolean clientConnected = false;

        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            this.clientConnected = getArguments().getBoolean(FtcDriverStationInspectionReportsActivity.CLIENT_CONNECTED);
            addPreferencesFromResource(C0648R.xml.inspection);
            Preference findPreference = findPreference(getString(C0648R.string.pref_launch_inspect_rc));
            if (!this.clientConnected) {
                findPreference.setEnabled(false);
            } else {
                findPreference.setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                    public boolean onPreferenceClick(Preference preference) {
                        ArrayList arrayList = new ArrayList();
                        for (UsbGamepad next : FtcDriverStationActivity.gamepadManager.getKnownGamepads()) {
                            if (next != null) {
                                arrayList.add(Integer.valueOf(next.getVid()));
                                arrayList.add(Integer.valueOf(next.getPid()));
                            }
                        }
                        Intent intent = new Intent(AppUtil.getDefContext(), RcInspectionActivity.class);
                        intent.putIntegerArrayListExtra(RcInspectionActivity.GAMEPAD_KEY, arrayList);
                        intent.putExtra(RcInspectionActivity.ADVANCED_FEATURES_KEY, FtcDriverStationActivity.gamepadManager.isAdvancedFeatures());
                        SettingsFragment.this.startActivity(intent);
                        return true;
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C0648R.C0650id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0648R.layout.activity_generic_settings);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        SettingsFragment settingsFragment = new SettingsFragment();
        Bundle bundle2 = new Bundle();
        bundle2.putBoolean(CLIENT_CONNECTED, new PreferencesHelper(TAG, (Context) this).readBoolean(getString(C0648R.string.pref_rc_connected), false));
        settingsFragment.setArguments(bundle2);
        getFragmentManager().beginTransaction().replace(C0648R.C0650id.container, settingsFragment).commit();
    }
}
