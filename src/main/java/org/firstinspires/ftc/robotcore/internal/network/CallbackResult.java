package org.firstinspires.ftc.robotcore.internal.network;

public enum CallbackResult {
    NOT_HANDLED,
    HANDLED,
    HANDLED_CONTINUE;

    public boolean isHandled() {
        return this != NOT_HANDLED;
    }

    public boolean stopDispatch() {
        return this == HANDLED;
    }
}
