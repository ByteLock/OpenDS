package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.robotcore.util.TypeConversion;
import java.util.Arrays;
import java.util.concurrent.atomic.AtomicReference;
import org.firstinspires.ftc.robotcore.internal.ftdi.FtConstants;

public abstract class ModernRoboticsDatagram {
    public static final int CB_HEADER = 5;
    public static final int IB_ADDRESS = 3;
    public static final int IB_FUNCTION = 2;
    public static final int IB_LENGTH = 4;
    public static final int IB_SYNC_0 = 0;
    public static final int IB_SYNC_1 = 1;
    public byte[] data;

    protected ModernRoboticsDatagram(int i) {
        this.data = new byte[(i + 5)];
    }

    /* access modifiers changed from: protected */
    public void initialize(int i, int i2) {
        byte[] bArr = this.data;
        bArr[0] = (byte) i;
        bArr[1] = (byte) i2;
        bArr[2] = 0;
        bArr[3] = 0;
        bArr[4] = 0;
    }

    public void clearPayload() {
        byte[] bArr = this.data;
        Arrays.fill(bArr, 5, bArr.length, (byte) 0);
    }

    public static class AllocationContext<DATAGRAM_TYPE extends ModernRoboticsDatagram> {
        protected AtomicReference<DATAGRAM_TYPE> cacheHeaderOnly0 = new AtomicReference<>((Object) null);
        protected AtomicReference<DATAGRAM_TYPE> cacheHeaderOnly1 = new AtomicReference<>((Object) null);
        protected AtomicReference<DATAGRAM_TYPE> cachedFullInstance0 = new AtomicReference<>((Object) null);
        protected AtomicReference<DATAGRAM_TYPE> cachedFullInstance1 = new AtomicReference<>((Object) null);

        /* access modifiers changed from: package-private */
        public DATAGRAM_TYPE tryAlloc(int i) {
            if (i == 0) {
                DATAGRAM_TYPE datagram_type = (ModernRoboticsDatagram) this.cacheHeaderOnly0.getAndSet((Object) null);
                return datagram_type == null ? (ModernRoboticsDatagram) this.cacheHeaderOnly1.getAndSet((Object) null) : datagram_type;
            }
            DATAGRAM_TYPE datagram_type2 = (ModernRoboticsDatagram) this.cachedFullInstance0.getAndSet((Object) null);
            if (datagram_type2 != null) {
                if (datagram_type2.getAllocatedPayload() == i) {
                    return datagram_type2;
                }
                tryCache0(datagram_type2);
            }
            DATAGRAM_TYPE datagram_type3 = (ModernRoboticsDatagram) this.cachedFullInstance1.getAndSet((Object) null);
            if (datagram_type3 != null) {
                if (datagram_type3.getAllocatedPayload() == i) {
                    return datagram_type3;
                }
                tryCache1(datagram_type3);
            }
            return null;
        }

        /* access modifiers changed from: package-private */
        public void tryCache0(DATAGRAM_TYPE datagram_type) {
            if (datagram_type.getAllocatedPayload() == 0) {
                if (!this.cacheHeaderOnly0.compareAndSet((Object) null, datagram_type)) {
                    this.cacheHeaderOnly1.compareAndSet((Object) null, datagram_type);
                }
            } else if (!this.cachedFullInstance0.compareAndSet((Object) null, datagram_type)) {
                this.cachedFullInstance1.compareAndSet((Object) null, datagram_type);
            }
        }

        /* access modifiers changed from: package-private */
        public void tryCache1(DATAGRAM_TYPE datagram_type) {
            if (datagram_type.getAllocatedPayload() == 0) {
                if (!this.cacheHeaderOnly1.compareAndSet((Object) null, datagram_type)) {
                    this.cacheHeaderOnly0.compareAndSet((Object) null, datagram_type);
                }
            } else if (!this.cachedFullInstance1.compareAndSet((Object) null, datagram_type)) {
                this.cachedFullInstance0.compareAndSet((Object) null, datagram_type);
            }
        }
    }

    public int getAllocatedPayload() {
        return this.data.length - 5;
    }

    public boolean isRead() {
        return (this.data[2] & FtConstants.DCD) != 0;
    }

    public boolean isWrite() {
        return !isRead();
    }

    public void setRead(int i) {
        this.data[2] = (byte) ((i & 127) | 128);
    }

    public void setWrite(int i) {
        this.data[2] = (byte) (i & 127);
    }

    public void setRead() {
        setRead(getFunction());
    }

    public void setWrite() {
        setWrite(getFunction());
    }

    public int getFunction() {
        return this.data[2] & Byte.MAX_VALUE;
    }

    public void setFunction(int i) {
        byte[] bArr = this.data;
        bArr[2] = (byte) ((i & Byte.MAX_VALUE) | (bArr[2] & FtConstants.DCD));
    }

    public int getAddress() {
        return TypeConversion.unsignedByteToInt(this.data[3]);
    }

    public void setAddress(int i) {
        if (i < 0 || i > 255) {
            throw new IllegalArgumentException(String.format("address=%d; must be unsigned byte", new Object[]{Integer.valueOf(i)}));
        } else {
            this.data[3] = (byte) i;
        }
    }

    public void setPayload(byte[] bArr) {
        setPayloadLength(bArr.length);
        System.arraycopy(bArr, 0, this.data, 5, bArr.length);
    }

    public int getPayloadLength() {
        return TypeConversion.unsignedByteToInt(this.data[4]);
    }

    public void setPayloadLength(int i) {
        if (i < 0 || i > 255) {
            throw new IllegalArgumentException(String.format("length=%d; must be unsigned byte", new Object[]{Integer.valueOf(i)}));
        } else {
            this.data[4] = (byte) i;
        }
    }

    public boolean isFailure() {
        byte[] bArr = this.data;
        return bArr[2] == -1 && bArr[3] == -1;
    }
}
