package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;

public class Assert {
    public static final String TAG = "Assert";

    public static void assertTrue(boolean z) {
        if (!z) {
            assertFailed();
        }
    }

    public static void assertFalse(boolean z) {
        if (z) {
            assertFailed();
        }
    }

    public static void assertNull(Object obj) {
        if (obj != null) {
            assertFailed();
        }
    }

    public static void assertNotNull(Object obj) {
        if (obj == null) {
            assertFailed();
        }
    }

    public static void assertEquals(int i, int i2) {
        if (i != i2) {
            assertFailed();
        }
    }

    public static void assertTrue(boolean z, String str, Object... objArr) {
        if (!z) {
            assertFailed(str, objArr);
        }
    }

    public static void assertFalse(boolean z, String str, Object... objArr) {
        if (z) {
            assertFailed(str, objArr);
        }
    }

    public static void assertNull(Object obj, String str, Object... objArr) {
        if (obj != null) {
            assertFailed(str, objArr);
        }
    }

    public static void assertNotNull(Object obj, String str, Object... objArr) {
        if (obj == null) {
            assertFailed(str, objArr);
        }
    }

    public static void assertFailed() {
        try {
            throw new RuntimeException("assertion failed");
        } catch (Exception e) {
            RobotLog.m38aa(TAG, (Throwable) e, "assertion failed");
        }
    }

    public static void assertFailed(String str, Object[] objArr) {
        String str2 = "assertion failed: " + String.format(str, objArr);
        try {
            throw new RuntimeException(str2);
        } catch (Exception e) {
            RobotLog.m38aa(TAG, (Throwable) e, str2);
        }
    }
}
