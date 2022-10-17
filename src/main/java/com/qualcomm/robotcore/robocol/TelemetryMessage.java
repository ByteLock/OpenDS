package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.ByteBuffer;
import java.nio.charset.Charset;
import java.util.LinkedHashMap;
import java.util.Map;
import org.firstinspires.inspection.InspectionState;

public class TelemetryMessage extends RobocolParsableBase {
    private static final Charset CHARSET = Charset.forName("UTF-8");
    public static final String DEFAULT_TAG = "TELEMETRY_DATA";
    public static final int cCountMax = 255;
    static final int cbCountLen = 1;
    static final int cbFloat = 4;
    static final int cbKeyLen = 2;
    public static final int cbKeyMax = 65535;
    static final int cbRobotState = 1;
    static final int cbSorted = 1;
    static final int cbTagLen = 1;
    public static final int cbTagMax = 255;
    static final int cbTimestamp = 8;
    static final int cbValueLen = 2;
    public static final int cbValueMax = 65535;
    private final Map<String, Float> dataNumbers = new LinkedHashMap();
    private final Map<String, String> dataStrings = new LinkedHashMap();
    private boolean isSorted = true;
    private RobotState robotState = RobotState.UNKNOWN;
    private String tag = InspectionState.NO_VERSION;
    private long timestamp = 0;

    public TelemetryMessage() {
    }

    public TelemetryMessage(byte[] bArr) throws RobotCoreException {
        fromByteArray(bArr);
    }

    public synchronized long getTimestamp() {
        return this.timestamp;
    }

    public boolean isSorted() {
        return this.isSorted;
    }

    public void setSorted(boolean z) {
        this.isSorted = z;
    }

    public RobotState getRobotState() {
        return this.robotState;
    }

    public void setRobotState(RobotState robotState2) {
        this.robotState = robotState2;
    }

    public synchronized void setTag(String str) {
        this.tag = str;
    }

    public synchronized String getTag() {
        if (this.tag.length() == 0) {
            return DEFAULT_TAG;
        }
        return this.tag;
    }

    public synchronized void addData(String str, String str2) {
        this.dataStrings.put(str, str2);
    }

    public synchronized void addData(String str, Object obj) {
        this.dataStrings.put(str, obj.toString());
    }

    public synchronized void addData(String str, float f) {
        this.dataNumbers.put(str, Float.valueOf(f));
    }

    public synchronized void addData(String str, double d) {
        this.dataNumbers.put(str, Float.valueOf((float) d));
    }

    public synchronized Map<String, String> getDataStrings() {
        return this.dataStrings;
    }

    public synchronized Map<String, Float> getDataNumbers() {
        return this.dataNumbers;
    }

    public synchronized boolean hasData() {
        return !this.dataStrings.isEmpty() || !this.dataNumbers.isEmpty();
    }

    public synchronized void clearData() {
        this.timestamp = 0;
        this.dataStrings.clear();
        this.dataNumbers.clear();
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.TELEMETRY;
    }

    public synchronized byte[] toByteArray() throws RobotCoreException {
        ByteBuffer writeBuffer;
        this.timestamp = System.currentTimeMillis();
        if (this.dataStrings.size() > 255) {
            throw new RobotCoreException("Cannot have more than %d string data points", 255);
        } else if (this.dataNumbers.size() <= 255) {
            writeBuffer = getWriteBuffer(countMessageBytes());
            writeBuffer.putLong(this.timestamp);
            writeBuffer.put((byte) (this.isSorted ? 1 : 0));
            writeBuffer.put(this.robotState.asByte());
            if (this.tag.length() == 0) {
                putTagLen(writeBuffer, 0);
            } else {
                byte[] bytes = this.tag.getBytes(CHARSET);
                if (bytes.length <= 255) {
                    putTagLen(writeBuffer, bytes.length);
                    writeBuffer.put(bytes);
                } else {
                    throw new RobotCoreException(String.format("Telemetry tag cannot exceed %d bytes [%s]", new Object[]{255, this.tag}));
                }
            }
            putCount(writeBuffer, this.dataStrings.size());
            for (Map.Entry next : this.dataStrings.entrySet()) {
                Charset charset = CHARSET;
                byte[] bytes2 = ((String) next.getKey()).getBytes(charset);
                byte[] bytes3 = ((String) next.getValue()).getBytes(charset);
                if (bytes2.length > 65535) {
                    throw new RobotCoreException("telemetry key '%s' too long: %d bytes; max %d bytes", next.getKey(), Integer.valueOf(bytes2.length), 65535);
                } else if (bytes3.length <= 65535) {
                    putKeyLen(writeBuffer, bytes2.length);
                    writeBuffer.put(bytes2);
                    putValueLen(writeBuffer, bytes3.length);
                    writeBuffer.put(bytes3);
                } else {
                    throw new RobotCoreException("telemetry value '%s' too long: %d bytes; max %d bytes", next.getValue(), Integer.valueOf(bytes3.length), 65535);
                }
            }
            putCount(writeBuffer, this.dataNumbers.size());
            for (Map.Entry next2 : this.dataNumbers.entrySet()) {
                byte[] bytes4 = ((String) next2.getKey()).getBytes(CHARSET);
                float floatValue = ((Float) next2.getValue()).floatValue();
                if (bytes4.length <= 65535) {
                    putKeyLen(writeBuffer, bytes4.length);
                    writeBuffer.put(bytes4);
                    writeBuffer.putFloat(floatValue);
                } else {
                    throw new RobotCoreException("telemetry key '%s' too long: %d bytes; max %d bytes", next2.getKey(), Integer.valueOf(bytes4.length), 65535);
                }
            }
        } else {
            throw new RobotCoreException("Cannot have more than %d number data points", 255);
        }
        return writeBuffer.array();
    }

