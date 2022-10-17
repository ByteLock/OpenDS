package com.qualcomm.robotcore.util;

import com.qualcomm.robotcore.C0705R;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.MappedByteBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.network.WifiDirectAgent;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class ClassUtil {
    public static final String TAG = "ClassUtil";

    public static List<Constructor> getDeclaredConstructors(Class<?> cls) {
        Constructor[] constructorArr;
        try {
            constructorArr = cls.getDeclaredConstructors();
        } catch (Exception | LinkageError unused) {
            constructorArr = new Constructor[0];
        }
        LinkedList linkedList = new LinkedList();
        linkedList.addAll(Arrays.asList(constructorArr));
        return linkedList;
    }

    public static boolean inheritsFrom(Class cls, Class cls2) {
        while (true) {
            if (cls == null) {
                return false;
            }
            if (cls == cls2) {
                return true;
            }
            if (cls2.isInterface()) {
                for (Class inheritsFrom : cls.getInterfaces()) {
                    if (inheritsFrom(inheritsFrom, cls2)) {
                        return true;
                    }
                }
                continue;
            }
            cls = cls.getSuperclass();
        }
    }

    public static Method getDeclaredMethod(Class cls, String str, Class<?>... clsArr) {
        try {
            Method method = cls.getMethod(str, clsArr);
            method.setAccessible(true);
            return method;
        } catch (LinkageError unused) {
            return null;
        } catch (NoSuchMethodException e) {
            Class superclass = cls.getSuperclass();
            if (superclass != null) {
                return getDeclaredMethod(superclass, str, clsArr);
            }
            RobotLog.m51ee(WifiDirectAgent.TAG, e, "method not found: %s", str);
            return null;
        }
    }

    public static List<Method> getAllDeclaredMethods(Class cls) {
        ArrayList arrayList = new ArrayList();
        Class superclass = cls.getSuperclass();
        if (superclass != null) {
            arrayList.addAll(getAllDeclaredMethods(superclass));
        }
        arrayList.addAll(getLocalDeclaredMethods(cls));
        return arrayList;
    }

    public static List<Method> getLocalDeclaredMethods(Class<?> cls) {
        Method[] methodArr;
        try {
            methodArr = cls.getDeclaredMethods();
        } catch (Exception | LinkageError unused) {
            methodArr = new Method[0];
        }
        return Arrays.asList(methodArr);
    }

    public static Field getDeclaredField(Class cls, String str) {
        try {
            Field declaredField = cls.getDeclaredField(str);
            declaredField.setAccessible(true);
            return declaredField;
        } catch (LinkageError unused) {
            return null;
        } catch (NoSuchFieldException e) {
            Class superclass = cls.getSuperclass();
            if (superclass != null) {
                return getDeclaredField(superclass, str);
            }
            RobotLog.m51ee(WifiDirectAgent.TAG, e, "field not found: %s.%s", cls.getName(), str);
            return null;
        }
    }

    public static List<Field> getAllDeclaredFields(Class cls) {
        ArrayList arrayList = new ArrayList();
        Class superclass = cls.getSuperclass();
        if (superclass != null) {
            arrayList.addAll(getAllDeclaredFields(superclass));
        }
        arrayList.addAll(getLocalDeclaredFields(cls));
        return arrayList;
    }

    public static List<Field> getLocalDeclaredFields(Class<?> cls) {
        Field[] fieldArr;
        try {
            fieldArr = cls.getDeclaredFields();
        } catch (Exception | LinkageError unused) {
            fieldArr = new Field[0];
        }
        return Arrays.asList(fieldArr);
    }

    public static Object invoke(Object obj, Method method, Object... objArr) {
        try {
            return method.invoke(obj, objArr);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            if (cause instanceof RuntimeException) {
                throw ((RuntimeException) cause);
            }
            throw new RuntimeException(String.format("exception in %s#%s", new Object[]{method.getDeclaringClass().getSimpleName(), method.getName()}), cause);
        } catch (IllegalAccessException e2) {
            throw new RuntimeException(String.format("access denied in %s#%s", new Object[]{method.getDeclaringClass().getSimpleName(), method.getName()}), e2);
        }
    }

    public static boolean searchInheritance(Class cls, Predicate<Class<?>> predicate) {
        return searchInheritance(cls, predicate, new HashSet());
    }

    private static boolean searchInheritance(Class cls, Predicate<Class<?>> predicate, Set<Class<?>> set) {
        while (true) {
            if (cls == null || set.contains(cls)) {
                return false;
            }
            set.add(cls);
            if (predicate.test(cls)) {
                return true;
            }
            for (Class searchInheritance : cls.getInterfaces()) {
                if (searchInheritance(searchInheritance, predicate, set)) {
                    return true;
                }
            }
            cls = cls.getSuperclass();
        }
    }

    public static int getStringResId(String str, Class<?> cls) {
        return AppUtil.getDefContext().getResources().getIdentifier(str, "string", AppUtil.getDefContext().getPackageName());
    }

    public static String decodeStringRes(String str) {
        if (!str.startsWith("@string/")) {
            return str;
        }
        return AppUtil.getDefContext().getString(getStringResId(str.substring(8), C0705R.string.class));
    }

    protected static Class findClass(String str) {
        try {
            return Class.forName(str);
        } catch (ClassNotFoundException e) {
            RobotLog.m51ee(TAG, e, "class not found: %s", str);
            throw new RuntimeException("class not found");
        }
    }

    protected static class MappedByteBufferInfo {
        public static Field addressField;
        public static Field blockField;

        protected MappedByteBufferInfo() {
        }

        static {
            Field declaredField = ClassUtil.getDeclaredField(MappedByteBuffer.class, "block");
            blockField = declaredField;
            addressField = ClassUtil.getDeclaredField(declaredField.getType(), "address");
        }
    }

    public static long memoryAddressFrom(MappedByteBuffer mappedByteBuffer) {
        try {
            return ((Long) MappedByteBufferInfo.addressField.get(MappedByteBufferInfo.blockField.get(mappedByteBuffer))).longValue();
        } catch (Throwable th) {
            RobotLog.m50ee(TAG, th, "internal error: can't extract address from MappedByteBuffer");
            throw new RuntimeException("can't extract address from MappedByteBuffer");
        }
    }
}
