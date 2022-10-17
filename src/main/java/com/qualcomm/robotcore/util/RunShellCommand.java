package com.qualcomm.robotcore.util;

import com.qualcomm.robotcore.exception.RobotCoreException;

public class RunShellCommand {
    private static int BUFFER_SIZE = 524288;
    private boolean logging = false;
    private Process process = null;

    public void enableLogging(boolean z) {
        this.logging = z;
    }

    public ProcessResult run(String str) {
        return runCommand(str, false);
    }

    public ProcessResult runAsRoot(String str) {
        return runCommand(str, true);
    }

    /* JADX WARNING: Code restructure failed: missing block: B:10:0x006b, code lost:
        if (r10 != null) goto L_0x006d;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:11:0x006d, code lost:
        r10.destroy();
     */
    /* JADX WARNING: Code restructure failed: missing block: B:16:0x007c, code lost:
        if (r10 == null) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:21:0x0085, code lost:
        if (r10 == null) goto L_0x0088;
     */
    /* JADX WARNING: Code restructure failed: missing block: B:23:0x008e, code lost:
        return new com.qualcomm.robotcore.util.RunShellCommand.ProcessResult(r8, r7, (com.qualcomm.robotcore.util.RunShellCommand.C07601) null);
     */
    /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0073 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    private com.qualcomm.robotcore.util.RunShellCommand.ProcessResult runCommand(java.lang.String r10, boolean r11) {
        /*
            r9 = this;
            java.lang.ProcessBuilder r0 = new java.lang.ProcessBuilder
            r1 = 0
            java.lang.String[] r2 = new java.lang.String[r1]
            r0.<init>(r2)
            int r2 = BUFFER_SIZE
            byte[] r2 = new byte[r2]
            r3 = 2
            java.lang.String r4 = "-c"
            r5 = 3
            r6 = 1
            java.lang.String r7 = ""
            r8 = -1
            if (r11 == 0) goto L_0x0028
            java.lang.String[] r11 = new java.lang.String[r5]     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.String r5 = "su"
            r11[r1] = r5     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11[r6] = r4     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11[r3] = r10     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.ProcessBuilder r11 = r0.command(r11)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11.redirectErrorStream(r6)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            goto L_0x0039
        L_0x0028:
            java.lang.String[] r11 = new java.lang.String[r5]     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.String r5 = "sh"
            r11[r1] = r5     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11[r6] = r4     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11[r3] = r10     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.ProcessBuilder r11 = r0.command(r11)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11.redirectErrorStream(r6)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
        L_0x0039:
            java.lang.Process r11 = r0.start()     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r9.process = r11     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            int r8 = r11.waitFor()     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.StringBuilder r11 = new java.lang.StringBuilder     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11.<init>()     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.String r0 = "Done running "
            r11.append(r0)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11.append(r10)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.String r10 = r11.toString()     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            com.qualcomm.robotcore.util.RobotLog.m52i(r10)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.lang.Process r10 = r9.process     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            java.io.InputStream r10 = r10.getInputStream()     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            int r10 = r10.read(r2)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            if (r10 <= 0) goto L_0x0069
            java.lang.String r11 = new java.lang.String     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r11.<init>(r2, r1, r10)     // Catch:{ IOException -> 0x007f, InterruptedException -> 0x0073 }
            r7 = r11
        L_0x0069:
            java.lang.Process r10 = r9.process
            if (r10 == 0) goto L_0x0088
        L_0x006d:
            r10.destroy()
            goto L_0x0088
        L_0x0071:
            r10 = move-exception
            goto L_0x008f
        L_0x0073:
            java.lang.Thread r10 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0071 }
            r10.interrupt()     // Catch:{ all -> 0x0071 }
            java.lang.Process r10 = r9.process
            if (r10 == 0) goto L_0x0088
            goto L_0x006d
        L_0x007f:
            r10 = move-exception
            com.qualcomm.robotcore.util.RobotLog.logStackTrace(r10)     // Catch:{ all -> 0x0071 }
            java.lang.Process r10 = r9.process
            if (r10 == 0) goto L_0x0088
            goto L_0x006d
        L_0x0088:
            com.qualcomm.robotcore.util.RunShellCommand$ProcessResult r10 = new com.qualcomm.robotcore.util.RunShellCommand$ProcessResult
            r11 = 0
            r10.<init>(r8, r7)
            return r10
        L_0x008f:
            java.lang.Process r11 = r9.process
            if (r11 == 0) goto L_0x0096
            r11.destroy()
        L_0x0096:
            throw r10
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.util.RunShellCommand.runCommand(java.lang.String, boolean):com.qualcomm.robotcore.util.RunShellCommand$ProcessResult");
    }

    public void commitSeppuku() {
        Process process2 = this.process;
        if (process2 != null) {
            process2.destroy();
            try {
                this.process.waitFor();
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    public static void killSpawnedProcess(String str, String str2) throws RobotCoreException {
        try {
            int spawnedProcessPid = getSpawnedProcessPid(str, str2);
            while (spawnedProcessPid != -1) {
                RobotLog.m58v("Killing PID " + spawnedProcessPid);
                new RunShellCommand().run(String.format("kill %d", new Object[]{Integer.valueOf(spawnedProcessPid)}));
                spawnedProcessPid = getSpawnedProcessPid(str, str2);
            }
        } catch (Exception unused) {
            throw new RobotCoreException(String.format("Failed to kill %s instances started by this app", new Object[]{str}));
        }
    }

    public static int getSpawnedProcessPid(String str, String str2) {
        String str3;
        String access$100 = new RunShellCommand().runCommand("ps", false).output;
        String[] split = access$100.split("\n");
        int length = split.length;
        int i = 0;
        while (true) {
            if (i >= length) {
                str3 = "invalid";
                break;
            }
            String str4 = split[i];
            if (str4.contains(str2)) {
                str3 = str4.split("\\s+")[0];
                break;
            }
            i++;
        }
        for (String str5 : access$100.split("\n")) {
            if (str5.contains(str) && str5.contains(str3)) {
                return Integer.parseInt(str5.split("\\s+")[1]);
            }
        }
        return -1;
    }

    public static final class ProcessResult {
        /* access modifiers changed from: private */
        public final String output;
        private final int returnCode;

        private ProcessResult(int i, String str) {
            this.returnCode = i;
            this.output = str;
        }

        public int getReturnCode() {
            return this.returnCode;
        }

        public String getOutput() {
            return this.output;
        }

        public boolean equals(Object obj) {
            if (this == obj) {
                return true;
            }
            if (obj == null || getClass() != obj.getClass()) {
                return false;
            }
            ProcessResult processResult = (ProcessResult) obj;
            if (this.returnCode != processResult.returnCode) {
                return false;
            }
            String str = this.output;
            String str2 = processResult.output;
            if (str != null) {
                return str.equals(str2);
            }
            if (str2 == null) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            int i = this.returnCode * 31;
            String str = this.output;
            return i + (str != null ? str.hashCode() : 0);
        }
    }
}
