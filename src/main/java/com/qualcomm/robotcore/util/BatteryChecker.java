package com.qualcomm.robotcore.util;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class BatteryChecker {
    protected static final int BATTERY_WARN_THRESHOLD = 30;
    private static final int LOG_THRESHOLD = 70;
    public static final String TAG = "BatteryChecker";
    protected static final boolean debugBattery = false;
    Runnable batteryLevelChecker = new Runnable() {
        public void run() {
            if (!BatteryChecker.this.closed) {
                BatteryChecker batteryChecker = BatteryChecker.this;
                batteryChecker.pollBatteryLevel(batteryChecker.watcher);
                BatteryChecker.this.scheduler.schedule(BatteryChecker.this.batteryLevelChecker, BatteryChecker.this.repeatDelayMs, TimeUnit.MILLISECONDS);
            }
        }
    };
    protected volatile boolean closed;
    private final Context context;
    private final long initialDelayMs = 5000;
    protected final Monitor monitor = new Monitor();
    /* access modifiers changed from: private */
    public final long repeatDelayMs;
    protected final ScheduledExecutorService scheduler = ThreadPool.getDefaultScheduler();
    /* access modifiers changed from: private */
    public final BatteryWatcher watcher;

    public interface BatteryWatcher {
        void updateBatteryStatus(BatteryStatus batteryStatus);
    }

    public static class BatteryStatus {
        public boolean isCharging;
        public double percent;

        public BatteryStatus(double d, boolean z) {
            this.percent = d;
            this.isCharging = z;
        }

        protected BatteryStatus() {
        }

        public String serialize() {
            StringBuilder sb = new StringBuilder();
            sb.append(this.percent);
            sb.append('|');
            sb.append(this.isCharging);
            return sb.toString();
        }

        public static BatteryStatus deserialize(String str) {
            String[] split = str.split("\\|");
            BatteryStatus batteryStatus = new BatteryStatus();
            batteryStatus.percent = Double.parseDouble(split[0]);
            batteryStatus.isCharging = Boolean.parseBoolean(split[1]);
            return batteryStatus;
        }
    }

    public BatteryChecker(BatteryWatcher batteryWatcher, long j) {
        this.watcher = batteryWatcher;
        this.repeatDelayMs = j;
        this.closed = true;
        this.context = AppUtil.getDefContext();
    }

    public synchronized void startBatteryMonitoring() {
        if (this.closed) {
            registerReceiver(this.monitor);
        }
        this.closed = false;
        this.scheduler.schedule(this.batteryLevelChecker, 5000, TimeUnit.MILLISECONDS);
    }

    public synchronized void close() {
        if (!this.closed) {
            try {
                this.context.unregisterReceiver(this.monitor);
            } catch (Exception e) {
                RobotLog.m50ee(TAG, (Throwable) e, "Failed to unregister battery monitor receiver; ignored");
            }
        }
        this.closed = true;
    }

    protected class Monitor extends BroadcastReceiver {
        protected Monitor() {
        }

        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();
            action.hashCode();
            if (action.equals("android.intent.action.BATTERY_CHANGED")) {
                BatteryChecker.this.processBatteryChanged(intent);
            }
        }
    }

    public void pollBatteryLevel(BatteryWatcher batteryWatcher) {
        processBatteryChanged(registerReceiver((BroadcastReceiver) null));
    }

    /* access modifiers changed from: protected */
    public Intent registerReceiver(BroadcastReceiver broadcastReceiver) {
        return this.context.registerReceiver(broadcastReceiver, new IntentFilter("android.intent.action.BATTERY_CHANGED"));
    }

    /* access modifiers changed from: protected */
    public void processBatteryChanged(Intent intent) {
        if (intent != null) {
            int intExtra = intent.getIntExtra("level", -1);
            int intExtra2 = intent.getIntExtra("scale", -1);
            if (intExtra >= 0 && intExtra2 > 0) {
                boolean z = true;
                if ((intent.getIntExtra("plugged", 1) & 7) == 0) {
                    z = false;
                }
                int i = (intExtra * 100) / intExtra2;
                logBatteryInfo(i, z);
                this.watcher.updateBatteryStatus(new BatteryStatus((double) i, z));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void logBatteryInfo(int i, boolean z) {
        if (i < 30) {
            RobotLog.m54ii(TAG, "percent remaining: " + i + " is charging: " + z);
        }
    }
}
