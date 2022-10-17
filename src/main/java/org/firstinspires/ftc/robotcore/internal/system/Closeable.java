package org.firstinspires.ftc.robotcore.internal.system;

public class Closeable {
    protected boolean closeCalled = false;
    protected int closeCount = 1;
    protected final Object lock = new Object();

    /* access modifiers changed from: protected */
    public final boolean ctorOnlyCloseNeededToDestruct() {
        return true;
    }

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public void postClose() {
    }

    /* access modifiers changed from: protected */
    public void preClose() {
    }

    protected Closeable() {
    }

    /* access modifiers changed from: protected */
    public final void enableOnlyClose() {
        Assert.assertTrue(ctorOnlyCloseNeededToDestruct());
    }

    public final void close() {
        synchronized (this.lock) {
            int i = this.closeCount;
            if (i != 0) {
                int i2 = i - 1;
                this.closeCount = i2;
                if (i2 == 0) {
                    this.closeCalled = true;
                    preClose();
                    doClose();
                    postClose();
                }
            }
        }
    }
}
