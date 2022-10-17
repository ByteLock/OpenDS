package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxUnsupportedCommandException;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.BufferUnderflowException;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

public class LynxDatagram {
    public static final ByteOrder LYNX_ENDIAN = ByteOrder.LITTLE_ENDIAN;
    public static final int cbFrameBytesAndPacketLength = 4;
    public static final byte[] frameBytes = {68, 75};
    private byte checksum;
    private byte destModuleAddress;
    private byte messageNumber;
    private short packetId;
    private short packetLength;
    private byte[] payloadData;
    private TimeWindow payloadTimeWindow;
    private byte referenceNumber;
    private byte sourceModuleAddress;

    public static int getFixedPacketLength() {
        return 11;
    }

    public static boolean beginsWithFraming(byte[] bArr) {
        int length = bArr.length;
        byte[] bArr2 = frameBytes;
        return length >= bArr2.length && bArr[0] == bArr2[0] && bArr[1] == bArr2[1];
    }

    public static boolean beginsWithFraming(ByteBuffer byteBuffer) {
        byte b = byteBuffer.get();
        byte[] bArr = frameBytes;
        return b == bArr[0] && byteBuffer.get() == bArr[1];
    }

    public LynxDatagram() {
        this.destModuleAddress = 0;
        this.sourceModuleAddress = 0;
        this.messageNumber = 0;
        this.referenceNumber = 0;
        this.packetId = 0;
        this.payloadData = new byte[0];
    }

    public LynxDatagram(LynxMessage lynxMessage) throws LynxUnsupportedCommandException {
        this();
        int commandNumber = lynxMessage.getCommandNumber();
        lynxMessage.getModule().validateCommand(lynxMessage);
        setDestModuleAddress(lynxMessage.getDestModuleAddress());
        setMessageNumber(lynxMessage.getMessageNumber());
        setReferenceNumber(lynxMessage.getReferenceNumber());
        setPacketId(commandNumber);
        setPayloadData(lynxMessage.toPayloadByteArray());
    }

    public void setPayloadTimeWindow(TimeWindow timeWindow) {
        this.payloadTimeWindow = timeWindow;
    }

    public TimeWindow getPayloadTimeWindow() {
        TimeWindow timeWindow = this.payloadTimeWindow;
        return timeWindow == null ? new TimeWindow() : timeWindow;
    }

    public int getPacketLength() {
        return TypeConversion.unsignedShortToInt(this.packetLength);
    }

    public void setPacketLength(int i) {
        this.packetLength = (short) ((byte) i);
    }

    public int updatePacketLength() {
        int fixedPacketLength = getFixedPacketLength() + this.payloadData.length;
        setPacketLength(fixedPacketLength);
        return fixedPacketLength;
    }

    public int getDestModuleAddress() {
        return TypeConversion.unsignedByteToInt(this.destModuleAddress);
    }

    public void setDestModuleAddress(int i) {
        this.destModuleAddress = (byte) i;
    }

    public int getSourceModuleAddress() {
        return TypeConversion.unsignedByteToInt(this.sourceModuleAddress);
    }

    public void setSourceModuleAddress(int i) {
        this.sourceModuleAddress = (byte) i;
    }

    public int getMessageNumber() {
        return TypeConversion.unsignedByteToInt(this.messageNumber);
    }

    public void setMessageNumber(int i) {
        this.messageNumber = (byte) i;
    }

    public int getReferenceNumber() {
        return TypeConversion.unsignedByteToInt(this.referenceNumber);
    }

    public void setReferenceNumber(int i) {
        this.referenceNumber = (byte) i;
    }

    public int getPacketId() {
        return TypeConversion.unsignedShortToInt(this.packetId);
    }

    public void setPacketId(int i) {
        this.packetId = (short) i;
    }

    public boolean isResponse() {
        return getPacketId() >= 32768;
    }

    public int getCommandNumber() {
        return getPacketId() & -32769;
    }

    public byte[] getPayloadData() {
        return this.payloadData;
    }

    public void setPayloadData(byte[] bArr) {
        this.payloadData = bArr;
    }

    public int getChecksum() {
        return TypeConversion.unsignedByteToInt(this.checksum);
    }

    public void setChecksum(int i) {
        this.checksum = (byte) i;
    }

    public byte computeChecksum() {
        byte checksumBytes = checksumBytes((byte) 0, frameBytes);
        short s = this.packetLength;
        ByteOrder byteOrder = LYNX_ENDIAN;
        return checksumBytes(checksumBytes((byte) (((byte) (((byte) (((byte) (checksumBytes(checksumBytes, TypeConversion.shortToByteArray(s, byteOrder)) + this.destModuleAddress)) + this.sourceModuleAddress)) + this.messageNumber)) + this.referenceNumber), TypeConversion.shortToByteArray(this.packetId, byteOrder)), this.payloadData);
    }

    private static byte checksumBytes(byte b, byte[] bArr) {
        for (byte b2 : bArr) {
            b = (byte) (b + b2);
        }
        return b;
    }

    public boolean isChecksumValid() {
        return this.checksum == computeChecksum();
    }

    public byte[] toByteArray() {
        int updatePacketLength = updatePacketLength();
        setChecksum(computeChecksum());
        ByteBuffer allocate = ByteBuffer.allocate(updatePacketLength);
        allocate.order(LYNX_ENDIAN);
        allocate.put(frameBytes);
        allocate.putShort(this.packetLength);
        allocate.put(this.destModuleAddress);
        allocate.put(this.sourceModuleAddress);
        allocate.put(this.messageNumber);
        allocate.put(this.referenceNumber);
        allocate.putShort(this.packetId);
        allocate.put(this.payloadData);
        allocate.put(this.checksum);
        return allocate.array();
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException {
        ByteBuffer wrap = ByteBuffer.wrap(bArr);
        wrap.order(LYNX_ENDIAN);
        try {
            if (beginsWithFraming(wrap)) {
                this.packetLength = wrap.getShort();
                this.destModuleAddress = wrap.get();
                this.sourceModuleAddress = wrap.get();
                this.messageNumber = wrap.get();
                this.referenceNumber = wrap.get();
                this.packetId = wrap.getShort();
                byte[] bArr2 = new byte[(getPacketLength() - getFixedPacketLength())];
                this.payloadData = bArr2;
                wrap.get(bArr2);
                this.checksum = wrap.get();
                return;
            }
            throw illegalDatagram();
        } catch (BufferUnderflowException e) {
            throw RobotCoreException.createChained(e, "Lynx datagram buffer underflow", new Object[0]);
        }
    }

    private RobotCoreException illegalDatagram() {
        return new RobotCoreException("illegal Lynx datagram format");
    }
}
