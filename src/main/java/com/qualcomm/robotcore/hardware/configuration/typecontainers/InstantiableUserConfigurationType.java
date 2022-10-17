package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import com.qualcomm.robotcore.util.ClassUtil;
import com.qualcomm.robotcore.util.RobotLog;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.LinkedList;
import java.util.List;

public abstract class InstantiableUserConfigurationType extends UserConfigurationType {
    private Class<? extends HardwareDevice> clazz;
    private List<Constructor> constructors;

    public boolean classMustBeInstantiable() {
        return true;
    }

    protected InstantiableUserConfigurationType(Class cls, ConfigurationType.DeviceFlavor deviceFlavor, String str, ConstructorPrototype[] constructorPrototypeArr) {
        super(cls, deviceFlavor, str);
        this.clazz = cls;
        this.constructors = findUsableConstructors(constructorPrototypeArr);
    }

    protected InstantiableUserConfigurationType(ConfigurationType.DeviceFlavor deviceFlavor) {
        super(deviceFlavor);
    }

    public void processAnnotation(DeviceProperties deviceProperties) {
        super.processAnnotation(deviceProperties);
    }

    private List<Constructor> findUsableConstructors(ConstructorPrototype[] constructorPrototypeArr) {
        LinkedList linkedList = new LinkedList();
        for (Constructor next : ClassUtil.getDeclaredConstructors(getClazz())) {
            if ((next.getModifiers() & 1) == 1) {
                int length = constructorPrototypeArr.length;
                int i = 0;
                while (true) {
                    if (i >= length) {
                        break;
                    } else if (constructorPrototypeArr[i].matches(next)) {
                        linkedList.add(next);
                        break;
                    } else {
                        i++;
                    }
                }
            }
        }
        return linkedList;
    }

    /* access modifiers changed from: protected */
    public final Constructor<HardwareDevice> findMatch(ConstructorPrototype constructorPrototype) {
        for (Constructor<HardwareDevice> next : this.constructors) {
            if (constructorPrototype.matches(next)) {
                return next;
            }
        }
        return null;
    }

    public final boolean hasConstructors() {
        return this.constructors.size() > 0;
    }

    public final Class<? extends HardwareDevice> getClazz() {
        return this.clazz;
    }

    /* access modifiers changed from: protected */
    public final void handleConstructorExceptions(Exception exc) {
        RobotLog.m59v("Creating user sensor %s failed: ", getName());
        RobotLog.logStackTrace(exc);
        if (exc instanceof InvocationTargetException) {
            Throwable targetException = ((InvocationTargetException) exc).getTargetException();
            if (targetException != null) {
                RobotLog.m46e("InvocationTargetException caused by: ");
                RobotLog.logStackTrace(targetException);
            }
            if (!isBuiltIn()) {
                throw new RuntimeException("Constructor of device type " + getName() + " threw an exception. See log.");
            }
        }
        if (!isBuiltIn()) {
            throw new RuntimeException("Internal error while creating device of type " + getName() + ". See log.");
        }
    }

    private Object writeReplace() {
        return new UserConfigurationType.SerializationProxy(this);
    }
}
