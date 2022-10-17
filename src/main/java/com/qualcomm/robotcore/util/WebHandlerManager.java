package com.qualcomm.robotcore.util;

import org.firstinspires.ftc.robotcore.internal.webserver.WebHandler;
import org.firstinspires.ftc.robotcore.internal.webserver.WebObserver;

public interface WebHandlerManager {
    WebHandler getRegistered(String str);

    WebServer getWebServer();

    void register(String str, WebHandler webHandler);

    void registerObserver(String str, WebObserver webObserver);
}
