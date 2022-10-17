package com.qualcomm.robotcore.eventloop.opmode;

import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;

public interface OpModeManager {
    public static final String DEFAULT_OP_MODE_NAME = "$Stop$Robot$";

    void register(String str, OpMode opMode);

    void register(String str, Class<? extends OpMode> cls);

    void register(OpModeMeta opModeMeta, OpMode opMode);

    void register(OpModeMeta opModeMeta, Class<? extends OpMode> cls);
}
