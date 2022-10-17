package com.qualcomm.robotcore.hardware.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpansionHubMotorControllerPositionParams {
    /* renamed from: D */
    double mo10129D();

    /* renamed from: I */
    double mo10130I();

    /* renamed from: P */
    double mo10131P();
}
