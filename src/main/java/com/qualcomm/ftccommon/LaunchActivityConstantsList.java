package com.qualcomm.ftccommon;

public class LaunchActivityConstantsList {
    public static final String RC_WEB_INFO = "RC_WEB_INFO";
    public static final String VIEW_LOGS_ACTIVITY_FILENAME = "org.firstinspires.ftc.ftccommon.logFilename";

    public enum RequestCode {
        UNKNOWN,
        CONFIGURE_ROBOT_CONTROLLER,
        CONFIGURE_DRIVER_STATION,
        PROGRAM_AND_MANAGE,
        SETTINGS_DRIVER_STATION,
        SETTINGS_ROBOT_CONTROLLER,
        INSPECTIONS,
        WRITE_TO_SYSTEM_SETTINGS
    }
}
