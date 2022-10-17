package com.qualcomm.hardware.adafruit;

import com.qualcomm.hardware.bosch.BNO055IMUImpl;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;
import org.firstinspires.ftc.robotcore.system.AppUtil;

@DeviceProperties(builtIn = true, description = "@string/adafruit_imu_description", name = "@string/adafruit_imu_name", xmlTag = "AdafruitBNO055IMU")
@I2cDeviceType
public class AdafruitBNO055IMU extends BNO055IMUImpl {
    public AdafruitBNO055IMU(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch);
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.adafruit_imu_name);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Adafruit;
    }
}
