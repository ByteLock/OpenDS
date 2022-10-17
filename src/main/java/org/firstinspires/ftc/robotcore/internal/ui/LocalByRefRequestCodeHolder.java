package org.firstinspires.ftc.robotcore.internal.p013ui;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.LocalByRefRequestCodeHolder */
public class LocalByRefRequestCodeHolder<T> {
    public static final String TAG = "LocalByRefRequestCodeHolder";
    private static final Map<Integer, LocalByRefRequestCodeHolder> mapRequestCodeToHolder = new ConcurrentHashMap();
    protected static final AtomicInteger requestCodeGenerator = new AtomicInteger(1000000);
    protected int actualRequestCode;
    protected T target;
    protected int userRequestCode;

    public LocalByRefRequestCodeHolder(int i, T t) {
        int andIncrement = requestCodeGenerator.getAndIncrement();
        this.actualRequestCode = andIncrement;
        this.userRequestCode = i;
        this.target = t;
        mapRequestCodeToHolder.put(Integer.valueOf(andIncrement), this);
    }

    public int getActualRequestCode() {
        return this.actualRequestCode;
    }

    public int getUserRequestCode() {
        return this.userRequestCode;
    }

    public T getTargetAndForget() {
        mapRequestCodeToHolder.remove(Integer.valueOf(this.actualRequestCode));
        return this.target;
    }

    public static LocalByRefRequestCodeHolder from(int i) {
        return mapRequestCodeToHolder.get(Integer.valueOf(i));
    }
}
