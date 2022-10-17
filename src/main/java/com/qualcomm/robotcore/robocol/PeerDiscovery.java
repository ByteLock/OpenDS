package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.RobotProtocolException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.TypeConversion;
import java.nio.BufferOverflowException;
import java.nio.ByteBuffer;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.threeten.p014bp.YearMonth;

public class PeerDiscovery extends RobocolParsableBase {
    public static final String TAG = "PeerDiscovery";
    static final int cbBufferHistorical = 13;
    static final int cbPayloadHistorical = 10;
    private PeerType peerType;
    private byte sdkBuildMonth;
    private short sdkBuildYear;
    private int sdkMajorVersion;
    private int sdkMinorVersion;

    public enum PeerType {
        NOT_SET(0),
        PEER(1),
        GROUP_OWNER(2),
        NOT_CONNECTED_DUE_TO_PREEXISTING_CONNECTION(3);
        
        private static final PeerType[] VALUES_CACHE = null;
        private final int type;

        static {
            VALUES_CACHE = values();
        }

        public static PeerType fromByte(byte b) {
            PeerType peerType = NOT_SET;
            try {
                return VALUES_CACHE[b];
            } catch (ArrayIndexOutOfBoundsException e) {
                RobotLog.m67ww("PeerDiscovery", "Cannot convert %d to Peer: %s", Byte.valueOf(b), e.toString());
                return peerType;
            }
        }

        private PeerType(int i) {
            this.type = i;
        }

        public byte asByte() {
            return (byte) this.type;
        }
    }

    public static PeerDiscovery forReceive() {
        return new PeerDiscovery(PeerType.NOT_SET, (byte) 1, 1, 0, 0);
    }

    public static PeerDiscovery forTransmission(PeerType peerType2) {
        YearMonth localSdkBuildMonth = AppUtil.getInstance().getLocalSdkBuildMonth();
        return new PeerDiscovery(peerType2, (byte) localSdkBuildMonth.getMonthValue(), (short) localSdkBuildMonth.getYear(), 8, 0);
    }

    private PeerDiscovery(PeerType peerType2, byte b, short s, int i, int i2) {
        this.peerType = peerType2;
        this.sdkBuildMonth = b;
        this.sdkBuildYear = s;
        this.sdkMajorVersion = i;
        this.sdkMinorVersion = i2;
    }

    public PeerType getPeerType() {
        return this.peerType;
    }

    public YearMonth getSdkBuildMonth() {
        byte b = this.sdkBuildMonth;
        if (b < 1 || b > 12) {
            return YearMonth.m114of(1, 1);
        }
        return YearMonth.m114of((int) this.sdkBuildYear, (int) b);
    }

    public boolean isSdkBuildMonthValid() {
        return this.sdkBuildMonth > 0;
    }

    public int getSdkMajorVersion() {
        return this.sdkMajorVersion;
    }

    public int getSdkMinorVersion() {
        return this.sdkMinorVersion;
    }

    public RobocolParsable.MsgType getRobocolMsgType() {
        return RobocolParsable.MsgType.PEER_DISCOVERY;
    }

    public byte[] toByteArray() throws RobotCoreException {
        ByteBuffer allocateWholeWriteBuffer = allocateWholeWriteBuffer(13);
        try {
            allocateWholeWriteBuffer.put(getRobocolMsgType().asByte());
            allocateWholeWriteBuffer.putShort(10);
            allocateWholeWriteBuffer.put((byte) 123);
            allocateWholeWriteBuffer.put(this.peerType.asByte());
            allocateWholeWriteBuffer.putShort((short) this.sequenceNumber);
            allocateWholeWriteBuffer.put(this.sdkBuildMonth);
            allocateWholeWriteBuffer.putShort(this.sdkBuildYear);
            allocateWholeWriteBuffer.put((byte) this.sdkMajorVersion);
            allocateWholeWriteBuffer.put((byte) this.sdkMinorVersion);
        } catch (BufferOverflowException e) {
            RobotLog.logStacktrace(e);
        }
        return allocateWholeWriteBuffer.array();
    }

    public void fromByteArray(byte[] bArr) throws RobotCoreException, RobotProtocolException {
        if (bArr.length >= 13) {
            ByteBuffer wholeReadBuffer = getWholeReadBuffer(bArr);
            wholeReadBuffer.get();
            wholeReadBuffer.getShort();
            byte b = wholeReadBuffer.get() & 255;
            byte b2 = wholeReadBuffer.get();
            short s = wholeReadBuffer.getShort();
            this.sdkBuildMonth = wholeReadBuffer.get();
            this.sdkBuildYear = wholeReadBuffer.getShort();
            this.sdkMajorVersion = TypeConversion.unsignedByteToInt(wholeReadBuffer.get());
            this.sdkMinorVersion = TypeConversion.unsignedByteToInt(wholeReadBuffer.get());
            if (b != 123) {
                RobotLog.m49ee("PeerDiscovery", "Incompatible robocol versions, remote: %d, local: %d", Integer.valueOf(b), 123);
                String str = "Robot Controller";
                if (!AppUtil.getInstance().isRobotController() ? b >= 123 : b < 123) {
                    str = "Driver Station";
                }
                throw new RobotProtocolException(AppUtil.getDefContext().getString(C0705R.string.incompatibleAppsError), str);
            }
            this.peerType = PeerType.fromByte(b2);
            if (b > 1) {
                setSequenceNumber(s);
                return;
            }
            return;
        }
        throw new RobotCoreException("Expected buffer of at least %d bytes, received %d", 13, Integer.valueOf(bArr.length));
    }

    public String toString() {
        return String.format("Peer Discovery - peer type: %s", new Object[]{this.peerType.name()});
    }
}
