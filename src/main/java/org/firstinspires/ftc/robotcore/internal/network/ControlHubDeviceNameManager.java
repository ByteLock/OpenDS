package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.AndroidSerialNumberNotFoundException;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ShortHash;
import java.util.zip.CRC32;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.system.CallbackRegistrar;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class ControlHubDeviceNameManager implements DeviceNameManager {
    private static final int MAX_SSID_CHARS = 4;
    private static final String MISSING_SERIAL_SSID = "FTC-MISSING-SERIAL";
    private static final String TAG = "NetDiscover_ControlHubNameManager";
    private static final ControlHubDeviceNameManager theInstance = new ControlHubDeviceNameManager();
    private CallbackRegistrar<DeviceNameListener> callbacks = new CallbackRegistrar<>();
    /* access modifiers changed from: private */
    public Context context;
    /* access modifiers changed from: private */
    public String defaultMadeUpDeviceName;
    private String deviceName;
    private PreferencesHelper preferencesHelper;
    private SharedPreferences sharedPreferences;
    private SharedPreferencesListener sharedPreferencesListener = new SharedPreferencesListener();

    protected enum DeviceNameTracking {
        UNINITIALIZED,
        WIFIAP
    }

    public boolean start(StartResult startResult) {
        return true;
    }

    public static ControlHubDeviceNameManager getControlHubDeviceNameManager() {
        RobotLog.m53i(TAG, "Getting name manager");
        return theInstance;
    }

    public ControlHubDeviceNameManager() {
        Application defContext = AppUtil.getDefContext();
        this.context = defContext;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(defContext);
        this.sharedPreferences = defaultSharedPreferences;
        this.preferencesHelper = new PreferencesHelper(TAG, defaultSharedPreferences);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
    }

    public synchronized String getDeviceName() {
        initializeDeviceNameIfNecessary();
        return internalGetDeviceName();
    }

    public synchronized void setDeviceName(String str, boolean z) {
        internalSetDeviceName(str, Boolean.valueOf(z));
    }

    public synchronized String resetDeviceName(boolean z) {
        initializeDeviceNameFromMadeUp(z);
        return getDeviceName();
    }

    public synchronized void initializeDeviceNameIfNecessary() {
        if (getDeviceNameTracking() == DeviceNameTracking.UNINITIALIZED) {
            initializeDeviceNameFromSharedPrefs();
        }
        boolean z = true;
        if (getDeviceNameTracking() == DeviceNameTracking.UNINITIALIZED) {
            initializeDeviceNameFromMadeUp(true);
        }
        if (getDeviceNameTracking() == DeviceNameTracking.UNINITIALIZED) {
            z = false;
        }
        Assert.assertTrue(z);
    }

    /* access modifiers changed from: protected */
    public void initializeDeviceNameFromSharedPrefs() {
        String internalGetDeviceName = internalGetDeviceName();
        this.deviceName = internalGetDeviceName;
        if (internalGetDeviceName != null) {
            setDeviceNameTracking(DeviceNameTracking.WIFIAP);
            internalSetDeviceName(this.deviceName, true);
        }
    }

    /* access modifiers changed from: protected */
    public String handleFactoryReset() {
        RobotLog.m42dd(TAG, "handleFactoryReset");
        try {
            String serialNumber = Device.getSerialNumber();
            RobotLog.m43dd(TAG, "Serial: %s", serialNumber);
            CRC32 crc32 = new CRC32();
            ShortHash shortHash = new ShortHash("FiRsTiNsPiReS");
            crc32.update(serialNumber.getBytes());
            String encode = shortHash.encode((long) ((int) (crc32.getValue() % ((long) (((int) Math.pow((double) shortHash.getAlphabetLength(), 4.0d)) - 1)))));
            return "FTC-" + encode;
        } catch (AndroidSerialNumberNotFoundException unused) {
            RobotLog.m48ee(TAG, "Failed to find Android serial number. Setting SSID to FTC-MISSING-SERIAL");
            return MISSING_SERIAL_SSID;
        }
    }

    /* access modifiers changed from: protected */
    public void initializeDeviceNameFromMadeUp(boolean z) {
        RobotLog.m61vv(TAG, "initializeDeviceNameFromMadeUp(): name=%s ...", this.defaultMadeUpDeviceName);
        this.defaultMadeUpDeviceName = handleFactoryReset();
        setDeviceNameTracking(DeviceNameTracking.WIFIAP);
        internalSetDeviceName(this.defaultMadeUpDeviceName, Boolean.valueOf(z));
        RobotLog.m60vv(TAG, "..initializeDeviceNameFromMadeUp()");
    }

    /* access modifiers changed from: protected */
    public synchronized void internalSetDeviceName(final String str, Boolean bool) {
        RobotLog.m54ii(TAG, "Robot controller name: " + str);
        boolean z = false;
        if (bool != null) {
            z = bool.booleanValue();
        }
        if (this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_device_name_internal), str)) {
            if (bool == null) {
                z = true;
            }
            this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_device_name), str);
            this.deviceName = str;
            this.callbacks.callbacksDo(new Consumer<DeviceNameListener>() {
                public void accept(DeviceNameListener deviceNameListener) {
                    deviceNameListener.onDeviceNameChanged(str);
                }
            });
        }
        if (z) {
            Intent intent = new Intent(Intents.ACTION_FTC_AP_NAME_CHANGE);
            intent.putExtra(Intents.EXTRA_AP_PREF, str);
            this.context.sendBroadcast(intent);
        }
    }

    /* access modifiers changed from: protected */
    public void internalSetAccessPointPassword(String str) {
        Intent intent = new Intent(Intents.ACTION_FTC_AP_PASSWORD_CHANGE);
        intent.putExtra(Intents.EXTRA_AP_PREF, str);
        this.context.sendBroadcast(intent);
    }

    /* access modifiers changed from: protected */
    public DeviceNameTracking getDeviceNameTracking() {
        String readString = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name), InspectionState.NO_VERSION);
        String readString2 = this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name_internal), InspectionState.NO_VERSION);
        if (readString.isEmpty() || readString2.isEmpty()) {
            return DeviceNameTracking.UNINITIALIZED;
        }
        try {
            return DeviceNameTracking.valueOf(this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name_tracking), DeviceNameTracking.UNINITIALIZED.toString()));
        } catch (Exception unused) {
            return DeviceNameTracking.UNINITIALIZED;
        }
    }

    /* access modifiers changed from: protected */
    public void setDeviceNameTracking(DeviceNameTracking deviceNameTracking) {
        this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_device_name_tracking), deviceNameTracking.toString());
    }

    /* access modifiers changed from: protected */
    public String internalGetDeviceName() {
        return this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name_internal), this.defaultMadeUpDeviceName);
    }

    public void stop(StartResult startResult) {
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
    }

    public void registerCallback(DeviceNameListener deviceNameListener) {
        this.callbacks.registerCallback(deviceNameListener);
        deviceNameListener.onDeviceNameChanged(getDeviceName());
    }

    public void unregisterCallback(DeviceNameListener deviceNameListener) {
        this.callbacks.unregisterCallback(deviceNameListener);
    }

    protected class SharedPreferencesListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        protected SharedPreferencesListener() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            if (str.equals(ControlHubDeviceNameManager.this.context.getString(C0705R.string.pref_device_name))) {
                synchronized (ControlHubDeviceNameManager.this) {
                    ControlHubDeviceNameManager.this.internalSetDeviceName(sharedPreferences.getString(str, ControlHubDeviceNameManager.this.defaultMadeUpDeviceName), (Boolean) null);
                }
            }
        }
    }
}
