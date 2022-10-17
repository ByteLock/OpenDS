package com.qualcomm.robotcore.util;

public class Range {
    public static byte clip(byte b, byte b2, byte b3) {
        return b < b2 ? b2 : b > b3 ? b3 : b;
    }

    public static double clip(double d, double d2, double d3) {
        return d < d2 ? d2 : d > d3 ? d3 : d;
    }

    public static float clip(float f, float f2, float f3) {
        return f < f2 ? f2 : f > f3 ? f3 : f;
    }

    public static int clip(int i, int i2, int i3) {
        return i < i2 ? i2 : i > i3 ? i3 : i;
    }

    public static short clip(short s, short s2, short s3) {
        return s < s2 ? s2 : s > s3 ? s3 : s;
    }

    public static double scale(double d, double d2, double d3, double d4, double d5) {
        double d6 = d4 - d5;
        double d7 = d2 - d3;
        return ((d6 / d7) * d) + (d4 - ((d2 * d6) / d7));
    }

    private Range() {
    }

    public static void throwIfRangeIsInvalid(double d, double d2, double d3) throws IllegalArgumentException {
        if (d < d2 || d > d3) {
            throw new IllegalArgumentException(String.format("number %f is invalid; valid ranges are %f..%f", new Object[]{Double.valueOf(d), Double.valueOf(d2), Double.valueOf(d3)}));
        }
    }

    public static void throwIfRangeIsInvalid(int i, int i2, int i3) throws IllegalArgumentException {
        if (i < i2 || i > i3) {
            throw new IllegalArgumentException(String.format("number %d is invalid; valid ranges are %d..%d", new Object[]{Integer.valueOf(i), Integer.valueOf(i2), Integer.valueOf(i3)}));
        }
    }
}
