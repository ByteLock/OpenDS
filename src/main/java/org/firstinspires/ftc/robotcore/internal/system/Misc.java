package org.firstinspires.ftc.robotcore.internal.system;

import androidx.appcompat.widget.ActivityChooserView;
import com.qualcomm.hardware.lynx.LynxServoController;
import java.lang.reflect.Array;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.firstinspires.inspection.InspectionState;

public class Misc {
    public static final String TAG = "Misc";

    public static String formatForUser(String str) {
        return str;
    }

    public static String formatInvariant(String str) {
        return str;
    }

    public static boolean isEven(byte b) {
        return (b & 1) == 0;
    }

    public static boolean isEven(int i) {
        return (i & 1) == 0;
    }

    public static boolean isEven(long j) {
        return (j & 1) == 0;
    }

    public static boolean isEven(short s) {
        return (s & 1) == 0;
    }

    public static int saturatingAdd(int i, int i2) {
        if (!(i == 0 || i2 == 0)) {
            boolean z = true;
            boolean z2 = i > 0;
            if (i2 <= 0) {
                z = false;
            }
            if (!(z ^ z2)) {
                if (i > 0) {
                    return ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED - i < i2 ? ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED : i + i2;
                }
                if (Integer.MIN_VALUE - i > i2) {
                    return Integer.MIN_VALUE;
                }
                return i + i2;
            }
        }
        return i + i2;
    }

    public static long saturatingAdd(long j, long j2) {
        int i;
        int i2 = (j > 0 ? 1 : (j == 0 ? 0 : -1));
        if (!(i2 == 0 || j2 == 0)) {
            boolean z = true;
            boolean z2 = i2 > 0;
            if (i <= 0) {
                z = false;
            }
            if (!(z2 ^ z)) {
                if (i2 > 0) {
                    if (Long.MAX_VALUE - j < j2) {
                        return Long.MAX_VALUE;
                    }
                    return j + j2;
                } else if (Long.MIN_VALUE - j > j2) {
                    return Long.MIN_VALUE;
                } else {
                    return j + j2;
                }
            }
        }
        return j + j2;
    }

    public static String formatInvariant(String str, Object... objArr) {
        return String.format(Locale.ROOT, str, objArr);
    }

    public static String formatForUser(String str, Object... objArr) {
        return String.format(Locale.getDefault(), str, objArr);
    }

    public static String formatForUser(int i, Object... objArr) {
        return AppUtil.getDefContext().getString(i, objArr);
    }

    public static String formatForUser(int i) {
        return AppUtil.getDefContext().getString(i);
    }

    public static String encodeEntity(String str) {
        return encodeEntity(str, InspectionState.NO_VERSION);
    }

    public static String encodeEntity(String str, String str2) {
        StringBuilder sb = new StringBuilder();
        for (char c : str.toCharArray()) {
            if (c == '\"') {
                sb.append("&quot;");
            } else if (c == '<') {
                sb.append("&lt;");
            } else if (c == '>') {
                sb.append("&gt;");
            } else if (c == '&') {
                sb.append("&amp;");
            } else if (c == '\'') {
                sb.append("&apos;");
            } else if (str2.indexOf(c) >= 0) {
                sb.append(formatInvariant("&#x%x;", Character.valueOf(c)));
            } else {
                sb.append(c);
            }
        }
        return sb.toString();
    }

    public static String decodeEntity(String str) {
        StringBuilder sb = new StringBuilder();
        int i = 0;
        while (i < str.length()) {
            char charAt = str.charAt(i);
            if (charAt == '&') {
                int i2 = i + 1;
                int i3 = i2;
                while (str.charAt(i3) != ';') {
                    i3++;
                }
                String substring = str.substring(i2, i3 - 1);
                substring.hashCode();
                char c = 65535;
                switch (substring.hashCode()) {
                    case 3309:
                        if (substring.equals("gt")) {
                            c = 0;
                            break;
                        }
                        break;
                    case 3464:
                        if (substring.equals("lt")) {
                            c = 1;
                            break;
                        }
                        break;
                    case 96708:
                        if (substring.equals("amp")) {
                            c = 2;
                            break;
                        }
                        break;
                    case 3000915:
                        if (substring.equals("apos")) {
                            c = 3;
                            break;
                        }
                        break;
                    case 3482377:
                        if (substring.equals("quot")) {
                            c = 4;
                            break;
                        }
                        break;
                }
                switch (c) {
                    case 0:
                        sb.append('>');
                        break;
                    case 1:
                        sb.append('<');
                        break;
                    case 2:
                        sb.append('&');
                        break;
                    case 3:
                        sb.append('\'');
                        break;
                    case 4:
                        sb.append('\"');
                        break;
                    default:
                        if (substring.length() > 2 && substring.charAt(0) == '#' && substring.charAt(1) == 'x') {
                            sb.append((char) Integer.decode("0x" + substring.substring(2)).intValue());
                            break;
                        } else {
                            throw illegalArgumentException("illegal entity reference");
                        }
                        break;
                }
                i = i3;
            } else {
                sb.append(charAt);
            }
            i++;
        }
        return sb.toString();
    }

    public static boolean isOdd(byte b) {
        return !isEven(b);
    }

    public static boolean isOdd(short s) {
        return !isEven(s);
    }

