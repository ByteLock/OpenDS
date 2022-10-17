package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.util.Set;

public class LightMultiplexor implements SwitchableLight {
    protected static final Set<LightMultiplexor> extantMultiplexors = new WeakReferenceSet();
    protected int enableCount = 0;
    protected final SwitchableLight target;

    public static synchronized LightMultiplexor forLight(SwitchableLight switchableLight) {
        synchronized (LightMultiplexor.class) {
            for (LightMultiplexor next : extantMultiplexors) {
                if (next.target.equals(switchableLight)) {
                    return next;
                }
            }
            LightMultiplexor lightMultiplexor = new LightMultiplexor(switchableLight);
            extantMultiplexors.add(lightMultiplexor);
            return lightMultiplexor;
        }
    }

    protected LightMultiplexor(SwitchableLight switchableLight) {
        this.target = switchableLight;
    }

    public boolean isLightOn() {
        return this.target.isLightOn();
    }

    public synchronized void enableLight(boolean z) {
        if (z) {
            int i = this.enableCount;
            this.enableCount = i + 1;
            if (i == 0) {
                this.target.enableLight(true);
            }
        } else {
            int i2 = this.enableCount;
            if (i2 > 0) {
                int i3 = i2 - 1;
                this.enableCount = i3;
                if (i3 == 0) {
                    this.target.enableLight(false);
                }
            }
        }
    }
}
