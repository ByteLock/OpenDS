package com.qualcomm.robotcore.util;

import android.content.Context;
import android.os.Build;
import android.util.Log;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.exception.RobotCoreException;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Locale;
import java.util.WeakHashMap;
import org.firstinspires.ftc.robotcore.internal.files.LogOutputStream;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Misc;
import org.firstinspires.inspection.InspectionState;

public class RobotLog {
    public static final String OPMODE_START_TAG = "******************** START - OPMODE %s ********************";
    public static final String OPMODE_STOP_TAG = "******************** STOP - OPMODE %s ********************";
    public static final String TAG = "RobotCore";
    private static final Object globalErrorLock = new Object();
    private static String globalErrorMessage = "";
    private static boolean globalErrorMsgSticky = false;
    private static final Object globalWarningLock = new Object();
    private static String globalWarningMessage = "";
    private static boolean globalWarningMsgSticky = false;
    private static WeakHashMap<GlobalWarningSource, Integer> globalWarningSources = new WeakHashMap<>();
    private static final int kbLogcatQuantum = 4096;
    private static final String logcatCommand = "exec logcat";
    private static final String logcatCommandRaw = "logcat";
    private static final String logcatFilter = "UsbRequestJNI:S UsbRequest:S art:W ThreadPool:W System:W ExtendedExtractor:W OMXClient:W MediaPlayer:W dalvikvm:W  *:V";
    private static final String logcatFormat = "threadtime";
    private static final int logcatRotatedLogsMax = 4;
    /* access modifiers changed from: private */
    public static LoggingThread loggingThread = null;
    private static String matchLogFilename = null;
    private static Calendar matchStartTime = null;
    private static double msTimeOffset = LynxServoController.apiPositionFirst;

    public static class GlobalWarningMessage {
        public final boolean deservesWarningSound;
        public final String message;

        public GlobalWarningMessage(String str, boolean z) {
            this.message = str;
            this.deservesWarningSound = z;
        }
    }

    protected static class LoggingThread extends Thread {
        private RunShellCommand shell = new RunShellCommand();

        LoggingThread(String str) {
            super(str);
        }

        public void run(String str) {
            this.shell.run(str);
        }

        public void kill() {
            this.shell.commitSeppuku();
        }
    }

    private RobotLog() {
    }

    public static void processTimeSynch(long j, long j2, long j3, long j4) {
        if (j != 0 && j2 != 0 && j3 != 0 && j4 != 0) {
            setMsTimeOffset(((double) ((j2 - j) + (j3 - j4))) / 2.0d);
        }
    }

    public static void setMsTimeOffset(double d) {
        msTimeOffset = d;
    }

    public static long getRemoteTime() {
        return getRemoteTime(AppUtil.getInstance().getWallClockTime());
    }

    public static long getRemoteTime(long j) {
        return (long) (((double) j) + msTimeOffset + 0.5d);
    }

    public static long getLocalTime(long j) {
        return (long) ((((double) j) - msTimeOffset) + 0.5d);
    }

    /* renamed from: a */
    public static void m35a(String str, Object... objArr) {
        m58v(String.format(str, objArr));
    }

    /* renamed from: a */
    public static void m34a(String str) {
        internalLog(7, TAG, str);
    }

    /* renamed from: aa */
    public static void m37aa(String str, String str2, Object... objArr) {
        m60vv(str, String.format(str2, objArr));
    }

    /* renamed from: aa */
    public static void m36aa(String str, String str2) {
        internalLog(7, str, str2);
    }

    /* renamed from: aa */
    public static void m39aa(String str, Throwable th, String str2, Object... objArr) {
        m62vv(str, th, String.format(str2, objArr));
    }

    /* renamed from: aa */
    public static void m38aa(String str, Throwable th, String str2) {
        internalLog(7, str, th, str2);
    }

    /* renamed from: v */
    public static void m59v(String str, Object... objArr) {
        m58v(String.format(str, objArr));
    }

    /* renamed from: v */
    public static void m58v(String str) {
        internalLog(2, TAG, str);
    }

