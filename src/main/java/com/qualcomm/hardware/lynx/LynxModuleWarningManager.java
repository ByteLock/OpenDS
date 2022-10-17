package com.qualcomm.hardware.lynx;

import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.robotcore.eventloop.opmode.OpMode;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerNotifier;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class LynxModuleWarningManager {
    private static final int LOW_BATTERY_LOG_FREQUENCY_SECONDS = 2;
    private static final int LOW_BATTERY_STATUS_TIMEOUT_SECONDS = 2;
    private static final int MIN_FW_VERSION_ENG = 2;
    private static final String MIN_FW_VERSION_HUMAN_STRING = "1.8.2";
    private static final int MIN_FW_VERSION_MAJOR = 1;
    private static final int MIN_FW_VERSION_MINOR = 8;
    private static final String TAG = "LynxModuleWarningManager";
    private static final int UNRESPONSIVE_LOG_FREQUENCY_SECONDS = 2;
    private static final LynxModuleWarningManager instance = new LynxModuleWarningManager();
    /* access modifiers changed from: private */
    public String cachedWarningMessage = null;
    private volatile HardwareMap hardwareMap = null;
    /* access modifiers changed from: private */
    public final ConcurrentMap<Integer, LowBatteryStatus> modulesReportedLowBattery = new ConcurrentHashMap();
    /* access modifiers changed from: private */
    public final Set<String> modulesReportedReset = Collections.newSetFromMap(new ConcurrentHashMap());
    /* access modifiers changed from: private */
    public final ConcurrentMap<Integer, UnresponsiveStatus> modulesReportedUnresponsive = new ConcurrentHashMap();
    private final OpModeManagerNotifier.Notifications opModeNotificationListener = new WarningManagerOpModeListener();
    /* access modifiers changed from: private */
    public final Set<String> outdatedModules = Collections.newSetFromMap(new ConcurrentHashMap());
    private final SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(AppUtil.getDefContext());
    /* access modifiers changed from: private */
    public volatile boolean userOpModeRunning = false;
    /* access modifiers changed from: private */
    public final Object warningMessageLock = new Object();
    private final GlobalWarningSource warningSource = new LynxModuleWarningSource();

    private boolean isFwVersionOutdated(int i, int i2, int i3) {
        if (i > 1) {
            return false;
        }
        if (i < 1) {
            return true;
        }
        if (i2 > 8) {
            return false;
        }
        if (i2 < 8) {
            return true;
        }
        return i3 < 2;
    }

    public static LynxModuleWarningManager getInstance() {
        return instance;
    }

    public void init(OpModeManagerImpl opModeManagerImpl, HardwareMap hardwareMap2) {
        this.hardwareMap = hardwareMap2;
        opModeManagerImpl.registerListener(this.opModeNotificationListener);
        this.warningSource.clearGlobalWarning();
        RobotLog.registerGlobalWarningSource(this.warningSource);
        if (this.sharedPrefs.getBoolean(AppUtil.getDefContext().getString(C0660R.string.pref_warn_about_obsolete_software), true)) {
            lookForOutdatedModules();
        } else {
            this.outdatedModules.clear();
        }
    }

    public void reportModuleUnresponsive(LynxModule lynxModule) {
        if (lynxModule.isUserModule() && lynxModule.isOpen) {
            int moduleAddress = lynxModule.getModuleAddress();
            UnresponsiveStatus unresponsiveStatus = (UnresponsiveStatus) this.modulesReportedUnresponsive.get(Integer.valueOf(moduleAddress));
            if (unresponsiveStatus == null) {
                UnresponsiveStatus unresponsiveStatus2 = new UnresponsiveStatus(lynxModule, getModuleName(lynxModule));
                this.modulesReportedUnresponsive.put(Integer.valueOf(moduleAddress), unresponsiveStatus2);
                unresponsiveStatus = unresponsiveStatus2;
            }
            unresponsiveStatus.reportConditionAndLogWithThrottle(this.userOpModeRunning);
        }
    }

    public void reportModuleReset(LynxModule lynxModule) {
        String str;
        if (lynxModule.isUserModule()) {
            String moduleName = getModuleName(lynxModule);
            if (this.userOpModeRunning) {
                str = "%s regained power after a complete power loss." + " A user Op Mode was running, so unexpected behavior may occur.";
                if (this.modulesReportedReset.add(moduleName)) {
                    synchronized (this.warningMessageLock) {
                        this.cachedWarningMessage = null;
                    }
                }
            } else {
                str = "%s regained power after a complete power loss." + " No user Op Mode was running.";
            }
            RobotLog.m67ww("HubPowerCycle", str, moduleName);
        }
    }

    public void reportModuleLowBattery(LynxModule lynxModule) {
        if (lynxModule.isUserModule()) {
            int moduleAddress = lynxModule.getModuleAddress();
            LowBatteryStatus lowBatteryStatus = (LowBatteryStatus) this.modulesReportedLowBattery.get(Integer.valueOf(moduleAddress));
            if (lowBatteryStatus == null) {
                LowBatteryStatus lowBatteryStatus2 = new LowBatteryStatus(lynxModule, getModuleName(lynxModule));
                this.modulesReportedLowBattery.put(Integer.valueOf(moduleAddress), lowBatteryStatus2);
                lowBatteryStatus = lowBatteryStatus2;
            }
            lowBatteryStatus.reportConditionAndLogWithThrottle(this.userOpModeRunning);
        }
    }

    private String getModuleName(LynxModule lynxModule) {
        try {
            return this.hardwareMap.getNamesOf(lynxModule).iterator().next();
        } catch (RuntimeException unused) {
            return "Expansion Hub " + lynxModule.getModuleAddress();
        }
    }

    private void lookForOutdatedModules() {
        this.outdatedModules.clear();
        for (LynxModule next : this.hardwareMap.getAll(LynxModule.class)) {
            try {
                String nullableFirmwareVersionString = next.getNullableFirmwareVersionString();
                if (nullableFirmwareVersionString != null) {
                    String[] strArr = (String[]) Arrays.copyOfRange(nullableFirmwareVersionString.split("(, )?\\w*: "), 2, 5);
                    if (isFwVersionOutdated(Integer.parseInt(strArr[0]), Integer.parseInt(strArr[1]), Integer.parseInt(strArr[2]))) {
                        this.outdatedModules.add(getModuleName(next));
                    }
                }
            } catch (RuntimeException e) {
                RobotLog.m50ee(TAG, (Throwable) e, "Exception caught while checking if module is outdated");
            }
        }
    }

    private static abstract class ConditionStatus {
        boolean conditionPreviouslyTrue = false;
        boolean conditionTrueDuringOpModeRun = false;
        final int logFrequencySeconds;
        final LynxModuleIntf lynxModule;
        final String moduleName;
        final ElapsedTime timeSinceConditionLastReported = new ElapsedTime();
        final ElapsedTime timeSinceConditionLogged = new ElapsedTime(0);

        /* access modifiers changed from: package-private */
        public abstract boolean conditionCurrentlyTrue();

        /* access modifiers changed from: package-private */
        public abstract void logCondition();

        ConditionStatus(LynxModuleIntf lynxModuleIntf, String str, int i) {
            this.lynxModule = lynxModuleIntf;
            this.logFrequencySeconds = i;
            this.moduleName = str;
        }

        /* access modifiers changed from: package-private */
        public void reportConditionAndLogWithThrottle(boolean z) {
            if (z) {
                this.conditionTrueDuringOpModeRun = true;
            }
            this.timeSinceConditionLastReported.reset();
            if (this.timeSinceConditionLogged.seconds() > ((double) this.logFrequencySeconds)) {
                logCondition();
                this.timeSinceConditionLogged.reset();
            }
        }

        /* access modifiers changed from: package-private */
        public final boolean hasChangedSinceLastCheck() {
            boolean conditionCurrentlyTrue = conditionCurrentlyTrue();
            boolean z = conditionCurrentlyTrue != this.conditionPreviouslyTrue;
            this.conditionPreviouslyTrue = conditionCurrentlyTrue;
            return z;
        }
    }

    private static class UnresponsiveStatus extends ConditionStatus {
        private UnresponsiveStatus(LynxModuleIntf lynxModuleIntf, String str) {
            super(lynxModuleIntf, str, 2);
        }

        /* access modifiers changed from: package-private */
        public boolean conditionCurrentlyTrue() {
            return this.lynxModule.isNotResponding();
        }

        /* access modifiers changed from: package-private */
        public void logCondition() {
            RobotLog.m65w("%s is currently unresponsive.", this.moduleName);
        }
    }

    private static class LowBatteryStatus extends ConditionStatus {
        private LowBatteryStatus(LynxModule lynxModule, String str) {
            super(lynxModule, str, 2);
        }

        /* access modifiers changed from: package-private */
        public boolean conditionCurrentlyTrue() {
            return this.timeSinceConditionLastReported.seconds() < 2.0d;
        }

        /* access modifiers changed from: package-private */
        public void logCondition() {
            RobotLog.m65w("%s currently has a battery too low to run motors and servos.", this.moduleName);
        }
    }

    private class LynxModuleWarningSource implements GlobalWarningSource {
        private int warningMessageSuppressionCount;

        public void setGlobalWarning(String str) {
        }

        public boolean shouldTriggerWarningSound() {
            return true;
        }

        private LynxModuleWarningSource() {
            this.warningMessageSuppressionCount = 0;
        }

        /* JADX WARNING: Removed duplicated region for block: B:28:0x0064  */
        /* Code decompiled incorrectly, please refer to instructions dump. */
        public java.lang.String getGlobalWarning() {
            /*
                r5 = this;
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r0 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this
                java.lang.Object r0 = r0.warningMessageLock
                monitor-enter(r0)
                int r1 = r5.warningMessageSuppressionCount     // Catch:{ all -> 0x0075 }
                if (r1 <= 0) goto L_0x000f
                java.lang.String r1 = ""
                monitor-exit(r0)     // Catch:{ all -> 0x0075 }
                return r1
            L_0x000f:
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r1 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this     // Catch:{ all -> 0x0075 }
                java.lang.String r1 = r1.cachedWarningMessage     // Catch:{ all -> 0x0075 }
                r2 = 0
                if (r1 == 0) goto L_0x001a
                r1 = 1
                goto L_0x001b
            L_0x001a:
                r1 = r2
            L_0x001b:
                if (r1 == 0) goto L_0x0061
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r3 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this     // Catch:{ all -> 0x0075 }
                java.util.concurrent.ConcurrentMap r3 = r3.modulesReportedLowBattery     // Catch:{ all -> 0x0075 }
                java.util.Collection r3 = r3.values()     // Catch:{ all -> 0x0075 }
                java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x0075 }
            L_0x002b:
                boolean r4 = r3.hasNext()     // Catch:{ all -> 0x0075 }
                if (r4 == 0) goto L_0x003e
                java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x0075 }
                com.qualcomm.hardware.lynx.LynxModuleWarningManager$LowBatteryStatus r4 = (com.qualcomm.hardware.lynx.LynxModuleWarningManager.LowBatteryStatus) r4     // Catch:{ all -> 0x0075 }
                boolean r4 = r4.hasChangedSinceLastCheck()     // Catch:{ all -> 0x0075 }
                if (r4 == 0) goto L_0x002b
                r1 = r2
            L_0x003e:
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r3 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this     // Catch:{ all -> 0x0075 }
                java.util.concurrent.ConcurrentMap r3 = r3.modulesReportedUnresponsive     // Catch:{ all -> 0x0075 }
                java.util.Collection r3 = r3.values()     // Catch:{ all -> 0x0075 }
                java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x0075 }
            L_0x004c:
                boolean r4 = r3.hasNext()     // Catch:{ all -> 0x0075 }
                if (r4 == 0) goto L_0x0061
                java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x0075 }
                com.qualcomm.hardware.lynx.LynxModuleWarningManager$UnresponsiveStatus r4 = (com.qualcomm.hardware.lynx.LynxModuleWarningManager.UnresponsiveStatus) r4     // Catch:{ all -> 0x0075 }
                boolean r4 = r4.hasChangedSinceLastCheck()     // Catch:{ all -> 0x0075 }
                if (r4 != 0) goto L_0x0062
                if (r1 != 0) goto L_0x004c
                goto L_0x0062
            L_0x0061:
                r2 = r1
            L_0x0062:
                if (r2 != 0) goto L_0x006d
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r1 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this     // Catch:{ all -> 0x0075 }
                java.lang.String r2 = r5.composeWarning()     // Catch:{ all -> 0x0075 }
                java.lang.String unused = r1.cachedWarningMessage = r2     // Catch:{ all -> 0x0075 }
            L_0x006d:
                com.qualcomm.hardware.lynx.LynxModuleWarningManager r1 = com.qualcomm.hardware.lynx.LynxModuleWarningManager.this     // Catch:{ all -> 0x0075 }
                java.lang.String r1 = r1.cachedWarningMessage     // Catch:{ all -> 0x0075 }
                monitor-exit(r0)     // Catch:{ all -> 0x0075 }
                return r1
            L_0x0075:
                r1 = move-exception
                monitor-exit(r0)     // Catch:{ all -> 0x0075 }
                throw r1
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxModuleWarningManager.LynxModuleWarningSource.getGlobalWarning():java.lang.String");
        }

        private String composeWarning() {
            String str;
            String str2 = null;
            if (LynxModuleWarningManager.this.modulesReportedUnresponsive.size() > 0) {
                ArrayList arrayList = new ArrayList();
                ArrayList arrayList2 = new ArrayList();
                for (UnresponsiveStatus unresponsiveStatus : LynxModuleWarningManager.this.modulesReportedUnresponsive.values()) {
                    if (unresponsiveStatus.lynxModule.isNotResponding()) {
                        arrayList.add(unresponsiveStatus.moduleName);
                    } else if (unresponsiveStatus.conditionTrueDuringOpModeRun && !LynxModuleWarningManager.this.modulesReportedReset.contains(unresponsiveStatus.moduleName)) {
                        arrayList2.add(unresponsiveStatus.moduleName);
                    }
                }
                String composeCurrentlyUnresponsiveWarning = composeCurrentlyUnresponsiveWarning(arrayList);
                String composePreviouslyNotRespondingWarning = composePreviouslyNotRespondingWarning(arrayList2);
                str2 = composeCurrentlyUnresponsiveWarning;
                str = composePreviouslyNotRespondingWarning;
            } else {
                str = null;
            }
            return RobotLog.combineGlobalWarnings(Arrays.asList(new String[]{str2, str, composePowerIssuesWarning(), composeOutdatedHubsWarning()}));
        }

        private String composeCurrentlyUnresponsiveWarning(List<String> list) {
            if (list.size() <= 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            composeModuleList(list, sb);
            sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModuleCurrentlyNotResponding));
            return sb.toString();
        }

        private String composePreviouslyNotRespondingWarning(List<String> list) {
            if (list.size() <= 0) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            composeModuleList(list, sb);
            sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModulePreviouslyNotResponding));
            return sb.toString();
        }

        private String composePowerIssuesWarning() {
            if (LynxModuleWarningManager.this.modulesReportedReset.size() < 1 && LynxModuleWarningManager.this.modulesReportedLowBattery.size() < 1) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            boolean composePowerLossWarning = composePowerLossWarning(sb);
            if (composePowerLossWarning) {
                sb.append(" ");
            }
            boolean composeBatteryLowWarning = composeBatteryLowWarning(sb);
            if (composeBatteryLowWarning) {
                sb.append(" ");
            }
            if (!composePowerLossWarning && !composeBatteryLowWarning) {
                return null;
            }
            composePowerIssueTip(LynxModuleWarningManager.this.userOpModeRunning, sb);
            return sb.toString();
        }

        private String composeOutdatedHubsWarning() {
            if (LynxModuleWarningManager.this.outdatedModules.size() < 1) {
                return null;
            }
            StringBuilder sb = new StringBuilder();
            composeModuleList(LynxModuleWarningManager.this.outdatedModules, sb);
            sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModuleFirmwareOutdated, new Object[]{"1.8.2"}));
            return sb.toString();
        }

        private boolean composePowerLossWarning(StringBuilder sb) {
            if (LynxModuleWarningManager.this.modulesReportedReset.size() < 1) {
                return false;
            }
            composeModuleList(LynxModuleWarningManager.this.modulesReportedReset, sb);
            sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModulePowerLost));
            return true;
        }

        private boolean composeBatteryLowWarning(StringBuilder sb) {
            boolean z = false;
            if (LynxModuleWarningManager.this.modulesReportedLowBattery.size() < 1) {
                return false;
            }
            ArrayList arrayList = new ArrayList();
            ArrayList arrayList2 = new ArrayList();
            for (LowBatteryStatus lowBatteryStatus : LynxModuleWarningManager.this.modulesReportedLowBattery.values()) {
                if (lowBatteryStatus.conditionCurrentlyTrue()) {
                    arrayList.add(lowBatteryStatus.moduleName);
                } else if (lowBatteryStatus.conditionTrueDuringOpModeRun) {
                    arrayList2.add(lowBatteryStatus.moduleName);
                }
            }
            if (arrayList.size() > 0) {
                composeModuleList(arrayList, sb);
                sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModuleBatteryIsCurrentlyLow));
                z = true;
            }
            if (arrayList2.size() <= 0) {
                return z;
            }
            if (z) {
                sb.append(" ");
            }
            composeModuleList(arrayList2, sb);
            sb.append(AppUtil.getDefContext().getString(C0660R.string.lynxModuleBatteryWasLow));
            return true;
        }

        private void composePowerIssueTip(boolean z, StringBuilder sb) {
            if (z) {
                sb.append(AppUtil.getDefContext().getString(C0660R.string.powerIssueTip));
            } else {
                sb.append(AppUtil.getDefContext().getString(C0660R.string.robotOffTip));
            }
        }

        /* access modifiers changed from: package-private */
        public void composeModuleList(Collection<String> collection, StringBuilder sb) {
            Iterator<String> it = collection.iterator();
            while (it.hasNext()) {
                sb.append(it.next());
                if (it.hasNext()) {
                    sb.append(", ");
                }
            }
            sb.append(" ");
        }

        public void suppressGlobalWarning(boolean z) {
            synchronized (LynxModuleWarningManager.this.warningMessageLock) {
                if (z) {
                    this.warningMessageSuppressionCount++;
                } else {
                    this.warningMessageSuppressionCount--;
                }
            }
        }

        public void clearGlobalWarning() {
            synchronized (LynxModuleWarningManager.this.warningMessageLock) {
                String unused = LynxModuleWarningManager.this.cachedWarningMessage = null;
                LynxModuleWarningManager.this.modulesReportedUnresponsive.clear();
                LynxModuleWarningManager.this.modulesReportedReset.clear();
                LynxModuleWarningManager.this.modulesReportedLowBattery.clear();
                LynxModuleWarningManager.this.outdatedModules.clear();
                this.warningMessageSuppressionCount = 0;
            }
        }
    }

    private class WarningManagerOpModeListener implements OpModeManagerNotifier.Notifications {
        public void onOpModePreStart(OpMode opMode) {
        }

        private WarningManagerOpModeListener() {
        }

        public void onOpModePreInit(OpMode opMode) {
            boolean unused = LynxModuleWarningManager.this.userOpModeRunning = !(opMode instanceof OpModeManagerImpl.DefaultOpMode);
        }

        public void onOpModePostStop(OpMode opMode) {
            boolean unused = LynxModuleWarningManager.this.userOpModeRunning = false;
        }
    }
}
