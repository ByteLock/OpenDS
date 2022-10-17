package com.qualcomm.robotcore.hardware.configuration.annotations;

import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ServoType {
    ServoFlavor flavor();

    double usPulseFrameRate() default 20000.0d;

    double usPulseLower() default 600.0d;

    double usPulseUpper() default 2400.0d;
}
