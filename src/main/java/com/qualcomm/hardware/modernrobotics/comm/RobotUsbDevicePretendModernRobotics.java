package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.internal.collections.CircularByteBuffer;
import org.firstinspires.ftc.robotcore.internal.collections.MarkedItemQueue;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.system.Misc;
import org.firstinspires.ftc.robotcore.internal.usb.UsbConstants;

public class RobotUsbDevicePretendModernRobotics implements RobotUsbDevice {
    protected CircularByteBuffer circularByteBuffer = new CircularByteBuffer(0);
    protected boolean debugRetainBuffers = false;
    protected DeviceManager.UsbDeviceType deviceType = DeviceManager.UsbDeviceType.FTDI_USB_UNKNOWN_DEVICE;
    protected RobotUsbDevice.FirmwareVersion firmwareVersion = new RobotUsbDevice.FirmwareVersion();
    protected boolean interruptRequested = false;
    protected MarkedItemQueue markedItemQueue = new MarkedItemQueue();
    protected ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> requestAllocationContext = new ModernRoboticsDatagram.AllocationContext<>();
    protected ModernRoboticsDatagram.AllocationContext<ModernRoboticsResponse> responseAllocationContext = new ModernRoboticsDatagram.AllocationContext<>();
    protected SerialNumber serialNumber;

    public void close() {
    }

    public boolean isAttached() {
        return true;
    }

    public boolean isOpen() {
        return true;
    }

    public void setBaudRate(int i) {
    }

    public void setBreak(boolean z) {
    }

    public void setDataCharacteristics(byte b, byte b2, byte b3) {
    }

    public void setLatencyTimer(int i) {
    }

    public RobotUsbDevicePretendModernRobotics(SerialNumber serialNumber2) {
        this.serialNumber = serialNumber2;
    }

    public SerialNumber getSerialNumber() {
        return this.serialNumber;
    }

    public String getProductName() {
        return Misc.formatForUser("pretend %s", this.deviceType);
    }

    public void setDeviceType(DeviceManager.UsbDeviceType usbDeviceType) {
        this.deviceType = usbDeviceType;
    }

    public DeviceManager.UsbDeviceType getDeviceType() {
        return this.deviceType;
    }

    public void setDebugRetainBuffers(boolean z) {
        this.debugRetainBuffers = z;
    }

    public boolean getDebugRetainBuffers() {
        return this.debugRetainBuffers;
    }

    public void logRetainedBuffers(long j, long j2, String str, String str2, Object... objArr) {
        RobotLog.m49ee(str, str2, objArr);
    }

    public void skipToLikelyUsbPacketStart() {
        this.circularByteBuffer.skip(this.markedItemQueue.removeUpToNextMarkedItemOrEnd());
    }

