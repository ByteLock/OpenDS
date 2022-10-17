package com.qualcomm.robotcore.hardware.configuration.annotations;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface MotorType {
    double achieveableMaxRPMFraction() default 0.85d;

    double gearing();

    double maxRPM();

    Rotation orientation() default Rotation.f212CW;

    double ticksPerRev();
}
