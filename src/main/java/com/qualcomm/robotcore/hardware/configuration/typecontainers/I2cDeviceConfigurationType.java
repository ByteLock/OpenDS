package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.I2cDevice;
import com.qualcomm.robotcore.hardware.I2cDeviceSynch;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.RobotCoreLynxModule;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.I2cSensor;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import com.qualcomm.robotcore.util.ClassUtil;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import org.firstinspires.ftc.robotcore.external.Func;

public final class I2cDeviceConfigurationType extends InstantiableUserConfigurationType {
    private static final ConstructorPrototype[] allowableConstructorPrototypes;
    private static final ConstructorPrototype ctorI2cDevice;
    private static final ConstructorPrototype ctorI2cDeviceSynch;
    private static final ConstructorPrototype ctorI2cDeviceSynchSimple;

    static {
        ConstructorPrototype constructorPrototype = new ConstructorPrototype(I2cDeviceSynchSimple.class);
        ctorI2cDeviceSynchSimple = constructorPrototype;
        ConstructorPrototype constructorPrototype2 = new ConstructorPrototype(I2cDeviceSynch.class);
        ctorI2cDeviceSynch = constructorPrototype2;
        ConstructorPrototype constructorPrototype3 = new ConstructorPrototype(I2cDevice.class);
        ctorI2cDevice = constructorPrototype3;
        allowableConstructorPrototypes = new ConstructorPrototype[]{constructorPrototype, constructorPrototype2, constructorPrototype3};
    }

    public I2cDeviceConfigurationType(Class<? extends HardwareDevice> cls, String str) {
        super(cls, ConfigurationType.DeviceFlavor.I2C, str, allowableConstructorPrototypes);
    }

    public static I2cDeviceConfigurationType getLynxEmbeddedBNO055ImuType() {
        return (I2cDeviceConfigurationType) ConfigurationTypeManager.getInstance().configurationTypeFromTag(LynxConstants.EMBEDDED_BNO055_IMU_XML_TAG);
    }

    public static I2cDeviceConfigurationType getLynxEmbeddedBHI260APImuType() {
        return (I2cDeviceConfigurationType) ConfigurationTypeManager.getInstance().configurationTypeFromTag(LynxConstants.EMBEDDED_BHI260AP_IMU_XML_TAG);
    }

    public I2cDeviceConfigurationType() {
        super(ConfigurationType.DeviceFlavor.I2C);
    }

    public void processAnnotation(I2cSensor i2cSensor) {
        if (i2cSensor != null) {
            if (this.name.isEmpty()) {
                this.name = ClassUtil.decodeStringRes(i2cSensor.name().trim());
            }
            this.description = ClassUtil.decodeStringRes(i2cSensor.description());
        }
    }

    public HardwareDevice createInstance(RobotCoreLynxModule robotCoreLynxModule, Func<I2cDeviceSynchSimple> func, Func<I2cDeviceSynch> func2) {
        try {
            Constructor<HardwareDevice> findMatch = findMatch(ctorI2cDeviceSynchSimple);
            if (findMatch != null) {
                return findMatch.newInstance(new Object[]{func.value()});
            }
            Constructor<HardwareDevice> findMatch2 = findMatch(ctorI2cDeviceSynch);
            if (findMatch2 != null) {
                return findMatch2.newInstance(new Object[]{func2.value()});
            }
            throw new RuntimeException("internal error: unable to locate constructor for user sensor type " + getName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            handleConstructorExceptions(e);
            return null;
        }
    }

    private Object writeReplace() {
        return new UserConfigurationType.SerializationProxy(this);
    }
}