    /* renamed from: vv */
    public static void m61vv(String str, String str2, Object... objArr) {
        m60vv(str, String.format(str2, objArr));
    }

    /* renamed from: vv */
    public static void m60vv(String str, String str2) {
        internalLog(2, str, str2);
    }

    /* renamed from: vv */
    public static void m63vv(String str, Throwable th, String str2, Object... objArr) {
        m62vv(str, th, String.format(str2, objArr));
    }

    /* renamed from: vv */
    public static void m62vv(String str, Throwable th, String str2) {
        internalLog(2, str, th, str2);
    }

    /* renamed from: d */
    public static void m41d(String str, Object... objArr) {
        m40d(String.format(str, objArr));
    }

    /* renamed from: d */
    public static void m40d(String str) {
        internalLog(3, TAG, str);
    }

    /* renamed from: dd */
    public static void m43dd(String str, String str2, Object... objArr) {
        m42dd(str, String.format(str2, objArr));
    }

    /* renamed from: dd */
    public static void m42dd(String str, String str2) {
        internalLog(3, str, str2);
    }

    /* renamed from: dd */
    public static void m45dd(String str, Throwable th, String str2, Object... objArr) {
        m44dd(str, th, String.format(str2, objArr));
    }

    /* renamed from: dd */
    public static void m44dd(String str, Throwable th, String str2) {
        internalLog(3, str, th, str2);
    }

    /* renamed from: i */
    public static void m53i(String str, Object... objArr) {
        m52i(String.format(str, objArr));
    }

    /* renamed from: i */
    public static void m52i(String str) {
        internalLog(4, TAG, str);
    }

    /* renamed from: ii */
    public static void m55ii(String str, String str2, Object... objArr) {
        m54ii(str, String.format(str2, objArr));
    }

    /* renamed from: ii */
    public static void m54ii(String str, String str2) {
        internalLog(4, str, str2);
    }

    /* renamed from: ii */
    public static void m57ii(String str, Throwable th, String str2, Object... objArr) {
        m56ii(str, th, String.format(str2, objArr));
    }

    /* renamed from: ii */
    public static void m56ii(String str, Throwable th, String str2) {
        internalLog(4, str, th, str2);
    }

    /* renamed from: w */
    public static void m65w(String str, Object... objArr) {
        m64w(String.format(str, objArr));
    }

    /* renamed from: w */
    public static void m64w(String str) {
        internalLog(5, TAG, str);
    }

    /* renamed from: ww */
    public static void m67ww(String str, String str2, Object... objArr) {
        m66ww(str, String.format(str2, objArr));
    }

    /* renamed from: ww */
    public static void m66ww(String str, String str2) {
        internalLog(5, str, str2);
    }

    /* renamed from: ww */
    public static void m69ww(String str, Throwable th, String str2, Object... objArr) {
        m68ww(str, th, String.format(str2, objArr));
    }

    /* renamed from: ww */
    public static void m68ww(String str, Throwable th, String str2) {
        internalLog(5, str, th, str2);
    }

    /* renamed from: e */
    public static void m47e(String str, Object... objArr) {
        m46e(String.format(str, objArr));
    }

    /* renamed from: e */
    public static void m46e(String str) {
        internalLog(6, TAG, str);
    }

    /* renamed from: ee */
    public static void m49ee(String str, String str2, Object... objArr) {
        m48ee(str, String.format(str2, objArr));
    }

    /* renamed from: ee */
    public static void m48ee(String str, String str2) {
        internalLog(6, str, str2);
    }

    /* renamed from: ee */
    public static void m51ee(String str, Throwable th, String str2, Object... objArr) {
        m50ee(str, th, String.format(str2, objArr));
    }

    /* renamed from: ee */
    public static void m50ee(String str, Throwable th, String str2) {
        internalLog(6, str, th, str2);
    }

