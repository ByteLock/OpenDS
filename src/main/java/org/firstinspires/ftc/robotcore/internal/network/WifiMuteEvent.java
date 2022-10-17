package org.firstinspires.ftc.robotcore.internal.network;

import org.firstinspires.ftc.robotcore.external.Event;

public enum WifiMuteEvent implements Event {
    USER_ACTIVITY,
    WATCHDOG_WARNING,
    WATCHDOG_TIMEOUT,
    RUNNING_OPMODE,
    STOPPED_OPMODE,
    ACTIVITY_START,
    ACTIVITY_STOP,
    ACTIVITY_OTHER;

    public String getName() {
        return toString();
    }
}
