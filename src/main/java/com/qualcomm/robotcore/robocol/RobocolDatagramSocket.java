package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.util.RobotLog;
import java.io.IOException;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.SocketException;

public class RobocolDatagramSocket {
    private static final boolean DEBUG = false;
    public static final String TAG = "Robocol";
    private static final boolean VERBOSE_DEBUG = false;
    private final Object bindCloseLock = new Object();
    private int msReceiveTimeout;
    private int receiveBufferSize;
    private boolean recvErrorReported = false;
    private final Object recvLock = new Object();
    private long rxDataSample = 0;
    private long rxDataTotal = 0;
    private int sendBufferSize;
    private boolean sendErrorReported = false;
    private final Object sendLock = new Object();
    private DatagramSocket socket;
    private volatile State state = State.CLOSED;
    private boolean trafficDataCollection = false;
    private long txDataSample = 0;
    private long txDataTotal = 0;

    public enum State {
        LISTENING,
        CLOSED,
        ERROR
    }

    public void listenUsingDestination(InetAddress inetAddress) throws SocketException {
        bind(new InetSocketAddress(RobocolConfig.determineBindAddress(inetAddress), RobocolConfig.PORT_NUMBER));
    }

    public void bind(InetSocketAddress inetSocketAddress) throws SocketException {
        synchronized (this.bindCloseLock) {
            if (this.state != State.CLOSED) {
                close();
            }
            this.state = State.LISTENING;
            DatagramSocket datagramSocket = new DatagramSocket(inetSocketAddress);
            this.socket = datagramSocket;
            this.sendErrorReported = false;
            this.recvErrorReported = false;
            datagramSocket.setSoTimeout(300);
            this.receiveBufferSize = Math.min(RobocolConfig.MAX_MAX_PACKET_SIZE, this.socket.getReceiveBufferSize());
            this.sendBufferSize = this.socket.getSendBufferSize();
            this.msReceiveTimeout = this.socket.getSoTimeout();
            RobotLog.m42dd("Robocol", String.format("RobocolDatagramSocket listening addr=%s cbRec=%d cbSend=%d msRecTO=%d", new Object[]{inetSocketAddress.toString(), Integer.valueOf(this.receiveBufferSize), Integer.valueOf(this.sendBufferSize), Integer.valueOf(this.msReceiveTimeout)}));
        }
    }

    public void close() {
        synchronized (this.bindCloseLock) {
            this.state = State.CLOSED;
            DatagramSocket datagramSocket = this.socket;
            if (datagramSocket != null) {
                datagramSocket.close();
            }
            RobotLog.m42dd("Robocol", "RobocolDatagramSocket is closed");
        }
    }

    public void send(RobocolDatagram robocolDatagram) {
        synchronized (this.sendLock) {
            try {
                if (robocolDatagram.getLength() <= this.sendBufferSize) {
                    this.socket.send(robocolDatagram.getPacket());
                    if (this.trafficDataCollection) {
                        this.txDataSample += (long) robocolDatagram.getPayloadLength();
                    }
                } else {
                    throw new RuntimeException(String.format("send packet too large: size=%d max=%d", new Object[]{Integer.valueOf(robocolDatagram.getLength()), Integer.valueOf(this.sendBufferSize)}));
                }
            } catch (RuntimeException e) {
                RobotLog.logExceptionHeader("Robocol", e, "exception sending datagram", new Object[0]);
            } catch (IOException e2) {
                if (!this.sendErrorReported) {
                    this.sendErrorReported = true;
                    RobotLog.logExceptionHeader("Robocol", e2, "exception sending datagram", new Object[0]);
                }
            }
        }
    }

