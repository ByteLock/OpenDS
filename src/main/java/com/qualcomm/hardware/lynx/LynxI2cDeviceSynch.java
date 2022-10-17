package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.C0660R;
import com.qualcomm.hardware.lynx.LynxUsbUtil;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cConfigureChannelCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadMultipleBytesCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadSingleByteCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadStatusQueryCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cReadStatusQueryResponse;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteMultipleBytesCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteSingleByteCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteStatusQueryCommand;
import com.qualcomm.hardware.lynx.commands.core.LynxI2cWriteStatusQueryResponse;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import com.qualcomm.robotcore.hardware.I2cAddr;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchReadHistory;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchReadHistoryImpl;
import com.qualcomm.robotcore.hardware.I2cDeviceSynchSimple;
import com.qualcomm.robotcore.hardware.I2cWaitControl;
import com.qualcomm.robotcore.hardware.I2cWarningManager;
import com.qualcomm.robotcore.hardware.TimestampedData;
import com.qualcomm.robotcore.hardware.TimestampedI2cData;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.Util;
import java.util.concurrent.BlockingQueue;

public abstract class LynxI2cDeviceSynch extends LynxController implements I2cDeviceSynchSimple, I2cDeviceSynchReadHistory {
    public static final String TAG = "LynxI2cDeviceSynch";
    protected int bus;
    protected I2cAddr i2cAddr;
    private boolean loggingEnabled;
    private String loggingTag;
    private String name;
    private final I2cDeviceSynchReadHistoryImpl readHistory = new I2cDeviceSynchReadHistoryImpl();
    private LynxUsbUtil.Placeholder<TimestampedData> readStatusQueryPlaceholder = new LynxUsbUtil.Placeholder<>(TAG, "readStatusQuery", new Object[0]);
    protected LynxUsbUtil.Placeholder<TimestampedData> readTimeStampedPlaceholder = new LynxUsbUtil.Placeholder<>(TAG, "readTimestamped", new Object[0]);

    public void enableWriteCoalescing(boolean z) {
    }

    /* access modifiers changed from: protected */
    public String getTag() {
        return TAG;
    }

    public boolean isWriteCoalescingEnabled() {
        return false;
    }

    public abstract TimestampedData readTimeStamped(int i, int i2);

    protected LynxI2cDeviceSynch(Context context, LynxModule lynxModule, int i) {
        super(context, lynxModule);
        this.bus = i;
        this.i2cAddr = I2cAddr.zero();
        this.loggingEnabled = false;
        this.loggingTag = TAG;
        finishConstruction();
    }

    public String getDeviceName() {
        return this.context.getString(C0660R.string.lynxI2cDeviceSynchDisplayName);
    }

    public String getConnectionInfo() {
        return String.format("%s; bus %d; addr7=0x%02x", new Object[]{getModule().getConnectionInfo(), Integer.valueOf(this.bus), Integer.valueOf(this.i2cAddr.get7Bit())});
    }

    public void resetDeviceConfigurationForOpMode() {
        super.resetDeviceConfigurationForOpMode();
        setBusSpeed(BusSpeed.STANDARD_100K);
        this.readTimeStampedPlaceholder.reset();
        this.readStatusQueryPlaceholder.reset();
    }

    public void close() {
        setHealthStatus(HardwareDeviceHealth.HealthStatus.CLOSED);
        super.close();
    }

    public boolean isArmed() {
        return super.isArmed();
    }

    public void setI2cAddress(I2cAddr i2cAddr2) {
        this.i2cAddr = i2cAddr2;
    }

    public void setI2cAddr(I2cAddr i2cAddr2) {
        this.i2cAddr = i2cAddr2;
    }

    public I2cAddr getI2cAddress() {
        return this.i2cAddr;
    }

    public I2cAddr getI2cAddr() {
        return this.i2cAddr;
    }

    public void setUserConfiguredName(String str) {
        this.name = str;
    }

    public String getUserConfiguredName() {
        return this.name;
    }

    public void setLogging(boolean z) {
        this.loggingEnabled = z;
    }

    public boolean getLogging() {
        return this.loggingEnabled;
    }

    public void setLoggingTag(String str) {
        this.loggingTag = str;
    }

    public String getLoggingTag() {
        return this.loggingTag;
    }

