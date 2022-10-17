package com.qualcomm.robotcore.exception;

public class DuplicateNameException extends RuntimeException {
    public DuplicateNameException(String str) {
        super(str);
    }

    public DuplicateNameException(String str, Object... objArr) {
        super(String.format(str, objArr));
    }
}
