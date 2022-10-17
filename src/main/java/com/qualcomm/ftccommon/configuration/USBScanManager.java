package com.qualcomm.ftccommon.configuration;

import android.content.Context;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.hardware.HardwareDeviceManager;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.LynxModuleMetaList;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.NextLock;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import com.qualcomm.robotcore.util.ThreadPool;
import java.util.Map;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.external.function.Supplier;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;

public class USBScanManager {
    public static final String TAG = "FtcConfigTag";
    public static final int msWaitDefault = 4000;
    protected Context context;
    protected DeviceManager deviceManager;
    protected ExecutorService executorService = null;
    protected boolean isRemoteConfig;
    protected final Map<String, LynxModuleDiscoveryState> lynxModuleDiscoveryStateMap = new ConcurrentHashMap();
    protected NetworkConnectionHandler networkConnectionHandler = NetworkConnectionHandler.getInstance();
    protected ScannedDevices remoteScannedDevices;
    protected final Object remoteScannedDevicesLock = new Object();
    protected NextLock scanResultsSequence;
    protected ThreadPool.Singleton<ScannedDevices> scanningSingleton = new ThreadPool.Singleton<>();

    protected class LynxModuleDiscoveryState {
        protected NextLock lynxDiscoverySequence = new NextLock();
        protected ThreadPool.Singleton<LynxModuleMetaList> lynxDiscoverySingleton = new ThreadPool.Singleton<>();
        protected final Object remoteLynxDiscoveryLock = new Object();
        protected LynxModuleMetaList remoteLynxModules;
        protected SerialNumber serialNumber;

        protected LynxModuleDiscoveryState(SerialNumber serialNumber2) {
            this.serialNumber = serialNumber2;
            this.remoteLynxModules = new LynxModuleMetaList(serialNumber2);
            startExecutorService();
        }

        /* access modifiers changed from: protected */
        public void startExecutorService() {
            ExecutorService executorService = USBScanManager.this.executorService;
            if (executorService != null) {
                this.lynxDiscoverySingleton.reset();
                this.lynxDiscoverySingleton.setService(executorService);
            }
        }
    }

    public USBScanManager(Context context2, boolean z) {
        this.context = context2;
        this.isRemoteConfig = z;
        this.scanResultsSequence = new NextLock();
        if (!z) {
            this.deviceManager = new HardwareDeviceManager(context2, (SyncdDevice.Manager) null);
        }
    }

    public void startExecutorService() {
        this.executorService = ThreadPool.newCachedThreadPool("USBScanManager");
        this.scanningSingleton.reset();
        this.scanningSingleton.setService(this.executorService);
        for (LynxModuleDiscoveryState startExecutorService : this.lynxModuleDiscoveryStateMap.values()) {
            startExecutorService.startExecutorService();
        }
    }

    public void stopExecutorService() {
        this.executorService.shutdownNow();
        ThreadPool.awaitTerminationOrExitApplication(this.executorService, 5, TimeUnit.SECONDS, "USBScanManager service", "internal error");
        this.executorService = null;
    }

    public ExecutorService getExecutorService() {
        return this.executorService;
    }

    public DeviceManager getDeviceManager() {
        return this.deviceManager;
    }

    /* access modifiers changed from: package-private */
    public LynxModuleDiscoveryState getDiscoveryState(SerialNumber serialNumber) {
        LynxModuleDiscoveryState lynxModuleDiscoveryState;
        synchronized (this.lynxModuleDiscoveryStateMap) {
            lynxModuleDiscoveryState = this.lynxModuleDiscoveryStateMap.get(serialNumber.getString());
            if (lynxModuleDiscoveryState == null) {
                lynxModuleDiscoveryState = new LynxModuleDiscoveryState(serialNumber);
                this.lynxModuleDiscoveryStateMap.put(serialNumber.getString(), lynxModuleDiscoveryState);
            }
        }
        return lynxModuleDiscoveryState;
    }

    public Supplier<LynxModuleMetaList> getLynxModuleMetaListSupplier(final SerialNumber serialNumber) {
        return new Supplier<LynxModuleMetaList>() {
            public LynxModuleMetaList get() {
                try {
                    return USBScanManager.this.startLynxModuleEnumerationIfNecessary(serialNumber).await();
                } catch (InterruptedException unused) {
                    Thread.currentThread().interrupt();
                    return null;
                }
            }
        };
    }

