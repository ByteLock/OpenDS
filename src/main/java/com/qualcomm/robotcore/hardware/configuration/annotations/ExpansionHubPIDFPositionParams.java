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
public @interface ExpansionHubPIDFPositionParams {
    /* renamed from: P */
    double mo10196P();

    MotorControlAlgorithm algorithm() default MotorControlAlgorithm.PIDF;
}
