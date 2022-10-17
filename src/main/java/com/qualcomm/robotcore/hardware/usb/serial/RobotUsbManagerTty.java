package com.qualcomm.robotcore.hardware.usb.serial;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.List;

public class RobotUsbManagerTty implements RobotUsbManager {
    public static final String TAG = "RobotUsbManagerTty";
    protected SerialNumber serialNumberEmbedded = LynxConstants.SERIAL_NUMBER_EMBEDDED;

    /* access modifiers changed from: package-private */
    public Object getLock() {
        return RobotUsbManagerTty.class;
    }

    public List<SerialNumber> scanForDevices() throws RobotCoreException {
        ArrayList arrayList = new ArrayList();
        arrayList.add(this.serialNumberEmbedded);
        return arrayList;
    }

    /* JADX WARNING: Can't wrap try/catch for region: R(9:7|8|9|10|11|12|13|14|15) */
    /* JADX WARNING: Missing exception handler attribute for start block: B:14:0x0050 */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.qualcomm.robotcore.hardware.usb.RobotUsbDevice openBySerialNumber(com.qualcomm.robotcore.util.SerialNumber r8) throws com.qualcomm.robotcore.exception.RobotCoreException {
        /*
            r7 = this;
            java.lang.Object r0 = r7.getLock()
            monitor-enter(r0)
            com.qualcomm.robotcore.util.SerialNumber r1 = r7.serialNumberEmbedded     // Catch:{ all -> 0x007f }
            boolean r1 = r1.equals((java.lang.Object) r8)     // Catch:{ all -> 0x007f }
            r2 = 0
            r3 = 1
            if (r1 == 0) goto L_0x0073
            boolean r1 = com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase.isOpen(r8)     // Catch:{ all -> 0x007f }
            if (r1 != 0) goto L_0x0067
            org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard r8 = org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard.getInstance()     // Catch:{ all -> 0x007f }
            java.io.File r8 = r8.getUartLocation()     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.hardware.usb.serial.SerialPort r1 = new com.qualcomm.robotcore.hardware.usb.serial.SerialPort     // Catch:{ IOException -> 0x0052 }
            r4 = 460800(0x70800, float:6.45718E-40)
            r1.<init>(r8, r4)     // Catch:{ IOException -> 0x0052 }
            com.qualcomm.robotcore.hardware.usb.serial.RobotUsbDeviceTty r5 = new com.qualcomm.robotcore.hardware.usb.serial.RobotUsbDeviceTty     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.util.SerialNumber r6 = r7.serialNumberEmbedded     // Catch:{ all -> 0x007f }
            r5.<init>(r1, r6, r8)     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice$FirmwareVersion r8 = new com.qualcomm.robotcore.hardware.usb.RobotUsbDevice$FirmwareVersion     // Catch:{ all -> 0x007f }
            r8.<init>(r3, r2)     // Catch:{ all -> 0x007f }
            r5.setFirmwareVersion(r8)     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r8 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.LYNX_USB_DEVICE     // Catch:{ all -> 0x007f }
            r5.setDeviceType(r8)     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice$USBIdentifiers r8 = com.qualcomm.robotcore.hardware.usb.RobotUsbDevice.USBIdentifiers.createLynxIdentifiers()     // Catch:{ all -> 0x007f }
            r5.setUsbIdentifiers(r8)     // Catch:{ all -> 0x007f }
            android.app.Application r8 = org.firstinspires.ftc.robotcore.internal.system.AppUtil.getDefContext()     // Catch:{ all -> 0x007f }
            int r1 = com.qualcomm.robotcore.C0705R.string.descriptionLynxEmbeddedModule     // Catch:{ all -> 0x007f }
            java.lang.String r8 = r8.getString(r1)     // Catch:{ all -> 0x007f }
            r5.setProductName(r8)     // Catch:{ all -> 0x007f }
            r5.setBaudRate(r4)     // Catch:{ RobotUsbException -> 0x0050 }
        L_0x0050:
            monitor-exit(r0)     // Catch:{ all -> 0x007f }
            return r5
        L_0x0052:
            r1 = move-exception
            java.lang.String r4 = "exception in %s.open(%s)"
            r5 = 2
            java.lang.Object[] r5 = new java.lang.Object[r5]     // Catch:{ all -> 0x007f }
            java.lang.String r6 = "RobotUsbManagerTty"
            r5[r2] = r6     // Catch:{ all -> 0x007f }
            java.lang.String r8 = r8.getPath()     // Catch:{ all -> 0x007f }
            r5[r3] = r8     // Catch:{ all -> 0x007f }
            com.qualcomm.robotcore.exception.RobotCoreException r8 = com.qualcomm.robotcore.exception.RobotCoreException.createChained(r1, r4, r5)     // Catch:{ all -> 0x007f }
            throw r8     // Catch:{ all -> 0x007f }
        L_0x0067:
            com.qualcomm.robotcore.exception.RobotCoreException r1 = new com.qualcomm.robotcore.exception.RobotCoreException     // Catch:{ all -> 0x007f }
            java.lang.String r4 = "%s is already open: unable to open second time"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x007f }
            r3[r2] = r8     // Catch:{ all -> 0x007f }
            r1.<init>((java.lang.String) r4, (java.lang.Object[]) r3)     // Catch:{ all -> 0x007f }
            throw r1     // Catch:{ all -> 0x007f }
        L_0x0073:
            com.qualcomm.robotcore.exception.RobotCoreException r1 = new com.qualcomm.robotcore.exception.RobotCoreException     // Catch:{ all -> 0x007f }
            java.lang.String r4 = "TTY for %s not found"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x007f }
            r3[r2] = r8     // Catch:{ all -> 0x007f }
            r1.<init>((java.lang.String) r4, (java.lang.Object[]) r3)     // Catch:{ all -> 0x007f }
            throw r1     // Catch:{ all -> 0x007f }
        L_0x007f:
            r8 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x007f }
            throw r8
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.usb.serial.RobotUsbManagerTty.openBySerialNumber(com.qualcomm.robotcore.util.SerialNumber):com.qualcomm.robotcore.hardware.usb.RobotUsbDevice");
    }
}
