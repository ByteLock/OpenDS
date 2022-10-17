package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.LynxModuleMeta;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.I2cDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import org.firstinspires.ftc.robotcore.external.function.Supplier;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.Misc;

public class ConfigurationUtility {
    public static final String TAG = "ConfigurationUtility";
    public static final int firstNamedDeviceNumber = 1;
    protected Set<String> existingNames;

    public ConfigurationUtility() {
        resetNameUniquifiers();
    }

    public void resetNameUniquifiers() {
        this.existingNames = new HashSet();
    }

    public Set<String> getExistingNames(ConfigurationType configurationType) {
        return this.existingNames;
    }

    /* access modifiers changed from: protected */
    public void noteExistingName(ConfigurationType configurationType, String str) {
        getExistingNames(configurationType).add(str);
    }

    /* access modifiers changed from: protected */
    public String createUniqueName(ConfigurationType configurationType, int i) {
        return createUniqueName(configurationType, AppUtil.getDefContext().getString(i), 1);
    }

    /* access modifiers changed from: protected */
    public String createUniqueName(ConfigurationType configurationType, int i, int i2) {
        return createUniqueName(configurationType, AppUtil.getDefContext().getString(i), i2);
    }

    /* access modifiers changed from: protected */
    public String createUniqueName(ConfigurationType configurationType, String str, int i) {
        return createUniqueName(configurationType, (String) null, str, i);
    }

    /* access modifiers changed from: protected */
    public String createUniqueName(ConfigurationType configurationType, int i, int i2, int i3) {
        return createUniqueName(configurationType, AppUtil.getDefContext().getString(i), AppUtil.getDefContext().getString(i2), i3);
    }

    private String createUniqueName(ConfigurationType configurationType, String str, String str2, int i) {
        Set<String> existingNames2 = getExistingNames(configurationType);
        if (str == null || existingNames2.contains(str)) {
            String formatForUser = Misc.formatForUser(str2, Integer.valueOf(i));
            if (!existingNames2.contains(formatForUser)) {
                noteExistingName(configurationType, formatForUser);
                return formatForUser;
            }
            int i2 = 1;
            while (true) {
                String formatForUser2 = Misc.formatForUser(str2, Integer.valueOf(i2));
                if (!existingNames2.contains(formatForUser2)) {
                    noteExistingName(configurationType, formatForUser2);
                    return formatForUser2;
                }
                i2++;
            }
        } else {
            noteExistingName(configurationType, str);
            return str;
        }
    }

