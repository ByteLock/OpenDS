package com.qualcomm.robotcore.hardware.configuration;

import android.content.Context;
import com.qualcomm.ftccommon.configuration.RobotConfigResFilter;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public enum BuiltInConfigurationType implements ConfigurationType {
    GYRO("Gyro", ConfigurationType.DeviceFlavor.I2C),
    COMPASS("Compass", (int) null),
    IR_SEEKER("IrSeeker", (int) null),
    LIGHT_SENSOR("LightSensor", (int) null),
    ACCELEROMETER("Accelerometer", (int) null),
    TOUCH_SENSOR("TouchSensor", ConfigurationType.DeviceFlavor.DIGITAL_IO),
    PULSE_WIDTH_DEVICE("PulseWidthDevice", (int) null),
    IR_SEEKER_V3("IrSeekerV3", ConfigurationType.DeviceFlavor.I2C),
    ULTRASONIC_SENSOR("UltrasonicSensor", (int) null),
    ADAFRUIT_COLOR_SENSOR("AdafruitColorSensor", ConfigurationType.DeviceFlavor.I2C),
    COLOR_SENSOR("ColorSensor", ConfigurationType.DeviceFlavor.I2C),
    LYNX_COLOR_SENSOR("LynxColorSensor", ConfigurationType.DeviceFlavor.I2C),
    LYNX_USB_DEVICE("LynxUsbDevice", (int) null),
    LYNX_MODULE(LynxModule.TAG, (int) null),
    WEBCAM("Webcam", (int) null),
    ROBOT(RobotConfigResFilter.robotConfigRootTag, (int) null),
    NOTHING("Nothing", (int) null),
    UNKNOWN("<unknown>", (int) null);
    
    private static final List<BuiltInConfigurationType> valuesCache = null;
    private final Context context;
    private final ConfigurationType.DeviceFlavor deviceFlavor;
    private final String xmlTag;

    public String[] getXmlTagAliases() {
        return new String[0];
    }

    static {
        valuesCache = Collections.unmodifiableList(Arrays.asList(values()));
    }

    private BuiltInConfigurationType(String str, ConfigurationType.DeviceFlavor deviceFlavor2) {
        this.context = AppUtil.getDefContext();
        this.xmlTag = str;
        this.deviceFlavor = deviceFlavor2;
    }

    public static BuiltInConfigurationType fromXmlTag(String str) {
        for (BuiltInConfigurationType next : valuesCache) {
            if (str.equalsIgnoreCase(next.xmlTag)) {
                return next;
            }
        }
        return UNKNOWN;
    }

    public static ConfigurationType fromString(String str) {
        for (ConfigurationType next : valuesCache) {
            if (str.equalsIgnoreCase(next.toString())) {
                return next;
            }
        }
        return UNKNOWN;
    }

    public static ConfigurationType fromUSBDeviceType(DeviceManager.UsbDeviceType usbDeviceType) {
        int i = C07271.f125x1abb0d8c[usbDeviceType.ordinal()];
        if (i == 1) {
            return LYNX_USB_DEVICE;
        }
        if (i != 2) {
            return UNKNOWN;
        }
        return WEBCAM;
    }

    public boolean isDeviceFlavor(ConfigurationType.DeviceFlavor deviceFlavor2) {
        if (deviceFlavor2 == ConfigurationType.DeviceFlavor.BUILT_IN || deviceFlavor2 == this.deviceFlavor) {
            return true;
        }
        return false;
    }

    public ConfigurationType.DeviceFlavor getDeviceFlavor() {
        ConfigurationType.DeviceFlavor deviceFlavor2 = this.deviceFlavor;
        if (deviceFlavor2 != null) {
            return deviceFlavor2;
        }
        return ConfigurationType.DeviceFlavor.BUILT_IN;
    }

    /* renamed from: com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType$1 */
    static /* synthetic */ class C07271 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$DeviceManager$UsbDeviceType */
        static final /* synthetic */ int[] f125x1abb0d8c = null;

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$configuration$BuiltInConfigurationType */
        static final /* synthetic */ int[] f126x8d86fe56 = null;

        /* JADX WARNING: Can't wrap try/catch for region: R(31:0|(2:1|2)|3|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|29|30|31|32|34) */
        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|5|6|7|8|9|10|11|12|13|14|15|16|17|18|19|20|21|22|23|24|25|26|27|29|30|31|32|34) */
        /* JADX WARNING: Code restructure failed: missing block: B:35:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x0033 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:19:0x0060 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:21:0x006c */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x0078 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0084 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x00a1 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0028 */
        static {
            /*
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType[] r0 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f126x8d86fe56 = r0
                r1 = 1
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r2 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.LYNX_USB_DEVICE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.WEBCAM     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.PULSE_WIDTH_DEVICE     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r4 = 3
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.IR_SEEKER_V3     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r4 = 4
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.ADAFRUIT_COLOR_SENSOR     // Catch:{ NoSuchFieldError -> 0x003e }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r4 = 5
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.LYNX_COLOR_SENSOR     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r4 = 6
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.LYNX_MODULE     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r4 = 7
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.NOTHING     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r4 = 8
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x006c }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.TOUCH_SENSOR     // Catch:{ NoSuchFieldError -> 0x006c }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x006c }
                r4 = 9
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x006c }
            L_0x006c:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0078 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.GYRO     // Catch:{ NoSuchFieldError -> 0x0078 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0078 }
                r4 = 10
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0078 }
            L_0x0078:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0084 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.COLOR_SENSOR     // Catch:{ NoSuchFieldError -> 0x0084 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0084 }
                r4 = 11
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0084 }
            L_0x0084:
                int[] r2 = f126x8d86fe56     // Catch:{ NoSuchFieldError -> 0x0090 }
                com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType r3 = com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0090 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0090 }
                r4 = 12
                r2[r3] = r4     // Catch:{ NoSuchFieldError -> 0x0090 }
            L_0x0090:
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType[] r2 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.values()
                int r2 = r2.length
                int[] r2 = new int[r2]
                f125x1abb0d8c = r2
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r3 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.LYNX_USB_DEVICE     // Catch:{ NoSuchFieldError -> 0x00a1 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x00a1 }
                r2[r3] = r1     // Catch:{ NoSuchFieldError -> 0x00a1 }
            L_0x00a1:
                int[] r1 = f125x1abb0d8c     // Catch:{ NoSuchFieldError -> 0x00ab }
                com.qualcomm.robotcore.hardware.DeviceManager$UsbDeviceType r2 = com.qualcomm.robotcore.hardware.DeviceManager.UsbDeviceType.WEBCAM     // Catch:{ NoSuchFieldError -> 0x00ab }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x00ab }
                r1[r2] = r0     // Catch:{ NoSuchFieldError -> 0x00ab }
            L_0x00ab:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType.C07271.<clinit>():void");
        }
    }

    public DeviceManager.UsbDeviceType toUSBDeviceType() {
        int i = C07271.f126x8d86fe56[ordinal()];
        if (i == 1) {
            return DeviceManager.UsbDeviceType.LYNX_USB_DEVICE;
        }
        if (i != 2) {
            return DeviceManager.UsbDeviceType.FTDI_USB_UNKNOWN_DEVICE;
        }
        return DeviceManager.UsbDeviceType.WEBCAM;
    }

    public String getDisplayName(ConfigurationType.DisplayNameFlavor displayNameFlavor) {
        switch (C07271.f126x8d86fe56[ordinal()]) {
            case 1:
                return this.context.getString(C0705R.string.configTypeLynxUSBDevice);
            case 2:
                return this.context.getString(C0705R.string.configTypeWebcam);
            case 3:
                return this.context.getString(C0705R.string.configTypePulseWidthDevice);
            case 4:
                return this.context.getString(C0705R.string.configTypeIrSeekerV3);
            case 5:
                return this.context.getString(C0705R.string.configTypeAdafruitColorSensor);
            case 6:
                return this.context.getString(C0705R.string.configTypeLynxColorSensor);
            case 7:
                return this.context.getString(C0705R.string.configTypeLynxModule);
            case 8:
                return this.context.getString(C0705R.string.configTypeNothing);
            case 9:
                return this.context.getString(C0705R.string.configTypeMRTouchSensor);
            case 10:
                return this.context.getString(C0705R.string.configTypeMRGyro);
            case 11:
                return this.context.getString(C0705R.string.configTypeMRColorSensor);
            default:
                return this.context.getString(C0705R.string.configTypeUnknown);
        }
    }

    public boolean isDeprecated() {
        try {
            return BuiltInConfigurationType.class.getField(toString()).isAnnotationPresent(Deprecated.class);
        } catch (NoSuchFieldException e) {
            RobotLog.logStackTrace(e);
            return false;
        }
    }

    public String getXmlTag() {
        return this.xmlTag;
    }
}
