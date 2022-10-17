package com.qualcomm.robotcore.hardware;

import com.qualcomm.hardware.lynx.LynxServoController;
import org.firstinspires.ftc.robotcore.system.Misc;

public class PIDCoefficients {

    /* renamed from: d */
    public double f118d;

    /* renamed from: i */
    public double f119i;

    /* renamed from: p */
    public double f120p;

    public String toString() {
        return Misc.formatForUser("%s(p=%f i=%f d=%f)", getClass().getSimpleName(), Double.valueOf(this.f120p), Double.valueOf(this.f119i), Double.valueOf(this.f118d));
    }

    public PIDCoefficients() {
        this.f118d = LynxServoController.apiPositionFirst;
        this.f119i = LynxServoController.apiPositionFirst;
        this.f120p = LynxServoController.apiPositionFirst;
    }

    public PIDCoefficients(double d, double d2, double d3) {
        this.f120p = d;
        this.f119i = d2;
        this.f118d = d3;
    }
}
