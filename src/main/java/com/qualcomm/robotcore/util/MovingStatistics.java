package com.qualcomm.robotcore.util;

import java.util.LinkedList;
import java.util.Queue;

public class MovingStatistics {
    final int capacity;
    final Queue<Double> samples;
    final Statistics statistics;

    public MovingStatistics(int i) {
        if (i > 0) {
            this.statistics = new Statistics();
            this.capacity = i;
            this.samples = new LinkedList();
            return;
        }
        throw new IllegalArgumentException("MovingStatistics capacity must be positive");
    }

    public int getCount() {
        return this.statistics.getCount();
    }

    public double getMean() {
        return this.statistics.getMean();
    }

    public double getVariance() {
        return this.statistics.getVariance();
    }

    public double getStandardDeviation() {
        return this.statistics.getStandardDeviation();
    }

    public void clear() {
        this.statistics.clear();
        this.samples.clear();
    }

    public void add(double d) {
        this.statistics.add(d);
        this.samples.add(Double.valueOf(d));
        if (this.samples.size() > this.capacity) {
            this.statistics.remove(this.samples.remove().doubleValue());
        }
    }
}
