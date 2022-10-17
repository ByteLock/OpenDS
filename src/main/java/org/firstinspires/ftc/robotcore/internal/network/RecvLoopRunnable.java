package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.RobocolDatagramSocket;
import com.qualcomm.robotcore.robocol.RobocolParsable;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.concurrent.LinkedBlockingDeque;

public class RecvLoopRunnable implements Runnable {
    private static final int BANDWIDTH_SAMPLE_PERIOD = 500;
    public static boolean DEBUG = false;
    /* access modifiers changed from: private */
    public static boolean DO_TRAFFIC_DATA = false;
    public static final String TAG = "Robocol";
    /* access modifiers changed from: private */
    public static ElapsedTime bandwidthSampleTimer = new ElapsedTime(ElapsedTime.Resolution.MILLISECONDS);
    private double bytesPerMilli = LynxServoController.apiPositionFirst;
    protected RecvLoopCallback callback;
    protected ElapsedTime commandProcessingTimer;
    protected LinkedBlockingDeque<Command> commandsToProcess = new LinkedBlockingDeque<>();
    protected ElapsedTime lastRecvPacket;
    protected double msCommandProcessingTimerReportingThreshold;
    protected double msPacketProcessingTimerReportingThreshold;
    protected ElapsedTime packetProcessingTimer;
    protected LinkedBlockingDeque<RobocolDatagram> packetsToProcess = new LinkedBlockingDeque<>();
    protected RobocolDatagramSocket socket;

    public interface RecvLoopCallback {
        CallbackResult commandEvent(Command command) throws RobotCoreException;

        CallbackResult emptyEvent(RobocolDatagram robocolDatagram) throws RobotCoreException;

        CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) throws RobotCoreException;

        CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) throws RobotCoreException;

        CallbackResult packetReceived(RobocolDatagram robocolDatagram) throws RobotCoreException;

        CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException;

        CallbackResult reportGlobalError(String str, boolean z);

        CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException;
    }

    public static abstract class DegenerateCallback implements RecvLoopCallback {
        public CallbackResult packetReceived(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult commandEvent(Command command) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult emptyEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
            return CallbackResult.NOT_HANDLED;
        }

        public CallbackResult reportGlobalError(String str, boolean z) {
            return CallbackResult.NOT_HANDLED;
        }
    }

    public RecvLoopRunnable(RecvLoopCallback recvLoopCallback, RobocolDatagramSocket robocolDatagramSocket, ElapsedTime elapsedTime) {
        this.callback = recvLoopCallback;
        this.socket = robocolDatagramSocket;
        this.lastRecvPacket = elapsedTime;
        this.packetProcessingTimer = new ElapsedTime();
        this.commandProcessingTimer = new ElapsedTime();
        this.msCommandProcessingTimerReportingThreshold = 500.0d;
        this.msPacketProcessingTimerReportingThreshold = 50.0d;
        this.socket.gatherTrafficData(DO_TRAFFIC_DATA);
        RobotLog.m60vv("Robocol", "RecvLoopRunnable created");
    }

    public void setCallback(RecvLoopCallback recvLoopCallback) {
        this.callback = recvLoopCallback;
    }

    public class PacketProcessor implements Runnable {
        public PacketProcessor() {
        }

        public void run() {
            RobocolDatagram takeFirst;
            RobotLog.m60vv("Robocol", "PacketProcessor started");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    takeFirst = RecvLoopRunnable.this.packetsToProcess.takeFirst();
                    try {
                        RecvLoopRunnable.this.packetProcessingTimer.reset();
                        if (RecvLoopRunnable.this.callback.packetReceived(takeFirst) != CallbackResult.HANDLED) {
                            switch (C11002.f270x658239ea[takeFirst.getMsgType().ordinal()]) {
                                case 1:
                                    RecvLoopRunnable.this.callback.peerDiscoveryEvent(takeFirst);
                                    break;
                                case 2:
                                    Command command = new Command(takeFirst);
                                    if (!NetworkConnectionHandler.getInstance().processAcknowledgments(command).isHandled()) {
                                        RobotLog.m61vv("Robocol", "received command: %s(%d) %s", command.getName(), Integer.valueOf(command.getSequenceNumber()), command.getExtra());
                                        RecvLoopRunnable.this.commandsToProcess.addLast(command);
                                        break;
                                    }
                                    break;
                                case 3:
                                    RecvLoopRunnable.this.callback.telemetryEvent(takeFirst);
                                    break;
                                case 4:
                                    RecvLoopRunnable.this.callback.gamepadEvent(takeFirst);
                                    break;
                                case 5:
                                    RecvLoopRunnable.this.callback.emptyEvent(takeFirst);
                                    break;
                                case 6:
                                    break;
                                default:
                                    RobotLog.m48ee("Robocol", "Unhandled message type: " + takeFirst.getMsgType().name());
                                    break;
                            }
                        }
                        double milliseconds = RecvLoopRunnable.this.packetProcessingTimer.milliseconds();
                        if (milliseconds > RecvLoopRunnable.this.msPacketProcessingTimerReportingThreshold) {
                            RobotLog.m61vv("Robocol", "packet processing took %.1fms: type=%s", Double.valueOf(milliseconds), takeFirst.getMsgType().toString());
                        }
                    } catch (RobotCoreException e) {
                        RobotLog.m51ee("Robocol", e, "exception in PacketProcessor thread %s", Thread.currentThread().getName());
                        RecvLoopRunnable.this.callback.reportGlobalError(e.getMessage(), false);
                    }
                    takeFirst.close();
                } catch (InterruptedException unused) {
                    RobotLog.m60vv("Robocol", "PacketProcessor exiting");
                    return;
                } catch (Throwable th) {
                    takeFirst.close();
                    throw th;
                }
            }
        }
    }

    /* renamed from: org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable$2 */
    static /* synthetic */ class C11002 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$robocol$RobocolParsable$MsgType */
        static final /* synthetic */ int[] f270x658239ea;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|14) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType[] r0 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f270x658239ea = r0
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.PEER_DISCOVERY     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f270x658239ea     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.COMMAND     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f270x658239ea     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.TELEMETRY     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f270x658239ea     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.GAMEPAD     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f270x658239ea     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.EMPTY     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = f270x658239ea     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.robotcore.robocol.RobocolParsable$MsgType r1 = com.qualcomm.robotcore.robocol.RobocolParsable.MsgType.KEEPALIVE     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: org.firstinspires.ftc.robotcore.internal.network.RecvLoopRunnable.C11002.<clinit>():void");
        }
    }

    public class CommandProcessor implements Runnable {
        public CommandProcessor() {
        }

        public void run() {
            RobotLog.m60vv("Robocol", "CommandProcessor started");
            while (!Thread.currentThread().isInterrupted()) {
                try {
                    Command takeFirst = RecvLoopRunnable.this.commandsToProcess.takeFirst();
                    RecvLoopRunnable.this.commandProcessingTimer.reset();
                    if (RecvLoopRunnable.DEBUG) {
                        RobotLog.m61vv("Robocol", "command=%s...", takeFirst.getName());
                    }
                    RecvLoopRunnable.this.callback.commandEvent(takeFirst);
                    if (RecvLoopRunnable.DEBUG) {
                        RobotLog.m61vv("Robocol", "...command=%s", takeFirst.getName());
                    }
                    double milliseconds = RecvLoopRunnable.this.commandProcessingTimer.milliseconds();
                    if (milliseconds > RecvLoopRunnable.this.msCommandProcessingTimerReportingThreshold) {
                        RobotLog.m49ee("Robocol", "command processing took %d ms: command=%s", Integer.valueOf((int) milliseconds), takeFirst.getName());
                    }
                } catch (InterruptedException unused) {
                    RobotLog.m60vv("Robocol", "CommandProcessor exiting");
                    return;
                } catch (RobotCoreException | RuntimeException e) {
                    RobotLog.m51ee("Robocol", e, "exception in CommandProcessor thread %s", Thread.currentThread().getName());
                    RecvLoopRunnable.this.callback.reportGlobalError(e.getMessage(), false);
                }
            }
        }
    }

    public void injectReceivedCommand(Command command) {
        this.commandsToProcess.addLast(command);
    }

    public long getBytesPerSecond() {
        return (long) (this.bytesPerMilli * 1000.0d);
    }

    /* access modifiers changed from: protected */
    public void calculateBytesPerMilli() {
        if (bandwidthSampleTimer.time() >= 500.0d) {
            this.bytesPerMilli = ((double) (this.socket.getRxDataSample() + this.socket.getTxDataSample())) / bandwidthSampleTimer.time();
            bandwidthSampleTimer.reset();
            this.socket.resetDataSample();
        }
    }

    public void run() {
        ThreadPool.logThreadLifeCycle("RecvLoopRunnable.run()", new Runnable() {
            public void run() {
                RecvLoopRunnable.bandwidthSampleTimer.reset();
                while (!Thread.currentThread().isInterrupted()) {
                    RobocolDatagram recv = RecvLoopRunnable.this.socket.recv();
                    if (Thread.currentThread().isInterrupted()) {
                        RobotLog.m60vv("Robocol", "RecvLoopRunnable interrupted and exiting");
                        return;
                    } else if (recv != null) {
                        boolean equals = recv.getAddress().equals(NetworkConnectionHandler.getInstance().getCurrentPeerAddr());
                        if (equals || recv.getMsgType() == RobocolParsable.MsgType.PEER_DISCOVERY) {
                            if (equals && RecvLoopRunnable.this.lastRecvPacket != null) {
                                RecvLoopRunnable.this.lastRecvPacket.reset();
                            }
                            if (recv.getMsgType() == RobocolParsable.MsgType.HEARTBEAT) {
                                try {
                                    if (RecvLoopRunnable.this.callback.packetReceived(recv) != CallbackResult.HANDLED) {
                                        RecvLoopRunnable.this.callback.heartbeatEvent(recv);
                                    }
                                } catch (RobotCoreException e) {
                                    RobotLog.m51ee("Robocol", e, "exception processing heartbeat", Thread.currentThread().getName());
                                    RecvLoopRunnable.this.callback.reportGlobalError(e.getMessage(), false);
                                }
                            } else {
                                RecvLoopRunnable.this.packetsToProcess.addLast(recv);
                            }
                            if (RecvLoopRunnable.DO_TRAFFIC_DATA) {
                                RecvLoopRunnable.this.calculateBytesPerMilli();
                            }
                        } else {
                            recv.close();
                        }
                    } else if (RecvLoopRunnable.this.socket.isClosed()) {
                        RobotLog.m61vv("Robocol", "socket closed; %s returning", Thread.currentThread().getName());
                        return;
                    } else {
                        Thread.yield();
                    }
                }
                RobotLog.m61vv("Robocol", "interrupted; %s returning", Thread.currentThread().getName());
            }
        });
    }
}