    public static void internalLog(int i, String str, String str2) {
        if (msTimeOffset == LynxServoController.apiPositionFirst) {
            Log.println(i, str, str2);
            return;
        }
        GregorianCalendar gregorianCalendar = new GregorianCalendar();
        gregorianCalendar.setTimeInMillis(getRemoteTime());
        Log.println(i, str, Misc.formatInvariant("{%5d %2d.%03d} %s", Integer.valueOf((int) (msTimeOffset + 0.5d)), Integer.valueOf(gregorianCalendar.get(13)), Integer.valueOf(gregorianCalendar.get(14)), str2));
    }

    public static void internalLog(int i, String str, Throwable th, String str2) {
        internalLog(i, str, str2);
        logStackTrace(str, th);
    }

    public static void logExceptionHeader(Exception exc, String str, Object... objArr) {
        m47e("exception %s(%s): %s [%s]", exc.getClass().getSimpleName(), exc.getMessage(), String.format(str, objArr), getStackTop(exc));
    }

    public static void logExceptionHeader(String str, Exception exc, String str2, Object... objArr) {
        m49ee(str, "exception %s(%s): %s [%s]", exc.getClass().getSimpleName(), exc.getMessage(), String.format(str2, objArr), getStackTop(exc));
    }

    private static StackTraceElement getStackTop(Exception exc) {
        StackTraceElement[] stackTrace = exc.getStackTrace();
        if (stackTrace.length > 0) {
            return stackTrace[0];
        }
        return null;
    }

    @Deprecated
    public static void logStacktrace(Throwable th) {
        logStackTrace(th);
    }

    public static void logStackTrace(Throwable th) {
        logStackTrace(TAG, th);
    }

    public static void logStackTrace(Thread thread, String str, Object... objArr) {
        m47e("thread id=%d tid=%d name=\"%s\" %s", Long.valueOf(thread.getId()), Integer.valueOf(ThreadPool.getTID(thread)), thread.getName(), String.format(str, objArr));
        logStackFrames(thread.getStackTrace());
    }

    public static void logStackTrace(Thread thread, StackTraceElement[] stackTraceElementArr) {
        m47e("thread id=%d tid=%d name=\"%s\"", Long.valueOf(thread.getId()), Integer.valueOf(ThreadPool.getTID(thread)), thread.getName());
        logStackFrames(stackTraceElementArr);
    }

    public static void logStackTrace(String str, Throwable th) {
        if (th != null) {
            th.printStackTrace(LogOutputStream.printStream(str));
        }
    }

    private static void logStackFrames(StackTraceElement[] stackTraceElementArr) {
        int length = stackTraceElementArr.length;
        for (int i = 0; i < length; i++) {
            m47e("    at %s", stackTraceElementArr[i].toString());
        }
    }

    public static void logAndThrow(String str) throws RobotCoreException {
        m64w(str);
        throw new RobotCoreException(str);
    }

    public static boolean setGlobalErrorMsg(String str) {
        synchronized (globalErrorLock) {
            if (!globalErrorMessage.isEmpty()) {
                return false;
            }
            globalErrorMessage += str;
            return true;
        }
    }

    public static void setGlobalErrorMsg(String str, Object... objArr) {
        setGlobalErrorMsg(String.format(str, objArr));
    }

    public static void addGlobalWarningMessage(String str) {
        synchronized (globalWarningLock) {
            if (!globalWarningMessage.isEmpty()) {
                globalWarningMessage += "; " + str;
            } else {
                globalWarningMessage = str;
            }
        }
    }

    public static void addGlobalWarningMessage(String str, Object... objArr) {
        addGlobalWarningMessage(String.format(str, objArr));
    }

    public static void registerGlobalWarningSource(GlobalWarningSource globalWarningSource) {
        synchronized (globalWarningLock) {
            globalWarningSources.put(globalWarningSource, 1);
        }
    }

    public static void unregisterGlobalWarningSource(GlobalWarningSource globalWarningSource) {
        synchronized (globalWarningLock) {
            globalWarningSources.remove(globalWarningSource);
        }
    }

    public static void setGlobalErrorMsg(RobotCoreException robotCoreException, String str) {
        setGlobalErrorMsg(str + ": " + robotCoreException.getMessage());
    }

