package com.qualcomm.robotcore.hardware;

import org.firstinspires.ftc.robotcore.internal.system.Misc;

public class LynxModuleMeta {
    protected volatile ImuType imuType;
    protected boolean isParent;
    protected int moduleAddress;

    public enum ImuType {
        UNKNOWN,
        NONE,
        BNO055,
        BHI260
    }

    public LynxModuleMeta(int i, boolean z) {
        this.moduleAddress = i;
        this.isParent = z;
        this.imuType = ImuType.UNKNOWN;
    }

    public LynxModuleMeta(LynxModuleMeta lynxModuleMeta) {
        this.moduleAddress = lynxModuleMeta.getModuleAddress();
        this.isParent = lynxModuleMeta.isParent();
        this.imuType = lynxModuleMeta.imuType;
    }

    public int getModuleAddress() {
        return this.moduleAddress;
    }

    public boolean isParent() {
        return this.isParent;
    }

    public ImuType imuType() {
        return this.imuType;
    }

    public void setImuType(ImuType imuType2) {
        this.imuType = imuType2;
    }

    public String toString() {
        return Misc.formatForUser("LynxModuleMeta(#%d,%b,ImuType.%s)", Integer.valueOf(this.moduleAddress), Boolean.valueOf(this.isParent), this.imuType);
    }
}
