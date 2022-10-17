package com.qualcomm.robotcore.hardware.configuration.annotations;

import com.qualcomm.robotcore.hardware.MotorControlAlgorithm;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpansionHubPIDFVelocityParams {
    /* renamed from: D */
    double mo10198D() default 0.0d;

    /* renamed from: F */
    double mo10199F() default 0.0d;

    /* renamed from: I */
    double mo10200I() default 0.0d;

    /* renamed from: P */
    double mo10201P();

    MotorControlAlgorithm algorithm() default MotorControlAlgorithm.PIDF;
}
