package com.qualcomm.robotcore.eventloop.opmode;

public interface OpModeManagerNotifier {

    public interface Notifications {
        void onOpModePostStop(OpMode opMode);

        void onOpModePreInit(OpMode opMode);

        void onOpModePreStart(OpMode opMode);
    }

    OpMode registerListener(Notifications notifications);

    void unregisterListener(Notifications notifications);
}
