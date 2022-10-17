package org.firstinspires.ftc.robotcore.internal.network;

import android.os.SystemClock;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.KeepAlive;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.List;
import java.util.TimeZone;
import java.util.concurrent.CopyOnWriteArrayList;
import org.firstinspires.ftc.robotcore.internal.p013ui.RobotCoreGamepadManager;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class SendOnceRunnable implements Runnable {
    public static final double ASSUME_DISCONNECT_TIMER = 2.0d;
    public static boolean DEBUG = false;
    public static final long GAMEPAD_UPDATE_THRESHOLD = 1000;
    public static final int MAX_COMMAND_ATTEMPTS = 10;
    public static final int MS_BATCH_TRANSMISSION_INTERVAL = 40;
    public static final int MS_HEARTBEAT_TRANSMISSION_INTERVAL = 100;
    public static final int MS_KEEPALIVE_TRANSMISSION_INTERVAL = 20;
    public static final String TAG = "Robocol";
    protected final AppUtil appUtil = AppUtil.getInstance();
    protected DisconnectionCallback disconnectionCallback;
    protected Heartbeat heartbeatSend = new Heartbeat();
    protected KeepAlive keepAliveSend = new KeepAlive();
    protected final ElapsedTime lastRecvPacket;
    protected final Parameters parameters;
    protected volatile List<Command> pendingCommands = new CopyOnWriteArrayList();

    public interface DisconnectionCallback {
        void disconnected();
    }

    public static class Parameters {
        public boolean disconnectOnTimeout = true;
        public volatile RobotCoreGamepadManager gamepadManager = null;
        public boolean originateHeartbeats = AppUtil.getInstance().isDriverStation();
        public boolean originateKeepAlives = false;
    }

    public SendOnceRunnable(DisconnectionCallback disconnectionCallback2, ElapsedTime elapsedTime) {
        this.disconnectionCallback = disconnectionCallback2;
        this.lastRecvPacket = elapsedTime;
        this.parameters = new Parameters();
        RobotLog.m60vv("Robocol", "SendOnceRunnable created");
    }

    public void run() {
        boolean z;
        NetworkConnectionHandler instance = NetworkConnectionHandler.getInstance();
        try {
            double seconds = this.lastRecvPacket.seconds();
            if (!this.parameters.disconnectOnTimeout || seconds <= 2.0d) {
                if (!this.parameters.originateHeartbeats || this.heartbeatSend.getElapsedSeconds() <= 0.1d) {
                    z = false;
                } else {
                    Heartbeat createWithTimeStamp = Heartbeat.createWithTimeStamp();
                    this.heartbeatSend = createWithTimeStamp;
                    createWithTimeStamp.setTimeZoneId(TimeZone.getDefault().getID());
                    this.heartbeatSend.f135t0 = this.appUtil.getWallClockTime();
                    instance.sendDataToPeer(this.heartbeatSend);
                    z = true;
                }
                if (this.parameters.gamepadManager != null) {
                    long uptimeMillis = SystemClock.uptimeMillis();
                    for (Gamepad next : this.parameters.gamepadManager.getGamepadsForTransmission()) {
                        if (uptimeMillis - next.timestamp <= 1000 || !next.atRest()) {
                            next.setSequenceNumber();
                            instance.sendDataToPeer(next);
                            z = true;
                        }
                    }
                }
                if (!z && this.parameters.originateKeepAlives && this.keepAliveSend.getElapsedSeconds() > 0.02d) {
                    KeepAlive createWithTimeStamp2 = KeepAlive.createWithTimeStamp();
                    this.keepAliveSend = createWithTimeStamp2;
                    instance.sendDataToPeer(createWithTimeStamp2);
                }
                long nanoTime = System.nanoTime();
                ArrayList arrayList = new ArrayList();
                for (Command next2 : this.pendingCommands) {
                    if (next2.getAttempts() <= 10) {
                        if (!next2.hasExpired()) {
                            if (next2.isAcknowledged() || next2.shouldTransmit(nanoTime)) {
                                if (!next2.isAcknowledged()) {
                                    RobotLog.m61vv("Robocol", "sending %s(%d), attempt: %d", next2.getName(), Integer.valueOf(next2.getSequenceNumber()), Byte.valueOf(next2.getAttempts()));
                                } else if (DEBUG) {
                                    RobotLog.m61vv("Robocol", "acking %s(%d)", next2.getName(), Integer.valueOf(next2.getSequenceNumber()));
                                }
                                instance.sendDataToPeer(next2);
                                if (next2.isAcknowledged()) {
                                    arrayList.add(next2);
                                }
                            }
                        }
                    }
                    RobotLog.m60vv("Robocol", String.format(AppUtil.getDefContext().getString(C0705R.string.configGivingUpOnCommand), new Object[]{next2.getName(), Integer.valueOf(next2.getSequenceNumber()), Byte.valueOf(next2.getAttempts())}));
                    arrayList.add(next2);
                }
                this.pendingCommands.removeAll(arrayList);
                return;
            }
            this.disconnectionCallback.disconnected();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void sendCommand(Command command) {
        this.pendingCommands.add(command);
    }

    public boolean removeCommand(Command command) {
        return this.pendingCommands.remove(command);
    }

    public void clearCommands() {
        this.pendingCommands.clear();
    }
}
