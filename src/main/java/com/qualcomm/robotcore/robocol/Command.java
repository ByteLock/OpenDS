package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.net.InetSocketAddress;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import java.util.Comparator;
import org.firstinspires.ftc.robotcore.system.Deadline;
import org.firstinspires.inspection.InspectionState;

public class Command extends RobocolParsableBase implements Comparable<Command>, Comparator<Command> {
    private static final short cbPayloadBase = 9;
    private static final short cbStringLength = 2;
    boolean mAcknowledged;
    byte mAttempts;
    String mExtra;
    boolean mIsInjected;
    String mName;
    InetSocketAddress mSender;
    long mTimestamp;
    Deadline mTransmissionDeadline;

    public Command(String str) {
        this(str, InspectionState.NO_VERSION);
    }

    public Command(String str, String str2) {
        this.mAcknowledged = false;
        this.mAttempts = 0;
        this.mIsInjected = false;
        this.mTransmissionDeadline = null;
        this.mName = str;
        this.mExtra = str2;
        this.mTimestamp = generateTimestamp();
    }

    public Command(RobocolDatagram robocolDatagram) throws RobotCoreException {
        this.mAcknowledged = false;
        this.mAttempts = 0;
        this.mIsInjected = false;
        this.mTransmissionDeadline = null;
        fromByteArray(robocolDatagram.getData());
        this.mSender = new InetSocketAddress(robocolDatagram.getAddress(), robocolDatagram.getPort());
    }

    public void acknowledge() {
        this.mAcknowledged = true;
    }

    public boolean isAcknowledged() {
        return this.mAcknowledged;
    }

    public String getName() {
        return this.mName;
    }

    public String getExtra() {
        return this.mExtra;
    }

    public byte getAttempts() {
        return this.mAttempts;
    }

    public boolean hasExpired() {
        Deadline deadline = this.mTransmissionDeadline;
        return deadline != null && deadline.hasExpired();
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.COMMAND;
    }

    public boolean isInjected() {
        return this.mIsInjected;
    }

    public void setIsInjected(boolean z) {
        this.mIsInjected = z;
    }

    public void setTransmissionDeadline(Deadline deadline) {
        this.mTransmissionDeadline = deadline;
    }

    public InetSocketAddress getSender() {
        return this.mSender;
    }

    public byte[] toByteArray() throws RobotCoreException {
        byte b = this.mAttempts;
        int i = 1;
        if (b != Byte.MAX_VALUE) {
            this.mAttempts = (byte) (b + 1);
        }
        byte[] stringToUtf8 = TypeConversion.stringToUtf8(this.mName);
        byte[] stringToUtf82 = TypeConversion.stringToUtf8(this.mExtra);
        short payloadSize = (short) getPayloadSize(stringToUtf8.length, stringToUtf82.length);
        if (payloadSize <= Short.MAX_VALUE) {
            ByteBuffer writeBuffer = getWriteBuffer(payloadSize);
            try {
                writeBuffer.putLong(this.mTimestamp);
                if (!this.mAcknowledged) {
                    i = 0;
                }
                writeBuffer.put((byte) i);
                writeBuffer.putShort((short) stringToUtf8.length);
                writeBuffer.put(stringToUtf8);
                if (!this.mAcknowledged) {
                    writeBuffer.putShort((short) stringToUtf82.length);
                    writeBuffer.put(stringToUtf82);
                }
            } catch (BufferOverflowException e) {
                RobotLog.logStacktrace(e);
            }
            return writeBuffer.array();
        }
        throw new IllegalArgumentException(String.format("command payload is too large: %d", new Object[]{Short.valueOf(payloadSize)}));
    }

    /* access modifiers changed from: package-private */
    public int getPayloadSize(int i, int i2) {
        return this.mAcknowledged ? i + 11 : i + 11 + 2 + i2;
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException {
        ByteBuffer readBuffer = getReadBuffer(bArr);
        this.mTimestamp = readBuffer.getLong();
        this.mAcknowledged = readBuffer.get() != 0;
        byte[] bArr2 = new byte[TypeConversion.unsignedShortToInt(readBuffer.getShort())];
        readBuffer.get(bArr2);
        this.mName = TypeConversion.utf8ToString(bArr2);
        if (!this.mAcknowledged) {
            byte[] bArr3 = new byte[TypeConversion.unsignedShortToInt(readBuffer.getShort())];
            readBuffer.get(bArr3);
            this.mExtra = TypeConversion.utf8ToString(bArr3);
        }
    }

    public String toString() {
        return String.format("command: %20d %5s %s", new Object[]{Long.valueOf(this.mTimestamp), Boolean.valueOf(this.mAcknowledged), this.mName});
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof Command)) {
            return false;
        }
        Command command = (Command) obj;
        return this.mName.equals(command.mName) && this.mTimestamp == command.mTimestamp;
    }

    public int hashCode() {
        return this.mName.hashCode() ^ ((int) this.mTimestamp);
    }

    public int compareTo(Command command) {
        int compareTo = this.mName.compareTo(command.mName);
        if (compareTo != 0) {
            return compareTo;
        }
        long j = this.mTimestamp;
        long j2 = command.mTimestamp;
        if (j < j2) {
            return -1;
        }
        return j > j2 ? 1 : 0;
    }

    public int compare(Command command, Command command2) {
        return command.compareTo(command2);
    }

    public static long generateTimestamp() {
        return System.nanoTime();
    }
}
