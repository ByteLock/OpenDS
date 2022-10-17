package com.qualcomm.robotcore.robot;

import com.qualcomm.robotcore.eventloop.EventLoop;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;

public class Robot {
    public final EventLoopManager eventLoopManager;

    public Robot(EventLoopManager eventLoopManager2) {
        this.eventLoopManager = eventLoopManager2;
    }

    public void start(EventLoop eventLoop) throws RobotCoreException {
        this.eventLoopManager.start(eventLoop);
    }

    public void shutdown() {
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        if (eventLoopManager2 != null) {
            eventLoopManager2.shutdown();
        }
    }
}
