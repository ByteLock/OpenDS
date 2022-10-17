package org.firstinspires.ftc.robotcore.internal.system;

import android.app.Application;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ServiceController {
    public static final String TAG = "ServiceStarter";
    protected static final String metaDataAutoStartPrefix = "autoStartService.";

    public static void onApplicationStart() {
        autoStartServices();
    }

    protected static class AutoStartableService {
        public String className;
        public int launchOrder;

        public AutoStartableService(String str, int i) {
            this.className = str;
            this.launchOrder = i;
        }
    }

    protected static List<AutoStartableService> getAutoStartableServices() {
        ArrayList arrayList = new ArrayList();
        try {
            Bundle bundle = AppUtil.getDefContext().getPackageManager().getApplicationInfo(AppUtil.getInstance().getApplication().getPackageName(), 128).metaData;
            for (String str : bundle.keySet()) {
                if (str.startsWith(metaDataAutoStartPrefix)) {
                    String[] split = bundle.getString(str).split("\\|");
                    if (split.length != 2) {
                        throw AppUtil.getInstance().failFast(TAG, "incorrect manifest construction");
                    } else if (("RC".equalsIgnoreCase(split[0]) && AppUtil.getInstance().isRobotController()) || (("DS".equalsIgnoreCase(split[0]) && AppUtil.getInstance().isDriverStation()) || "BOTH".equalsIgnoreCase(split[0]))) {
                        arrayList.add(new AutoStartableService(str.substring(17), Integer.parseInt(split[1])));
                    }
                }
            }
            Collections.sort(arrayList, new Comparator<AutoStartableService>() {
                public int compare(AutoStartableService autoStartableService, AutoStartableService autoStartableService2) {
                    int i = autoStartableService.launchOrder - autoStartableService2.launchOrder;
                    return i == 0 ? autoStartableService.className.compareTo(autoStartableService2.className) : i;
                }
            });
            return arrayList;
        } catch (PackageManager.NameNotFoundException e) {
            throw AppUtil.getInstance().unreachable(TAG, e);
        }
    }

    protected static void autoStartServices() {
        for (AutoStartableService autoStartableService : getAutoStartableServices()) {
            try {
                startService(Class.forName(autoStartableService.className));
            } catch (ClassNotFoundException e) {
                throw AppUtil.getInstance().failFast(TAG, (Throwable) e, "configured service not found");
            }
        }
    }

    public static boolean startService(Class cls) {
        RobotLog.m61vv(TAG, "attempting to start service %s", cls.getSimpleName());
        Application defContext = AppUtil.getDefContext();
        try {
            if (defContext.startService(new Intent(defContext, cls)) == null) {
                RobotLog.m49ee(TAG, "unable to start service %s", cls.getSimpleName());
                return false;
            }
            RobotLog.m61vv(TAG, "started service %s", cls.getSimpleName());
            return true;
        } catch (SecurityException e) {
            RobotLog.m51ee(TAG, e, "unable to start service %s", cls.getSimpleName());
        }
    }

    public static boolean stopService(Class cls) {
        RobotLog.m61vv(TAG, "attempting to stop service %s", cls.getSimpleName());
        Application defContext = AppUtil.getDefContext();
        try {
            defContext.stopService(new Intent(defContext, cls));
            return true;
        } catch (SecurityException e) {
            RobotLog.m51ee(TAG, e, "unable to stop service %s", cls.getSimpleName());
            return false;
        }
    }
}