    /* JADX WARNING: Removed duplicated region for block: B:29:0x0040 A[Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.qualcomm.robotcore.robocol.RobocolDatagram recv() {
        /*
            r9 = this;
            java.lang.Object r0 = r9.recvLock
            monitor-enter(r0)
            int r1 = r9.receiveBufferSize     // Catch:{ all -> 0x004e }
            com.qualcomm.robotcore.robocol.RobocolDatagram r1 = com.qualcomm.robotcore.robocol.RobocolDatagram.forReceive(r1)     // Catch:{ all -> 0x004e }
            java.net.DatagramPacket r2 = r1.getPacket()     // Catch:{ all -> 0x004e }
            r3 = 0
            r4 = 0
            java.net.DatagramSocket r5 = r9.socket     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            if (r5 != 0) goto L_0x0015
            monitor-exit(r0)     // Catch:{ all -> 0x004e }
            return r4
        L_0x0015:
            r5.receive(r2)     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            r1.markReceivedNow()     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            boolean r2 = r9.trafficDataCollection     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            if (r2 == 0) goto L_0x0029
            long r5 = r9.rxDataSample     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            int r2 = r1.getPayloadLength()     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            long r7 = (long) r2     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
            long r5 = r5 + r7
            r9.rxDataSample = r5     // Catch:{ SocketException -> 0x003b, SocketTimeoutException -> 0x0039, IOException -> 0x002d, RuntimeException -> 0x002b }
        L_0x0029:
            monitor-exit(r0)     // Catch:{ all -> 0x004e }
            return r1
        L_0x002b:
            r1 = move-exception
            goto L_0x002e
        L_0x002d:
            r1 = move-exception
        L_0x002e:
            java.lang.String r2 = "Robocol"
            java.lang.String r5 = "no packet received"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x004e }
            com.qualcomm.robotcore.util.RobotLog.logExceptionHeader(r2, r1, r5, r3)     // Catch:{ all -> 0x004e }
            monitor-exit(r0)     // Catch:{ all -> 0x004e }
            return r4
        L_0x0039:
            r1 = move-exception
            goto L_0x003c
        L_0x003b:
            r1 = move-exception
        L_0x003c:
            boolean r2 = r9.recvErrorReported     // Catch:{ all -> 0x004e }
            if (r2 != 0) goto L_0x004c
            r2 = 1
            r9.recvErrorReported = r2     // Catch:{ all -> 0x004e }
            java.lang.String r2 = "Robocol"
            java.lang.String r5 = "no packet received"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ all -> 0x004e }
            com.qualcomm.robotcore.util.RobotLog.logExceptionHeader(r2, r1, r5, r3)     // Catch:{ all -> 0x004e }
        L_0x004c:
            monitor-exit(r0)     // Catch:{ all -> 0x004e }
            return r4
        L_0x004e:
            r1 = move-exception
            monitor-exit(r0)     // Catch:{ all -> 0x004e }
            throw r1
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.robotcore.robocol.RobocolDatagramSocket.recv():com.qualcomm.robotcore.robocol.RobocolDatagram");
    }

    public void gatherTrafficData(boolean z) {
        this.trafficDataCollection = z;
    }

    public long getRxDataSample() {
        return this.rxDataSample;
    }

    public long getTxDataSample() {
        return this.txDataSample;
    }

    public long getRxDataCount() {
        return this.rxDataTotal;
    }

    public long getTxDataCount() {
        return this.txDataTotal;
    }

    public void resetDataSample() {
        this.rxDataTotal += this.rxDataSample;
        this.txDataTotal += this.txDataSample;
        this.rxDataSample = 0;
        this.txDataSample = 0;
    }

    public State getState() {
        return this.state;
    }

    public InetAddress getInetAddress() {
        DatagramSocket datagramSocket = this.socket;
        if (datagramSocket == null) {
            return null;
        }
        return datagramSocket.getInetAddress();
    }

    public InetAddress getLocalAddress() {
        DatagramSocket datagramSocket = this.socket;
        if (datagramSocket == null) {
            return null;
        }
        return datagramSocket.getLocalAddress();
    }

    public boolean isRunning() {
        return this.state == State.LISTENING;
    }

    public boolean isClosed() {
        return this.state == State.CLOSED;
    }
}
