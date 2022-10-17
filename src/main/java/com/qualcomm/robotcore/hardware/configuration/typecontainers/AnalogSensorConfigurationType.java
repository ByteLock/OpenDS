package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class AnalogSensorConfigurationType extends InstantiableUserConfigurationType {
    private static final ConstructorPrototype ctorAnalogSensor = new ConstructorPrototype(AnalogInputController.class, Integer.TYPE);

    public AnalogSensorConfigurationType(Class<? extends HardwareDevice> cls, String str) {
        super(cls, ConfigurationType.DeviceFlavor.ANALOG_SENSOR, str, new ConstructorPrototype[]{ctorAnalogSensor});
    }

    public AnalogSensorConfigurationType() {
        super(ConfigurationType.DeviceFlavor.ANALOG_SENSOR);
    }

    public HardwareDevice createInstance(AnalogInputController analogInputController, int i) {
        try {
            Constructor<HardwareDevice> findMatch = findMatch(ctorAnalogSensor);
            if (findMatch != null) {
                return findMatch.newInstance(new Object[]{analogInputController, Integer.valueOf(i)});
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
