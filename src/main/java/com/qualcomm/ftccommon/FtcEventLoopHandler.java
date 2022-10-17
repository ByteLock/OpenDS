package com.qualcomm.ftccommon;

import android.content.Context;
import com.qualcomm.ftccommon.configuration.USBScanManager;
import com.qualcomm.hardware.HardwareFactory;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.LynxUsbDeviceImpl;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.HardwareDeviceCloseOnTearDown;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.VoltageSensor;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationUtility;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.usb.RobotArmingStateNotifier;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.MovingStatistics;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.function.Supplier;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.inspection.InspectionState;

public class FtcEventLoopHandler implements BatteryChecker.BatteryWatcher {
    protected static final boolean DEBUG = false;
    public static final String NO_VOLTAGE_SENSOR = "$no$voltage$sensor$";
    public static final String TAG = "FtcEventLoopHandler";
    protected final UpdateUI.Callback callback;
    protected EventLoopManager eventLoopManager;
    protected int gamepadEffectsInterval = 50;
    protected ElapsedTime gamepadEffectsTimer = new ElapsedTime();
    protected final HardwareFactory hardwareFactory;
    protected HardwareMap hardwareMap = null;
    protected HardwareMap hardwareMapExtra = null;
    protected final Object refreshUserTelemetryLock = new Object();
    protected double robotBatteryInterval = 3.0d;
    protected double robotBatteryLoggingInterval = this.robotControllerBatteryCheckerInterval;
    protected ElapsedTime robotBatteryLoggingTimer = null;
    protected MovingStatistics robotBatteryStatistics = new MovingStatistics(10);
    protected ElapsedTime robotBatteryTimer = new ElapsedTime();
    protected BatteryChecker robotControllerBatteryChecker;
    protected double robotControllerBatteryCheckerInterval = 180.0d;
    protected final Context robotControllerContext;
    protected double updateUIInterval = 0.25d;
    protected ElapsedTime updateUITimer = new ElapsedTime();
    protected double userTelemetryInterval = 0.25d;
    protected ElapsedTime userTelemetryTimer = new ElapsedTime(0);

    public FtcEventLoopHandler(HardwareFactory hardwareFactory2, UpdateUI.Callback callback2, Context context) {
        this.hardwareFactory = hardwareFactory2;
        this.callback = callback2;
        this.robotControllerContext = context;
        this.robotControllerBatteryChecker = new BatteryChecker(this, (long) (this.robotControllerBatteryCheckerInterval * 1000.0d));
    }

    public void init(EventLoopManager eventLoopManager2) {
        this.eventLoopManager = eventLoopManager2;
        this.robotControllerBatteryChecker.startBatteryMonitoring();
    }

    public void close() {
        closeHardwareMap(this.hardwareMap);
        closeHardwareMap(this.hardwareMapExtra);
        closeBatteryMonitoring();
        this.eventLoopManager = null;
    }

    protected static void closeHardwareMap(HardwareMap hardwareMap2) {
        closeMotorControllers(hardwareMap2);
        closeServoControllers(hardwareMap2);
        closeAutoCloseOnTeardown(hardwareMap2);
    }

    public EventLoopManager getEventLoopManager() {
        return this.eventLoopManager;
    }

    public HardwareMap getHardwareMap() throws RobotCoreException, InterruptedException {
        HardwareMap hardwareMap2;
        synchronized (this.hardwareFactory) {
            if (this.hardwareMap == null) {
                this.hardwareMap = this.hardwareFactory.createHardwareMap(this.eventLoopManager);
                this.hardwareMapExtra = new HardwareMap(this.robotControllerContext);
            }
            hardwareMap2 = this.hardwareMap;
        }
        return hardwareMap2;
    }

    public List<LynxUsbDeviceImpl> getExtantLynxDeviceImpls() {
        ArrayList arrayList;
        synchronized (this.hardwareFactory) {
            arrayList = new ArrayList();
            HardwareMap hardwareMap2 = this.hardwareMap;
            if (hardwareMap2 != null) {
                for (LynxUsbDevice next : hardwareMap2.getAll(LynxUsbDevice.class)) {
                    if (next.getArmingState() == RobotArmingStateNotifier.ARMINGSTATE.ARMED) {
                        arrayList.add(next.getDelegationTarget());
                    }
                }
            }
            HardwareMap hardwareMap3 = this.hardwareMapExtra;
            if (hardwareMap3 != null) {
                for (LynxUsbDevice next2 : hardwareMap3.getAll(LynxUsbDevice.class)) {
                    if (next2.getArmingState() == RobotArmingStateNotifier.ARMINGSTATE.ARMED) {
                        arrayList.add(next2.getDelegationTarget());
                    }
                }
            }
        }
        return arrayList;
    }

