package com.qualcomm.hardware.lynx.commands.core;

import android.content.Context;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynch;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynchV1;
import com.qualcomm.hardware.lynx.LynxI2cDeviceSynchV2;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.robotcore.util.RobotLog;

public class LynxFirmwareVersionManager {
    public static LynxI2cDeviceSynch createLynxI2cDeviceSynch(Context context, LynxModule lynxModule, int i) {
        if (lynxModule.isCommandSupported(LynxI2cWriteReadMultipleBytesCommand.class)) {
            RobotLog.m52i("LynxFirmwareVersionManager: LynxI2cDeviceSynchV2");
            return new LynxI2cDeviceSynchV2(context, lynxModule, i);
        }
        RobotLog.m52i("LynxFirmwareVersionManager: LynxI2cDeviceSynchV1");
        return new LynxI2cDeviceSynchV1(context, lynxModule, i);
    }
}
