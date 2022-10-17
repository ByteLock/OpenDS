package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.internal.system.RefCounted;

public abstract class DestructOnFinalize<ParentType extends RefCounted> extends RefCounted implements Finalizable {
    protected Finalizer finalizer = Finalizer.forTarget(this);
    protected boolean inFinalize = false;
    protected boolean ownParentRef = false;
    protected ParentType parent = null;

    protected DestructOnFinalize() {
    }

    protected DestructOnFinalize(RefCounted.TraceLevel traceLevel) {
        super(traceLevel);
    }

    /* access modifiers changed from: protected */
    public void setParent(ParentType parenttype) {
        synchronized (this.lock) {
            ParentType parenttype2 = this.parent;
            if (parenttype2 != parenttype) {
                if (parenttype2 != null) {
                    parenttype2.releaseRef();
                    this.ownParentRef = false;
                }
                if (parenttype != null) {
                    parenttype.addRef();
                    this.ownParentRef = true;
                }
                this.parent = parenttype;
            }
        }
    }

    /* access modifiers changed from: protected */
    public ParentType getParent() {
        return this.parent;
    }

    public void doFinalize() {
        synchronized (this.lock) {
            this.inFinalize = true;
            try {
                doLockAndDestruct();
            } finally {
                this.inFinalize = false;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void suppressFinalize() {
        synchronized (this.lock) {
            Finalizer finalizer2 = this.finalizer;
            if (finalizer2 != null) {
                finalizer2.dispose();
                this.finalizer = null;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void preDestructor() {
        if (traceDtor() && this.inFinalize) {
            RobotLog.m61vv(getTag(), "finalize(%s)", getTraceIdentifier());
        }
        suppressFinalize();
        super.preDestructor();
    }

    /* access modifiers changed from: protected */
    public void destructor() {
        if (this.ownParentRef) {
            this.parent.releaseRef();
            this.ownParentRef = false;
        }
        super.destructor();
    }

    /* access modifiers changed from: protected */
    public boolean traceDtor() {
        return super.traceDtor() || this.inFinalize;
    }
}
