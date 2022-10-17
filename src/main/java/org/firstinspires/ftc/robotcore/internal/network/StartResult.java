package org.firstinspires.ftc.robotcore.internal.network;

public class StartResult {
    private int startCount;
    private WifiStartStoppable startStoppable;

    public StartResult() {
        this((WifiStartStoppable) null, 0);
    }

    public StartResult(WifiStartStoppable wifiStartStoppable, int i) {
        this.startStoppable = wifiStartStoppable;
        this.startCount = i;
    }

    public void setStartStoppable(WifiStartStoppable wifiStartStoppable) {
        this.startStoppable = wifiStartStoppable;
    }

    public WifiStartStoppable getStartStoppable() {
        return this.startStoppable;
    }

    public void setStartCount(int i) {
        this.startCount = i;
    }

    public int getStartCount() {
        return this.startCount;
    }

    public void incrementStartCount() {
        this.startCount++;
    }

    public void decrementStartCount() {
        this.startCount--;
    }
}
