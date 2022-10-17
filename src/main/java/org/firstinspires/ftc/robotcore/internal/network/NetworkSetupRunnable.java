package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.PeerDiscoveryManager;
import com.qualcomm.robotcore.robocol.RobocolDatagramSocket;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import java.net.SocketException;
import java.util.Objects;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable;

public class NetworkSetupRunnable implements Runnable {
    public static final String TAG = "SetupRunnable";
    protected CountDownLatch countDownLatch = new CountDownLatch(1);
    protected final ElapsedTime lastRecvPacket;
    protected NetworkConnection networkConnection;
    protected PeerDiscoveryManager peerDiscoveryManager;
    protected RecvLoopRunnable.RecvLoopCallback recvLoopCallback;
    protected volatile RecvLoopRunnable recvLoopRunnable;
    protected ExecutorService recvLoopService;
    protected RobocolDatagramSocket socket;

    public NetworkSetupRunnable(RecvLoopRunnable.RecvLoopCallback recvLoopCallback2, NetworkConnection networkConnection2, ElapsedTime elapsedTime) {
        this.recvLoopCallback = recvLoopCallback2;
        this.networkConnection = networkConnection2;
        this.lastRecvPacket = elapsedTime;
    }

    public void run() {
        ThreadPool.logThreadLifeCycle("SetupRunnable.run()", new Runnable() {
            public void run() {
                try {
                    if (NetworkSetupRunnable.this.socket != null) {
                        NetworkSetupRunnable.this.socket.close();
                    }
                    NetworkSetupRunnable.this.socket = new RobocolDatagramSocket();
                    NetworkSetupRunnable.this.socket.listenUsingDestination(NetworkSetupRunnable.this.networkConnection.getConnectionOwnerAddress());
                } catch (SocketException e) {
                    RobotLog.m46e("Failed to open socket: " + e.toString());
                }
                NetworkSetupRunnable.this.recvLoopService = ThreadPool.newFixedThreadPool(3, "ReceiveLoopService");
                NetworkSetupRunnable.this.recvLoopRunnable = new RecvLoopRunnable(NetworkSetupRunnable.this.recvLoopCallback, NetworkSetupRunnable.this.socket, NetworkSetupRunnable.this.lastRecvPacket);
                RecvLoopRunnable recvLoopRunnable = NetworkSetupRunnable.this.recvLoopRunnable;
                Objects.requireNonNull(recvLoopRunnable);
                RecvLoopRunnable.PacketProcessor packetProcessor = new RecvLoopRunnable.PacketProcessor();
                RecvLoopRunnable recvLoopRunnable2 = NetworkSetupRunnable.this.recvLoopRunnable;
                Objects.requireNonNull(recvLoopRunnable2);
                RecvLoopRunnable.CommandProcessor commandProcessor = new RecvLoopRunnable.CommandProcessor();
                NetworkConnectionHandler.getInstance().setRecvLoopRunnable(NetworkSetupRunnable.this.recvLoopRunnable);
                NetworkSetupRunnable.this.recvLoopService.execute(packetProcessor);
                NetworkSetupRunnable.this.recvLoopService.execute(commandProcessor);
                NetworkSetupRunnable.this.recvLoopService.execute(NetworkSetupRunnable.this.recvLoopRunnable);
                if (NetworkSetupRunnable.this.peerDiscoveryManager != null) {
                    NetworkSetupRunnable.this.peerDiscoveryManager.stop();
                }
                NetworkSetupRunnable networkSetupRunnable = NetworkSetupRunnable.this;
                networkSetupRunnable.peerDiscoveryManager = new PeerDiscoveryManager(networkSetupRunnable.socket, NetworkSetupRunnable.this.networkConnection.getConnectionOwnerAddress());
                NetworkSetupRunnable.this.countDownLatch.countDown();
                RobotLog.m58v("Setup complete");
            }
        });
    }

    public RobocolDatagramSocket getSocket() {
        return this.socket;
    }

    public void injectReceivedCommand(Command command) {
        RecvLoopRunnable recvLoopRunnable2 = this.recvLoopRunnable;
        if (recvLoopRunnable2 != null) {
            recvLoopRunnable2.injectReceivedCommand(command);
        } else {
            RobotLog.m60vv(TAG, "injectReceivedCommand(): recvLoopRunnable==null; command ignored");
        }
    }

    public void stopPeerDiscovery() {
        PeerDiscoveryManager peerDiscoveryManager2 = this.peerDiscoveryManager;
        if (peerDiscoveryManager2 != null) {
            peerDiscoveryManager2.stop();
            this.peerDiscoveryManager = null;
        }
    }

    /* access modifiers changed from: protected */
    public void closeSocket() {
        RobocolDatagramSocket robocolDatagramSocket = this.socket;
        if (robocolDatagramSocket != null) {
            robocolDatagramSocket.close();
            this.socket = null;
        }
    }

    public void shutdown() {
        RobotLog.m54ii(TAG, "Shutting down setup and receive loop");
        try {
            this.countDownLatch.await();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        }
        ExecutorService executorService = this.recvLoopService;
        if (executorService != null) {
            executorService.shutdownNow();
            ThreadPool.awaitTerminationOrExitApplication(this.recvLoopService, 5, TimeUnit.SECONDS, "ReceiveLoopService", "internal error");
            this.recvLoopService = null;
            this.recvLoopRunnable = null;
        }
        stopPeerDiscovery();
        closeSocket();
    }

    public long getRxDataCount() {
        return this.socket.getRxDataCount();
    }

    public long getTxDataCount() {
        return this.socket.getTxDataCount();
    }

    public long getBytesPerSecond() {
        if (this.recvLoopRunnable != null) {
            return this.recvLoopRunnable.getBytesPerSecond();
        }
        return 0;
    }
}
