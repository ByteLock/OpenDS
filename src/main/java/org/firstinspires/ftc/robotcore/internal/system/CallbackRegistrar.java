package org.firstinspires.ftc.robotcore.internal.system;

import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.util.Iterator;
import org.firstinspires.ftc.robotcore.external.Consumer;

public class CallbackRegistrar<T> {
    protected final WeakReferenceSet<T> callbacks = new WeakReferenceSet<>();

    /* access modifiers changed from: protected */
    public Object getCallbacksLock() {
        return this.callbacks;
    }

    public void registerCallback(T t) {
        synchronized (getCallbacksLock()) {
            this.callbacks.add(t);
        }
    }

    public void unregisterCallback(T t) {
        synchronized (getCallbacksLock()) {
            this.callbacks.remove(t);
        }
    }

    public void callbacksDo(Consumer<T> consumer) {
        synchronized (getCallbacksLock()) {
            Iterator<T> it = this.callbacks.iterator();
            while (it.hasNext()) {
                consumer.accept(it.next());
            }
        }
    }
}
