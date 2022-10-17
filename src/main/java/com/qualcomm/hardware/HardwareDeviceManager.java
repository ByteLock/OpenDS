package com.qualcomm.hardware;

import android.content.Context;
import android.text.TextUtils;
import android.util.Pair;
import com.qualcomm.hardware.adafruit.AdafruitI2cColorSensor;
import com.qualcomm.hardware.lynx.LynxI2cColorRangeSensor;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.LynxUsbDeviceImpl;
import com.qualcomm.hardware.lynx.LynxUsbUtil;
import com.qualcomm.hardware.lynx.commands.core.LynxFirmwareVersionManager;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cColorSensor;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cGyro;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsI2cIrSeekerSensorV3;
import com.qualcomm.hardware.modernrobotics.ModernRoboticsTouchSensor;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.CRServo;
import com.qualcomm.robotcore.hardware.CRServoImpl;
import com.qualcomm.robotcore.hardware.CRServoImplEx;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorImpl;
import com.qualcomm.robotcore.hardware.DcMotorImplEx;
import com.qualcomm.robotcore.hardware.DcMotorSimple;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.GyroSensor;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchImplOnSimple;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.IrSeekerSensor;
import com.qualcomm.robotcore.hardware.LED;
import com.qualcomm.robotcore.hardware.PWMOutput;
import com.qualcomm.robotcore.hardware.PWMOutputController;
import com.qualcomm.robotcore.hardware.PWMOutputImpl;
import com.qualcomm.robotcore.hardware.RobotCoreLynxModule;
import com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.Servo;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.ServoImpl;
import com.qualcomm.robotcore.hardware.ServoImplEx;
import com.qualcomm.robotcore.hardware.TouchSensor;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.AnalogSensorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.DigitalIoDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.I2cDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManager;
import com.qualcomm.robotcore.hardware.usb.RobotUsbManagerCombining;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.hardware.usb.ftdi.RobotUsbManagerFtdi;
import com.qualcomm.robotcore.hardware.usb.serial.RobotUsbManagerTty;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.external.ClassFactory;
import org.firstinspires.ftc.robotcore.external.Func;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.firstinspires.ftc.robotcore.internal.camera.CameraManagerInternal;
import org.firstinspires.ftc.robotcore.internal.hardware.UserNameable;
import org.firstinspires.ftc.robotcore.internal.hardware.usb.ArmableUsbDevice;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.ftc.robotcore.internal.usb.VendorProductSerialNumber;
import org.firstinspires.inspection.InspectionState;

public class HardwareDeviceManager implements DeviceManager {
    public static final String TAG = "HardwareDeviceManager";
    public static final String TAG_USB_SCAN = "USBScan";
    public static final Object scanDevicesLock = new Object();
    private final Context context;
    private final SyncdDevice.Manager manager;
    /* access modifiers changed from: private */
    public RobotUsbManager usbManager = createUsbManager();

    public HardwareDeviceManager(Context context2, SyncdDevice.Manager manager2) {
        this.context = context2;
        this.manager = manager2;
    }

    public static RobotUsbManager createUsbManager() {
        RobotUsbManagerFtdi robotUsbManagerFtdi = new RobotUsbManagerFtdi();
        if (!LynxConstants.isRevControlHub()) {
            return robotUsbManagerFtdi;
        }
        RobotUsbManagerCombining robotUsbManagerCombining = new RobotUsbManagerCombining();
        robotUsbManagerCombining.addManager(robotUsbManagerFtdi);
        robotUsbManagerCombining.addManager(new RobotUsbManagerTty());
        return robotUsbManagerCombining;
    }

