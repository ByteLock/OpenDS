package com.qualcomm.hardware.lynx;

import android.content.Context;
import com.qualcomm.hardware.lynx.commands.LynxCommand;
import com.qualcomm.hardware.lynx.commands.LynxInterface;
import com.qualcomm.hardware.lynx.commands.LynxMessage;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.Engagable;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealth;
import com.qualcomm.robotcore.hardware.HardwareDeviceHealthImpl;
import com.qualcomm.robotcore.hardware.RobotCoreLynxController;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.WeakReferenceSet;
import java.util.Iterator;
import java.util.concurrent.Callable;

public abstract class LynxController extends LynxCommExceptionHandler implements RobotCoreLynxController, Engagable, HardwareDeviceHealth, RobotArmingStateNotifier.Callback, RobotArmingStateNotifier {
    protected Context context;
    protected final HardwareDeviceHealthImpl hardwareDeviceHealth;
    protected boolean isEngaged;
    protected boolean isHardwareInitialized;
    protected boolean isHooked;
    /* access modifiers changed from: private */
    public LynxModule module;
    private LynxModuleIntf pretendModule;
    protected final WeakReferenceSet<RobotArmingStateNotifier.Callback> registeredCallbacks = new WeakReferenceSet<>();

    /* access modifiers changed from: protected */
    public void doHook() {
    }

    /* access modifiers changed from: protected */
    public void doUnhook() {
    }

    /* access modifiers changed from: protected */
    public void floatHardware() {
    }

    public void forgetLastKnown() {
    }

    public abstract String getDeviceName();

    /* access modifiers changed from: protected */
    public abstract String getTag();

    public int getVersion() {
        return 1;
    }

    /* access modifiers changed from: protected */
    public void initializeHardware() throws RobotCoreException, InterruptedException {
    }

    public LynxController(Context context2, LynxModule lynxModule) {
        this.context = context2;
        this.module = lynxModule;
        this.isEngaged = true;
        this.isHooked = false;
        this.isHardwareInitialized = false;
        this.pretendModule = new PretendLynxModule();
        this.hardwareDeviceHealth = new HardwareDeviceHealthImpl(getTag(), getHealthStatusOverride());
        this.module.noteController(this);
    }

    /* access modifiers changed from: protected */
    public void finishConstruction() {
        moduleNowArmedOrPretending();
        this.module.registerCallback(this, false);
    }

    /* renamed from: com.qualcomm.hardware.lynx.LynxController$2 */
    static /* synthetic */ class C06682 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$hardware$usb$RobotArmingStateNotifier$ARMINGSTATE */
        static final /* synthetic */ int[] f84x44709aa9;

        /* JADX WARNING: Can't wrap try/catch for region: R(6:0|1|2|3|4|(3:5|6|8)) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        static {
            /*
                com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier$ARMINGSTATE[] r0 = com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier.ARMINGSTATE.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f84x44709aa9 = r0
                com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier$ARMINGSTATE r1 = com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier.ARMINGSTATE.ARMED     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f84x44709aa9     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier$ARMINGSTATE r1 = com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier.ARMINGSTATE.PRETENDING     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f84x44709aa9     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier$ARMINGSTATE r1 = com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier.ARMINGSTATE.DISARMED     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.lynx.LynxController.C06682.<clinit>():void");
        }
    }

    public synchronized void onModuleStateChange(RobotArmingStateNotifier robotArmingStateNotifier, RobotArmingStateNotifier.ARMINGSTATE armingstate) {
        int i = C06682.f84x44709aa9[armingstate.ordinal()];
        if (i == 1 || i == 2) {
            moduleNowArmedOrPretending();
        } else if (i == 3) {
            moduleNowDisarmed();
        }
        Iterator<RobotArmingStateNotifier.Callback> it = this.registeredCallbacks.iterator();
        while (it.hasNext()) {
            it.next().onModuleStateChange(this, armingstate);
        }
    }

    /* access modifiers changed from: protected */
    public void moduleNowArmedOrPretending() {
        adjustHookingToMatchEngagement();
    }

    /* access modifiers changed from: protected */
    public void moduleNowDisarmed() {
        if (this.isHooked) {
            unhook();
        }
    }

    public SerialNumber getSerialNumber() {
        return this.module.getSerialNumber();
    }

    public RobotArmingStateNotifier.ARMINGSTATE getArmingState() {
        return this.module.getArmingState();
    }

    public void registerCallback(RobotArmingStateNotifier.Callback callback, boolean z) {
        this.registeredCallbacks.add(callback);
        if (z) {
            callback.onModuleStateChange(this, getArmingState());
        }
    }

    public void unregisterCallback(RobotArmingStateNotifier.Callback callback) {
        this.registeredCallbacks.remove(callback);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return HardwareDevice.Manufacturer.Lynx;
    }

    public synchronized void close() {
        if (isEngaged()) {
            floatHardware();
            disengage();
        }
        setHealthStatus(HardwareDeviceHealth.HealthStatus.CLOSED);
    }

    public String getConnectionInfo() {
        return getModule().getConnectionInfo();
    }

    public void resetDeviceConfigurationForOpMode() {
        try {
            initializeHardware();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        } catch (RobotCoreException e) {
            RobotLog.m62vv(getTag(), (Throwable) e, "exception initializing hardware; ignored");
        }
    }

