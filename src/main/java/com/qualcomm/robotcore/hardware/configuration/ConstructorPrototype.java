package com.qualcomm.robotcore.hardware.configuration;

import java.lang.reflect.Constructor;

public class ConstructorPrototype {
    Class<?>[] prototypeParameterTypes;

    public ConstructorPrototype(Class<?>... clsArr) {
        this.prototypeParameterTypes = clsArr;
    }

    public boolean matches(Constructor constructor) {
        Class[] parameterTypes = constructor.getParameterTypes();
        if (parameterTypes.length != this.prototypeParameterTypes.length) {
            return false;
        }
        int i = 0;
        while (true) {
            Class<?>[] clsArr = this.prototypeParameterTypes;
            if (i >= clsArr.length) {
                return true;
            }
            if (!parameterTypes[i].equals(clsArr[i])) {
                return false;
            }
            i++;
        }
    }
}
