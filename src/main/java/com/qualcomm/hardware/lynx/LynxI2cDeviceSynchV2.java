package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteReadMultipleBytesCommand;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.TimestampedI2cData;

public class LynxI2cDeviceSynchV2 extends LynxI2cDeviceSynch {
    public LynxI2cDeviceSynchV2(Context context, LynxModule lynxModule, int i) {
        super(context, lynxModule, i);
    }

    public synchronized TimestampedData readTimeStamped(final int i, final int i2) {
        try {
            final C06851 r0 = new Supplier<LynxCommand<?>>() {
                public LynxCommand<?> get() {
                    return new LynxI2cWriteReadMultipleBytesCommand(LynxI2cDeviceSynchV2.this.getModule(), LynxI2cDeviceSynchV2.this.bus, LynxI2cDeviceSynchV2.this.i2cAddr, i, i2);
                }
            };
            return (TimestampedData) acquireI2cLockWhile(new Supplier<TimestampedData>() {
                public TimestampedData get() throws InterruptedException, RobotCoreException, LynxNackException {
                    LynxI2cDeviceSynchV2.this.sendI2cTransaction(r0);
                    LynxI2cDeviceSynchV2.this.readTimeStampedPlaceholder.reset();
                    LynxI2cDeviceSynchV2 lynxI2cDeviceSynchV2 = LynxI2cDeviceSynchV2.this;
                    return lynxI2cDeviceSynchV2.pollForReadResult(lynxI2cDeviceSynchV2.i2cAddr, i, i2);
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
