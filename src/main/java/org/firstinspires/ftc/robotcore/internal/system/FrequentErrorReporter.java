package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;

public class FrequentErrorReporter<T> {
    protected T value;

    public FrequentErrorReporter() {
        reset();
    }

    public void reset() {
        this.value = null;
    }

    /* renamed from: aa */
    public synchronized void mo15458aa(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m37aa(str, str2, objArr);
        }
    }

    /* renamed from: vv */
    public synchronized void mo15463vv(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m61vv(str, str2, objArr);
        }
    }

    /* renamed from: dd */
    public synchronized void mo15459dd(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m43dd(str, str2, objArr);
        }
    }

    /* renamed from: ii */
    public synchronized void mo15461ii(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m55ii(str, str2, objArr);
        }
    }

    /* renamed from: ww */
    public synchronized void mo15464ww(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m67ww(str, str2, objArr);
        }
    }

    /* renamed from: ee */
    public synchronized void mo15460ee(T t, String str, String str2, Object... objArr) {
        Assert.assertNotNull(t);
        T t2 = this.value;
        if (t2 == null || !t2.equals(t)) {
            this.value = t;
            RobotLog.m49ee(str, str2, objArr);
        }
    }
}
