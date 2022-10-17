package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.hardware.lynx.LynxModuleWarningManager;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.LynxUnsupportedCommandException;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.hardware.lynx.commands.standard.LynxAck;
import com.qualcomm.hardware.lynx.commands.standard.LynxNack;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

public abstract class LynxRespondable<RESPONSE extends LynxMessage> extends LynxMessage {
    private final CountDownLatch ackOrNackReceived;
    private final RESPONSE defaultResponse;
    private volatile boolean isAckOrResponseReceived;
    private volatile LynxNack nackReceived;
    private volatile RESPONSE response;
    private final CountDownLatch responseOrNackReceived;

    /* access modifiers changed from: protected */
    public int getMsAwaitInterval() {
        return SyncdDevice.msAbnormalReopenInterval;
    }

    /* access modifiers changed from: protected */
    public int getMsRetransmissionInterval() {
        return 100;
    }

    public boolean isAckable() {
        return true;
    }

    /* access modifiers changed from: protected */
    public boolean usePretendResponseIfRealModuleDoesntSupport() {
        return false;
    }

    public LynxRespondable(LynxModuleIntf lynxModuleIntf) {
        super(lynxModuleIntf);
        this.isAckOrResponseReceived = false;
        this.nackReceived = null;
        this.response = null;
        this.ackOrNackReceived = new CountDownLatch(1);
        this.responseOrNackReceived = new CountDownLatch(1);
        this.defaultResponse = null;
    }

    public LynxRespondable(LynxModuleIntf lynxModuleIntf, RESPONSE response2) {
        super(lynxModuleIntf);
        this.isAckOrResponseReceived = false;
        this.nackReceived = null;
        this.response = null;
        this.ackOrNackReceived = new CountDownLatch(1);
        this.responseOrNackReceived = new CountDownLatch(1);
        this.defaultResponse = response2;
        response2.setPayloadTimeWindow(new TimeWindow());
    }

    public void onPretendTransmit() throws InterruptedException {
        super.onPretendTransmit();
        pretendFinish();
    }

    public boolean hasBeenAcknowledged() {
        return isAckOrResponseReceived() || isNackReceived();
    }

    public boolean isAckOrResponseReceived() {
        return this.isAckOrResponseReceived;
    }

    public boolean isNackReceived() {
        return this.nackReceived != null;
    }

    public LynxNack getNackReceived() {
        return this.nackReceived;
    }

    public final boolean isResponseExpected() {
        return this.defaultResponse != null;
    }

    public void pretendFinish() throws InterruptedException {
        this.isAckOrResponseReceived = true;
        if (isResponseExpected()) {
            this.response = this.defaultResponse;
            onResponseReceived();
        }
        if (this.module != null) {
            this.module.finishedWithMessage(this);
        }
        this.ackOrNackReceived.countDown();
    }

    public void onAckReceived(LynxAck lynxAck) {
        if (!this.isAckOrResponseReceived) {
            this.isAckOrResponseReceived = true;
            if (lynxAck.isAttentionRequired()) {
                noteAttentionRequired();
            }
            this.ackOrNackReceived.countDown();
        }
    }

    /* access modifiers changed from: protected */
    public void noteAttentionRequired() {
        this.module.noteAttentionRequired();
    }

    public void onResponseReceived(LynxMessage lynxMessage) {
        this.response = lynxMessage;
        onResponseReceived();
    }

    private void onResponseReceived() {
        if (isResponseExpected()) {
            this.isAckOrResponseReceived = true;
            this.responseOrNackReceived.countDown();
            return;
        }
        RobotLog.m47e("internal error: unexpected response received for msg#=%d", Integer.valueOf(getMessageNumber()));
    }