    public <T> T getHardwareDevice(Class<? extends T> cls, SerialNumber serialNumber, Supplier<USBScanManager> supplier) {
        T t;
        boolean z;
        synchronized (this.hardwareFactory) {
            RobotLog.m61vv(TAG, "getHardwareDevice(%s)...", serialNumber);
            t = null;
            try {
                getHardwareMap();
                Object obj = this.hardwareMap.get(Object.class, serialNumber);
                if (obj == null) {
                    obj = this.hardwareMapExtra.get(Object.class, serialNumber);
                }
                if (obj == null) {
                    SerialNumber scannableDeviceSerialNumber = serialNumber.getScannableDeviceSerialNumber();
                    if (scannableDeviceSerialNumber.equals((Object) serialNumber) || (this.hardwareMap.get(Object.class, scannableDeviceSerialNumber) == null && this.hardwareMapExtra.get(Object.class, scannableDeviceSerialNumber) == null)) {
                        z = true;
                    } else {
                        RobotLog.m49ee(TAG, "internal error: %s absent but scannable %s present", serialNumber, scannableDeviceSerialNumber);
                        z = false;
                    }
                    if (z) {
                        USBScanManager uSBScanManager = supplier.get();
                        if (uSBScanManager != null) {
                            try {
                                ScannedDevices awaitScannedDevices = uSBScanManager.awaitScannedDevices();
                                if (awaitScannedDevices.containsKey(scannableDeviceSerialNumber)) {
                                    ControllerConfiguration buildNewControllerConfiguration = new ConfigurationUtility().buildNewControllerConfiguration(scannableDeviceSerialNumber, awaitScannedDevices.get(scannableDeviceSerialNumber), uSBScanManager.getLynxModuleMetaListSupplier(scannableDeviceSerialNumber));
                                    if (buildNewControllerConfiguration != null) {
                                        buildNewControllerConfiguration.setEnabled(true);
                                        buildNewControllerConfiguration.setKnownToBeAttached(true);
                                        this.hardwareFactory.instantiateConfiguration(this.hardwareMapExtra, buildNewControllerConfiguration, this.eventLoopManager);
                                        obj = this.hardwareMapExtra.get(Object.class, serialNumber);
                                        RobotLog.m55ii(TAG, "found %s: hardwareMapExtra:", serialNumber);
                                        this.hardwareMapExtra.logDevices();
                                    } else {
                                        RobotLog.m49ee(TAG, "buildNewControllerConfiguration(%s) failed", scannableDeviceSerialNumber);
                                    }
                                } else {
                                    RobotLog.m48ee(TAG, InspectionState.NO_VERSION);
                                }
                            } catch (InterruptedException unused) {
                                Thread.currentThread().interrupt();
                            } catch (RobotCoreException e) {
                                RobotLog.m51ee(TAG, e, "exception in getHardwareDevice(%s)", serialNumber);
                            }
                        } else {
                            RobotLog.m48ee(TAG, "usbScanManager supplied as null");
                        }
                    }
                }
                if (obj != null && cls.isInstance(obj)) {
                    t = cls.cast(obj);
                }
                RobotLog.m61vv(TAG, "...getHardwareDevice(%s)=%s,%s", serialNumber, obj, t);
            } catch (InterruptedException unused2) {
                Thread.currentThread().interrupt();
                return null;
            } catch (RobotCoreException unused3) {
                return null;
            }
        }
        return t;
    }

    public void displayGamePadInfo(String str) {
        if (this.updateUITimer.time() > this.updateUIInterval) {
            this.updateUITimer.reset();
            this.callback.updateUi(str, getGamepads());
        }
    }

    public Gamepad[] getGamepads() {
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        return eventLoopManager2 != null ? eventLoopManager2.getGamepads() : new Gamepad[2];
    }

