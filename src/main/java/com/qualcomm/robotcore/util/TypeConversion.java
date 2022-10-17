package com.qualcomm.robotcore.util;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.Charset;

public class TypeConversion {
    private static final Charset UTF8_CHARSET = Charset.forName("UTF-8");

    public static double unsignedByteToDouble(byte b) {
        return (double) (b & 255);
    }

    public static int unsignedByteToInt(byte b) {
        return b & 255;
    }

    public static long unsignedIntToLong(int i) {
        return ((long) i) & 4294967295L;
    }

    public static int unsignedShortToInt(short s) {
        return s & 65535;
    }

    private TypeConversion() {
    }

    public static byte[] shortToByteArray(short s) {
        return shortToByteArray(s, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] shortToByteArray(short s, ByteOrder byteOrder) {
        return ByteBuffer.allocate(2).order(byteOrder).putShort(s).array();
    }

    public static byte[] intToByteArray(int i) {
        return intToByteArray(i, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] intToByteArray(int i, ByteOrder byteOrder) {
        return ByteBuffer.allocate(4).order(byteOrder).putInt(i).array();
    }

    public static byte[] longToByteArray(long j) {
        return longToByteArray(j, ByteOrder.BIG_ENDIAN);
    }

    public static byte[] longToByteArray(long j, ByteOrder byteOrder) {
        return ByteBuffer.allocate(8).order(byteOrder).putLong(j).array();
    }

    public static short byteArrayToShort(byte[] bArr) {
        return byteArrayToShort(bArr, ByteOrder.BIG_ENDIAN);
    }

    public static short byteArrayToShort(byte[] bArr, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bArr).order(byteOrder).getShort();
    }

    public static short byteArrayToShort(byte[] bArr, int i, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bArr, i, bArr.length - i).order(byteOrder).getShort();
    }

    public static int byteArrayToInt(byte[] bArr) {
        return byteArrayToInt(bArr, ByteOrder.BIG_ENDIAN);
    }

    public static int byteArrayToInt(byte[] bArr, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bArr).order(byteOrder).getInt();
    }

    public static long byteArrayToLong(byte[] bArr) {
        return byteArrayToLong(bArr, ByteOrder.BIG_ENDIAN);
    }

    public static long byteArrayToLong(byte[] bArr, ByteOrder byteOrder) {
        return ByteBuffer.wrap(bArr).order(byteOrder).getLong();
    }

    public static byte[] stringToUtf8(String str) {
        Charset charset = UTF8_CHARSET;
        byte[] bytes = str.getBytes(charset);
        if (str.equals(new String(bytes, charset))) {
            return bytes;
        }
        throw new IllegalArgumentException(String.format("string cannot be cleanly encoded into %s - '%s' -> '%s'", new Object[]{charset.name(), str, new String(bytes, charset)}));
    }

    public static int doubleToFixedInt(double d, int i) {
        return (int) Math.round(d * power2(i));
    }

    public static double doubleFromFixed(int i, int i2) {
        return ((double) i) / power2(i2);
    }

    public static long doubleToFixedLong(double d, int i) {
        return Math.round(d * power2(i));
    }

    public static double doubleFromFixed(long j, int i) {
        return ((double) j) / power2(i);
    }

    public static String utf8ToString(byte[] bArr) {
        return new String(bArr, UTF8_CHARSET);
    }

    private static double power2(int i) {
        if (i == 20) {
            return 1048576.0d;
        }
        if (i == 24) {
            return 1.6777216E7d;
        }
        if (i == 32) {
            return 4.294967296E9d;
        }
        if (i == 64) {
            return 1.8446744073709552E19d;
        }
        switch (i) {
            case 0:
                return 1.0d;
            case 1:
                return 2.0d;
            case 2:
                return 4.0d;
            case 3:
                return 8.0d;
            case 4:
                return 16.0d;
            case 5:
                return 32.0d;
            case 6:
                return 64.0d;
            case 7:
                return 128.0d;
            case 8:
                return 256.0d;
            case 9:
                return 512.0d;
            case 10:
                return 1024.0d;
            case 11:
                return 2048.0d;
            case 12:
                return 4096.0d;
            case 13:
                return 8192.0d;
            case 14:
                return 16384.0d;
            case 15:
                return 32768.0d;
            case 16:
                return 65536.0d;
            default:
                return Math.pow(2.0d, (double) i);
        }
    }

    public static boolean toBoolean(Boolean bool) {
        return toBoolean(bool, false);
    }

    public static boolean toBoolean(Boolean bool, boolean z) {
        return bool != null ? bool.booleanValue() : z;
    }
}
