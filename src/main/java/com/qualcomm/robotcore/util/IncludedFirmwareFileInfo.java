package com.qualcomm.robotcore.util;

import java.io.File;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class IncludedFirmwareFileInfo {
    private static final String FILENAME = "REVHubFirmware_1_08_02.bin";
    public static final RobotCoreCommandList.FWImage FW_IMAGE;
    public static final String HUMAN_READABLE_FW_VERSION = "1.8.2";
    private static final File assetLocation;
    private static final File assetParentLocation;

    static {
        File file = new File(AppUtil.UPDATES_DIR.getName(), AppUtil.LYNX_FIRMWARE_UPDATE_DIR.getName());
        assetParentLocation = file;
        File file2 = new File(file, FILENAME);
        assetLocation = file2;
        FW_IMAGE = new RobotCoreCommandList.FWImage(file2, true);
    }
}