    public static void setGlobalErrorMsgAndThrow(RobotCoreException robotCoreException, String str) throws RobotCoreException {
        setGlobalErrorMsg(robotCoreException, str);
        throw robotCoreException;
    }

    public static void setGlobalErrorMsg(RuntimeException runtimeException, String str) {
        setGlobalErrorMsg(String.format("%s: %s: %s", new Object[]{str, runtimeException.getClass().getSimpleName(), runtimeException.getMessage()}));
    }

    public static void setGlobalErrorMsgAndThrow(RuntimeException runtimeException, String str) throws RobotCoreException {
        setGlobalErrorMsg(runtimeException, str);
        throw runtimeException;
    }

    public static String getGlobalErrorMsg() {
        String str;
        synchronized (globalErrorLock) {
            str = globalErrorMessage;
        }
        return str;
    }

    public static void setGlobalErrorMsgSticky(boolean z) {
        globalErrorMsgSticky = z;
    }

    public static GlobalWarningMessage getGlobalWarningMessage() {
        boolean z;
        ArrayList arrayList = new ArrayList();
        synchronized (globalWarningLock) {
            arrayList.add(globalWarningMessage);
            z = false;
            for (GlobalWarningSource next : globalWarningSources.keySet()) {
                String globalWarning = next.getGlobalWarning();
                if (globalWarning != null && !globalWarning.isEmpty()) {
                    arrayList.add(globalWarning);
                    if (next.shouldTriggerWarningSound()) {
                        z = true;
                    }
                }
            }
        }
        return new GlobalWarningMessage(combineGlobalWarnings(arrayList), z);
    }

    public static void setGlobalWarningMsgSticky(boolean z) {
        globalWarningMsgSticky = z;
    }

    public static String combineGlobalWarnings(List<String> list) {
        StringBuilder sb = new StringBuilder();
        for (String next : list) {
            if (next != null && !next.isEmpty()) {
                if (sb.length() > 0) {
                    sb.append("\n\n");
                }
                sb.append(next);
            }
        }
        return sb.toString();
    }

    public static boolean hasGlobalErrorMsg() {
        return !getGlobalErrorMsg().isEmpty();
    }

    public static boolean hasGlobalWarningMsg() {
        return !getGlobalWarningMessage().message.isEmpty();
    }

    public static void clearGlobalErrorMsg() {
        if (!globalErrorMsgSticky) {
            synchronized (globalErrorLock) {
                globalErrorMessage = InspectionState.NO_VERSION;
            }
        }
    }

    public static void clearGlobalWarningMsg() {
        if (!globalWarningMsgSticky) {
            synchronized (globalWarningLock) {
                globalWarningMessage = InspectionState.NO_VERSION;
            }
        }
    }

    public static void onApplicationStart() {
        writeLogcatToDisk(AppUtil.getDefContext(), 4096);
    }

    protected static synchronized void writeLogcatToDisk(final Context context, final int i) {
        synchronized (RobotLog.class) {
            if (loggingThread == null) {
                C07571 r1 = new LoggingThread("Logging Thread") {
                    public void run() {
                        String absolutePath = RobotLog.getLogFile(context).getAbsolutePath();
                        String format = String.format("%s -f %s -r%d -n%d -v %s %s", new Object[]{RobotLog.logcatCommand, absolutePath, Integer.valueOf(i), 4, RobotLog.logcatFormat, RobotLog.logcatFilter});
                        RobotLog.m60vv(RobotLog.TAG, "saving logcat to " + absolutePath);
                        RobotLog.m60vv(RobotLog.TAG, "logging command line: " + format);
                        try {
                            RunShellCommand.killSpawnedProcess(RobotLog.logcatCommandRaw, context.getPackageName());
                        } catch (RobotCoreException e) {
                            e.printStackTrace();
                        }
                        run(format);
                        LoggingThread unused = RobotLog.loggingThread = null;
                    }
                };
                loggingThread = r1;
                r1.start();
            }
        }
    }