    public ThreadPool.SingletonResult<LynxModuleMetaList> startLynxModuleEnumerationIfNecessary(final SerialNumber serialNumber) {
        final LynxModuleDiscoveryState discoveryState = getDiscoveryState(serialNumber);
        return discoveryState.lynxDiscoverySingleton.submit((int) msWaitDefault, new Callable<LynxModuleMetaList>() {
            /* JADX WARNING: Removed duplicated region for block: B:29:0x0083 A[SYNTHETIC, Splitter:B:29:0x0083] */
            /* JADX WARNING: Removed duplicated region for block: B:43:0x00be  */
            /* JADX WARNING: Removed duplicated region for block: B:44:0x00c1  */
            /* Code decompiled incorrectly, please refer to instructions dump. */
            public com.qualcomm.robotcore.hardware.LynxModuleMetaList call() throws java.lang.InterruptedException {
                /*
                    r8 = this;
                    com.qualcomm.ftccommon.configuration.USBScanManager r0 = com.qualcomm.ftccommon.configuration.USBScanManager.this
                    boolean r0 = r0.isRemoteConfig
                    if (r0 == 0) goto L_0x0041
                    com.qualcomm.ftccommon.configuration.USBScanManager$LynxModuleDiscoveryState r0 = r0
                    com.qualcomm.robotcore.util.NextLock r0 = r0.lynxDiscoverySequence
                    com.qualcomm.robotcore.util.NextLock$Waiter r0 = r0.getNextWaiter()
                    java.lang.String r1 = "FtcConfigTag"
                    java.lang.String r2 = "sending remote lynx module discovery request..."
                    com.qualcomm.robotcore.util.RobotLog.m60vv(r1, r2)
                    com.qualcomm.ftccommon.configuration.USBScanManager r1 = com.qualcomm.ftccommon.configuration.USBScanManager.this
                    org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler r1 = r1.networkConnectionHandler
                    com.qualcomm.robotcore.robocol.Command r2 = new com.qualcomm.robotcore.robocol.Command
                    java.lang.String r3 = "CMD_DISCOVER_LYNX_MODULES"
                    com.qualcomm.robotcore.util.SerialNumber r4 = r4
                    java.lang.String r4 = r4.getString()
                    r2.<init>(r3, r4)
                    r1.sendCommand(r2)
                    r0.awaitNext()
                    java.lang.String r0 = "FtcConfigTag"
                    java.lang.String r1 = "...remote scan lynx module discovery completed."
                    com.qualcomm.robotcore.util.RobotLog.m60vv(r0, r1)
                    com.qualcomm.ftccommon.configuration.USBScanManager$LynxModuleDiscoveryState r0 = r0
                    java.lang.Object r0 = r0.remoteLynxDiscoveryLock
                    monitor-enter(r0)
                    com.qualcomm.ftccommon.configuration.USBScanManager$LynxModuleDiscoveryState r1 = r0     // Catch:{ all -> 0x003e }
                    com.qualcomm.robotcore.hardware.LynxModuleMetaList r1 = r1.remoteLynxModules     // Catch:{ all -> 0x003e }
                    monitor-exit(r0)     // Catch:{ all -> 0x003e }
                    return r1
                L_0x003e:
                    r1 = move-exception
                    monitor-exit(r0)     // Catch:{ all -> 0x003e }
                    throw r1
                L_0x0041:
                    java.lang.String r0 = "FtcConfigTag"
                    java.lang.String r1 = "discovering lynx modules on lynx device=%s..."
                    r2 = 1
                    java.lang.Object[] r3 = new java.lang.Object[r2]
                    com.qualcomm.robotcore.util.SerialNumber r4 = r4
                    r5 = 0
                    r3[r5] = r4
                    com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r1, (java.lang.Object[]) r3)
                    r0 = 0
                    com.qualcomm.ftccommon.configuration.USBScanManager r1 = com.qualcomm.ftccommon.configuration.USBScanManager.this     // Catch:{ all -> 0x007f }
                    com.qualcomm.robotcore.hardware.DeviceManager r1 = r1.deviceManager     // Catch:{ all -> 0x007f }
                    com.qualcomm.robotcore.util.SerialNumber r3 = r4     // Catch:{ all -> 0x007f }
                    com.qualcomm.robotcore.hardware.RobotCoreLynxUsbDevice r1 = r1.createLynxUsbDevice(r3, r0)     // Catch:{ all -> 0x007f }
                    com.qualcomm.robotcore.hardware.LynxModuleMetaList r3 = r1.discoverModules(r2)     // Catch:{ all -> 0x007d }
                    if (r1 == 0) goto L_0x0067
                    r1.close()     // Catch:{ RobotCoreException -> 0x0065 }
                    goto L_0x0067
                L_0x0065:
                    r1 = move-exception
                    goto L_0x008d
                L_0x0067:
                    java.lang.String r0 = "FtcConfigTag"
                    java.lang.String r1 = "...discovering lynx modules complete: %s"
                    java.lang.Object[] r2 = new java.lang.Object[r2]
                    if (r3 != 0) goto L_0x0072
                    java.lang.String r4 = "null"
                    goto L_0x0076
                L_0x0072:
                    java.lang.String r4 = r3.toString()
                L_0x0076:
                    r2[r5] = r4
                    com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r0, (java.lang.String) r1, (java.lang.Object[]) r2)
                    r0 = r3
                    goto L_0x00b4
                L_0x007d:
                    r3 = move-exception
                    goto L_0x0081
                L_0x007f:
                    r3 = move-exception
                    r1 = r0
                L_0x0081:
                    if (r1 == 0) goto L_0x0086
                    r1.close()     // Catch:{ RobotCoreException -> 0x008b, all -> 0x0087 }
                L_0x0086:
                    throw r3     // Catch:{ RobotCoreException -> 0x008b, all -> 0x0087 }
                L_0x0087:
                    r1 = move-exception
                    r3 = r0
                    r0 = r1
                    goto L_0x00b6
                L_0x008b:
                    r1 = move-exception
                    r3 = r0
                L_0x008d:
                    java.lang.String r4 = "FtcConfigTag"
                    java.lang.StringBuilder r6 = new java.lang.StringBuilder     // Catch:{ all -> 0x00b5 }
                    r6.<init>()     // Catch:{ all -> 0x00b5 }
                    java.lang.String r7 = "discovering lynx modules threw exception: "
                    r6.append(r7)     // Catch:{ all -> 0x00b5 }
                    java.lang.String r1 = r1.toString()     // Catch:{ all -> 0x00b5 }
                    r6.append(r1)     // Catch:{ all -> 0x00b5 }
                    java.lang.String r1 = r6.toString()     // Catch:{ all -> 0x00b5 }
                    com.qualcomm.robotcore.util.RobotLog.m48ee(r4, r1)     // Catch:{ all -> 0x00b5 }
                    java.lang.String r1 = "FtcConfigTag"
                    java.lang.String r3 = "...discovering lynx modules complete: %s"
                    java.lang.Object[] r2 = new java.lang.Object[r2]
                    java.lang.String r4 = "null"
                    r2[r5] = r4
                    com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r1, (java.lang.String) r3, (java.lang.Object[]) r2)
                L_0x00b4:
                    return r0
                L_0x00b5:
                    r0 = move-exception
                L_0x00b6:
                    java.lang.String r1 = "FtcConfigTag"
                    java.lang.String r4 = "...discovering lynx modules complete: %s"
                    java.lang.Object[] r2 = new java.lang.Object[r2]
                    if (r3 != 0) goto L_0x00c1
                    java.lang.String r3 = "null"
                    goto L_0x00c5
                L_0x00c1:
                    java.lang.String r3 = r3.toString()
                L_0x00c5:
                    r2[r5] = r3
                    com.qualcomm.robotcore.util.RobotLog.m61vv((java.lang.String) r1, (java.lang.String) r4, (java.lang.Object[]) r2)
                    throw r0
                */
                throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.configuration.USBScanManager.C05342.call():com.qualcomm.robotcore.hardware.LynxModuleMetaList");
            }
        });
    }

