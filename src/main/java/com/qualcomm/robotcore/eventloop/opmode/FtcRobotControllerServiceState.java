package com.qualcomm.robotcore.eventloop.opmode;

import com.qualcomm.robotcore.eventloop.EventLoopManager;

public interface FtcRobotControllerServiceState extends EventLoopManagerClient {
    EventLoopManager getEventLoopManager();
}
