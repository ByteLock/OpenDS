package com.qualcomm.robotcore.util;

import java.util.LinkedList;
import java.util.Queue;

public class RollingAverage {
    public static final int DEFAULT_SIZE = 100;
    private final Queue<Integer> queue = new LinkedList();
    private int size;
    private long total;

    public RollingAverage() {
        resize(100);
    }

    public RollingAverage(int i) {
        resize(i);
    }

    public int size() {
        return this.size;
    }

    public void resize(int i) {
        this.size = i;
        this.queue.clear();
    }

    public void addNumber(int i) {
        if (this.queue.size() >= this.size) {
            this.total -= (long) this.queue.remove().intValue();
        }
        this.queue.add(Integer.valueOf(i));
        this.total += (long) i;
    }

    public int getAverage() {
        if (this.queue.isEmpty()) {
            return 0;
        }
        return (int) (this.total / ((long) this.queue.size()));
    }

    public void reset() {
        this.queue.clear();
    }
}
