package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.commands.core.LynxGetServoEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetServoEnableResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetServoPulseWidthCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetServoPulseWidthResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoConfigurationCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetServoPulseWidthCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.PwmControl;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.util.LastKnown;
import com.qualcomm.robotcore.util.Range;

public class LynxServoController extends LynxController implements ServoController, ServoControllerEx {
    public static final String TAG = "LynxServoController";
    public static final double apiPositionFirst = 0.0d;
    public static final double apiPositionLast = 1.0d;
    public static final int apiServoFirst = 0;
    public static final int apiServoLast = 5;
    protected PwmControl.PwmRange[] defaultPwmRanges = new PwmControl.PwmRange[6];
    protected final LastKnown<Double>[] lastKnownCommandedPosition = LastKnown.createArray(6);
    protected final LastKnown<Boolean>[] lastKnownEnabled = LastKnown.createArray(6);
    protected PwmControl.PwmRange[] pwmRanges = new PwmControl.PwmRange[6];

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public LynxServoController(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        int i = 0;
        while (true) {
            PwmControl.PwmRange[] pwmRangeArr = this.pwmRanges;
            if (i < pwmRangeArr.length) {
                pwmRangeArr[i] = PwmControl.PwmRange.defaultRange;
                this.defaultPwmRanges[i] = PwmControl.PwmRange.defaultRange;
                i++;
            } else {
                finishConstruction();
                return;
            }
        }
    }

    public void initializeHardware() {
        for (int i = 0; i <= 5; i++) {
            int i2 = i + 0;
            this.pwmRanges[i2] = null;
            setServoPwmRange(i, this.defaultPwmRanges[i2]);
        }
        floatHardware();
        forgetLastKnown();
    }

    public void floatHardware() {
        pwmDisable();
    }

