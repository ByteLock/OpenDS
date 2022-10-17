package org.firstinspires.ftc.robotcore.internal.system;

import android.graphics.Bitmap;
import android.graphics.Color;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteOrder;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderGetStatusResponse;

public class BmpFileWriter {
    protected static final int BI_BITFIELDS = 3;
    protected static boolean useNativeCopyPixels = true;
    protected final Bitmap bitmap;
    protected final int cbDibHeader;
    protected final int cbFile;
    protected final int cbFileHeader;
    protected final int cbImage;
    protected final int cbPadding;
    protected final int cbPerPixel;
    protected final int cbPerRow;
    protected final int cbRowMultiple;
    protected final int dibImageData;
    protected final int height;
    protected final byte[] rgbPadding;
    protected final int width;

    protected static native void nativeCopyPixelsRGBA(int i, int i2, int i3, Bitmap bitmap2, long j, int i4);

    public BmpFileWriter(Bitmap bitmap2) {
        if (bitmap2.getConfig() == Bitmap.Config.ARGB_8888) {
            this.bitmap = bitmap2;
            this.cbRowMultiple = 4;
            this.cbPerPixel = 4;
            int width2 = bitmap2.getWidth();
            this.width = width2;
            int height2 = bitmap2.getHeight();
            this.height = height2;
            int i = width2 * 4;
            this.cbPerRow = i;
            int i2 = i % 4 == 0 ? 0 : 4 - (i % 4);
            this.cbPadding = i2;
            byte[] bArr = new byte[i2];
            this.rgbPadding = bArr;
            int length = (i + bArr.length) * height2;
            this.cbImage = length;
            this.cbFileHeader = 14;
            this.cbDibHeader = 56;
            int i3 = 14 + 56;
            this.dibImageData = i3;
            this.cbFile = i3 + length;
            return;
        }
        throw new IllegalArgumentException("unsupported bitmap format");
    }

    public int getSize() {
        return this.cbFile;
    }

    public void save(File file) throws IOException {
        RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
        randomAccessFile.setLength((long) this.cbFile);
        FileChannel channel = randomAccessFile.getChannel();
        save(channel.map(FileChannel.MapMode.READ_WRITE, 0, (long) this.cbFile));
        channel.close();
        randomAccessFile.close();
    }

    protected static byte[] getByteMask(int i) {
        byte[] bArr = new byte[4];
        bArr[i] = -1;
        return bArr;
    }

    /* access modifiers changed from: protected */
    public byte[] getRedMask() {
        return getByteMask(0);
    }

    /* access modifiers changed from: protected */
    public byte[] getGreenMask() {
        return getByteMask(1);
    }

    /* access modifiers changed from: protected */
    public byte[] getBlueMask() {
        return getByteMask(2);
    }

    /* access modifiers changed from: protected */
    public byte[] getAlphaMask() {
        return getByteMask(3);
    }

    public void save(MappedByteBuffer mappedByteBuffer) {
        mappedByteBuffer.order(ByteOrder.LITTLE_ENDIAN);
        mappedByteBuffer.put(FlashLoaderGetStatusResponse.STATUS_INVALID_CMD);
        mappedByteBuffer.put((byte) 77);
        mappedByteBuffer.putInt(this.cbFile);
        mappedByteBuffer.putShort(0);
        mappedByteBuffer.putShort(0);
        mappedByteBuffer.putInt(this.dibImageData);
        Assert.assertTrue(mappedByteBuffer.position() == this.cbFileHeader);
        mappedByteBuffer.putInt(this.cbDibHeader);
        mappedByteBuffer.putInt(this.width);
        mappedByteBuffer.putInt(this.height);
        mappedByteBuffer.putShort(1);
        mappedByteBuffer.putShort((short) (this.cbPerPixel * 8));
        mappedByteBuffer.putInt(3);
        mappedByteBuffer.putInt(this.cbImage);
        mappedByteBuffer.putInt(0);
        mappedByteBuffer.putInt(0);
        mappedByteBuffer.putInt(0);
        mappedByteBuffer.putInt(0);
        mappedByteBuffer.put(getRedMask());
        mappedByteBuffer.put(getGreenMask());
        mappedByteBuffer.put(getBlueMask());
        mappedByteBuffer.put(getAlphaMask());
        Assert.assertTrue(mappedByteBuffer.position() == this.dibImageData);
        if (!useNativeCopyPixels) {
            int i = this.width;
            int i2 = this.height;
            int[] iArr = new int[(i * i2)];
            this.bitmap.getPixels(iArr, 0, i, 0, 0, i, i2);
            for (int i3 = this.height - 1; i3 >= 0; i3--) {
                int i4 = this.width * i3;
                int i5 = 0;
                while (i5 < this.width) {
                    int i6 = i4 + 1;
                    int i7 = iArr[i4];
                    mappedByteBuffer.put((byte) Color.red(i7));
                    mappedByteBuffer.put((byte) Color.green(i7));
                    mappedByteBuffer.put((byte) Color.blue(i7));
                    mappedByteBuffer.put((byte) Color.alpha(i7));
                    i5++;
                    i4 = i6;
                }
                mappedByteBuffer.put(this.rgbPadding);
            }
            return;
        }
        nativeCopyPixelsRGBA(this.width, this.height, this.cbPadding, this.bitmap, ClassUtil.memoryAddressFrom(mappedByteBuffer), mappedByteBuffer.position());
    }

    static {
        System.loadLibrary(RobotLog.TAG);
    }
}