    public static void startMatchLogging(Context context, String str, int i) throws RobotCoreException {
        matchStartTime = Calendar.getInstance();
        matchLogFilename = getMatchLogFilename(context, str, i);
        m54ii(TAG, String.format(OPMODE_START_TAG, new Object[]{str}));
    }

    public static synchronized void stopMatchLogging() {
        synchronized (RobotLog.class) {
            if (matchStartTime != null) {
                m54ii(TAG, String.format(OPMODE_STOP_TAG, new Object[]{matchLogFilename}));
                logMatch();
            }
            matchStartTime = null;
        }
    }

    private static void logMatch() {
        File file = new File(matchLogFilename);
        final String absolutePath = file.getAbsolutePath();
        pruneMatchLogsIfNecessary();
        if (file.exists() && !file.delete()) {
            m48ee(TAG, "Could not delete match log file: " + absolutePath);
        }
        String format = String.format(Locale.ENGLISH, "'%d-%d %d:%d:%d.000'", new Object[]{Integer.valueOf(matchStartTime.get(2) + 1), Integer.valueOf(matchStartTime.get(5)), Integer.valueOf(matchStartTime.get(11)), Integer.valueOf(matchStartTime.get(12)), Integer.valueOf(matchStartTime.get(13))});
        final String format2 = String.format(Locale.ENGLISH, "%s -d -T %s -f '%s' -n%d -v %s %s", new Object[]{logcatCommand, format, absolutePath, 4, logcatFormat, logcatFilter});
        new LoggingThread("MatchLogging") {
            public void run() {
                RobotLog.m54ii(RobotLog.TAG, "saving match logcat to " + absolutePath);
                RobotLog.m54ii(RobotLog.TAG, "logging command line: " + format2);
                run(format2);
                RobotLog.m54ii(RobotLog.TAG, "exiting match logcat for " + absolutePath);
            }
        }.start();
    }

    public static String getLogFilename() {
        return getLogFilename(AppUtil.getDefContext());
    }

    public static String getLogFilename(Context context) {
        String str;
        File file = AppUtil.LOG_FOLDER;
        file.mkdirs();
        if (AppUtil.getInstance().isRobotController()) {
            str = "robotControllerLog.txt";
        } else if (AppUtil.getInstance().isDriverStation()) {
            str = "driverStationLog.txt";
        } else {
            str = context.getPackageName() + "Log.txt";
        }
        return new File(file, str).getAbsolutePath();
    }

    protected static void pruneMatchLogsIfNecessary() {
        File[] listFiles = AppUtil.MATCH_LOG_FOLDER.listFiles();
        if (listFiles.length >= 9) {
            Arrays.sort(listFiles, new Comparator<File>() {
                public int compare(File file, File file2) {
                    return Long.compare(file.lastModified(), file2.lastModified());
                }
            });
            for (int i = 0; i < listFiles.length - 9; i++) {
                m54ii(TAG, "Pruning old match logs: deleting " + listFiles[i].getName());
                listFiles[i].delete();
            }
        }
    }

    public static String getMatchLogFilename(Context context, String str, int i) {
        File file = AppUtil.MATCH_LOG_FOLDER;
        file.mkdirs();
        return new File(file, String.format(Locale.ENGLISH, "Match-%d-%s.txt", new Object[]{Integer.valueOf(i), str.replaceAll(" ", "_")})).getAbsolutePath();
    }

    /* access modifiers changed from: private */
    public static File getLogFile(Context context) {
        return new File(getLogFilename(context));
    }

    private static File getMatchLogFile(Context context, String str, int i) {
        return new File(getMatchLogFilename(context, str, i));
    }

