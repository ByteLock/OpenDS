package com.qualcomm.robotcore.hardware.usb;

import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import org.firstinspires.ftc.robotcore.system.Assert;

public abstract class RobotUsbDeviceImplBase implements RobotUsbDevice {
    protected static final ConcurrentHashMap<SerialNumber, DeviceManager.UsbDeviceType> deviceTypes = new ConcurrentHashMap<>();
    protected static final ConcurrentHashMap<SerialNumber, RobotUsbDevice> extantDevices = new ConcurrentHashMap<>();
    protected DeviceManager.UsbDeviceType deviceType;
    protected RobotUsbDevice.FirmwareVersion firmwareVersion = new RobotUsbDevice.FirmwareVersion();
    protected final SerialNumber serialNumber;

    public abstract String getTag();

    protected RobotUsbDeviceImplBase(SerialNumber serialNumber2) {
        this.serialNumber = serialNumber2;
        DeviceManager.UsbDeviceType usbDeviceType = deviceTypes.get(serialNumber2);
        this.deviceType = usbDeviceType;
        if (usbDeviceType == null) {
            this.deviceType = DeviceManager.UsbDeviceType.UNKNOWN_DEVICE;
        }
        ConcurrentHashMap<SerialNumber, RobotUsbDevice> concurrentHashMap = extantDevices;
        Assert.assertFalse(concurrentHashMap.contains(serialNumber2));
        concurrentHashMap.put(serialNumber2, this);
    }

    /* access modifiers changed from: protected */
    public void removeFromExtantDevices() {
        extantDevices.remove(this.serialNumber);
    }

    public static Collection<RobotUsbDevice> getExtantDevices() {
        return extantDevices.values();
    }

    public static boolean isOpen(SerialNumber serialNumber2) {
        return extantDevices.containsKey(serialNumber2);
    }

    public static DeviceManager.UsbDeviceType getDeviceType(SerialNumber serialNumber2) {
        DeviceManager.UsbDeviceType usbDeviceType = deviceTypes.get(serialNumber2);
        return usbDeviceType == null ? DeviceManager.UsbDeviceType.UNKNOWN_DEVICE : usbDeviceType;
    }

    public synchronized void setDeviceType(DeviceManager.UsbDeviceType usbDeviceType) {
        this.deviceType = usbDeviceType;
        deviceTypes.put(this.serialNumber, usbDeviceType);
    }

    public synchronized DeviceManager.UsbDeviceType getDeviceType() {
        return this.deviceType;
    }

    public SerialNumber getSerialNumber() {
        return this.serialNumber;
    }

    public RobotUsbDevice.FirmwareVersion getFirmwareVersion() {
        return this.firmwareVersion;
    }

    public void setFirmwareVersion(RobotUsbDevice.FirmwareVersion firmwareVersion2) {
        this.firmwareVersion = firmwareVersion2;
    }

    /* access modifiers changed from: protected */
    public void dumpBytesReceived(byte[] bArr, int i, int i2) {
        RobotLog.logBytes(getTag(), "received", bArr, i, i2);
    }

    /* access modifiers changed from: protected */
    public void dumpBytesSent(byte[] bArr) {
        RobotLog.logBytes(getTag(), "sent", bArr, bArr.length);
    }
}
