package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetBulkInputDataResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelCurrentAlertLevelCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelCurrentAlertLevelResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelEnableResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelModeCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorChannelModeResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorConstantPowerCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorConstantPowerResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorEncoderPositionCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorEncoderPositionResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorPIDControlLoopCoefficientsCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorPIDControlLoopCoefficientsResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorPIDFControlLoopCoefficientsCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorPIDFControlLoopCoefficientsResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorTargetPositionCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorTargetPositionResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorTargetVelocityCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetMotorTargetVelocityResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxIsMotorAtTargetCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxIsMotorAtTargetResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxResetMotorEncoderCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelCurrentAlertLevelCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorChannelModeCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDControlLoopCoefficientsCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorPIDFControlLoopCoefficientsCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorTargetPositionCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetMotorTargetVelocityCommand;
import com.qualcomm.hardware.lynx.commands.standard.LynxNack;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.TargetPositionNotSetException;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DcMotorControllerEx;
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm;
import com.qualcomm.robotcore.hardware.PIDCoefficients;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerParamsState;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.util.LastKnown;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.firstinspires.ftc.robotcore.external.navigation.AngleUnit;
import org.firstinspires.ftc.robotcore.external.navigation.CurrentUnit;
import org.firstinspires.ftc.robotcore.external.navigation.UnnormalizedAngleUnit;
import org.firstinspires.ftc.robotcore.system.Misc;

public class LynxDcMotorController extends LynxController implements DcMotorController, DcMotorControllerEx {
    protected static boolean DEBUG = false;
    public static final String TAG = "LynxMotor";
    public static final int apiMotorFirst = 0;
    public static final int apiMotorLast = 3;
    public static final double apiPowerFirst = -1.0d;
    public static final double apiPowerLast = 1.0d;
    protected final MotorProperties[] motors = new MotorProperties[4];

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    protected class MotorProperties {
        Map<DcMotor.RunMode, ExpansionHubMotorControllerParamsState> desiredPIDParams = new ConcurrentHashMap();
        MotorConfigurationType internalMotorType = null;
        LastKnown<Double> lastKnownCurrentAlert = new LastKnown<>();
        LastKnown<Boolean> lastKnownEnable = new LastKnown<>();
        LastKnown<DcMotor.RunMode> lastKnownMode = new LastKnown<>();
        LastKnown<Double> lastKnownPower = new LastKnown<>();
        LastKnown<Integer> lastKnownTargetPosition = new LastKnown<>();
        LastKnown<DcMotor.ZeroPowerBehavior> lastKnownZeroPowerBehavior = new LastKnown<>();
        MotorConfigurationType motorType = MotorConfigurationType.getUnspecifiedMotorType();
        Map<DcMotor.RunMode, ExpansionHubMotorControllerParamsState> originalPIDParams = new ConcurrentHashMap();

        protected MotorProperties() {
        }
    }

    public LynxDcMotorController(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        int i = 0;
        while (true) {
            MotorProperties[] motorPropertiesArr = this.motors;
            if (i < motorPropertiesArr.length) {
                motorPropertiesArr[i] = new MotorProperties();
                i++;
            } else {
                finishConstruction();
                return;
            }
        }
    }

    public void initializeHardware() throws RobotCoreException, InterruptedException {
        floatHardware();
        runWithoutEncoders();
        forgetLastKnown();
        for (int i = 0; i <= 3; i++) {
            updateMotorParams(i);
        }
        reportPIDFControlLoopCoefficients();
    }

    /* access modifiers changed from: protected */
    public void doHook() {
        forgetLastKnown();
    }

    /* access modifiers changed from: protected */
    public void doUnhook() {
        forgetLastKnown();
    }

