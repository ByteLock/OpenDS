package com.qualcomm.ftccommon.configuration;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;

public class EditI2cDevicesActivity extends EditI2cDevicesActivityAbstract<DeviceConfiguration> {
    public static final RequestCode requestCode = RequestCode.EDIT_I2C_PORT;

    public String getTag() {
        return getClass().getSimpleName();
    }
}
