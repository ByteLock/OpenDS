package org.firstinspires.ftc.robotcore.internal.system;

import org.firstinspires.ftc.robotcore.internal.system.RefCounted;

public class CloseableRefCounted extends RefCounted {
    protected boolean closeCalled = false;
    protected int closeCount = 0;

    /* access modifiers changed from: protected */
    public void doClose() {
    }

    /* access modifiers changed from: protected */
    public void postClose() {
    }

    /* access modifiers changed from: protected */
    public void preClose() {
    }

    protected CloseableRefCounted() {
    }

    protected CloseableRefCounted(RefCounted.TraceLevel traceLevel) {
        super(traceLevel);
    }

    /* access modifiers changed from: protected */
    public final boolean ctorOnlyCloseNeededToDestruct() {
        boolean z;
        synchronized (this.lock) {
            z = true;
            if (this.closeCount != 1 || this.refCount.get() != 1) {
                z = false;
            }
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public final void enableClose() {
        synchronized (this.lock) {
            if (!this.closeCalled) {
                int i = this.closeCount;
                this.closeCount = i + 1;
                if (i == 0) {
                    addRef();
                }
            } else {
                throw new IllegalStateException("enableClose() on an already closed object: " + this);
            }
        }
    }

    /* access modifiers changed from: protected */
    public final void enableOnlyClose() {
        enableClose();
        releaseRef();
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
                    releaseRef();
                }
            }
        }
    }
}
