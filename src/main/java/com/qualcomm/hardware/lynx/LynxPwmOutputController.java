package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMConfigurationCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMConfigurationResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMEnableResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMPulseWidthCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetPWMPulseWidthResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMConfigurationCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMEnableCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxSetPWMPulseWidthCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.PWMOutputController;
import com.qualcomm.robotcore.hardware.PWMOutputControllerEx;
import com.qualcomm.robotcore.util.LastKnown;
import com.qualcomm.robotcore.util.SerialNumber;

@Deprecated
public class LynxPwmOutputController extends LynxController implements PWMOutputController, PWMOutputControllerEx {
    public static final String TAG = "LynxPwmOutputController";
    public static final int apiPortFirst = 0;
    public static final int apiPortLast = 3;
    protected LastKnown<Integer>[] lastKnownOutputTimes = LastKnown.createArray(4);
    protected LastKnown<Integer>[] lastKnownPulseWidthPeriods = LastKnown.createArray(4);

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public LynxPwmOutputController(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        finishConstruction();
    }

    public void initializeHardware() {
        for (int i = 0; i <= 3; i++) {
            setPwmDisable(i);
            internalSetPulseWidthPeriod(i + 0, 20000);
        }
    }

    public void floatHardware() {
        for (int i = 0; i <= 3; i++) {
            setPwmDisable(i);
        }
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxPwmOutputControllerDisplayName);
    }

    public SerialNumber getSerialNumber() {
        return getModule().getSerialNumber();
    }

    public synchronized void setPulseWidthOutputTime(int i, int i2) {
        validatePort(i);
        int i3 = i + 0;
        internalSetPulseWidthOutputTime(i3, i2);
        setPwmEnable(i3 + 0);
    }

    /* access modifiers changed from: package-private */
    public void internalSetPulseWidthOutputTime(int i, int i2) {
        if (this.lastKnownOutputTimes[i].updateValue(Integer.valueOf(i2))) {
            try {
                new LynxSetPWMPulseWidthCommand(getModule(), i, i2).send();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    public synchronized void setPulseWidthPeriod(int i, int i2) {
        validatePort(i);
        int i3 = i + 0;
        internalSetPulseWidthPeriod(i3, i2);
        setPwmEnable(i3 + 0);
    }

    /* access modifiers changed from: package-private */
    public void internalSetPulseWidthPeriod(int i, int i2) {
        if (this.lastKnownOutputTimes[i].updateValue(Integer.valueOf(i2))) {
            try {
                new LynxSetPWMConfigurationCommand(getModule(), i, i2).send();
            } catch (LynxNackException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    public synchronized int getPulseWidthOutputTime(int i) {
        validatePort(i);
        try {
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
        return ((LynxGetPWMPulseWidthResponse) new LynxGetPWMPulseWidthCommand(getModule(), i + 0).sendReceive()).getPulseWidth();
    }

    public synchronized int getPulseWidthPeriod(int i) {
        validatePort(i);
        try {
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Integer) LynxUsbUtil.makePlaceholderValue(0)).intValue();
        }
        return ((LynxGetPWMConfigurationResponse) new LynxGetPWMConfigurationCommand(getModule(), i + 0).sendReceive()).getFramePeriod();
    }

    public synchronized void setPwmEnable(int i) {
        validatePort(i);
        internalSetPwmEnable(i + 0, true);
    }

    public synchronized void setPwmDisable(int i) {
        validatePort(i);
        internalSetPwmEnable(i + 0, false);
    }

    public synchronized boolean isPwmEnabled(int i) {
        validatePort(i);
        return internalGetPwmEnable(i + 0);
    }

    private void internalSetPwmEnable(int i, boolean z) {
        try {
            new LynxSetPWMEnableCommand(getModule(), i, z).send();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }

    private boolean internalGetPwmEnable(int i) {
        try {
            return ((LynxGetPWMEnableResponse) new LynxGetPWMEnableCommand(getModule(), i).sendReceive()).isEnabled();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(true)).booleanValue();
        }
    }

    private void validatePort(int i) {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException(String.format("port %d is invalid; valid ports are %d..%d", new Object[]{Integer.valueOf(i), 0, 3}));
        }
    }
}
