package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import java.net.DatagramPacket;
import java.net.InetAddress;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class RobocolDatagram {
    public static final String TAG = "Robocol";
    static Queue<byte[]> receiveBuffers = new ConcurrentLinkedQueue();
    private long nanoTimeReceived;
    private DatagramPacket packet;
    private byte[] receiveBuffer;
    private final Object receivedTimeLock;
    private long wallClockTimeMsReceived;

    public RobocolDatagram(RobocolParsable robocolParsable, InetAddress inetAddress) throws RobotCoreException {
        this.receiveBuffer = null;
        this.receivedTimeLock = new Object();
        this.wallClockTimeMsReceived = 0;
        this.nanoTimeReceived = 0;
        byte[] byteArrayForTransmission = robocolParsable.toByteArrayForTransmission();
        this.packet = new DatagramPacket(byteArrayForTransmission, byteArrayForTransmission.length, inetAddress, RobocolConfig.PORT_NUMBER);
    }

    public static RobocolDatagram forReceive(int i) {
        byte[] poll = receiveBuffers.poll();
        if (poll == null || poll.length != i) {
            poll = new byte[i];
        }
        DatagramPacket datagramPacket = new DatagramPacket(poll, poll.length);
        RobocolDatagram robocolDatagram = new RobocolDatagram();
        robocolDatagram.packet = datagramPacket;
        robocolDatagram.receiveBuffer = poll;
        return robocolDatagram;
    }

    private RobocolDatagram() {
        this.receiveBuffer = null;
        this.receivedTimeLock = new Object();
        this.wallClockTimeMsReceived = 0;
        this.nanoTimeReceived = 0;
        this.packet = null;
    }

    public void close() {
        byte[] bArr = this.receiveBuffer;
        if (bArr != null) {
            receiveBuffers.add(bArr);
            this.receiveBuffer = null;
        }
        this.packet = null;
    }

    public RobocolParsable.MsgType getMsgType() {
        return RobocolParsable.MsgType.fromByte(this.packet.getData()[0]);
    }

    public int getLength() {
        return this.packet.getLength();
    }

    public int getPayloadLength() {
        return this.packet.getLength() - 5;
    }

    public byte[] getData() {
        return this.packet.getData();
    }

    public InetAddress getAddress() {
        return this.packet.getAddress();
    }

    public int getPort() {
        return this.packet.getPort();
    }

    public long getWallClockTimeMsReceived() {
        long j;
        synchronized (this.receivedTimeLock) {
            j = this.wallClockTimeMsReceived;
        }
        return j;
    }

    public long getNanoTimeReceived() {
        long j;
        synchronized (this.receivedTimeLock) {
            j = this.nanoTimeReceived;
        }
        return j;
    }

    public void setAddress(InetAddress inetAddress) {
        this.packet.setAddress(inetAddress);
    }

    public String toString() {
        String str;
        int i;
        String str2;
        DatagramPacket datagramPacket = this.packet;
        if (datagramPacket == null || datagramPacket.getAddress() == null || this.packet.getLength() <= 0) {
            str2 = "NONE";
            str = null;
            i = 0;
        } else {
            str2 = RobocolParsable.MsgType.fromByte(this.packet.getData()[0]).name();
            i = this.packet.getLength();
            str = this.packet.getAddress().getHostAddress();
        }
        return String.format("RobocolDatagram - type:%s, addr:%s, size:%d", new Object[]{str2, str, Integer.valueOf(i)});
    }

    /* access modifiers changed from: protected */
    public DatagramPacket getPacket() {
        return this.packet;
    }

    /* access modifiers changed from: protected */
    public void setPacket(DatagramPacket datagramPacket) {
        this.packet = datagramPacket;
    }

    /* access modifiers changed from: protected */
    public void markReceivedNow() {
        synchronized (this.receivedTimeLock) {
            this.wallClockTimeMsReceived = AppUtil.getInstance().getWallClockTime();
            this.nanoTimeReceived = System.nanoTime();
        }
    }
}
