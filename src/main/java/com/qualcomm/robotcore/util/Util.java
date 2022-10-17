package com.qualcomm.robotcore.util;

import android.widget.TextView;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Random;
import org.firstinspires.ftc.robotcore.external.Predicate;

public class Util {
    public static String ASCII_RECORD_SEPARATOR = "\u001e";
    public static final String LOWERCASE_ALPHA_NUM_CHARACTERS = "0123456789qwertyuiopasdfghjklzxcvbnm";

    public static String getRandomString(int i, String str) {
        Random random = new Random();
        StringBuilder sb = new StringBuilder();
        for (int i2 = 0; i2 < i; i2++) {
            sb.append(str.charAt(random.nextInt(str.length())));
        }
        return sb.toString();
    }

    public static void sortFilesByName(File[] fileArr) {
        Arrays.sort(fileArr, new Comparator<File>() {
            public int compare(File file, File file2) {
                return file.getName().compareTo(file2.getName());
            }
        });
    }

    public static void updateTextView(final TextView textView, final String str) {
        if (textView != null) {
            textView.post(new Runnable() {
                public void run() {
                    textView.setText(str);
                }
            });
        }
    }

    public static byte[] concatenateByteArrays(byte[] bArr, byte[] bArr2) {
        byte[] bArr3 = new byte[(bArr.length + bArr2.length)];
        System.arraycopy(bArr, 0, bArr3, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr3, bArr.length, bArr2.length);
        return bArr3;
    }

    public static byte[] concatenateByteArrays(byte[] bArr, byte[] bArr2, byte[] bArr3) {
        byte[] bArr4 = new byte[(bArr.length + bArr2.length + bArr3.length)];
        System.arraycopy(bArr, 0, bArr4, 0, bArr.length);
        System.arraycopy(bArr2, 0, bArr4, bArr.length, bArr2.length);
        System.arraycopy(bArr3, 0, bArr4, bArr.length + bArr2.length, bArr3.length);
        return bArr4;
    }

    public static boolean isPrefixOf(String str, String str2) {
        if (str == null) {
            return true;
        }
        if (str2 == null || str.length() > str2.length()) {
            return false;
        }
        for (int i = 0; i < str.length(); i++) {
            if (str.charAt(i) != str2.charAt(i)) {
                return false;
            }
        }
        return true;
    }

    public static boolean isGoodString(String str) {
        if (str == null || !str.trim().equals(str) || str.length() == 0) {
            return false;
        }
        return true;
    }

    public static void forEachInFolder(File file, boolean z, Predicate<File> predicate) throws FileNotFoundException {
        if (file.isDirectory()) {
            for (File file2 : file.listFiles()) {
                if (z && file2.isDirectory()) {
                    forEachInFolder(file2, true, predicate);
                }
                predicate.test(file2);
            }
            return;
        }
        throw new FileNotFoundException("not a directory");
    }
}
