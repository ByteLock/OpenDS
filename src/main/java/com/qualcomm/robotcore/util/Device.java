package com.qualcomm.robotcore.util;

import android.app.UiModeManager;
import android.os.Build;
import androidx.core.p003os.EnvironmentCompat;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import org.firstinspires.ftc.robotcore.network.WifiUtil;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.SystemProperties;
import org.firstinspires.inspection.InspectionState;

public final class Device {
    private static final boolean DISABLE_FALLBACK_SERIAL_NUMBER_RETRIEVAL = false;
    public static final String MANUFACTURER_MOTOROLA = "motorola";
    public static final String MANUFACTURER_REV = "REV Robotics";
    public static final String MODEL_E4 = "Moto E (4)";
    public static final String MODEL_E5_PLAY = "moto e5 play";
    public static final String MODEL_E5_XT1920DL = "moto e5 (XT1920DL)";
    private static final String SERIAL_NUMBER_PROPERTY = "ro.serialno";
    private static final String SERIAL_NUMBER_RETRIEVAL_COMMAND = "getprop ro.serialno";
    public static final String TAG = "Device";
    private static String cachedSerialNumberOrUnknown;
    private static Boolean hasBackButton;
    private static Boolean isDriverHub;
    private static Boolean isMoto;
    private static LinuxKernelVersion kernelVersion;
    private static final UiModeManager uiManager = ((UiModeManager) AppUtil.getDefContext().getSystemService("uimode"));

    public static LinuxKernelVersion getLinuxKernelVersion() {
        if (kernelVersion == null) {
            kernelVersion = new LinuxKernelVersion(System.getProperty("os.version"));
        }
        return kernelVersion;
    }

    public static boolean isMotorola() {
        if (isMoto == null) {
            isMoto = Boolean.valueOf(Build.MANUFACTURER.equalsIgnoreCase(MANUFACTURER_MOTOROLA));
        }
        return isMoto.booleanValue();
    }

    public static boolean isRevDriverHub() {
        if (isDriverHub == null) {
            isDriverHub = Boolean.valueOf(SystemProperties.getBoolean("persist.rds", false));
        }
        return isDriverHub.booleanValue();
    }

    public static boolean deviceHasBackButton() {
        if (hasBackButton == null) {
            boolean z = true;
            if (uiManager.getCurrentModeType() != 1) {
                z = false;
            }
            hasBackButton = Boolean.valueOf(z);
        }
        return hasBackButton.booleanValue();
    }

    public static boolean phoneImplementsAggressiveWifiScanning() {
        return isMotorola() && WifiUtil.is5GHzAvailable();
    }

    public static boolean wifiP2pRemoteChannelChangeWorks() {
        return !isRevControlHub();
    }

    public static boolean isRevControlHub() {
        return LynxConstants.isRevControlHub();
    }

    public static String getSerialNumber() throws AndroidSerialNumberNotFoundException {
        if (cachedSerialNumberOrUnknown == null) {
            try {
                cachedSerialNumberOrUnknown = internalGetSerialNumber();
            } catch (AndroidSerialNumberNotFoundException unused) {
                cachedSerialNumberOrUnknown = EnvironmentCompat.MEDIA_UNKNOWN;
            }
        }
        if (!cachedSerialNumberOrUnknown.isEmpty() && !cachedSerialNumberOrUnknown.equals(EnvironmentCompat.MEDIA_UNKNOWN)) {
            return cachedSerialNumberOrUnknown;
        }
        throw new AndroidSerialNumberNotFoundException();
    }

    public static String getSerialNumberOrUnknown() {
        try {
            return getSerialNumber();
        } catch (AndroidSerialNumberNotFoundException unused) {
            return EnvironmentCompat.MEDIA_UNKNOWN;
        }
    }

    private static String internalGetSerialNumber() throws AndroidSerialNumberNotFoundException {
        String str;
        if (Build.VERSION.SDK_INT < 26) {
            str = Build.SERIAL;
        } else {
            try {
                str = Build.getSerial();
            } catch (SecurityException unused) {
                str = EnvironmentCompat.MEDIA_UNKNOWN;
            }
        }
        if (!str.isEmpty() && !str.equals(EnvironmentCompat.MEDIA_UNKNOWN)) {
            return str;
        }
        String str2 = SystemProperties.get(SERIAL_NUMBER_PROPERTY, EnvironmentCompat.MEDIA_UNKNOWN);
        if (!str2.equals(EnvironmentCompat.MEDIA_UNKNOWN)) {
            return str2;
        }
        RobotLog.m66ww(TAG, "Failed to find Android serial number through Android API. Using fallback method.");
        RunShellCommand.ProcessResult run = new RunShellCommand().run(SERIAL_NUMBER_RETRIEVAL_COMMAND);
        String trim = run.getOutput().trim();
        if (run.getReturnCode() == 0 && !trim.isEmpty() && !trim.equals(EnvironmentCompat.MEDIA_UNKNOWN)) {
            return trim;
        }
        throw new AndroidSerialNumberNotFoundException();
    }

    public static class LinuxKernelVersion {
        String TAG;
        public final int major;
        public final int minor;
        public final int patch;

        private LinuxKernelVersion(String str) {
            this.TAG = "LinuxKernelVersion";
            RobotLog.m60vv("LinuxKernelVersion", "Raw Linux kernel version string: " + str);
            String[] split = str.split("\\.");
            this.major = parseIntFromStringArraySafely(split, 0);
            this.minor = parseIntFromStringArraySafely(split, 1);
            this.patch = parseIntFromStringArraySafely(split, 2);
            String str2 = this.TAG;
            RobotLog.m60vv(str2, "Processed Linux kernel version: " + toString());
        }

        public String toString() {
            return InspectionState.NO_VERSION + this.major + '.' + this.minor + '.' + this.patch;
        }

        private int parseIntFromStringArraySafely(String[] strArr, int i) {
            if (i >= strArr.length) {
                RobotLog.m54ii(this.TAG, "The Linux kernel version string does not have all 3 fields. Substituting a zero.");
                return 0;
            }
            try {
                return Integer.parseInt(strArr[i]);
            } catch (RuntimeException e) {
                RobotLog.m69ww(this.TAG, e, "Failed to parse int from String \"%s\". Substituting a zero.", strArr[i]);
                return 0;
            }
        }
    }
}
