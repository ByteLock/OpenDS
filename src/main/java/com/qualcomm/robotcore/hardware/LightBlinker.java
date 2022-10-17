package com.qualcomm.robotcore.hardware;

import androidx.appcompat.widget.ActivityChooserView;
import androidx.core.view.ViewCompat;
import com.qualcomm.robotcore.hardware.Blinker;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Deque;
import java.util.NoSuchElementException;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class LightBlinker implements Blinker {
    public static final String TAG = "LightBlinker";
    protected ArrayList<Blinker.Step> currentSteps = new ArrayList<>();
    protected ScheduledFuture<?> future = null;
    protected final SwitchableLight light;
    protected int nextStep;
    protected Deque<ArrayList<Blinker.Step>> previousSteps = new ArrayDeque();

    public int getBlinkerPatternMaxLength() {
        return ActivityChooserView.ActivityChooserViewAdapter.MAX_ACTIVITY_COUNT_UNLIMITED;
    }

    public LightBlinker(SwitchableLight switchableLight) {
        this.light = switchableLight;
    }

    public void setConstant(int i) {
        Blinker.Step step = new Blinker.Step(i, 1, TimeUnit.SECONDS);
        ArrayList arrayList = new ArrayList();
        arrayList.add(step);
        setPattern(arrayList);
    }

    public void stopBlinking() {
        setConstant(ViewCompat.MEASURED_STATE_MASK);
    }

    public synchronized void pushPattern(Collection<Blinker.Step> collection) {
        this.previousSteps.push(this.currentSteps);
        setPattern(collection);
    }

    public synchronized boolean patternStackNotEmpty() {
        return this.previousSteps.size() > 0;
    }

    public synchronized boolean popPattern() {
        try {
            setPattern(this.previousSteps.pop());
        } catch (NoSuchElementException unused) {
            setPattern((Collection<Blinker.Step>) null);
            return false;
        }
        return true;
    }

    public synchronized void setPattern(Collection<Blinker.Step> collection) {
        if (isCurrentPattern(collection)) {
            stop();
            if (collection != null) {
                if (collection.size() != 0) {
                    this.currentSteps = new ArrayList<>(collection);
                    if (collection.size() == 1) {
                        this.light.enableLight(this.currentSteps.get(0).isLit());
                    } else {
                        this.nextStep = 0;
                        scheduleNext();
                    }
                }
            }
            this.currentSteps = new ArrayList<>();
            this.light.enableLight(false);
        }
    }

    /* access modifiers changed from: protected */
    public boolean isCurrentPattern(Collection<Blinker.Step> collection) {
        if (collection.size() != this.currentSteps.size()) {
            return false;
        }
        int i = 0;
        for (Blinker.Step equals : collection) {
            int i2 = i + 1;
            if (!equals.equals(this.currentSteps.get(i))) {
                return false;
            }
            i = i2;
        }
        return true;
    }

    public synchronized Collection<Blinker.Step> getPattern() {
        return new ArrayList(this.currentSteps);
    }

    /* access modifiers changed from: protected */
    public synchronized void scheduleNext() {
        ArrayList<Blinker.Step> arrayList = this.currentSteps;
        int i = this.nextStep;
        this.nextStep = i + 1;
        Blinker.Step step = arrayList.get(i);
        if (this.nextStep >= this.currentSteps.size()) {
            this.nextStep = 0;
        }
        this.light.enableLight(step.isLit());
        this.future = ThreadPool.getDefaultScheduler().schedule(new Runnable() {
            public void run() {
                LightBlinker.this.scheduleNext();
            }
        }, (long) step.getDurationMs(), TimeUnit.MILLISECONDS);
    }

    /* access modifiers changed from: protected */
    public synchronized void stop() {
        ScheduledFuture<?> scheduledFuture = this.future;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(false);
            this.future = null;
        }
    }
}
