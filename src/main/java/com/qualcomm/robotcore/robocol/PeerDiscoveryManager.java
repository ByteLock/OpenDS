package com.qualcomm.robotcore.robocol;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.PeerDiscovery;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.net.InetAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class PeerDiscoveryManager {
    private static final boolean DEBUG = false;
    public static final String TAG = "PeerDiscovery";
    private ScheduledFuture<?> discoveryLoopFuture;
    private ScheduledExecutorService discoveryLoopService;
    private CountDownLatch interlock = new CountDownLatch(1);
    /* access modifiers changed from: private */
    public final PeerDiscovery message;
    /* access modifiers changed from: private */
    public InetAddress peerDiscoveryDevice;
    /* access modifiers changed from: private */
    public final RobocolDatagramSocket socket;

    private class PeerDiscoveryRunnable implements Runnable {
        private PeerDiscoveryRunnable() {
        }

        public void run() {
            try {
                PeerDiscoveryManager.this.socket.send(new RobocolDatagram(PeerDiscoveryManager.this.message, PeerDiscoveryManager.this.peerDiscoveryDevice));
            } catch (RobotCoreException e) {
                RobotLog.m48ee("PeerDiscovery", "Unable to send peer discovery packet: " + e.toString());
            }
        }
    }

    public PeerDiscoveryManager(RobocolDatagramSocket robocolDatagramSocket, InetAddress inetAddress) {
        this.socket = robocolDatagramSocket;
        this.message = PeerDiscovery.forTransmission(PeerDiscovery.PeerType.PEER);
        this.peerDiscoveryDevice = inetAddress;
        start();
    }

    public InetAddress getPeerDiscoveryDevice() {
        return this.peerDiscoveryDevice;
    }

    private void start() {
        RobotLog.m60vv("PeerDiscovery", "Starting peer discovery remote: " + this.peerDiscoveryDevice.toString() + " local: " + this.socket.getLocalAddress().toString());
        if (this.peerDiscoveryDevice.equals(this.socket.getLocalAddress())) {
            RobotLog.m60vv("PeerDiscovery", "No need for initiating peer discovery, we are the Robot Controller");
        } else {
            ThreadPool.RecordingScheduledExecutor newScheduledExecutor = ThreadPool.newScheduledExecutor(1, "discovery service");
            this.discoveryLoopService = newScheduledExecutor;
            this.discoveryLoopFuture = newScheduledExecutor.scheduleAtFixedRate(new PeerDiscoveryRunnable(), 1, 1, TimeUnit.SECONDS);
        }
        this.interlock.countDown();
    }

    public void stop() {
        RobotLog.m60vv("PeerDiscovery", "Stopping peer discovery");
        try {
            this.interlock.await();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        ScheduledFuture<?> scheduledFuture = this.discoveryLoopFuture;
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            this.discoveryLoopFuture = null;
        }
    }
}
