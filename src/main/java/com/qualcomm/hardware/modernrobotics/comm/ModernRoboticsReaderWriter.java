package com.qualcomm.hardware.modernrobotics.comm;

import com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram;
import com.qualcomm.robotcore.hardware.usb.RobotUsbDevice;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.concurrent.TimeUnit;
import org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow;
import org.firstinspires.ftc.robotcore.internal.system.Deadline;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbProtocolException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbTimeoutException;
import org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbTooManySequentialErrorsException;

public class ModernRoboticsReaderWriter {
    public static final String COMM_ERROR_READ = "comm error read";
    public static final String COMM_ERROR_WRITE = "comm error write";
    public static final String COMM_FAILURE_READ = "comm failure read";
    public static final String COMM_FAILURE_WRITE = "comm failure write";
    public static final String COMM_PAYLOAD_ERROR_READ = "comm payload error read";
    public static final String COMM_PAYLOAD_ERROR_WRITE = "comm payload error write";
    public static final String COMM_SYNC_LOST = "comm sync lost";
    public static final String COMM_TIMEOUT_READ = "comm timeout awaiting response (read)";
    public static final String COMM_TIMEOUT_WRITE = "comm timeout awaiting response (write)";
    public static final String COMM_TYPE_ERROR_READ = "comm type error read";
    public static final String COMM_TYPE_ERROR_WRITE = "comm type error write";
    public static boolean DEBUG = false;
    public static int MAX_SEQUENTIAL_USB_ERROR_COUNT = 5;
    public static int MS_COMM_ERROR_WAIT = 100;
    public static int MS_FAILURE_WAIT = 40;
    public static int MS_GARBAGE_COLLECTION_SPURT = 40;
    public static int MS_INTER_BYTE_TIMEOUT = 10;
    public static int MS_MAX_TIMEOUT = 100;
    public static int MS_REQUEST_RESPONSE_TIMEOUT = ((2 * 2) + 50);
    public static int MS_RESYNCH_TIMEOUT = 1000;
    public static int MS_USB_HUB_LATENCY = 2;
    public static final String TAG = "MRReaderWriter";
    protected final RobotUsbDevice device;
    protected boolean isSynchronized = false;
    protected int msUsbReadRetryInterval = 20;
    protected int msUsbWriteRetryInterval = 20;
    protected ModernRoboticsDatagram.AllocationContext<ModernRoboticsRequest> requestAllocationContext = new ModernRoboticsDatagram.AllocationContext<>();
    protected ModernRoboticsDatagram.AllocationContext<ModernRoboticsResponse> responseAllocationContext = new ModernRoboticsDatagram.AllocationContext<>();
    protected Deadline responseDeadline = new Deadline((long) MS_RESYNCH_TIMEOUT, TimeUnit.MILLISECONDS);
    protected int usbReadRetryCount = 4;
    protected int usbSequentialCommReadErrorCount = 0;
    protected int usbSequentialCommWriteErrorCount = 0;
    protected int usbWriteRetryCount = 4;

    public ModernRoboticsReaderWriter(RobotUsbDevice robotUsbDevice) {
        this.device = robotUsbDevice;
        robotUsbDevice.setDebugRetainBuffers(true);
    }

    public void throwIfTooManySequentialCommErrors() throws RobotUsbTooManySequentialErrorsException {
        if (this.device.isOpen()) {
            int i = this.usbSequentialCommReadErrorCount;
            int i2 = MAX_SEQUENTIAL_USB_ERROR_COUNT;
            if (i > i2 || this.usbSequentialCommWriteErrorCount > i2) {
                throw new RobotUsbTooManySequentialErrorsException("%s: too many sequential USB comm errors on device", this.device.getSerialNumber());
            }
        }
    }

    public void close() {
        this.device.close();
    }

