package com.qualcomm.hardware.lynx;

import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.LynxInterface;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Engagable;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.RobotCoreLynxModule;

public interface LynxModuleIntf extends RobotCoreLynxModule, HardwareDevice, Engagable {
    <T> T acquireI2cLockWhile(Supplier<T> supplier) throws InterruptedException, RobotCoreException, LynxNackException;

    void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException;

    void finishedWithMessage(LynxMessage lynxMessage) throws InterruptedException;

    LynxInterface getInterface(String str);

    boolean isCommandSupported(Class<? extends LynxCommand> cls);

    boolean isNotResponding();

    boolean isOpen();

    void noteAttentionRequired();

    void noteNotResponding();

    void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException;

    void resetPingTimer(LynxMessage lynxMessage);

    void retransmit(LynxMessage lynxMessage) throws InterruptedException;

    void sendCommand(LynxMessage lynxMessage) throws InterruptedException, LynxUnsupportedCommandException;

    void validateCommand(LynxMessage lynxMessage) throws LynxUnsupportedCommandException;
}
