package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

public class DefaultGamepadManager {
    private static final String KEY_POSITION_1_DEFAULT = "GAMEPAD_POSITION_1_DEFAULT";
    private static final String KEY_POSITION_2_DEFAULT = "GAMEPAD_POSITION_2_DEFAULT";
    private static DefaultGamepadManager theInstance;
    private SharedPreferences sharedPreferences;

    public static synchronized DefaultGamepadManager getInstance(Context context) {
        DefaultGamepadManager defaultGamepadManager;
        synchronized (DefaultGamepadManager.class) {
            if (theInstance == null) {
                theInstance = new DefaultGamepadManager(context);
            }
            defaultGamepadManager = theInstance;
        }
        return defaultGamepadManager;
    }

    private DefaultGamepadManager(Context context) {
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
    }

    public synchronized void setPosition1Default(int i, int i2, String str) {
        this.sharedPreferences.edit().putString(KEY_POSITION_1_DEFAULT, getIdString(i, i2, str)).apply();
    }

    public synchronized void setPosition2Default(int i, int i2, String str) {
        this.sharedPreferences.edit().putString(KEY_POSITION_2_DEFAULT, getIdString(i, i2, str)).apply();
    }

    public synchronized void clearPosition1Default() {
        this.sharedPreferences.edit().remove(KEY_POSITION_1_DEFAULT).apply();
    }

    public synchronized void clearPosition2Default() {
        this.sharedPreferences.edit().remove(KEY_POSITION_2_DEFAULT).apply();
    }

    public synchronized String getPosition1Default() {
        return this.sharedPreferences.getString(KEY_POSITION_1_DEFAULT, (String) null);
    }

    public synchronized String getPosition2Default() {
        return this.sharedPreferences.getString(KEY_POSITION_2_DEFAULT, (String) null);
    }

    public static String getIdString(int i, int i2, String str) {
        return String.format("0x%X-0x%X-%s", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), str});
    }
}
