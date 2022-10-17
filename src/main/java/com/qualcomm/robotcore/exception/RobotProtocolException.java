package com.qualcomm.robotcore.exception;

public class RobotProtocolException extends Exception {
    public RobotProtocolException(String str) {
        super(str);
    }

    public RobotProtocolException(String str, Object... objArr) {
        super(String.format(str, objArr));
    }
}