    /* renamed from: com.qualcomm.robotcore.hardware.configuration.ConfigurationUtility$2 */
    static /* synthetic */ class C07342 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$DeviceManager$UsbDeviceType */
        static final /* synthetic */ int[] f128x1abb0d8c;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|6) */
        /* JADX WARNING: Code restructure failed: missing block: B:7:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        static {
            /*
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType[] r0 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f128x1abb0d8c = r0
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r1 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.WEBCAM     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f128x1abb0d8c     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r1 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.LYNX_USB_DEVICE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.configuration.ConfigurationUtility.C07342.<clinit>():void");
        }
    }

    public ControllerConfiguration buildNewControllerConfiguration(SerialNumber serialNumber, DeviceManager.UsbDeviceType usbDeviceType, Supplier<LynxModuleMetaList> supplier) {
        int i = C07342.f128x1abb0d8c[usbDeviceType.ordinal()];
        if (i == 1) {
            return buildNewWebcam(serialNumber);
        }
        if (i != 2) {
            return null;
        }
        return buildNewLynxUsbDevice(serialNumber, supplier);
    }

    public WebcamConfiguration buildNewWebcam(SerialNumber serialNumber) {
        return new WebcamConfiguration(createUniqueName(BuiltInConfigurationType.WEBCAM, C0705R.string.counted_camera_name), serialNumber);
    }

    public LynxUsbDeviceConfiguration buildNewLynxUsbDevice(SerialNumber serialNumber, Supplier<LynxModuleMetaList> supplier) {
        return buildNewLynxUsbDevice(serialNumber, supplier.get());
    }

    public LynxUsbDeviceConfiguration buildNewLynxUsbDevice(SerialNumber serialNumber, LynxModuleMetaList lynxModuleMetaList) {
        LynxModuleMeta.ImuType imuType;
        String str;
        LynxModuleMeta.ImuType imuType2;
        LynxModuleMeta.ImuType imuType3;
        RobotLog.m61vv(TAG, "buildNewLynxUsbDevice(%s)...", serialNumber);
        boolean isEmbedded = serialNumber.isEmbedded();
        if (lynxModuleMetaList == null) {
            try {
                lynxModuleMetaList = new LynxModuleMetaList(serialNumber);
            } catch (Throwable th) {
                RobotLog.m61vv(TAG, "...buildNewLynxUsbDevice(%s): ", serialNumber);
                throw th;
            }
        }
        RobotLog.m61vv(TAG, "buildLynxUsbDevice(): discovered lynx modules: %s", lynxModuleMetaList);
        if (lynxModuleMetaList.getParent() == null) {
            imuType = LynxModuleMeta.ImuType.NONE;
        } else {
            imuType = lynxModuleMetaList.getParent().imuType();
            if (imuType == LynxModuleMeta.ImuType.UNKNOWN) {
                RobotLog.m36aa(TAG, "parent IMU type was UNKNOWN in buildNewLynxDevice()");
                imuType = LynxModuleMeta.ImuType.NONE;
            }
        }
        LinkedList linkedList = new LinkedList();
        Iterator<LynxModuleMeta> it = lynxModuleMetaList.iterator();
        while (it.hasNext()) {
            LynxModuleMeta next = it.next();
            boolean z = isEmbedded && next.isParent() && next.getModuleAddress() == 173;
            if (imuType == LynxModuleMeta.ImuType.NONE) {
                imuType3 = next.imuType();
                if (imuType3 == LynxModuleMeta.ImuType.UNKNOWN) {
                    RobotLog.m36aa(TAG, "module IMU type was UNKNOWN in buildNewLynxDevice()");
                    imuType3 = LynxModuleMeta.ImuType.NONE;
                }
            } else if (next.isParent()) {
                imuType2 = imuType;
                linkedList.add(buildNewLynxModule(next.getModuleAddress(), next.isParent(), imuType2, true, z));
            } else {
                imuType3 = LynxModuleMeta.ImuType.NONE;
            }
            imuType2 = imuType3;
            linkedList.add(buildNewLynxModule(next.getModuleAddress(), next.isParent(), imuType2, true, z));
        }
        DeviceConfiguration.sortByName(linkedList);
        RobotLog.m61vv(TAG, "buildNewLynxUsbDevice(%s): %d modules", serialNumber, Integer.valueOf(linkedList.size()));
        if (isEmbedded) {
            str = createUniqueName((ConfigurationType) BuiltInConfigurationType.LYNX_USB_DEVICE, C0705R.string.control_hub_usb_device_name, C0705R.string.counted_lynx_usb_device_name, 0);
        } else {
            str = createUniqueName(BuiltInConfigurationType.LYNX_USB_DEVICE, C0705R.string.counted_lynx_usb_device_name);
        }
        LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration = new LynxUsbDeviceConfiguration(str, linkedList, serialNumber);
        RobotLog.m61vv(TAG, "...buildNewLynxUsbDevice(%s): ", serialNumber);
        return lynxUsbDeviceConfiguration;
    }

    public LynxModuleConfiguration buildNewLynxModule(int i, boolean z, LynxModuleMeta.ImuType imuType, boolean z2, boolean z3) {
        String str;
        I2cDeviceConfigurationType i2cDeviceConfigurationType;
        if (z3) {
            str = createUniqueName((ConfigurationType) BuiltInConfigurationType.LYNX_MODULE, C0705R.string.control_hub_module_name, C0705R.string.counted_lynx_module_name, 0);
        } else {
            str = createUniqueName((ConfigurationType) BuiltInConfigurationType.LYNX_MODULE, C0705R.string.counted_lynx_module_name, i);
        }
        LynxModuleConfiguration buildEmptyLynxModule = buildEmptyLynxModule(str, i, z, z2);
        if (!(imuType == LynxModuleMeta.ImuType.NONE || imuType == LynxModuleMeta.ImuType.UNKNOWN)) {
            if (imuType == LynxModuleMeta.ImuType.BNO055) {
                i2cDeviceConfigurationType = I2cDeviceConfigurationType.getLynxEmbeddedBNO055ImuType();
            } else if (imuType == LynxModuleMeta.ImuType.BHI260) {
                i2cDeviceConfigurationType = I2cDeviceConfigurationType.getLynxEmbeddedBHI260APImuType();
            } else {
                throw new RuntimeException("Unrecognized embedded IMU type");
            }
            Assert.assertTrue(i2cDeviceConfigurationType != null && i2cDeviceConfigurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.I2C));
            String createUniqueName = createUniqueName((ConfigurationType) i2cDeviceConfigurationType, C0705R.string.preferred_imu_name, C0705R.string.counted_imu_name, 1);
            LynxI2cDeviceConfiguration lynxI2cDeviceConfiguration = new LynxI2cDeviceConfiguration();
            lynxI2cDeviceConfiguration.setConfigurationType(i2cDeviceConfigurationType);
            lynxI2cDeviceConfiguration.setName(createUniqueName);
            lynxI2cDeviceConfiguration.setEnabled(true);
            lynxI2cDeviceConfiguration.setBus(0);
            buildEmptyLynxModule.getI2cDevices().add(lynxI2cDeviceConfiguration);
        }
        return buildEmptyLynxModule;
    }

    /* access modifiers changed from: protected */
    public LynxUsbDeviceConfiguration buildNewEmbeddedLynxUsbDevice(final DeviceManager deviceManager) {
        LynxUsbDeviceConfiguration buildNewLynxUsbDevice = buildNewLynxUsbDevice(LynxConstants.SERIAL_NUMBER_EMBEDDED, (Supplier<LynxModuleMetaList>) new Supplier<LynxModuleMetaList>() {
            /* JADX WARNING: Code restructure failed: missing block: B:23:0x002f, code lost:
                if (r1 != null) goto L_0x0031;
             */
            /* JADX WARNING: Removed duplicated region for block: B:18:0x0026 A[Catch:{ InterruptedException -> 0x0027, RobotCoreException -> 0x001b, all -> 0x0016, all -> 0x0035 }] */
            /* JADX WARNING: Removed duplicated region for block: B:28:0x0038  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public com.qualcomm.robotcore.hardware.LynxModuleMetaList get() {
                /*
                    r6 = this;
                    r0 = 0
                    com.qualcomm.robotcore.hardware.DeviceManager r1 = r4     // Catch:{ InterruptedException -> 0x0027, RobotCoreException -> 0x001b, all -> 0x0016 }
                    com.qualcomm.robotcore.util.SerialNumber r2 = com.qualcomm.robotcore.hardware.configuration.LynxConstants.SERIAL_NUMBER_EMBEDDED     // Catch:{ InterruptedException -> 0x0027, RobotCoreException -> 0x001b, all -> 0x0016 }
                    com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r1 = r1.createLynxUsbDevice(r2, r0)     // Catch:{ InterruptedException -> 0x0027, RobotCoreException -> 0x001b, all -> 0x0016 }
                    r2 = 1
                    com.qualcomm.robotcore.hardware.LynxModuleMetaList r0 = r1.discoverModules(r2)     // Catch:{ InterruptedException -> 0x0028, RobotCoreException -> 0x0014 }
                    if (r1 == 0) goto L_0x0013
                    r1.close()
                L_0x0013:
                    return r0
                L_0x0014:
                    r2 = move-exception
                    goto L_0x001d
                L_0x0016:
                    r1 = move-exception
                    r5 = r1
                    r1 = r0
                    r0 = r5
                    goto L_0x0036
                L_0x001b:
                    r2 = move-exception
                    r1 = r0
                L_0x001d:
                    java.lang.String r3 = "ConfigurationUtility"
                    java.lang.String r4 = "exception in buildNewEmbeddedLynxUsbDevice()"
                    com.qualcomm.robotcore.util.RobotLog.m50ee((java.lang.String) r3, (java.lang.Throwable) r2, (java.lang.String) r4)     // Catch:{ all -> 0x0035 }
                    if (r1 == 0) goto L_0x0034
                    goto L_0x0031
                L_0x0027:
                    r1 = r0
                L_0x0028:
                    java.lang.Thread r2 = java.lang.Thread.currentThread()     // Catch:{ all -> 0x0035 }
                    r2.interrupt()     // Catch:{ all -> 0x0035 }
                    if (r1 == 0) goto L_0x0034
                L_0x0031:
                    r1.close()
                L_0x0034:
                    return r0
                L_0x0035:
                    r0 = move-exception
                L_0x0036:
                    if (r1 == 0) goto L_0x003b
                    r1.close()
                L_0x003b:
                    throw r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.configuration.ConfigurationUtility.C07331.get():com.qualcomm.robotcore.hardware.LynxModuleMetaList");
            }
        });
        buildNewLynxUsbDevice.setEnabled(true);
        buildNewLynxUsbDevice.setSystemSynthetic(true);
        for (LynxModuleConfiguration systemSynthetic : buildNewLynxUsbDevice.getModules()) {
            systemSynthetic.setSystemSynthetic(true);
        }
        return buildNewLynxUsbDevice;
    }

    protected static List<DeviceConfiguration> buildEmptyDevices(int i, int i2, ConfigurationType configurationType) {
        ArrayList arrayList = new ArrayList();
        for (int i3 = 0; i3 < i2; i3++) {
            arrayList.add(new DeviceConfiguration(i3 + i, configurationType, DeviceConfiguration.DISABLED_DEVICE_NAME, false));
        }
        return arrayList;
    }

    public static List<DeviceConfiguration> buildEmptyMotors(int i, int i2) {
        return buildEmptyDevices(i, i2, MotorConfigurationType.getUnspecifiedMotorType());
    }

    public static List<DeviceConfiguration> buildEmptyServos(int i, int i2) {
        return buildEmptyDevices(i, i2, ServoConfigurationType.getStandardServoType());
    }

    /* access modifiers changed from: protected */
    public LynxModuleConfiguration buildEmptyLynxModule(String str, int i, boolean z, boolean z2) {
        RobotLog.m61vv(TAG, "buildEmptyLynxModule() mod#=%d...", Integer.valueOf(i));
        noteExistingName(BuiltInConfigurationType.LYNX_MODULE, str);
        LynxModuleConfiguration lynxModuleConfiguration = new LynxModuleConfiguration(str);
        lynxModuleConfiguration.setModuleAddress(i);
        lynxModuleConfiguration.setIsParent(z);
        lynxModuleConfiguration.setEnabled(z2);
        RobotLog.m61vv(TAG, "...buildEmptyLynxModule() mod#=%d", Integer.valueOf(i));
        return lynxModuleConfiguration;
    }
}