    public void forgetLastKnown() {
        for (MotorProperties motorProperties : this.motors) {
            motorProperties.lastKnownMode.invalidate();
            motorProperties.lastKnownPower.invalidate();
            motorProperties.lastKnownTargetPosition.invalidate();
            motorProperties.lastKnownZeroPowerBehavior.invalidate();
            motorProperties.lastKnownEnable.invalidate();
        }
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxDcMotorControllerDisplayName);
    }

    public synchronized void setMotorEnable(int i) {
        validateMotor(i);
        internalSetMotorEnable(i + 0, true);
    }

    public synchronized void setMotorDisable(int i) {
        validateMotor(i);
        internalSetMotorEnable(i + 0, false);
    }

    /* access modifiers changed from: package-private */
    public void internalSetMotorEnable(int i, boolean z) {
        if (this.motors[i].lastKnownEnable.updateValue(Boolean.valueOf(z))) {
            LynxSetMotorChannelEnableCommand lynxSetMotorChannelEnableCommand = new LynxSetMotorChannelEnableCommand(getModule(), i, z);
            try {
                if (DEBUG) {
                    RobotLog.m61vv(TAG, "setMotorEnable mod=%d motor=%d enable=%s", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), Boolean.valueOf(z).toString());
                }
                lynxSetMotorChannelEnableCommand.send();
            } catch (LynxNackException e) {
                if (e.getNack().getNackReasonCode() != LynxNack.StandardReasonCode.MOTOR_NOT_CONFIG_BEFORE_ENABLED) {
                    handleException(e);
                    return;
                }
                throw new TargetPositionNotSetException();
            } catch (InterruptedException | RuntimeException e2) {
                handleException(e2);
            }
        }
    }

    public synchronized boolean isMotorEnabled(int i) {
        validateMotor(i);
        int i2 = i + 0;
        Boolean value = this.motors[i2].lastKnownEnable.getValue();
        if (value != null) {
            return value.booleanValue();
        }
        try {
            Boolean valueOf = Boolean.valueOf(((LynxGetMotorChannelEnableResponse) new LynxGetMotorChannelEnableCommand(getModule(), i2).sendReceive()).isEnabled());
            this.motors[i2].lastKnownEnable.setValue(valueOf);
            return valueOf.booleanValue();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(true)).booleanValue();
        }
    }

    public synchronized void resetDeviceConfigurationForOpMode(int i) {
        validateMotor(i);
        int i2 = i + 0;
        this.motors[i2].desiredPIDParams.remove(DcMotor.RunMode.RUN_TO_POSITION);
        this.motors[i2].desiredPIDParams.remove(DcMotor.RunMode.RUN_USING_ENCODER);
        if (this.motors[i2].originalPIDParams.containsKey(DcMotor.RunMode.RUN_TO_POSITION)) {
            this.motors[i2].desiredPIDParams.put(DcMotor.RunMode.RUN_TO_POSITION, this.motors[i2].originalPIDParams.get(DcMotor.RunMode.RUN_TO_POSITION));
        }
        if (this.motors[i2].originalPIDParams.containsKey(DcMotor.RunMode.RUN_USING_ENCODER)) {
            this.motors[i2].desiredPIDParams.put(DcMotor.RunMode.RUN_USING_ENCODER, this.motors[i2].originalPIDParams.get(DcMotor.RunMode.RUN_USING_ENCODER));
        }
        if (this.motors[i2].internalMotorType != null) {
            setMotorType(i2 + 0, this.motors[i2].internalMotorType);
        } else {
            updateMotorParams(i2);
        }
    }

    public synchronized MotorConfigurationType getMotorType(int i) {
        validateMotor(i);
        return this.motors[i + 0].motorType;
    }

    public synchronized void setMotorType(int i, MotorConfigurationType motorConfigurationType) {
        validateMotor(i);
        int i2 = i + 0;
        this.motors[i2].motorType = motorConfigurationType;
        if (this.motors[i2].internalMotorType == null) {
            this.motors[i2].internalMotorType = motorConfigurationType;
        }
        if (motorConfigurationType.hasExpansionHubVelocityParams()) {
            rememberPIDParams(i2, motorConfigurationType.getHubVelocityParams());
        }
        if (motorConfigurationType.hasExpansionHubPositionParams()) {
            rememberPIDParams(i2, motorConfigurationType.getHubPositionParams());
        }
        updateMotorParams(i2);
    }

    /* access modifiers changed from: protected */
    public void rememberPIDParams(int i, ExpansionHubMotorControllerParamsState expansionHubMotorControllerParamsState) {
        this.motors[i].desiredPIDParams.put(expansionHubMotorControllerParamsState.mode, expansionHubMotorControllerParamsState);
    }

    /* access modifiers changed from: protected */
    public void updateMotorParams(int i) {
        for (ExpansionHubMotorControllerParamsState next : this.motors[i].desiredPIDParams.values()) {
            if (!next.isDefault()) {
                internalSetPIDFCoefficients(i, next.mode, next.getPidfCoefficients());
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getDefaultMaxMotorSpeed(int i) {
        return this.motors[i].motorType.getAchieveableMaxTicksPerSecondRounded();
    }

    public synchronized void setMotorMode(int i, DcMotor.RunMode runMode) {
        LynxCommand lynxCommand;
        validateMotor(i);
        int i2 = i + 0;
        if (!this.motors[i2].lastKnownMode.isValue(runMode)) {
            Double nonTimedValue = this.motors[i2].lastKnownPower.getNonTimedValue();
            if (nonTimedValue == null) {
                nonTimedValue = Double.valueOf(internalGetMotorPower(i2));
            }
            DcMotor.ZeroPowerBehavior zeroPowerBehavior = DcMotor.ZeroPowerBehavior.UNKNOWN;
            if (runMode == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
                internalSetMotorPower(i2, LynxServoController.apiPositionFirst);
                lynxCommand = new LynxResetMotorEncoderCommand(getModule(), i2);
            } else {
                zeroPowerBehavior = internalGetZeroPowerBehavior(i2);
                lynxCommand = new LynxSetMotorChannelModeCommand(getModule(), i2, runMode, zeroPowerBehavior);
            }
            try {
                if (DEBUG) {
                    RobotLog.m61vv(TAG, "setMotorChannelMode: mod=%d motor=%d mode=%s power=%f zero=%s", Integer.valueOf(getModuleAddress()), Integer.valueOf(i2), runMode.toString(), nonTimedValue, zeroPowerBehavior.toString());
                }
                lynxCommand.send();
                this.motors[i2].lastKnownMode.setValue(runMode);
                internalSetMotorPower(i2, nonTimedValue.doubleValue(), true);
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
        return;
    }

    public synchronized DcMotor.RunMode getMotorMode(int i) {
        validateMotor(i);
        return internalGetPublicMotorMode(i + 0);
    }

    /* access modifiers changed from: protected */
    public DcMotor.RunMode internalGetPublicMotorMode(int i) {
        DcMotor.RunMode value = this.motors[i].lastKnownMode.getValue();
        if (value != null) {
            return value;
        }
        if (this.motors[i].lastKnownMode.getNonTimedValue() == DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            return DcMotor.RunMode.STOP_AND_RESET_ENCODER;
        }
        try {
            DcMotor.RunMode mode = ((LynxGetMotorChannelModeResponse) new LynxGetMotorChannelModeCommand(getModule(), i).sendReceive()).getMode();
            this.motors[i].lastKnownMode.setValue(mode);
            return mode;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return (DcMotor.RunMode) LynxUsbUtil.makePlaceholderValue(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    /* access modifiers changed from: protected */
    public DcMotor.RunMode internalGetMotorChannelMode(int i) {
        DcMotor.RunMode value = this.motors[i].lastKnownMode.getValue();
        if (value != null && value != DcMotor.RunMode.STOP_AND_RESET_ENCODER) {
            return value;
        }
        try {
            return ((LynxGetMotorChannelModeResponse) new LynxGetMotorChannelModeCommand(getModule(), i).sendReceive()).getMode();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return (DcMotor.RunMode) LynxUsbUtil.makePlaceholderValue(DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    public synchronized void setMotorPower(int i, double d) {
        validateMotor(i);
        internalSetMotorPower(i + 0, d);
    }

    public synchronized double getMotorPower(int i) {
        validateMotor(i);
        return internalGetMotorPower(i + 0);
    }

    /* access modifiers changed from: package-private */
    public DcMotor.ZeroPowerBehavior internalGetZeroPowerBehavior(int i) {
        DcMotor.ZeroPowerBehavior value = this.motors[i].lastKnownZeroPowerBehavior.getValue();
        if (value != null) {
            return value;
        }
        try {
            DcMotor.ZeroPowerBehavior zeroPowerBehavior = ((LynxGetMotorChannelModeResponse) new LynxGetMotorChannelModeCommand(getModule(), i).sendReceive()).getZeroPowerBehavior();
            this.motors[i].lastKnownZeroPowerBehavior.setValue(zeroPowerBehavior);
            return zeroPowerBehavior;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return (DcMotor.ZeroPowerBehavior) LynxUsbUtil.makePlaceholderValue(DcMotor.ZeroPowerBehavior.BRAKE);
        }
    }

    /* access modifiers changed from: package-private */
    public void internalSetZeroPowerBehavior(int i, DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        if (this.motors[i].lastKnownZeroPowerBehavior.updateValue(zeroPowerBehavior)) {
            LynxSetMotorChannelModeCommand lynxSetMotorChannelModeCommand = new LynxSetMotorChannelModeCommand(getModule(), i, internalGetMotorChannelMode(i), zeroPowerBehavior);
            try {
                if (DEBUG) {
                    RobotLog.m61vv(TAG, "setZeroBehavior mod=%d motor=%d zero=%s", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), zeroPowerBehavior.toString());
                }
                lynxSetMotorChannelModeCommand.send();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    /* access modifiers changed from: package-private */
    public void internalSetMotorPower(int i, double d) {
        internalSetMotorPower(i, d, false);
    }

    /* access modifiers changed from: package-private */
    /* JADX WARNING: Removed duplicated region for block: B:12:0x0081 A[SYNTHETIC, Splitter:B:12:0x0081] */
    /* JADX WARNING: Removed duplicated region for block: B:21:? A[RETURN, SYNTHETIC] */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void internalSetMotorPower(int r24, double r25, boolean r27) {
        /*
            r23 = this;
            r1 = r23
            r0 = r24
            r4 = -4616189618054758400(0xbff0000000000000, double:-1.0)
            r6 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            r2 = r25
            double r8 = com.qualcomm.robotcore.util.Range.clip((double) r2, (double) r4, (double) r6)
            com.qualcomm.hardware.lynx.LynxDcMotorController$MotorProperties[] r2 = r1.motors
            r2 = r2[r0]
            com.qualcomm.robotcore.util.LastKnown<java.lang.Double> r2 = r2.lastKnownPower
            java.lang.Double r3 = java.lang.Double.valueOf(r8)
            boolean r2 = r2.updateValue(r3)
            if (r2 != 0) goto L_0x0020
            if (r27 == 0) goto L_0x00b3
        L_0x0020:
            com.qualcomm.robotcore.hardware.DcMotor$RunMode r2 = r23.internalGetPublicMotorMode(r24)
            int[] r3 = com.qualcomm.hardware.lynx.LynxDcMotorController.C06691.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode
            int r2 = r2.ordinal()
            r2 = r3[r2]
            r3 = 3
            r4 = 2
            r5 = 0
            r6 = 1
            if (r2 == r6) goto L_0x0056
            if (r2 == r4) goto L_0x0056
            if (r2 == r3) goto L_0x0039
            r2 = 0
            r7 = r5
            goto L_0x007f
        L_0x0039:
            r10 = -4616189618054758400(0xbff0000000000000, double:-1.0)
            r12 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            r14 = -4548635898522107904(0xc0dfffc000000000, double:-32767.0)
            r16 = 4674736138332667904(0x40dfffc000000000, double:32767.0)
            double r7 = com.qualcomm.robotcore.util.Range.scale(r8, r10, r12, r14, r16)
            int r2 = (int) r7
            com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand r7 = new com.qualcomm.hardware.lynx.commands.core.LynxSetMotorConstantPowerCommand
            com.qualcomm.hardware.lynx.LynxModuleIntf r8 = r23.getModule()
            r7.<init>(r8, r0, r2)
            goto L_0x007a
        L_0x0056:
            double r10 = java.lang.Math.signum(r8)
            double r12 = java.lang.Math.abs(r8)
            r14 = 0
            r16 = 4607182418800017408(0x3ff0000000000000, double:1.0)
            r18 = 0
            int r2 = r23.getDefaultMaxMotorSpeed(r24)
            double r7 = (double) r2
            r20 = r7
            double r7 = com.qualcomm.robotcore.util.Range.scale(r12, r14, r16, r18, r20)
            double r10 = r10 * r7
            int r2 = (int) r10
            com.qualcomm.hardware.lynx.commands.core.LynxSetMotorTargetVelocityCommand r7 = new com.qualcomm.hardware.lynx.commands.core.LynxSetMotorTargetVelocityCommand
            com.qualcomm.hardware.lynx.LynxModuleIntf r8 = r23.getModule()
            r7.<init>(r8, r0, r2)
        L_0x007a:
            r22 = r7
            r7 = r2
            r2 = r22
        L_0x007f:
            if (r2 == 0) goto L_0x00b3
            boolean r8 = DEBUG     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            if (r8 == 0) goto L_0x00a4
            java.lang.String r8 = "LynxMotor"
            java.lang.String r9 = "setMotorPower: mod=%d motor=%d iPower=%d"
            java.lang.Object[] r3 = new java.lang.Object[r3]     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            int r10 = r23.getModuleAddress()     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            java.lang.Integer r10 = java.lang.Integer.valueOf(r10)     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            r3[r5] = r10     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r24)     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            r3[r6] = r5     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            java.lang.Integer r5 = java.lang.Integer.valueOf(r7)     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            r3[r4] = r5     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r8, (java.lang.String) r9, (java.lang.Object[]) r3)     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
        L_0x00a4:
            r2.send()     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            r1.internalSetMotorEnable(r0, r6)     // Catch:{ InterruptedException -> 0x00af, RuntimeException -> 0x00ad, LynxNackException -> 0x00ab }
            goto L_0x00b3
        L_0x00ab:
            r0 = move-exception
            goto L_0x00b0
        L_0x00ad:
            r0 = move-exception
            goto L_0x00b0
        L_0x00af:
            r0 = move-exception
        L_0x00b0:
            r1.handleException(r0)
        L_0x00b3:
            return
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxDcMotorController.internalSetMotorPower(int, double, boolean):void");
    }

    /* renamed from: com.qualcomm.hardware.lynx.LynxDcMotorController$1 */
    static /* synthetic */ class C06691 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode;

        /* JADX WARNING: Can't wrap try/catch for region: R(8:0|1|2|3|4|5|6|(3:7|8|10)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        static {
            /*
                com.qualcomm.robotcore.hardware.DcMotor$RunMode[] r0 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode = r0
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_TO_POSITION     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_USING_ENCODER     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.RUN_WITHOUT_ENCODER     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.hardware.DcMotor$RunMode r1 = com.qualcomm.robotcore.hardware.DcMotor.RunMode.STOP_AND_RESET_ENCODER     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxDcMotorController.C06691.<clinit>():void");
        }
    }

    /* access modifiers changed from: package-private */
    public double internalGetMotorPower(int i) {
        Double d;
        int i2 = i;
        Double value = this.motors[i2].lastKnownPower.getValue();
        if (value != null) {
            if (DEBUG) {
                RobotLog.m61vv(TAG, "getMotorPower(cached): mod=%d motor=%d power=%f", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), value);
            }
            return value.doubleValue();
        }
        try {
            int i3 = C06691.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[internalGetPublicMotorMode(i).ordinal()];
            if (i3 == 1 || i3 == 2) {
                int velocity = ((LynxGetMotorTargetVelocityResponse) new LynxGetMotorTargetVelocityCommand(getModule(), i2).sendReceive()).getVelocity();
                double abs = (double) Math.abs(velocity);
                int defaultMaxMotorSpeed = getDefaultMaxMotorSpeed(i);
                String str = TAG;
                d = Double.valueOf(((double) Math.signum((float) velocity)) * Range.scale(abs, LynxServoController.apiPositionFirst, (double) defaultMaxMotorSpeed, LynxServoController.apiPositionFirst, 1.0d));
                if (DEBUG) {
                    RobotLog.m61vv(str, "getMotorPower: mod=%d motor=%d velocity=%d power=%f", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), Integer.valueOf(velocity), d);
                }
            } else {
                int power = ((LynxGetMotorConstantPowerResponse) new LynxGetMotorConstantPowerCommand(getModule(), i2).sendReceive()).getPower();
                d = Double.valueOf(Range.scale((double) power, -32767.0d, 32767.0d, -1.0d, 1.0d));
                if (DEBUG) {
                    RobotLog.m61vv(TAG, "getMotorPower: mod=%d motor=%d iPower=%d power=%f", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), Integer.valueOf(power), d);
                }
            }
            Double valueOf = Double.valueOf(Range.clip(d.doubleValue(), -1.0d, 1.0d));
            this.motors[i2].lastKnownPower.setValue(valueOf);
            return valueOf.doubleValue();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return (double) ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
    }

    public synchronized boolean isBusy(int i) {
        validateMotor(i);
        int i2 = i + 0;
        LynxIsMotorAtTargetCommand lynxIsMotorAtTargetCommand = new LynxIsMotorAtTargetCommand(getModule(), i2);
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxIsMotorAtTargetCommand).isMotorBusy(i2);
            }
        }
        if (internalGetMotorChannelMode(i2) != DcMotor.RunMode.RUN_TO_POSITION) {
            return false;
        }
        try {
            return !((LynxIsMotorAtTargetResponse) lynxIsMotorAtTargetCommand.sendReceive()).isAtTarget();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(false)).booleanValue();
        }
    }

    public synchronized void setMotorZeroPowerBehavior(int i, DcMotor.ZeroPowerBehavior zeroPowerBehavior) {
        validateMotor(i);
        int i2 = i + 0;
        if (zeroPowerBehavior != DcMotor.ZeroPowerBehavior.UNKNOWN) {
            internalSetZeroPowerBehavior(i2, zeroPowerBehavior);
        } else {
            throw new IllegalArgumentException("zeroPowerBehavior may not be UNKNOWN");
        }
    }

    public synchronized DcMotor.ZeroPowerBehavior getMotorZeroPowerBehavior(int i) {
        validateMotor(i);
        return internalGetZeroPowerBehavior(i + 0);
    }

    /* access modifiers changed from: protected */
    public synchronized void setMotorPowerFloat(int i) {
        validateMotor(i);
        int i2 = i + 0;
        internalSetZeroPowerBehavior(i2, DcMotor.ZeroPowerBehavior.FLOAT);
        internalSetMotorPower(i2, LynxServoController.apiPositionFirst);
    }

    public synchronized boolean getMotorPowerFloat(int i) {
        boolean z;
        validateMotor(i);
        z = false;
        int i2 = i + 0;
        if (internalGetZeroPowerBehavior(i2) == DcMotor.ZeroPowerBehavior.FLOAT && internalGetMotorPower(i2) == LynxServoController.apiPositionFirst) {
            z = true;
        }
        return z;
    }

    public synchronized void setMotorTargetPosition(int i, int i2) {
        setMotorTargetPosition(i, i2, 5);
    }

    public synchronized void setMotorTargetPosition(int i, int i2, int i3) {
        validateMotor(i);
        try {
            new LynxSetMotorTargetPositionCommand(getModule(), i + 0, i2, i3).send();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
        return;
    }

    public synchronized int getMotorTargetPosition(int i) {
        validateMotor(i);
        try {
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
        return ((LynxGetMotorTargetPositionResponse) new LynxGetMotorTargetPositionCommand(getModule(), i + 0).sendReceive()).getTarget();
    }

    public synchronized int getMotorCurrentPosition(int i) {
        validateMotor(i);
        int i2 = i + 0;
        LynxGetMotorEncoderPositionCommand lynxGetMotorEncoderPositionCommand = new LynxGetMotorEncoderPositionCommand(getModule(), i2);
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxGetMotorEncoderPositionCommand).getMotorCurrentPosition(i2);
            }
        }
        try {
            return ((LynxGetMotorEncoderPositionResponse) lynxGetMotorEncoderPositionCommand.sendReceive()).getPosition();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
    }

    public synchronized void setMotorVelocity(int i, double d) {
        int i2 = C06691.$SwitchMap$com$qualcomm$robotcore$hardware$DcMotor$RunMode[getMotorMode(i).ordinal()];
        if (!(i2 == 1 || i2 == 2)) {
            setMotorMode(i, DcMotor.RunMode.RUN_USING_ENCODER);
        }
        validateMotor(i);
        int i3 = i + 0;
        int clip = Range.clip((int) Math.round(d), -32767, 32767);
        try {
            LynxSetMotorTargetVelocityCommand lynxSetMotorTargetVelocityCommand = new LynxSetMotorTargetVelocityCommand(getModule(), i3, clip);
            if (DEBUG) {
                RobotLog.m61vv(TAG, "setMotorVelocity: mod=%d motor=%d iPower=%d", Integer.valueOf(getModuleAddress()), Integer.valueOf(i3), Integer.valueOf(clip));
            }
            lynxSetMotorTargetVelocityCommand.send();
            internalSetMotorEnable(i3, true);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
        return;
    }

    public synchronized void setMotorVelocity(int i, double d, AngleUnit angleUnit) {
        validateMotor(i);
        int i2 = i + 0;
        double ticksPerRev = this.motors[i2].motorType.getTicksPerRev();
        setMotorVelocity(i2 + 0, ticksPerRev * (UnnormalizedAngleUnit.DEGREES.fromUnit(angleUnit.getUnnormalized(), d) / 360.0d));
    }

    public synchronized double getMotorVelocity(int i) {
        validateMotor(i);
        return (double) internalGetMotorTicksPerSecond(i + 0);
    }

    public synchronized double getMotorVelocity(int i, AngleUnit angleUnit) {
        int i2;
        validateMotor(i);
        i2 = i + 0;
        return angleUnit.getUnnormalized().fromDegrees((((double) internalGetMotorTicksPerSecond(i2)) / this.motors[i2].motorType.getTicksPerRev()) * 360.0d);
    }

    /* access modifiers changed from: package-private */
    public int internalGetMotorTicksPerSecond(int i) {
        LynxGetBulkInputDataCommand lynxGetBulkInputDataCommand = new LynxGetBulkInputDataCommand(getModule());
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxGetBulkInputDataCommand, "motorVelocity" + i).getMotorVelocity(i);
            }
        }
        try {
            return ((LynxGetBulkInputDataResponse) lynxGetBulkInputDataCommand.sendReceive()).getVelocity(i);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
    }

    public void setPIDCoefficients(int i, DcMotor.RunMode runMode, PIDCoefficients pIDCoefficients) {
        setPIDFCoefficients(i, runMode, new PIDFCoefficients(pIDCoefficients));
    }

    public synchronized void setPIDFCoefficients(int i, DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) {
        validatePIDMode(i, runMode);
        validateMotor(i);
        int i2 = i + 0;
        DcMotor.RunMode migrate = runMode.migrate();
        rememberPIDParams(i2, new ExpansionHubMotorControllerParamsState(migrate, pIDFCoefficients));
        if (!internalSetPIDFCoefficients(i2, migrate, pIDFCoefficients)) {
            throw new UnsupportedOperationException(Misc.formatForUser("setting of pidf coefficents not supported: motor=%d mode=%s pidf=%s", Integer.valueOf(i2 + 0), migrate, pIDFCoefficients));
        }
    }

    /* access modifiers changed from: protected */
    public boolean internalSetPIDFCoefficients(int i, DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) {
        boolean z;
        boolean handleException;
        DcMotor.RunMode runMode2 = runMode;
        PIDFCoefficients pIDFCoefficients2 = pIDFCoefficients;
        if (!this.motors[i].originalPIDParams.containsKey(runMode2)) {
            this.motors[i].originalPIDParams.put(runMode2, new ExpansionHubMotorControllerParamsState(runMode2, getPIDFCoefficients(i + 0, runMode2)));
        }
        int internalCoefficientFromExternal = LynxSetMotorPIDControlLoopCoefficientsCommand.internalCoefficientFromExternal(pIDFCoefficients2.f124p);
        int internalCoefficientFromExternal2 = LynxSetMotorPIDControlLoopCoefficientsCommand.internalCoefficientFromExternal(pIDFCoefficients2.f123i);
        int internalCoefficientFromExternal3 = LynxSetMotorPIDControlLoopCoefficientsCommand.internalCoefficientFromExternal(pIDFCoefficients2.f121d);
        int internalCoefficientFromExternal4 = LynxSetMotorPIDControlLoopCoefficientsCommand.internalCoefficientFromExternal(pIDFCoefficients2.f122f);
        if (runMode2 != DcMotor.RunMode.RUN_TO_POSITION || pIDFCoefficients2.algorithm == MotorControlAlgorithm.LegacyPID || (pIDFCoefficients2.f123i == LynxServoController.apiPositionFirst && pIDFCoefficients2.f121d == LynxServoController.apiPositionFirst && pIDFCoefficients2.f122f == LynxServoController.apiPositionFirst)) {
            z = true;
        } else {
            RobotLog.m67ww(TAG, "using unreasonable coefficients for RUN_TO_POSITION: setPIDFCoefficients(%d, %s, %s)", Integer.valueOf(i + 0), runMode2, pIDFCoefficients2);
            z = false;
        }
        if (z) {
            if (getModule().isCommandSupported(LynxSetMotorPIDFControlLoopCoefficientsCommand.class)) {
                try {
                    new LynxSetMotorPIDFControlLoopCoefficientsCommand(getModule(), i, runMode, internalCoefficientFromExternal, internalCoefficientFromExternal2, internalCoefficientFromExternal3, internalCoefficientFromExternal4, LynxSetMotorPIDFControlLoopCoefficientsCommand.InternalMotorControlAlgorithm.fromExternal(pIDFCoefficients2.algorithm)).send();
                } catch (LynxNackException | InterruptedException | RuntimeException e) {
                    handleException = handleException(e);
                }
            } else if (internalCoefficientFromExternal4 == 0 && pIDFCoefficients2.algorithm == MotorControlAlgorithm.LegacyPID) {
                try {
                    new LynxSetMotorPIDControlLoopCoefficientsCommand(getModule(), i, runMode, internalCoefficientFromExternal, internalCoefficientFromExternal2, internalCoefficientFromExternal3).send();
                } catch (LynxNackException | InterruptedException | RuntimeException e2) {
                    handleException = handleException(e2);
                }
            } else {
                RobotLog.m67ww(TAG, "not supported: setPIDFCoefficients(%d, %s, %s)", Integer.valueOf(i + 0), runMode2, pIDFCoefficients2);
                return false;
            }
        }
        return z;
        return handleException;
    }

    public synchronized PIDCoefficients getPIDCoefficients(int i, DcMotor.RunMode runMode) {
        validateMotor(i);
        int i2 = i + 0;
        try {
            LynxGetMotorPIDControlLoopCoefficientsResponse lynxGetMotorPIDControlLoopCoefficientsResponse = (LynxGetMotorPIDControlLoopCoefficientsResponse) new LynxGetMotorPIDControlLoopCoefficientsCommand(getModule(), i2, runMode).sendReceive();
            return new PIDCoefficients(LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDControlLoopCoefficientsResponse.getP()), LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDControlLoopCoefficientsResponse.getI()), LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDControlLoopCoefficientsResponse.getD()));
        } catch (LynxNackException e) {
            if (e.getNack().getNackReasonCode() == LynxNack.StandardReasonCode.PARAM2) {
                PIDFCoefficients pIDFCoefficients = getPIDFCoefficients(i2 + 0, runMode);
                return new PIDCoefficients(pIDFCoefficients.f124p, pIDFCoefficients.f123i, pIDFCoefficients.f121d);
            }
            handleException(e);
            return (PIDCoefficients) LynxUsbUtil.makePlaceholderValue(new PIDCoefficients());
        } catch (InterruptedException | RuntimeException e2) {
            handleException(e2);
            return (PIDCoefficients) LynxUsbUtil.makePlaceholderValue(new PIDCoefficients());
        }
    }

    public synchronized PIDFCoefficients getPIDFCoefficients(int i, DcMotor.RunMode runMode) {
        if (getModule().isCommandSupported(LynxGetMotorPIDFControlLoopCoefficientsCommand.class)) {
            validateMotor(i);
            try {
                LynxGetMotorPIDFControlLoopCoefficientsResponse lynxGetMotorPIDFControlLoopCoefficientsResponse = (LynxGetMotorPIDFControlLoopCoefficientsResponse) new LynxGetMotorPIDFControlLoopCoefficientsCommand(getModule(), i + 0, runMode).sendReceive();
                return new PIDFCoefficients(LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDFControlLoopCoefficientsResponse.getP()), LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDFControlLoopCoefficientsResponse.getI()), LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDFControlLoopCoefficientsResponse.getD()), LynxSetMotorPIDControlLoopCoefficientsCommand.externalCoefficientFromInternal(lynxGetMotorPIDFControlLoopCoefficientsResponse.getF()), lynxGetMotorPIDFControlLoopCoefficientsResponse.getInternalMotorControlAlgorithm().toExternal());
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
                return (PIDFCoefficients) LynxUsbUtil.makePlaceholderValue(new PIDFCoefficients());
            }
        } else {
            return new PIDFCoefficients(getPIDCoefficients(i, runMode));
        }
    }

    public double getMotorCurrent(int i, CurrentUnit currentUnit) {
        try {
            return currentUnit.convert((double) ((LynxGetADCResponse) new LynxGetADCCommand(getModule(), LynxGetADCCommand.Channel.motorCurrent(i), LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue(), CurrentUnit.MILLIAMPS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public double getMotorCurrentAlert(int i, CurrentUnit currentUnit) {
        Double value = this.motors[i].lastKnownCurrentAlert.getValue();
        if (value != null) {
            return currentUnit.convert(value.doubleValue(), CurrentUnit.MILLIAMPS);
        }
        try {
            double currentLimit = (double) ((LynxGetMotorChannelCurrentAlertLevelResponse) new LynxGetMotorChannelCurrentAlertLevelCommand(getModule(), i).sendReceive()).getCurrentLimit();
            this.motors[i].lastKnownCurrentAlert.setValue(Double.valueOf(currentLimit));
            return currentUnit.convert(currentLimit, CurrentUnit.MILLIAMPS);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    public void setMotorCurrentAlert(int i, double d, CurrentUnit currentUnit) {
        try {
            new LynxSetMotorChannelCurrentAlertLevelCommand(getModule(), i, (int) Math.round(currentUnit.toMilliAmps(d))).send();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }

    public boolean isMotorOverCurrent(int i) {
        LynxGetBulkInputDataCommand lynxGetBulkInputDataCommand = new LynxGetBulkInputDataCommand(getModule());
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxGetBulkInputDataCommand, "motorOverCurrent" + i).isMotorOverCurrent(i);
            }
        }
        try {
            return ((LynxGetBulkInputDataResponse) lynxGetBulkInputDataCommand.sendReceive()).isOverCurrent(i);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(false)).booleanValue();
        }
    }

    public void floatHardware() {
        for (int i = 0; i <= 3; i++) {
            setMotorPowerFloat(i);
        }
    }

    private void runWithoutEncoders() {
        for (int i = 0; i <= 3; i++) {
            setMotorMode(i, DcMotor.RunMode.RUN_WITHOUT_ENCODER);
        }
    }

    private void validateMotor(int i) {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException(String.format("motor %d is invalid; valid motors are %d..%d", new Object[]{Integer.valueOf(i), 0, 3}));
        }
    }

    private void validatePIDMode(int i, DcMotor.RunMode runMode) {
        if (!runMode.isPIDMode()) {
            throw new IllegalArgumentException(String.format("motor %d: mode %s is invalid as PID Mode", new Object[]{Integer.valueOf(i), runMode}));
        }
    }

    private void reportPIDFControlLoopCoefficients() throws RobotCoreException, InterruptedException {
        reportPIDFControlLoopCoefficients(DcMotor.RunMode.RUN_TO_POSITION);
        reportPIDFControlLoopCoefficients(DcMotor.RunMode.RUN_USING_ENCODER);
    }

    private void reportPIDFControlLoopCoefficients(DcMotor.RunMode runMode) throws RobotCoreException, InterruptedException {
        if (DEBUG) {
            for (int i = 0; i <= 3; i++) {
                PIDFCoefficients pIDFCoefficients = getPIDFCoefficients(i, runMode);
                RobotLog.m61vv(TAG, "mod=%d motor=%d mode=%s p=%f i=%f d=%f f=%f alg=%s", Integer.valueOf(getModuleAddress()), Integer.valueOf(i), runMode.toString(), Double.valueOf(pIDFCoefficients.f124p), Double.valueOf(pIDFCoefficients.f123i), Double.valueOf(pIDFCoefficients.f121d), Double.valueOf(pIDFCoefficients.f122f), pIDFCoefficients.algorithm);
            }
        }
    }

    /* access modifiers changed from: protected */
    public int getModuleAddress() {
        return getModule().getModuleAddress();
    }
}
