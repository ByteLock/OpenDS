package com.qualcomm.hardware.rev;

import com.qualcomm.hardware.stmicroelectronics.VL53L0X;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.annotations.I2cDeviceType;

@DeviceProperties(builtIn = true, compatibleControlSystems = {ControlSystem.REV_HUB}, description = "@string/rev_laser_sensor_name", name = "@string/rev_laser_sensor_name", xmlTag = "REV_VL53L0X_RANGE_SENSOR")
@I2cDeviceType
public class Rev2mDistanceSensor extends VL53L0X {
    public String getDeviceName() {
        return "REV 2M ToF Distance Sensor";
    }

    public Rev2mDistanceSensor(I2cDeviceSynch i2cDeviceSynch) {
        super(i2cDeviceSynch);
    }
}
