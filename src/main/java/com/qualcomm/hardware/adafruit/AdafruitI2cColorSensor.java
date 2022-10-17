package com.qualcomm.hardware.adafruit;

import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.ams.AMSColorSensor;
import com.qualcomm.hardware.ams.AMSColorSensorImpl;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class AdafruitI2cColorSensor extends AMSColorSensorImpl {
    public AdafruitI2cColorSensor(I2cDeviceSynchSimple i2cDeviceSynchSimple) {
        super(AMSColorSensor.Parameters.createForTCS34725(), i2cDeviceSynchSimple, true);
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0660R.string.configTypeAdafruitColorSensor);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Adafruit;
    }
}
