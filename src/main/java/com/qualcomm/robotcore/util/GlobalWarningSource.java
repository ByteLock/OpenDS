package com.qualcomm.robotcore.util;

public interface GlobalWarningSource {
    void clearGlobalWarning();

    String getGlobalWarning();

    void setGlobalWarning(String str);

    boolean shouldTriggerWarningSound();

    void suppressGlobalWarning(boolean z);
}
