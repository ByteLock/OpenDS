package org.firstinspires.ftc.robotcore.internal.system;

public interface PermissionListener {
    void onPermissionDenied(String str);

    void onPermissionGranted(String str);

    void onPermissionPermanentlyDenied(String str);
}
