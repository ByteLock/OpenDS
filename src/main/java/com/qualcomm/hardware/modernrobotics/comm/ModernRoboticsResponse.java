package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram;
import org.firstinspires.ftc.robotcore.internal.stellaris.FlashLoaderDatagram;

public class ModernRoboticsResponse extends ModernRoboticsDatagram {
    public static final byte[] syncBytes = {FlashLoaderDatagram.NAK, FlashLoaderDatagram.ACK};
    protected final ModernRoboticsDatagram.AllocationContext<ModernRoboticsResponse> allocationContext;

    private ModernRoboticsResponse(ModernRoboticsDatagram.AllocationContext<ModernRoboticsResponse> allocationContext2, int i) {
        super(i);
        this.allocationContext = allocationContext2;
    }

    public static ModernRoboticsResponse newInstance(ModernRoboticsDatagram.AllocationContext<ModernRoboticsResponse> allocationContext2, int i) {
        ModernRoboticsResponse tryAlloc = allocationContext2.tryAlloc(i);
        if (tryAlloc == null) {
            tryAlloc = new ModernRoboticsResponse(allocationContext2, i);
        }
        byte[] bArr = syncBytes;
        tryAlloc.initialize(bArr[0], bArr[1]);
        return tryAlloc;
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
