package com.qualcomm.ftccommon;

import android.app.Application;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.preference.Preference;
import android.preference.PreferenceCategory;
import android.preference.PreferenceFragment;
import android.text.TextUtils;
import android.widget.FrameLayout;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.util.Version;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import java.util.Locale;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.inspection.C1275R;
import org.threeten.p014bp.LocalDateTime;
import org.threeten.p014bp.ZoneId;
import org.threeten.p014bp.format.DateTimeFormatter;
import org.threeten.p014bp.format.DateTimeParseException;
import org.threeten.p014bp.format.FormatStyle;
import p007fi.iki.elonen.NanoHTTPD;

public class FtcAboutActivity extends ThemedActivity {
    public static final String TAG = "FtcDriverStationAboutActivity";
    private static String buildTimeFromBuildConfig;
    protected AboutFragment aboutFragment;
    protected final Context context = AppUtil.getDefContext();
    final RecvLoopRunnable.RecvLoopCallback recvLoopCallback = new RecvLoopRunnable.DegenerateCallback() {
        public CallbackResult commandEvent(Command command) {
            RobotLog.m61vv(FtcAboutActivity.TAG, "commandEvent: %s", command.getName());
            if (FtcAboutActivity.this.remoteConfigure) {
                String name = command.getName();
                name.hashCode();
                if (name.equals(RobotCoreCommandList.CMD_REQUEST_ABOUT_INFO_RESP)) {
                    final RobotCoreCommandList.AboutInfo deserialize = RobotCoreCommandList.AboutInfo.deserialize(command.getExtra());
                    AppUtil.getInstance().runOnUiThread(new Runnable() {
                        public void run() {
                            FtcAboutActivity.this.refreshRemote(deserialize);
                        }
                    });
                    return CallbackResult.HANDLED;
                }
            }
            return CallbackResult.NOT_HANDLED;
        }
    };
    protected Future refreshFuture = null;
    protected final boolean remoteConfigure = AppUtil.getInstance().isDriverStation();

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    public static RobotCoreCommandList.AboutInfo getLocalAboutInfo() {
        RobotCoreCommandList.AboutInfo aboutInfo = new RobotCoreCommandList.AboutInfo();
        aboutInfo.appVersion = getAppVersion();
        aboutInfo.libVersion = Version.getLibraryVersion();
        aboutInfo.buildTime = getBuildTime();
        aboutInfo.networkProtocolVersion = String.format(Locale.US, "v%d", new Object[]{123});
        if (Device.isRevControlHub()) {
            aboutInfo.osVersion = LynxConstants.getControlHubOsVersion();
        } else if (Device.isRevDriverHub()) {
            aboutInfo.osVersion = LynxConstants.getDriverHubOsVersion();
        }
        NetworkConnection networkConnection = NetworkConnectionHandler.getInstance().getNetworkConnection();
        if (networkConnection != null) {
            aboutInfo.networkConnectionInfo = networkConnection.getInfo();
        } else {
            aboutInfo.networkConnectionInfo = AppUtil.getDefContext().getString(C0470R.string.unavailable);
        }
        return aboutInfo;
    }

    protected static String getAppVersion() {
        Application defContext = AppUtil.getDefContext();
        try {
            return defContext.getPackageManager().getPackageInfo(defContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException unused) {
            return defContext.getString(C0470R.string.unavailable);
        }
    }

    public static void setBuildTimeFromBuildConfig(String str) {
        buildTimeFromBuildConfig = str;
    }

    protected static String getBuildTime() {
        try {
            return DateTimeFormatter.ofLocalizedDateTime(FormatStyle.LONG).withZone(ZoneId.systemDefault()).format(LocalDateTime.parse(buildTimeFromBuildConfig, AppUtil.getInstance().getIso8601DateTimeFormatter()));
        } catch (DateTimeParseException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "exception determining build time");
            return AppUtil.getDefContext().getString(C0470R.string.unavailable);
        }
    }

    public static class AboutFragment extends PreferenceFragment {
        private boolean firstRemoteRefresh = true;
        protected final boolean remoteConfigure = AppUtil.getInstance().isDriverStation();

        public void refreshLocal(RobotCoreCommandList.AboutInfo aboutInfo) {
            setPreferenceSummary(C0470R.string.pref_app_version, aboutInfo.appVersion);
            setPreferenceSummary(C0470R.string.pref_lib_version, aboutInfo.libVersion);
            setPreferenceSummary(C0470R.string.pref_network_protocol_version, aboutInfo.networkProtocolVersion);
            setPreferenceSummary(C0470R.string.pref_build_time, aboutInfo.buildTime);
            setPreferenceSummary(C0470R.string.pref_network_connection_info, aboutInfo.networkConnectionInfo);
            setPreferenceSummary(C0470R.string.pref_os_version, aboutInfo.osVersion);
        }

