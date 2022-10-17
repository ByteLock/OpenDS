package com.qualcomm.robotcore.hardware.configuration;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface ExpansionHubMotorControllerVelocityParams {
    /* renamed from: D */
    double mo10132D();

    /* renamed from: I */
    double mo10133I();

    /* renamed from: P */
    double mo10134P();
}
