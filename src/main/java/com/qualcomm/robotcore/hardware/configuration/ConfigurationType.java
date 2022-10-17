package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.hardware.DeviceManager;

public interface ConfigurationType {

    public enum DeviceFlavor {
        BUILT_IN,
        I2C,
        MOTOR,
        ANALOG_SENSOR,
        SERVO,
        DIGITAL_IO,
        ANALOG_OUTPUT
    }

    public enum DisplayNameFlavor {
        Normal,
        Legacy
    }

    DeviceFlavor getDeviceFlavor();

    String getDisplayName(DisplayNameFlavor displayNameFlavor);

    String getXmlTag();

    String[] getXmlTagAliases();

    boolean isDeprecated();

    boolean isDeviceFlavor(DeviceFlavor deviceFlavor);

    DeviceManager.UsbDeviceType toUSBDeviceType();
}
