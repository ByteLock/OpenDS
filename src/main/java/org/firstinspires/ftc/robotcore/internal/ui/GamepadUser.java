package org.firstinspires.ftc.robotcore.internal.p013ui;

/* renamed from: org.firstinspires.ftc.robotcore.internal.ui.GamepadUser */
public enum GamepadUser {
    ONE(1),
    TWO(2);
    

    /* renamed from: id */
    public byte f280id;

    private GamepadUser(int i) {
        this.f280id = (byte) i;
    }

    public static GamepadUser from(int i) {
        if (i == 1) {
            return ONE;
        }
        if (i == 2) {
            return TWO;
        }
        return null;
    }
}