    public boolean mightBeAtUsbPacketStart() {
        return this.markedItemQueue.isAtMarkedItem() || this.markedItemQueue.isEmpty();
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(10:6|(1:8)(1:9)|10|11|12|13|14|15|(1:17)|(2:19|31)(1:32)) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:14:0x006a */
    /* JADX WARNING: Removed duplicated region for block: B:27:0x009d  */
    /* JADX WARNING: Removed duplicated region for block: B:29:0x00a2  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void write(byte[] r5) {
        /*
            r4 = this;
            r0 = 0
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest> r1 = r4.requestAllocationContext     // Catch:{ all -> 0x0099 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest r5 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest.from(r1, r5)     // Catch:{ all -> 0x0099 }
            int r1 = r5.getFunction()     // Catch:{ all -> 0x0097 }
            if (r1 != 0) goto L_0x007c
            boolean r1 = r5.isWrite()     // Catch:{ all -> 0x0097 }
            if (r1 == 0) goto L_0x0029
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse> r1 = r4.responseAllocationContext     // Catch:{ all -> 0x0097 }
            r2 = 0
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r0 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.newInstance(r1, r2)     // Catch:{ all -> 0x0097 }
            int r1 = r5.getFunction()     // Catch:{ all -> 0x0097 }
            r0.setWrite(r1)     // Catch:{ all -> 0x0097 }
            int r1 = r5.getAddress()     // Catch:{ all -> 0x0097 }
            r0.setAddress(r1)     // Catch:{ all -> 0x0097 }
            goto L_0x004b
        L_0x0029:
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse> r1 = r4.responseAllocationContext     // Catch:{ all -> 0x0097 }
            int r2 = r5.getPayloadLength()     // Catch:{ all -> 0x0097 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r0 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.newInstance(r1, r2)     // Catch:{ all -> 0x0097 }
            int r1 = r5.getFunction()     // Catch:{ all -> 0x0097 }
            r0.setRead(r1)     // Catch:{ all -> 0x0097 }
            int r1 = r5.getAddress()     // Catch:{ all -> 0x0097 }
            r0.setAddress(r1)     // Catch:{ all -> 0x0097 }
            int r1 = r5.getPayloadLength()     // Catch:{ all -> 0x0097 }
            r0.setPayloadLength(r1)     // Catch:{ all -> 0x0097 }
            r0.clearPayload()     // Catch:{ all -> 0x0097 }
        L_0x004b:
            org.firstinspires.ftc.robotcore.internal.collections.CircularByteBuffer r1 = r4.circularByteBuffer     // Catch:{ all -> 0x0097 }
            byte[] r2 = r0.data     // Catch:{ all -> 0x0097 }
            r1.write((byte[]) r2)     // Catch:{ all -> 0x0097 }
            org.firstinspires.ftc.robotcore.internal.collections.MarkedItemQueue r1 = r4.markedItemQueue     // Catch:{ all -> 0x0097 }
            r1.addMarkedItem()     // Catch:{ all -> 0x0097 }
            org.firstinspires.ftc.robotcore.internal.collections.MarkedItemQueue r1 = r4.markedItemQueue     // Catch:{ all -> 0x0097 }
            byte[] r2 = r0.data     // Catch:{ all -> 0x0097 }
            int r2 = r2.length     // Catch:{ all -> 0x0097 }
            int r2 = r2 + -1
            r1.addUnmarkedItems(r2)     // Catch:{ all -> 0x0097 }
            r1 = 3
            r3 = 500000(0x7a120, float:7.00649E-40)
            java.lang.Thread.sleep(r1, r3)     // Catch:{ InterruptedException -> 0x006a }
            goto L_0x0071
        L_0x006a:
            java.lang.Thread r1 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0097 }
            r1.interrupt()     // Catch:{ all -> 0x0097 }
        L_0x0071:
            if (r0 == 0) goto L_0x0076
            r0.close()
        L_0x0076:
            if (r5 == 0) goto L_0x007b
            r5.close()
        L_0x007b:
            return
        L_0x007c:
            java.lang.IllegalArgumentException r1 = new java.lang.IllegalArgumentException     // Catch:{ all -> 0x0097 }
            java.lang.StringBuilder r2 = new java.lang.StringBuilder     // Catch:{ all -> 0x0097 }
            r2.<init>()     // Catch:{ all -> 0x0097 }
            java.lang.String r3 = "undefined function: "
            r2.append(r3)     // Catch:{ all -> 0x0097 }
            int r3 = r5.getFunction()     // Catch:{ all -> 0x0097 }
            r2.append(r3)     // Catch:{ all -> 0x0097 }
            java.lang.String r2 = r2.toString()     // Catch:{ all -> 0x0097 }
            r1.<init>(r2)     // Catch:{ all -> 0x0097 }
            throw r1     // Catch:{ all -> 0x0097 }
        L_0x0097:
            r1 = move-exception
            goto L_0x009b
        L_0x0099:
            r1 = move-exception
            r5 = r0
        L_0x009b:
            if (r0 == 0) goto L_0x00a0
            r0.close()
        L_0x00a0:
            if (r5 == 0) goto L_0x00a5
            r5.close()
        L_0x00a5:
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.modernrobotics.comm.RobotUsbDevicePretendModernRobotics.write(byte[]):void");
    }

    public int read(byte[] bArr, int i, int i2, long j, TimeWindow timeWindow) {
        int read = this.circularByteBuffer.read(bArr, i, i2);
        this.markedItemQueue.removeItems(read);
        if (timeWindow != null) {
            timeWindow.clear();
        }
        return read;
    }

    public void resetAndFlushBuffers() {
        this.circularByteBuffer.clear();
        this.markedItemQueue.clear();
    }

    public RobotUsbDevice.FirmwareVersion getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(RobotUsbDevice.FirmwareVersion firmwareVersion2) {
        this.firmwareVersion = firmwareVersion2;
    }

    public void requestReadInterrupt(boolean z) {
        this.interruptRequested = z;
    }

    public RobotUsbDevice.USBIdentifiers getUsbIdentifiers() {
        RobotUsbDevice.USBIdentifiers uSBIdentifiers = new RobotUsbDevice.USBIdentifiers();
        uSBIdentifiers.vendorId = UsbConstants.VENDOR_ID_FTDI;
        uSBIdentifiers.productId = 0;
        uSBIdentifiers.bcdDevice = 0;
        return uSBIdentifiers;
    }
}
