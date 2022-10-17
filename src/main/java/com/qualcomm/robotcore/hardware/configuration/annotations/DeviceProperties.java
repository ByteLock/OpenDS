package com.qualcomm.robotcore.hardware.configuration.annotations;

import com.qualcomm.robotcore.hardware.ControlSystem;
import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Documented
@Target({ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
public @interface DeviceProperties {
    boolean builtIn() default false;

    ControlSystem[] compatibleControlSystems() default {ControlSystem.REV_HUB};

    String description() default "";

    String name();

    String xmlTag();

    String[] xmlTagAliases() default {};
}
