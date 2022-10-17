package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Engagable;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.hardware.usb.RobotUsbModule;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import org.firstinspires.ftc.robotcore.external.Consumer;
import org.firstinspires.ftc.robotcore.internal.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.internal.p013ui.ProgressParameters;

public interface LynxUsbDevice extends RobotUsbModule, GlobalWarningSource, RobotCoreLynxUsbDevice, HardwareDevice, SyncdDevice, Engagable {
    void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException;

    LynxModule addConfiguredModule(LynxModule lynxModule) throws RobotCoreException, InterruptedException;

    void changeModuleAddress(LynxModule lynxModule, int i, Runnable runnable);

    LynxModuleMetaList discoverModules(boolean z) throws RobotCoreException, InterruptedException;

    void failSafe();

    LynxModule getConfiguredModule(int i);

    LynxUsbDeviceImpl getDelegationTarget();

    RobotUsbDevice getRobotUsbDevice();

    boolean isSystemSynthetic();

    void noteMissingModule(LynxModule lynxModule, String str);

    void performSystemOperationOnConnectedModule(int i, boolean z, Consumer<LynxModule> consumer) throws RobotCoreException, InterruptedException;

    void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException;

    void removeConfiguredModule(LynxModule lynxModule);

    void setSystemSynthetic(boolean z);

    boolean setupControlHubEmbeddedModule() throws InterruptedException, RobotCoreException;

    void transmit(LynxMessage lynxMessage) throws InterruptedException;

    RobotCoreCommandList.LynxFirmwareUpdateResp updateFirmware(RobotCoreCommandList.FWImage fWImage, String str, Consumer<ProgressParameters> consumer);
}