    public static boolean isOdd(int i) {
        return !isEven(i);
    }

    public static boolean isOdd(long j) {
        return !isEven(j);
    }

    public static boolean isFinite(double d) {
        return !Double.isNaN(d) && !Double.isInfinite(d);
    }

    public static boolean approximatelyEquals(double d, double d2) {
        return approximatelyEquals(d, d2, 1.0E-9d);
    }

    public static boolean approximatelyEquals(double d, double d2, double d3) {
        if (d == d2) {
            return true;
        }
        if (d2 != LynxServoController.apiPositionFirst) {
            d = (d / d2) - 1.0d;
        }
        return Math.abs(d) < d3;
    }

    public static UUID uuidFromBytes(byte[] bArr, ByteOrder byteOrder) {
        Assert.assertTrue(bArr.length == 16);
        ByteBuffer order = ByteBuffer.wrap(bArr).order(byteOrder);
        ByteBuffer order2 = ByteBuffer.allocate(8).order(ByteOrder.BIG_ENDIAN);
        order2.putInt(order.getInt());
        order2.putShort(order.getShort());
        order2.putShort(order.getShort());
        order2.rewind();
        long j = order2.getLong();
        order2.rewind();
        order2.put(order);
        order2.rewind();
        return new UUID(j, order2.getLong());
    }

    public static boolean contains(byte[] bArr, byte b) {
        for (byte b2 : bArr) {
            if (b2 == b) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(short[] sArr, short s) {
        for (short s2 : sArr) {
            if (s2 == s) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(int[] iArr, int i) {
        for (int i2 : iArr) {
            if (i2 == i) {
                return true;
            }
        }
        return false;
    }

    public static boolean contains(long[] jArr, long j) {
        for (long j2 : jArr) {
            if (j2 == j) {
                return true;
            }
        }
        return false;
    }

    public static <T> T[] toArray(T[] tArr, Collection<T> collection) {
        int size = collection.size();
        if (tArr.length < size) {
            tArr = (Object[]) Array.newInstance(tArr.getClass().getComponentType(), size);
        }
        int i = 0;
        for (T t : collection) {
            tArr[i] = t;
            i++;
        }
        if (tArr.length > size) {
            tArr[size] = null;
        }
        return tArr;
    }

    public static <T> T[] toArray(T[] tArr, ArrayList<T> arrayList) {
        return arrayList.toArray(tArr);
    }

    public static long[] toLongArray(Collection<Long> collection) {
        long[] jArr = new long[collection.size()];
        int i = 0;
        for (Long longValue : collection) {
            jArr[i] = longValue.longValue();
            i++;
        }
        return jArr;
    }

    public static int[] toIntArray(Collection<Integer> collection) {
        int[] iArr = new int[collection.size()];
        int i = 0;
        for (Integer intValue : collection) {
            iArr[i] = intValue.intValue();
            i++;
        }
        return iArr;
    }

    public static short[] toShortArray(Collection<Short> collection) {
        short[] sArr = new short[collection.size()];
        int i = 0;
        for (Short shortValue : collection) {
            sArr[i] = shortValue.shortValue();
            i++;
        }
        return sArr;
    }

    public static byte[] toByteArray(Collection<Byte> collection) {
        byte[] bArr = new byte[collection.size()];
        int i = 0;
        for (Byte byteValue : collection) {
            bArr[i] = byteValue.byteValue();
            i++;
        }
        return bArr;
    }

    public static <E> Set<E> intersect(Set<E> set, Set<E> set2) {
        HashSet hashSet = new HashSet();
        for (E next : set) {
            if (set2.contains(next)) {
                hashSet.add(next);
            }
        }
        return hashSet;
    }

    public static IllegalArgumentException illegalArgumentException(String str) {
        return new IllegalArgumentException(str);
    }

    public static IllegalArgumentException illegalArgumentException(String str, Object... objArr) {
        return new IllegalArgumentException(formatInvariant(str, objArr));
    }

    public static IllegalArgumentException illegalArgumentException(Throwable th, String str, Object... objArr) {
        return new IllegalArgumentException(formatInvariant(str, objArr), th);
    }

    public static IllegalArgumentException illegalArgumentException(Throwable th, String str) {
        return new IllegalArgumentException(str, th);
    }

    public static IllegalStateException illegalStateException(String str) {
        return new IllegalStateException(str);
    }

    public static IllegalStateException illegalStateException(String str, Object... objArr) {
        return new IllegalStateException(formatInvariant(str, objArr));
    }

    public static IllegalStateException illegalStateException(Throwable th, String str, Object... objArr) {
        return new IllegalStateException(formatInvariant(str, objArr), th);
    }

    public static IllegalStateException illegalStateException(Throwable th, String str) {
        return new IllegalStateException(str, th);
    }

    public static RuntimeException internalError(String str) {
        return new RuntimeException("internal error:" + str);
    }

    public static RuntimeException internalError(String str, Object... objArr) {
        return new RuntimeException("internal error:" + formatInvariant(str, objArr));
    }

    public static RuntimeException internalError(Throwable th, String str, Object... objArr) {
        return new RuntimeException("internal error:" + formatInvariant(str, objArr), th);
    }

    public static RuntimeException internalError(Throwable th, String str) {
        return new RuntimeException("internal error:" + str, th);
    }
}
