package com.qualcomm.robotcore.util;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class ReadWriteFile {
    private static final String OLD_VERSION_SUFFIX = ".old_version";
    public static final String TAG = "ReadWriteFile";
    protected static Charset charset = Charset.forName("UTF-8");

    /* JADX INFO: finally extract failed */
    public static String readFileOrThrow(File file) throws IOException {
        FileInputStream fileInputStream = new FileInputStream(file);
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(fileInputStream.available());
        try {
            AppUtil.getInstance().copyStream((InputStream) fileInputStream, (OutputStream) byteArrayOutputStream);
            fileInputStream.close();
            return charset.decode(ByteBuffer.wrap(byteArrayOutputStream.toByteArray())).toString();
        } catch (Throwable th) {
            fileInputStream.close();
            throw th;
        }
    }

    public static String readFile(File file) {
        try {
            return readFileOrThrow(file);
        } catch (IOException e) {
            RobotLog.m51ee(TAG, e, "error reading file: %s", file.getPath());
            return InspectionState.NO_VERSION;
        }
    }

    public static byte[] readBytes(RobotCoreCommandList.FWImage fWImage) {
        if (fWImage.isAsset) {
            return readAssetBytes(fWImage.file);
        }
        return readFileBytes(fWImage.file);
    }

    public static byte[] readAssetBytes(File file) {
        try {
            return readAssetBytesOrThrow(file);
        } catch (IOException e) {
            RobotLog.m51ee(TAG, e, "error reading asset: %s", file.getPath());
            return new byte[0];
        }
    }

    public static byte[] readFileBytes(File file) {
        try {
            return readFileBytesOrThrow(file);
        } catch (IOException e) {
            RobotLog.m51ee(TAG, e, "error reading file: %s", file.getPath());
            return new byte[0];
        }
    }

    public static byte[] readAssetBytesOrThrow(File file) throws IOException {
        return readBytesOrThrow(0, AppUtil.getDefContext().getAssets().open(file.getPath()));
    }

    public static byte[] readRawResourceBytesOrThrow(int i) throws IOException {
        return readBytesOrThrow(0, AppUtil.getDefContext().getResources().openRawResource(i));
    }

    public static byte[] readFileBytesOrThrow(File file) throws IOException {
        return readBytesOrThrow((int) file.length(), new FileInputStream(file));
    }

    /* JADX INFO: finally extract failed */
    protected static byte[] readBytesOrThrow(int i, InputStream inputStream) throws IOException {
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream(i);
        byte[] bArr = new byte[1000];
        while (inputStream.read(bArr) != -1) {
            try {
                byteArrayOutputStream.write(bArr);
            } catch (Throwable th) {
                inputStream.close();
                throw th;
            }
        }
        inputStream.close();
        return byteArrayOutputStream.toByteArray();
    }

    public static void writeFile(File file, String str) {
        writeFile(file.getParentFile(), file.getName(), str);
    }

    public static void writeFileOrThrow(File file, String str) throws IOException {
        writeFileOrThrow(file.getParentFile(), file.getName(), str);
    }

    public static void writeFileOrThrow(File file, String str, String str2) throws IOException {
        AppUtil.getInstance().ensureDirectoryExists(file);
        ByteBuffer encode = charset.encode(str2);
        FileOutputStream fileOutputStream = new FileOutputStream(new File(file, str));
        try {
            fileOutputStream.write(encode.array(), 0, encode.limit());
            fileOutputStream.flush();
        } finally {
            fileOutputStream.getFD().sync();
            fileOutputStream.close();
        }
    }

    public static void writeFile(File file, String str, String str2) {
        try {
            writeFileOrThrow(file, str, str2);
        } catch (IOException e) {
            RobotLog.m51ee(TAG, e, "error writing file: %s", new File(file, str).getPath());
        }
    }

    public static void ensureAllChangesAreCommitted(File file) {
        try {
            Util.forEachInFolder(file, true, new Predicate<File>() {
                public boolean test(File file) {
                    if (!file.exists()) {
                        return true;
                    }
                    String absolutePath = file.getAbsolutePath();
                    if (absolutePath.endsWith(ReadWriteFile.OLD_VERSION_SUFFIX)) {
                        file.renameTo(new File(absolutePath.substring(0, absolutePath.length() - 12)));
                        return true;
                    }
                    ReadWriteFile.ensureChangesAreCommitted(file);
                    return true;
                }
            });
        } catch (FileNotFoundException e) {
            throw new RuntimeException("failed to ensure all changes have been written", e);
        }
    }

    public static void ensureChangesAreCommitted(File file) {
        File file2 = new File(file.getAbsolutePath() + OLD_VERSION_SUFFIX);
        if (file2.exists()) {
            file2.renameTo(file);
        }
    }

    public static void updateFileRequiringCommit(File file, String str) {
        ensureChangesAreCommitted(file);
        if (!file.exists()) {
            writeFile(file, str);
            return;
        }
        File file2 = new File(file.getAbsolutePath() + OLD_VERSION_SUFFIX);
        file.renameTo(file2);
        try {
            writeFileOrThrow(file, str);
            file2.delete();
        } catch (IOException e) {
            RobotLog.m51ee(TAG, e, "error writing file: %s", file.getPath());
        }
    }
}
