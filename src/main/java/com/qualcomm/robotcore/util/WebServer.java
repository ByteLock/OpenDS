package com.qualcomm.robotcore.util;

import org.firstinspires.ftc.robotcore.internal.webserver.RobotControllerWebInfo;
import org.firstinspires.ftc.robotcore.internal.webserver.websockets.WebSocketManager;

public interface WebServer {
    RobotControllerWebInfo getConnectionInformation();

    WebHandlerManager getWebHandlerManager();

    WebSocketManager getWebSocketManager();

    void start();

    void stop();

    boolean wasStarted();
}