    public void read(boolean z, int i, byte[] bArr, TimeWindow timeWindow) throws RobotUsbException, InterruptedException {
        if (DEBUG) {
            RobotLog.m61vv(TAG, "%s: read(addr=%d cb=%d)", this.device.getSerialNumber(), Integer.valueOf(i), Integer.valueOf(bArr.length));
        }
        RobotUsbException e = null;
        int i2 = 0;
        while (i2 < this.usbReadRetryCount) {
            if (i2 > 0) {
                RobotLog.m49ee(TAG, "%s: retry #%d read(addr=%d cb=%d)", this.device.getSerialNumber(), Integer.valueOf(i2), Integer.valueOf(i), Integer.valueOf(bArr.length));
            }
            try {
                readOnce(i, bArr, timeWindow);
                return;
            } catch (RobotUsbException e2) {
                e = e2;
                if (this.device.isOpen()) {
                    if (!z) {
                        RobotLog.m49ee(TAG, "%s: ignoring failed read(addr=%d cb=%d)", this.device.getSerialNumber(), Integer.valueOf(i), Integer.valueOf(bArr.length));
                        return;
                    }
                    Thread.sleep((long) this.msUsbReadRetryInterval);
                    this.device.resetAndFlushBuffers();
                    i2++;
                } else {
                    return;
                }
            }
        }
        if (e != null) {
            throw e;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v2, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v5, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:39:0x0090  */
    /* JADX WARNING: Removed duplicated region for block: B:41:0x0095  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void readOnce(int r6, byte[] r7, org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow r8) throws org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException, java.lang.InterruptedException {
        /*
            r5 = this;
            r0 = 0
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest> r1 = r5.requestAllocationContext     // Catch:{ RobotUsbException -> 0x0082, all -> 0x007f }
            r2 = 0
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest r1 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest.newInstance(r1, r2)     // Catch:{ RobotUsbException -> 0x0082, all -> 0x007f }
            r1.setRead(r2)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            r1.setAddress(r6)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            int r3 = r7.length     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            r1.setPayloadLength(r3)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r3 = r5.device     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            byte[] r4 = r1.data     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            r3.write(r4)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r0 = r5.readResponse(r1, r8)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            boolean r8 = r0.isFailure()     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            if (r8 == 0) goto L_0x002f
            int r6 = MS_FAILURE_WAIT     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            long r6 = (long) r6     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            java.lang.Thread.sleep(r6)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            java.lang.String r6 = "comm failure read"
            r5.logAndThrowProtocol(r1, r0, r6)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            goto L_0x006e
        L_0x002f:
            boolean r8 = r0.isRead()     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            if (r8 == 0) goto L_0x0052
            int r8 = r0.getFunction()     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            if (r8 != 0) goto L_0x0052
            int r8 = r0.getAddress()     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            if (r8 != r6) goto L_0x0052
            int r6 = r0.getPayloadLength()     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            int r8 = r7.length     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            if (r6 != r8) goto L_0x0052
            r5.usbSequentialCommReadErrorCount = r2     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            byte[] r6 = r0.data     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            r8 = 5
            int r3 = r7.length     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            java.lang.System.arraycopy(r6, r8, r7, r2, r3)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            goto L_0x006e
        L_0x0052:
            int r6 = MS_COMM_ERROR_WAIT     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            long r6 = (long) r6     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            java.lang.Thread.sleep(r6)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            java.lang.String r6 = "comm error read"
            r5.logAndThrowProtocol(r1, r0, r6)     // Catch:{ RobotUsbTimeoutException -> 0x005e }
            goto L_0x006e
        L_0x005e:
            r6 = move-exception
            int r7 = MS_COMM_ERROR_WAIT     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            long r7 = (long) r7     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            java.lang.Thread.sleep(r7)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            java.lang.String r7 = "comm timeout awaiting response (read)"
            java.lang.String r7 = r5.timeoutMessage(r7, r6)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
            r5.logAndRethrowTimeout(r6, r1, r7)     // Catch:{ RobotUsbException -> 0x007b, all -> 0x0079 }
        L_0x006e:
            if (r0 == 0) goto L_0x0073
            r0.close()
        L_0x0073:
            if (r1 == 0) goto L_0x0078
            r1.close()
        L_0x0078:
            return
        L_0x0079:
            r6 = move-exception
            goto L_0x008e
        L_0x007b:
            r6 = move-exception
            r7 = r0
            r0 = r1
            goto L_0x0084
        L_0x007f:
            r6 = move-exception
            r1 = r0
            goto L_0x008e
        L_0x0082:
            r6 = move-exception
            r7 = r0
        L_0x0084:
            int r8 = r5.usbSequentialCommReadErrorCount     // Catch:{ all -> 0x008b }
            int r8 = r8 + 1
            r5.usbSequentialCommReadErrorCount = r8     // Catch:{ all -> 0x008b }
            throw r6     // Catch:{ all -> 0x008b }
        L_0x008b:
            r6 = move-exception
            r1 = r0
            r0 = r7
        L_0x008e:
            if (r0 == 0) goto L_0x0093
            r0.close()
        L_0x0093:
            if (r1 == 0) goto L_0x0098
            r1.close()
        L_0x0098:
            throw r6
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsReaderWriter.readOnce(int, byte[], org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow):void");
    }

    public void write(int i, byte[] bArr) throws RobotUsbException, InterruptedException {
        if (DEBUG) {
            RobotLog.m61vv(TAG, "%s: write(addr=%d cb=%d)", this.device.getSerialNumber(), Integer.valueOf(i), Integer.valueOf(bArr.length));
        }
        RobotUsbException e = null;
        int i2 = 0;
        while (i2 < this.usbWriteRetryCount) {
            if (i2 > 0) {
                RobotLog.m49ee(TAG, "%s: retry #%d write(addr=%d cb=%d)", this.device.getSerialNumber(), Integer.valueOf(i2), Integer.valueOf(i), Integer.valueOf(bArr.length));
            }
            try {
                writeOnce(i, bArr);
                return;
            } catch (RobotUsbException e2) {
                e = e2;
                if (this.device.isOpen()) {
                    Thread.sleep((long) this.msUsbWriteRetryInterval);
                    this.device.resetAndFlushBuffers();
                    i2++;
                } else {
                    return;
                }
            }
        }
        if (e != null) {
            throw e;
        }
    }

    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v0, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v0, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v1, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v2, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v1, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v3, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v4, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v6, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v4, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v5, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r0v6, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse} */
    /* JADX DEBUG: Multi-variable search result rejected for TypeSearchVarInfo{r1v7, resolved type: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest} */
    /* access modifiers changed from: protected */
    /* JADX WARNING: Multi-variable type inference failed */
    /* JADX WARNING: Removed duplicated region for block: B:40:0x0088  */
    /* JADX WARNING: Removed duplicated region for block: B:42:0x008d  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public void writeOnce(int r5, byte[] r6) throws org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException, java.lang.InterruptedException {
        /*
            r4 = this;
            r0 = 0
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest> r1 = r4.requestAllocationContext     // Catch:{ RobotUsbException -> 0x007a, all -> 0x0077 }
            int r2 = r6.length     // Catch:{ RobotUsbException -> 0x007a, all -> 0x0077 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest r1 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest.newInstance(r1, r2)     // Catch:{ RobotUsbException -> 0x007a, all -> 0x0077 }
            r2 = 0
            r1.setWrite(r2)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            r1.setAddress(r5)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            r1.setPayload(r6)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r6 = r4.device     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            byte[] r3 = r1.data     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            r6.write(r3)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r0 = r4.readResponse(r1, r0)     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            boolean r6 = r0.isFailure()     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            if (r6 == 0) goto L_0x002f
            int r5 = MS_FAILURE_WAIT     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            long r5 = (long) r5     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            java.lang.Thread.sleep(r5)     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            java.lang.String r5 = "comm failure write"
            r4.logAndThrowProtocol(r1, r0, r5)     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            goto L_0x0066
        L_0x002f:
            boolean r6 = r0.isWrite()     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            if (r6 == 0) goto L_0x004a
            int r6 = r0.getFunction()     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            if (r6 != 0) goto L_0x004a
            int r6 = r0.getAddress()     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            if (r6 != r5) goto L_0x004a
            int r5 = r0.getPayloadLength()     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            if (r5 != 0) goto L_0x004a
            r4.usbSequentialCommWriteErrorCount = r2     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            goto L_0x0066
        L_0x004a:
            int r5 = MS_COMM_ERROR_WAIT     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            long r5 = (long) r5     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            java.lang.Thread.sleep(r5)     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            java.lang.String r5 = "comm error write"
            r4.logAndThrowProtocol(r1, r0, r5)     // Catch:{ RobotUsbTimeoutException -> 0x0056 }
            goto L_0x0066
        L_0x0056:
            r5 = move-exception
            int r6 = MS_COMM_ERROR_WAIT     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            long r2 = (long) r6     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            java.lang.Thread.sleep(r2)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            java.lang.String r6 = "comm timeout awaiting response (write)"
            java.lang.String r6 = r4.timeoutMessage(r6, r5)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
            r4.logAndRethrowTimeout(r5, r1, r6)     // Catch:{ RobotUsbException -> 0x0073, all -> 0x0071 }
        L_0x0066:
            if (r0 == 0) goto L_0x006b
            r0.close()
        L_0x006b:
            if (r1 == 0) goto L_0x0070
            r1.close()
        L_0x0070:
            return
        L_0x0071:
            r5 = move-exception
            goto L_0x0086
        L_0x0073:
            r5 = move-exception
            r6 = r0
            r0 = r1
            goto L_0x007c
        L_0x0077:
            r5 = move-exception
            r1 = r0
            goto L_0x0086
        L_0x007a:
            r5 = move-exception
            r6 = r0
        L_0x007c:
            int r1 = r4.usbSequentialCommWriteErrorCount     // Catch:{ all -> 0x0083 }
            int r1 = r1 + 1
            r4.usbSequentialCommWriteErrorCount = r1     // Catch:{ all -> 0x0083 }
            throw r5     // Catch:{ all -> 0x0083 }
        L_0x0083:
            r5 = move-exception
            r1 = r0
            r0 = r6
        L_0x0086:
            if (r0 == 0) goto L_0x008b
            r0.close()
        L_0x008b:
            if (r1 == 0) goto L_0x0090
            r1.close()
        L_0x0090:
            throw r5
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsReaderWriter.writeOnce(int, byte[]):void");
    }

    /* access modifiers changed from: protected */
    /* JADX WARNING: Removed duplicated region for block: B:44:0x00c5 A[Catch:{ all -> 0x0100 }] */
    /* JADX WARNING: Removed duplicated region for block: B:51:0x00fc  */
    /* Code decompiled incorrectly, please refer to instructions dump. */
    public com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse readResponse(com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest r14, org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow r15) throws org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbException, java.lang.InterruptedException {
        /*
            r13 = this;
            org.firstinspires.ftc.robotcore.internal.system.Deadline r0 = r13.responseDeadline
            r0.reset()
        L_0x0005:
            org.firstinspires.ftc.robotcore.internal.system.Deadline r0 = r13.responseDeadline
            boolean r0 = r0.hasExpired()
            r1 = 1
            r2 = 0
            if (r0 != 0) goto L_0x0107
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r0 = r13.device
            boolean r0 = r0.mightBeAtUsbPacketStart()
            if (r0 != 0) goto L_0x0019
            r13.isSynchronized = r2
        L_0x0019:
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse> r0 = r13.responseAllocationContext
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r0 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.newInstance(r0, r2)
            boolean r3 = r13.isSynchronized     // Catch:{ all -> 0x0100 }
            if (r3 != 0) goto L_0x006a
            byte[] r3 = new byte[r1]     // Catch:{ all -> 0x0100 }
            r4 = 3
            byte[] r12 = new byte[r4]     // Catch:{ all -> 0x0100 }
            com.qualcomm.robotcore.hardware.usb.RobotUsbDevice r5 = r13.device     // Catch:{ all -> 0x0100 }
            r5.skipToLikelyUsbPacketStart()     // Catch:{ all -> 0x0100 }
            int r5 = MS_REQUEST_RESPONSE_TIMEOUT     // Catch:{ all -> 0x0100 }
            java.lang.String r6 = "sync0"
            r7 = 0
            byte r5 = r13.readSingleByte(r3, r5, r7, r6)     // Catch:{ all -> 0x0100 }
            byte[] r6 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.syncBytes     // Catch:{ all -> 0x0100 }
            byte r6 = r6[r2]     // Catch:{ all -> 0x0100 }
            if (r5 == r6) goto L_0x0042
            if (r0 == 0) goto L_0x0005
        L_0x003e:
            r0.close()
            goto L_0x0005
        L_0x0042:
            java.lang.String r5 = "sync1"
            byte r3 = r13.readSingleByte(r3, r2, r7, r5)     // Catch:{ all -> 0x0100 }
            byte[] r5 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.syncBytes     // Catch:{ all -> 0x0100 }
            byte r5 = r5[r1]     // Catch:{ all -> 0x0100 }
            if (r3 == r5) goto L_0x0051
            if (r0 == 0) goto L_0x0005
            goto L_0x003e
        L_0x0051:
            r7 = 0
            r9 = 0
            r10 = 0
            java.lang.String r11 = "syncSuffix"
            r8 = 3
            r5 = r13
            r6 = r12
            r5.readIncomingBytes(r6, r7, r8, r9, r10, r11)     // Catch:{ all -> 0x0100 }
            byte[] r3 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.syncBytes     // Catch:{ all -> 0x0100 }
            byte[] r5 = r0.data     // Catch:{ all -> 0x0100 }
            r6 = 2
            java.lang.System.arraycopy(r3, r2, r5, r2, r6)     // Catch:{ all -> 0x0100 }
            byte[] r3 = r0.data     // Catch:{ all -> 0x0100 }
            java.lang.System.arraycopy(r12, r2, r3, r6, r4)     // Catch:{ all -> 0x0100 }
            goto L_0x0084
        L_0x006a:
            byte[] r4 = r0.data     // Catch:{ all -> 0x0100 }
            r5 = 0
            byte[] r3 = r0.data     // Catch:{ all -> 0x0100 }
            int r6 = r3.length     // Catch:{ all -> 0x0100 }
            int r7 = MS_REQUEST_RESPONSE_TIMEOUT     // Catch:{ all -> 0x0100 }
            r8 = 0
            java.lang.String r9 = "header"
            r3 = r13
            r3.readIncomingBytes(r4, r5, r6, r7, r8, r9)     // Catch:{ all -> 0x0100 }
            boolean r3 = r0.syncBytesValid()     // Catch:{ all -> 0x0100 }
            if (r3 != 0) goto L_0x0084
            java.lang.String r3 = "comm sync lost"
            r13.logAndThrowProtocol(r14, r0, r3)     // Catch:{ all -> 0x0100 }
        L_0x0084:
            boolean r3 = r0.isFailure()     // Catch:{ all -> 0x0100 }
            if (r3 != 0) goto L_0x00ac
            boolean r3 = r14.isRead()     // Catch:{ all -> 0x0100 }
            boolean r4 = r0.isRead()     // Catch:{ all -> 0x0100 }
            if (r3 != r4) goto L_0x009e
            int r3 = r14.getFunction()     // Catch:{ all -> 0x0100 }
            int r4 = r0.getFunction()     // Catch:{ all -> 0x0100 }
            if (r3 == r4) goto L_0x00ac
        L_0x009e:
            boolean r3 = r14.isWrite()     // Catch:{ all -> 0x0100 }
            if (r3 == 0) goto L_0x00a7
            java.lang.String r3 = "comm type error write"
            goto L_0x00a9
        L_0x00a7:
            java.lang.String r3 = "comm type error read"
        L_0x00a9:
            r13.logAndThrowProtocol(r14, r0, r3)     // Catch:{ all -> 0x0100 }
        L_0x00ac:
            boolean r3 = r0.isFailure()     // Catch:{ all -> 0x0100 }
            if (r3 == 0) goto L_0x00b4
        L_0x00b2:
            r3 = r2
            goto L_0x00bf
        L_0x00b4:
            boolean r3 = r14.isWrite()     // Catch:{ all -> 0x0100 }
            if (r3 == 0) goto L_0x00bb
            goto L_0x00b2
        L_0x00bb:
            int r3 = r14.getPayloadLength()     // Catch:{ all -> 0x0100 }
        L_0x00bf:
            int r4 = r0.getPayloadLength()     // Catch:{ all -> 0x0100 }
            if (r3 == r4) goto L_0x00d3
            boolean r3 = r14.isWrite()     // Catch:{ all -> 0x0100 }
            if (r3 == 0) goto L_0x00ce
            java.lang.String r3 = "comm payload error write"
            goto L_0x00d0
        L_0x00ce:
            java.lang.String r3 = "comm payload error read"
        L_0x00d0:
            r13.logAndThrowProtocol(r14, r0, r3)     // Catch:{ all -> 0x0100 }
        L_0x00d3:
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsDatagram$AllocationContext<com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse> r14 = r13.responseAllocationContext     // Catch:{ all -> 0x0100 }
            int r3 = r0.getPayloadLength()     // Catch:{ all -> 0x0100 }
            com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse r14 = com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse.newInstance(r14, r3)     // Catch:{ all -> 0x0100 }
            byte[] r3 = r0.data     // Catch:{ all -> 0x0100 }
            byte[] r4 = r14.data     // Catch:{ all -> 0x0100 }
            byte[] r5 = r0.data     // Catch:{ all -> 0x0100 }
            int r5 = r5.length     // Catch:{ all -> 0x0100 }
            java.lang.System.arraycopy(r3, r2, r4, r2, r5)     // Catch:{ all -> 0x0100 }
            byte[] r7 = r14.data     // Catch:{ all -> 0x0100 }
            byte[] r2 = r0.data     // Catch:{ all -> 0x0100 }
            int r8 = r2.length     // Catch:{ all -> 0x0100 }
            int r9 = r0.getPayloadLength()     // Catch:{ all -> 0x0100 }
            r10 = 0
            java.lang.String r12 = "payload"
            r6 = r13
            r11 = r15
            r6.readIncomingBytes(r7, r8, r9, r10, r11, r12)     // Catch:{ all -> 0x0100 }
            r13.isSynchronized = r1     // Catch:{ all -> 0x0100 }
            if (r0 == 0) goto L_0x00ff
            r0.close()
        L_0x00ff:
            return r14
        L_0x0100:
            r14 = move-exception
            if (r0 == 0) goto L_0x0106
            r0.close()
        L_0x0106:
            throw r14
        L_0x0107:
            org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbTimeoutException r14 = new org.firstinspires.ftc.robotcore.internal.usb.exception.RobotUsbTimeoutException
            org.firstinspires.ftc.robotcore.internal.system.Deadline r15 = r13.responseDeadline
            long r3 = r15.startTimeNanoseconds()
            java.lang.Object[] r15 = new java.lang.Object[r1]
            org.firstinspires.ftc.robotcore.internal.system.Deadline r0 = r13.responseDeadline
            java.util.concurrent.TimeUnit r1 = java.util.concurrent.TimeUnit.MILLISECONDS
            long r0 = r0.getDuration(r1)
            java.lang.Long r0 = java.lang.Long.valueOf(r0)
            r15[r2] = r0
            java.lang.String r0 = "timeout waiting %d ms for response"
            r14.<init>((long) r3, (java.lang.String) r0, (java.lang.Object[]) r15)
            throw r14
        */
        throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsReaderWriter.readResponse(com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsRequest, org.firstinspires.ftc.robotcore.internal.hardware.TimeWindow):com.qualcomm.hardware.modernrobotics.comm.ModernRoboticsResponse");
    }

    /* access modifiers changed from: protected */
    public void readIncomingBytes(byte[] bArr, int i, int i2, int i3, TimeWindow timeWindow, String str) throws RobotUsbException, InterruptedException {
        int i4 = i2;
        if (i4 > 0) {
            long min = Math.min((long) ((MS_INTER_BYTE_TIMEOUT * (i4 + 2)) + i3 + MS_GARBAGE_COLLECTION_SPURT), (long) MS_MAX_TIMEOUT);
            long nanoTime = System.nanoTime();
            int read = this.device.read(bArr, i, i2, min, timeWindow);
            if (read != i4) {
                if (read != 0) {
                    logAndThrowProtocol("readIncomingBytes(%s) cbToRead=%d cbRead=%d", str, Integer.valueOf(i2), Integer.valueOf(read));
                    return;
                }
                throw new RobotUsbTimeoutException(nanoTime, "%s: unable to read %d bytes in %d ms", str, Integer.valueOf(i2), Long.valueOf(min));
            }
        }
    }

    /* access modifiers changed from: protected */
    public byte readSingleByte(byte[] bArr, int i, TimeWindow timeWindow, String str) throws RobotUsbException, InterruptedException {
        readIncomingBytes(bArr, 0, 1, i, timeWindow, str);
        return bArr[0];
    }

    /* access modifiers changed from: protected */
    public String timeoutMessage(String str, RobotUsbTimeoutException robotUsbTimeoutException) {
        return String.format("%s: %s", new Object[]{str, robotUsbTimeoutException.getMessage()});
    }

    /* access modifiers changed from: protected */
    public void doExceptionBookkeeping() {
        this.isSynchronized = false;
    }

    /* access modifiers changed from: protected */
    public void logAndRethrowTimeout(RobotUsbTimeoutException robotUsbTimeoutException, ModernRoboticsRequest modernRoboticsRequest, String str) throws RobotUsbTimeoutException {
        ModernRoboticsRequest newInstance = ModernRoboticsRequest.newInstance(this.requestAllocationContext, 0);
        System.arraycopy(modernRoboticsRequest.data, 0, newInstance.data, 0, newInstance.data.length);
        RobotLog.m49ee(TAG, "%s: %s request=%s", this.device.getSerialNumber(), str, bufferToString(newInstance.data));
        this.device.logRetainedBuffers(robotUsbTimeoutException.nsTimerStart, robotUsbTimeoutException.nsTimerExpire, TAG, "recent data on %s", this.device.getSerialNumber());
        doExceptionBookkeeping();
        throw robotUsbTimeoutException;
    }

    /* access modifiers changed from: protected */
    public void logAndThrowProtocol(String str, Object... objArr) throws RobotUsbProtocolException {
        String format = String.format(str, objArr);
        RobotLog.m49ee(TAG, "%s: %s", this.device.getSerialNumber(), format);
        doExceptionBookkeeping();
        throw new RobotUsbProtocolException(format);
    }

    /* access modifiers changed from: protected */
    public void logAndThrowProtocol(ModernRoboticsRequest modernRoboticsRequest, ModernRoboticsResponse modernRoboticsResponse, String str) throws RobotUsbProtocolException {
        ModernRoboticsRequest newInstance = ModernRoboticsRequest.newInstance(this.requestAllocationContext, 0);
        System.arraycopy(modernRoboticsRequest.data, 0, newInstance.data, 0, newInstance.data.length);
        ModernRoboticsResponse newInstance2 = ModernRoboticsResponse.newInstance(this.responseAllocationContext, 0);
        System.arraycopy(modernRoboticsResponse.data, 0, newInstance2.data, 0, newInstance2.data.length);
        RobotLog.m49ee(TAG, "%s: %s: request:%s response:%s", this.device.getSerialNumber(), str, bufferToString(newInstance.data), bufferToString(newInstance2.data));
        doExceptionBookkeeping();
        throw new RobotUsbProtocolException(str);
    }

    protected static String bufferToString(byte[] bArr) {
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        if (bArr.length > 0) {
            sb.append(String.format("%02x", new Object[]{Byte.valueOf(bArr[0])}));
        }
        int min = Math.min(bArr.length, 16);
        for (int i = 1; i < min; i++) {
            sb.append(String.format(" %02x", new Object[]{Byte.valueOf(bArr[i])}));
        }
        if (min < bArr.length) {
            sb.append(" ...");
        }
        sb.append("]");
        return sb.toString();
    }
}
