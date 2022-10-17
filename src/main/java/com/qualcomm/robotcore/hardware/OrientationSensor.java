package com.qualcomm.robotcore.hardware;

import java.util.Set;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.AxesOrder;
import org.firstinspires.ftc.robotcore.external.navigation.AxesReference;
import org.firstinspires.ftc.robotcore.external.navigation.Axis;
import org.firstinspires.ftc.robotcore.external.navigation.Orientation;

public interface OrientationSensor {
    Orientation getAngularOrientation(AxesReference axesReference, AxesOrder axesOrder, AngleUnit angleUnit);

    Set<Axis> getAngularOrientationAxes();
}
