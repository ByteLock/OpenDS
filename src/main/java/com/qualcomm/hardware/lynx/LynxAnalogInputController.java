package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.util.SerialNumber;

public class LynxAnalogInputController extends LynxController implements AnalogInputController {
    public static final String TAG = "LynxAnalogInputController";
    public static final int apiPortFirst = 0;
    public static final int apiPortLast = 3;

    public double getMaxAnalogInputVoltage() {
        return 3.3d;
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public LynxAnalogInputController(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        finishConstruction();
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxAnalogInputControllerDisplayName);
    }

    public SerialNumber getSerialNumber() {
        return getModule().getSerialNumber();
    }

    public double getAnalogInputVoltage(int i) {
        validatePort(i);
        int i2 = i + 0;
        LynxGetADCCommand lynxGetADCCommand = new LynxGetADCCommand(getModule(), LynxGetADCCommand.Channel.user(i2), LynxGetADCCommand.Mode.ENGINEERING);
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxGetADCCommand).getAnalogInputVoltage(i2);
            }
        }
        try {
            return ((double) ((LynxGetADCResponse) lynxGetADCCommand.sendReceive()).getValue()) * 0.001d;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }

    private void validatePort(int i) {
        if (i < 0 || i > 3) {
            throw new IllegalArgumentException(String.format("port %d is invalid; valid ports are %d..%d", new Object[]{Integer.valueOf(i), 0, 3}));
        }
    }
}
