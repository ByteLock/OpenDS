package com.qualcomm.hardware.lynx;

import com.qualcomm.robotcore.util.RobotLog;

public class EmbeddedControlHubModule {
    public static final String TAG = "EmbeddedControlHubModule";
    protected static volatile LynxModule embeddedLynxModule;

    public static LynxModule get() {
        return embeddedLynxModule;
    }

    public static void set(LynxModule lynxModule) {
        RobotLog.m60vv(TAG, "setting embedded module");
        embeddedLynxModule = lynxModule;
    }

    public static void clear() {
        RobotLog.m60vv(TAG, "clearing embedded module");
        embeddedLynxModule = null;
    }
}
