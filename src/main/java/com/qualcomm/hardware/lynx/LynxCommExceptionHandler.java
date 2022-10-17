package com.qualcomm.hardware.lynx;

import com.qualcomm.robotcore.eventloop.opmode.OpModeManagerImpl;
import com.qualcomm.robotcore.exception.TargetPositionNotSetException;
import com.qualcomm.robotcore.util.RobotLog;

public class LynxCommExceptionHandler {
    protected String tag;

    /* access modifiers changed from: protected */
    public String getTag() {
        return this.tag;
    }

    public LynxCommExceptionHandler() {
        this.tag = RobotLog.TAG;
    }

    public LynxCommExceptionHandler(String str) {
        this.tag = str;
    }

    /* access modifiers changed from: protected */
    public boolean handleException(Exception exc) {
        if (exc instanceof InterruptedException) {
            handleSpecificException((InterruptedException) exc);
            return true;
        } else if (exc instanceof OpModeManagerImpl.ForceStopException) {
            throw ((OpModeManagerImpl.ForceStopException) exc);
        } else if (exc instanceof LynxNackException) {
            LynxNackException lynxNackException = (LynxNackException) exc;
            handleSpecificException(lynxNackException);
            return true ^ lynxNackException.getNack().getNackReasonCode().isUnsupportedReason();
        } else if (exc instanceof TargetPositionNotSetException) {
            handleSpecificException((TargetPositionNotSetException) exc);
            return true;
        } else if (exc instanceof RuntimeException) {
            handleSpecificException((RuntimeException) exc);
            return true;
        } else {
            RobotLog.m50ee(getTag(), (Throwable) exc, "unexpected exception thrown during lynx communication");
            return true;
        }
    }

    /* access modifiers changed from: protected */
    public void handleSpecificException(InterruptedException interruptedException) {
        Thread.currentThread().interrupt();
    }

    /* access modifiers changed from: protected */
    public void handleSpecificException(TargetPositionNotSetException targetPositionNotSetException) {
        throw targetPositionNotSetException;
    }

    /* access modifiers changed from: protected */
    public void handleSpecificException(RuntimeException runtimeException) {
        RobotLog.m50ee(getTag(), (Throwable) runtimeException, "exception thrown during lynx communication");
    }

    /* renamed from: com.qualcomm.hardware.lynx.LynxCommExceptionHandler$1 */
    static /* synthetic */ class C06661 {

        /* renamed from: $SwitchMap$com$qualcomm$hardware$lynx$commands$standard$LynxNack$StandardReasonCode */
        static final /* synthetic */ int[] f83xe0728a3e;

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
                f83xe0728a3e = r0
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_ACK     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.ABANDONED_WAITING_FOR_RESPONSE     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.COMMAND_IMPL_PENDING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.COMMAND_ROUTING_ERROR     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.PACKET_TYPE_ID_UNKNOWN     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.BATTERY_TOO_LOW_TO_RUN_MOTOR     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = f83xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.BATTERY_TOO_LOW_TO_RUN_SERVO     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxCommExceptionHandler.C06661.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void handleSpecificException(LynxNackException lynxNackException) {
        switch (C06661.f83xe0728a3e[lynxNackException.getNack().getNackReasonCodeAsEnum().ordinal()]) {
            case 1:
            case 2:
            case 6:
            case 7:
                return;
            case 3:
                RobotLog.m67ww(getTag(), "%s not implemented by lynx hw; ignoring", lynxNackException.getCommand().getClass().getSimpleName());
                return;
            case 4:
                RobotLog.m49ee(getTag(), "%s not delivered in module mod#=%d cmd#=%d", lynxNackException.getCommand().getClass().getSimpleName(), Integer.valueOf(lynxNackException.getNack().getModuleAddress()), Integer.valueOf(lynxNackException.getNack().getCommandNumber()));
                return;
            case 5:
                RobotLog.m49ee(getTag(), "%s not supported by module mod#=%d cmd#=%d", lynxNackException.getCommand().getClass().getSimpleName(), Integer.valueOf(lynxNackException.getNack().getModuleAddress()), Integer.valueOf(lynxNackException.getNack().getCommandNumber()));
                return;
            default:
                RobotLog.m50ee(getTag(), (Throwable) lynxNackException, "exception thrown during lynx communication");
                return;
        }
    }
}
