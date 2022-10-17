package com.qualcomm.ftccommon;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.widget.FrameLayout;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectDeviceNameManager;
import org.firstinspires.ftc.robotcore.internal.p013ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class FtcRobotControllerSettingsActivity extends ThemedActivity {
    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C0470R.C0472id.backbar);
    }

    public static class SettingsFragment extends PreferenceFragment {
        public void onCreate(Bundle bundle) {
            super.onCreate(bundle);
            addPreferencesFromResource(C0470R.xml.app_settings);
            findPreference(getString(C0470R.string.pref_device_name)).setOnPreferenceChangeListener(new Preference.OnPreferenceChangeListener() {
                public boolean onPreferenceChange(Preference preference, Object obj) {
                    if ((obj instanceof String) && WifiDirectDeviceNameManager.validDeviceName((String) obj)) {
                        return true;
                    }
                    AlertDialog.Builder builder = new AlertDialog.Builder(SettingsFragment.this.getActivity());
                    builder.setTitle(SettingsFragment.this.getString(C0470R.string.prefedit_device_name_invalid_title));
                    builder.setMessage(SettingsFragment.this.getString(C0470R.string.prefedit_device_name_invalid_text));
                    builder.setPositiveButton(17039370, (DialogInterface.OnClickListener) null);
                    builder.show();
                    return false;
                }
            });
            findPreference(getString(C0470R.string.pref_launch_viewlogs)).setOnPreferenceClickListener(new Preference.OnPreferenceClickListener() {
                public boolean onPreferenceClick(Preference preference) {
                    Intent intent = new Intent(AppUtil.getDefContext(), ViewLogsActivity.class);
                    intent.putExtra("org.firstinspires.ftc.ftccommon.logFilename", RobotLog.getLogFilename(SettingsFragment.this.getActivity()));
                    SettingsFragment.this.startActivity(intent);
                    return true;
                }
            });
            if (!new PreferencesHelper(getTag()).readBoolean(getString(C0470R.string.pref_has_speaker), true)) {
                findPreference(getString(C0470R.string.pref_sound_on_off)).setEnabled(false);
            }
        }

        public void onActivityResult(int i, int i2, Intent intent) {
            if ((i == LaunchActivityConstantsList.RequestCode.CONFIGURE_ROBOT_CONTROLLER.ordinal() || i == LaunchActivityConstantsList.RequestCode.SETTINGS_ROBOT_CONTROLLER.ordinal()) && i2 == -1) {
                getActivity().setResult(-1, intent);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_generic_settings);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        getFragmentManager().beginTransaction().replace(C0470R.C0472id.container, new SettingsFragment()).commit();
    }
}
