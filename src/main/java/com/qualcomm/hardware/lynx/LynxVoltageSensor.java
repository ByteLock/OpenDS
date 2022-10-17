package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetADCResponse;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.VoltageSensor;

public class LynxVoltageSensor extends LynxController implements VoltageSensor {
    public static final String TAG = "LynxVoltageSensor";

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public LynxVoltageSensor(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        finishConstruction();
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxVoltageSensorDisplayName);
    }

    public double getVoltage() {
        try {
            return ((double) ((LynxGetADCResponse) new LynxGetADCCommand(getModule(), LynxGetADCCommand.Channel.BATTERY_MONITOR, LynxGetADCCommand.Mode.ENGINEERING).sendReceive()).getValue()) * 0.001d;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Double) LynxUsbUtil.makePlaceholderValue(Double.valueOf(LynxServoController.apiPositionFirst))).doubleValue();
        }
    }
}
