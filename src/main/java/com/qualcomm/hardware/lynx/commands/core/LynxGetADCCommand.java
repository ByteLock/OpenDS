package com.qualcomm.hardware.lynx.commands.core;

import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.commands.LynxDatagram;
import com.qualcomm.hardware.lynx.commands.LynxInterfaceResponse;
import java.nio.ByteBuffer;

public class LynxGetADCCommand extends LynxDekaInterfaceCommand<LynxGetADCResponse> {
    private static final int cbPayload = 2;
    private byte channel;
    private byte mode;

    public enum Channel {
        USER0(0),
        USER1(1),
        USER2(2),
        USER3(3),
        GPIO_CURRENT(4),
        I2C_BUS_CURRENT(5),
        SERVO_CURRENT(6),
        BATTERY_CURRENT(7),
        MOTOR0_CURRENT(8),
        MOTOR1_CURRENT(9),
        MOTOR2_CURRENT(10),
        MOTOR3_CURRENT(11),
        FIVE_VOLT_MONITOR(12),
        BATTERY_MONITOR(13),
        CONTROLLER_TEMPERATURE(14);
        
        public final byte bVal;

        private Channel(int i) {
            this.bVal = (byte) i;
        }

        public static Channel motorCurrent(int i) {
            if (i == 0) {
                return MOTOR0_CURRENT;
            }
            if (i == 1) {
                return MOTOR1_CURRENT;
            }
            if (i == 2) {
                return MOTOR2_CURRENT;
            }
            if (i == 3) {
                return MOTOR3_CURRENT;
            }
            throw new IllegalArgumentException(String.format("illegal motor port %d", new Object[]{Integer.valueOf(i)}));
        }

        public static Channel user(int i) {
            if (i == 0) {
                return USER0;
            }
            if (i == 1) {
                return USER1;
            }
            if (i == 2) {
                return USER2;
            }
            if (i == 3) {
                return USER3;
            }
            throw new IllegalArgumentException(String.format("illegal user port %d", new Object[]{Integer.valueOf(i)}));
        }
    }

    public enum Mode {
        ENGINEERING(0),
        RAW(1);
        
        public final byte bVal;

        private Mode(int i) {
            this.bVal = (byte) i;
        }
    }

    public LynxGetADCCommand(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf, new LynxGetADCResponse(lynxModuleIntf));
    }

    public LynxGetADCCommand(LynxModuleIntf lynxModuleIntf, Channel channel2, Mode mode2) {
        this(lynxModuleIntf);
        this.channel = channel2.bVal;
        this.mode = mode2.bVal;
    }

    public static Class<? extends LynxInterfaceResponse> getResponseClass() {
        return LynxGetADCResponse.class;
    }

    public byte[] toPayloadByteArray() {
        ByteBuffer order = ByteBuffer.allocate(2).order(LynxDatagram.LYNX_ENDIAN);
        order.put(this.channel);
        order.put(this.mode);
        return order.array();
    }

    public void fromPayloadByteArray(byte[] bArr) {
        ByteBuffer order = ByteBuffer.wrap(bArr).order(LynxDatagram.LYNX_ENDIAN);
        this.channel = order.get();
        this.mode = order.get();
    }
}
