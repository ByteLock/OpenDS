package com.qualcomm.ftcdriverstation;

import android.app.ActivityManager;
import android.app.AlertDialog;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.net.wifi.WifiManager;
import android.os.Build;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.preference.PreferenceManager;
import android.text.Html;
import android.view.ContextThemeWrapper;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.qualcomm.ftccommon.ClassManagerFactory;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.ftccommon.FtcAboutActivity;
import com.qualcomm.ftccommon.FtcEventLoopHandler;
import com.qualcomm.ftccommon.LaunchActivityConstantsList;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.ftccommon.StackTraceActivity;
import com.qualcomm.ftccommon.configuration.EditParameters;
import com.qualcomm.ftccommon.configuration.FtcLoadFileActivity;
import com.qualcomm.ftccommon.configuration.RobotConfigFile;
import com.qualcomm.ftccommon.configuration.RobotConfigFileManager;
import com.qualcomm.robotcore.eventloop.EventLoopManager;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.exception.RobotProtocolException;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.robocol.Heartbeat;
import com.qualcomm.robotcore.robocol.PeerDiscovery;
import com.qualcomm.robotcore.robocol.RobocolDatagram;
import com.qualcomm.robotcore.robocol.RobocolParsableBase;
import com.qualcomm.robotcore.robocol.TelemetryMessage;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.util.BatteryChecker;
import com.qualcomm.robotcore.util.Device;
import com.qualcomm.robotcore.util.ElapsedTime;
import com.qualcomm.robotcore.util.ImmersiveMode;
import com.qualcomm.robotcore.util.Range;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.RollingAverage;
import com.qualcomm.robotcore.wifi.DriverStationAccessPointAssistant;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import com.qualcomm.robotcore.wifi.NetworkType;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.TreeSet;
import org.firstinspires.directgamepadaccess.android.AndroidGamepadManager;
import org.firstinspires.directgamepadaccess.internal.KnownGamepads;
import org.firstinspires.ftc.driverstation.internal.StopWatchDrawable;
import org.firstinspires.ftc.ftccommon.external.SoundPlayingRobotMonitor;
import org.firstinspires.ftc.ftccommon.internal.ProgramAndManageActivity;
import org.firstinspires.ftc.robotcore.external.Predicate;
import org.firstinspires.ftc.robotcore.external.Telemetry;
import org.firstinspires.ftc.robotcore.external.android.AndroidTextToSpeech;
import org.firstinspires.ftc.robotcore.external.function.Consumer;
import org.firstinspires.ftc.robotcore.external.stream.CameraStreamClient;
import org.firstinspires.ftc.robotcore.network.CallbackResult;
import org.firstinspires.ftc.robotcore.network.DeviceNameListener;
import org.firstinspires.ftc.robotcore.network.DeviceNameManager;
import org.firstinspires.ftc.robotcore.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.network.PeerStatusCallback;
import org.firstinspires.ftc.robotcore.network.PreferenceRemoterDS;
import org.firstinspires.ftc.robotcore.network.RecvLoopRunnable;
import org.firstinspires.ftc.robotcore.network.RobotCoreCommandList;
import org.firstinspires.ftc.robotcore.network.StartResult;
import org.firstinspires.ftc.robotcore.network.WifiMuteEvent;
import org.firstinspires.ftc.robotcore.network.WifiMuteStateMachine;
import org.firstinspires.ftc.robotcore.internal.opmode.OpModeMeta;
import org.firstinspires.ftc.robotcore.ui.FilledPolygonDrawable;
import org.firstinspires.ftc.robotcore.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.ui.ProgressParameters;
import org.firstinspires.ftc.robotcore.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.PreferencesHelper;
import org.firstinspires.inspection.InspectionState;

public class FtcDriverStationActivity extends ThemedActivity implements NetworkConnection.NetworkConnectionCallback, RecvLoopRunnable.RecvLoopCallback, SharedPreferences.OnSharedPreferenceChangeListener, OpModeSelectionDialogFragment.OpModeSelectionDialogListener, BatteryChecker.BatteryWatcher, PeerStatusCallback, WifiMuteStateMachine.Callback, DriverStationAccessPointAssistant.ConnectedNetworkHealthListener {
    protected static final float FULLY_OPAQUE = 1.0f;
    private static final String LIFECYCLE_TAG = "Lifecycle ";
    protected static final int MATCH_NUMBER_LOWER_BOUND = 0;
    protected static final int MATCH_NUMBER_UPPER_BOUND = 1000;
    protected static final float PARTLY_OPAQUE = 0.3f;
    public static final String TAG = "DriverStation";
    protected static final boolean debugBattery = false;
    protected static DriverStationGamepadManager gamepadManager = null;
    private static final Set<Integer> instanceSet = Collections.synchronizedSet(new TreeSet());
    protected static boolean permissionsValidated = false;
    public static boolean usingUserspaceDriver;
    protected double V12BatteryMin;
    protected String V12BatteryMinString;
    protected TextView activeConfigText;
    protected AlertDialog alertDialogConnectedAsGroupOwner;
    protected AlertDialog alertDialogWifiDirectNameNonPrintableChars;
    protected AlertDialog alertDialogWifiDirectWrongDevice;
    private final AndroidTextToSpeech androidTextToSpeech;
    protected boolean annotationAutomagicPreselect;
    protected AppUtil appUtil;
    protected BatteryChecker batteryChecker;
    protected View batteryInfo;
    protected View btnGuiPreselectTele;
    protected Button buttonAutonomous;
    protected View buttonInit;
    protected View buttonInitStop;
    protected ImageButton buttonMenu;
    protected View buttonStart;
    protected ImageButton buttonStop;
    protected Button buttonTeleOp;
    protected ImageView cameraStreamImageView;
    protected LinearLayout cameraStreamLayout;
    protected boolean cameraStreamOpen;
    protected View chooseOpModePrompt;
    protected boolean clientConnected;
    View configAndTimerRegion;
    protected String connectionOwner;
    protected String connectionOwnerPassword;
    protected Context context;
    protected View controlPanelBack;
    protected TextView currentOpModeName;
    protected boolean debugLogging;
    protected final OpModeMeta defaultOpMode;
    protected boolean defaultOpModeRunning;
    protected DeviceNameManagerCallback deviceNameManagerCallback;
    protected StartResult deviceNameManagerStartResult;
    protected boolean disconnectFromPeerOnActivityStop;
    View dividerRcBatt12vBatt;
    protected ImageView dsBatteryIcon;
    protected TextView dsBatteryInfo;
    protected boolean f310InDinputModeDialogShowing;
    protected Map<GamepadUser, GamepadIndicator> gamepadIndicators = new HashMap();
    protected OpModeMeta guiPreselectedTeleop;
    ImageView headerColorLeft;
    LinearLayout headerColorRight;
    protected Heartbeat heartbeatRecv = new Heartbeat();
    protected ImmersiveMode immersion;
    private final int instanceId;
    private final String instanceIdStr;
    protected boolean landscape;
    protected ElapsedTime lastUiUpdate;
    protected View layoutGuiTelePreselection;
    LinearLayout layoutPingChan;
    View matchLoggingContainer;
    protected EditText matchNumField;
    TextView matchNumTxtView;
    protected NetworkConnectionHandler networkConnectionHandler;
    ImageView networkSignalLevel;
    TextView network_ssid;
    protected OpModeCountDownTimer opModeCountDown;
    protected boolean opModeUseTimer;
    protected List<OpModeMeta> opModes;
    protected PackageManager packageManager;
    protected boolean pendingStopFromTimer;
    protected RollingAverage pingAverage;
    PracticeTimerManager practiceTimerManager;
    protected StartResult prefRemoterStartResult;
    protected SharedPreferences preferences;
    protected PreferencesHelper preferencesHelper;
    protected boolean processUserActivity;
    protected OpModeMeta queuedOpMode;
    protected OpModeMeta queuedOpModeWhenMuted;
    protected View rcBatteryContainer;
    protected ImageView rcBatteryIcon;
    protected TextView rcBatteryTelemetry;
    protected boolean rcHasIndependentBattery;
    protected TextView rcObsoleteWarning;
    protected volatile boolean receivedPeerDiscoveryFromCurrentPeer;
    protected boolean replugGamepadDialogShowing;
    protected boolean requestingPermissionToWriteToSystemSettings;
    protected TextView robotBatteryMinimum;
    protected TextView robotBatteryTelemetry;
    protected RobotConfigFileManager robotConfigFileManager;
    protected RobotState robotState;
    protected final Queue<Runnable> runOnResumeQueue;
    protected boolean stopRequestedOnDsSide;
    protected boolean suppressLedCommands;
    protected boolean suppressRumbleCommands;
    protected TextView systemTelemetry;
    protected int systemTelemetryOriginalColor;
    protected Telemetry.DisplayFormat telemetryMode;
    protected View telemetryRegion;
    protected TextView textBytesPerSecond;
    TextView textDbmLink;
    protected TextView textDeviceName;
    protected TextView textDsUiStateIndicator;
    protected TextView textPingStatus;
    protected TextView textTelemetry;
    protected TextView textWifiChannel;
    protected TextView textWifiDirectStatus;
    protected boolean textWifiDirectStatusShowingRC;
    protected View timerAndTimerSwitch;
    protected TextView txtGuiPreselectedTele;
    protected UIState uiState;
    protected Thread uiThread;
    protected BroadcastReceiver usbInsertionReceiver;
    protected UsbManager usbManager;
    protected Utility utility;
    protected boolean waitingForResumeAfterOnActivityResultCalled;
    WiFiStatsView wiFiStatsView;
    protected View wifiInfo;
    protected WifiMuteStateMachine wifiMuteStateMachine;

    protected enum ControlPanelBack {
        NO_CHANGE,
        DIM,
        BRIGHT
    }

    enum WiFiStatsView {
        PING_CHAN,
        DBM_LINK
    }

    public String getTag() {
        return TAG;
    }

    public FtcDriverStationActivity() {
        OpModeMeta build = new OpModeMeta.Builder().setName("$Stop$Robot$").build();
        this.defaultOpMode = build;
        this.queuedOpMode = build;
        this.queuedOpModeWhenMuted = build;
        this.opModes = new LinkedList();
        this.opModeUseTimer = false;
        this.pingAverage = new RollingAverage(10);
        this.lastUiUpdate = new ElapsedTime();
        this.uiState = UIState.UNKNOWN;
        this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
        this.debugLogging = false;
        this.networkConnectionHandler = NetworkConnectionHandler.getInstance();
        this.appUtil = AppUtil.getInstance();
        this.deviceNameManagerStartResult = new StartResult();
        this.prefRemoterStartResult = new StartResult();
        this.deviceNameManagerCallback = new DeviceNameManagerCallback();
        this.processUserActivity = false;
        this.disconnectFromPeerOnActivityStop = true;
        this.guiPreselectedTeleop = null;
        this.defaultOpModeRunning = false;
        this.suppressRumbleCommands = true;
        this.suppressLedCommands = true;
        this.pendingStopFromTimer = false;
        this.stopRequestedOnDsSide = false;
        this.receivedPeerDiscoveryFromCurrentPeer = false;
        this.waitingForResumeAfterOnActivityResultCalled = false;
        this.runOnResumeQueue = new ArrayDeque();
        this.requestingPermissionToWriteToSystemSettings = false;
        this.androidTextToSpeech = new AndroidTextToSpeech();
        this.landscape = false;
        this.wiFiStatsView = WiFiStatsView.PING_CHAN;
        int identityHashCode = System.identityHashCode(this);
        this.instanceId = identityHashCode;
        instanceSet.add(Integer.valueOf(identityHashCode));
        this.instanceIdStr = Integer.toHexString(identityHashCode);
    }