        public void refreshRemote(RobotCoreCommandList.AboutInfo aboutInfo) {
            if (this.remoteConfigure) {
                if (this.firstRemoteRefresh && aboutInfo.osVersion != null) {
                    Preference preference = new Preference(getPreferenceScreen().getContext());
                    preference.setTitle(getString(C0470R.string.about_ch_os_version));
                    preference.setKey(getString(C0470R.string.pref_os_version_rc));
                    ((PreferenceCategory) findPreference(getString(C0470R.string.pref_app_category_rc))).addPreference(preference);
                }
                this.firstRemoteRefresh = false;
                setPreferenceSummary(C0470R.string.pref_app_version_rc, aboutInfo.appVersion);
                setPreferenceSummary(C0470R.string.pref_lib_version_rc, aboutInfo.libVersion);
                setPreferenceSummary(C0470R.string.pref_network_protocol_version_rc, aboutInfo.networkProtocolVersion);
                setPreferenceSummary(C0470R.string.pref_build_time_rc, aboutInfo.buildTime);
                setPreferenceSummary(C0470R.string.pref_network_connection_info_rc, aboutInfo.networkConnectionInfo);
                setPreferenceSummary(C0470R.string.pref_os_version_rc, aboutInfo.osVersion);
            }
        }

        public void refreshAllUnavailable() {
            setPreferenceSummary(C0470R.string.pref_app_version, (String) null);
            setPreferenceSummary(C0470R.string.pref_lib_version, (String) null);
            setPreferenceSummary(C0470R.string.pref_network_protocol_version, (String) null);
            setPreferenceSummary(C0470R.string.pref_build_time, (String) null);
            setPreferenceSummary(C0470R.string.pref_network_connection_info, (String) null);
            setPreferenceSummary(C0470R.string.pref_os_version, (String) null);
            setPreferenceSummary(C0470R.string.pref_app_version_rc, (String) null);
            setPreferenceSummary(C0470R.string.pref_lib_version_rc, (String) null);
            setPreferenceSummary(C0470R.string.pref_network_protocol_version_rc, (String) null);
            setPreferenceSummary(C0470R.string.pref_build_time_rc, (String) null);
            setPreferenceSummary(C0470R.string.pref_network_connection_info_rc, (String) null);
            setPreferenceSummary(C0470R.string.pref_os_version_rc, (String) null);
        }

        public void onCreate(Bundle bundle) {
            String str;
            super.onCreate(bundle);
            addPreferencesFromResource(C0470R.xml.ftc_about_activity);
            PreferenceCategory preferenceCategory = (PreferenceCategory) findPreference(getString(C0470R.string.pref_app_category));
            preferenceCategory.setTitle(this.remoteConfigure ? C0470R.string.prefcat_about_ds : C0470R.string.prefcat_about_rc);
            if (Device.isRevControlHub() || Device.isRevDriverHub()) {
                if (Device.isRevControlHub()) {
                    str = getString(C0470R.string.about_ch_os_version);
                } else {
                    str = getString(C0470R.string.about_dh_os_version);
                }
                Preference preference = new Preference(getPreferenceScreen().getContext());
                preference.setTitle(str);
                preference.setKey(getString(C0470R.string.pref_os_version));
                preferenceCategory.addPreference(preference);
            }
            if (this.remoteConfigure) {
                addPreferencesFromResource(C0470R.xml.ftc_about_activity_rc);
                findPreference(getString(C0470R.string.pref_app_category_rc)).setTitle(C0470R.string.prefcat_about_rc);
            }
            refreshAllUnavailable();
        }

        /* access modifiers changed from: protected */
        public void setPreferenceSummary(int i, String str) {
            setPreferenceSummary(AppUtil.getDefContext().getString(i), str);
        }

        /* access modifiers changed from: protected */
        public void setPreferenceSummary(String str, String str2) {
            if (TextUtils.isEmpty(str2)) {
                str2 = AppUtil.getDefContext().getString(C0470R.string.unavailable);
            }
            Preference findPreference = findPreference(str);
            if (findPreference != null) {
                findPreference.setSummary(str2);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void startRefreshing() {
        stopRefreshing();
        this.refreshFuture = ThreadPool.getDefaultScheduler().scheduleAtFixedRate(new Runnable() {
            public void run() {
                AppUtil.getInstance().runOnUiThread(new Runnable() {
                    public void run() {
                        FtcAboutActivity.this.refresh();
                    }
                });
            }
        }, 0, (long) NanoHTTPD.SOCKET_READ_TIMEOUT, TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: protected */
    public void stopRefreshing() {
        Future future = this.refreshFuture;
        if (future != null) {
            future.cancel(false);
            this.refreshFuture = null;
        }
    }

    /* access modifiers changed from: protected */
    public void refreshRemote(RobotCoreCommandList.AboutInfo aboutInfo) {
        this.aboutFragment.refreshRemote(aboutInfo);
    }

    /* access modifiers changed from: protected */
    public void refresh() {
        this.aboutFragment.refreshLocal(getLocalAboutInfo());
        if (this.remoteConfigure) {
            NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_ABOUT_INFO));
        }
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        RobotLog.m60vv(TAG, "onCreate()");
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_generic_settings);
        DeviceNameManagerFactory.getInstance().initializeDeviceNameIfNecessary();
        this.aboutFragment = new AboutFragment();
        getFragmentManager().beginTransaction().replace(C0470R.C0472id.container, this.aboutFragment).commit();
        NetworkConnectionHandler.getInstance().pushReceiveLoopCallback(this.recvLoopCallback);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        startRefreshing();
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        stopRefreshing();
        super.onPause();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        RobotLog.m60vv(TAG, "onDestroy()");
        super.onDestroy();
        NetworkConnectionHandler.getInstance().removeReceiveLoopCallback(this.recvLoopCallback);
    }
}
