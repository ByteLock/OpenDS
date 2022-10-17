package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.util.WebServer;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaHelper;

public interface EventLoopManagerClient {
    OnBotJavaHelper getOnBotJavaHelper();

    WebServer getWebServer();
}
