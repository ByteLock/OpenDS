package com.qualcomm.robotcore.hardware;

public interface PwmControl {
    PwmRange getPwmRange();

    boolean isPwmEnabled();

    void setPwmDisable();

    void setPwmEnable();

    void setPwmRange(PwmRange pwmRange);

    public static class PwmRange {
        public static final PwmRange defaultRange = new PwmRange(600.0d, 2400.0d);
        public static final double usFrameDefault = 20000.0d;
        public static final double usPulseLowerDefault = 600.0d;
        public static final double usPulseUpperDefault = 2400.0d;
        public final double usFrame;
        public final double usPulseLower;
        public final double usPulseUpper;

        public PwmRange(double d, double d2) {
            this(d, d2, 20000.0d);
        }

        public PwmRange(double d, double d2, double d3) {
            this.usPulseLower = d;
            this.usPulseUpper = d2;
            this.usFrame = d3;
        }

        public boolean equals(Object obj) {
            if (!(obj instanceof PwmRange)) {
                return false;
            }
            PwmRange pwmRange = (PwmRange) obj;
            if (this.usPulseLower == pwmRange.usPulseLower && this.usPulseUpper == pwmRange.usPulseUpper && this.usFrame == pwmRange.usFrame) {
                return true;
            }
            return false;
        }

        public int hashCode() {
            return (Double.valueOf(this.usPulseLower).hashCode() ^ Double.valueOf(this.usPulseUpper).hashCode()) ^ Double.valueOf(this.usFrame).hashCode();
        }
    }
}
