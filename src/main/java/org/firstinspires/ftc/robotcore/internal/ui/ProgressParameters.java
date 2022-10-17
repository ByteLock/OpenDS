package org.firstinspires.ftc.robotcore.internal.p013ui;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.ProgressParameters */
public class ProgressParameters {
    public int cur;
    public int max;

    public ProgressParameters() {
        this.cur = 0;
        this.max = 100;
    }

    public ProgressParameters(int i, int i2) {
        this.cur = i;
        this.max = i2;
    }

    public double fractionComplete() {
        return ((double) this.cur) / ((double) this.max);
    }

    public static ProgressParameters fromFraction(double d, int i) {
        return new ProgressParameters((int) Math.round(d * ((double) i)), i);
    }

    public static ProgressParameters fromFraction(double d) {
        return fromFraction(d, 100);
    }
}
