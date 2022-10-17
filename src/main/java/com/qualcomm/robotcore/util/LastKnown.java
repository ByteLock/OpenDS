package com.qualcomm.robotcore.util;

public class LastKnown<T> {
    protected boolean isValid;
    protected double msFreshness;
    protected ElapsedTime timer;
    protected T value;

    public LastKnown() {
        this(500.0d);
    }

    public LastKnown(double d) {
        this.value = null;
        this.isValid = false;
        this.timer = new ElapsedTime();
        this.msFreshness = d;
    }

    public static <X> LastKnown<X>[] createArray(int i) {
        LastKnown<X>[] lastKnownArr = new LastKnown[i];
        for (int i2 = 0; i2 < i; i2++) {
            lastKnownArr[i2] = new LastKnown<>();
        }
        return lastKnownArr;
    }

    public static <X> void invalidateArray(LastKnown<X>[] lastKnownArr) {
        for (LastKnown<X> invalidate : lastKnownArr) {
            invalidate.invalidate();
        }
    }

    public void invalidate() {
        this.isValid = false;
    }

    public boolean isValid() {
        return this.isValid && this.timer.milliseconds() <= this.msFreshness;
    }

    public T getValue() {
        if (isValid()) {
            return this.value;
        }
        return null;
    }

    public T getNonTimedValue() {
        if (this.isValid) {
            return this.value;
        }
        return null;
    }

    public T getRawValue() {
        return this.value;
    }

    public T setValue(T t) {
        T t2 = this.value;
        this.value = t;
        this.isValid = true;
        if (t == null) {
            invalidate();
        } else {
            this.timer.reset();
        }
        return t2;
    }

    public boolean isValue(T t) {
        if (isValid()) {
            return this.value.equals(t);
        }
        return false;
    }

    public boolean updateValue(T t) {
        if (isValue(t)) {
            return false;
        }
        setValue(t);
        return true;
    }
}
