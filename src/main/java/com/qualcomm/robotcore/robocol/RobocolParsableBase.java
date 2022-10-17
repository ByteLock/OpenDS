package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;
import java.util.concurrent.atomic.AtomicInteger;

public abstract class RobocolParsableBase implements RobocolParsable {
    protected static final long nanotimeTransmitInterval = 200000000;
    protected static AtomicInteger nextSequenceNumber = new AtomicInteger();
    protected long nanotimeTransmit = 0;
    protected int sequenceNumber;

    public static void initializeSequenceNumber(int i) {
        nextSequenceNumber = new AtomicInteger(i);
    }

    public RobocolParsableBase() {
        setSequenceNumber();
    }

    public int getSequenceNumber() {
        return this.sequenceNumber;
    }

    public void setSequenceNumber(short s) {
        this.sequenceNumber = TypeConversion.unsignedShortToInt(s);
    }

    public void setSequenceNumber() {
        setSequenceNumber((short) nextSequenceNumber.getAndIncrement());
    }

    public byte[] toByteArrayForTransmission() throws RobotCoreException {
        byte[] byteArray = toByteArray();
        this.nanotimeTransmit = System.nanoTime();
        return byteArray;
    }

    public boolean shouldTransmit(long j) {
        long j2 = this.nanotimeTransmit;
        return j2 == 0 || j - j2 > nanotimeTransmitInterval;
    }

    /* access modifiers changed from: protected */
    public ByteBuffer allocateWholeWriteBuffer(int i) {
        return ByteBuffer.allocate(i);
    }

    /* access modifiers changed from: protected */
    public ByteBuffer getWholeReadBuffer(byte[] bArr) {
        return ByteBuffer.wrap(bArr);
    }

    /* access modifiers changed from: protected */
    public ByteBuffer getWriteBuffer(int i) {
        ByteBuffer allocateWholeWriteBuffer = allocateWholeWriteBuffer(i + 5);
        allocateWholeWriteBuffer.put(getRobocolMsgType().asByte());
        allocateWholeWriteBuffer.putShort((short) i);
        allocateWholeWriteBuffer.putShort((short) this.sequenceNumber);
        return allocateWholeWriteBuffer;
    }

    /* access modifiers changed from: protected */
    public ByteBuffer getReadBuffer(byte[] bArr) {
        ByteBuffer wrap = ByteBuffer.wrap(bArr, 3, bArr.length - 3);
        setSequenceNumber(wrap.getShort());
        return wrap;
    }
}
