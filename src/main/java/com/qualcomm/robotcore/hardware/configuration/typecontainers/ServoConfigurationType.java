package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.ConstructorPrototype;
import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.annotations.ServoType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

public final class ServoConfigurationType extends InstantiableUserConfigurationType {
    private static final ConstructorPrototype ctorServo = new ConstructorPrototype(ServoController.class, Integer.TYPE);
    private static final ConstructorPrototype ctorServoEx = new ConstructorPrototype(ServoControllerEx.class, Integer.TYPE);
    @Expose
    private ServoFlavor servoFlavor;
    @Expose
    private double usFrame;
    @Expose
    private double usPulseLower;
    @Expose
    private double usPulseUpper;

    public ServoConfigurationType(Class<? extends HardwareDevice> cls, String str) {
        super(cls, ConfigurationType.DeviceFlavor.SERVO, str, new ConstructorPrototype[]{ctorServo, ctorServoEx});
    }

    public ServoConfigurationType() {
        super(ConfigurationType.DeviceFlavor.SERVO);
    }

    public static ServoConfigurationType getStandardServoType() {
        return ConfigurationTypeManager.getInstance().getStandardServoType();
    }

    public void processAnnotation(ServoType servoType) {
        if (servoType != null) {
            this.servoFlavor = servoType.flavor();
            this.usPulseLower = servoType.usPulseLower();
            this.usPulseUpper = servoType.usPulseUpper();
            this.usFrame = servoType.usPulseFrameRate();
        }
    }

    public ServoFlavor getServoFlavor() {
        return this.servoFlavor;
    }

    public double getUsPulseLower() {
        return this.usPulseLower;
    }

    public double getUsPulseUpper() {
        return this.usPulseUpper;
    }

    public double getUsFrame() {
        return this.usFrame;
    }

    public boolean classMustBeInstantiable() {
        return this.servoFlavor == ServoFlavor.CUSTOM;
    }

    public HardwareDevice createInstanceRev(ServoControllerEx servoControllerEx, int i) {
        if (this.servoFlavor == ServoFlavor.CUSTOM) {
            try {
                Constructor<HardwareDevice> findMatch = findMatch(ctorServoEx);
                if (findMatch != null) {
                    servoControllerEx.setServoType(i, this);
                    return findMatch.newInstance(new Object[]{servoControllerEx, Integer.valueOf(i)});
                }
                throw new RuntimeException("internal error: unable to locate constructor for user device type " + getName());
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                handleConstructorExceptions(e);
                return null;
            }
        } else {
            throw new RuntimeException("Can't create instance of noninstantiable servo type " + this.name);
        }
    }

    public HardwareDevice createInstanceMr(ServoController servoController, int i) {
        if (this.servoFlavor == ServoFlavor.CUSTOM) {
            try {
                Constructor<HardwareDevice> findMatch = findMatch(ctorServo);
                if (findMatch != null) {
                    return findMatch.newInstance(new Object[]{servoController, Integer.valueOf(i)});
                }
                throw new RuntimeException("internal error: unable to locate constructor for user device type " + getName());
            } catch (IllegalAccessException | InstantiationException | InvocationTargetException e) {
                handleConstructorExceptions(e);
                return null;
            }
        } else {
            throw new RuntimeException("Can't create instance of noninstantiable servo type " + this.name);
        }
    }

    private Object writeReplace() {
        return new UserConfigurationType.SerializationProxy(this);
    }
}
