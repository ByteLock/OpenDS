package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.commands.core.LynxGetDIODirectionCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetDIODirectionResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxGetSingleDIOInputCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxGetSingleDIOInputResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxSetDIODirectionCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DigitalChannel;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.util.LastKnown;
import com.qualcomm.robotcore.util.SerialNumber;

public class LynxDigitalChannelController extends LynxController implements DigitalChannelController {
    public static final String TAG = "LynxDigitalChannelController";
    public static final int apiPinFirst = 0;
    public static final int apiPinLast = 7;
    protected final PinProperties[] pins = new PinProperties[8];

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    protected class PinProperties {
        LastKnown<DigitalChannel.Mode> lastKnownMode = new LastKnown<>();
        LastKnown<Boolean> lastKnownState = new LastKnown<>();

        protected PinProperties() {
        }
    }

    public LynxDigitalChannelController(Context context, LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        super(context, lynxModule);
        for (int i = 0; i <= 7; i++) {
            this.pins[i + 0] = new PinProperties();
        }
        finishConstruction();
    }

    public void initializeHardware() {
        forgetLastKnown();
        for (int i = 0; i <= 7; i++) {
            internalSetDigitalChannelMode(i + 0, DigitalChannel.Mode.INPUT);
        }
    }

    public void forgetLastKnown() {
        for (PinProperties pinProperties : this.pins) {
            pinProperties.lastKnownMode.invalidate();
            pinProperties.lastKnownState.invalidate();
        }
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxDigitalChannelControllerDisplayName);
    }

    public SerialNumber getSerialNumber() {
        return getModule().getSerialNumber();
    }

    public synchronized DigitalChannel.Mode getDigitalChannelMode(int i) {
        validatePin(i);
        int i2 = i + 0;
        DigitalChannel.Mode value = this.pins[i2].lastKnownMode.getValue();
        if (value != null) {
            return value;
        }
        try {
            DigitalChannel.Mode mode = ((LynxGetDIODirectionResponse) new LynxGetDIODirectionCommand(getModule(), i2).sendReceive()).getMode();
            this.pins[i2].lastKnownMode.setValue(mode);
            return mode;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return (DigitalChannel.Mode) LynxUsbUtil.makePlaceholderValue(DigitalChannel.Mode.INPUT);
        }
    }

    public synchronized void setDigitalChannelMode(int i, DigitalChannel.Mode mode) {
        DigitalChannel.Mode digitalChannelMode = getDigitalChannelMode(i);
        validatePin(i);
        int i2 = i + 0;
        internalSetDigitalChannelMode(i2, mode);
        if (digitalChannelMode == DigitalChannel.Mode.INPUT && mode == DigitalChannel.Mode.OUTPUT) {
            this.pins[i2].lastKnownState.setValue(false);
        }
    }

    @Deprecated
    public void setDigitalChannelMode(int i, DigitalChannelController.Mode mode) {
        setDigitalChannelMode(i, mode.migrate());
    }

    /* access modifiers changed from: package-private */
    public void internalSetDigitalChannelMode(int i, DigitalChannel.Mode mode) {
        try {
            new LynxSetDIODirectionCommand(getModule(), i, mode).send();
            this.pins[i].lastKnownMode.setValue(mode);
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            this.pins[i].lastKnownMode.invalidate();
        }
    }

    public synchronized boolean getDigitalChannelState(int i) {
        DigitalChannel.Mode digitalChannelMode = getDigitalChannelMode(i);
        validatePin(i);
        int i2 = i + 0;
        if (digitalChannelMode == DigitalChannel.Mode.OUTPUT) {
            return this.pins[i2].lastKnownState.getNonTimedValue().booleanValue();
        }
        LynxGetSingleDIOInputCommand lynxGetSingleDIOInputCommand = new LynxGetSingleDIOInputCommand(getModule(), i2);
        if (getModule() instanceof LynxModule) {
            LynxModule lynxModule = (LynxModule) getModule();
            if (lynxModule.getBulkCachingMode() != LynxModule.BulkCachingMode.OFF) {
                return lynxModule.recordBulkCachingCommandIntent(lynxGetSingleDIOInputCommand).getDigitalChannelState(i2);
            }
        }
        try {
            boolean value = ((LynxGetSingleDIOInputResponse) lynxGetSingleDIOInputCommand.sendReceive()).getValue();
            this.pins[i2].lastKnownState.setValue(Boolean.valueOf(value));
            return value;
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Boolean) LynxUsbUtil.makePlaceholderValue(false)).booleanValue();
        }
    }

    /* JADX WARNING: Code restructure failed: missing block: B:18:0x003c, code lost:
        return;
     */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public synchronized void setDigitalChannelState(int r3, boolean r4) {
        /*
            r2 = this;
            monitor-enter(r2)
            com.qualcomm.robotcore.hardware.DigitalChannel$Mode r0 = r2.getDigitalChannelMode(r3)     // Catch:{ all -> 0x003d }
            r2.validatePin(r3)     // Catch:{ all -> 0x003d }
            int r3 = r3 + 0
            com.qualcomm.robotcore.hardware.DigitalChannel$Mode r1 = com.qualcomm.robotcore.hardware.DigitalChannel.Mode.OUTPUT     // Catch:{ all -> 0x003d }
            if (r0 != r1) goto L_0x003b
            com.qualcomm.hardware.lynx.commands.core.LynxSetSingleDIOOutputCommand r0 = new com.qualcomm.hardware.lynx.commands.core.LynxSetSingleDIOOutputCommand     // Catch:{ all -> 0x003d }
            com.qualcomm.hardware.lynx.LynxModuleIntf r1 = r2.getModule()     // Catch:{ all -> 0x003d }
            r0.<init>(r1, r3, r4)     // Catch:{ all -> 0x003d }
            r0.send()     // Catch:{ InterruptedException -> 0x002c, RuntimeException -> 0x002a, LynxNackException -> 0x0028 }
            com.qualcomm.hardware.lynx.LynxDigitalChannelController$PinProperties[] r0 = r2.pins     // Catch:{ all -> 0x003d }
            r3 = r0[r3]     // Catch:{ all -> 0x003d }
            com.qualcomm.robotcore.util.LastKnown<java.lang.Boolean> r3 = r3.lastKnownState     // Catch:{ all -> 0x003d }
            java.lang.Boolean r4 = java.lang.Boolean.valueOf(r4)     // Catch:{ all -> 0x003d }
            r3.setValue(r4)     // Catch:{ all -> 0x003d }
            goto L_0x003b
        L_0x0028:
            r4 = move-exception
            goto L_0x002d
        L_0x002a:
            r4 = move-exception
            goto L_0x002d
        L_0x002c:
            r4 = move-exception
        L_0x002d:
            r2.handleException(r4)     // Catch:{ all -> 0x003d }
            com.qualcomm.hardware.lynx.LynxDigitalChannelController$PinProperties[] r4 = r2.pins     // Catch:{ all -> 0x003d }
            r3 = r4[r3]     // Catch:{ all -> 0x003d }
            com.qualcomm.robotcore.util.LastKnown<java.lang.Boolean> r3 = r3.lastKnownState     // Catch:{ all -> 0x003d }
            r3.invalidate()     // Catch:{ all -> 0x003d }
            monitor-exit(r2)
            return
        L_0x003b:
            monitor-exit(r2)
            return
        L_0x003d:
            r3 = move-exception
            monitor-exit(r2)
            throw r3
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxDigitalChannelController.setDigitalChannelState(int, boolean):void");
    }

    private void validatePin(int i) {
        if (i < 0 || i > 7) {
            throw new IllegalArgumentException(String.format("pin %d is invalid; valid pins are %d..%d", new Object[]{Integer.valueOf(i), 0, 7}));
        }
    }
}
