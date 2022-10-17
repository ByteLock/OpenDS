package com.qualcomm.robotcore.util;

import android.app.Application;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.robocol.PeerDiscovery;
import java.util.Arrays;
import java.util.List;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.threeten.p014bp.Year;
import org.threeten.p014bp.YearMonth;

public class SoftwareVersionWarningSource implements GlobalWarningSource, PeerStatusCallback, SharedPreferences.OnSharedPreferenceChangeListener {
    private static final String MISMATCHED_APPS_PREF;
    private static final String OBSOLETE_SOFTWARE_PREF;
    private static final AppUtil appUtil = AppUtil.getInstance();
    private static final Application context;
    private static final SoftwareVersionWarningSource instance = new SoftwareVersionWarningSource();
    private static final SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(AppUtil.getDefContext());
    private static final List<String> relevantWarningPreferences;
    private final boolean chOsIsObsolete;
    private volatile boolean dhOsIsObsolete = false;
    private volatile boolean dsIsObsolete = false;
    private volatile MismatchedAppsDetail mismatchedAppsDetail = null;
    private volatile boolean needToAnalyzeDsPeerDiscovery = true;
    private volatile boolean rcIsObsolete;
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
        String string = defContext.getString(C0705R.string.pref_warn_about_obsolete_software);
        OBSOLETE_SOFTWARE_PREF = string;
        String string2 = defContext.getString(C0705R.string.pref_warn_about_mismatched_app_versions);
        MISMATCHED_APPS_PREF = string2;
        relevantWarningPreferences = Arrays.asList(new String[]{string, string2});
    }

    public static SoftwareVersionWarningSource getInstance() {
        return instance;
    }

    private SoftwareVersionWarningSource() {
        boolean z = true;
        RobotLog.registerGlobalWarningSource(this);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        preferences.registerOnSharedPreferenceChangeListener(this);
        this.rcIsObsolete = appUtil.localAppIsObsolete();
        this.chOsIsObsolete = (!Device.isRevControlHub() || !LynxConstants.controlHubOsVersionIsObsolete()) ? false : z;
        refreshWarning();
    }

    public void onReceivedPeerDiscoveryFromCurrentPeer(PeerDiscovery peerDiscovery) {
        if (this.needToAnalyzeDsPeerDiscovery && !waitingForTimeFromDs()) {
            boolean z = false;
            this.needToAnalyzeDsPeerDiscovery = false;
            int sdkMajorVersion = peerDiscovery.getSdkMajorVersion();
            int sdkMinorVersion = peerDiscovery.getSdkMinorVersion();
            YearMonth sdkBuildMonth = peerDiscovery.getSdkBuildMonth();
            AppUtil appUtil2 = appUtil;
            if (appUtil2.appIsObsolete(sdkBuildMonth)) {
                this.dsIsObsolete = true;
            } else {
                Year ftcSeasonYear = appUtil2.getFtcSeasonYear(sdkBuildMonth);
                Year ftcSeasonYear2 = appUtil2.getFtcSeasonYear(appUtil2.getLocalSdkBuildMonth());
                if (ftcSeasonYear.equals(ftcSeasonYear2)) {
                    this.dsIsObsolete = false;
                } else if (ftcSeasonYear2.isBefore(ftcSeasonYear)) {
                    this.rcIsObsolete = true;
                    this.dsIsObsolete = false;
                } else {
                    this.dsIsObsolete = true;
                }
            }
            if (!(sdkMajorVersion == 8 && sdkMinorVersion == 0)) {
                z = true;
            }
            if (z) {
                String str = "Robot Controller";
                if (8 != sdkMajorVersion ? 8 >= sdkMajorVersion : sdkMinorVersion <= 0) {
                    str = "Driver Station";
                }
                this.mismatchedAppsDetail = new MismatchedAppsDetail(str);
            } else {
                this.mismatchedAppsDetail = null;
            }
            refreshWarning();
        }
    }

    public void onReceivedDriverHubOsVersionCode(int i) {
        this.dhOsIsObsolete = i < 21;
        refreshWarning();
    }

    public String getGlobalWarning() {
        return this.warning;
    }

    public void onPeerDisconnected() {
        this.needToAnalyzeDsPeerDiscovery = true;
        this.dsIsObsolete = false;
        this.dhOsIsObsolete = false;
        this.mismatchedAppsDetail = null;
        refreshWarning();
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        if (relevantWarningPreferences.contains(str)) {
            refreshWarning();
        }
    }

    private boolean waitingForTimeFromDs() {
        return Device.isRevControlHub() && !appUtil.isSaneWallClockTime(System.currentTimeMillis());
    }

    private void refreshWarning() {
        String str;
        SharedPreferences sharedPreferences = preferences;
        boolean z = sharedPreferences.getBoolean(OBSOLETE_SOFTWARE_PREF, true);
        boolean z2 = sharedPreferences.getBoolean(MISMATCHED_APPS_PREF, true);
        MismatchedAppsDetail mismatchedAppsDetail2 = this.mismatchedAppsDetail;
        String str2 = null;
        if ((this.rcIsObsolete || this.dsIsObsolete) && z) {
            str = (!this.rcIsObsolete || !this.dsIsObsolete) ? this.rcIsObsolete ? context.getString(C0705R.string.warningRcAppObsolete) : context.getString(C0705R.string.warningDsAppObsolete) : context.getString(C0705R.string.warningBothAppsObsolete);
        } else {
            str = (mismatchedAppsDetail2 == null || !z2) ? null : context.getString(C0705R.string.warningMismatchedAppVersions, new Object[]{mismatchedAppsDetail2.oldApp});
        }
        String string = (!this.chOsIsObsolete || !z) ? null : context.getString(C0705R.string.warningChOsObsolete, new Object[]{LynxConstants.MINIMUM_LEGAL_CH_OS_VERSION_STRING});
        if (this.dhOsIsObsolete && z) {
            str2 = context.getString(C0705R.string.warningDhOsObsolete, new Object[]{LynxConstants.MINIMUM_LEGAL_DH_OS_VERSION_STRING});
        }
        this.warning = RobotLog.combineGlobalWarnings(Arrays.asList(new String[]{str, string, str2}));
    }

    private static class MismatchedAppsDetail {
        final String oldApp;

        private MismatchedAppsDetail(String str) {
            this.oldApp = str;
        }
    }
}
