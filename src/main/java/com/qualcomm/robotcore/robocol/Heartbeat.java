package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.RobotLog;
import java.nio.BufferOverflowException;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.TimeZone;
import org.firstinspires.ftc.robotcore.internal.system.Misc;

public class Heartbeat extends RobocolParsableBase {
    public static final short BASE_PAYLOAD_SIZE = 33;
    private static final Charset charset = Charset.forName("UTF-8");
    private RobotState robotState = RobotState.NOT_STARTED;

    /* renamed from: t0 */
    public long f135t0 = 0;

    /* renamed from: t1 */
    public long f136t1 = 0;

    /* renamed from: t2 */
    public long f137t2 = 0;
    private String timeZoneId = TimeZone.getDefault().getID();
    private byte[] timeZoneIdBytes;
    private long timestamp = 0;

    /* access modifiers changed from: protected */
    public int cbPayload() {
        return this.timeZoneIdBytes.length + 34;
    }

    public String getTimeZoneId() {
        return this.timeZoneId;
    }

    public void setTimeZoneId(String str) {
        Charset charset2 = charset;
        if (str.getBytes(charset2).length <= 127) {
            this.timeZoneId = str;
            this.timeZoneIdBytes = str.getBytes(charset2);
            return;
        }
        throw Misc.illegalArgumentException("timezone id too long");
    }

    public static Heartbeat createWithTimeStamp() {
        Heartbeat heartbeat = new Heartbeat();
        heartbeat.timestamp = System.nanoTime();
        return heartbeat;
    }

    public long getTimestamp() {
        return this.timestamp;
    }

    public double getElapsedSeconds() {
        return ((double) (System.nanoTime() - this.timestamp)) / 1.0E9d;
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.HEARTBEAT;
    }

    public byte getRobotState() {
        return this.robotState.asByte();
    }

    public void setRobotState(RobotState robotState2) {
        this.robotState = robotState2;
    }

    public byte[] toByteArray() throws RobotCoreException {
        ByteBuffer writeBuffer = getWriteBuffer(cbPayload());
        try {
            writeBuffer.putLong(this.timestamp);
            writeBuffer.put(this.robotState.asByte());
            writeBuffer.putLong(this.f135t0);
            writeBuffer.putLong(this.f136t1);
            writeBuffer.putLong(this.f137t2);
            writeBuffer.put((byte) this.timeZoneIdBytes.length);
            writeBuffer.put(this.timeZoneIdBytes);
        } catch (BufferOverflowException e) {
            RobotLog.logStackTrace(e);
        }
        return writeBuffer.array();
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException {
        try {
            ByteBuffer readBuffer = getReadBuffer(bArr);
            this.timestamp = readBuffer.getLong();
            this.robotState = RobotState.fromByte(readBuffer.get());
            this.f135t0 = readBuffer.getLong();
            this.f136t1 = readBuffer.getLong();
            this.f137t2 = readBuffer.getLong();
            byte[] bArr2 = new byte[readBuffer.get()];
            this.timeZoneIdBytes = bArr2;
            readBuffer.get(bArr2);
            this.timeZoneId = new String(this.timeZoneIdBytes, charset);
        } catch (BufferUnderflowException e) {
            throw RobotCoreException.createChained(e, "incoming packet too small", new Object[0]);
        }
    }

    public String toString() {
        return String.format("Heartbeat - seq: %4d, time: %d", new Object[]{Integer.valueOf(getSequenceNumber()), Long.valueOf(this.timestamp)});
    }
}
