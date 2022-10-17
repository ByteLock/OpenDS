package com.qualcomm.ftcdriverstation;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.util.Device;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.system.PermissionValidatorActivity;

public class PermissionValidatorWrapper extends PermissionValidatorActivity {
    private final String TAG = "PermissionValidatorWrapper";
    protected List<String> driverStationPermissions = new ArrayList<String>() {
        {
            add("android.permission.WRITE_EXTERNAL_STORAGE");
            add("android.permission.READ_EXTERNAL_STORAGE");
            add("android.permission.ACCESS_FINE_LOCATION");
        }
    };
    private SharedPreferences sharedPreferences;

    public String mapPermissionToExplanation(String str) {
        if (str.equals("android.permission.WRITE_EXTERNAL_STORAGE")) {
            return Misc.formatForUser((int) C0648R.string.permDsWriteExternalStorageExplain);
        }
        if (str.equals("android.permission.READ_EXTERNAL_STORAGE")) {
            return Misc.formatForUser((int) C0648R.string.permDsReadExternalStorageExplain);
        }
        if (str.equals("android.permission.ACCESS_FINE_LOCATION")) {
            return Misc.formatForUser((int) C0648R.string.permAccessLocationExplain);
        }
        return Misc.formatForUser((int) C0648R.string.permGenericExplain);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.permissions = this.driverStationPermissions;
    }

    /* access modifiers changed from: protected */
    public Class onStartApplication() {
        FtcDriverStationActivity.setPermissionsValidated();
        String string = getResources().getString(C0648R.string.pref_pairing_kind);
        String string2 = getResources().getString(C0648R.string.key_ds_layout);
        if (!Device.isRevDriverHub()) {
            return FtcDriverStationActivity.class;
        }
        if (!this.sharedPreferences.contains(string2)) {
            this.sharedPreferences.edit().putString(string2, getResources().getString(C0648R.string.ds_ui_land)).commit();
        }
        if (this.sharedPreferences.contains(string)) {
            return FtcDriverStationActivity.class;
        }
        this.sharedPreferences.edit().putString(string, getResources().getString(C0648R.string.network_type_wireless_ap)).commit();
        return FtcDriverStationActivity.class;
    }
}
