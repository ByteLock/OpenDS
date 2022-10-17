package com.qualcomm.robotcore.hardware;

import android.graphics.Color;
import androidx.core.view.ViewCompat;
import java.util.Collection;
import java.util.Locale;
import java.util.concurrent.TimeUnit;

public interface Blinker {
    int getBlinkerPatternMaxLength();

    Collection<Step> getPattern();

    boolean patternStackNotEmpty();

    boolean popPattern();

    void pushPattern(Collection<Step> collection);

    void setConstant(int i);

    void setPattern(Collection<Step> collection);

    void stopBlinking();

    public static class Step {
        protected int color;
        protected int msDuration;

        public Step() {
            this.color = 0;
            this.msDuration = 0;
        }

        public Step(int i, long j, TimeUnit timeUnit) {
            this.msDuration = 0;
            this.color = i & ViewCompat.MEASURED_SIZE_MASK;
            setDuration(j, timeUnit);
        }

        public static Step nullStep() {
            return new Step();
        }

        public boolean equals(Object obj) {
            if (obj instanceof Step) {
                return equals((Step) obj);
            }
            return false;
        }

        public boolean equals(Step step) {
            return this.color == step.color && this.msDuration == step.msDuration;
        }

        public int hashCode() {
            return ((this.color << 5) | this.msDuration) ^ 11994;
        }

        public boolean isLit() {
            return (Color.red(this.color) == 0 && Color.green(this.color) == 0 && Color.blue(this.color) == 0) ? false : true;
        }

        public void setLit(boolean z) {
            setColor(z ? -1 : ViewCompat.MEASURED_STATE_MASK);
        }

        public int getColor() {
            return this.color;
        }

        public void setColor(int i) {
            this.color = i;
        }

        public int getDurationMs() {
            return this.msDuration;
        }

        public void setDuration(long j, TimeUnit timeUnit) {
            this.msDuration = (int) timeUnit.toMillis(j);
        }

        public String toString() {
            return String.format(Locale.US, "Step(color=%d, msDuration=%d)", new Object[]{Integer.valueOf(this.color), Integer.valueOf(this.msDuration)});
        }
    }
}
