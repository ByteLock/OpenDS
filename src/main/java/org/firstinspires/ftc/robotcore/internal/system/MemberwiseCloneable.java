package org.firstinspires.ftc.robotcore.internal.system;

public abstract class MemberwiseCloneable<T> implements Cloneable {
    /* access modifiers changed from: protected */
    public T memberwiseClone() {
        try {
            return super.clone();
        } catch (CloneNotSupportedException unused) {
            throw AppUtil.getInstance().unreachable();
        }
    }
}
