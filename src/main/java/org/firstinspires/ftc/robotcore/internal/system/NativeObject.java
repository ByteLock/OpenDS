package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteOrder;
import org.firstinspires.ftc.robotcore.internal.system.RefCounted;

public class NativeObject<ParentType extends RefCounted> extends DestructOnFinalize<ParentType> {
    protected ByteOrder byteOrder;
    protected MemoryAllocator memoryAllocator;
    /* access modifiers changed from: protected */
    public long pointer;

    public enum MemoryAllocator {
        UNKNOWN,
        MALLOC,
        EXTERNAL
    }

    protected static native long nativeAllocMemory(long j);

    protected static native void nativeFreeMemory(long j);

    protected static native byte[] nativeGetBytes(long j, int i, int i2);

    protected static native long[] nativeGetLinkedList(long j, int i);

    protected static native long[] nativeGetNullTerminatedList(long j, int i, int i2);

    protected static native long nativeGetPointer(long j, int i);

    protected static native String nativeGetString(long j, int i);

    protected static native void nativeSetBytes(long j, int i, byte[] bArr);

    protected static native void nativeSetPointer(long j, int i, long j2);

    protected NativeObject(long j, MemoryAllocator memoryAllocator2) {
        this(j, memoryAllocator2, defaultTraceLevel);
    }

    protected NativeObject(long j, MemoryAllocator memoryAllocator2, RefCounted.TraceLevel traceLevel) {
        super(RefCounted.TraceLevel.None);
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        this.traceLevel = traceLevel;
        if (j != 0) {
            this.pointer = j;
            this.memoryAllocator = memoryAllocator2;
            if (traceCtor()) {
                RobotLog.m61vv(getTag(), "construct(%s)", getTraceIdentifier());
                return;
            }
            return;
        }
        throw new IllegalArgumentException("pointer must not be null");
    }

    protected NativeObject(long j) {
        this(j, MemoryAllocator.UNKNOWN);
    }

    protected NativeObject(long j, RefCounted.TraceLevel traceLevel) {
        this(j, MemoryAllocator.UNKNOWN, traceLevel);
    }

    protected NativeObject() {
        this(defaultTraceLevel);
    }

    protected NativeObject(RefCounted.TraceLevel traceLevel) {
        super(traceLevel);
        this.byteOrder = ByteOrder.LITTLE_ENDIAN;
        this.pointer = 0;
        this.memoryAllocator = MemoryAllocator.UNKNOWN;
    }

    public String getTraceIdentifier() {
        return Misc.formatInvariant("pointer=0x%08x", Long.valueOf(this.pointer));
    }

    /* access modifiers changed from: protected */
    public void destructor() {
        freeMemory();
        super.destructor();
    }

    protected static long checkAlloc(long j) {
        if (j != 0) {
            return j;
        }
        throw new OutOfMemoryError();
    }

    public void allocateMemory(long j) {
        synchronized (this.lock) {
            freeMemory();
            int i = (j > 0 ? 1 : (j == 0 ? 0 : -1));
            if (i > 0) {
                this.pointer = checkAlloc(nativeAllocMemory(j));
                setMemoryAllocator(MemoryAllocator.MALLOC);
            } else if (i < 0) {
                throw new IllegalArgumentException("cbAlloc must be >= 0");
            }
        }
    }

    public void freeMemory() {
        synchronized (this.lock) {
            if (this.memoryAllocator == MemoryAllocator.MALLOC) {
                nativeFreeMemory(this.pointer);
                clearPointer();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void clearPointer() {
        synchronized (this.lock) {
            this.pointer = 0;
        }
    }

    /* access modifiers changed from: protected */
    public void setMemoryAllocator(MemoryAllocator memoryAllocator2) {
        synchronized (this.lock) {
            this.memoryAllocator = memoryAllocator2;
        }
    }

    /* access modifiers changed from: protected */
    public byte getByte(int i) {
        return nativeGetBytes(this.pointer, i, 1)[0];
    }

    /* access modifiers changed from: protected */
    public int getUByte(int i) {
        return TypeConversion.unsignedByteToInt(getByte(i));
    }

    /* access modifiers changed from: protected */
    public short getShort(int i) {
        return TypeConversion.byteArrayToShort(nativeGetBytes(this.pointer, i, 2), this.byteOrder);
    }

    /* access modifiers changed from: protected */
    public int getUShort(int i) {
        return TypeConversion.unsignedShortToInt(getShort(i));
    }

    /* access modifiers changed from: protected */
    public int getInt(int i) {
        return TypeConversion.byteArrayToInt(nativeGetBytes(this.pointer, i, 4), this.byteOrder);
    }

    /* access modifiers changed from: protected */
    public void setInt(int i, int i2) {
        nativeSetBytes(this.pointer, i, TypeConversion.intToByteArray(i2, this.byteOrder));
    }

    /* access modifiers changed from: protected */
    public long getUInt(int i) {
        return TypeConversion.unsignedIntToLong(getInt(i));
    }

    /* access modifiers changed from: protected */
    public void setUInt(int i, long j) {
        setInt(i, (int) j);
    }

    /* access modifiers changed from: protected */
    public int getSizet(int i) {
        return getInt(i);
    }

    /* access modifiers changed from: protected */
    public long getLong(int i) {
        return TypeConversion.byteArrayToLong(nativeGetBytes(this.pointer, i, 8), this.byteOrder);
    }

    /* access modifiers changed from: protected */
    public void setLong(int i, long j) {
        nativeSetBytes(this.pointer, i, TypeConversion.longToByteArray(j, this.byteOrder));
    }

    /* access modifiers changed from: protected */
    public String getString(int i) {
        return nativeGetString(this.pointer, i);
    }

    /* access modifiers changed from: protected */
    public byte[] getBytes(int i, int i2) {
        return nativeGetBytes(this.pointer, i, i2);
    }

    /* access modifiers changed from: protected */
    public void setBytes(int i, byte[] bArr) {
        nativeSetBytes(this.pointer, i, bArr);
    }

    /* access modifiers changed from: protected */
    public long getPointer(int i) {
        return nativeGetPointer(this.pointer, i);
    }

    /* access modifiers changed from: protected */
    public void setPointer(int i, long j) {
        nativeSetPointer(this.pointer, i, j);
    }

    static {
        System.loadLibrary(RobotLog.TAG);
    }
}
