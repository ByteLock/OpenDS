package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.util.RobotLog;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;

public class KeepAlive extends RobocolParsableBase {
    public static final short BASE_PAYLOAD_SIZE = 1;

    /* renamed from: id */
    private byte f138id = 0;
    private long timestamp = 0;

    /* access modifiers changed from: protected */
    public int cbPayload() {
        return 1;
    }

    public static KeepAlive createWithTimeStamp() {
        KeepAlive keepAlive = new KeepAlive();
        keepAlive.timestamp = System.nanoTime();
        return keepAlive;
    }

    public double getElapsedSeconds() {
        return ((double) (System.nanoTime() - this.timestamp)) / 1.0E9d;
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.KEEPALIVE;
    }

    public byte[] toByteArray() throws RobotCoreException {
        ByteBuffer writeBuffer = getWriteBuffer(cbPayload());
        try {
            writeBuffer.put(this.f138id);
        } catch (BufferOverflowException e) {
            RobotLog.logStackTrace(e);
        }
        return writeBuffer.array();
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException {
        try {
            this.f138id = getReadBuffer(bArr).get();
        } catch (BufferUnderflowException e) {
            throw RobotCoreException.createChained(e, "incoming packet too small", new Object[0]);
        }
    }

    public String toString() {
        return String.format("KeepAlive - time: %d", new Object[]{Long.valueOf(this.timestamp)});
    }
}
