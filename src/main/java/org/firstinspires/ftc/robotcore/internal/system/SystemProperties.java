package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.ClassUtil;
import java.lang.reflect.Method;

public class SystemProperties {
    public static final String TAG = "SystemProperties";
    protected static Class<?> clazzSystemProperties;
    protected static Method methodBooleanGet;
    protected static Method methodIntGet;
    protected static Method methodLongGet;
    protected static Method methodStringGetDefault;
    protected static Method methodStringSet;

    static {
        try {
            Class<?> cls = Class.forName("android.os.SystemProperties");
            clazzSystemProperties = cls;
            methodStringGetDefault = cls.getMethod("get", new Class[]{String.class, String.class});
            methodStringSet = clazzSystemProperties.getMethod("set", new Class[]{String.class, String.class});
            methodIntGet = clazzSystemProperties.getMethod("getInt", new Class[]{String.class, Integer.TYPE});
            methodLongGet = clazzSystemProperties.getMethod("getLong", new Class[]{String.class, Long.TYPE});
            methodBooleanGet = clazzSystemProperties.getMethod("getBoolean", new Class[]{String.class, Boolean.TYPE});
        } catch (ClassNotFoundException | NoSuchMethodException unused) {
        }
    }

    public static String get(String str, String str2) {
        return (String) ClassUtil.invoke((Object) null, methodStringGetDefault, str, str2);
    }

    public static int getInt(String str, int i) {
        return ((Integer) ClassUtil.invoke((Object) null, methodIntGet, str, Integer.valueOf(i))).intValue();
    }

    public static long getLong(String str, long j) {
        return ((Long) ClassUtil.invoke((Object) null, methodLongGet, str, Long.valueOf(j))).longValue();
    }

    public static boolean getBoolean(String str, boolean z) {
        return ((Boolean) ClassUtil.invoke((Object) null, methodBooleanGet, str, Boolean.valueOf(z))).booleanValue();
    }

    public static void set(String str, String str2) {
        ClassUtil.invoke((Object) null, methodStringSet, str, str2);
    }
}
