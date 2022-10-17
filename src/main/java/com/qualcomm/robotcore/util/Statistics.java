package com.qualcomm.robotcore.util;

import com.qualcomm.hardware.lynx.LynxServoController;

public class Statistics {

    /* renamed from: m2 */
    double f142m2;
    double mean;

    /* renamed from: n */
    int f143n;

    public Statistics() {
        clear();
    }

    public int getCount() {
        return this.f143n;
    }

    public double getMean() {
        return this.mean;
    }

    public double getVariance() {
        return this.f142m2 / ((double) (this.f143n - 1));
    }

    public double getStandardDeviation() {
        return Math.sqrt(getVariance());
    }

    public void clear() {
        this.f143n = 0;
        this.mean = LynxServoController.apiPositionFirst;
        this.f142m2 = LynxServoController.apiPositionFirst;
    }

    public void add(double d) {
        int i = this.f143n + 1;
        this.f143n = i;
        double d2 = this.mean;
        double d3 = d - d2;
        double d4 = d2 + (d3 / ((double) i));
        this.mean = d4;
        this.f142m2 += d3 * (d - d4);
    }

    public void remove(double d) {
        int i = this.f143n;
        int i2 = i - 1;
        if (i2 == 0) {
            clear();
            return;
        }
        double d2 = this.mean;
        double d3 = d - d2;
        double d4 = (double) i2;
        this.f142m2 -= ((((double) i) * d3) / d4) * d3;
        this.mean = ((d2 * ((double) i)) - d) / d4;
        this.f143n = i2;
    }
}