    public void forgetLastKnown() {
        LastKnown.invalidateArray(this.lastKnownCommandedPosition);
        LastKnown.invalidateArray(this.lastKnownEnabled);
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxServoControllerDisplayName);
    }

    public synchronized void pwmEnable() {
        for (int i = 0; i < 6; i++) {
            internalSetPwmEnable(i, true);
        }
    }

    public synchronized void pwmDisable() {
        for (int i = 0; i < 6; i++) {
            internalSetPwmEnable(i, false);
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:20:0x002a, code lost:
        return r0.booleanValue() ? com.qualcomm.robotcore.hardware.ServoController.PwmStatus.ENABLED : com.qualcomm.robotcore.hardware.ServoController.PwmStatus.DISABLED;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized com.qualcomm.robotcore.hardware.ServoController.PwmStatus getPwmStatus() {
        /*
            r4 = this;
            monitor-enter(r4)
            r0 = 0
            r1 = 0
        L_0x0003:
            r2 = 6
            if (r1 >= r2) goto L_0x001e
            boolean r2 = r4.internalGetPwmEnable(r1)     // Catch:{ all -> 0x002b }
            if (r0 != 0) goto L_0x0011
            java.lang.Boolean r0 = java.lang.Boolean.valueOf(r2)     // Catch:{ all -> 0x002b }
            goto L_0x001b
        L_0x0011:
            boolean r3 = r0.booleanValue()     // Catch:{ all -> 0x002b }
            if (r3 == r2) goto L_0x001b
            com.qualcomm.robotcore.hardware.ServoController$PwmStatus r0 = com.qualcomm.robotcore.hardware.ServoController.PwmStatus.MIXED     // Catch:{ all -> 0x002b }
            monitor-exit(r4)
            return r0
        L_0x001b:
            int r1 = r1 + 1
            goto L_0x0003
        L_0x001e:
            boolean r0 = r0.booleanValue()     // Catch:{ all -> 0x002b }
            if (r0 == 0) goto L_0x0027
            com.qualcomm.robotcore.hardware.ServoController$PwmStatus r0 = com.qualcomm.robotcore.hardware.ServoController.PwmStatus.ENABLED     // Catch:{ all -> 0x002b }
            goto L_0x0029
        L_0x0027:
            com.qualcomm.robotcore.hardware.ServoController$PwmStatus r0 = com.qualcomm.robotcore.hardware.ServoController.PwmStatus.DISABLED     // Catch:{ all -> 0x002b }
        L_0x0029:
            monitor-exit(r4)
            return r0
        L_0x002b:
            r0 = move-exception
            monitor-exit(r4)
            throw r0
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxServoController.getPwmStatus():com.qualcomm.robotcore.hardware.ServoController$PwmStatus");
    }

    public synchronized void setServoPwmEnable(int i) {
        validateServo(i);
        internalSetPwmEnable(i + 0, true);
    }

    public synchronized void setServoPwmDisable(int i) {
        validateServo(i);
        internalSetPwmEnable(i + 0, false);
    }

    public synchronized boolean isServoPwmEnabled(int i) {
        validateServo(i);
        return internalGetPwmEnable(i + 0);
    }

    public void setServoType(int i, ServoConfigurationType servoConfigurationType) {
        validateServo(i);
        int i2 = i + 0;
        PwmControl.PwmRange pwmRange = new PwmControl.PwmRange(servoConfigurationType.getUsPulseLower(), servoConfigurationType.getUsPulseUpper(), servoConfigurationType.getUsFrame());
        this.defaultPwmRanges[i2] = pwmRange;
        setServoPwmRange(i2, pwmRange);
    }

    private void internalSetPwmEnable(int i, boolean z) {
        if (this.lastKnownEnabled[i].updateValue(Boolean.valueOf(z))) {
            if (!z) {
                this.lastKnownCommandedPosition[i].invalidate();
            }
            try {
                new LynxSetServoEnableCommand(getModule(), i, z).send();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    private boolean internalGetPwmEnable(int i) {
        Boolean value = this.lastKnownEnabled[i].getValue();
        if (value != null) {
            return value.booleanValue();
        }
        try {
            Boolean valueOf = Boolean.valueOf(((LynxGetServoEnableResponse) new LynxGetServoEnableCommand(getModule(), i).sendReceive()).isEnabled());
            this.lastKnownEnabled[i].setValue(valueOf);
            return valueOf.booleanValue();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(true)).booleanValue();
        }
    }

    public synchronized void setServoPosition(int i, double d) {
        synchronized (this) {
            validateServo(i);
            int i2 = i + 0;
            validateApiServoPosition(d);
            if (this.lastKnownCommandedPosition[i2].updateValue(Double.valueOf(d))) {
                try {
                    new LynxSetServoPulseWidthCommand(getModule(), i2, (int) Range.clip(Range.scale(d, apiPositionFirst, 1.0d, this.pwmRanges[i2].usPulseLower, this.pwmRanges[i2].usPulseUpper), 1.0d, 65535.0d)).send();
                } catch (LynxNackException | InterruptedException | RuntimeException e) {
                    handleException(e);
                }
                internalSetPwmEnable(i2, true);
            }
        }
    }

    public synchronized double getServoPosition(int i) {
        validateServo(i);
        int i2 = i + 0;
        Double value = this.lastKnownCommandedPosition[i2].getValue();
        if (value != null) {
            return value.doubleValue();
        }
        try {
            Double valueOf = Double.valueOf(Range.clip(Double.valueOf(Range.scale((double) ((LynxGetServoPulseWidthResponse) new LynxGetServoPulseWidthCommand(getModule(), i2).sendReceive()).getPulseWidth(), this.pwmRanges[i2].usPulseLower, this.pwmRanges[i2].usPulseUpper, apiPositionFirst, 1.0d)).doubleValue(), (double) apiPositionFirst, 1.0d));
            this.lastKnownCommandedPosition[i2].setValue(valueOf);
            return valueOf.doubleValue();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(apiPositionFirst))).doubleValue();
        }
    }

    public synchronized void setServoPwmRange(int i, PwmControl.PwmRange pwmRange) {
        validateServo(i);
        int i2 = i + 0;
        if (!pwmRange.equals(this.pwmRanges[i2])) {
            this.pwmRanges[i2] = pwmRange;
            try {
                new LynxSetServoConfigurationCommand(getModule(), i2, (int) pwmRange.usFrame).send();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
        return;
    }

    public synchronized PwmControl.PwmRange getServoPwmRange(int i) {
        validateServo(i);
        return this.pwmRanges[i + 0];
    }

    private void validateServo(int i) {
        if (i < 0 || i > 5) {
            throw new IllegalArgumentException(String.format("Servo %d is invalid; valid servos are %d..%d", new Object[]{Integer.valueOf(i), 0, 5}));
        }
    }

    private void validateApiServoPosition(double d) {
        if (apiPositionFirst > d || d > 1.0d) {
            throw new IllegalArgumentException(String.format("illegal servo position %f; must be in interval [%f,%f]", new Object[]{Double.valueOf(d), Double.valueOf(apiPositionFirst), Double.valueOf(1.0d)}));
        }
    }
}