    public ThreadPool.SingletonResult<ScannedDevices> startDeviceScanIfNecessary() {
        return this.scanningSingleton.submit((int) msWaitDefault, new Callable<ScannedDevices>() {
            public ScannedDevices call() throws InterruptedException {
                String str;
                ScannedDevices scannedDevices;
                if (USBScanManager.this.isRemoteConfig) {
                    NextLock.Waiter nextWaiter = USBScanManager.this.scanResultsSequence.getNextWaiter();
                    RobotLog.m60vv("FtcConfigTag", "sending remote scan request...");
                    USBScanManager.this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_SCAN));
                    nextWaiter.awaitNext();
                    RobotLog.m60vv("FtcConfigTag", "...remote scan request completed.");
                    synchronized (USBScanManager.this.remoteScannedDevicesLock) {
                        scannedDevices = USBScanManager.this.remoteScannedDevices;
                    }
                    return scannedDevices;
                }
                RobotLog.m60vv("FtcConfigTag", "scanning USB bus...");
                try {
                    ScannedDevices scanForUsbDevices = USBScanManager.this.deviceManager.scanForUsbDevices();
                    Object[] objArr = new Object[1];
                    if (scanForUsbDevices == null) {
                        str = "null";
                    } else {
                        str = scanForUsbDevices.keySet().toString();
                    }
                    objArr[0] = str;
                    RobotLog.m61vv("FtcConfigTag", ".. scanning complete: %s", objArr);
                    return scanForUsbDevices;
                } catch (RobotCoreException e) {
                    RobotLog.m50ee("FtcConfigTag", (Throwable) e, "USB bus scan threw exception");
                    RobotLog.m61vv("FtcConfigTag", ".. scanning complete: %s", "null");
                    return null;
                } catch (Throwable th) {
                    RobotLog.m61vv("FtcConfigTag", ".. scanning complete: %s", "null");
                    throw th;
                }
            }
        });
    }

    public ScannedDevices awaitScannedDevices() throws InterruptedException {
        ScannedDevices await = this.scanningSingleton.await();
        if (await != null) {
            return await;
        }
        RobotLog.m60vv("FtcConfigTag", "USBScanManager.await() returning made-up scan result");
        return new ScannedDevices();
    }

    public LynxModuleMetaList awaitLynxModules(SerialNumber serialNumber) throws InterruptedException {
        LynxModuleMetaList await = getDiscoveryState(serialNumber).lynxDiscoverySingleton.await();
        if (await != null) {
            return await;
        }
        RobotLog.m60vv("FtcConfigTag", "USBScanManager.awaitLynxModules() returning made-up result");
        return new LynxModuleMetaList(serialNumber);
    }

    public String packageCommandResponse(ScannedDevices scannedDevices) {
        return scannedDevices.toSerializationString();
    }

    public String packageCommandResponse(LynxModuleMetaList lynxModuleMetaList) {
        return lynxModuleMetaList.toSerializationString();
    }

    public void handleCommandScanResponse(String str) throws RobotCoreException {
        RobotLog.m60vv("FtcConfigTag", "handleCommandScanResponse()...");
        ScannedDevices fromSerializationString = ScannedDevices.fromSerializationString(str);
        synchronized (this.remoteScannedDevicesLock) {
            this.remoteScannedDevices = fromSerializationString;
            this.scanResultsSequence.advanceNext();
        }
        RobotLog.m60vv("FtcConfigTag", "...handleCommandScanResponse()");
    }

    public void handleCommandDiscoverLynxModulesResponse(String str) throws RobotCoreException {
        RobotLog.m60vv("FtcConfigTag", "handleCommandDiscoverLynxModulesResponse()...");
        LynxModuleMetaList fromSerializationString = LynxModuleMetaList.fromSerializationString(str);
        LynxModuleDiscoveryState discoveryState = getDiscoveryState(fromSerializationString.serialNumber);
        synchronized (discoveryState.remoteLynxDiscoveryLock) {
            discoveryState.remoteLynxModules = fromSerializationString;
            discoveryState.lynxDiscoverySequence.advanceNext();
        }
        RobotLog.m60vv("FtcConfigTag", "...handleCommandDiscoverLynxModulesResponse()");
    }
}
