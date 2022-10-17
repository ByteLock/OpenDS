package com.qualcomm.robotcore.util;

import android.util.Log;

public class ShortHash {
    private static final String DEFAULT_ALPHABET = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ1234567890";
    private static final int DEFAULT_MIN_HASH_LENGTH = 0;
    private static final String DEFAULT_SALT = "";
    private static final String DEFAULT_SEPS = "cfhistuCFHISTU";
    private static final int GUARD_DIV = 12;
    public static final long MAX_NUMBER = 9007199254740992L;
    private static final int MIN_ALPHABET_LENGTH = 16;
    private static final double SEP_DIV = 3.5d;
    private static final String TAG = "ShortHash";
    private final String alphabet;
    private final String guards;
    private final int minHashLength;
    private final String salt;
    private final String seps;

    public String getVersion() {
        return "1.0.0";
    }

    public ShortHash() {
        this("");
    }

    public ShortHash(String str) {
        this(str, 0);
    }

    public ShortHash(String str, int i) {
        this(str, i, DEFAULT_ALPHABET);
    }

    public ShortHash(String str, int i, String str2) {
        String str3;
        this.salt = str == null ? "" : str;
        this.minHashLength = i <= 0 ? 0 : i;
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < str2.length(); i2++) {
            if (sb.indexOf(String.valueOf(str2.charAt(i2))) == -1) {
                sb.append(str2.charAt(i2));
            }
        }
        String sb2 = sb.toString();
        if (sb2.length() < 16) {
            throw new IllegalArgumentException("alphabet must contain at least 16 unique characters");
        } else if (!sb2.contains(" ")) {
            String str4 = DEFAULT_SEPS;
            for (int i3 = 0; i3 < str4.length(); i3++) {
                int indexOf = sb2.indexOf(str4.charAt(i3));
                if (indexOf == -1) {
                    str4 = str4.substring(0, i3) + " " + str4.substring(i3 + 1);
                } else {
                    sb2 = sb2.substring(0, indexOf) + " " + sb2.substring(indexOf + 1);
                }
            }
            String replaceAll = sb2.replaceAll("\\s+", "");
            String consistentShuffle = consistentShuffle(str4.replaceAll("\\s+", ""), this.salt);
            if (consistentShuffle.isEmpty() || ((double) (((float) replaceAll.length()) / ((float) consistentShuffle.length()))) > SEP_DIV) {
                int ceil = (int) Math.ceil(((double) replaceAll.length()) / SEP_DIV);
                ceil = ceil == 1 ? ceil + 1 : ceil;
                if (ceil > consistentShuffle.length()) {
                    int length = ceil - consistentShuffle.length();
                    consistentShuffle = consistentShuffle + replaceAll.substring(0, length);
                    replaceAll = replaceAll.substring(length);
                } else {
                    consistentShuffle = consistentShuffle.substring(0, ceil);
                }
            }
            String consistentShuffle2 = consistentShuffle(replaceAll, this.salt);
            int ceil2 = (int) Math.ceil(((double) consistentShuffle2.length()) / 12.0d);
            if (consistentShuffle2.length() < 3) {
                str3 = consistentShuffle.substring(0, ceil2);
                consistentShuffle = consistentShuffle.substring(ceil2);
            } else {
                str3 = consistentShuffle2.substring(0, ceil2);
                consistentShuffle2 = consistentShuffle2.substring(ceil2);
            }
            this.guards = str3;
            this.alphabet = consistentShuffle2;
            this.seps = consistentShuffle;
        } else {
            throw new IllegalArgumentException("alphabet cannot contains spaces");
        }
    }

    public String encode(long j) {
        return _encode(j);
    }

    public int getAlphabetLength() {
        return this.alphabet.length();
    }

    private String _encode(long j) {
        String str = this.alphabet;
        return hash(j, consistentShuffle(str, (this.salt + str).substring(0, str.length())));
    }

    private static String consistentShuffle(String str, String str2) {
        if (str2.length() <= 0) {
            return str;
        }
        char[] charArray = str.toCharArray();
        int length = charArray.length - 1;
        int i = 0;
        int i2 = 0;
        while (length > 0) {
            int length2 = i % str2.length();
            char charAt = str2.charAt(length2);
            i2 += charAt;
            int i3 = ((charAt + length2) + i2) % length;
            char c = charArray[i3];
            charArray[i3] = charArray[length];
            charArray[length] = c;
            length--;
            i = length2 + 1;
        }
        return new String(charArray);
    }

    private static String hash(long j, String str) {
        int length = str.length();
        Log.i(TAG, "Alphabet length " + length);
        String str2 = "";
        do {
            long j2 = (long) length;
            int i = (int) (j % j2);
            if (i >= 0 && i < str.length()) {
                str2 = str.charAt(i) + str2;
            }
            j /= j2;
        } while (j > 0);
        return str2;
    }
}
