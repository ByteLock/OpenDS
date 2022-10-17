package com.qualcomm.hardware.lynx.commands;

import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxModuleIntf;
import com.qualcomm.robotcore.util.TypeConversion;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;

public abstract class LynxMessage {
    protected boolean hasBeenTransmitted = false;
    protected byte messageNumber = 0;
    protected LynxModuleIntf module;
    protected long nanotimeLastTransmit = 0;
    protected TimeWindow payloadTimeWindow;
    protected byte referenceNumber = 0;
    protected LynxDatagram serialization = null;

    public abstract void fromPayloadByteArray(byte[] bArr);

    public abstract int getCommandNumber();

    public boolean isAck() {
        return false;
    }

    public boolean isAckable() {
        return false;
    }

    public boolean isNack() {
        return false;
    }

    public boolean isResponse() {
        return false;
    }

    public boolean isResponseExpected() {
        return false;
    }

    public void onPretendTransmit() throws InterruptedException {
    }

    public abstract byte[] toPayloadByteArray();

    public LynxMessage(LynxModuleIntf lynxModuleIntf) {
        this.module = lynxModuleIntf;
        setPayloadTimeWindow((TimeWindow) null);
    }

    public static Object invokeStaticNullaryMethod(Class cls, String str) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException {
        Method declaredMethod = cls.getDeclaredMethod(str, new Class[0]);
        if ((declaredMethod.getModifiers() & 9) == 9 && (declaredMethod.getModifiers() & 1024) == 0) {
            return declaredMethod.invoke((Object) null, new Object[0]);
        }
        throw new IllegalAccessException("incorrect modifiers");
    }

    public int getDestModuleAddress() {
        return getModule().getModuleAddress();
    }

    public void noteHasBeenTransmitted() {
        this.hasBeenTransmitted = true;
    }

    public boolean hasBeenTransmitted() {
        return this.hasBeenTransmitted;
    }

    public long getNanotimeLastTransmit() {
        return this.nanotimeLastTransmit;
    }

    public void setNanotimeLastTransmit(long j) {
        this.nanotimeLastTransmit = j;
    }

    public void acquireNetworkLock() throws InterruptedException {
        this.module.acquireNetworkTransmissionLock(this);
    }

    public void releaseNetworkLock() throws InterruptedException {
        this.module.releaseNetworkTransmissionLock(this);
    }

    public void resetModulePingTimer() {
        this.module.resetPingTimer(this);
    }

    public LynxModuleIntf getModule() {
        return this.module;
    }

    public void setModule(LynxModule lynxModule) {
        this.module = lynxModule;
    }

    public int getModuleAddress() {
        return this.module.getModuleAddress();
    }

    public int getMessageNumber() {
        return TypeConversion.unsignedByteToInt(this.messageNumber);
    }

    public void setMessageNumber(int i) {
        this.messageNumber = (byte) i;
    }

    public int getReferenceNumber() {
        return TypeConversion.unsignedByteToInt(this.referenceNumber);
    }

    public void setReferenceNumber(int i) {
        this.referenceNumber = (byte) i;
    }

    public TimeWindow getPayloadTimeWindow() {
        return this.payloadTimeWindow;
    }

    public void setPayloadTimeWindow(TimeWindow timeWindow) {
        this.payloadTimeWindow = timeWindow;
    }

    public LynxDatagram getSerialization() {
        return this.serialization;
    }

    public void forgetSerialization() {
        setSerialization((LynxDatagram) null);
    }

    public void setSerialization(LynxDatagram lynxDatagram) {
        this.serialization = lynxDatagram;
    }

    public void loadFromSerialization() {
        setPayloadTimeWindow(this.serialization.getPayloadTimeWindow());
        fromPayloadByteArray(this.serialization.getPayloadData());
        setMessageNumber(this.serialization.getMessageNumber());
        setReferenceNumber(this.serialization.getReferenceNumber());
    }
}
