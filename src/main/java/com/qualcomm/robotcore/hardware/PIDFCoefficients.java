package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import org.firstinspires.ftc.robotcore.internal.system.Misc;

public class PIDFCoefficients {
    public MotorControlAlgorithm algorithm;

    /* renamed from: d */
    public double f121d;

    /* renamed from: f */
    public double f122f;

    /* renamed from: i */
    public double f123i;

    /* renamed from: p */
    public double f124p;

    public String toString() {
        return Misc.formatForUser("%s(p=%f i=%f d=%f f=%f alg=%s)", getClass().getSimpleName(), Double.valueOf(this.f124p), Double.valueOf(this.f123i), Double.valueOf(this.f121d), Double.valueOf(this.f122f), this.algorithm);
    }

    public PIDFCoefficients() {
        this.f122f = LynxServoController.apiPositionFirst;
        this.f121d = LynxServoController.apiPositionFirst;
        this.f123i = LynxServoController.apiPositionFirst;
        this.f124p = LynxServoController.apiPositionFirst;
        this.algorithm = MotorControlAlgorithm.PIDF;
    }

    public PIDFCoefficients(double d, double d2, double d3, double d4, MotorControlAlgorithm motorControlAlgorithm) {
        this.f124p = d;
        this.f123i = d2;
        this.f121d = d3;
        this.f122f = d4;
        this.algorithm = motorControlAlgorithm;
    }

    public PIDFCoefficients(double d, double d2, double d3, double d4) {
        this(d, d2, d3, d4, MotorControlAlgorithm.PIDF);
    }

    public PIDFCoefficients(PIDFCoefficients pIDFCoefficients) {
        this.f124p = pIDFCoefficients.f124p;
        this.f123i = pIDFCoefficients.f123i;
        this.f121d = pIDFCoefficients.f121d;
        this.f122f = pIDFCoefficients.f122f;
        this.algorithm = pIDFCoefficients.algorithm;
    }

    public PIDFCoefficients(PIDCoefficients pIDCoefficients) {
        this.f124p = pIDCoefficients.f120p;
        this.f123i = pIDCoefficients.f119i;
        this.f121d = pIDCoefficients.f118d;
        this.f122f = LynxServoController.apiPositionFirst;
        this.algorithm = MotorControlAlgorithm.LegacyPID;
    }
}
