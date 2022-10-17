package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.RobotProtocolException;
import com.qualcomm.robotcore.util.RobotLog;

public interface RobocolParsable {
    public static final int HEADER_LENGTH = 5;

    void fromByteArray(byte[] bArr) throws RobotCoreException, RobotProtocolException;

    MsgType getRobocolMsgType();

    int getSequenceNumber();

    void setSequenceNumber();

    boolean shouldTransmit(long j);

    byte[] toByteArray() throws RobotCoreException;

    byte[] toByteArrayForTransmission() throws RobotCoreException;

    public enum MsgType {
        EMPTY(0),
        HEARTBEAT(1),
        GAMEPAD(2),
        PEER_DISCOVERY(3),
        COMMAND(4),
        TELEMETRY(5),
        KEEPALIVE(6);
        
        private static final MsgType[] VALUES_CACHE = null;
        private final int type;

        static {
            VALUES_CACHE = values();
        }

        public static MsgType fromByte(byte b) {
            MsgType msgType = EMPTY;
            try {
                return VALUES_CACHE[b];
            } catch (ArrayIndexOutOfBoundsException e) {
                RobotLog.m64w(String.format("Cannot convert %d to MsgType: %s", new Object[]{Byte.valueOf(b), e.toString()}));
                return msgType;
            }
        }

        private MsgType(int i) {
            this.type = i;
        }

        public byte asByte() {
            return (byte) this.type;
        }
    }
}