    public void gamepadEffects() {
        if (this.gamepadEffectsTimer.milliseconds() > ((double) this.gamepadEffectsInterval)) {
            Gamepad[] gamepads = getGamepads();
            Gamepad.RumbleEffect poll = gamepads[0].rumbleQueue.poll();
            Gamepad.RumbleEffect poll2 = gamepads[1].rumbleQueue.poll();
            if (poll != null) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_RUMBLE_GAMEPAD, poll.serialize()));
            }
            if (poll2 != null) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_RUMBLE_GAMEPAD, poll2.serialize()));
            }
            Gamepad.LedEffect poll3 = gamepads[0].ledQueue.poll();
            Gamepad.LedEffect poll4 = gamepads[1].ledQueue.poll();
            if (poll3 != null) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_GAMEPAD_LED_EFFECT, poll3.serialize()));
            }
            if (poll4 != null) {
                NetworkConnectionHandler.getInstance().sendCommand(new Command(RobotCoreCommandList.CMD_GAMEPAD_LED_EFFECT, poll4.serialize()));
            }
            this.gamepadEffectsTimer.reset();
        }
    }

    public void refreshUserTelemetry(TelemetryMessage telemetryMessage, double d) {
        synchronized (this.refreshUserTelemetryLock) {
            if (Double.isNaN(d)) {
                d = this.userTelemetryInterval;
            }
            boolean z = true;
            boolean z2 = this.userTelemetryTimer.seconds() >= d;
            if (this.robotBatteryTimer.seconds() < this.robotBatteryInterval) {
                if (!z2 || this.robotBatteryStatistics.getMean() >= 2.0d) {
                    z = false;
                }
            }
            if (z2 || z) {
                if (z2) {
                    this.userTelemetryTimer.reset();
                }
                if (z) {
                    telemetryMessage.addData(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY, buildRobotBatteryMsg());
                    this.robotBatteryTimer.reset();
                }
                if (telemetryMessage.hasData()) {
                    EventLoopManager eventLoopManager2 = this.eventLoopManager;
                    if (eventLoopManager2 != null) {
                        eventLoopManager2.sendTelemetryData(telemetryMessage);
                    }
                    telemetryMessage.clearData();
                }
            }
        }
    }

    public void sendBatteryInfo() {
        this.robotControllerBatteryChecker.pollBatteryLevel(this);
        String buildRobotBatteryMsg = buildRobotBatteryMsg();
        if (buildRobotBatteryMsg != null) {
            sendTelemetry(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY, buildRobotBatteryMsg);
        }
    }

    private String buildRobotBatteryMsg() {
        HardwareMap hardwareMap2 = this.hardwareMap;
        if (hardwareMap2 == null) {
            return null;
        }
        Iterator<VoltageSensor> it = hardwareMap2.voltageSensor.iterator();
        double d = Double.POSITIVE_INFINITY;
        while (it.hasNext()) {
            long nanoTime = System.nanoTime();
            double voltage = it.next().getVoltage();
            long nanoTime2 = System.nanoTime();
            if (voltage >= 1.0d) {
                this.robotBatteryStatistics.add(((double) (nanoTime2 - nanoTime)) / 1000000.0d);
                if (voltage < d) {
                    d = voltage;
                }
            }
        }
        if (d == Double.POSITIVE_INFINITY) {
            return NO_VOLTAGE_SENSOR;
        }
        String num = Integer.toString((int) (d * 100.0d));
        return new StringBuilder(num).insert(num.length() - 2, ".").toString();
    }

    public void sendTelemetry(String str, String str2) {
        TelemetryMessage telemetryMessage = new TelemetryMessage();
        telemetryMessage.setTag(str);
        telemetryMessage.addData(str, str2);
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        if (eventLoopManager2 != null) {
            eventLoopManager2.sendTelemetryData(telemetryMessage);
        } else {
            RobotLog.m60vv(TAG, "sendTelemetry() with null EventLoopManager; ignored");
        }
        telemetryMessage.clearData();
    }

    protected static void closeMotorControllers(HardwareMap hardwareMap2) {
        if (hardwareMap2 != null) {
            for (DcMotorController close : hardwareMap2.getAll(DcMotorController.class)) {
                close.close();
            }
        }
    }

    protected static void closeServoControllers(HardwareMap hardwareMap2) {
        if (hardwareMap2 != null) {
            for (ServoController close : hardwareMap2.getAll(ServoController.class)) {
                close.close();
            }
        }
    }

    protected static void closeAutoCloseOnTeardown(HardwareMap hardwareMap2) {
        if (hardwareMap2 != null) {
            for (HardwareDeviceCloseOnTearDown close : hardwareMap2.getAll(HardwareDeviceCloseOnTearDown.class)) {
                close.close();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void closeBatteryMonitoring() {
        this.robotControllerBatteryChecker.close();
    }

    public void restartRobot() {
        RobotLog.m42dd(TAG, "restarting robot...");
        closeBatteryMonitoring();
        this.callback.restartRobot();
    }

    public String getOpMode(String str) {
        EventLoopManager eventLoopManager2 = this.eventLoopManager;
        return (eventLoopManager2 == null || eventLoopManager2.state != RobotState.RUNNING) ? "$Stop$Robot$" : str;
    }

    public void updateBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        sendTelemetry(EventLoopManager.RC_BATTERY_STATUS_KEY, batteryStatus.serialize());
    }
}