    /* access modifiers changed from: protected */
    public void setBatteryIcon(final BatteryChecker.BatteryStatus batteryStatus, final ImageView imageView) {
        runOnUiThread(new Runnable() {
            public void run() {
                if (batteryStatus.percent <= 15.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? C0648R.C0649drawable.icon_battery0_charging : C0648R.C0649drawable.icon_battery0);
                    imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.phoneBatteryCritical), PorterDuff.Mode.MULTIPLY);
                } else if (batteryStatus.percent > 15.0d && batteryStatus.percent <= 45.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? C0648R.C0649drawable.icon_battery25_charging : C0648R.C0649drawable.icon_battery25);
                    if (batteryStatus.percent <= 30.0d) {
                        imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.phoneBatteryLow), PorterDuff.Mode.MULTIPLY);
                    } else {
                        imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.text_white), PorterDuff.Mode.MULTIPLY);
                    }
                } else if (batteryStatus.percent > 45.0d && batteryStatus.percent <= 65.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? C0648R.C0649drawable.icon_battery50_charging : C0648R.C0649drawable.icon_battery50);
                    imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.text_white), PorterDuff.Mode.MULTIPLY);
                } else if (batteryStatus.percent <= 65.0d || batteryStatus.percent > 85.0d) {
                    imageView.setImageResource(batteryStatus.isCharging ? C0648R.C0649drawable.icon_battery100_charging : C0648R.C0649drawable.icon_battery100);
                    imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.text_white), PorterDuff.Mode.MULTIPLY);
                } else {
                    imageView.setImageResource(batteryStatus.isCharging ? C0648R.C0649drawable.icon_battery75_charging : C0648R.C0649drawable.icon_battery75);
                    imageView.setColorFilter(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.text_white), PorterDuff.Mode.MULTIPLY);
                }
            }
        });
    }

    public void updateBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        if (this.landscape) {
            TextView textView = this.dsBatteryInfo;
            setTextView(textView, "DS: " + Math.round(batteryStatus.percent) + "%");
        } else {
            TextView textView2 = this.dsBatteryInfo;
            setTextView(textView2, Double.toString(batteryStatus.percent) + "%");
        }
        setBatteryIcon(batteryStatus, this.dsBatteryIcon);
    }

    /* access modifiers changed from: protected */
    public void updateRcBatteryStatus(BatteryChecker.BatteryStatus batteryStatus) {
        if (this.landscape) {
            TextView textView = this.rcBatteryTelemetry;
            setTextView(textView, "RC: " + Math.round(batteryStatus.percent) + "%");
        } else {
            TextView textView2 = this.rcBatteryTelemetry;
            setTextView(textView2, Double.toString(batteryStatus.percent) + "%");
        }
        setBatteryIcon(batteryStatus, this.rcBatteryIcon);
    }

    /* access modifiers changed from: protected */
    public void displayRcBattery(boolean z) {
        int i = 0;
        this.rcBatteryContainer.setVisibility(z ? 0 : 8);
        if (this.landscape) {
            View view = this.dividerRcBatt12vBatt;
            if (!z) {
                i = 8;
            }
            view.setVisibility(i);
        }
    }

    private void checkRcIndependentBattery(SharedPreferences sharedPreferences) {
        this.rcHasIndependentBattery = sharedPreferences.getBoolean(getString(C0648R.string.pref_has_independent_phone_battery_rc), true);
    }

    private void updateRcBatteryIndependence(SharedPreferences sharedPreferences, boolean z) {
        checkRcIndependentBattery(sharedPreferences);
        RobotLog.m61vv(TAG, "updateRcBatteryIndependence(%s)", Boolean.valueOf(this.rcHasIndependentBattery));
        if (z) {
            displayRcBattery(this.rcHasIndependentBattery);
        }
    }

    private void updateRcBatteryIndependence(SharedPreferences sharedPreferences) {
        updateRcBatteryIndependence(sharedPreferences, true);
    }

    public void onWifiOn() {
        this.queuedOpMode = this.queuedOpModeWhenMuted;
        this.processUserActivity = true;
        RobotLog.m54ii(TAG, "Wi-Fi On: " + this.queuedOpMode.name);
    }

    public void onWifiOff() {
        this.queuedOpModeWhenMuted = this.queuedOpMode;
        RobotLog.m54ii(TAG, "Wi-Fi Off: " + this.queuedOpMode.name);
    }

    public void onPendingOn() {
        this.processUserActivity = false;
        RobotLog.m54ii(TAG, "Pending Wi-Fi Off: " + this.queuedOpMode.name);
    }

    public void onPendingCancel() {
        this.processUserActivity = true;
        RobotLog.m54ii(TAG, "Pending Wi-Fi Cancel: " + this.queuedOpMode.name);
    }

    private class OpModeCountDownTimer {
        public static final long MS_COUNTDOWN_INTERVAL = 30000;
        public static final long MS_PER_S = 1000;
        public static final long MS_TICK = 1000;
        public static final long TICK_INTERVAL = 1;
        /* access modifiers changed from: private */
        public CountDownTimer countDownTimer = null;
        private boolean enabled = false;
        /* access modifiers changed from: private */
        public long msRemaining = MS_COUNTDOWN_INTERVAL;
        private View timerStopWatch;
        private View timerSwitchOff;
        private View timerSwitchOn;
        private TextView timerText;

        public OpModeCountDownTimer() {
            this.timerStopWatch = FtcDriverStationActivity.this.findViewById(C0648R.C0650id.timerStopWatch);
            this.timerText = (TextView) FtcDriverStationActivity.this.findViewById(C0648R.C0650id.timerText);
            this.timerSwitchOn = FtcDriverStationActivity.this.findViewById(C0648R.C0650id.timerSwitchOn);
            this.timerSwitchOff = FtcDriverStationActivity.this.findViewById(C0648R.C0650id.timerSwitchOff);
        }

        private void displaySecondsRemaining(long j) {
            if (this.enabled) {
                FtcDriverStationActivity.this.setTextView(this.timerText, String.valueOf(j));
            }
        }

        public void enable() {
            if (!this.enabled) {
                FtcDriverStationActivity.this.setVisibility(this.timerText, 0);
                FtcDriverStationActivity.this.setVisibility(this.timerStopWatch, 8);
                FtcDriverStationActivity.this.setVisibility(this.timerSwitchOn, 0);
                FtcDriverStationActivity.this.setVisibility(this.timerSwitchOff, 8);
                this.enabled = true;
                displaySecondsRemaining(getSecondsRemaining());
            }
        }

        public void disable() {
            FtcDriverStationActivity.this.setTextView(this.timerText, InspectionState.NO_VERSION);
            FtcDriverStationActivity.this.setVisibility(this.timerText, 8);
            FtcDriverStationActivity.this.setVisibility(this.timerStopWatch, 0);
            FtcDriverStationActivity.this.setVisibility(this.timerSwitchOn, 8);
            FtcDriverStationActivity.this.setVisibility(this.timerSwitchOff, 0);
            this.enabled = false;
        }

        public boolean isEnabled() {
            return this.enabled;
        }

        public void start() {
            if (this.enabled) {
                RobotLog.m60vv(FtcDriverStationActivity.TAG, "Starting to run current op mode for " + getSecondsRemaining() + " seconds");
                FtcDriverStationActivity.this.appUtil.synchronousRunOnUiThread(new Runnable() {
                    public void run() {
                        CountDownTimer access$000 = OpModeCountDownTimer.this.countDownTimer;
                        if (access$000 != null) {
                            access$000.cancel();
                        }
                        CountDownTimer unused = OpModeCountDownTimer.this.countDownTimer = new CountDownTimer(OpModeCountDownTimer.this.msRemaining, 1000) {
                            public void onTick(long j) {
                                FtcDriverStationActivity.this.assertUiThread();
                                OpModeCountDownTimer.this.setMsRemaining(j);
                                RobotLog.m60vv(FtcDriverStationActivity.TAG, "Running current op mode for " + (j / 1000) + " seconds");
                            }

                            public void onFinish() {
                                FtcDriverStationActivity.this.assertUiThread();
                                RobotLog.m60vv(FtcDriverStationActivity.TAG, "Stopping current op mode, timer expired");
                                OpModeCountDownTimer.this.resetCountdown();
                                FtcDriverStationActivity.this.handleOpModeStop(true);
                            }
                        }.start();
                    }
                });
            }
        }

        public void stop() {
            FtcDriverStationActivity.this.appUtil.synchronousRunOnUiThread(new Runnable() {
                public void run() {
                    if (OpModeCountDownTimer.this.countDownTimer != null) {
                        OpModeCountDownTimer.this.countDownTimer.cancel();
                        CountDownTimer unused = OpModeCountDownTimer.this.countDownTimer = null;
                    }
                }
            });
        }

        public void stopPreservingRemainingTime() {
            CountDownTimer countDownTimer2 = this.countDownTimer;
            long j = this.msRemaining;
            if (countDownTimer2 != null) {
                synchronized (countDownTimer2) {
                    j = this.msRemaining;
                }
            }
            stop();
            setMsRemaining(j);
        }

        public long getSecondsRemaining() {
            return this.msRemaining / 1000;
        }

        public void resetCountdown() {
            setMsRemaining(MS_COUNTDOWN_INTERVAL);
        }

        public void setMsRemaining(long j) {
            this.msRemaining = j;
            if (this.enabled) {
                displaySecondsRemaining(j / 1000);
            }
        }
    }

    protected enum UIState {
        UNKNOWN("U"),
        CANT_CONTINUE("E"),
        DISCONNECTED("X"),
        CONNNECTED("C"),
        WAITING_FOR_OPMODE_SELECTION("M"),
        WAITING_FOR_INIT_EVENT("K"),
        WAITING_FOR_ACK("KW"),
        WAITING_FOR_START_EVENT("S"),
        WAITING_FOR_STOP_EVENT("P"),
        ROBOT_STOPPED("Z");
        
        public final String indicator;

        private UIState(String str) {
            this.indicator = str;
        }
    }

    /* access modifiers changed from: protected */
    public boolean enforcePermissionValidator() {
        if (!permissionsValidated) {
            RobotLog.m60vv(TAG, "Redirecting to permission validator");
            startActivity(new Intent(AppUtil.getDefContext(), PermissionValidatorWrapper.class));
            return true;
        }
        RobotLog.m60vv(TAG, "Permissions validated already");
        return false;
    }

    public static void setPermissionsValidated() {
        permissionsValidated = true;
    }

    public View getPopupMenuAnchor() {
        if (!this.landscape) {
            return this.buttonMenu;
        }
        if (Device.isRevDriverHub()) {
            return this.buttonMenu;
        }
        return this.wifiInfo;
    }

    private void enableUserspaceUsbDriver() {
        PackageManager packageManager2 = this.packageManager;
        String packageName = getPackageName();
        packageManager2.setComponentEnabledSetting(new ComponentName(packageName, getPackageName() + ".DriverStationUserspaceAlias"), 1, 1);
        PackageManager packageManager3 = this.packageManager;
        String packageName2 = getPackageName();
        packageManager3.setComponentEnabledSetting(new ComponentName(packageName2, getPackageName() + ".DriverStationLegacyAlias"), 2, 1);
    }

    private void disableUserspaceUsbDriver() {
        PackageManager packageManager2 = this.packageManager;
        String packageName = getPackageName();
        packageManager2.setComponentEnabledSetting(new ComponentName(packageName, getPackageName() + ".DriverStationUserspaceAlias"), 2, 1);
        PackageManager packageManager3 = this.packageManager;
        String packageName2 = getPackageName();
        packageManager3.setComponentEnabledSetting(new ComponentName(packageName2, getPackageName() + ".DriverStationLegacyAlias"), 1, 1);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        RobotLog.m54ii(TAG, "Lifecycle onCreate() : " + this.instanceIdStr);
        super.onCreate(bundle);
        if (enforcePermissionValidator()) {
            finish();
            return;
        }
        this.preferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.packageManager = getApplicationContext().getPackageManager();
        this.landscape = this.preferences.getString(getResources().getString(C0648R.string.key_ds_layout), getResources().getString(C0648R.string.ds_ui_portrait)).equals(getResources().getString(C0648R.string.ds_ui_land));
        boolean z = this.preferences.getBoolean(getResources().getString(C0648R.string.pref_key_advanced_gamepad_features), true);
        usingUserspaceDriver = z;
        if (z) {
            enableUserspaceUsbDriver();
        } else {
            disableUserspaceUsbDriver();
        }
        this.uiThread = Thread.currentThread();
        if (this.landscape) {
            setRequestedOrientation(11);
            setContentView(C0648R.layout.activity_ds_land_main);
            this.headerColorLeft = (ImageView) findViewById(C0648R.C0650id.headerColorLeft);
            this.headerColorRight = (LinearLayout) findViewById(C0648R.C0650id.headerColorRight);
            this.configAndTimerRegion = findViewById(C0648R.C0650id.configAndTimerRegion);
            this.practiceTimerManager = new PracticeTimerManager(this, (ImageView) findViewById(C0648R.C0650id.practiceTimerStartStopBtn), (TextView) findViewById(C0648R.C0650id.practiceTimerTimeView));
            this.matchLoggingContainer = findViewById(C0648R.C0650id.matchNumContainer);
            this.matchNumTxtView = (TextView) findViewById(C0648R.C0650id.matchNumTextField);
            this.networkSignalLevel = (ImageView) findViewById(C0648R.C0650id.networkSignalLevel);
            this.layoutPingChan = (LinearLayout) findViewById(C0648R.C0650id.layoutPingChan);
            this.textDbmLink = (TextView) findViewById(C0648R.C0650id.textDbmLink);
            this.network_ssid = (TextView) findViewById(C0648R.C0650id.network_ssid);
            this.dividerRcBatt12vBatt = findViewById(C0648R.C0650id.dividerRcBatt12vBatt);
        } else {
            setRequestedOrientation(12);
            setContentView(C0648R.layout.activity_ftc_driver_station);
            this.matchNumField = (EditText) findViewById(C0648R.C0650id.matchNumTextField);
        }
        this.context = this;
        this.utility = new Utility(this);
        this.opModeCountDown = new OpModeCountDownTimer();
        this.rcHasIndependentBattery = false;
        PreferenceManager.setDefaultValues(this, C0648R.xml.app_settings, false);
        this.preferencesHelper = new PreferencesHelper(TAG, this.preferences);
        DeviceNameManagerFactory.getInstance().start(this.deviceNameManagerStartResult);
        PreferenceRemoterDS.getInstance().start(this.prefRemoterStartResult);
        NetworkConnectionHandler.getInstance().registerPeerStatusCallback(this);
        setClientConnected(false);
        if (permissionsValidated) {
            RobotLog.m54ii(TAG, "Processing all classes through class filter");
            ClassManagerFactory.registerResourceFilters();
            ClassManagerFactory.processAllClasses();
        }
        this.robotConfigFileManager = new RobotConfigFileManager(this);
        this.textDeviceName = (TextView) findViewById(C0648R.C0650id.textDeviceName);
        this.textDsUiStateIndicator = (TextView) findViewById(C0648R.C0650id.textDsUiStateIndicator);
        this.textWifiDirectStatus = (TextView) findViewById(C0648R.C0650id.textWifiDirectStatus);
        this.textWifiDirectStatusShowingRC = false;
        this.textWifiChannel = (TextView) findViewById(C0648R.C0650id.wifiChannel);
        this.textPingStatus = (TextView) findViewById(C0648R.C0650id.textPingStatus);
        this.textBytesPerSecond = (TextView) findViewById(C0648R.C0650id.bps);
        this.telemetryRegion = findViewById(C0648R.C0650id.telemetryRegion);
        this.textTelemetry = (TextView) findViewById(C0648R.C0650id.textTelemetry);
        TextView textView = (TextView) findViewById(C0648R.C0650id.textSystemTelemetry);
        this.systemTelemetry = textView;
        this.systemTelemetryOriginalColor = textView.getCurrentTextColor();
        this.rcObsoleteWarning = (TextView) findViewById(C0648R.C0650id.rcObsoleteWarning);
        this.rcBatteryContainer = findViewById(C0648R.C0650id.rcBatteryContainer);
        this.rcBatteryTelemetry = (TextView) findViewById(C0648R.C0650id.rcBatteryTelemetry);
        this.robotBatteryMinimum = (TextView) findViewById(C0648R.C0650id.robotBatteryMinimum);
        this.rcBatteryIcon = (ImageView) findViewById(C0648R.C0650id.rc_battery_icon);
        this.dsBatteryInfo = (TextView) findViewById(C0648R.C0650id.dsBatteryInfo);
        this.robotBatteryTelemetry = (TextView) findViewById(C0648R.C0650id.robotBatteryTelemetry);
        this.dsBatteryIcon = (ImageView) findViewById(C0648R.C0650id.DS_battery_icon);
        this.immersion = new ImmersiveMode(getWindow().getDecorView());
        doMatchNumFieldBehaviorInit();
        LinearLayout linearLayout = (LinearLayout) findViewById(C0648R.C0650id.cameraStreamLayout);
        this.cameraStreamLayout = linearLayout;
        linearLayout.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivity.this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_FRAME));
            }
        });
        this.cameraStreamImageView = (ImageView) findViewById(C0648R.C0650id.cameraStreamImageView);
        CameraStreamClient.getInstance().setListener(new CameraStreamClient.Listener() {
            public void onStreamAvailableChange(boolean z) {
                FtcDriverStationActivity.this.invalidateOptionsMenu();
                if (FtcDriverStationActivity.this.cameraStreamOpen && !z) {
                    FtcDriverStationActivity.this.hideCameraStream();
                }
            }

            public void onFrameBitmap(final Bitmap bitmap) {
                FtcDriverStationActivity.this.runOnUiThread(new Runnable() {
                    public void run() {
                        FtcDriverStationActivity.this.cameraStreamImageView.setImageBitmap(bitmap);
                    }
                });
            }
        });
        this.buttonInit = findViewById(C0648R.C0650id.buttonInit);
        this.buttonInitStop = findViewById(C0648R.C0650id.buttonInitStop);
        this.buttonStart = findViewById(C0648R.C0650id.buttonStart);
        this.controlPanelBack = findViewById(C0648R.C0650id.controlPanel);
        this.batteryInfo = findViewById(C0648R.C0650id.battery_info_layout);
        this.wifiInfo = findViewById(C0648R.C0650id.wifi_info_layout);
        ((ImageButton) findViewById(C0648R.C0650id.buttonStartArrow)).setImageDrawable(new FilledPolygonDrawable(((ColorDrawable) findViewById(C0648R.C0650id.buttonStartArrowColor).getBackground()).getColor(), 3));
        ((ImageView) findViewById(C0648R.C0650id.timerStopWatch)).setImageDrawable(new StopWatchDrawable(((ColorDrawable) findViewById(C0648R.C0650id.timerStopWatchColorHolder).getBackground()).getColor()));
        this.gamepadIndicators.put(GamepadUser.ONE, new GamepadIndicator(this, C0648R.C0650id.user1_icon_clicked, C0648R.C0650id.user1_icon_base));
        this.gamepadIndicators.put(GamepadUser.TWO, new GamepadIndicator(this, C0648R.C0650id.user2_icon_clicked, C0648R.C0650id.user2_icon_base));
        for (GamepadIndicator state : this.gamepadIndicators.values()) {
            state.setState(GamepadIndicator.State.INVISIBLE);
        }
        DriverStationGamepadManager driverStationGamepadManager = gamepadManager;
        if (driverStationGamepadManager == null) {
            DriverStationGamepadManager driverStationGamepadManager2 = new DriverStationGamepadManager(this);
            gamepadManager = driverStationGamepadManager2;
            driverStationGamepadManager2.setGamepadIndicators(this.gamepadIndicators);
            gamepadManager.initialize(usingUserspaceDriver);
        } else {
            driverStationGamepadManager.setGamepadIndicators(this.gamepadIndicators);
        }
        this.usbManager = (UsbManager) getSystemService("usb");
        this.usbInsertionReceiver = new BroadcastReceiver() {
            public void onReceive(Context context, Intent intent) {
                if (intent.getAction().equals("android.hardware.usb.action.USB_DEVICE_ATTACHED") && KnownGamepads.isLogitechF310Gamepad_DirectInput((UsbDevice) intent.getParcelableExtra("device"))) {
                    FtcDriverStationActivity.this.showF310InDinputModeDialog();
                }
            }
        };
        TextView textView2 = (TextView) findViewById(C0648R.C0650id.activeConfigName);
        this.activeConfigText = textView2;
        textView2.setText(" ");
        this.timerAndTimerSwitch = findViewById(C0648R.C0650id.timerAndTimerSwitch);
        this.buttonAutonomous = (Button) findViewById(C0648R.C0650id.buttonAutonomous);
        this.buttonTeleOp = (Button) findViewById(C0648R.C0650id.buttonTeleOp);
        this.currentOpModeName = (TextView) findViewById(C0648R.C0650id.currentOpModeName);
        this.chooseOpModePrompt = findViewById(C0648R.C0650id.chooseOpModePrompt);
        this.txtGuiPreselectedTele = (TextView) findViewById(C0648R.C0650id.textViewGuiPreselectedTele);
        View findViewById = findViewById(C0648R.C0650id.layoutGuiTelePreselection);
        this.layoutGuiTelePreselection = findViewById;
        findViewById.setVisibility(8);
        this.btnGuiPreselectTele = findViewById(C0648R.C0650id.btnPreselectTele);
        clearGuiTelePreselection();
        this.btnGuiPreselectTele.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                FtcDriverStationActivity.this.clearGuiTelePreselection();
                return true;
            }
        });
        this.buttonStop = (ImageButton) findViewById(C0648R.C0650id.buttonStop);
        ImageButton imageButton = (ImageButton) findViewById(C0648R.C0650id.menu_buttons);
        this.buttonMenu = imageButton;
        imageButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivity ftcDriverStationActivity = FtcDriverStationActivity.this;
                PopupMenu popupMenu = new PopupMenu(ftcDriverStationActivity, ftcDriverStationActivity.getPopupMenuAnchor());
                popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
                    public boolean onMenuItemClick(MenuItem menuItem) {
                        return FtcDriverStationActivity.this.onOptionsItemSelected(menuItem);
                    }
                });
                FtcDriverStationActivity.this.onCreateOptionsMenu(popupMenu.getMenu());
                popupMenu.show();
            }
        });
        this.preferences.registerOnSharedPreferenceChangeListener(this);
        BatteryChecker batteryChecker2 = new BatteryChecker(this, (long) 300000);
        this.batteryChecker = batteryChecker2;
        batteryChecker2.startBatteryMonitoring();
        resetBatteryStats();
        pingStatus((int) C0648R.string.ping_status_no_heartbeat);
        this.networkConnectionHandler.pushNetworkConnectionCallback(this);
        this.networkConnectionHandler.pushReceiveLoopCallback(this);
        DeviceNameManagerFactory.getInstance().registerCallback(this.deviceNameManagerCallback);
        ((WifiManager) AppUtil.getDefContext().getApplicationContext().getSystemService("wifi")).setWifiEnabled(true);
        WifiMuteStateMachine wifiMuteStateMachine2 = new WifiMuteStateMachine();
        this.wifiMuteStateMachine = wifiMuteStateMachine2;
        wifiMuteStateMachine2.initialize();
        this.wifiMuteStateMachine.start();
        this.wifiMuteStateMachine.registerCallback(this);
        this.processUserActivity = true;
        SoundPlayingRobotMonitor.prefillSoundCache();
        RobotLog.logAppInfo();
        RobotLog.logDeviceInfo();
        this.androidTextToSpeech.initialize();
    }

    /* access modifiers changed from: protected */
    public void doMatchNumFieldBehaviorInit() {
        if (!this.preferencesHelper.readBoolean(getString(C0648R.string.pref_match_logging_on_off), false)) {
            disableMatchLoggingUI();
        } else if (this.landscape) {
            matchNumFieldBehaviorInitLandscape();
        } else {
            matchNumFieldBehaviorInitPortrait();
        }
    }

    /* access modifiers changed from: protected */
    public void matchNumFieldBehaviorInitPortrait() {
        this.matchNumField.setText(InspectionState.NO_VERSION);
        this.matchNumField.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                FtcDriverStationActivity.this.matchNumField.setText(InspectionState.NO_VERSION);
            }
        });
        this.matchNumField.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if (i != 6) {
                    return false;
                }
                int validateMatchEntry = FtcDriverStationActivity.this.validateMatchEntry(textView.getText().toString());
                if (validateMatchEntry == -1) {
                    AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivity.this.getString(C0648R.string.invalidMatchNumber));
                    FtcDriverStationActivity.this.matchNumField.setText(InspectionState.NO_VERSION);
                    return false;
                }
                FtcDriverStationActivity.this.sendMatchNumber(validateMatchEntry);
                return false;
            }
        });
        findViewById(C0648R.C0650id.buttonInit).requestFocus();
    }

    /* access modifiers changed from: protected */
    public void matchNumFieldBehaviorInitLandscape() {
        this.matchLoggingContainer.setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                new ManualKeyInDialog(FtcDriverStationActivity.this.context, "Enter Match Number", new ManualKeyInDialog.Listener() {
                    public void onInput(String str) {
                        int validateMatchEntry = FtcDriverStationActivity.this.validateMatchEntry(str);
                        if (validateMatchEntry == -1) {
                            AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, FtcDriverStationActivity.this.getString(C0648R.string.invalidMatchNumber));
                            FtcDriverStationActivity.this.clearMatchNumber();
                            return;
                        }
                        FtcDriverStationActivity.this.matchNumTxtView.setText(Integer.toString(validateMatchEntry));
                        FtcDriverStationActivity.this.sendMatchNumber(validateMatchEntry);
                    }
                }).show();
            }
        });
    }

    /* access modifiers changed from: protected */
    public void enableMatchLoggingUI() {
        RobotLog.m54ii(TAG, "Show match logging UI");
        if (this.landscape) {
            this.matchLoggingContainer.setVisibility(0);
            return;
        }
        this.matchNumField.setVisibility(0);
        this.matchNumField.setEnabled(true);
        findViewById(C0648R.C0650id.matchNumLabel).setVisibility(0);
    }

    /* access modifiers changed from: protected */
    public void disableMatchLoggingUI() {
        RobotLog.m54ii(TAG, "Hide match logging UI");
        if (this.landscape) {
            this.matchLoggingContainer.setVisibility(8);
            return;
        }
        this.matchNumField.setVisibility(4);
        this.matchNumField.setEnabled(false);
        findViewById(C0648R.C0650id.matchNumLabel).setVisibility(4);
    }

    /* access modifiers changed from: protected */
    public void startOrRestartNetwork() {
        RobotLog.m60vv(TAG, "startOrRestartNetwork()");
        NetworkConnection networkConnection = NetworkConnectionHandler.getInstance().getNetworkConnection();
        if (networkConnection instanceof DriverStationAccessPointAssistant) {
            ((DriverStationAccessPointAssistant) networkConnection).resetSecondsBetweenWiFiScansToDefault();
        }
        assumeClientDisconnect();
        showWifiStatus(false, getString(C0648R.string.wifiStatusDisconnected));
        initializeNetwork();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        RobotLog.m60vv(TAG, "Lifecycle onStart() : " + this.instanceIdStr);
        RobotLog.onApplicationStart();
        updateRcBatteryIndependence(this.preferences);
        resetBatteryStats();
        pingStatus((int) C0648R.string.ping_status_no_heartbeat);
        if (this.networkConnectionHandler.isShutDown()) {
            startOrRestartNetwork();
        }
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
        this.wifiMuteStateMachine.unMaskEvent(WifiMuteEvent.STOPPED_OPMODE);
        if (areF310sInDirectInputModeConnected()) {
            showF310InDinputModeDialog();
        }
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction("android.hardware.usb.action.USB_DEVICE_ATTACHED");
        registerReceiver(this.usbInsertionReceiver, intentFilter);
        FtcAboutActivity.setBuildTimeFromBuildConfig(BuildConfig.APP_BUILD_TIME);
    }

    /* access modifiers changed from: protected */
    public void onResume() {
        super.onResume();
        this.waitingForResumeAfterOnActivityResultCalled = false;
        RobotLog.m60vv(TAG, "Lifecycle onResume() : " + this.instanceIdStr);
        this.disconnectFromPeerOnActivityStop = true;
        this.annotationAutomagicPreselect = this.preferences.getBoolean(getResources().getString(C0648R.string.key_pref_auto_queue), true);
        if (!this.preferences.getBoolean(getString(C0648R.string.pref_warn_about_obsolete_software), true)) {
            this.rcObsoleteWarning.setVisibility(8);
        }
        if (this.landscape) {
            NetworkConnection networkConnection = this.networkConnectionHandler.getNetworkConnection();
            if (networkConnection.getNetworkType() == NetworkType.WIRELESSAP) {
                ((DriverStationAccessPointAssistant) networkConnection).registerNetworkHealthListener(this);
            }
        }
        synchronized (this.runOnResumeQueue) {
            Runnable poll = this.runOnResumeQueue.poll();
            while (poll != null) {
                poll.run();
                poll = this.runOnResumeQueue.poll();
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onNewIntent(Intent intent) {
        DriverStationGamepadManager driverStationGamepadManager = gamepadManager;
        if (driverStationGamepadManager != null) {
            driverStationGamepadManager.handleNewIntent(intent);
        }
    }

    /* access modifiers changed from: protected */
    public void initializeNetwork() {
        updateLoggingPrefs();
        NetworkType networkType = NetworkConnectionHandler.getNetworkType(this);
        this.connectionOwner = this.preferences.getString(getString(C0648R.string.pref_connection_owner_identity), getString(C0648R.string.connection_owner_default));
        String string = this.preferences.getString(getString(C0648R.string.pref_connection_owner_password), getString(C0648R.string.connection_owner_password_default));
        this.connectionOwnerPassword = string;
        try {
            this.networkConnectionHandler.init(networkType, this.connectionOwner, string, this, gamepadManager);
            if (this.networkConnectionHandler.isNetworkConnected()) {
                RobotLog.m60vv("Robocol", "Spoofing a Network Connection event...");
                onNetworkConnectionEvent(NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE);
            }
        } catch (SecurityException e) {
            if (Build.VERSION.SDK_INT != 23) {
                throw e;
            } else if (!this.requestingPermissionToWriteToSystemSettings) {
                this.requestingPermissionToWriteToSystemSettings = true;
                AppUtil.DialogParams dialogParams = new AppUtil.DialogParams(UILocation.ONLY_LOCAL, AppUtil.getDefContext().getString(C0648R.string.dialogEnableModifyingSettingsTitle), AppUtil.getDefContext().getString(C0648R.string.dialogEnableModifyingSettingsMessage));
                dialogParams.activity = this;
                AppUtil.getInstance().showDialog(dialogParams, (Consumer<AppUtil.DialogContext>) new Consumer<AppUtil.DialogContext>() {
                    public void accept(AppUtil.DialogContext dialogContext) {
                        NetworkConnectionHandler.getInstance().shutdown();
                        Intent intent = new Intent("android.settings.action.MANAGE_WRITE_SETTINGS");
                        intent.setData(Uri.parse("package:" + AppUtil.getDefContext().getPackageName()));
                        FtcDriverStationActivity.this.startActivityForResult(intent, LaunchActivityConstantsList.RequestCode.WRITE_TO_SYSTEM_SETTINGS.ordinal());
                    }
                });
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onPause() {
        super.onPause();
        RobotLog.m60vv(TAG, "Lifecycle onPause() : " + this.instanceIdStr);
        if (this.landscape) {
            this.practiceTimerManager.reset();
            NetworkConnection networkConnection = this.networkConnectionHandler.getNetworkConnection();
            if (networkConnection.getNetworkType() == NetworkType.WIRELESSAP) {
                ((DriverStationAccessPointAssistant) networkConnection).unregisterNetworkHealthListener(this);
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStop() {
        super.onStop();
        RobotLog.m60vv(TAG, "Lifecycle onStop() : " + this.instanceIdStr);
        pingStatus((int) C0648R.string.ping_status_stopped);
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_STOP);
        initDefaultOpMode();
        if (this.disconnectFromPeerOnActivityStop) {
            RobotLog.m54ii(TAG, "App appears to be exiting. Shutting down network so that another DS can connect");
            this.networkConnectionHandler.shutdown();
            this.networkConnectionHandler.cancelConnectionSearch();
        }
        unregisterReceiver(this.usbInsertionReceiver);
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        RobotLog.m60vv(TAG, "Lifecycle onDestroy() : " + this.instanceIdStr);
        this.androidTextToSpeech.close();
        Set<Integer> set = instanceSet;
        set.remove(Integer.valueOf(this.instanceId));
        if (set.isEmpty()) {
            DeviceNameManager instance = DeviceNameManagerFactory.getInstance();
            instance.unregisterCallback(this.deviceNameManagerCallback);
            this.networkConnectionHandler.removeNetworkConnectionCallback(this);
            this.networkConnectionHandler.removeReceiveLoopCallback(this);
            shutdown();
            PreferenceRemoterDS.getInstance().stop(this.prefRemoterStartResult);
            instance.stop(this.deviceNameManagerStartResult);
            RobotLog.cancelWriteLogcatToDisk();
        }
    }

    public void onWindowFocusChanged(boolean z) {
        super.onWindowFocusChanged(z);
        if (z) {
            this.immersion.hideSystemUI();
            getWindow().setFlags(134217728, 134217728);
        }
    }

    public void showToast(String str) {
        this.appUtil.showToast(UILocation.ONLY_LOCAL, str);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String str) {
        RobotLog.m61vv(TAG, "onSharedPreferenceChanged() pref=%s", str);
        if (str.equals(this.context.getString(C0648R.string.pref_device_name_rc_display))) {
            final String string = sharedPreferences.getString(str, InspectionState.NO_VERSION);
            if (string.length() > 0) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        if (FtcDriverStationActivity.this.textWifiDirectStatusShowingRC) {
                            FtcDriverStationActivity.this.textWifiDirectStatus.setText(string);
                        }
                    }
                });
            }
        } else if (str.equals(getString(C0648R.string.pref_has_independent_phone_battery_rc))) {
            updateRcBatteryIndependence(this.preferences);
        } else if (!str.equals(getString(C0648R.string.pref_app_theme)) && str.equals("pref_wifip2p_channel")) {
            RobotLog.m60vv(TAG, "pref_wifip2p_channel changed.");
            showWifiChannel();
        }
        updateLoggingPrefs();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(C0648R.C0652menu.ftc_driver_station, menu);
        if (this.uiState != UIState.WAITING_FOR_START_EVENT || !CameraStreamClient.getInstance().isStreamAvailable()) {
            menu.findItem(C0648R.C0650id.action_camera_stream).setVisible(false);
        } else {
            menu.findItem(C0648R.C0650id.action_camera_stream).setVisible(true);
        }
        return true;
    }

    public boolean onOptionsItemSelected(MenuItem menuItem) {
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_OTHER);
        this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
        switch (menuItem.getItemId()) {
            case C0648R.C0650id.action_about:
                startActivity(new Intent(AppUtil.getDefContext(), FtcAboutActivity.class));
                return true;
            case C0648R.C0650id.action_camera_stream:
                if (this.cameraStreamOpen) {
                    hideCameraStream();
                } else {
                    showCameraStream();
                }
                return true;
            case C0648R.C0650id.action_configure:
                if (NetworkConnectionHandler.getInstance().isPeerConnected()) {
                    EditParameters editParameters = new EditParameters();
                    Intent intent = new Intent(AppUtil.getDefContext(), FtcLoadFileActivity.class);
                    editParameters.putIntent(intent);
                    startActivityForResult(intent, LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal());
                } else {
                    Toast.makeText(this, "Not connected to a robot controller", 0).show();
                }
                return true;
            case C0648R.C0650id.action_exit_app:
                finishAffinity();
                for (ActivityManager.AppTask finishAndRemoveTask : ((ActivityManager) getSystemService("activity")).getAppTasks()) {
                    finishAndRemoveTask.finishAndRemoveTask();
                }
                AppUtil.getInstance().exitApplication();
                return true;
            case C0648R.C0650id.action_inspection_mode:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationInspectionReportsActivity.class), LaunchActivityConstantsList.RequestCode.INSPECTIONS.ordinal());
                return true;
            case C0648R.C0650id.action_program_and_manage:
                RobotLog.m60vv(TAG, "action_program_and_manage clicked");
                this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE));
                return true;
            case C0648R.C0650id.action_restart_robot:
                this.stopRequestedOnDsSide = true;
                this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.ACTIVITY_START);
                this.wifiMuteStateMachine.maskEvent(WifiMuteEvent.STOPPED_OPMODE);
                return true;
            case C0648R.C0650id.action_settings:
                startActivityForResult(new Intent(getBaseContext(), FtcDriverStationSettingsActivity.class), LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal());
                return true;
            default:
                return super.onOptionsItemSelected(menuItem);
        }
    }

    public void onActivityResult(int i, int i2, Intent intent) {
        RobotLog.m61vv(TAG, "onActivityResult(request=%d)", Integer.valueOf(i));
        this.waitingForResumeAfterOnActivityResultCalled = true;
        if (i == LaunchActivityConstantsList.RequestCode.SETTINGS_DRIVER_STATION.ordinal()) {
            if (intent != null) {
                FtcDriverStationSettingsActivity.Result deserialize = FtcDriverStationSettingsActivity.Result.deserialize(intent.getExtras().getString("RESULT"));
                if (deserialize.prefLogsClicked) {
                    updateLoggingPrefs();
                }
                if (deserialize.prefPairingMethodChanged) {
                    RobotLog.m54ii(TAG, "Pairing method changed in settings activity, shutdown network to force complete restart");
                    startOrRestartNetwork();
                }
                if (deserialize.prefPairClicked) {
                    startOrRestartNetwork();
                }
                if (deserialize.prefAdvancedClicked) {
                    this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
                }
            }
        } else if (i == LaunchActivityConstantsList.RequestCode.CONFIGURE_DRIVER_STATION.ordinal()) {
            requestUIState();
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RESTART_ROBOT));
        } else if (i == LaunchActivityConstantsList.RequestCode.WRITE_TO_SYSTEM_SETTINGS.ordinal()) {
            this.requestingPermissionToWriteToSystemSettings = false;
        }
    }

    /* access modifiers changed from: protected */
    public void updateLoggingPrefs() {
        this.debugLogging = this.preferences.getBoolean(getString(C0648R.string.pref_debug_driver_station_logs), false);
        if (this.preferences.getBoolean(getString(C0648R.string.pref_match_logging_on_off), false)) {
            enableMatchLoggingUI();
        } else {
            disableMatchLoggingUI();
        }
    }

    public void onConfigurationChanged(Configuration configuration) {
        super.onConfigurationChanged(configuration);
    }

    public boolean dispatchGenericMotionEvent(MotionEvent motionEvent) {
        if (gamepadManager == null || !AndroidGamepadManager.isGamepadDeviceId(motionEvent.getDeviceId())) {
            return super.dispatchGenericMotionEvent(motionEvent);
        }
        gamepadManager.handleGamepadEvent(motionEvent);
        return true;
    }

    public boolean dispatchKeyEvent(KeyEvent keyEvent) {
        InputDevice device = InputDevice.getDevice(keyEvent.getDeviceId());
        if (device != null) {
            if (gamepadManager != null && AndroidGamepadManager.isGamepadDeviceId(keyEvent.getDeviceId())) {
                if (!usingUserspaceDriver || !KnownGamepads.isKnownGamepad(device.getVendorId(), device.getProductId())) {
                    gamepadManager.handleGamepadEvent(keyEvent);
                } else {
                    RobotLog.m66ww(TAG, "Received Android gamepad event from gamepad that should be under userspace control");
                    showReplugGamepadDialog();
                }
                return true;
            } else if (KnownGamepads.isLogitechF310Gamepad_DirectInput(device.getVendorId(), device.getProductId())) {
                showF310InDinputModeDialog();
                return true;
            }
        }
        return super.dispatchKeyEvent(keyEvent);
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRunLedEffect(String str) {
        if (!this.suppressLedCommands) {
            Gamepad.LedEffect deserialize = Gamepad.LedEffect.deserialize(str);
            if (deserialize.steps.size() == 1 && deserialize.steps.get(0).duration == -1) {
                gamepadManager.setLedColor(deserialize.user, deserialize.steps.get(0).f117r, deserialize.steps.get(0).f116g, deserialize.steps.get(0).f115b);
            } else {
                gamepadManager.runLedEffect(deserialize);
            }
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandRunRumbleEffect(String str) {
        if (!this.suppressRumbleCommands) {
            Gamepad.RumbleEffect deserialize = Gamepad.RumbleEffect.deserialize(str);
            if (deserialize.steps.size() == 1 && deserialize.steps.get(0).duration == -1) {
                gamepadManager.setRumblePowers(deserialize.user, (short) deserialize.steps.get(0).large, (short) deserialize.steps.get(0).small);
            } else {
                gamepadManager.runRumbleEffect(deserialize);
            }
        }
        return CallbackResult.HANDLED;
    }

    private void showReplugGamepadDialog() {
        if (!this.replugGamepadDialogShowing) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Please replug gamepad");
            builder.setMessage("Unplug and replug the gamepad you just touched. If a USB permission prompt appears, check the box 'Use by default'.");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcDriverStationActivity.this.replugGamepadDialogShowing = false;
                }
            });
            builder.show();
            this.replugGamepadDialogShowing = true;
        }
    }

    /* access modifiers changed from: private */
    public void showF310InDinputModeDialog() {
        if (!this.f310InDinputModeDialogShowing) {
            AlertDialog.Builder builder = new AlertDialog.Builder(this);
            builder.setTitle("Switch gamepad mode");
            builder.setMessage("There are one or more Logitech F310 gamepads connected which are set to DirectInput mode, which is not supported. Please set the gamepad to Xinput mode, by moving the switch on the back to the 'X' position.");
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcDriverStationActivity.this.f310InDinputModeDialogShowing = false;
                }
            });
            builder.show();
            this.f310InDinputModeDialogShowing = true;
        }
    }

    public boolean areF310sInDirectInputModeConnected() {
        for (UsbDevice isLogitechF310Gamepad_DirectInput : this.usbManager.getDeviceList().values()) {
            if (KnownGamepads.isLogitechF310Gamepad_DirectInput(isLogitechF310Gamepad_DirectInput)) {
                return true;
            }
        }
        return false;
    }

    public CallbackResult onNetworkConnectionEvent(NetworkConnection.NetworkEvent networkEvent) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        RobotLog.m54ii(TAG, "Received networkConnectionEvent: " + networkEvent.toString());
        switch (C060650.f70x94151df2[networkEvent.ordinal()]) {
            case 1:
                if (this.networkConnectionHandler.isWifiDirect()) {
                    onPeersAvailableWifiDirect();
                } else {
                    onPeersAvailableSoftAP();
                }
                return CallbackResult.HANDLED;
            case 2:
                RobotLog.m48ee(TAG, "Wi-Fi Direct - connected as Group Owner, was expecting Peer");
                showWifiStatus(false, getString(C0648R.string.wifiStatusErrorConnectedAsGroupOwner));
                showWifiDirectConnectedAsGroupOwnerDialog();
                return CallbackResult.HANDLED;
            case 3:
                showWifiStatus(false, getString(C0648R.string.wifiStatusConnecting));
                return CallbackResult.HANDLED;
            case 4:
                showWifiStatus(false, getString(C0648R.string.wifiStatusConnected));
                return CallbackResult.HANDLED;
            case 5:
                showWifiStatus(true, getBestRobotControllerName());
                showWifiChannel();
                if (!NetworkConnection.isDeviceNameValid(this.networkConnectionHandler.getDeviceName())) {
                    RobotLog.m48ee(TAG, "Wi-Fi Direct device name contains non-printable characters");
                    showWifiDirectNameUnprintableCharsDialog();
                } else if (this.networkConnectionHandler.connectedWithUnexpectedDevice()) {
                    showWifiStatus(false, getString(C0648R.string.wifiStatusErrorWrongDevice));
                    if (!this.networkConnectionHandler.isWifiDirect()) {
                        String str = this.connectionOwner;
                        if (str == null && this.connectionOwnerPassword == null) {
                            showWifiStatus(false, getString(C0648R.string.wifiStatusNotPaired));
                            return CallbackResult.HANDLED;
                        }
                        this.networkConnectionHandler.startConnection(str, this.connectionOwnerPassword);
                    } else if (!this.networkConnectionHandler.getDeviceName().equals(this.networkConnectionHandler.getConnectionOwnerName())) {
                        showWifiDirectConnectedToWrongDeviceDialog();
                    } else {
                        showWifiDirectConnectedAsGroupOwnerDialog();
                    }
                    return CallbackResult.HANDLED;
                }
                this.networkConnectionHandler.handleConnectionInfoAvailable();
                this.networkConnectionHandler.cancelConnectionSearch();
                assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
                return CallbackResult.HANDLED;
            case 6:
                String string = getString(C0648R.string.wifiStatusDisconnected);
                showWifiStatus(false, string);
                RobotLog.m60vv(TAG, "Network Connection - " + string);
                NetworkConnection networkConnection = NetworkConnectionHandler.getInstance().getNetworkConnection();
                if (networkConnection instanceof DriverStationAccessPointAssistant) {
                    ((DriverStationAccessPointAssistant) networkConnection).temporarilySetSecondsBetweenWifiScans(5, 45);
                }
                this.networkConnectionHandler.discoverPotentialConnections();
                assumeClientDisconnect();
                return CallbackResult.HANDLED;
            case 7:
                String string2 = getString(C0648R.string.dsErrorMessage, new Object[]{this.networkConnectionHandler.getFailureReason()});
                showWifiStatus(false, string2);
                RobotLog.m60vv(TAG, "Network Connection - " + string2);
                return callbackResult;
            default:
                return callbackResult;
        }
    }

    private void showWifiDirectConnectedToWrongDeviceDialog() {
        if (this.alertDialogWifiDirectWrongDevice == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 16973935));
            builder.setTitle(getString(C0648R.string.title_p2p_connected_wrong_device));
            builder.setMessage(getString(C0648R.string.msg_ds_p2p_connected_wrong_device));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcDriverStationActivity.this.alertDialogWifiDirectWrongDevice = null;
                }
            });
            this.alertDialogWifiDirectWrongDevice = builder.show();
        }
    }

    private void showWifiDirectConnectedAsGroupOwnerDialog() {
        if (this.alertDialogConnectedAsGroupOwner == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 16973935));
            builder.setTitle(getString(C0648R.string.title_p2p_misconfigured));
            builder.setMessage(getString(C0648R.string.msg_ds_p2p_misconfigured));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcDriverStationActivity.this.alertDialogConnectedAsGroupOwner = null;
                }
            });
            this.alertDialogConnectedAsGroupOwner = builder.show();
        }
    }

    private void showWifiDirectNameUnprintableCharsDialog() {
        if (this.alertDialogWifiDirectNameNonPrintableChars == null) {
            AlertDialog.Builder builder = new AlertDialog.Builder(new ContextThemeWrapper(this, 16973935));
            builder.setTitle(getString(C0648R.string.title_p2p_unprintable_chars));
            builder.setMessage(getString(C0648R.string.msg_p2p_unprintable_chars));
            builder.setCancelable(false);
            builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    FtcDriverStationActivity.this.alertDialogWifiDirectNameNonPrintableChars = null;
                }
            });
            this.alertDialogWifiDirectNameNonPrintableChars = builder.show();
        }
    }

    private String getBestRobotControllerName() {
        return this.networkConnectionHandler.getConnectionOwnerName();
    }

    private void onPeersAvailableWifiDirect() {
        if (!this.networkConnectionHandler.connectingOrConnected()) {
            onPeersAvailableSoftAP();
        }
    }

    private void onPeersAvailableSoftAP() {
        if (this.networkConnectionHandler.connectionMatches(getString(C0648R.string.connection_owner_default))) {
            showWifiStatus(false, getString(C0648R.string.wifiStatusNotPaired));
        } else {
            showWifiStatus(false, getString(C0648R.string.wifiStatusSearching));
        }
        this.networkConnectionHandler.handlePeersAvailable();
    }

    public void onClickButtonInit(View view) {
        handleOpModeInit();
    }

    public void onClickButtonStart(View view) {
        handleOpModeStart();
    }

    public void onClickTimer(View view) {
        boolean z = !this.opModeUseTimer;
        this.opModeUseTimer = z;
        enableAndResetTimer(z);
    }

    /* access modifiers changed from: protected */
    public void enableAndResetTimer(boolean z) {
        if (!z) {
            this.opModeCountDown.disable();
        } else {
            stopTimerAndReset();
            this.opModeCountDown.enable();
        }
        this.opModeUseTimer = z;
    }

    /* access modifiers changed from: protected */
    public void enableAndResetTimerForQueued() {
        enableAndResetTimer(this.queuedOpMode.flavor == OpModeMeta.Flavor.AUTONOMOUS);
    }

    /* access modifiers changed from: package-private */
    public void stopTimerPreservingRemainingTime() {
        this.opModeCountDown.stopPreservingRemainingTime();
    }

    /* access modifiers changed from: package-private */
    public void stopTimerAndReset() {
        this.opModeCountDown.stop();
        this.opModeCountDown.resetCountdown();
    }

    public void onClickButtonAutonomous(View view) {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>() {
            public boolean test(OpModeMeta opModeMeta) {
                return opModeMeta.flavor == OpModeMeta.Flavor.AUTONOMOUS;
            }
        }), C0648R.string.opmodeDialogTitleAutonomous, true);
    }

    public void onClickButtonTeleOp(View view) {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>() {
            public boolean test(OpModeMeta opModeMeta) {
                return opModeMeta.flavor == OpModeMeta.Flavor.TELEOP;
            }
        }), C0648R.string.opmodeDialogTitleTeleOp, true);
    }

    /* access modifiers changed from: protected */
    public List<OpModeMeta> filterOpModes(Predicate<OpModeMeta> predicate) {
        LinkedList linkedList = new LinkedList();
        for (OpModeMeta next : this.opModes) {
            if (predicate.test(next)) {
                linkedList.add(next);
            }
        }
        return linkedList;
    }

    /* access modifiers changed from: protected */
    public void showOpModeDialog(List<OpModeMeta> list, int i, boolean z) {
        showOpModeDialog(list, i, this, z);
    }

    /* access modifiers changed from: protected */
    public void showOpModeDialog(List<OpModeMeta> list, int i, OpModeSelectionDialogFragment.OpModeSelectionDialogListener opModeSelectionDialogListener, boolean z) {
        if (z) {
            stopTimerPreservingRemainingTime();
            initDefaultOpMode();
        }
        OpModeSelectionDialogFragment opModeSelectionDialogFragment = new OpModeSelectionDialogFragment();
        opModeSelectionDialogFragment.setOnSelectionDialogListener(opModeSelectionDialogListener);
        opModeSelectionDialogFragment.setOpModes(list);
        opModeSelectionDialogFragment.setTitle(i);
        opModeSelectionDialogFragment.show(getFragmentManager(), "op_mode_selection");
    }

    /* access modifiers changed from: protected */
    public void showCameraStream() {
        this.cameraStreamOpen = true;
        gamepadManager.setEnabled(false);
        setVisibility(this.cameraStreamLayout, 0);
        setVisibility(this.buttonStart, 4);
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_FRAME));
        showToast(getString(C0648R.string.toastDisableGamepadsStream));
    }

    /* access modifiers changed from: protected */
    public void hideCameraStream() {
        this.cameraStreamOpen = false;
        gamepadManager.setEnabled(true);
        setVisibility(this.cameraStreamLayout, 4);
        setVisibility(this.buttonStart, 0);
    }

    public void onClickButtonStop(View view) {
        handleOpModeStop(false);
    }

    public void clearGuiTelePreselection() {
        this.guiPreselectedTeleop = null;
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.btnGuiPreselectTele.setAlpha(FtcDriverStationActivity.PARTLY_OPAQUE);
                FtcDriverStationActivity.this.txtGuiPreselectedTele.setVisibility(8);
            }
        });
    }

    public void hideLayoutGuiTelePreselection() {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.layoutGuiTelePreselection.setVisibility(8);
            }
        });
    }

    public void showLayoutGuiTelePreselection() {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.layoutGuiTelePreselection.setVisibility(0);
            }
        });
    }

    public void configureGuiTelePreselectionFor(final OpModeMeta opModeMeta) {
        this.guiPreselectedTeleop = opModeMeta;
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.btnGuiPreselectTele.setAlpha(1.0f);
                FtcDriverStationActivity.this.txtGuiPreselectedTele.setVisibility(0);
                TextView textView = FtcDriverStationActivity.this.txtGuiPreselectedTele;
                textView.setText("Up Next: " + opModeMeta.name);
            }
        });
    }

    public void onClickBtnGuiTelePreselection(View view) {
        showOpModeDialog(filterOpModes(new Predicate<OpModeMeta>() {
            public boolean test(OpModeMeta opModeMeta) {
                return opModeMeta.flavor == OpModeMeta.Flavor.TELEOP;
            }
        }), C0648R.string.opmodeDialogTitlePreselectTele, new OpModeSelectionDialogFragment.OpModeSelectionDialogListener() {
            public void onOpModeSelectionClick(OpModeMeta opModeMeta) {
                FtcDriverStationActivity.this.configureGuiTelePreselectionFor(opModeMeta);
            }
        }, false);
    }

    public void onOpModeSelectionClick(OpModeMeta opModeMeta) {
        handleOpModeQueued(opModeMeta);
    }

    /* access modifiers changed from: protected */
    public void shutdown() {
        this.networkConnectionHandler.stop();
        this.networkConnectionHandler.shutdown();
    }

    public CallbackResult packetReceived(RobocolDatagram robocolDatagram) throws RobotCoreException {
        return CallbackResult.NOT_HANDLED;
    }

    public void onPeerConnected() {
        RobotLog.m60vv(TAG, "robot controller connected");
        assumeClientConnectAndRefreshUI(ControlPanelBack.NO_CHANGE);
        PreferenceRemoterDS.getInstance().sendInformationalPrefsToRc();
    }

    public void onPeerDisconnected() {
        RobotLog.m60vv(TAG, "robot controller disconnected");
        assumeClientDisconnect();
    }

    public CallbackResult peerDiscoveryEvent(RobocolDatagram robocolDatagram) throws RobotCoreException {
        try {
            PeerDiscovery updateConnection = this.networkConnectionHandler.updateConnection(robocolDatagram);
            if (!this.receivedPeerDiscoveryFromCurrentPeer && !updateConnection.isSdkBuildMonthValid() && this.preferences.getBoolean(getString(C0648R.string.pref_warn_about_obsolete_software), true)) {
                runOnUiThread(new Runnable() {
                    public void run() {
                        FtcDriverStationActivity.this.rcObsoleteWarning.setVisibility(0);
                    }
                });
            }
            this.receivedPeerDiscoveryFromCurrentPeer = true;
        } catch (RobotProtocolException e) {
            reportGlobalError(e.getMessage(), false);
            showRobotBatteryVoltage(FtcEventLoopHandler.NO_VOLTAGE_SENSOR);
        }
        return CallbackResult.HANDLED;
    }

    public CallbackResult heartbeatEvent(RobocolDatagram robocolDatagram) {
        try {
            this.heartbeatRecv.fromByteArray(robocolDatagram.getData());
            RobotLog.processTimeSynch(this.heartbeatRecv.f135t0, this.heartbeatRecv.f136t1, this.heartbeatRecv.f137t2, robocolDatagram.getWallClockTimeMsReceived());
            setRobotState(RobotState.fromByte(this.heartbeatRecv.getRobotState()));
            this.pingAverage.addNumber((int) (((double) (robocolDatagram.getNanoTimeReceived() - this.heartbeatRecv.getTimestamp())) / 1000000.0d));
            if (this.lastUiUpdate.time() > 0.5d) {
                this.lastUiUpdate.reset();
                networkStatus();
            }
        } catch (RobotCoreException e) {
            RobotLog.logStackTrace(e);
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void networkStatus() {
        pingStatus(String.format("%dms", new Object[]{Integer.valueOf(this.pingAverage.getAverage())}));
        long bytesPerSecond = this.networkConnectionHandler.getBytesPerSecond();
        if (bytesPerSecond > 0) {
            showBytesPerSecond(bytesPerSecond);
        }
    }

    /* access modifiers changed from: protected */
    public void setRobotState(RobotState robotState2) {
        WifiMuteStateMachine wifiMuteStateMachine2;
        if (this.robotState != robotState2) {
            this.robotState = robotState2;
            if (robotState2 == RobotState.STOPPED) {
                traceUiStateChange("ui:uiRobotStopped", UIState.ROBOT_STOPPED);
                disableAndDimOpModeMenu();
                disableOpModeControls();
                dimControlPanelBack();
            }
            if (robotState2 == RobotState.EMERGENCY_STOP && (wifiMuteStateMachine2 = this.wifiMuteStateMachine) != null) {
                wifiMuteStateMachine2.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
            }
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleNotifyRobotState(String str) {
        setRobotState(RobotState.fromByte(Integer.valueOf(str).intValue()));
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleReportGlobalError(String str) {
        RobotLog.m48ee(TAG, "Received error from robot controller: " + str);
        RobotLog.setGlobalErrorMsg(str);
        return CallbackResult.HANDLED;
    }

    public CallbackResult commandEvent(Command command) {
        CallbackResult callbackResult = CallbackResult.NOT_HANDLED;
        try {
            String name = command.getName();
            String extra = command.getExtra();
            char c = 65535;
            switch (name.hashCode()) {
                case -2001838744:
                    if (name.equals(RobotCoreCommandList.CMD_GAMEPAD_LED_EFFECT)) {
                        c = 24;
                        break;
                    }
                    break;
                case -1530733715:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_OP_MODE_LIST)) {
                        c = 1;
                        break;
                    }
                    break;
                case -1121067382:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_TOAST)) {
                        c = 6;
                        break;
                    }
                    break;
                case -992356734:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_DIALOG)) {
                        c = 11;
                        break;
                    }
                    break;
                case -939314969:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_PROGRESS)) {
                        c = 8;
                        break;
                    }
                    break;
                case -856964827:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_DIALOG)) {
                        c = 10;
                        break;
                    }
                    break;
                case -832763622:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_STACKTRACE)) {
                        c = 9;
                        break;
                    }
                    break;
                case -362340438:
                    if (name.equals(RobotCoreCommandList.CMD_SET_TELEMETRY_DISPLAY_FORMAT)) {
                        c = 22;
                        break;
                    }
                    break;
                case -321815447:
                    if (name.equals(CommandList.CmdPlaySound.Command)) {
                        c = 15;
                        break;
                    }
                    break;
                case -206959740:
                    if (name.equals(RobotCoreCommandList.CMD_ROBOT_CONTROLLER_PREFERENCE)) {
                        c = 14;
                        break;
                    }
                    break;
                case -44710726:
                    if (name.equals(CommandList.CmdRequestSound.Command)) {
                        c = 16;
                        break;
                    }
                    break;
                case 78754538:
                    if (name.equals(RobotCoreCommandList.CMD_STREAM_CHANGE)) {
                        c = 18;
                        break;
                    }
                    break;
                case 202444237:
                    if (name.equals(CommandList.CmdStopPlayingSounds.Command)) {
                        c = 17;
                        break;
                    }
                    break;
                case 323288778:
                    if (name.equals(RobotCoreCommandList.CMD_SHOW_PROGRESS)) {
                        c = 7;
                        break;
                    }
                    break;
                case 619130094:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_ACTIVE_CONFIGURATION)) {
                        c = 3;
                        break;
                    }
                    break;
                case 739339659:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_ROBOT_STATE)) {
                        c = 0;
                        break;
                    }
                    break;
                case 857479075:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_INIT_OP_MODE)) {
                        c = 4;
                        break;
                    }
                    break;
                case 899701436:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_RUN_OP_MODE)) {
                        c = 5;
                        break;
                    }
                    break;
                case 1332202628:
                    if (name.equals(RobotCoreCommandList.CMD_NOTIFY_USER_DEVICE_LIST)) {
                        c = 2;
                        break;
                    }
                    break;
                case 1506024019:
                    if (name.equals(RobotCoreCommandList.CMD_DISMISS_ALL_DIALOGS)) {
                        c = 12;
                        break;
                    }
                    break;
                case 1509292278:
                    if (name.equals(RobotCoreCommandList.CMD_RECEIVE_FRAME_BEGIN)) {
                        c = 19;
                        break;
                    }
                    break;
                case 1510318778:
                    if (name.equals(RobotCoreCommandList.CMD_RECEIVE_FRAME_CHUNK)) {
                        c = 20;
                        break;
                    }
                    break;
                case 1661597945:
                    if (name.equals(CommandList.CMD_START_DS_PROGRAM_AND_MANAGE_RESP)) {
                        c = 13;
                        break;
                    }
                    break;
                case 1852830809:
                    if (name.equals(RobotCoreCommandList.CMD_TEXT_TO_SPEECH)) {
                        c = 21;
                        break;
                    }
                    break;
                case 2076411034:
                    if (name.equals(RobotCoreCommandList.CMD_RUMBLE_GAMEPAD)) {
                        c = 23;
                        break;
                    }
                    break;
            }
            switch (c) {
                case 0:
                    return handleNotifyRobotState(extra);
                case 1:
                    return handleCommandNotifyOpModeList(extra);
                case 2:
                    return handleCommandNotifyUserDeviceList(extra);
                case 3:
                    return handleCommandNotifyActiveConfig(extra);
                case 4:
                    return handleCommandNotifyInitOpMode(extra);
                case 5:
                    return handleCommandNotifyStartOpMode(extra);
                case 6:
                    return handleCommandShowToast(extra);
                case 7:
                    return handleCommandShowProgress(extra);
                case 8:
                    return handleCommandDismissProgress();
                case 9:
                    return handleCommandShowStacktrace(command);
                case 10:
                    return handleCommandShowDialog(extra);
                case 11:
                    return handleCommandDismissDialog(command);
                case 12:
                    return handleCommandDismissAllDialogs(command);
                case 13:
                    return handleCommandStartProgramAndManageResp(extra);
                case 14:
                    return PreferenceRemoterDS.getInstance().handleCommandRobotControllerPreference(extra);
                case 15:
                    return SoundPlayer.getInstance().handleCommandPlaySound(extra);
                case 16:
                    return SoundPlayer.getInstance().handleCommandRequestSound(command);
                case 17:
                    return SoundPlayer.getInstance().handleCommandStopPlayingSounds(command);
                case 18:
                    return CameraStreamClient.getInstance().handleStreamChange(extra);
                case 19:
                    return CameraStreamClient.getInstance().handleReceiveFrameBegin(extra);
                case 20:
                    return CameraStreamClient.getInstance().handleReceiveFrameChunk(extra);
                case 21:
                    return handleCommandTextToSpeech(extra);
                case 22:
                    CallbackResult handleCommandSetTelemetryDisplayFormat = handleCommandSetTelemetryDisplayFormat(extra);
                    break;
                case 23:
                    break;
                case 24:
                    return handleCommandRunLedEffect(extra);
                default:
                    return callbackResult;
            }
            return handleCommandRunRumbleEffect(extra);
        } catch (Exception e) {
            RobotLog.logStackTrace(e);
            return callbackResult;
        }
    }

    public CallbackResult telemetryEvent(RobocolDatagram robocolDatagram) {
        StringBuilder sb = new StringBuilder();
        try {
            TelemetryMessage telemetryMessage = new TelemetryMessage(robocolDatagram.getData());
            if (telemetryMessage.getRobotState() != RobotState.UNKNOWN) {
                setRobotState(telemetryMessage.getRobotState());
            }
            Map<String, String> dataStrings = telemetryMessage.getDataStrings();
            boolean z = false;
            for (String next : telemetryMessage.isSorted() ? new TreeSet<>(dataStrings.keySet()) : dataStrings.keySet()) {
                if (next.equals(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY)) {
                    showRobotBatteryVoltage(dataStrings.get(next));
                } else {
                    if (next.length() > 0 && next.charAt(0) != 0) {
                        sb.append(next);
                        sb.append(": ");
                    }
                    sb.append(dataStrings.get(next));
                    sb.append("\n");
                    z = true;
                }
            }
            sb.append("\n");
            Map<String, Float> dataNumbers = telemetryMessage.getDataNumbers();
            for (String next2 : telemetryMessage.isSorted() ? new TreeSet<>(dataNumbers.keySet()) : dataNumbers.keySet()) {
                if (next2.length() > 0 && next2.charAt(0) != 0) {
                    sb.append(next2);
                    sb.append(": ");
                }
                sb.append(dataNumbers.get(next2));
                sb.append("\n");
                z = true;
            }
            String tag = telemetryMessage.getTag();
            if (tag.equals(EventLoopManager.SYSTEM_NONE_KEY)) {
                clearSystemTelemetry();
            } else if (tag.equals(EventLoopManager.SYSTEM_ERROR_KEY)) {
                reportGlobalError(dataStrings.get(tag), true);
            } else if (tag.equals(EventLoopManager.SYSTEM_WARNING_KEY)) {
                reportGlobalWarning(dataStrings.get(tag));
            } else if (tag.equals(EventLoopManager.RC_BATTERY_STATUS_KEY)) {
                updateRcBatteryStatus(BatteryChecker.BatteryStatus.deserialize(dataStrings.get(tag)));
            } else if (tag.equals(EventLoopManager.ROBOT_BATTERY_LEVEL_KEY)) {
                showRobotBatteryVoltage(dataStrings.get(tag));
            } else if (z) {
                setUserTelemetry(sb.toString());
            }
            return CallbackResult.HANDLED;
        } catch (RobotCoreException e) {
            RobotLog.logStackTrace(e);
            return CallbackResult.HANDLED;
        }
    }

    /* access modifiers changed from: protected */
    public void showRobotBatteryVoltage(String str) {
        String str2 = str;
        RelativeLayout relativeLayout = (RelativeLayout) findViewById(C0648R.C0650id.robot_battery_background);
        View findViewById = findViewById(C0648R.C0650id.rc_battery_layout);
        TextView textView = (TextView) findViewById(C0648R.C0650id.rc_no_voltage_sensor);
        if (str2.equals(FtcEventLoopHandler.NO_VOLTAGE_SENSOR)) {
            setVisibility(findViewById, 8);
            setVisibility(textView, 0);
            resetBatteryStats();
            setBG(relativeLayout, findViewById(C0648R.C0650id.rcBatteryBackgroundReference).getBackground());
            return;
        }
        setVisibility(findViewById, 0);
        setVisibility(textView, 8);
        double doubleValue = Double.valueOf(str).doubleValue();
        if (doubleValue < this.V12BatteryMin) {
            this.V12BatteryMin = doubleValue;
            this.V12BatteryMinString = str2;
        }
        TextView textView2 = this.robotBatteryTelemetry;
        setTextView(textView2, str2 + " V");
        TextView textView3 = this.robotBatteryMinimum;
        setTextView(textView3, "( " + this.V12BatteryMinString + " V )");
        double d = (double) 10.0f;
        double d2 = (double) 14.0f;
        setBGColor(relativeLayout, Color.HSVToColor(new float[]{(float) Range.scale(Range.clip(doubleValue, d, d2), d, d2, (double) 0.0f, (double) 128.0f), 1.0f, 0.6f}));
    }

    /* access modifiers changed from: protected */
    public void setBGColor(final View view, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setBackgroundColor(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setBG(final View view, final Drawable drawable) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setBackground(drawable);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void resetBatteryStats() {
        this.V12BatteryMin = Double.POSITIVE_INFINITY;
        this.V12BatteryMinString = InspectionState.NO_VERSION;
    }

    /* access modifiers changed from: protected */
    public void setUserTelemetry(String str) {
        int i = C060650.f71x7538999a[this.telemetryMode.ordinal()];
        if (i == 1 || i == 2) {
            setTextView(this.textTelemetry, str);
        } else if (i == 3) {
            setTextView(this.textTelemetry, Html.fromHtml(str.replace("\n", "<br>")));
        }
    }

    /* access modifiers changed from: protected */
    public void clearUserTelemetry() {
        setTextView(this.textTelemetry, InspectionState.NO_VERSION);
    }

    /* access modifiers changed from: protected */
    public void clearSystemTelemetry() {
        setVisibility(this.systemTelemetry, 8);
        setTextView(this.systemTelemetry, InspectionState.NO_VERSION);
        setTextColor(this.systemTelemetry, this.systemTelemetryOriginalColor);
        RobotLog.clearGlobalErrorMsg();
        RobotLog.clearGlobalWarningMsg();
    }

    public CallbackResult gamepadEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult emptyEvent(RobocolDatagram robocolDatagram) {
        return CallbackResult.NOT_HANDLED;
    }

    public CallbackResult reportGlobalError(String str, boolean z) {
        brightenTelemetryRegion();
        if (!RobotLog.getGlobalErrorMsg().equals(str)) {
            RobotLog.m48ee(TAG, "System telemetry error: " + str);
            RobotLog.clearGlobalErrorMsg();
            RobotLog.setGlobalErrorMsg(str);
        }
        TextView textView = this.systemTelemetry;
        AppUtil.getInstance();
        setTextColor(textView, AppUtil.getColor(C0648R.color.text_error));
        setVisibility(this.systemTelemetry, 0);
        StringBuilder sb = new StringBuilder();
        RobotState robotState2 = this.robotState;
        if (!(robotState2 == null || robotState2 == RobotState.UNKNOWN)) {
            sb.append(String.format(getString(C0648R.string.dsRobotStatus), new Object[]{this.robotState.toString(this)}));
        }
        if (z) {
            sb.append(getString(C0648R.string.dsToAttemptRecovery));
        }
        sb.append(String.format(getString(C0648R.string.dsErrorMessage), new Object[]{str}));
        setTextView(this.systemTelemetry, sb.toString());
        stopTimerAndReset();
        uiRobotCantContinue();
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void reportGlobalWarning(String str) {
        if (!RobotLog.getGlobalWarningMessage().equals(str)) {
            RobotLog.m48ee(TAG, "System telemetry warning: " + str);
            RobotLog.clearGlobalWarningMsg();
            RobotLog.addGlobalWarningMessage(str);
        }
        TextView textView = this.systemTelemetry;
        AppUtil.getInstance();
        setTextColor(textView, AppUtil.getColor(C0648R.color.text_warning));
        setVisibility(this.systemTelemetry, 0);
        setTextView(this.systemTelemetry, str);
    }

    /* access modifiers changed from: protected */
    public void uiRobotCantContinue() {
        traceUiStateChange("ui:uiRobotCantContinue", UIState.CANT_CONTINUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
        dimControlPanelBack();
    }

    /* access modifiers changed from: protected */
    public void disableOpModeControls() {
        setEnabled(this.buttonInit, false);
        setVisibility(this.buttonInit, 0);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 4);
        setVisibility(this.timerAndTimerSwitch, 4);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void dimAndDisableAllControls() {
        dimControlPanelBack();
        setOpacity(this.wifiInfo, PARTLY_OPAQUE);
        setOpacity(this.batteryInfo, PARTLY_OPAQUE);
        disableAndDimOpModeMenu();
        disableOpModeControls();
        setOpacity(this.telemetryRegion, PARTLY_OPAQUE);
        if (this.landscape) {
            setOpacity(this.configAndTimerRegion, PARTLY_OPAQUE);
        }
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsDisconnected() {
        traceUiStateChange("ui:uiRobotControllerIsDisconnected", UIState.DISCONNECTED);
        dimAndDisableAllControls();
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.rcObsoleteWarning.setVisibility(8);
            }
        });
        if (this.landscape) {
            runOnUiThread(new Runnable() {
                public void run() {
                    FtcDriverStationActivity.this.headerColorRight.setBackground(FtcDriverStationActivity.this.getResources().getDrawable(C0648R.C0649drawable.lds_header_shadow_disconnected));
                    FtcDriverStationActivity.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.lds_header_red_gradient_start));
                    FtcDriverStationActivity.this.textWifiDirectStatus.setText("Disconnected");
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void uiRobotControllerIsConnected(ControlPanelBack controlPanelBack2) {
        traceUiStateChange("ui:uiRobotControllerIsConnected", UIState.CONNNECTED);
        enableAndBrightenForConnected(controlPanelBack2);
        AppUtil.getInstance().dismissAllDialogs(UILocation.ONLY_LOCAL);
        AppUtil.getInstance().dismissProgress(UILocation.ONLY_LOCAL);
        setTextView(this.rcBatteryTelemetry, InspectionState.NO_VERSION);
        setTextView(this.robotBatteryTelemetry, InspectionState.NO_VERSION);
        showWifiChannel();
        hideCameraStream();
        if (this.landscape) {
            runOnUiThread(new Runnable() {
                public void run() {
                    FtcDriverStationActivity.this.headerColorRight.setBackground(FtcDriverStationActivity.this.getResources().getDrawable(C0648R.C0649drawable.lds_header_shadow_connected));
                    FtcDriverStationActivity.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.lds_header_green_gradient_start));
                    FtcDriverStationActivity.this.textWifiDirectStatus.setText("Robot Connected");
                }
            });
        }
    }

    /* access modifiers changed from: protected */
    public void enableAndBrightenForConnected(ControlPanelBack controlPanelBack2) {
        setControlPanelBack(controlPanelBack2);
        setOpacity(this.wifiInfo, 1.0f);
        setOpacity(this.batteryInfo, 1.0f);
        enableAndBrightenOpModeMenu();
        brightenTelemetryRegion();
        if (this.landscape) {
            setOpacity(this.configAndTimerRegion, 1.0f);
        }
    }

    /* access modifiers changed from: protected */
    public void checkConnectedEnableBrighten(ControlPanelBack controlPanelBack2) {
        if (!this.clientConnected) {
            RobotLog.m60vv(TAG, "auto-rebrightening for connected state");
            enableAndBrightenForConnected(controlPanelBack2);
            setClientConnected(true);
            requestUIState();
        }
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForOpModeSelection() {
        traceUiStateChange("ui:uiWaitingForOpModeSelection", UIState.WAITING_FOR_OPMODE_SELECTION);
        checkConnectedEnableBrighten(ControlPanelBack.DIM);
        dimControlPanelBack();
        enableAndBrightenOpModeMenu();
        showQueuedOpModeName();
        disableOpModeControls();
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForInitEvent() {
        traceUiStateChange("ui:uiWaitingForInitEvent", UIState.WAITING_FOR_INIT_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        brightenControlPanelBack();
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setEnabled(this.buttonInit, true);
        setVisibility(this.buttonInit, 0);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 4);
        setTimerButtonEnabled(true);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void setTimerButtonEnabled(boolean z) {
        setEnabled(this.timerAndTimerSwitch, z);
        setEnabled(findViewById(C0648R.C0650id.timerBackground), z);
        setEnabled(findViewById(C0648R.C0650id.timerStopWatch), z);
        setEnabled(findViewById(C0648R.C0650id.timerText), z);
        setEnabled(findViewById(C0648R.C0650id.timerSwitchOn), z);
        setEnabled(findViewById(C0648R.C0650id.timerSwitchOff), z);
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForStartEvent() {
        traceUiStateChange("ui:uiWaitingForStartEvent", UIState.WAITING_FOR_START_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(this.buttonStart, 0);
        setVisibility(this.buttonInit, 4);
        setVisibility(this.buttonStop, 4);
        setVisibility(this.buttonInitStop, 0);
        setTimerButtonEnabled(true);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public void uiWaitingForStopEvent() {
        traceUiStateChange("ui:uiWaitingForStopEvent", UIState.WAITING_FOR_STOP_EVENT);
        checkConnectedEnableBrighten(ControlPanelBack.BRIGHT);
        showQueuedOpModeName();
        enableAndBrightenOpModeMenu();
        setVisibility(this.buttonStop, 0);
        setVisibility(this.buttonInit, 4);
        setVisibility(this.buttonStart, 4);
        setVisibility(this.buttonInitStop, 4);
        setTimerButtonEnabled(false);
        setVisibility(this.timerAndTimerSwitch, 0);
        hideCameraStream();
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultOpMode(String str) {
        return this.defaultOpMode.name.equals(str);
    }

    /* access modifiers changed from: protected */
    public boolean isDefaultOpMode(OpModeMeta opModeMeta) {
        return isDefaultOpMode(opModeMeta.name);
    }

    /* access modifiers changed from: protected */
    public OpModeMeta getOpModeMeta(String str) {
        synchronized (this.opModes) {
            for (OpModeMeta next : this.opModes) {
                if (next.name.equals(str)) {
                    return next;
                }
            }
            return new OpModeMeta.Builder().setName(str).build();
        }
    }

    /* access modifiers changed from: protected */
    public void showQueuedOpModeName() {
        showQueuedOpModeName(this.queuedOpMode);
    }

    /* access modifiers changed from: protected */
    public void showQueuedOpModeName(OpModeMeta opModeMeta) {
        if (isDefaultOpMode(opModeMeta)) {
            setVisibility(this.currentOpModeName, 8);
            setVisibility(this.chooseOpModePrompt, 0);
            return;
        }
        setTextView(this.currentOpModeName, opModeMeta.name);
        setVisibility(this.currentOpModeName, 0);
        setVisibility(this.chooseOpModePrompt, 8);
    }

    /* access modifiers changed from: protected */
    public void traceUiStateChange(String str, UIState uIState) {
        RobotLog.m60vv(TAG, str);
        this.uiState = uIState;
        setTextView(this.textDsUiStateIndicator, uIState.indicator);
        invalidateOptionsMenu();
    }

    /* access modifiers changed from: protected */
    public void assumeClientConnectAndRefreshUI(ControlPanelBack controlPanelBack2) {
        assumeClientConnect(controlPanelBack2);
        requestUIState();
    }

    /* access modifiers changed from: protected */
    public void assumeClientConnect(ControlPanelBack controlPanelBack2) {
        RobotLog.m60vv(TAG, "Assuming client connected");
        if (this.uiState == UIState.UNKNOWN || this.uiState == UIState.DISCONNECTED || this.uiState == UIState.CANT_CONTINUE) {
            setClientConnected(true);
            uiRobotControllerIsConnected(controlPanelBack2);
        }
    }

    /* access modifiers changed from: protected */
    public void assumeClientDisconnect() {
        RobotLog.m60vv(TAG, "Assuming client disconnected");
        setClientConnected(false);
        this.receivedPeerDiscoveryFromCurrentPeer = false;
        enableAndResetTimer(false);
        this.opModeCountDown.disable();
        this.queuedOpMode = this.defaultOpMode;
        this.opModes.clear();
        pingStatus((int) C0648R.string.ping_status_no_heartbeat);
        stopKeepAlives();
        this.networkConnectionHandler.clientDisconnect();
        RobocolParsableBase.initializeSequenceNumber(10000);
        RobotLog.clearGlobalErrorMsg();
        setRobotState(RobotState.UNKNOWN);
        uiRobotControllerIsDisconnected();
    }

    /* access modifiers changed from: protected */
    public boolean setClientConnected(boolean z) {
        boolean z2 = this.clientConnected;
        this.clientConnected = z;
        this.preferencesHelper.writeBooleanPrefIfDifferent(getString(C0648R.string.pref_rc_connected), z);
        return z2;
    }

    /* access modifiers changed from: protected */
    public void handleOpModeQueued(OpModeMeta opModeMeta) {
        if (setQueuedOpModeIfDifferent(opModeMeta)) {
            enableAndResetTimerForQueued();
        }
        uiWaitingForInitEvent();
        if (opModeMeta.flavor == OpModeMeta.Flavor.AUTONOMOUS) {
            clearGuiTelePreselection();
            showLayoutGuiTelePreselection();
            if (opModeMeta.autoTransition != null) {
                for (OpModeMeta next : this.opModes) {
                    if (next.name.equals(opModeMeta.autoTransition) && this.annotationAutomagicPreselect) {
                        configureGuiTelePreselectionFor(next);
                    }
                }
                return;
            }
            return;
        }
        clearGuiTelePreselection();
        hideLayoutGuiTelePreselection();
    }

    /* access modifiers changed from: protected */
    public boolean setQueuedOpModeIfDifferent(String str) {
        return setQueuedOpModeIfDifferent(getOpModeMeta(str));
    }

    /* access modifiers changed from: protected */
    public boolean setQueuedOpModeIfDifferent(OpModeMeta opModeMeta) {
        if (opModeMeta.name.equals(this.queuedOpMode.name)) {
            return false;
        }
        this.queuedOpMode = opModeMeta;
        showQueuedOpModeName();
        return true;
    }

    /* access modifiers changed from: protected */
    public int validateMatchEntry(String str) {
        try {
            int parseInt = Integer.parseInt(str);
            if (parseInt < 0 || parseInt > 1000) {
                return -1;
            }
            return parseInt;
        } catch (NumberFormatException e) {
            RobotLog.logStackTrace(e);
            return -1;
        }
    }

    /* access modifiers changed from: protected */
    public void sendMatchNumber(int i) {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_SET_MATCH_NUMBER, String.valueOf(i)));
    }

    /* access modifiers changed from: protected */
    public void sendMatchNumberIfNecessary() {
        try {
            sendMatchNumber(getMatchNumber());
        } catch (NumberFormatException unused) {
            sendMatchNumber(0);
        }
    }

    /* access modifiers changed from: protected */
    public int getMatchNumber() throws NumberFormatException {
        if (this.landscape) {
            return Integer.parseInt(this.matchNumTxtView.getText().toString());
        }
        return Integer.parseInt(this.matchNumField.getText().toString());
    }

    /* access modifiers changed from: protected */
    public void clearMatchNumberIfNecessary() {
        if (this.queuedOpMode.flavor == OpModeMeta.Flavor.TELEOP) {
            clearMatchNumber();
        }
    }

    /* access modifiers changed from: protected */
    public void clearMatchNumber() {
        if (this.landscape) {
            this.matchNumTxtView.setText("NONE");
        } else {
            this.matchNumField.setText(InspectionState.NO_VERSION);
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeInit() {
        if (this.uiState == UIState.WAITING_FOR_INIT_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            sendMatchNumberIfNecessary();
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, this.queuedOpMode.name));
            if (!this.queuedOpMode.name.equals(this.defaultOpMode.name)) {
                this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.RUNNING_OPMODE);
            }
            hideCameraStream();
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeStart() {
        if (this.uiState == UIState.WAITING_FOR_START_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, this.queuedOpMode.name));
        }
    }

    /* access modifiers changed from: protected */
    public void handleOpModeStop(boolean z) {
        this.pendingStopFromTimer = z;
        if (this.uiState == UIState.WAITING_FOR_START_EVENT || this.uiState == UIState.WAITING_FOR_STOP_EVENT) {
            traceUiStateChange("ui:uiWaitingForAck", UIState.WAITING_FOR_ACK);
            clearMatchNumberIfNecessary();
            initDefaultOpMode();
            this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
        }
    }

    /* access modifiers changed from: protected */
    public void initDefaultOpMode() {
        this.stopRequestedOnDsSide = true;
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_INIT_OP_MODE, this.defaultOpMode.name));
    }

    /* access modifiers changed from: protected */
    public void runDefaultOpMode() {
        this.networkConnectionHandler.sendCommand(new Command(CommandList.CMD_RUN_OP_MODE, this.defaultOpMode.name));
        this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.STOPPED_OPMODE);
    }

    /* access modifiers changed from: protected */
    public void handlePreselectionAutoTransition() {
        if (!this.defaultOpModeRunning) {
            synchronized (this.opModes) {
                OpModeMeta opModeMeta = this.guiPreselectedTeleop;
                if (opModeMeta != null) {
                    handleOpModeQueued(opModeMeta);
                }
            }
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyInitOpMode(String str) {
        if (this.uiState == UIState.CANT_CONTINUE) {
            return CallbackResult.HANDLED;
        }
        RobotLog.m60vv(TAG, "Robot Controller initializing op mode: " + str);
        stopTimerPreservingRemainingTime();
        if (isDefaultOpMode(str)) {
            this.androidTextToSpeech.stop();
            stopKeepAlives();
            runOnUiThread(new Runnable() {
                public void run() {
                    FtcDriverStationActivity.this.telemetryMode = Telemetry.DisplayFormat.CLASSIC;
                    FtcDriverStationActivity.this.textTelemetry.setTypeface(Typeface.DEFAULT);
                }
            });
            handleDefaultOpModeInitOrStart(false);
            this.defaultOpModeRunning = true;
        } else {
            clearUserTelemetry();
            startKeepAlives();
            if (setQueuedOpModeIfDifferent(str)) {
                RobotLog.m60vv(TAG, "timer: init new opmode");
                enableAndResetTimerForQueued();
            } else if (this.opModeCountDown.isEnabled()) {
                RobotLog.m60vv(TAG, "timer: init w/ timer enabled");
                this.opModeCountDown.resetCountdown();
            } else {
                RobotLog.m60vv(TAG, "timer: init w/o timer enabled");
            }
            uiWaitingForStartEvent();
            this.defaultOpModeRunning = false;
            this.suppressRumbleCommands = false;
            this.suppressLedCommands = false;
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyStartOpMode(String str) {
        if (this.uiState == UIState.CANT_CONTINUE) {
            return CallbackResult.HANDLED;
        }
        RobotLog.m60vv(TAG, "Robot Controller starting op mode: " + str);
        if (isDefaultOpMode(str)) {
            this.androidTextToSpeech.stop();
            stopKeepAlives();
            handleDefaultOpModeInitOrStart(true);
            this.defaultOpModeRunning = true;
        } else {
            if (setQueuedOpModeIfDifferent(str)) {
                RobotLog.m60vv(TAG, "timer: started new opmode: auto-initing timer");
                enableAndResetTimerForQueued();
            }
            uiWaitingForStopEvent();
            if (this.opModeUseTimer) {
                this.opModeCountDown.start();
            } else {
                stopTimerAndReset();
            }
            this.defaultOpModeRunning = false;
            this.suppressRumbleCommands = false;
            this.suppressLedCommands = false;
        }
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public void handleDefaultOpModeInitOrStart(boolean z) {
        this.suppressRumbleCommands = true;
        this.suppressLedCommands = true;
        gamepadManager.stopGamepadRumble();
        gamepadManager.resetLedsForBindStatus();
        if (isDefaultOpMode(this.queuedOpMode)) {
            uiWaitingForOpModeSelection();
        } else {
            uiWaitingForInitEvent();
            if (!z) {
                runDefaultOpMode();
            }
        }
        if (!this.stopRequestedOnDsSide || this.pendingStopFromTimer) {
            handlePreselectionAutoTransition();
        }
        this.pendingStopFromTimer = false;
        this.stopRequestedOnDsSide = false;
    }

    /* access modifiers changed from: protected */
    public void requestUIState() {
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_ACTIVE_CONFIG));
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_USER_DEVICE_TYPES));
        this.networkConnectionHandler.sendCommand(new Command(RobotCoreCommandList.CMD_REQUEST_OP_MODE_LIST));
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyOpModeList(String str) {
        assumeClientConnect(ControlPanelBack.NO_CHANGE);
        this.opModes = (List) new Gson().fromJson(str, new TypeToken<Collection<OpModeMeta>>() {
        }.getType());
        RobotLog.m60vv(TAG, "Received the following op modes: " + this.opModes.toString());
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyUserDeviceList(String str) {
        ConfigurationTypeManager.getInstance().deserializeUserDeviceTypes(str);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyActiveConfig(String str) {
        RobotLog.m61vv(TAG, "%s.handleCommandRequestActiveConfigResp(%s)", getClass().getSimpleName(), str);
        final RobotConfigFile configFromString = this.robotConfigFileManager.getConfigFromString(str);
        this.robotConfigFileManager.setActiveConfig(configFromString);
        this.appUtil.runOnUiThread(this, new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.activeConfigText.setText(configFromString.getName());
            }
        });
        return CallbackResult.HANDLED_CONTINUE;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowToast(String str) {
        RobotCoreCommandList.ShowToast deserialize = RobotCoreCommandList.ShowToast.deserialize(str);
        this.appUtil.showToast(UILocation.ONLY_LOCAL, deserialize.message, deserialize.duration);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowProgress(String str) {
        RobotCoreCommandList.ShowProgress deserialize = RobotCoreCommandList.ShowProgress.deserialize(str);
        this.appUtil.showProgress(UILocation.ONLY_LOCAL, deserialize.message, (ProgressParameters) deserialize);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissProgress() {
        this.appUtil.dismissProgress(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowStacktrace(final Command command) {
        AppUtil.getInstance().runOnUiThread(new Runnable() {
            public void run() {
                Intent intent = new Intent(FtcDriverStationActivity.this, StackTraceActivity.class);
                intent.putExtra(StackTraceActivity.KEY_STACK_TRACE, command.getExtra());
                FtcDriverStationActivity.this.startActivity(intent);
            }
        });
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandShowDialog(String str) {
        RobotCoreCommandList.ShowDialog deserialize = RobotCoreCommandList.ShowDialog.deserialize(str);
        AppUtil.DialogParams dialogParams = new AppUtil.DialogParams(UILocation.ONLY_LOCAL, deserialize.title, deserialize.message);
        dialogParams.uuidString = deserialize.uuidString;
        this.appUtil.showDialog(dialogParams);
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissDialog(Command command) {
        this.appUtil.dismissDialog(UILocation.ONLY_LOCAL, RobotCoreCommandList.DismissDialog.deserialize(command.getExtra()));
        return CallbackResult.HANDLED;
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandDismissAllDialogs(Command command) {
        this.appUtil.dismissAllDialogs(UILocation.ONLY_LOCAL);
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandStartProgramAndManageResp(String str) {
        if (str != null && !str.isEmpty()) {
            Intent intent = new Intent(AppUtil.getDefContext(), ProgramAndManageActivity.class);
            intent.putExtra(LaunchActivityConstantsList.RC_WEB_INFO, str);
            startActivityForResult(intent, LaunchActivityConstantsList.RequestCode.PROGRAM_AND_MANAGE.ordinal());
        }
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandSetTelemetryDisplayFormat(String str) {
        try {
            Telemetry.DisplayFormat valueOf = Telemetry.DisplayFormat.valueOf(str);
            if (valueOf != this.telemetryMode) {
                int i = C060650.f71x7538999a[valueOf.ordinal()];
                if (i == 1) {
                    this.textTelemetry.setTypeface(Typeface.MONOSPACE);
                } else if (i == 2 || i == 3) {
                    this.textTelemetry.setTypeface(Typeface.DEFAULT);
                }
            }
            this.telemetryMode = valueOf;
        } catch (IllegalArgumentException unused) {
        }
        return CallbackResult.HANDLED;
    }

    private CallbackResult handleCommandTextToSpeech(String str) {
        RobotCoreCommandList.TextToSpeech deserialize = RobotCoreCommandList.TextToSpeech.deserialize(str);
        String text = deserialize.getText();
        String languageCode = deserialize.getLanguageCode();
        String countryCode = deserialize.getCountryCode();
        if (languageCode != null && !languageCode.isEmpty()) {
            if (countryCode == null || countryCode.isEmpty()) {
                this.androidTextToSpeech.setLanguage(languageCode);
            } else {
                this.androidTextToSpeech.setLanguageAndCountry(languageCode, countryCode);
            }
        }
        this.androidTextToSpeech.speak(text);
        return CallbackResult.HANDLED;
    }

    public void onClickRCBatteryToast(View view) {
        showToast(getString(C0648R.string.toastRobotControllerBattery));
    }

    public void onClickRobotBatteryToast(View view) {
        resetBatteryStats();
        showToast(getString(C0648R.string.toastRobotBattery));
    }

    public void onClickDSBatteryToast(View view) {
        showToast(getString(C0648R.string.toastDriverStationBattery));
    }

    /* access modifiers changed from: protected */
    public void showWifiStatus(boolean z, String str) {
        if (this.landscape) {
            showWifiStatusLandscape(z, str);
        } else {
            showWifiStatusPortrait(z, str);
        }
    }

    /* access modifiers changed from: protected */
    public void showWifiStatusPortrait(final boolean z, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.textWifiDirectStatusShowingRC = z;
                FtcDriverStationActivity.this.textWifiDirectStatus.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showWifiStatusLandscape(final boolean z, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.textWifiDirectStatusShowingRC = z;
                FtcDriverStationActivity.this.textWifiDirectStatus.setText(str);
                if (str.equals(FtcDriverStationActivity.this.getString(C0648R.string.wifiStatusDisconnected)) || str.equals(FtcDriverStationActivity.this.getString(C0648R.string.actionlistenerfailure_busy)) || str.equals(FtcDriverStationActivity.this.getString(C0648R.string.wifiStatusNotPaired))) {
                    FtcDriverStationActivity.this.headerColorRight.setBackground(FtcDriverStationActivity.this.getResources().getDrawable(C0648R.C0649drawable.lds_header_shadow_disconnected));
                    FtcDriverStationActivity.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.lds_header_red_gradient_start));
                } else if (str.equals(FtcDriverStationActivity.this.getString(C0648R.string.wifiStatusConnecting)) || str.equals(FtcDriverStationActivity.this.getString(C0648R.string.wifiStatusSearching))) {
                    FtcDriverStationActivity.this.headerColorRight.setBackground(FtcDriverStationActivity.this.getResources().getDrawable(C0648R.C0649drawable.lds_header_shadow_connecting));
                    FtcDriverStationActivity.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.lds_header_yellow_gradient_start));
                } else {
                    FtcDriverStationActivity.this.textWifiDirectStatus.setText("Robot Connected");
                    String str = str;
                    if (str.contains("DIRECT-") && str.contains("RC")) {
                        str = str.substring(10);
                    }
                    TextView textView = FtcDriverStationActivity.this.network_ssid;
                    textView.setText("Network: " + str);
                    FtcDriverStationActivity.this.headerColorRight.setBackground(FtcDriverStationActivity.this.getResources().getDrawable(C0648R.C0649drawable.lds_header_shadow_connected));
                    FtcDriverStationActivity.this.headerColorLeft.setBackgroundColor(FtcDriverStationActivity.this.getResources().getColor(C0648R.color.lds_header_green_gradient_start));
                }
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showWifiChannel() {
        runOnUiThread(new Runnable() {
            public void run() {
                if (FtcDriverStationActivity.this.networkConnectionHandler.getWifiChannel() > 0) {
                    FtcDriverStationActivity.this.textWifiChannel.setText("ch " + FtcDriverStationActivity.this.networkConnectionHandler.getWifiChannel());
                    FtcDriverStationActivity.this.textWifiChannel.setVisibility(0);
                    return;
                }
                int i = FtcDriverStationActivity.this.preferences.getInt(FtcDriverStationActivity.this.getString(C0648R.string.pref_wifip2p_channel), -1);
                if (i == -1) {
                    RobotLog.m60vv(FtcDriverStationActivity.TAG, "pref_wifip2p_channel: showWifiChannel prefChannel not found");
                    FtcDriverStationActivity.this.textWifiChannel.setVisibility(8);
                    return;
                }
                RobotLog.m61vv(FtcDriverStationActivity.TAG, "pref_wifip2p_channel: showWifiChannel prefChannel = %d", Integer.valueOf(i));
                FtcDriverStationActivity.this.textWifiChannel.setText("ch " + Integer.toString(i));
                FtcDriverStationActivity.this.textWifiChannel.setVisibility(0);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void showBytesPerSecond(final long j) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.textBytesPerSecond.setText(String.valueOf(j));
            }
        });
    }

    protected class DeviceNameManagerCallback implements DeviceNameListener {
        protected DeviceNameManagerCallback() {
        }

        public void onDeviceNameChanged(String str) {
            FtcDriverStationActivity.this.displayDeviceName(str);
        }
    }

    /* access modifiers changed from: protected */
    public void displayDeviceName(final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.textDeviceName.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void assertUiThread() {
        Assert.assertTrue(Thread.currentThread() == this.uiThread);
    }

    /* access modifiers changed from: protected */
    public void setButtonText(final Button button, final String str) {
        runOnUiThread(new Runnable() {
            public void run() {
                button.setText(str);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setTextView(final TextView textView, final CharSequence charSequence) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setText(charSequence);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setTextColor(final TextView textView, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                textView.setTextColor(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setOpacity(final View view, final float f) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setAlpha(f);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setImageResource(final ImageButton imageButton, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                imageButton.setImageResource(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setVisibility(final View view, final int i) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setVisibility(i);
            }
        });
    }

    /* access modifiers changed from: protected */
    public void setEnabled(final View view, final boolean z) {
        runOnUiThread(new Runnable() {
            public void run() {
                view.setEnabled(z);
            }
        });
    }

    /* renamed from: com.qualcomm.ftcdriverstation.FtcDriverStationActivity$50 */
    static /* synthetic */ class C060650 {

        /* renamed from: $SwitchMap$com$qualcomm$ftcdriverstation$FtcDriverStationActivity$ControlPanelBack */
        static final /* synthetic */ int[] f69xe50949a5;

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent */
        static final /* synthetic */ int[] f70x94151df2;

        /* renamed from: $SwitchMap$org$firstinspires$ftc$robotcore$external$Telemetry$DisplayFormat */
        static final /* synthetic */ int[] f71x7538999a;

        /* JADX WARNING: Can't wrap try/catch for region: R(28:0|(2:1|2)|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|(3:33|34|36)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(29:0|1|2|3|(2:5|6)|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|(3:33|34|36)) */
        /* JADX WARNING: Can't wrap try/catch for region: R(32:0|1|2|3|5|6|7|9|10|11|13|14|15|16|17|18|19|21|22|23|24|25|26|27|28|29|30|31|32|33|34|36) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0039 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:17:0x0043 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:23:0x005e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:25:0x0068 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:27:0x0072 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:29:0x007d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:31:0x0088 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:33:0x0093 */
        static {
            /*
                com.qualcomm.ftcdriverstation.FtcDriverStationActivity$ControlPanelBack[] r0 = com.qualcomm.ftcdriverstation.FtcDriverStationActivity.ControlPanelBack.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f69xe50949a5 = r0
                r1 = 1
                com.qualcomm.ftcdriverstation.FtcDriverStationActivity$ControlPanelBack r2 = com.qualcomm.ftcdriverstation.FtcDriverStationActivity.ControlPanelBack.NO_CHANGE     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r2 = r2.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r0[r2] = r1     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                r0 = 2
                int[] r2 = f69xe50949a5     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.ftcdriverstation.FtcDriverStationActivity$ControlPanelBack r3 = com.qualcomm.ftcdriverstation.FtcDriverStationActivity.ControlPanelBack.DIM     // Catch:{ NoSuchFieldError -> 0x001d }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2[r3] = r0     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                r2 = 3
                int[] r3 = f69xe50949a5     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.ftcdriverstation.FtcDriverStationActivity$ControlPanelBack r4 = com.qualcomm.ftcdriverstation.FtcDriverStationActivity.ControlPanelBack.BRIGHT     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat[] r3 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                f71x7538999a = r3
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.MONOSPACE     // Catch:{ NoSuchFieldError -> 0x0039 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0039 }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x0039 }
            L_0x0039:
                int[] r3 = f71x7538999a     // Catch:{ NoSuchFieldError -> 0x0043 }
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.CLASSIC     // Catch:{ NoSuchFieldError -> 0x0043 }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x0043 }
                r3[r4] = r0     // Catch:{ NoSuchFieldError -> 0x0043 }
            L_0x0043:
                int[] r3 = f71x7538999a     // Catch:{ NoSuchFieldError -> 0x004d }
                org.firstinspires.ftc.robotcore.external.Telemetry$DisplayFormat r4 = org.firstinspires.ftc.robotcore.external.Telemetry.DisplayFormat.HTML     // Catch:{ NoSuchFieldError -> 0x004d }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x004d }
                r3[r4] = r2     // Catch:{ NoSuchFieldError -> 0x004d }
            L_0x004d:
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent[] r3 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.values()
                int r3 = r3.length
                int[] r3 = new int[r3]
                f70x94151df2 = r3
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r4 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.PEERS_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x005e }
                int r4 = r4.ordinal()     // Catch:{ NoSuchFieldError -> 0x005e }
                r3[r4] = r1     // Catch:{ NoSuchFieldError -> 0x005e }
            L_0x005e:
                int[] r1 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x0068 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r3 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_GROUP_OWNER     // Catch:{ NoSuchFieldError -> 0x0068 }
                int r3 = r3.ordinal()     // Catch:{ NoSuchFieldError -> 0x0068 }
                r1[r3] = r0     // Catch:{ NoSuchFieldError -> 0x0068 }
            L_0x0068:
                int[] r0 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x0072 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTING     // Catch:{ NoSuchFieldError -> 0x0072 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0072 }
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0072 }
            L_0x0072:
                int[] r0 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x007d }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_PEER     // Catch:{ NoSuchFieldError -> 0x007d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x007d }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x007d }
            L_0x007d:
                int[] r0 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x0088 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x0088 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0088 }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0088 }
            L_0x0088:
                int[] r0 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x0093 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x0093 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0093 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0093 }
            L_0x0093:
                int[] r0 = f70x94151df2     // Catch:{ NoSuchFieldError -> 0x009e }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.ERROR     // Catch:{ NoSuchFieldError -> 0x009e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x009e }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x009e }
            L_0x009e:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftcdriverstation.FtcDriverStationActivity.C060650.<clinit>():void");
        }
    }

    /* access modifiers changed from: protected */
    public void setControlPanelBack(ControlPanelBack controlPanelBack2) {
        int i = C060650.f69xe50949a5[controlPanelBack2.ordinal()];
        if (i == 2) {
            dimControlPanelBack();
        } else if (i == 3) {
            brightenControlPanelBack();
        }
    }

    /* access modifiers changed from: protected */
    public void dimControlPanelBack() {
        setOpacity(this.controlPanelBack, PARTLY_OPAQUE);
    }

    /* access modifiers changed from: protected */
    public void brightenControlPanelBack() {
        setOpacity(this.controlPanelBack, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void disableAndDimOpModeMenu() {
        disableAndDim(this.buttonAutonomous);
        disableAndDim(this.buttonTeleOp);
        disableAndDim(this.currentOpModeName);
        disableAndDim(this.chooseOpModePrompt);
    }

    /* access modifiers changed from: protected */
    public void enableAndBrightenOpModeMenu() {
        enableAndBrighten(this.buttonAutonomous);
        enableAndBrighten(this.buttonTeleOp);
        setOpacity(this.currentOpModeName, 1.0f);
        setOpacity(this.chooseOpModePrompt, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void brightenTelemetryRegion() {
        setOpacity(this.telemetryRegion, 1.0f);
    }

    /* access modifiers changed from: protected */
    public void disableAndDim(View view) {
        setOpacity(view, PARTLY_OPAQUE);
        setEnabled(view, false);
    }

    /* access modifiers changed from: protected */
    public void enableAndBrighten(View view) {
        setOpacity(view, 1.0f);
        setEnabled(view, true);
    }

    /* access modifiers changed from: protected */
    public void pingStatus(int i) {
        pingStatus(this.context.getString(i));
    }

    /* access modifiers changed from: protected */
    public void pingStatus(String str) {
        if (this.landscape) {
            TextView textView = this.textPingStatus;
            setTextView(textView, "Ping: " + str + " - ");
            return;
        }
        setTextView(this.textPingStatus, str);
    }

    /* access modifiers changed from: protected */
    public void startKeepAlives() {
        NetworkConnectionHandler networkConnectionHandler2 = this.networkConnectionHandler;
        if (networkConnectionHandler2 != null) {
            networkConnectionHandler2.startKeepAlives();
        }
    }

    /* access modifiers changed from: protected */
    public void stopKeepAlives() {
        NetworkConnectionHandler networkConnectionHandler2 = this.networkConnectionHandler;
        if (networkConnectionHandler2 != null) {
            networkConnectionHandler2.stopKeepAlives();
        }
    }

    public void onUserInteraction() {
        if (this.processUserActivity) {
            this.wifiMuteStateMachine.consumeEvent(WifiMuteEvent.USER_ACTIVITY);
        }
    }

    public int linkSpeedToWiFiSignal(int i, int i2) {
        float f = (float) i;
        if (f <= 6.0f) {
            return 0;
        }
        if (f >= 54.0f) {
            return i2;
        }
        return Math.round((f - 6.0f) / (48.0f / ((float) (i2 - 1))));
    }

    public int rssiToWiFiSignal(int i, int i2) {
        float f = (float) i;
        if (f <= -90.0f) {
            return 0;
        }
        if (f >= -55.0f) {
            return i2;
        }
        return Math.round((f - -90.0f) / (35.0f / ((float) (i2 - 1))));
    }

    public void toggleWifiStatsView(View view) {
        if (this.wiFiStatsView == WiFiStatsView.PING_CHAN) {
            this.wiFiStatsView = WiFiStatsView.DBM_LINK;
            this.textDbmLink.setVisibility(0);
            this.layoutPingChan.setVisibility(8);
        } else if (this.wiFiStatsView == WiFiStatsView.DBM_LINK) {
            this.wiFiStatsView = WiFiStatsView.PING_CHAN;
            this.layoutPingChan.setVisibility(0);
            this.textDbmLink.setVisibility(8);
        }
    }

    public void onNetworkHealthUpdate(final int i, final int i2) {
        int round = (int) Math.round(((double) ((float) (rssiToWiFiSignal(i, 5) + linkSpeedToWiFiSignal(i2, 5)))) / 2.0d);
        final int i3 = round != 0 ? round != 1 ? round != 2 ? round != 3 ? round != 4 ? round != 5 ? 0 : C0648R.C0649drawable.ic_signal_bars_5 : C0648R.C0649drawable.ic_signal_bars_4 : C0648R.C0649drawable.ic_signal_bars_3 : C0648R.C0649drawable.ic_signal_bars_2 : C0648R.C0649drawable.ic_signal_bars_1 : C0648R.C0649drawable.ic_signal_bars_0;
        runOnUiThread(new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.networkSignalLevel.setBackgroundResource(i3);
                FtcDriverStationActivity.this.textDbmLink.setText(String.format("%ddBm Link %dMb", new Object[]{Integer.valueOf(i), Integer.valueOf(i2)}));
            }
        });
    }

    public void startActivityForResult(final Intent intent, final int i, final Bundle bundle) {
        C060348 r0 = new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.disconnectFromPeerOnActivityStop = false;
                FtcDriverStationActivity.super.startActivityForResult(intent, i, bundle);
            }
        };
        if (this.waitingForResumeAfterOnActivityResultCalled) {
            synchronized (this.runOnResumeQueue) {
                this.runOnResumeQueue.add(r0);
            }
            return;
        }
        r0.run();
    }

    public void startActivity(final Intent intent, final Bundle bundle) {
        C060449 r0 = new Runnable() {
            public void run() {
                FtcDriverStationActivity.this.disconnectFromPeerOnActivityStop = false;
                FtcDriverStationActivity.super.startActivity(intent, bundle);
            }
        };
        if (this.waitingForResumeAfterOnActivityResultCalled) {
            synchronized (this.runOnResumeQueue) {
                this.runOnResumeQueue.add(r0);
            }
            return;
        }
        r0.run();
    }
}
