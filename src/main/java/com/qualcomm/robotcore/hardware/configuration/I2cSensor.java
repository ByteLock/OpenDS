package com.qualcomm.robotcore.hardware.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Deprecated
@Retention(RetentionPolicy.RUNTIME)
public @interface I2cSensor {
    String description() default "an I2c sensor";

    String name() default "";

    String xmlTag() default "";
}