    /*  JADX ERROR: StackOverflow in pass: MarkFinallyVisitor
        jadx.core.utils.exceptions.JadxOverflowException: 
        	at jadx.core.utils.ErrorsCounter.addError(ErrorsCounter.java:47)
        	at jadx.core.utils.ErrorsCounter.methodError(ErrorsCounter.java:81)
        */
    public com.qualcomm.robotcore.hardware.ScannedDevices scanForUsbDevices() throws com.qualcomm.robotcore.exception.RobotCoreException {
        /*
            r19 = this;
            r1 = r19
            java.lang.Object r2 = scanDevicesLock
            monitor-enter(r2)
            long r3 = java.lang.System.nanoTime()     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.hardware.ScannedDevices r0 = new com.qualcomm.robotcore.hardware.ScannedDevices     // Catch:{ all -> 0x0141 }
            r0.<init>()     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbManager r5 = r1.usbManager     // Catch:{ all -> 0x0141 }
            java.util.List r5 = r5.scanForDevices()     // Catch:{ all -> 0x0141 }
            int r6 = r5.size()     // Catch:{ all -> 0x0141 }
            java.lang.String r7 = "USBScan"
            java.lang.String r8 = "device count=%d"
            r9 = 1
            java.lang.Object[] r10 = new java.lang.Object[r9]     // Catch:{ all -> 0x0141 }
            java.lang.Integer r11 = java.lang.Integer.valueOf(r6)     // Catch:{ all -> 0x0141 }
            r12 = 0
            r10[r12] = r11     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r7, (java.lang.String) r8, (java.lang.Object[]) r10)     // Catch:{ all -> 0x0141 }
            r7 = 2
            if (r6 <= 0) goto L_0x0119
            java.lang.String r8 = "hw mgr usb scan"
            java.util.concurrent.ExecutorService r13 = com.qualcomm.robotcore.util.ThreadPool.newFixedThreadPool(r6, r8)     // Catch:{ all -> 0x0141 }
            java.util.concurrent.ConcurrentHashMap r6 = new java.util.concurrent.ConcurrentHashMap     // Catch:{ all -> 0x0141 }
            r6.<init>()     // Catch:{ all -> 0x0141 }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x00ea }
        L_0x003b:
            boolean r8 = r5.hasNext()     // Catch:{ all -> 0x00ea }
            if (r8 == 0) goto L_0x0050
            java.lang.Object r8 = r5.next()     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.util.SerialNumber r8 = (com.qualcomm.robotcore.util.SerialNumber) r8     // Catch:{ all -> 0x00ea }
            com.qualcomm.hardware.HardwareDeviceManager$1 r10 = new com.qualcomm.hardware.HardwareDeviceManager$1     // Catch:{ all -> 0x00ea }
            r10.<init>(r8, r6)     // Catch:{ all -> 0x00ea }
            r13.execute(r10)     // Catch:{ all -> 0x00ea }
            goto L_0x003b
        L_0x0050:
            r13.shutdown()     // Catch:{ all -> 0x00ea }
            r14 = 30
            java.util.concurrent.TimeUnit r16 = java.util.concurrent.TimeUnit.SECONDS     // Catch:{ all -> 0x00ea }
            java.lang.String r17 = "USB Scanning Service"
            java.lang.String r18 = "internal error"
            com.qualcomm.robotcore.util.ThreadPool.awaitTerminationOrExitApplication(r13, r14, r16, r17, r18)     // Catch:{ all -> 0x00ea }
            java.util.Set r5 = r6.entrySet()     // Catch:{ all -> 0x00ea }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x00ea }
        L_0x0066:
            boolean r8 = r5.hasNext()     // Catch:{ all -> 0x00ea }
            if (r8 == 0) goto L_0x0082
            java.lang.Object r8 = r5.next()     // Catch:{ all -> 0x00ea }
            java.util.Map$Entry r8 = (java.util.Map.Entry) r8     // Catch:{ all -> 0x00ea }
            java.lang.Object r10 = r8.getValue()     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r10 = (com.qualcomm.robotcore.hardware.usb.RobotUsbDevice) r10     // Catch:{ all -> 0x00ea }
            java.lang.Object r8 = r8.getKey()     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.util.SerialNumber r8 = (com.qualcomm.robotcore.util.SerialNumber) r8     // Catch:{ all -> 0x00ea }
            r1.determineDeviceType(r10, r8, r0)     // Catch:{ all -> 0x00ea }
            goto L_0x0066
        L_0x0082:
            java.util.Collection r5 = com.qualcomm.robotcore.hardware.usb.RobotUsbDeviceImplBase.getExtantDevices()     // Catch:{ all -> 0x00ea }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x00ea }
        L_0x008a:
            boolean r8 = r5.hasNext()     // Catch:{ all -> 0x00ea }
            if (r8 == 0) goto L_0x00bd
            java.lang.Object r8 = r5.next()     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r8 = (com.qualcomm.robotcore.hardware.usb.RobotUsbDevice) r8     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.util.SerialNumber r10 = r8.getSerialNumber()     // Catch:{ all -> 0x00ea }
            boolean r11 = r6.containsKey(r10)     // Catch:{ all -> 0x00ea }
            if (r11 != 0) goto L_0x008a
            com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r8 = r8.getDeviceType()     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r11 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.FTDI_USB_UNKNOWN_DEVICE     // Catch:{ all -> 0x00ea }
            if (r8 == r11) goto L_0x008a
            java.lang.String r11 = "USBScan"
            java.lang.String r13 = "added extant device %s type=%s"
            java.lang.Object[] r14 = new java.lang.Object[r7]     // Catch:{ all -> 0x00ea }
            r14[r12] = r10     // Catch:{ all -> 0x00ea }
            java.lang.String r15 = r8.toString()     // Catch:{ all -> 0x00ea }
            r14[r9] = r15     // Catch:{ all -> 0x00ea }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r11, (java.lang.String) r13, (java.lang.Object[]) r14)     // Catch:{ all -> 0x00ea }
            r0.put(r10, r8)     // Catch:{ all -> 0x00ea }
            goto L_0x008a
        L_0x00bd:
            java.util.Set r5 = r6.entrySet()     // Catch:{ all -> 0x0141 }
            java.util.Iterator r5 = r5.iterator()     // Catch:{ all -> 0x0141 }
        L_0x00c5:
            boolean r6 = r5.hasNext()     // Catch:{ all -> 0x0141 }
            if (r6 == 0) goto L_0x0119
            java.lang.Object r6 = r5.next()     // Catch:{ all -> 0x0141 }
            java.util.Map$Entry r6 = (java.util.Map.Entry) r6     // Catch:{ all -> 0x0141 }
            java.lang.String r8 = "USBScan"
            java.lang.String r10 = "closing %s"
            java.lang.Object[] r11 = new java.lang.Object[r9]     // Catch:{ all -> 0x0141 }
            java.lang.Object r13 = r6.getKey()     // Catch:{ all -> 0x0141 }
            r11[r12] = r13     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r8, (java.lang.String) r10, (java.lang.Object[]) r11)     // Catch:{ all -> 0x0141 }
            java.lang.Object r6 = r6.getValue()     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r6 = (com.qualcomm.robotcore.hardware.usb.RobotUsbDevice) r6     // Catch:{ all -> 0x0141 }
            r6.close()     // Catch:{ all -> 0x0141 }
            goto L_0x00c5
        L_0x00ea:
            r0 = move-exception
            java.util.Set r3 = r6.entrySet()     // Catch:{ all -> 0x0141 }
            java.util.Iterator r3 = r3.iterator()     // Catch:{ all -> 0x0141 }
        L_0x00f3:
            boolean r4 = r3.hasNext()     // Catch:{ all -> 0x0141 }
            if (r4 == 0) goto L_0x0118
            java.lang.Object r4 = r3.next()     // Catch:{ all -> 0x0141 }
            java.util.Map$Entry r4 = (java.util.Map.Entry) r4     // Catch:{ all -> 0x0141 }
            java.lang.String r5 = "USBScan"
            java.lang.String r6 = "closing %s"
            java.lang.Object[] r7 = new java.lang.Object[r9]     // Catch:{ all -> 0x0141 }
            java.lang.Object r8 = r4.getKey()     // Catch:{ all -> 0x0141 }
            r7[r12] = r8     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r5, (java.lang.String) r6, (java.lang.Object[]) r7)     // Catch:{ all -> 0x0141 }
            java.lang.Object r4 = r4.getValue()     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r4 = (com.qualcomm.robotcore.hardware.usb.RobotUsbDevice) r4     // Catch:{ all -> 0x0141 }
            r4.close()     // Catch:{ all -> 0x0141 }
            goto L_0x00f3
        L_0x0118:
            throw r0     // Catch:{ all -> 0x0141 }
        L_0x0119:
            r1.scanForWebcams(r0)     // Catch:{ all -> 0x0141 }
            long r5 = java.lang.System.nanoTime()     // Catch:{ all -> 0x0141 }
            java.lang.String r8 = "USBScan"
            java.lang.String r10 = "scanForUsbDevices() took %dms count=%d"
            java.lang.Object[] r7 = new java.lang.Object[r7]     // Catch:{ all -> 0x0141 }
            long r5 = r5 - r3
            r3 = 1000000(0xf4240, double:4.940656E-318)
            long r5 = r5 / r3
            int r3 = (int) r5     // Catch:{ all -> 0x0141 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0141 }
            r7[r12] = r3     // Catch:{ all -> 0x0141 }
            int r3 = r0.size()     // Catch:{ all -> 0x0141 }
            java.lang.Integer r3 = java.lang.Integer.valueOf(r3)     // Catch:{ all -> 0x0141 }
            r7[r9] = r3     // Catch:{ all -> 0x0141 }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r8, (java.lang.String) r10, (java.lang.Object[]) r7)     // Catch:{ all -> 0x0141 }
            monitor-exit(r2)     // Catch:{ all -> 0x0141 }
            return r0
        L_0x0141:
            r0 = move-exception
            monitor-exit(r2)     // Catch:{ all -> 0x0141 }
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.HardwareDeviceManager.scanForUsbDevices():com.qualcomm.robotcore.hardware.ScannedDevices");
    }

    /* access modifiers changed from: package-private */
    public Integer countVidPid(Map<Pair<Integer, Integer>, Integer> map, VendorProductSerialNumber vendorProductSerialNumber) {
        Integer num = map.get(new Pair(Integer.valueOf(vendorProductSerialNumber.getVendorId()), Integer.valueOf(vendorProductSerialNumber.getProductId())));
        if (num != null) {
            return num;
        }
        return 0;
    }

    /* access modifiers changed from: package-private */
    public void addVidPid(Map<Pair<Integer, Integer>, Integer> map, VendorProductSerialNumber vendorProductSerialNumber, int i) {
        map.put(new Pair(Integer.valueOf(vendorProductSerialNumber.getVendorId()), Integer.valueOf(vendorProductSerialNumber.getProductId())), Integer.valueOf(countVidPid(map, vendorProductSerialNumber).intValue() + i));
    }

    /* access modifiers changed from: protected */
    public void scanForWebcams(ScannedDevices scannedDevices) {
        synchronized (scanDevicesLock) {
            List<WebcamName> allWebcams = ClassFactory.getInstance().getCameraManager().getAllWebcams();
            HashMap hashMap = new HashMap();
            HashMap hashMap2 = new HashMap();
            for (WebcamName serialNumber : allWebcams) {
                SerialNumber serialNumber2 = serialNumber.getSerialNumber();
                if (serialNumber2.isVendorProduct()) {
                    VendorProductSerialNumber vendorProductSerialNumber = (VendorProductSerialNumber) serialNumber2;
                    if (TextUtils.isEmpty(vendorProductSerialNumber.getConnectionPath())) {
                        addVidPid(hashMap2, vendorProductSerialNumber, 1);
                    } else {
                        addVidPid(hashMap, vendorProductSerialNumber, 1);
                    }
                }
            }
            for (WebcamName serialNumber3 : allWebcams) {
                SerialNumber serialNumber4 = serialNumber3.getSerialNumber();
                if (serialNumber4.isVendorProduct()) {
                    VendorProductSerialNumber vendorProductSerialNumber2 = (VendorProductSerialNumber) serialNumber4;
                    int intValue = countVidPid(hashMap2, vendorProductSerialNumber2).intValue();
                    if (intValue > 1) {
                        RobotLog.m49ee(TAG, "%d serialnumless webcams w/o connection info; ignoring", Integer.valueOf(intValue), vendorProductSerialNumber2);
                    } else if (countVidPid(hashMap2, vendorProductSerialNumber2).intValue() == 0 && countVidPid(hashMap, vendorProductSerialNumber2).intValue() == 1) {
                        serialNumber4 = SerialNumber.fromVidPid(vendorProductSerialNumber2.getVendorId(), vendorProductSerialNumber2.getProductId(), InspectionState.NO_VERSION);
                    }
                }
                RobotLog.m61vv(TAG, "scanned webcam serial=%s", serialNumber4);
                scannedDevices.put(serialNumber4, DeviceManager.UsbDeviceType.WEBCAM);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void determineDeviceType(RobotUsbDevice robotUsbDevice, SerialNumber serialNumber, ScannedDevices scannedDevices) {
        DeviceManager.UsbDeviceType deviceType = RobotUsbDeviceImplBase.getDeviceType(serialNumber);
        if (deviceType == DeviceManager.UsbDeviceType.UNKNOWN_DEVICE) {
            if (robotUsbDevice.getUsbIdentifiers().isLynxDevice()) {
                RobotLog.m61vv(TAG_USB_SCAN, "%s is a lynx device", serialNumber);
                deviceType = getLynxDeviceType(robotUsbDevice);
            } else {
                return;
            }
        }
        scannedDevices.put(serialNumber, deviceType);
    }

    /* access modifiers changed from: package-private */
    public DeviceManager.UsbDeviceType getLynxDeviceType(RobotUsbDevice robotUsbDevice) {
        DeviceManager.UsbDeviceType usbDeviceType = DeviceManager.UsbDeviceType.LYNX_USB_DEVICE;
        robotUsbDevice.setDeviceType(usbDeviceType);
        return usbDeviceType;
    }

    public RobotCoreLynxUsbDevice createLynxUsbDevice(final SerialNumber serialNumber, String str) throws RobotCoreException, InterruptedException {
        Context context2 = this.context;
        HardwareFactory.noteSerialNumberType(context2, serialNumber, context2.getString(C0660R.string.moduleDisplayNameLynxUsbDevice));
        RobotLog.m59v("Creating %s", HardwareFactory.getDeviceDisplayName(this.context, serialNumber));
        return LynxUsbDeviceImpl.findOrCreateAndArm(this.context, serialNumber, this.manager, new ArmableUsbDevice.OpenRobotUsbDevice() {
            public RobotUsbDevice open() throws RobotCoreException {
                RobotUsbDevice robotUsbDevice = null;
                try {
                    boolean z = true;
                    RobotUsbDevice openUsbDevice = LynxUsbUtil.openUsbDevice(true, HardwareDeviceManager.this.usbManager, serialNumber);
                    if (!openUsbDevice.getUsbIdentifiers().isLynxDevice()) {
                        HardwareDeviceManager.this.closeAndThrowOnFailedDeviceTypeCheck(openUsbDevice, serialNumber);
                    }
                    if (HardwareDeviceManager.this.getLynxDeviceType(openUsbDevice) != DeviceManager.UsbDeviceType.LYNX_USB_DEVICE) {
                        z = false;
                    }
                    Assert.assertTrue(z);
                    return openUsbDevice;
                } catch (RobotCoreException | RuntimeException e) {
                    if (robotUsbDevice != null) {
                        robotUsbDevice.close();
                    }
                    throw e;
                }
            }
        });
    }

    public DcMotor createDcMotor(DcMotorController dcMotorController, int i, MotorConfigurationType motorConfigurationType, String str) {
        return new DcMotorImpl(dcMotorController, i, DcMotorSimple.Direction.FORWARD, motorConfigurationType);
    }

    public DcMotor createDcMotorEx(DcMotorController dcMotorController, int i, MotorConfigurationType motorConfigurationType, String str) {
        return new DcMotorImplEx(dcMotorController, i, DcMotorSimple.Direction.FORWARD, motorConfigurationType);
    }

    public Servo createServo(ServoController servoController, int i, String str) {
        return new ServoImpl(servoController, i, Servo.Direction.FORWARD);
    }

    public CRServo createCRServo(ServoController servoController, int i, String str) {
        return new CRServoImpl(servoController, i, DcMotorSimple.Direction.FORWARD);
    }

    public Servo createServoEx(ServoControllerEx servoControllerEx, int i, String str, ServoConfigurationType servoConfigurationType) {
        return new ServoImplEx(servoControllerEx, i, Servo.Direction.FORWARD, servoConfigurationType);
    }

    public CRServo createCRServoEx(ServoControllerEx servoControllerEx, int i, String str, ServoConfigurationType servoConfigurationType) {
        return new CRServoImplEx(servoControllerEx, i, DcMotorSimple.Direction.FORWARD, servoConfigurationType);
    }

    public HardwareDevice createCustomServoDevice(ServoController servoController, int i, ServoConfigurationType servoConfigurationType) {
        return servoConfigurationType.createInstanceMr(servoController, i);
    }

    public HardwareDevice createLynxCustomServoDevice(ServoControllerEx servoControllerEx, int i, ServoConfigurationType servoConfigurationType) {
        return servoConfigurationType.createInstanceRev(servoControllerEx, i);
    }

    public RobotCoreLynxModule createLynxModule(RobotCoreLynxUsbDevice robotCoreLynxUsbDevice, int i, boolean z, String str) {
        RobotLog.m59v("Creating Lynx Module - mod=%d parent=%s", Integer.valueOf(i), Boolean.toString(z));
        return new LynxModule((LynxUsbDevice) robotCoreLynxUsbDevice, i, z, true);
    }

    public WebcamName createWebcamName(final SerialNumber serialNumber, String str) throws RobotCoreException, InterruptedException {
        Context context2 = this.context;
        HardwareFactory.noteSerialNumberType(context2, serialNumber, context2.getString(C0660R.string.moduleDisplayNameWebcam));
        RobotLog.m59v("Creating %s", HardwareFactory.getDeviceDisplayName(this.context, serialNumber));
        WebcamName webcamNameFromSerialNumber = ((CameraManagerInternal) ClassFactory.getInstance().getCameraManager()).webcamNameFromSerialNumber(serialNumber, new ArmableUsbDevice.OpenRobotUsbDevice() {
            public RobotUsbDevice open() throws RobotCoreException {
                if (((CameraManagerInternal) ClassFactory.getInstance().getCameraManager()).isWebcamAttached(serialNumber)) {
                    return null;
                }
                RobotLog.logAndThrow("Unable to find webcam with serial number " + serialNumber);
                return null;
            }
        }, this.manager);
        if (webcamNameFromSerialNumber instanceof UserNameable) {
            ((UserNameable) webcamNameFromSerialNumber).setUserName(str);
        }
        ((RobotUsbModule) webcamNameFromSerialNumber).armOrPretend();
        return webcamNameFromSerialNumber;
    }

    public TouchSensor createMRDigitalTouchSensor(DigitalChannelController digitalChannelController, int i, String str) {
        RobotLog.m58v("Creating Modern Robotics digital Touch Sensor - Port: " + i);
        return new ModernRoboticsTouchSensor(digitalChannelController, i);
    }

    public IrSeekerSensor createMRI2cIrSeekerSensorV3(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating Modern Robotics I2C IR Seeker Sensor V3 - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new ModernRoboticsI2cIrSeekerSensorV3(createI2cDeviceSynch(robotCoreLynxModule, i2cChannel, str));
    }

    public HardwareDevice createAnalogSensor(AnalogInputController analogInputController, int i, AnalogSensorConfigurationType analogSensorConfigurationType) {
        RobotLog.m58v("Creating Analog Sensor - Type: " + analogSensorConfigurationType.getName() + " - Port: " + i);
        return analogSensorConfigurationType.createInstance(analogInputController, i);
    }

    public HardwareDevice createDigitalDevice(DigitalChannelController digitalChannelController, int i, DigitalIoDeviceConfigurationType digitalIoDeviceConfigurationType) {
        RobotLog.m58v("Creating Digital Channel Device - Type: " + digitalIoDeviceConfigurationType.getName() + " - Port: " + i);
        return digitalIoDeviceConfigurationType.createInstance(digitalChannelController, i);
    }

    public PWMOutput createPwmOutputDevice(PWMOutputController pWMOutputController, int i, String str) {
        RobotLog.m58v("Creating PWM Output Device - Port: " + i);
        return new PWMOutputImpl(pWMOutputController, i);
    }

    public HardwareDevice createUserI2cDevice(final RobotCoreLynxModule robotCoreLynxModule, final DeviceConfiguration.I2cChannel i2cChannel, I2cDeviceConfigurationType i2cDeviceConfigurationType, final String str) {
        RobotLog.m59v("Creating user sensor %s - on Lynx module=%d bus=%d", i2cDeviceConfigurationType.getName(), Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return i2cDeviceConfigurationType.createInstance(robotCoreLynxModule, new Func<I2cDeviceSynchSimple>() {
            public I2cDeviceSynchSimple value() {
                return HardwareDeviceManager.this.createI2cDeviceSynchSimple(robotCoreLynxModule, i2cChannel, str);
            }
        }, new Func<I2cDeviceSynch>() {
            public I2cDeviceSynch value() {
                return HardwareDeviceManager.this.createI2cDeviceSynch(robotCoreLynxModule, i2cChannel, str);
            }
        });
    }

    public ColorSensor createAdafruitI2cColorSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating Adafruit Color Sensor (Lynx) - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new AdafruitI2cColorSensor(createI2cDeviceSynchSimple(robotCoreLynxModule, i2cChannel, str));
    }

    public ColorSensor createLynxColorRangeSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating Lynx Color/Range Sensor - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new LynxI2cColorRangeSensor(createI2cDeviceSynchSimple(robotCoreLynxModule, i2cChannel, str));
    }

    public ColorSensor createModernRoboticsI2cColorSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating Modern Robotics I2C Color Sensor - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new ModernRoboticsI2cColorSensor(createI2cDeviceSynch(robotCoreLynxModule, i2cChannel, str));
    }

    public GyroSensor createModernRoboticsI2cGyroSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating Modern Robotics I2C Gyro Sensor - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new ModernRoboticsI2cGyro(createI2cDeviceSynch(robotCoreLynxModule, i2cChannel, str));
    }

    public LED createLED(DigitalChannelController digitalChannelController, int i, String str) {
        RobotLog.m58v("Creating LED - Port: " + i);
        return new LED(digitalChannelController, i);
    }

    public I2cDeviceSynch createI2cDeviceSynch(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        RobotLog.m59v("Creating I2cDeviceSynch (Lynx) - mod=%d bus=%d", Integer.valueOf(robotCoreLynxModule.getModuleAddress()), Integer.valueOf(i2cChannel.channel));
        return new I2cDeviceSynchImplOnSimple(createI2cDeviceSynchSimple(robotCoreLynxModule, i2cChannel, str), true);
    }

    /* access modifiers changed from: protected */
    public I2cDeviceSynchSimple createI2cDeviceSynchSimple(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str) {
        LynxI2cDeviceSynch createLynxI2cDeviceSynch = LynxFirmwareVersionManager.createLynxI2cDeviceSynch(this.context, (LynxModule) robotCoreLynxModule, i2cChannel.channel);
        createLynxI2cDeviceSynch.setUserConfiguredName(str);
        return createLynxI2cDeviceSynch;
    }

    private RobotUsbDevice.FirmwareVersion getModernRoboticsFirmwareVersion(byte[] bArr) {
        return new RobotUsbDevice.FirmwareVersion(bArr[0]);
    }

    /* access modifiers changed from: private */
    public void closeAndThrowOnFailedDeviceTypeCheck(RobotUsbDevice robotUsbDevice, SerialNumber serialNumber) throws RobotCoreException {
        String format = String.format("%s is returning garbage data on the USB bus", new Object[]{HardwareFactory.getDeviceDisplayName(this.context, serialNumber)});
        robotUsbDevice.close();
        logAndThrow(format);
    }

    private void logAndThrow(String str) throws RobotCoreException {
        System.err.println(str);
        throw new RobotCoreException(str);
    }
}
