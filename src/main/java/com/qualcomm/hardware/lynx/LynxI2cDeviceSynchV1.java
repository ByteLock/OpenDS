package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadMultipleBytesCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadSingleByteCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteSingleByteCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.TimestampedI2cData;

public class LynxI2cDeviceSynchV1 extends LynxI2cDeviceSynch {
    public LynxI2cDeviceSynchV1(Context context, LynxModule lynxModule, int i) {
        super(context, lynxModule, i);
    }

    public synchronized TimestampedData readTimeStamped(final int i, final int i2) {
        try {
            final C06821 r2 = new Supplier<LynxI2cWriteSingleByteCommand>() {
                public LynxI2cWriteSingleByteCommand get() {
                    return new LynxI2cWriteSingleByteCommand(LynxI2cDeviceSynchV1.this.getModule(), LynxI2cDeviceSynchV1.this.bus, LynxI2cDeviceSynchV1.this.i2cAddr, i);
                }
            };
            final C06832 r3 = new Supplier<LynxCommand<?>>() {
                public LynxCommand<?> get() {
                    if (i2 == 1) {
                        return new LynxI2cReadSingleByteCommand(LynxI2cDeviceSynchV1.this.getModule(), LynxI2cDeviceSynchV1.this.bus, LynxI2cDeviceSynchV1.this.i2cAddr);
                    }
                    return new LynxI2cReadMultipleBytesCommand(LynxI2cDeviceSynchV1.this.getModule(), LynxI2cDeviceSynchV1.this.bus, LynxI2cDeviceSynchV1.this.i2cAddr, i2);
                }
            };
            final int i3 = i;
            final int i4 = i2;
            return (TimestampedData) acquireI2cLockWhile(new Supplier<TimestampedData>() {
                public TimestampedData get() throws InterruptedException, RobotCoreException, LynxNackException {
                    LynxI2cDeviceSynchV1.this.sendI2cTransaction(r2);
                    LynxI2cDeviceSynchV1.this.internalWaitForWriteCompletions(I2cWaitControl.ATOMIC);
                    LynxI2cDeviceSynchV1.this.sendI2cTransaction(r3);
                    LynxI2cDeviceSynchV1.this.readTimeStampedPlaceholder.reset();
                    LynxI2cDeviceSynchV1 lynxI2cDeviceSynchV1 = LynxI2cDeviceSynchV1.this;
                    return lynxI2cDeviceSynchV1.pollForReadResult(lynxI2cDeviceSynchV1.i2cAddr, i3, i4);
                }
            });
        } catch (InterruptedException e) {
            e = e;
            handleException(e);
            return (TimestampedData) this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), i, i2));
        } catch (RobotCoreException e2) {
            e = e2;
            handleException(e);
            return (TimestampedData) this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), i, i2));
        } catch (RuntimeException e3) {
            e = e3;
            handleException(e);
            return (TimestampedData) this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), i, i2));
        } catch (LynxNackException e4) {
            I2cWarningManager.notifyProblemI2cDevice(this);
            handleException(e4);
            return (TimestampedData) this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), i, i2));
        }
    }
}
