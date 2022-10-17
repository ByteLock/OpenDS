package com.qualcomm.robotcore.exception;

public class RobotCoreException extends Exception {
    public RobotCoreException(String str) {
        super(str);
    }

    public RobotCoreException(String str, Throwable th) {
        super(str, th);
    }

    public RobotCoreException(String str, Object... objArr) {
        super(String.format(str, objArr));
    }

    public static RobotCoreException createChained(Exception exc, String str, Object... objArr) {
        return new RobotCoreException(String.format(str, objArr), (Throwable) exc);
    }
}
