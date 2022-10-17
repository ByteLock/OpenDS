package com.qualcomm.robotcore.hardware.usb;

import androidx.core.view.MotionEventCompat;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.Misc;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;

public interface RobotUsbDevice {

    public enum Channel {
        RX,
        TX,
        NONE,
        BOTH
    }

    void close();

    boolean getDebugRetainBuffers();

    DeviceManager.UsbDeviceType getDeviceType();

    FirmwareVersion getFirmwareVersion();

    String getProductName();

    SerialNumber getSerialNumber();

    USBIdentifiers getUsbIdentifiers();

    boolean isAttached();

    boolean isOpen();

    void logRetainedBuffers(long j, long j2, String str, String str2, Object... objArr);

    boolean mightBeAtUsbPacketStart();

    int read(byte[] bArr, int i, int i2, long j, TimeWindow timeWindow) throws RobotUsbException, InterruptedException;

    void requestReadInterrupt(boolean z);

    void resetAndFlushBuffers() throws RobotUsbException;

    void setBaudRate(int i) throws RobotUsbException;

    void setBreak(boolean z) throws RobotUsbException;

    void setDataCharacteristics(byte b, byte b2, byte b3) throws RobotUsbException;

    void setDebugRetainBuffers(boolean z);

    void setDeviceType(DeviceManager.UsbDeviceType usbDeviceType);

    void setFirmwareVersion(FirmwareVersion firmwareVersion);

    void setLatencyTimer(int i) throws RobotUsbException;

    void skipToLikelyUsbPacketStart();

    void write(byte[] bArr) throws InterruptedException, RobotUsbException;

    public static class FirmwareVersion {
        public int majorVersion;
        public int minorVersion;

        public FirmwareVersion(int i, int i2) {
            this.majorVersion = i;
            this.minorVersion = i2;
        }

        public FirmwareVersion(int i) {
            this.majorVersion = (i >> 4) & 15;
            this.minorVersion = (i >> 0) & 15;
        }

        public FirmwareVersion() {
            this(0, 0);
        }

        public String toString() {
            return Misc.formatInvariant("v%d.%d", Integer.valueOf(this.majorVersion), Integer.valueOf(this.minorVersion));
        }
    }

    public static class USBIdentifiers {
        private static final Set<Integer> bcdDevicesLynx = new HashSet(Arrays.asList(new Integer[]{4096}));
        private static final Set<Integer> productIdsLynx = new HashSet(Arrays.asList(new Integer[]{24597}));
        private static final int vendorIdFTDI = 1027;
        public int bcdDevice;
        public int productId;
        public int vendorId;

        public boolean isLynxDevice() {
            return this.vendorId == 1027 && productIdsLynx.contains(Integer.valueOf(this.productId)) && bcdDevicesLynx.contains(Integer.valueOf(this.bcdDevice & MotionEventCompat.ACTION_POINTER_INDEX_MASK));
        }

        public static USBIdentifiers createLynxIdentifiers() {
            USBIdentifiers uSBIdentifiers = new USBIdentifiers();
            uSBIdentifiers.vendorId = 1027;
            uSBIdentifiers.productId = ((Integer) first(productIdsLynx)).intValue();
            uSBIdentifiers.bcdDevice = ((Integer) first(bcdDevicesLynx)).intValue();
            Assert.assertTrue(uSBIdentifiers.isLynxDevice());
            return uSBIdentifiers;
        }

        protected static <T> T first(Set<T> set) {
            Iterator<T> it = set.iterator();
            if (it.hasNext()) {
                return it.next();
            }
            return null;
        }
    }
}
