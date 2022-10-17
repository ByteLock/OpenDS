package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Service;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.IBinder;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.Closeable;
import java.io.IOException;
import org.firstinspires.ftc.robotcore.external.Supplier;

public abstract class StartableService extends Service {
    protected Closeable instance;
    protected final Supplier<Closeable> instantiator;

    public abstract String getTag();

    protected StartableService(Supplier<Closeable> supplier) {
        this.instantiator = supplier;
    }

    public void onCreate() {
        RobotLog.m60vv(getTag(), "onCreate()");
    }

    public int onStartCommand(Intent intent, int i, int i2) {
        RobotLog.m61vv(getTag(), "onStartCommand() intent=%s flags=0x%x startId=%d", intent, Integer.valueOf(i), Integer.valueOf(i2));
        this.instance = this.instantiator.get();
        return 2;
    }

    public void onDestroy() {
        RobotLog.m60vv(getTag(), "onDestroy()");
        Closeable closeable = this.instance;
        if (closeable != null) {
            try {
                closeable.close();
            } catch (IOException e) {
                RobotLog.m50ee(getTag(), (Throwable) e, "exception during close; ignored");
            }
            this.instance = null;
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        RobotLog.m60vv(getTag(), "onConfigurationChanged()");
    }

    public void onLowMemory() {
        RobotLog.m60vv(getTag(), "onLowMemory()");
    }

    public void onTrimMemory(int i) {
        RobotLog.m60vv(getTag(), "onTrimMemory()");
    }

    public IBinder onBind(Intent intent) {
        RobotLog.m60vv(getTag(), "onBind()");
        return null;
    }

    public boolean onUnbind(Intent intent) {
        RobotLog.m60vv(getTag(), "onUnbind()");
        return super.onUnbind(intent);
    }

    public void onRebind(Intent intent) {
        RobotLog.m60vv(getTag(), "onRebind()");
    }

    public void onTaskRemoved(Intent intent) {
        RobotLog.m60vv(getTag(), "onTaskRemoved()");
    }
}