    public void setHistoryQueueCapacity(int i) {
        this.readHistory.setHistoryQueueCapacity(i);
    }

    public int getHistoryQueueCapacity() {
        return this.readHistory.getHistoryQueueCapacity();
    }

    public BlockingQueue<TimestampedI2cData> getHistoryQueue() {
        return this.readHistory.getHistoryQueue();
    }

    public byte[] read(int i, int i2) {
        return readTimeStamped(i, i2).data;
    }

    public synchronized byte read8(int i) {
        return read(i, 1)[0];
    }

    public byte[] read(int i) {
        return readTimeStamped(i).data;
    }

    public synchronized byte read8() {
        final C06701 r0;
        try {
            r0 = new Supplier<LynxI2cReadSingleByteCommand>() {
                public LynxI2cReadSingleByteCommand get() {
                    return new LynxI2cReadSingleByteCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr);
                }
            };
        } catch (LynxNackException | RobotCoreException | InterruptedException | RuntimeException e) {
            handleException(e);
            return ((Byte) LynxUsbUtil.makePlaceholderValue((byte) 0)).byteValue();
        }
        return ((Byte) acquireI2cLockWhile(new Supplier<Byte>() {
            public Byte get() throws InterruptedException, LynxNackException, RobotCoreException {
                LynxI2cDeviceSynch.this.sendI2cTransaction(r0);
                LynxI2cDeviceSynch lynxI2cDeviceSynch = LynxI2cDeviceSynch.this;
                return Byte.valueOf(lynxI2cDeviceSynch.pollForReadResult(lynxI2cDeviceSynch.i2cAddr, 0, 1).data[0]);
            }
        })).byteValue();
    }

    public synchronized TimestampedData readTimeStamped(final int i) {
        try {
            final C06733 r0 = new Supplier<LynxCommand<?>>() {
                public LynxCommand<?> get() {
                    if (i == 1) {
                        return new LynxI2cReadSingleByteCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr);
                    }
                    return new LynxI2cReadMultipleBytesCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr, i);
                }
            };
            return (TimestampedData) acquireI2cLockWhile(new Supplier<TimestampedData>() {
                public TimestampedData get() throws InterruptedException, LynxNackException, RobotCoreException {
                    LynxI2cDeviceSynch.this.sendI2cTransaction(r0);
                    LynxI2cDeviceSynch.this.readTimeStampedPlaceholder.reset();
                    LynxI2cDeviceSynch lynxI2cDeviceSynch = LynxI2cDeviceSynch.this;
                    return lynxI2cDeviceSynch.pollForReadResult(lynxI2cDeviceSynch.i2cAddr, 0, i);
                }
            });
        } catch (InterruptedException e) {
            e = e;
            handleException(e);
            return this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), 0, i));
        } catch (RobotCoreException e2) {
            e = e2;
            handleException(e);
            return this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), 0, i));
        } catch (RuntimeException e3) {
            e = e3;
            handleException(e);
            return this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), 0, i));
        } catch (LynxNackException e4) {
            I2cWarningManager.notifyProblemI2cDevice(this);
            handleException(e4);
            return this.readTimeStampedPlaceholder.log(TimestampedI2cData.makeFakeData(getI2cAddress(), 0, i));
        }
    }

    public void write(int i, byte[] bArr) {
        internalWrite(i, bArr, I2cWaitControl.ATOMIC);
    }

    public synchronized void write8(int i, int i2) {
        internalWrite(i, new byte[]{(byte) i2}, I2cWaitControl.ATOMIC);
    }

    public synchronized void write8(int i, int i2, I2cWaitControl i2cWaitControl) {
        internalWrite(i, new byte[]{(byte) i2}, i2cWaitControl);
    }

    public synchronized void write(int i, byte[] bArr, I2cWaitControl i2cWaitControl) {
        internalWrite(i, bArr, i2cWaitControl);
    }

    private void internalWrite(int i, byte[] bArr, final I2cWaitControl i2cWaitControl) {
        if (bArr.length > 0) {
            final byte[] concatenateByteArrays = Util.concatenateByteArrays(new byte[]{(byte) i}, bArr);
            final C06755 r4 = new Supplier<LynxCommand<?>>() {
                public LynxCommand<?> get() {
                    if (concatenateByteArrays.length == 1) {
                        return new LynxI2cWriteSingleByteCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr, concatenateByteArrays[0]);
                    }
                    return new LynxI2cWriteMultipleBytesCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr, concatenateByteArrays);
                }
            };
            try {
                acquireI2cLockWhile(new Supplier<Object>() {
                    public Object get() throws InterruptedException, RobotCoreException, LynxNackException {
                        LynxI2cDeviceSynch.this.sendI2cTransaction(r4);
                        LynxI2cDeviceSynch.this.internalWaitForWriteCompletions(i2cWaitControl);
                        return null;
                    }
                });
            } catch (LynxNackException | RobotCoreException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    public synchronized void write(byte[] bArr) {
        internalWrite(bArr, I2cWaitControl.ATOMIC);
    }

    public synchronized void write(byte[] bArr, I2cWaitControl i2cWaitControl) {
        internalWrite(bArr, i2cWaitControl);
    }

    public synchronized void write8(int i) {
        internalWrite(new byte[]{(byte) i}, I2cWaitControl.ATOMIC);
    }

    public synchronized void write8(int i, I2cWaitControl i2cWaitControl) {
        internalWrite(new byte[]{(byte) i}, i2cWaitControl);
    }

    private void internalWrite(final byte[] bArr, final I2cWaitControl i2cWaitControl) {
        if (bArr.length > 0) {
            final C06777 r0 = new Supplier<LynxCommand<?>>() {
                public LynxCommand<?> get() {
                    if (bArr.length == 1) {
                        return new LynxI2cWriteSingleByteCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr, bArr[0]);
                    }
                    return new LynxI2cWriteMultipleBytesCommand(LynxI2cDeviceSynch.this.getModule(), LynxI2cDeviceSynch.this.bus, LynxI2cDeviceSynch.this.i2cAddr, bArr);
                }
            };
            try {
                acquireI2cLockWhile(new Supplier<Object>() {
                    public Object get() throws InterruptedException, RobotCoreException, LynxNackException {
                        LynxI2cDeviceSynch.this.sendI2cTransaction(r0);
                        LynxI2cDeviceSynch.this.internalWaitForWriteCompletions(i2cWaitControl);
                        return null;
                    }
                });
            } catch (LynxNackException | RobotCoreException | InterruptedException | RuntimeException e) {
                handleException(e);
            }
        }
    }

    public synchronized void waitForWriteCompletions(final I2cWaitControl i2cWaitControl) {
        try {
            acquireI2cLockWhile(new Supplier<Object>() {
                public Object get() {
                    LynxI2cDeviceSynch.this.internalWaitForWriteCompletions(i2cWaitControl);
                    return null;
                }
            });
        } catch (LynxNackException | RobotCoreException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
        return;
    }

    /* access modifiers changed from: protected */
    public void sendI2cTransaction(Supplier<? extends LynxCommand<?>> supplier) throws LynxNackException, InterruptedException, RobotCoreException {
        while (true) {
            try {
                ((LynxCommand) supplier.get()).send();
                return;
            } catch (LynxNackException e) {
                int i = C067110.f85xe0728a3e[e.getNack().getNackReasonCodeAsEnum().ordinal()];
                if (i != 1 && i != 2) {
                    throw e;
                }
            }
        }
    }

    /* renamed from: com.qualcomm.hardware.lynx.LynxI2cDeviceSynch$10 */
    static /* synthetic */ class C067110 {

        /* renamed from: $SwitchMap$com$qualcomm$hardware$lynx$commands$standard$LynxNack$StandardReasonCode */
        static final /* synthetic */ int[] f85xe0728a3e;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode[] r0 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f85xe0728a3e = r0
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.I2C_MASTER_BUSY     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f85xe0728a3e     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.I2C_OPERATION_IN_PROGRESS     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f85xe0728a3e     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.hardware.lynx.commands.standard.LynxNack$StandardReasonCode r1 = com.qualcomm.hardware.lynx.commands.standard.LynxNack.StandardReasonCode.I2C_NO_RESULTS_PENDING     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxI2cDeviceSynch.C067110.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public <T> T acquireI2cLockWhile(Supplier<T> supplier) throws InterruptedException, RobotCoreException, LynxNackException {
        return getModule().acquireI2cLockWhile(supplier);
    }

    /* access modifiers changed from: protected */
    public void internalWaitForWriteCompletions(I2cWaitControl i2cWaitControl) {
        if (i2cWaitControl == I2cWaitControl.WRITTEN) {
            boolean z = true;
            while (z) {
                try {
                    if (((LynxI2cWriteStatusQueryResponse) new LynxI2cWriteStatusQueryCommand(getModule(), this.bus).sendReceive()).isStatusOk()) {
                        I2cWarningManager.removeProblemI2cDevice(this);
                        return;
                    } else {
                        I2cWarningManager.notifyProblemI2cDevice(this);
                        return;
                    }
                } catch (LynxNackException e) {
                    int i = C067110.f85xe0728a3e[e.getNack().getNackReasonCodeAsEnum().ordinal()];
                    if (i == 2) {
                        continue;
                    } else if (i != 3) {
                        handleException(e);
                        z = false;
                    } else {
                        return;
                    }
                } catch (InterruptedException | RuntimeException e2) {
                    handleException(e2);
                    z = false;
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public TimestampedData pollForReadResult(I2cAddr i2cAddr2, int i, int i2) {
        boolean z = true;
        while (z) {
            try {
                LynxI2cReadStatusQueryResponse lynxI2cReadStatusQueryResponse = (LynxI2cReadStatusQueryResponse) new LynxI2cReadStatusQueryCommand(getModule(), this.bus, i2).sendReceive();
                long nanoTime = System.nanoTime();
                lynxI2cReadStatusQueryResponse.logResponse();
                TimestampedI2cData timestampedI2cData = new TimestampedI2cData();
                timestampedI2cData.data = lynxI2cReadStatusQueryResponse.getBytes();
                if (!lynxI2cReadStatusQueryResponse.getPayloadTimeWindow().isCleared()) {
                    nanoTime = lynxI2cReadStatusQueryResponse.getPayloadTimeWindow().getNanosecondsLast();
                }
                timestampedI2cData.nanoTime = nanoTime;
                timestampedI2cData.i2cAddr = i2cAddr2;
                timestampedI2cData.register = i;
                if (timestampedI2cData.data.length == i2) {
                    this.readStatusQueryPlaceholder.reset();
                    this.readHistory.addToHistoryQueue(timestampedI2cData);
                    I2cWarningManager.removeProblemI2cDevice(this);
                    return timestampedI2cData;
                }
                RobotLog.m49ee(this.loggingTag, "readStatusQuery: cbExpected=%d cbRead=%d", Integer.valueOf(i2), Integer.valueOf(timestampedI2cData.data.length));
                I2cWarningManager.notifyProblemI2cDevice(this);
                z = false;
            } catch (LynxNackException e) {
                int i3 = C067110.f85xe0728a3e[e.getNack().getNackReasonCodeAsEnum().ordinal()];
                if (!(i3 == 1 || i3 == 2)) {
                    if (i3 != 3) {
                        handleException(e);
                        I2cWarningManager.notifyProblemI2cDevice(this);
                    } else {
                        handleException(e);
                        I2cWarningManager.notifyProblemI2cDevice(this);
                    }
                }
            } catch (InterruptedException | RuntimeException e2) {
                handleException(e2);
            }
        }
        return this.readStatusQueryPlaceholder.log(TimestampedI2cData.makeFakeData(i2cAddr2, i, i2));
    }

    public enum BusSpeed {
        STANDARD_100K {
            /* access modifiers changed from: protected */
            public LynxI2cConfigureChannelCommand.SpeedCode toSpeedCode() {
                return LynxI2cConfigureChannelCommand.SpeedCode.STANDARD_100K;
            }
        },
        FAST_400K {
            /* access modifiers changed from: protected */
            public LynxI2cConfigureChannelCommand.SpeedCode toSpeedCode() {
                return LynxI2cConfigureChannelCommand.SpeedCode.FAST_400K;
            }
        };

        /* access modifiers changed from: protected */
        public LynxI2cConfigureChannelCommand.SpeedCode toSpeedCode() {
            throw new AbstractMethodError();
        }
    }

    public void setBusSpeed(BusSpeed busSpeed) {
        try {
            new LynxI2cConfigureChannelCommand(getModule(), this.bus, busSpeed.toSpeedCode()).send();
        } catch (LynxNackException | InterruptedException | RuntimeException e) {
            handleException(e);
        }
    }
}
