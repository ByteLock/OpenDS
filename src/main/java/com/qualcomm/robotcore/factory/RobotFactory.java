package com.qualcomm.robotcore.factory;

import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robot.Robot;

public class RobotFactory {
    public static Robot createRobot(EventLoopManager eventLoopManager) throws RobotCoreException {
        return new Robot(eventLoopManager);
    }
}
