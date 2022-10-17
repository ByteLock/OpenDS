package org.firstinspires.ftc.robotcore.internal.system;

import java.util.Stack;

public class Finalizer {
    static final Stack<Finalizer> cache = new Stack<>();
    static int cacheSizeMax = 50;
    Finalizable target;

    public static String getTag() {
        return "Finalizer";
    }

    static Finalizer forTarget(Finalizable finalizable) {
        Finalizer finalizer;
        Stack<Finalizer> stack = cache;
        synchronized (stack) {
            finalizer = stack.isEmpty() ? new Finalizer() : stack.pop();
            finalizer.target = finalizable;
        }
        return finalizer;
    }

    public void dispose() {
        Stack<Finalizer> stack = cache;
        synchronized (stack) {
            if (this.target != null) {
                this.target = null;
                if (stack.size() < cacheSizeMax) {
                    stack.push(this);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public void finalize() throws Throwable {
        Finalizable finalizable = this.target;
        if (finalizable != null) {
            finalizable.doFinalize();
        }
        dispose();
        super.finalize();
    }
}
