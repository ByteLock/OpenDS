package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class ControlHubPasswordManager implements PasswordManager {
    private static final String FACTORY_DEFAULT_PASSWORD = "password";
    private static final String TAG = "ControlHubPasswordManager";
    private Context context;
    private String password;
    private PreferencesHelper preferencesHelper;
    private SharedPreferences sharedPreferences;

    public ControlHubPasswordManager() {
        Application application = AppUtil.getInstance().getApplication();
        this.context = application;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        this.sharedPreferences = defaultSharedPreferences;
        this.preferencesHelper = new PreferencesHelper(TAG, defaultSharedPreferences);
    }

    private StringBuffer stringify(byte[] bArr) {
        StringBuffer stringBuffer = new StringBuffer();
        for (byte b : bArr) {
            String hexString = Integer.toHexString(b & 255);
            if (hexString.length() == 1) {
                hexString = '0' + hexString;
            }
            stringBuffer.append(hexString);
        }
        return stringBuffer;
    }

    private String toSha256(String str) {
        try {
            return stringify(MessageDigest.getInstance("SHA-256").digest(str.getBytes(StandardCharsets.UTF_8))).toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return null;
        }
    }

    public void validatePassword(String str) throws InvalidNetworkSettingException {
        if (str.length() < 8 || str.length() > 63) {
            throw new InvalidNetworkSettingException("Invalid password length of " + str.length() + " chars.");
        }
    }

    public synchronized void setPassword(String str, boolean z) throws InvalidNetworkSettingException {
        validatePassword(str);
        internalSetDevicePassword(str);
        if (z) {
            RobotLog.m60vv(TAG, "Sending password change intent");
            Intent intent = new Intent(Intents.ACTION_FTC_AP_PASSWORD_CHANGE);
            intent.putExtra(Intents.EXTRA_AP_PREF, str);
            this.context.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: protected */
    public void internalSetDevicePassword(String str) {
        RobotLog.m54ii(TAG, "Robot controller password: " + str);
        this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_connection_owner_password), str);
    }

    /* access modifiers changed from: protected */
    public void initializePasswordIfNecessary() {
        String readString = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_connection_owner_password), InspectionState.NO_VERSION);
        this.password = readString;
        if (readString.isEmpty()) {
            this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_connection_owner_password), "password");
        }
        String readString2 = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_connection_owner_password), InspectionState.NO_VERSION);
        this.password = readString2;
        if (readString2.isEmpty()) {
            throw new IllegalStateException("Password not set");
        }
    }

    public synchronized String resetPassword(boolean z) {
        try {
            setPassword("password", z);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Unable to reset password to " + "password");
        }
        return "password";
    }

    public boolean isDefault() {
        return getPassword().equals("password");
    }

    public synchronized String getPassword() {
        String readString;
        initializePasswordIfNecessary();
        readString = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_connection_owner_password), "password");
        RobotLog.m52i(readString);
        return readString;
    }
}
