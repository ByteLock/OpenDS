package org.firstinspires.ftc.robotcore.internal.network;

import android.app.AlertDialog;
import android.bluetooth.BluetoothAdapter;
import android.content.DialogInterface;
import android.content.Intent;
import android.location.LocationManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.provider.Settings;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import java.lang.reflect.InvocationTargetException;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.inspection.InspectionState;

public class WifiUtil {
    private static final String NO_AP = "None";
    private static Boolean deviceSupports5Ghz = null;
    public static final LocationManager locationManager = ((LocationManager) AppUtil.getDefContext().getSystemService("location"));
    /* access modifiers changed from: private */
    public static boolean showingLocationServicesDlg = false;
    private static final WifiManager wifiManager = ((WifiManager) AppUtil.getDefContext().getApplicationContext().getSystemService("wifi"));

    public static boolean isAirplaneModeOn() {
        return Settings.Global.getInt(AppUtil.getDefContext().getContentResolver(), "airplane_mode_on", 0) != 0;
    }

    public static boolean isBluetoothOn() {
        return BluetoothAdapter.getDefaultAdapter() != null && BluetoothAdapter.getDefaultAdapter().isEnabled();
    }

    public static boolean isWifiEnabled() {
        WifiManager wifiManager2 = wifiManager;
        int wifiState = wifiManager2.getWifiState();
        RobotLog.m52i("state = " + wifiState);
        return wifiManager2.isWifiEnabled();
    }

    public static boolean isWifiApEnabled() {
        try {
            WifiManager wifiManager2 = wifiManager;
            return ((Boolean) wifiManager2.getClass().getMethod("isWifiApEnabled", new Class[0]).invoke(wifiManager2, new Object[0])).booleanValue();
        } catch (IllegalAccessException | NoSuchMethodException | InvocationTargetException e) {
            RobotLog.m46e("Could not invoke isWifiApEnabled " + e.toString());
            return false;
        }
    }

    public static boolean isWifiConnected() {
        if (!isWifiEnabled()) {
            return false;
        }
        NetworkInfo.DetailedState detailedStateOf = WifiInfo.getDetailedStateOf(wifiManager.getConnectionInfo().getSupplicantState());
        if (detailedStateOf == NetworkInfo.DetailedState.CONNECTED || detailedStateOf == NetworkInfo.DetailedState.OBTAINING_IPADDR) {
            return true;
        }
        return false;
    }

    public static boolean areLocationServicesEnabled() {
        int i;
        LocationManager locationManager2 = locationManager;
        if (locationManager2 == null) {
            return false;
        }
        if (Build.VERSION.SDK_INT >= 28) {
            return locationManager2.isLocationEnabled();
        }
        try {
            i = Settings.Secure.getInt(AppUtil.getDefContext().getContentResolver(), "location_mode");
        } catch (Settings.SettingNotFoundException unused) {
            i = 0;
        }
        if (i != 0) {
            return true;
        }
        return false;
    }

    public static void doLocationServicesCheck() {
        if (Build.VERSION.SDK_INT >= 26 && !AppUtil.getInstance().isRobotController() && !showingLocationServicesDlg && !areLocationServicesEnabled()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(AppUtil.getInstance().getActivity());
            builder.setMessage(Misc.formatForUser(C0705R.string.locationServices));
            builder.setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    AppUtil.getInstance().getActivity().startActivity(new Intent("android.settings.LOCATION_SOURCE_SETTINGS"));
                    boolean unused = WifiUtil.showingLocationServicesDlg = false;
                }
            });
            builder.create();
            builder.show();
            showingLocationServicesDlg = true;
        }
    }

    public static String getConnectedSsid() {
        if (!isWifiConnected()) {
            return NO_AP;
        }
        return wifiManager.getConnectionInfo().getSSID().replace("\"", InspectionState.NO_VERSION);
    }

    public static boolean is5GHzAvailable() {
        if (deviceSupports5Ghz == null) {
            if (Device.isRevControlHub()) {
                deviceSupports5Ghz = Boolean.valueOf(AndroidBoard.getInstance().supports5GhzAp());
            } else {
                deviceSupports5Ghz = Boolean.valueOf(wifiManager.is5GHzBandSupported());
            }
        }
        return deviceSupports5Ghz.booleanValue();
    }
}
