package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram;
import org.firstinspires.ftc.robotcore.internal.system.Assert;

public class ModernRoboticsRequest extends ModernRoboticsDatagram {
    public static final byte[] syncBytes = {85, -86};
    protected final ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> allocationContext;

    private ModernRoboticsRequest(ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> allocationContext2, int i) {
        super(i);
        this.allocationContext = allocationContext2;
    }

    public static ModernRoboticsRequest newInstance(ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> allocationContext2, int i) {
        ModernRoboticsRequest tryAlloc = allocationContext2.tryAlloc(i);
        if (tryAlloc == null) {
            tryAlloc = new ModernRoboticsRequest(allocationContext2, i);
        }
        byte[] bArr = syncBytes;
        tryAlloc.initialize(bArr[0], bArr[1]);
        return tryAlloc;
    }

    public static ModernRoboticsRequest from(ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> allocationContext2, byte[] bArr) {
        ModernRoboticsRequest newInstance = newInstance(allocationContext2, bArr.length - 5);
        System.arraycopy(bArr, 0, newInstance.data, 0, bArr.length);
        Assert.assertTrue(newInstance.syncBytesValid());
        return newInstance;
    }

    public void close() {
        this.allocationContext.tryCache0(this);
    }

    public boolean syncBytesValid() {
        byte b = this.data[0];
        byte[] bArr = syncBytes;
        return b == bArr[0] && this.data[1] == bArr[1];
    }
}
