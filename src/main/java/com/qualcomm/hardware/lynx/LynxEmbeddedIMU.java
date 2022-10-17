package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DeviceProperties(builtIn = true, compatibleControlSystems = {ControlSystem.REV_HUB}, description = "@string/lynx_embedded_imu_description", name = "@string/lynx_embedded_bno055_imu_name", xmlTag = "LynxEmbeddedIMU")
@I2cDeviceType
public class LynxEmbeddedIMU extends BNO055IMUImpl {
    public LynxEmbeddedIMU(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch);
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.lynx_embedded_bno055_imu_name);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }
}