    /* renamed from: com.qualcomm.hardware.lynx.commands.LynxRespondable$1 */
    static /* synthetic */ class C06971 {

        /* renamed from: $SwitchMap$com$qualcomm$hardware$lynx$commands$standard$LynxNack$StandardReasonCode */
        static final /* synthetic */ int[] f88xe0728a3e;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|(3:13|14|16)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(16:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|16) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode[] r0 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f88xe0728a3e = r0
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.COMMAND_IMPL_PENDING     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.I2C_NO_RESULTS_PENDING     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.I2C_OPERATION_IN_PROGRESS     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_ACK     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_RESPONSE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.BATTERY_TOO_LOW_TO_RUN_MOTOR     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = f88xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.BATTERY_TOO_LOW_TO_RUN_SERVO     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.LynxRespondable.C06971.<clinit>():void");
        }
    }

    public void onNackReceived(LynxNack lynxNack) {
        switch (C06971.f88xe0728a3e[lynxNack.getNackReasonCodeAsEnum().ordinal()]) {
            case 1:
            case 2:
            case 3:
            case 4:
            case 5:
            case 6:
            case 7:
                break;
            default:
                RobotLog.m59v("nack rec'd mod=%d msg#=%d ref#=%d reason=%s:%d", Integer.valueOf(getModuleAddress()), Integer.valueOf(getMessageNumber()), Integer.valueOf(getReferenceNumber()), lynxNack.getNackReasonCode().toString(), Integer.valueOf(lynxNack.getNackReasonCode().getValue()));
                break;
        }
        this.nackReceived = lynxNack;
        this.ackOrNackReceived.countDown();
        this.responseOrNackReceived.countDown();
    }

    public void send() throws InterruptedException, LynxNackException {
        if (this.ackOrNackReceived.getCount() == 0 || this.responseOrNackReceived.getCount() == 0) {
            throw new RuntimeException("A LynxRespondable can only be sent once");
        }
        acquireNetworkLock();
        try {
            this.module.sendCommand(this);
        } catch (LynxUnsupportedCommandException e) {
            throwNackForUnsupportedCommand(e);
        } catch (Throwable th) {
            releaseNetworkLock();
            throw th;
        }
        awaitAckResponseOrNack();
        throwIfNack();
        releaseNetworkLock();
    }

    /* JADX WARNING: Unknown top exception splitter block from list: {B:18:0x003a=Splitter:B:18:0x003a, B:32:0x005f=Splitter:B:32:0x005f} */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public RESPONSE sendReceive() throws java.lang.InterruptedException, com.qualcomm.hardware.lynx.LynxNackException {
        /*
            r4 = this;
            java.util.concurrent.CountDownLatch r0 = r4.ackOrNackReceived
            long r0 = r0.getCount()
            r2 = 0
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 == 0) goto L_0x0064
            java.util.concurrent.CountDownLatch r0 = r4.responseOrNackReceived
            long r0 = r0.getCount()
            int r0 = (r0 > r2 ? 1 : (r0 == r2 ? 0 : -1))
            if (r0 == 0) goto L_0x0064
            r4.acquireNetworkLock()
            com.qualcomm.hardware.lynx.LynxModuleIntf r0 = r4.module     // Catch:{ LynxNackException -> 0x0042, LynxUnsupportedCommandException -> 0x002b }
            r0.sendCommand(r4)     // Catch:{ LynxNackException -> 0x0042, LynxUnsupportedCommandException -> 0x002b }
            r4.awaitAckResponseOrNack()     // Catch:{ LynxNackException -> 0x0042, LynxUnsupportedCommandException -> 0x002b }
            com.qualcomm.hardware.lynx.commands.LynxMessage r0 = r4.responseOrThrow()     // Catch:{ LynxNackException -> 0x0042, LynxUnsupportedCommandException -> 0x002b }
            r4.releaseNetworkLock()
            return r0
        L_0x0029:
            r0 = move-exception
            goto L_0x0060
        L_0x002b:
            r0 = move-exception
            boolean r1 = r4.usePretendResponseIfRealModuleDoesntSupport()     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x003a
            RESPONSE r1 = r4.defaultResponse     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x003a
            r4.releaseNetworkLock()
            return r1
        L_0x003a:
            r4.throwNackForUnsupportedCommand(r0)     // Catch:{ all -> 0x0029 }
            r0 = 0
            r4.releaseNetworkLock()
            return r0
        L_0x0042:
            r0 = move-exception
            com.qualcomm.hardware.lynx.commands.standard.LynxNack r1 = r0.getNack()     // Catch:{ all -> 0x0029 }
            com.qualcomm.hardware.lynx.commands.standard.LynxNack$ReasonCode r1 = r1.getNackReasonCode()     // Catch:{ all -> 0x0029 }
            boolean r1 = r1.isUnsupportedReason()     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x005f
            boolean r1 = r4.usePretendResponseIfRealModuleDoesntSupport()     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x005f
            RESPONSE r1 = r4.defaultResponse     // Catch:{ all -> 0x0029 }
            if (r1 == 0) goto L_0x005f
            r4.releaseNetworkLock()
            return r1
        L_0x005f:
            throw r0     // Catch:{ all -> 0x0029 }
        L_0x0060:
            r4.releaseNetworkLock()
            throw r0
        L_0x0064:
            java.lang.RuntimeException r0 = new java.lang.RuntimeException
            java.lang.String r1 = "A LynxRespondable can only be sent once"
            r0.<init>(r1)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.commands.LynxRespondable.sendReceive():com.qualcomm.hardware.lynx.commands.LynxMessage");
    }

    /* access modifiers changed from: protected */
    public void throwNackForUnsupportedCommand(LynxUnsupportedCommandException lynxUnsupportedCommandException) throws LynxNackException {
        this.nackReceived = new LynxNack(getModule(), (LynxNack.ReasonCode) LynxNack.StandardReasonCode.PACKET_TYPE_ID_UNKNOWN);
        throw new LynxNackException(this, "%s: command %s(#0x%04x) not supported by mod#=%d", getClass().getSimpleName(), lynxUnsupportedCommandException.getClazz().getSimpleName(), Integer.valueOf(lynxUnsupportedCommandException.getCommandNumber()), Integer.valueOf(getModuleAddress()));
    }

    /* access modifiers changed from: protected */
    public RESPONSE responseOrThrow() throws LynxNackException {
        if (!isNackReceived()) {
            return this.response;
        }
        throw new LynxNackException(this, "%s: nack received: %s:%d", getClass().getSimpleName(), this.nackReceived.getNackReasonCode().toString(), Integer.valueOf(this.nackReceived.getNackReasonCode().getValue()));
    }

    /* access modifiers changed from: protected */
    public void throwIfNack() throws LynxNackException {
        if (isNackReceived()) {
            throw new LynxNackException(this, "%s: nack received: %s:%d", getClass().getSimpleName(), this.nackReceived.getNackReasonCode().toString(), Integer.valueOf(this.nackReceived.getNackReasonCode().getValue()));
        }
    }

    /* access modifiers changed from: protected */
    public void awaitAndRetransmit(CountDownLatch countDownLatch, LynxNack.ReasonCode reasonCode, String str) throws InterruptedException {
        long nanoTime = System.nanoTime() + (((long) getMsAwaitInterval()) * ElapsedTime.MILLIS_IN_NANO);
        int msAwaitInterval = getMsAwaitInterval();
        int msRetransmissionInterval = getMsRetransmissionInterval();
        if (this.module.isNotResponding()) {
            if ((this.module instanceof LynxModule) && this.module.isOpen()) {
                LynxModuleWarningManager.getInstance().reportModuleUnresponsive((LynxModule) this.module);
            }
            onNackReceived(new LynxNack(this.module, reasonCode));
            this.module.finishedWithMessage(this);
            return;
        }
        while (true) {
            long nanoTime2 = nanoTime - System.nanoTime();
            if (nanoTime2 <= 0) {
                onNackReceived(new LynxNack(this.module, reasonCode));
                if ((this.module instanceof LynxModule) && this.module.isOpen()) {
                    RobotLog.m49ee(LynxModule.TAG, "timeout: abandoning waiting %dms for %s: cmd=%s mod=%d msg#=%d", Integer.valueOf(msAwaitInterval), str, getClass().getSimpleName(), Integer.valueOf(getModuleAddress()), Integer.valueOf(getMessageNumber()));
                    RobotLog.m49ee(LynxModule.TAG, "Marking module #%d as unresponsive until we receive some data back", Integer.valueOf(getModuleAddress()));
                    LynxModuleWarningManager.getInstance().reportModuleUnresponsive((LynxModule) this.module);
                }
                this.module.noteNotResponding();
                this.module.finishedWithMessage(this);
                return;
            } else if (!countDownLatch.await((long) Math.min((int) (nanoTime2 / ElapsedTime.MILLIS_IN_NANO), msRetransmissionInterval), TimeUnit.MILLISECONDS)) {
                this.module.retransmit(this);
            } else {
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void awaitAckResponseOrNack() throws InterruptedException {
        if (isResponseExpected()) {
            awaitAndRetransmit(this.responseOrNackReceived, LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_RESPONSE, "response");
        } else {
            awaitAndRetransmit(this.ackOrNackReceived, LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_ACK, "ack");
        }
    }
}
