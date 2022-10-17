package com.qualcomm.robotcore.hardware;

import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AngularVelocity;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;

public interface Gyroscope {
    AngularVelocity getAngularVelocity(AngleUnit angleUnit);

    Set<Axis> getAngularVelocityAxes();
}