    /* access modifiers changed from: package-private */
    public void initializeHardwareIfNecessary() throws RobotCoreException, InterruptedException {
        if (!this.isHardwareInitialized) {
            RobotLog.m61vv(getTag(), "initializeHardware() mod#=%d", Integer.valueOf(getModule().getModuleAddress()));
            initializeHardware();
            this.isHardwareInitialized = isArmed();
        }
    }

    /* access modifiers changed from: protected */
    public void setHealthyIfArmed() {
        if (isArmed()) {
            setHealthStatus(HardwareDeviceHealth.HealthStatus.HEALTHY);
        }
    }

    public void setHealthStatus(HardwareDeviceHealth.HealthStatus healthStatus) {
        this.hardwareDeviceHealth.setHealthStatus(healthStatus);
    }

    /* access modifiers changed from: protected */
    public Callable<HardwareDeviceHealth.HealthStatus> getHealthStatusOverride() {
        return new Callable<HardwareDeviceHealth.HealthStatus>() {
            public HardwareDeviceHealth.HealthStatus call() throws Exception {
                if (LynxController.this.module.getArmingState() == RobotArmingStateNotifier.ARMINGSTATE.PRETENDING) {
                    return HardwareDeviceHealth.HealthStatus.UNHEALTHY;
                }
                return HardwareDeviceHealth.HealthStatus.UNKNOWN;
            }
        };
    }

    public HardwareDeviceHealth.HealthStatus getHealthStatus() {
        return this.hardwareDeviceHealth.getHealthStatus();
    }

    public void engage() {
        synchronized (this) {
            if (!this.isEngaged) {
                RobotLog.m61vv(getTag(), "engaging mod#=%d", Integer.valueOf(getModule().getModuleAddress()));
                this.isEngaged = true;
                adjustHookingToMatchEngagement();
            }
        }
    }

    public void disengage() {
        synchronized (this) {
            if (!this.isEngaged) {
                RobotLog.m61vv(getTag(), "disengage mod#=%d", Integer.valueOf(getModule().getModuleAddress()));
                this.isEngaged = false;
                adjustHookingToMatchEngagement();
            }
        }
    }

    public boolean isEngaged() {
        boolean z;
        synchronized (this) {
            z = this.isEngaged;
        }
        return z;
    }

    /* access modifiers changed from: protected */
    public LynxModuleIntf getModule() {
        return this.isHooked ? this.module : this.pretendModule;
    }

    /* access modifiers changed from: protected */
    public void adjustHookingToMatchEngagement() {
        boolean z = this.isHooked;
        if (!z && this.isEngaged) {
            hook();
        } else if (z && !this.isEngaged) {
            unhook();
        }
    }

    /* access modifiers changed from: protected */
    public void hook() {
        doHook();
        this.isHooked = true;
        try {
            initializeHardwareIfNecessary();
        } catch (InterruptedException unused) {
            Thread.currentThread().interrupt();
        } catch (RobotCoreException e) {
            RobotLog.m50ee(getTag(), (Throwable) e, "exception thrown in LynxController.hook()");
        }
    }

    /* access modifiers changed from: protected */
    public void unhook() {
        doUnhook();
        this.isHooked = false;
    }

    /* access modifiers changed from: protected */
    public boolean isArmed() {
        return this.module.getArmingState() == RobotArmingStateNotifier.ARMINGSTATE.ARMED;
    }

    public class PretendLynxModule implements LynxModuleIntf {
        boolean isEngaged = true;

        public void acquireNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        }

        public void close() {
        }

        public void finishedWithMessage(LynxMessage lynxMessage) throws InterruptedException {
        }

        public String getNullableFirmwareVersionString() {
            return null;
        }

        public int getVersion() {
            return 1;
        }

        public boolean isCommandSupported(Class<? extends LynxCommand> cls) {
            return false;
        }

        public boolean isNotResponding() {
            return false;
        }

        public boolean isOpen() {
            return false;
        }

        public boolean isParent() {
            return true;
        }

        public void noteAttentionRequired() {
        }

        public void noteNotResponding() {
        }

        public void releaseNetworkTransmissionLock(LynxMessage lynxMessage) throws InterruptedException {
        }

        public void resetDeviceConfigurationForOpMode() {
        }

        public void resetPingTimer(LynxMessage lynxMessage) {
        }

        public void retransmit(LynxMessage lynxMessage) throws InterruptedException {
        }

        public void sendCommand(LynxMessage lynxMessage) throws InterruptedException, LynxUnsupportedCommandException {
        }

        public void validateCommand(LynxMessage lynxMessage) throws LynxUnsupportedCommandException {
        }

        public PretendLynxModule() {
        }

        public HardwareDevice.Manufacturer getManufacturer() {
            return HardwareDevice.Manufacturer.Lynx;
        }

        public String getFirmwareVersionString() {
            return getDeviceName();
        }

        public String getDeviceName() {
            return LynxController.this.module.getDeviceName() + " (pretend)";
        }

        public String getConnectionInfo() {
            return LynxController.this.module.getConnectionInfo();
        }

        public SerialNumber getSerialNumber() {
            return LynxController.this.module.getSerialNumber();
        }

        public <T> T acquireI2cLockWhile(Supplier<T> supplier) throws InterruptedException, RobotCoreException, LynxNackException {
            return supplier.get();
        }

        public int getModuleAddress() {
            return LynxController.this.module.getModuleAddress();
        }

        public LynxInterface getInterface(String str) {
            return LynxController.this.module.getInterface(str);
        }

        public boolean isEngaged() {
            return this.isEngaged;
        }

        public void engage() {
            this.isEngaged = true;
        }

        public void disengage() {
            this.isEngaged = false;
        }
    }
}
