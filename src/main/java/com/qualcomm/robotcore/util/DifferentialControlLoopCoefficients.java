package com.qualcomm.robotcore.util;

import com.qualcomm.hardware.lynx.LynxServoController;

public class DifferentialControlLoopCoefficients {

    /* renamed from: d */
    public double f139d;

    /* renamed from: i */
    public double f140i;

    /* renamed from: p */
    public double f141p;

    public DifferentialControlLoopCoefficients() {
        this.f141p = LynxServoController.apiPositionFirst;
        this.f140i = LynxServoController.apiPositionFirst;
        this.f139d = LynxServoController.apiPositionFirst;
    }

    public DifferentialControlLoopCoefficients(double d, double d2, double d3) {
        this.f141p = d;
        this.f140i = d2;
        this.f139d = d3;
    }
}