    public static List<File> getExtantLogFiles(Context context) {
        ArrayList arrayList = new ArrayList();
        File logFile = getLogFile(context);
        arrayList.add(logFile);
        int i = 1;
        while (true) {
            File parentFile = logFile.getParentFile();
            File file = new File(parentFile, logFile.getName() + "." + i + ".gz");
            if (!file.exists()) {
                return arrayList;
            }
            arrayList.add(file);
            i++;
        }
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(11:7|8|9|10|11|12|13|(4:16|(2:18|26)(1:27)|19|14)|25|20|21) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:12:0x001c */
    /* JADX WARNING: Removed duplicated region for block: B:16:0x002f  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public static synchronized void cancelWriteLogcatToDisk() {
        /*
            java.lang.Class<com.qualcomm.robotcore.util.RobotLog> r0 = com.qualcomm.robotcore.util.RobotLog.class
            monitor-enter(r0)
            com.qualcomm.robotcore.util.RobotLog$LoggingThread r1 = loggingThread     // Catch:{ all -> 0x0047 }
            if (r1 != 0) goto L_0x0009
            monitor-exit(r0)
            return
        L_0x0009:
            android.app.Application r1 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getDefContext()     // Catch:{ all -> 0x0047 }
            r1.getPackageName()     // Catch:{ all -> 0x0047 }
            java.io.File r1 = getLogFile(r1)     // Catch:{ all -> 0x0047 }
            r1.getAbsolutePath()     // Catch:{ all -> 0x0047 }
            r1 = 500(0x1f4, double:2.47E-321)
            java.lang.Thread.sleep(r1)     // Catch:{ InterruptedException -> 0x001c }
        L_0x001c:
            com.qualcomm.robotcore.util.RobotLog$LoggingThread r1 = loggingThread     // Catch:{ all -> 0x0047 }
            r1.kill()     // Catch:{ all -> 0x0047 }
            java.lang.String r1 = "Waiting for the logcat process to die."
            m58v(r1)     // Catch:{ all -> 0x0047 }
            com.qualcomm.robotcore.util.ElapsedTime r1 = new com.qualcomm.robotcore.util.ElapsedTime     // Catch:{ all -> 0x0047 }
            r1.<init>()     // Catch:{ all -> 0x0047 }
        L_0x002b:
            com.qualcomm.robotcore.util.RobotLog$LoggingThread r2 = loggingThread     // Catch:{ all -> 0x0047 }
            if (r2 == 0) goto L_0x0045
            double r2 = r1.milliseconds()     // Catch:{ all -> 0x0047 }
            r4 = 4652007308841189376(0x408f400000000000, double:1000.0)
            int r2 = (r2 > r4 ? 1 : (r2 == r4 ? 0 : -1))
            if (r2 <= 0) goto L_0x0041
            com.qualcomm.robotcore.util.RobotLog$LoggingThread r2 = loggingThread     // Catch:{ all -> 0x0047 }
            r2.interrupt()     // Catch:{ all -> 0x0047 }
        L_0x0041:
            java.lang.Thread.yield()     // Catch:{ all -> 0x0047 }
            goto L_0x002b
        L_0x0045:
            monitor-exit(r0)
            return
        L_0x0047:
            r1 = move-exception
            monitor-exit(r0)
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.util.RobotLog.cancelWriteLogcatToDisk():void");
    }

    public static void logAppInfo() {
        m53i("App info: version=%s appId=%s", String.format(Locale.ENGLISH, "%s.%s.%s", new Object[]{8, 0, 0}), AppUtil.getInstance().getApplicationId());
    }

    public static void logDeviceInfo() {
        m53i("Android Device: maker=%s model=%s sdk=%d serial=%s", Build.MANUFACTURER, Build.MODEL, Integer.valueOf(Build.VERSION.SDK_INT), Device.getSerialNumberOrUnknown());
    }

    public static void logBytes(String str, String str2, byte[] bArr, int i) {
        logBytes(str, str2, bArr, 0, i);
    }

    public static void logBytes(String str, String str2, byte[] bArr, int i, int i2) {
        int i3;
        char c = ':';
        while (i < i2) {
            StringBuilder sb = new StringBuilder();
            int i4 = 0;
            while (i4 < 16 && (i3 = i4 + i) < i2) {
                sb.append(String.format("%02x ", new Object[]{Byte.valueOf(bArr[i3])}));
                i4++;
            }
            m61vv(str, "%s%c %s", str2, Character.valueOf(c), sb.toString());
            c = '|';
            i += 16;
        }
    }
}
