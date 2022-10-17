package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Application;
import com.qualcomm.robotcore.util.RobotLog;

public class RobotApplication extends Application {
    public void onCreate() {
        super.onCreate();
        AppUtil.onApplicationStart(this);
        RobotLog.onApplicationStart();
        ClassFactoryImpl.onApplicationStart();
    }
}
