package com.qualcomm.robotcore.hardware.configuration;

import android.os.Build;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.internal.system.SystemProperties;

public class LynxConstants {
    public static final int CH_EMBEDDED_MODULE_ADDRESS = 173;
    public static final int DEFAULT_PARENT_MODULE_ADDRESS = 1;
    public static final int DEFAULT_TARGET_POSITION_TOLERANCE = 5;
    public static final int DRAGONBOARD_CH_VERSION = 0;
    private static final String DRAGONBOARD_MODEL = "FIRST Control Hub";
    public static final String EMBEDDED_BHI260AP_IMU_XML_TAG = "ControlHubImuBHI260AP";
    public static final String EMBEDDED_BNO055_IMU_XML_TAG = "LynxEmbeddedIMU";
    public static final int EMBEDDED_IMU_BUS = 0;
    private static final String EMPTY_STRING = "";
    public static final int INDICATOR_LED_BOOT = 4;
    public static final int INDICATOR_LED_INVITE_DIALOG_ACTIVE = 2;
    public static final int INDICATOR_LED_ROBOT_CONTROLLER_ALIVE = 1;
    public static final int INITIAL_MOTOR_PORT = 0;
    public static final int INITIAL_SERVO_PORT = 0;
    public static final int LATENCY_TIMER = 1;
    public static final int MAX_MODULES_DISCOVER = 254;
    public static final int MAX_NUMBER_OF_MODULES = 254;
    public static final int MAX_UNRESERVED_MODULE_ADDRESS = 10;
    public static final int MINIMUM_LEGAL_CH_OS_VERSION_CODE = 6;
    public static final String MINIMUM_LEGAL_CH_OS_VERSION_STRING = "1.1.2";
    public static final int MINIMUM_LEGAL_DH_OS_VERSION_CODE = 21;
    public static final String MINIMUM_LEGAL_DH_OS_VERSION_STRING = "1.1.0";
    public static final int NUMBER_OF_ANALOG_INPUTS = 4;
    public static final int NUMBER_OF_DIGITAL_IOS = 8;
    public static final int NUMBER_OF_I2C_BUSSES = 4;
    public static final int NUMBER_OF_MOTORS = 4;
    public static final int NUMBER_OF_PWM_CHANNELS = 4;
    public static final int NUMBER_OF_SERVO_CHANNELS = 6;
    private static final int ORIGINAL_CH_OS_VERSION_CODE = 1;
    public static final int SERIAL_MODULE_BAUD_RATE = 460800;
    public static final SerialNumber SERIAL_NUMBER_EMBEDDED = SerialNumber.createEmbedded();
    public static final String TAG = "LynxConstants";
    public static final int USB_BAUD_RATE = 460800;
    private static Boolean isControlHub;

    public static boolean isRevControlHub() {
        if (isControlHub == null) {
            isControlHub = Boolean.valueOf(SystemProperties.getBoolean("persist.ftcandroid.serialasusb", false));
        }
        return isControlHub.booleanValue();
    }

    public static int getControlHubVersion() {
        int i = SystemProperties.getInt("ro.ftcandroid.controlhubversion", -1);
        if (i != -1 || !Build.MODEL.equalsIgnoreCase(DRAGONBOARD_MODEL)) {
            return i;
        }
        return 0;
    }

    public static String getControlHubOsVersion() {
        String str = SystemProperties.get("ro.controlhub.os.version", "");
        if ("".equals(str)) {
            return null;
        }
        return str;
    }

    public static int getControlHubOsVersionCode() {
        return SystemProperties.getInt("ro.controlhub.os.versionnum", 1);
    }

    public static boolean controlHubOsVersionIsObsolete() {
        return Device.isRevControlHub() && getControlHubOsVersionCode() < 6;
    }

    public static String getDriverHubOsVersion() {
        String str = SystemProperties.get("ro.driverhub.os.version", "");
        if ("".equals(str)) {
            return null;
        }
        return str;
    }

    public static int getDriverHubOsVersionCode() {
        return SystemProperties.getInt("ro.driverhub.os.versionnum", 0);
    }

    public static boolean isEmbeddedSerialNumber(SerialNumber serialNumber) {
        return SERIAL_NUMBER_EMBEDDED.equals((Object) serialNumber);
    }

    public static boolean autorunRobotController() {
        return SystemProperties.getBoolean("persist.ftcandroid.autorunrc", false);
    }

    public static boolean useIndicatorLEDS() {
        return SystemProperties.getBoolean("persist.ftcandroid.rcuseleds", false);
    }

    public static void validateMotorZ(int i) {
        if (i < 0 || i >= 4) {
            throw new IllegalArgumentException(String.format("invalid motor: %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public static void validatePwmChannelZ(int i) {
        if (i < 0 || i >= 4) {
            throw new IllegalArgumentException(String.format("invalid pwm channel: %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public static void validateServoChannelZ(int i) {
        if (i < 0 || i >= 6) {
            throw new IllegalArgumentException(String.format("invalid servo channel: %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public static void validateI2cBusZ(int i) {
        if (i < 0 || i >= 4) {
            throw new IllegalArgumentException(String.format("invalid i2c bus: %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public static void validateAnalogInputZ(int i) {
        if (i < 0 || i >= 4) {
            throw new IllegalArgumentException(String.format("invalid analog input: %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public static void validateDigitalIOZ(int i) {
        if (i < 0 || i >= 8) {
            throw new IllegalArgumentException(String.format("invalid digital pin: %d", new Object[]{Integer.valueOf(i)}));
        }
    }
}
