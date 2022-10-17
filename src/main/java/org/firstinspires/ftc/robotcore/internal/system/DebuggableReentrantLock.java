package org.firstinspires.ftc.robotcore.internal.system;

import java.util.concurrent.locks.ReentrantLock;

public class DebuggableReentrantLock extends ReentrantLock {
    public DebuggableReentrantLock() {
    }

    public DebuggableReentrantLock(boolean z) {
        super(z);
    }

    public Thread getOwner() {
        return super.getOwner();
    }
}