    public synchronized void fromByteArray(byte[] bArr) throws RobotCoreException {
        clearData();
        ByteBuffer readBuffer = getReadBuffer(bArr);
        this.timestamp = readBuffer.getLong();
        this.isSorted = readBuffer.get() != 0;
        this.robotState = RobotState.fromByte(readBuffer.get());
        int tagLen = getTagLen(readBuffer);
        if (tagLen == 0) {
            this.tag = InspectionState.NO_VERSION;
        } else {
            byte[] bArr2 = new byte[tagLen];
            readBuffer.get(bArr2);
            this.tag = new String(bArr2, CHARSET);
        }
        int count = getCount(readBuffer);
        for (int i = 0; i < count; i++) {
            byte[] bArr3 = new byte[getKeyLen(readBuffer)];
            readBuffer.get(bArr3);
            byte[] bArr4 = new byte[getValueLen(readBuffer)];
            readBuffer.get(bArr4);
            Charset charset = CHARSET;
            this.dataStrings.put(new String(bArr3, charset), new String(bArr4, charset));
        }
        int count2 = getCount(readBuffer);
        for (int i2 = 0; i2 < count2; i2++) {
            byte[] bArr5 = new byte[getKeyLen(readBuffer)];
            readBuffer.get(bArr5);
            this.dataNumbers.put(new String(bArr5, CHARSET), Float.valueOf(readBuffer.getFloat()));
        }
    }

    static void putCount(ByteBuffer byteBuffer, int i) {
        byteBuffer.put((byte) i);
    }

    static int getCount(ByteBuffer byteBuffer) {
        return TypeConversion.unsignedByteToInt(byteBuffer.get());
    }

    static void putTagLen(ByteBuffer byteBuffer, int i) {
        byteBuffer.put((byte) i);
    }

    static int getTagLen(ByteBuffer byteBuffer) {
        return TypeConversion.unsignedByteToInt(byteBuffer.get());
    }

    static void putKeyLen(ByteBuffer byteBuffer, int i) {
        byteBuffer.putShort((short) i);
    }

    static int getKeyLen(ByteBuffer byteBuffer) {
        return TypeConversion.unsignedShortToInt(byteBuffer.getShort());
    }

    static void putValueLen(ByteBuffer byteBuffer, int i) {
        putKeyLen(byteBuffer, i);
    }

    static int getValueLen(ByteBuffer byteBuffer) {
        return getKeyLen(byteBuffer);
    }

    private int countMessageBytes() {
        int length = this.tag.getBytes(CHARSET).length + 1 + 10 + 1;
        for (Map.Entry next : this.dataStrings.entrySet()) {
            Charset charset = CHARSET;
            length = length + ((String) next.getKey()).getBytes(charset).length + 2 + ((String) next.getValue()).getBytes(charset).length + 2;
        }
        int i = length + 1;
        for (Map.Entry<String, Float> key : this.dataNumbers.entrySet()) {
            i = i + ((String) key.getKey()).getBytes(CHARSET).length + 2 + 4;
        }
        return i;
    }
}
