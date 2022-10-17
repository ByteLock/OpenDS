package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import java.nio.ByteBuffer;

public class LynxGetBulkInputDataResponse extends LynxDekaInterfaceResponse {
    short[] analogInputs = new short[4];
    public final int cbPayload = 34;
    byte digitalInputs = 0;
    int[] encoders = new int[4];
    byte motorStatus = 0;
    short[] velocities = new short[4];

    public LynxGetBulkInputDataResponse(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
    }

    public boolean getDigitalInput(int i) {
        LynxConstants.validateDigitalIOZ(i);
        if (((1 << i) & this.digitalInputs) != 0) {
            return true;
        }
        return false;
    }

    public int getEncoder(int i) {
        LynxConstants.validateMotorZ(i);
        return this.encoders[i];
    }

    public int getVelocity(int i) {
        LynxConstants.validateMotorZ(i);
        return this.velocities[i];
    }

    public boolean isAtTarget(int i) {
        LynxConstants.validateMotorZ(i);
        if (((1 << (i + 4)) & this.motorStatus) != 0) {
            return true;
        }
        return false;
    }

    public boolean isOverCurrent(int i) {
        LynxConstants.validateMotorZ(i);
        if (((1 << i) & this.motorStatus) != 0) {
            return true;
        }
        return false;
    }

    public int getAnalogInput(int i) {
        LynxConstants.validateAnalogInputZ(i);
        return this.analogInputs[i];
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(34).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.digitalInputs);
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr = this.encoders;
            if (i2 >= iArr.length) {
                break;
            }
            order.putInt(iArr[i2]);
            i2++;
        }
        order.put(this.motorStatus);
        int i3 = 0;
        while (true) {
            short[] sArr = this.velocities;
            if (i3 >= sArr.length) {
                break;
            }
            order.putShort(sArr[i3]);
            i3++;
        }
        while (true) {
            short[] sArr2 = this.analogInputs;
            if (i >= sArr2.length) {
                return order.array();
            }
            order.putShort(sArr2[i]);
            i++;
        }
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.digitalInputs = order.get();
        int i = 0;
        int i2 = 0;
        while (true) {
            int[] iArr = this.encoders;
            if (i2 >= iArr.length) {
                break;
            }
            iArr[i2] = order.getInt();
            i2++;
        }
        this.motorStatus = order.get();
        int i3 = 0;
        while (true) {
            short[] sArr = this.velocities;
            if (i3 >= sArr.length) {
                break;
            }
            sArr[i3] = order.getShort();
            i3++;
        }
        while (true) {
            short[] sArr2 = this.analogInputs;
            if (i < sArr2.length) {
                sArr2[i] = order.getShort();
                i++;
            } else {
                return;
            }
        }
    }
}
