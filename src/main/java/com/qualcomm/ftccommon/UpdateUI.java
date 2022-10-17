package com.qualcomm.ftccommon;

import android.app.Activity;
import android.widget.TextView;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.robot.RobotState;
import com.qualcomm.robotcore.robot.RobotStatus;
import com.qualcomm.robotcore.util.Dimmer;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.ThreadPool;
import com.qualcomm.robotcore.wifi.NetworkConnection;
import org.firstinspires.ftc.ftccommon.external.RobotStateMonitor;
import org.firstinspires.ftc.robotcore.network.DeviceNameListener;
import org.firstinspires.ftc.robotcore.network.DeviceNameManagerFactory;
import org.firstinspires.ftc.robotcore.network.NetworkStatus;
import org.firstinspires.ftc.robotcore.network.PeerStatus;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public class UpdateUI {
    public static final boolean DEBUG = false;
    private static final int NUM_GAMEPADS = 2;
    private static final String TAG = "UpdateUI";
    Activity activity;
    FtcRobotControllerService controllerService;
    Dimmer dimmer;
    protected NetworkStatus networkStatus = NetworkStatus.UNKNOWN;
    protected String networkStatusExtra = null;
    protected String networkStatusMessage = null;
    protected PeerStatus peerStatus = PeerStatus.DISCONNECTED;
    Restarter restarter;
    protected RobotState robotState = RobotState.NOT_STARTED;
    protected RobotStatus robotStatus = RobotStatus.NONE;
    protected String stateStatusMessage = null;
    protected TextView textDeviceName;
    protected TextView textErrorMessage;
    protected int textErrorMessageOriginalColor;
    protected TextView[] textGamepad = new TextView[2];
    protected TextView textNetworkConnectionStatus;
    protected TextView textOpMode;
    protected TextView textRobotStatus;

    public class Callback {
        DeviceNameManagerCallback deviceNameManagerCallback = new DeviceNameManagerCallback();
        RobotStateMonitor stateMonitor = null;

        public Callback() {
            DeviceNameManagerFactory.getInstance().registerCallback(this.deviceNameManagerCallback);
        }

        public void close() {
            DeviceNameManagerFactory.getInstance().unregisterCallback(this.deviceNameManagerCallback);
        }

        public RobotStateMonitor getStateMonitor() {
            return this.stateMonitor;
        }

        public void setStateMonitor(RobotStateMonitor robotStateMonitor) {
            this.stateMonitor = robotStateMonitor;
        }

        public void restartRobot() {
            ThreadPool.getDefault().submit(new Runnable() {
                public void run() {
                    AppUtil.getInstance().runOnUiThread(new Runnable() {
                        public void run() {
                            UpdateUI.this.requestRobotRestart();
                        }
                    });
                }
            });
        }

        public void updateUi(final String str, final Gamepad[] gamepadArr) {
            UpdateUI.this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    String str;
                    if (UpdateUI.this.textGamepad != null) {
                        for (int i = 0; i < UpdateUI.this.textGamepad.length; i++) {
                            Gamepad[] gamepadArr = gamepadArr;
                            if (i >= gamepadArr.length) {
                                break;
                            }
                            if (gamepadArr[i].getGamepadId() == -1) {
                                UpdateUI.this.setText(UpdateUI.this.textGamepad[i], InspectionState.NO_VERSION);
                            } else {
                                UpdateUI.this.setText(UpdateUI.this.textGamepad[i], gamepadArr[i].toString());
                            }
                        }
                    }
                    if (str.equals("$Stop$Robot$")) {
                        str = UpdateUI.this.activity.getString(C0470R.string.defaultOpModeName);
                    } else {
                        str = str;
                    }
                    UpdateUI updateUI = UpdateUI.this;
                    TextView textView = UpdateUI.this.textOpMode;
                    updateUI.setText(textView, "Op Mode: " + str);
                    Callback.this.refreshTextErrorMessage();
                }
            });
        }

        public void networkConnectionUpdate(NetworkConnection.NetworkEvent networkEvent) {
            switch (C04851.f67x94151df2[networkEvent.ordinal()]) {
                case 1:
                    updateNetworkConnectionStatus(NetworkStatus.UNKNOWN);
                    return;
                case 2:
                    updateNetworkConnectionStatus(NetworkStatus.INACTIVE);
                    return;
                case 3:
                    updateNetworkConnectionStatus(NetworkStatus.ENABLED);
                    return;
                case 4:
                    updateNetworkConnectionStatus(NetworkStatus.ERROR);
                    return;
                case 5:
                    updateNetworkConnectionStatus(NetworkStatus.ACTIVE);
                    return;
                case 6:
                    updateNetworkConnectionStatus(NetworkStatus.CREATED_AP_CONNECTION, UpdateUI.this.controllerService.getNetworkConnection().getConnectionOwnerName());
                    return;
                default:
                    return;
            }
        }

        protected class DeviceNameManagerCallback implements DeviceNameListener {
            protected DeviceNameManagerCallback() {
            }

            public void onDeviceNameChanged(String str) {
                Callback.this.displayDeviceName(str);
            }
        }

        /* access modifiers changed from: protected */
        public void displayDeviceName(final String str) {
            UpdateUI.this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    UpdateUI.this.textDeviceName.setText(str);
                }
            });
        }

        public void updateNetworkConnectionStatus(NetworkStatus networkStatus) {
            if (UpdateUI.this.networkStatus != networkStatus) {
                UpdateUI.this.networkStatus = networkStatus;
                UpdateUI.this.networkStatusExtra = null;
                RobotStateMonitor robotStateMonitor = this.stateMonitor;
                if (robotStateMonitor != null) {
                    robotStateMonitor.updateNetworkStatus(networkStatus, (String) null);
                }
                refreshNetworkStatus();
            }
        }

        public void updateNetworkConnectionStatus(NetworkStatus networkStatus, String str) {
            if (UpdateUI.this.networkStatus != networkStatus || !str.equals(UpdateUI.this.networkStatusExtra)) {
                UpdateUI.this.networkStatus = networkStatus;
                UpdateUI.this.networkStatusExtra = str;
                RobotStateMonitor robotStateMonitor = this.stateMonitor;
                if (robotStateMonitor != null) {
                    robotStateMonitor.updateNetworkStatus(networkStatus, str);
                }
                refreshNetworkStatus();
            }
        }

        public void updatePeerStatus(PeerStatus peerStatus) {
            if (UpdateUI.this.peerStatus != peerStatus) {
                UpdateUI.this.peerStatus = peerStatus;
                RobotStateMonitor robotStateMonitor = this.stateMonitor;
                if (robotStateMonitor != null) {
                    robotStateMonitor.updatePeerStatus(peerStatus);
                }
                refreshNetworkStatus();
            }
        }

        /* access modifiers changed from: package-private */
        public void refreshNetworkStatus() {
            String str;
            String string = UpdateUI.this.activity.getString(C0470R.string.networkStatusFormat);
            String networkStatus = UpdateUI.this.networkStatus.toString(UpdateUI.this.activity, UpdateUI.this.networkStatusExtra);
            if (UpdateUI.this.peerStatus == PeerStatus.UNKNOWN) {
                str = InspectionState.NO_VERSION;
            } else {
                str = String.format(", %s", new Object[]{UpdateUI.this.peerStatus.toString(UpdateUI.this.activity)});
            }
            final String format = String.format(string, new Object[]{networkStatus, str});
            if (!format.equals(UpdateUI.this.networkStatusMessage)) {
                RobotLog.m60vv(UpdateUI.TAG, format);
            }
            UpdateUI.this.networkStatusMessage = format;
            UpdateUI.this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    UpdateUI.this.setText(UpdateUI.this.textNetworkConnectionStatus, format);
                }
            });
        }

        public void updateRobotStatus(RobotStatus robotStatus) {
            UpdateUI.this.robotStatus = robotStatus;
            RobotStateMonitor robotStateMonitor = this.stateMonitor;
            if (robotStateMonitor != null) {
                robotStateMonitor.updateRobotStatus(UpdateUI.this.robotStatus);
            }
            refreshStateStatus();
        }

        public void updateRobotState(RobotState robotState) {
            UpdateUI.this.robotState = robotState;
            RobotStateMonitor robotStateMonitor = this.stateMonitor;
            if (robotStateMonitor != null) {
                robotStateMonitor.updateRobotState(UpdateUI.this.robotState);
            }
            refreshStateStatus();
        }

        /* access modifiers changed from: protected */
        public void refreshStateStatus() {
            String str;
            String string = UpdateUI.this.activity.getString(C0470R.string.robotStatusFormat);
            String robotState = UpdateUI.this.robotState.toString(UpdateUI.this.activity);
            if (UpdateUI.this.robotStatus == RobotStatus.NONE) {
                str = InspectionState.NO_VERSION;
            } else {
                str = String.format(", %s", new Object[]{UpdateUI.this.robotStatus.toString(UpdateUI.this.activity)});
            }
            final String format = String.format(string, new Object[]{robotState, str});
            if (!format.equals(UpdateUI.this.stateStatusMessage)) {
                RobotLog.m58v(format);
            }
            UpdateUI.this.stateStatusMessage = format;
            UpdateUI.this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    UpdateUI.this.setText(UpdateUI.this.textRobotStatus, format);
                    Callback.this.refreshTextErrorMessage();
                }
            });
        }

        public void refreshErrorTextOnUiThread() {
            UpdateUI.this.activity.runOnUiThread(new Runnable() {
                public void run() {
                    Callback.this.refreshTextErrorMessage();
                }
            });
        }

        /* access modifiers changed from: package-private */
        public void refreshTextErrorMessage() {
            String globalErrorMsg = RobotLog.getGlobalErrorMsg();
            RobotLog.GlobalWarningMessage globalWarningMessage = RobotLog.getGlobalWarningMessage();
            if (!globalErrorMsg.isEmpty() || !globalWarningMessage.message.isEmpty()) {
                if (!globalErrorMsg.isEmpty()) {
                    String string = UpdateUI.this.activity.getString(C0470R.string.error_text_error, new Object[]{globalErrorMsg});
                    UpdateUI updateUI = UpdateUI.this;
                    updateUI.setText(updateUI.textErrorMessage, string);
                    TextView textView = UpdateUI.this.textErrorMessage;
                    AppUtil.getInstance();
                    textView.setTextColor(AppUtil.getColor(C0470R.color.text_error));
                    RobotStateMonitor robotStateMonitor = this.stateMonitor;
                    if (robotStateMonitor != null) {
                        robotStateMonitor.updateErrorMessage(globalErrorMsg);
                    }
                } else {
                    UpdateUI updateUI2 = UpdateUI.this;
                    updateUI2.setText(updateUI2.textErrorMessage, globalWarningMessage.message);
                    TextView textView2 = UpdateUI.this.textErrorMessage;
                    AppUtil.getInstance();
                    textView2.setTextColor(AppUtil.getColor(C0470R.color.text_warning));
                    RobotStateMonitor robotStateMonitor2 = this.stateMonitor;
                    if (robotStateMonitor2 != null) {
                        robotStateMonitor2.updateWarningMessage(globalWarningMessage);
                    }
                }
                UpdateUI.this.dimmer.longBright();
                return;
            }
            UpdateUI updateUI3 = UpdateUI.this;
            updateUI3.setText(updateUI3.textErrorMessage, InspectionState.NO_VERSION);
            UpdateUI.this.textErrorMessage.setTextColor(UpdateUI.this.textErrorMessageOriginalColor);
            RobotStateMonitor robotStateMonitor3 = this.stateMonitor;
            if (robotStateMonitor3 != null) {
                robotStateMonitor3.updateErrorMessage((String) null);
                this.stateMonitor.updateWarningMessage((RobotLog.GlobalWarningMessage) null);
            }
        }
    }

    /* renamed from: com.qualcomm.ftccommon.UpdateUI$1 */
    static /* synthetic */ class C04851 {

        /* renamed from: $SwitchMap$com$qualcomm$robotcore$wifi$NetworkConnection$NetworkEvent */
        static final /* synthetic */ int[] f67x94151df2;

        /* JADX WARNING: Can't wrap try/catch for region: R(14:0|1|2|3|4|5|6|7|8|9|10|11|12|14) */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent[] r0 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                f67x94151df2 = r0
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.UNKNOWN     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = f67x94151df2     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.DISCONNECTED     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = f67x94151df2     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTED_AS_GROUP_OWNER     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = f67x94151df2     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.ERROR     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = f67x94151df2     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.CONNECTION_INFO_AVAILABLE     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = f67x94151df2     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.robotcore.wifi.NetworkConnection$NetworkEvent r1 = com.qualcomm.robotcore.wifi.NetworkConnection.NetworkEvent.AP_CREATED     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.UpdateUI.C04851.<clinit>():void");
        }
    }

    public UpdateUI(Activity activity2, Dimmer dimmer2) {
        this.activity = activity2;
        this.dimmer = dimmer2;
    }

    public void setTextViews(TextView textView, TextView textView2, TextView[] textViewArr, TextView textView3, TextView textView4, TextView textView5) {
        this.textNetworkConnectionStatus = textView;
        this.textRobotStatus = textView2;
        this.textGamepad = textViewArr;
        this.textOpMode = textView3;
        this.textErrorMessage = textView4;
        this.textErrorMessageOriginalColor = textView4.getCurrentTextColor();
        this.textDeviceName = textView5;
    }

    /* access modifiers changed from: protected */
    public void setText(TextView textView, String str) {
        if (textView != null && str != null) {
            String trim = str.trim();
            if (trim.length() > 0) {
                textView.setText(trim);
                textView.setVisibility(0);
                return;
            }
            textView.setVisibility(4);
            textView.setText(" ");
        }
    }

    public void setControllerService(FtcRobotControllerService ftcRobotControllerService) {
        this.controllerService = ftcRobotControllerService;
    }

    public void setRestarter(Restarter restarter2) {
        this.restarter = restarter2;
    }

    /* access modifiers changed from: private */
    public void requestRobotRestart() {
        this.restarter.requestRestart();
    }
}
