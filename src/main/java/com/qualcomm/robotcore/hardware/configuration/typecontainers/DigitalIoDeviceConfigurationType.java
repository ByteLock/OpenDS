package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class DigitalIoDeviceConfigurationType extends InstantiableUserConfigurationType {
    private static final ConstructorPrototype ctorDigitalDevice = new ConstructorPrototype(DigitalChannelController.class, Integer.TYPE);

    public DigitalIoDeviceConfigurationType(Class<? extends HardwareDevice> cls, String str) {
        super(cls, ConfigurationType.DeviceFlavor.DIGITAL_IO, str, new ConstructorPrototype[]{ctorDigitalDevice});
    }

    public DigitalIoDeviceConfigurationType() {
        super(ConfigurationType.DeviceFlavor.DIGITAL_IO);
    }

    public HardwareDevice createInstance(DigitalChannelController digitalChannelController, int i) {
        try {
            Constructor<HardwareDevice> findMatch = findMatch(ctorDigitalDevice);
            if (findMatch != null) {
                return findMatch.newInstance(new Object[]{digitalChannelController, Integer.valueOf(i)});
            }
            throw new RuntimeException("internal error: unable to locate constructor for user device type " + getName());
        } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
            handleConstructorExceptions(e);
            return null;
        }
    }

    private Object writeReplace() {
        return new UserConfigurationType.SerializationProxy(this);
    }
}
