package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareDeviceCloseOnTearDown;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.system.Assert;

public class LynxUsbDeviceDelegate implements LynxUsbDevice, HardwareDeviceCloseOnTearDown {
    public static String TAG = "LynxUsb";
    protected LynxUsbDeviceImpl delegate;
    protected boolean isOpen = true;
    protected boolean releaseOnClose = true;

    public LynxUsbDeviceDelegate(LynxUsbDeviceImpl lynxUsbDeviceImpl) {
        this.delegate = lynxUsbDeviceImpl;
        RobotLog.m61vv(TAG, "0x%08x on 0x%08x: new delegate to [%s]", Integer.valueOf(hashCode()), Integer.valueOf(this.delegate.hashCode()), this.delegate.getSerialNumber());
    }

    public LynxUsbDeviceImpl getDelegationTarget() {
        return this.delegate;
    }

    public synchronized void close() {
        if (this.releaseOnClose) {
            RobotLog.m61vv(TAG, "0x%08x on 0x%08x: releasing delegate to [%s]", Integer.valueOf(hashCode()), Integer.valueOf(this.delegate.hashCode()), this.delegate.getSerialNumber());
            this.releaseOnClose = false;
            this.delegate.releaseRef();
            this.isOpen = false;
        } else {
            RobotLog.m49ee(TAG, "0x%08x on 0x%08x: closing closed[%s]; ignored", Integer.valueOf(hashCode()), Integer.valueOf(this.delegate.hashCode()), this.delegate.getSerialNumber());
        }
    }

    /* access modifiers changed from: protected */
    public void assertOpen() {
        if (!this.isOpen) {
            Assert.assertTrue(false, "0x%08x on 0x%08x: closed", Integer.valueOf(hashCode()), Integer.valueOf(this.delegate.hashCode()));
        }
    }

    public void disengage() {
        assertOpen();
        this.delegate.disengage();
    }

    public void engage() {
        assertOpen();
        this.delegate.engage();
    }

    public boolean isEngaged() {
        assertOpen();
        return this.delegate.isEngaged();
    }

    public RobotUsbDevice getRobotUsbDevice() {
        assertOpen();
        return this.delegate.getRobotUsbDevice();
    }

    public boolean isSystemSynthetic() {
        assertOpen();
        return this.delegate.isSystemSynthetic();
    }

    public void setSystemSynthetic(boolean z) {
        assertOpen();
        this.delegate.setSystemSynthetic(z);
    }

    public void failSafe() {
        assertOpen();
        this.delegate.failSafe();
    }

    public void lockNetworkLockAcquisitions() {
        this.delegate.lockNetworkLockAcquisitions();
    }

    public void setThrowOnNetworkLockAcquisition(boolean z) {
        this.delegate.setThrowOnNetworkLockAcquisition(z);
    }

    public void changeModuleAddress(LynxModule lynxModule, int i, Runnable runnable) {
        assertOpen();
        this.delegate.changeModuleAddress(lynxModule, i, runnable);
    }

    public void noteMissingModule(LynxModule lynxModule, String str) {
        assertOpen();
        this.delegate.noteMissingModule(lynxModule, str);
    }

    public void performSystemOperationOnConnectedModule(int i, boolean z, Consumer<LynxModule> consumer) throws RobotCoreException, InterruptedException {
        assertOpen();
        this.delegate.performSystemOperationOnConnectedModule(i, z, consumer);
    }

    public LynxModule addConfiguredModule(LynxModule lynxModule) throws RobotCoreException, InterruptedException {
        assertOpen();
        return this.delegate.addConfiguredModule(lynxModule);
    }

    public LynxModule getConfiguredModule(int i) {
        assertOpen();
        return this.delegate.getConfiguredModule(i);
    }

    public void removeConfiguredModule(LynxModule lynxModule) {
        assertOpen();
        this.delegate.removeConfiguredModule(lynxModule);
    }

    public LynxModuleMetaList discoverModules(boolean z) throws RobotCoreException, InterruptedException {
        assertOpen();
        return this.delegate.discoverModules(z);
    }

    public void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        assertOpen();
        this.delegate.acquireNetworkTransmissionLock(lynxMessage);
    }

    public void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        assertOpen();
        this.delegate.releaseNetworkTransmissionLock(lynxMessage);
    }

    public void transmit(LynxMessage lynxMessage) throws InterruptedException {
        assertOpen();
        this.delegate.transmit(lynxMessage);
    }

    public boolean setupControlHubEmbeddedModule() throws RobotCoreException, InterruptedException {
        assertOpen();
        return this.delegate.setupControlHubEmbeddedModule();
    }

    public RobotCoreCommandList.LynxFirmwareUpdateResp updateFirmware(RobotCoreCommandList.FWImage fWImage, String str, Consumer<ProgressParameters> consumer) {
        return this.delegate.updateFirmware(fWImage, str, consumer);
    }

    public String getDeviceName() {
        assertOpen();
        return this.delegate.getDeviceName();
    }

    public String getConnectionInfo() {
        assertOpen();
        return this.delegate.getConnectionInfo();
    }

    public int getVersion() {
        assertOpen();
        return this.delegate.getVersion();
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        assertOpen();
        return this.delegate.getManufacturer();
    }

    public void resetDeviceConfigurationForOpMode() {
        assertOpen();
        this.delegate.resetDeviceConfigurationForOpMode();
    }

    public SyncdDevice.ShutdownReason getShutdownReason() {
        assertOpen();
        return this.delegate.getShutdownReason();
    }

    public void setOwner(RobotUsbModule robotUsbModule) {
        assertOpen();
        this.delegate.setOwner(robotUsbModule);
    }

    public RobotUsbModule getOwner() {
        assertOpen();
        return this.delegate.getOwner();
    }

    public SerialNumber getSerialNumber() {
        assertOpen();
        return this.delegate.getSerialNumber();
    }

    public RobotArmingStateNotifier.ARMINGSTATE getArmingState() {
        assertOpen();
        return this.delegate.getArmingState();
    }

    public void registerCallback(RobotArmingStateNotifier.Callback callback, boolean z) {
        assertOpen();
        this.delegate.registerCallback(callback, z);
    }

    public void unregisterCallback(RobotArmingStateNotifier.Callback callback) {
        assertOpen();
        this.delegate.unregisterCallback(callback);
    }

    public void arm() throws RobotCoreException, InterruptedException {
        assertOpen();
        this.delegate.arm();
    }

    public void pretend() throws RobotCoreException, InterruptedException {
        assertOpen();
        this.delegate.pretend();
    }

    public void armOrPretend() throws RobotCoreException, InterruptedException {
        assertOpen();
        this.delegate.armOrPretend();
    }

    public void disarm() throws RobotCoreException, InterruptedException {
        assertOpen();
        this.delegate.disarm();
    }

    public String getGlobalWarning() {
        assertOpen();
        return this.delegate.getGlobalWarning();
    }

    public boolean shouldTriggerWarningSound() {
        return this.delegate.shouldTriggerWarningSound();
    }

    public void suppressGlobalWarning(boolean z) {
        assertOpen();
        this.delegate.suppressGlobalWarning(z);
    }

    public void setGlobalWarning(String str) {
        assertOpen();
        this.delegate.setGlobalWarning(str);
    }

    public void clearGlobalWarning() {
        assertOpen();
        this.delegate.clearGlobalWarning();
    }
}
