package org.firstinspires.ftc.robotcore.internal.network;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import java.lang.reflect.Method;
import java.util.Random;
import java.util.concurrent.TimeoutException;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.system.CallbackRegistrar;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class WifiDirectDeviceNameManager extends WifiStartStoppable implements DeviceNameManager {
    public static final String TAG = "NetDiscover_name";
    protected static final String WIFI_P2P_DEVICE_NAME = "wifi_p2p_device_name";
    protected final Object callbackLock = new Object();
    protected CallbackRegistrar<DeviceNameListener> callbacks = new CallbackRegistrar<>();
    protected Context context;
    protected String defaultMadeUpDeviceName = null;
    protected StartResult deviceNameManagerStartResult = new StartResult();
    protected PreferencesHelper preferencesHelper;
    protected SharedPreferences sharedPreferences;
    protected SharedPreferencesListener sharedPreferencesListener = new SharedPreferencesListener();
    protected WifiAgentCallback wifiAgentCallback = new WifiAgentCallback();
    protected String wifiDirectName = null;

    protected enum DeviceNameTracking {
        UNINITIALIZED,
        AWAITING_WIFIDIRECT,
        WIFIDIRECT
    }

    public String getTag() {
        return TAG;
    }

    public WifiDirectDeviceNameManager() {
        super(WifiDirectAgent.getInstance());
        Application application = AppUtil.getInstance().getApplication();
        this.context = application;
        SharedPreferences defaultSharedPreferences = PreferenceManager.getDefaultSharedPreferences(application);
        this.sharedPreferences = defaultSharedPreferences;
        this.preferencesHelper = new PreferencesHelper(TAG, defaultSharedPreferences);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
    }

    /* access modifiers changed from: protected */
    public boolean doStart() {
        String str;
        String generateNameUniquifier = generateNameUniquifier();
        if (AppUtil.getInstance().isRobotController()) {
            str = this.context.getString(C0705R.string.device_name_format_rc, new Object[]{generateNameUniquifier});
        } else {
            str = this.context.getString(C0705R.string.device_name_format_ds, new Object[]{generateNameUniquifier});
        }
        this.defaultMadeUpDeviceName = str;
        return startWifiDirect();
    }

    /* access modifiers changed from: protected */
    public String generateNameUniquifier() {
        Random random = new Random();
        String str = InspectionState.NO_VERSION;
        for (int i = 0; i < 4; i++) {
            int nextInt = random.nextInt(26);
            str = str + ((char) (nextInt < 26 ? nextInt + 65 : (nextInt + 97) - 26));
        }
        return str;
    }

    /* access modifiers changed from: protected */
    public void doStop() {
        this.sharedPreferences.unregisterOnSharedPreferenceChangeListener(this.sharedPreferencesListener);
        stopWifiDirect();
    }

    public void registerCallback(DeviceNameListener deviceNameListener) {
        this.callbacks.registerCallback(deviceNameListener);
        deviceNameListener.onDeviceNameChanged(getDeviceName());
    }

    public void unregisterCallback(DeviceNameListener deviceNameListener) {
        this.callbacks.unregisterCallback(deviceNameListener);
    }

    public synchronized String getDeviceName() {
        initializeDeviceNameIfNecessary();
        return internalGetDeviceName();
    }

    public synchronized void setDeviceName(String str, boolean z) throws InvalidNetworkSettingException {
        if (validDeviceName(str)) {
            if (!z) {
                setDeviceNameTracking(DeviceNameTracking.AWAITING_WIFIDIRECT);
            }
            internalSetDeviceName(str);
        } else {
            throw new InvalidNetworkSettingException(String.format("Name \"%s\" does not conform to FIRST Tech Challenge naming rules", new Object[]{str}));
        }
    }

    public static boolean validDeviceName(String str) {
        return str.matches("[a-zA-Z0-9]+(-[a-zA-Z])?-(?i)(DS|RC)");
    }

    public String resetDeviceName(boolean z) {
        initializeDeviceNameFromMadeUp(!z);
        return getDeviceName();
    }

    public synchronized void initializeDeviceNameIfNecessary() {
        if (getDeviceNameTracking() == DeviceNameTracking.UNINITIALIZED) {
            initializeDeviceNameFromWifiDirect();
        }
        if (getDeviceNameTracking() == DeviceNameTracking.UNINITIALIZED) {
            initializeDeviceNameFromAndroidInternal();
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

    public boolean start(StartResult startResult) {
        return super.start(startResult);
    }

    public void stop(StartResult startResult) {
        super.stop(startResult);
    }

    /* access modifiers changed from: protected */
    public void initializeDeviceNameFromWifiDirect() {
        RobotLog.m60vv(TAG, "initializeDeviceNameFromWifiDirect()...");
        try {
            waitForWifiDirectName();
            RobotLog.m61vv(TAG, "initializeDeviceNameFromWifiDirect(): name=%s", this.wifiDirectName);
            setDeviceNameTracking(DeviceNameTracking.WIFIDIRECT);
            internalSetDeviceName(this.wifiDirectName);
        } catch (TimeoutException unused) {
        } catch (Throwable th) {
            RobotLog.m60vv(TAG, "...initializeDeviceNameFromWifiDirect()");
            throw th;
        }
        RobotLog.m60vv(TAG, "...initializeDeviceNameFromWifiDirect()");
    }

    /* access modifiers changed from: protected */
    public void initializeDeviceNameFromAndroidInternal() {
        RobotLog.m60vv(TAG, "initializeDeviceNameFromAndroidInternal()...");
        String string = Settings.Global.getString(this.context.getContentResolver(), WIFI_P2P_DEVICE_NAME);
        if (string != null) {
            RobotLog.m61vv(TAG, "initializeDeviceNameFromAndroidInternal(): name=%s", string);
            setDeviceNameTracking(DeviceNameTracking.WIFIDIRECT);
            this.wifiDirectName = string;
            internalSetDeviceName(string);
        }
        RobotLog.m60vv(TAG, "...initializeDeviceNameFromAndroidInternal()");
    }

    /* access modifiers changed from: protected */
    public void initializeDeviceNameFromMadeUp(boolean z) {
        RobotLog.m61vv(TAG, "initializeDeviceNameFromMadeUp(): name=%s onlyUseAsPlaceholder=%b ...", this.defaultMadeUpDeviceName, Boolean.valueOf(z));
        if (z) {
            setDeviceNameTracking(DeviceNameTracking.AWAITING_WIFIDIRECT);
        } else {
            setDeviceNameTracking(DeviceNameTracking.WIFIDIRECT);
        }
        internalSetDeviceName(this.defaultMadeUpDeviceName);
        RobotLog.m60vv(TAG, "..initializeDeviceNameFromMadeUp()");
    }

    /* access modifiers changed from: protected */
    public DeviceNameTracking getDeviceNameTracking() {
        return DeviceNameTracking.valueOf(this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name_tracking), DeviceNameTracking.UNINITIALIZED.toString()));
    }

    /* access modifiers changed from: protected */
    public void setDeviceNameTracking(DeviceNameTracking deviceNameTracking) {
        this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_device_name_tracking), deviceNameTracking.toString());
    }

    /* access modifiers changed from: protected */
    public String internalGetDeviceName() {
        return this.preferencesHelper.readString(this.context.getString(C0705R.string.pref_device_name), this.defaultMadeUpDeviceName);
    }

    /* access modifiers changed from: protected */
    public void internalSetDeviceName(String str) {
        this.preferencesHelper.writeStringPrefIfDifferent(this.context.getString(C0705R.string.pref_device_name), str);
    }

    protected class SharedPreferencesListener implements SharedPreferences.OnSharedPreferenceChangeListener {
        protected SharedPreferencesListener() {
        }

        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
            if (str.equals(WifiDirectDeviceNameManager.this.context.getString(C0705R.string.pref_device_name))) {
                final String internalGetDeviceName = WifiDirectDeviceNameManager.this.internalGetDeviceName();
                if (WifiDirectDeviceNameManager.this.preferencesHelper.writeStringPrefIfDifferent(WifiDirectDeviceNameManager.this.context.getString(C0705R.string.pref_device_name_old), internalGetDeviceName)) {
                    RobotLog.m61vv(WifiDirectDeviceNameManager.TAG, "deviceName pref changed: now=%s", internalGetDeviceName);
                    if (WifiDirectDeviceNameManager.this.getDeviceNameTracking() == DeviceNameTracking.WIFIDIRECT) {
                        WifiDirectDeviceNameManager.this.setWifiDirectDeviceName(internalGetDeviceName);
                    }
                    WifiDirectDeviceNameManager.this.callbacks.callbacksDo(new Consumer<DeviceNameListener>() {
                        public void accept(DeviceNameListener deviceNameListener) {
                            deviceNameListener.onDeviceNameChanged(internalGetDeviceName);
                        }
                    });
                }
            }
        }
    }

    protected class WifiAgentCallback implements WifiDirectAgent.Callback {
        protected WifiAgentCallback() {
        }

        public void onReceive(Context context, Intent intent) {
            if ("android.net.wifi.p2p.THIS_DEVICE_CHANGED".equals(intent.getAction())) {
                WifiDirectDeviceNameManager.this.internalRememberWifiDirectName(((WifiP2pDevice) intent.getParcelableExtra("wifiP2pDevice")).deviceName);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void internalRememberWifiDirectName(String str) {
        RobotLog.m61vv(TAG, "remembering wifiDirectName: %s...", str);
        synchronized (this.callbackLock) {
            Assert.assertNotNull(str);
            if (!str.equals(this.wifiDirectName)) {
                this.wifiDirectName = str;
                RobotLog.m61vv(TAG, "wifiDirectName=%s", str);
                DeviceNameTracking deviceNameTracking = getDeviceNameTracking();
                if (deviceNameTracking == DeviceNameTracking.WIFIDIRECT || deviceNameTracking == DeviceNameTracking.AWAITING_WIFIDIRECT) {
                    if (deviceNameTracking == DeviceNameTracking.AWAITING_WIFIDIRECT) {
                        setDeviceNameTracking(DeviceNameTracking.WIFIDIRECT);
                    }
                    internalSetDeviceName(str);
                }
                this.callbackLock.notifyAll();
            }
        }
        RobotLog.m60vv(TAG, "...remembering wifiDirectName");
    }

    /* access modifiers changed from: protected */
    public boolean startWifiDirect() {
        this.wifiDirectAgent.registerCallback(this.wifiAgentCallback);
        return this.wifiDirectAgent.start(this.wifiDirectAgentStarted);
    }

    /* access modifiers changed from: protected */
    public void stopWifiDirect() {
        this.wifiDirectAgent.stop(this.wifiDirectAgentStarted);
        this.wifiDirectAgent.unregisterCallback(this.wifiAgentCallback);
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x004f */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void waitForWifiDirectName() throws java.util.concurrent.TimeoutException {
        /*
            r8 = this;
            java.lang.String r0 = "NetDiscover_name"
            java.lang.String r1 = "waitForWifiDirectName() thread=%d..."
            r2 = 1
            java.lang.Object[] r2 = new java.lang.Object[r2]
            java.lang.Thread r3 = java.lang.Thread.currentThread()
            long r3 = r3.getId()
            java.lang.Long r3 = java.lang.Long.valueOf(r3)
            r4 = 0
            r2[r4] = r3
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r1, (java.lang.Object[]) r2)
            java.lang.Object r0 = r8.callbackLock     // Catch:{ InterruptedException -> 0x004f }
            monitor-enter(r0)     // Catch:{ InterruptedException -> 0x004f }
            r1 = 1000(0x3e8, float:1.401E-42)
            r2 = 100
            com.qualcomm.robotcore.util.ElapsedTime r3 = new com.qualcomm.robotcore.util.ElapsedTime     // Catch:{ all -> 0x004a }
            r3.<init>()     // Catch:{ all -> 0x004a }
        L_0x0025:
            java.lang.String r4 = r8.wifiDirectName     // Catch:{ all -> 0x004a }
            if (r4 == 0) goto L_0x002b
            monitor-exit(r0)     // Catch:{ all -> 0x004a }
            goto L_0x0056
        L_0x002b:
            double r4 = r3.milliseconds()     // Catch:{ all -> 0x004a }
            double r6 = (double) r1     // Catch:{ all -> 0x004a }
            int r4 = (r4 > r6 ? 1 : (r4 == r6 ? 0 : -1))
            if (r4 >= 0) goto L_0x003b
            java.lang.Object r4 = r8.callbackLock     // Catch:{ all -> 0x004a }
            long r5 = (long) r2     // Catch:{ all -> 0x004a }
            r4.wait(r5)     // Catch:{ all -> 0x004a }
            goto L_0x0025
        L_0x003b:
            java.lang.String r1 = "NetDiscover_name"
            java.lang.String r2 = "timeout in waitForWifiDirectName()"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r2)     // Catch:{ all -> 0x004a }
            java.util.concurrent.TimeoutException r1 = new java.util.concurrent.TimeoutException     // Catch:{ all -> 0x004a }
            java.lang.String r2 = "timeout in waitForWifiDirectName()"
            r1.<init>(r2)     // Catch:{ all -> 0x004a }
            throw r1     // Catch:{ all -> 0x004a }
        L_0x004a:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x004a }
            throw r1     // Catch:{ InterruptedException -> 0x004f }
        L_0x004d:
            r0 = move-exception
            goto L_0x005e
        L_0x004f:
            java.lang.Thread r0 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x004d }
            r0.interrupt()     // Catch:{ all -> 0x004d }
        L_0x0056:
            java.lang.String r0 = "NetDiscover_name"
            java.lang.String r1 = "...waitForWifiDirectName()"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)
            return
        L_0x005e:
            java.lang.String r1 = "NetDiscover_name"
            java.lang.String r2 = "...waitForWifiDirectName()"
            com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r2)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.WifiDirectDeviceNameManager.waitForWifiDirectName():void");
    }

    /* access modifiers changed from: protected */
    public void setWifiDirectDeviceName(final String str) {
        RobotLog.m61vv(TAG, "setWifiDirectDeviceName(%s)...", str);
        synchronized (this.callbackLock) {
            String str2 = this.wifiDirectName;
            if (str2 == null || !str2.equals(str)) {
                RobotLog.m61vv(TAG, "setWifiDirectDeviceName(%s): changing", str);
                Method declaredMethod = ClassUtil.getDeclaredMethod(this.wifiDirectAgent.getWifiP2pManager().getClass(), "setDeviceName", WifiP2pManager.Channel.class, String.class, WifiP2pManager.ActionListener.class);
                ClassUtil.invoke(this.wifiDirectAgent.getWifiP2pManager(), declaredMethod, this.wifiDirectAgent.getWifiP2pChannel(), str, new WifiP2pManager.ActionListener() {
                    public void onSuccess() {
                        RobotLog.m61vv(WifiDirectDeviceNameManager.TAG, "setWifiDirectDeviceName(%s): success", str);
                    }

                    public void onFailure(int i) {
                        RobotLog.m49ee(WifiDirectDeviceNameManager.TAG, "setWifiDirectDeviceName(%s): failed; reason=%d", str, Integer.valueOf(i));
                    }
                });
                internalRememberWifiDirectName(str);
            }
        }
        RobotLog.m61vv(TAG, "...setWifiDirectDeviceName(%s)", str);
    }
}
