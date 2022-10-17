package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.HashSet;
import java.util.Iterator;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class I2cWarningManager implements GlobalWarningSource {
    private static final I2cWarningManager instance;
    private static final Object lock = new Object();
    private static int newProblemDeviceSuppressionCount = 0;
    private final HashSet<I2cDeviceSynchSimple> problemDevices = new HashSet<>();
    private int warningSourceSuppressionCount = 0;

    public void setGlobalWarning(String str) {
    }

    public boolean shouldTriggerWarningSound() {
        return true;
    }

    static {
        I2cWarningManager i2cWarningManager = new I2cWarningManager();
        instance = i2cWarningManager;
        RobotLog.registerGlobalWarningSource(i2cWarningManager);
    }

    public static void notifyProblemI2cDevice(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        synchronized (lock) {
            if (newProblemDeviceSuppressionCount == 0) {
                instance.problemDevices.add(i2cDeviceSynchSimple);
            }
        }
    }

    public static void removeProblemI2cDevice(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        synchronized (lock) {
            I2cWarningManager i2cWarningManager = instance;
            if (!i2cWarningManager.problemDevices.isEmpty()) {
                i2cWarningManager.problemDevices.remove(i2cDeviceSynchSimple);
            }
        }
    }

    public static void suppressNewProblemDeviceWarningsWhile(Runnable runnable) {
        Object obj = lock;
        synchronized (obj) {
            newProblemDeviceSuppressionCount++;
        }
        try {
            runnable.run();
            synchronized (obj) {
                newProblemDeviceSuppressionCount--;
            }
        } catch (Throwable th) {
            synchronized (lock) {
                newProblemDeviceSuppressionCount--;
                throw th;
            }
        }
    }

    public static void suppressNewProblemDeviceWarnings(boolean z) {
        synchronized (lock) {
            if (z) {
                newProblemDeviceSuppressionCount++;
            } else {
                newProblemDeviceSuppressionCount--;
            }
        }
    }

    public static void clearI2cWarnings() {
        instance.clearGlobalWarning();
    }

    public String getGlobalWarning() {
        synchronized (lock) {
            if (!this.problemDevices.isEmpty()) {
                if (this.warningSourceSuppressionCount <= 0) {
                    StringBuilder sb = new StringBuilder();
                    sb.append(AppUtil.getDefContext().getString(C0705R.string.warningI2cCommError));
                    sb.append(" ");
                    Iterator<I2cDeviceSynchSimple> it = this.problemDevices.iterator();
                    while (it.hasNext()) {
                        String userConfiguredName = ((RobotConfigNameable) it.next()).getUserConfiguredName();
                        if (userConfiguredName != null) {
                            sb.append("'");
                            sb.append(userConfiguredName);
                            sb.append("'");
                        }
                        if (it.hasNext()) {
                            sb.append(", ");
                        } else {
                            sb.append(". ");
                        }
                    }
                    sb.append("Check your wiring and configuration. ");
                    String sb2 = sb.toString();
                    return sb2;
                }
            }
            return null;
        }
    }

    public void suppressGlobalWarning(boolean z) {
        synchronized (lock) {
            if (z) {
                this.warningSourceSuppressionCount++;
            } else {
                this.warningSourceSuppressionCount--;
            }
        }
    }

    public void clearGlobalWarning() {
        synchronized (lock) {
            this.problemDevices.clear();
        }
    }
}
