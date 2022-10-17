package com.qualcomm.ftcdriverstation;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.preference.PreferenceManager;
import android.view.KeyEvent;
import android.view.MotionEvent;
import android.widget.Toast;
import com.qualcomm.ftccommon.SoundPlayer;
import com.qualcomm.robotcore.hardware.Gamepad;
import com.qualcomm.robotcore.util.Device;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.firstinspires.directgamepadaccess.android.AndroidGamepadManager;
import org.firstinspires.directgamepadaccess.core.CompositeGamepadManager;
import org.firstinspires.directgamepadaccess.core.GamepadManager;
import org.firstinspires.directgamepadaccess.core.NPlayerGamepadHelper;
import org.firstinspires.directgamepadaccess.core.UsbGamepad;
import org.firstinspires.directgamepadaccess.core.UsbGamepadControlSurfaces;
import org.firstinspires.ftc.robotcore.internal.android.p009dx.p012io.Opcodes;
import org.firstinspires.ftc.robotcore.ui.GamepadUser;
import org.firstinspires.ftc.robotcore.ui.RobotCoreGamepadManager;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class DriverStationGamepadManager implements NPlayerGamepadHelper.Callback, RobotCoreGamepadManager, SharedPreferences.OnSharedPreferenceChangeListener {
    public static final int GAMEPAD_POSITION_USER_1 = 0;
    public static final int GAMEPAD_POSITION_USER_2 = 1;
    public static final int NUM_GAMEPAD_POSITIONS = 2;
    public static boolean RUMBLE_ON_BIND_DEFAULT = true;
    private static final int SOUND_ID_GAMEPAD_CONNECT = 2131558404;
    private static final int SOUND_ID_GAMEPAD_DISCONNECT = 2131558405;
    private static final int UI_INDICATION_THROTTLE_MS = 100;
    private boolean advancedFeatures;
    private Context context;
    private DefaultGamepadManager defaultGamepadManager;
    private boolean enabled;
    private UsbGamepadControlSurfaces[] gamepadControlSurfaces = new UsbGamepadControlSurfaces[2];
    private GamepadManager gamepadDeviceManager;
    /* access modifiers changed from: private */
    public Map<GamepadUser, GamepadIndicator> gamepadIndicators;
    private NPlayerGamepadHelper gamepadPositionManager;
    private UsbGamepad[] gamepads = new UsbGamepad[2];
    private long[] lastUiIndicationTime = new long[2];
    private String prefKeyRumbleOnBind;
    private Toast prevToast;
    private Gamepad[] robotCoreGamepads = new Gamepad[2];
    private volatile boolean rumbleOnBind = RUMBLE_ON_BIND_DEFAULT;
    private SharedPreferences sharedPreferences;
    private ArrayList<GamepadUser> syntheticUsersToTransmit = new ArrayList<>();
    private final boolean toastsAreBuggy;
    /* access modifiers changed from: private */
    public volatile boolean[] uiIndicationRunning = new boolean[2];

    public DriverStationGamepadManager(Context context2) {
        this.context = context2;
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context2);
        String string = context2.getResources().getString(C0648R.string.pref_key_rumble_on_bind);
        this.prefKeyRumbleOnBind = string;
        this.rumbleOnBind = this.sharedPreferences.getBoolean(string, RUMBLE_ON_BIND_DEFAULT);
        this.sharedPreferences.registerOnSharedPreferenceChangeListener(this);
        this.toastsAreBuggy = Build.MODEL.equalsIgnoreCase(Device.MODEL_E5_PLAY) || Build.MODEL.equalsIgnoreCase(Device.MODEL_E5_XT1920DL);
        this.robotCoreGamepads[0] = new Gamepad();
        this.robotCoreGamepads[1] = new Gamepad();
        this.robotCoreGamepads[0].setUser(GamepadUser.ONE);
        this.robotCoreGamepads[1].setUser(GamepadUser.TWO);
        this.gamepadControlSurfaces[0] = new UsbGamepadControlSurfaces();
        this.gamepadControlSurfaces[1] = new UsbGamepadControlSurfaces();
        SoundPlayer.getInstance().preload(context2, (int) C0648R.raw.controller_connection);
        SoundPlayer.getInstance().preload(context2, (int) C0648R.raw.controller_dropped);
    }

    public synchronized void setGamepadIndicators(Map<GamepadUser, GamepadIndicator> map) {
        this.gamepadIndicators = map;
        if (this.gamepads[0] != null) {
            map.get(GamepadUser.ONE).setState(GamepadIndicator.State.VISIBLE);
        }
        if (this.gamepads[1] != null) {
            map.get(GamepadUser.TWO).setState(GamepadIndicator.State.VISIBLE);
        }
    }

    public void initialize(boolean z) {
        this.defaultGamepadManager = DefaultGamepadManager.getInstance(this.context);
        this.advancedFeatures = z;
        if (z) {
            CompositeGamepadManager compositeGamepadManager = new CompositeGamepadManager();
            compositeGamepadManager.setGamepadMappingSuggestor(FtcGamepadMappingSuggestor.getInstance(this.context));
            this.gamepadDeviceManager = compositeGamepadManager;
        } else {
            AndroidGamepadManager androidGamepadManager = new AndroidGamepadManager(true);
            androidGamepadManager.setGamepadMappingSuggestor(FtcGamepadMappingSuggestor.getInstance(this.context));
            this.gamepadDeviceManager = androidGamepadManager;
        }
        NPlayerGamepadHelper nPlayerGamepadHelper = new NPlayerGamepadHelper(this, 2, new NPlayerGamepadHelper.AssignmentSupplier() {
            public int getPosition(UsbGamepadControlSurfaces usbGamepadControlSurfaces) {
                if (!usbGamepadControlSurfaces.start || !usbGamepadControlSurfaces.f165a) {
                    return (!usbGamepadControlSurfaces.start || !usbGamepadControlSurfaces.f166b) ? -1 : 1;
                }
                return 0;
            }
        });
        this.gamepadPositionManager = nPlayerGamepadHelper;
        this.gamepadDeviceManager.initialize(this.context, nPlayerGamepadHelper);
        this.gamepadPositionManager.initialize(this.gamepadDeviceManager.getCurrentlyKnownGamepads());
    }

    public boolean isAdvancedFeatures() {
        return this.advancedFeatures;
    }

    public List<UsbGamepad> getKnownGamepads() {
        return this.gamepadDeviceManager.getCurrentlyKnownGamepads();
    }

    public void close() {
        this.gamepadDeviceManager.close();
    }

    public synchronized void onGamepadBound(UsbGamepad usbGamepad, int i, NPlayerGamepadHelper.BindReason bindReason) {
        if (bindReason == NPlayerGamepadHelper.BindReason.BY_DEFAULT) {
            showToast(String.format("Assigned gamepad %d by default", new Object[]{Integer.valueOf(i + 1)}));
        } else if (bindReason == NPlayerGamepadHelper.BindReason.AUTO_RECOVERY) {
            showToast(String.format("Gamepad %d auto-recovered", new Object[]{Integer.valueOf(i + 1)}));
        }
        this.robotCoreGamepads[i].setGamepadId(usbGamepad.getId());
        this.gamepads[i] = usbGamepad;
        if (i == 0) {
            this.gamepadIndicators.get(GamepadUser.ONE).setState(GamepadIndicator.State.VISIBLE);
            usbGamepad.setLEDMode(UsbGamepad.LedMode.USER_1_CONNECTED);
            if (this.rumbleOnBind) {
                usbGamepad.setRumblePower(255, 0, Opcodes.XOR_INT_LIT16);
            }
        } else if (i == 1) {
            this.gamepadIndicators.get(GamepadUser.TWO).setState(GamepadIndicator.State.VISIBLE);
            usbGamepad.setLEDMode(UsbGamepad.LedMode.USER_2_CONNECTED);
            if (this.rumbleOnBind) {
                usbGamepad.runRumbleEffect(new UsbGamepad.RumbleEffect.Builder().addStep(255, 0, 175).addStep(0, 0, 100).addStep(255, 0, 150).build());
            }
        }
        SoundPlayer.getInstance().play(this.context, (int) C0648R.raw.controller_connection, 1.0f, 0, 1.0f);
    }

    public synchronized void onGamepadUnbound(int i, NPlayerGamepadHelper.UnbindReason unbindReason) {
        if (unbindReason == NPlayerGamepadHelper.UnbindReason.USB_DEV_DCed) {
            showToast(String.format("Gamepad %d connection lost", new Object[]{Integer.valueOf(i + 1)}));
            SoundPlayer.getInstance().play(this.context, (int) C0648R.raw.controller_dropped, 1.0f, 0, 1.0f);
        }
        this.gamepads[i] = null;
        if (i == 0) {
            this.gamepadIndicators.get(GamepadUser.ONE).setState(GamepadIndicator.State.INVISIBLE);
            this.syntheticUsersToTransmit.add(GamepadUser.ONE);
        } else if (i == 1) {
            this.gamepadIndicators.get(GamepadUser.TWO).setState(GamepadIndicator.State.INVISIBLE);
            this.syntheticUsersToTransmit.add(GamepadUser.TWO);
        }
    }

    public synchronized void onGamepadUpdate(final int i, UsbGamepadControlSurfaces usbGamepadControlSurfaces) {
        this.robotCoreGamepads[i].refreshTimestamp();
        long currentTimeMillis = System.currentTimeMillis();
        long[] jArr = this.lastUiIndicationTime;
        if (currentTimeMillis - jArr[i] > 100 || jArr[i] == 0) {
            jArr[i] = currentTimeMillis;
            if (!this.uiIndicationRunning[i]) {
                this.uiIndicationRunning[i] = true;
                AppUtil.getInstance().runOnUiThread(new Runnable() {
                    public void run() {
                        int i = i;
                        if (i == 0) {
                            ((GamepadIndicator) DriverStationGamepadManager.this.gamepadIndicators.get(GamepadUser.ONE)).setState(GamepadIndicator.State.INDICATE);
                        } else if (i == 1) {
                            ((GamepadIndicator) DriverStationGamepadManager.this.gamepadIndicators.get(GamepadUser.TWO)).setState(GamepadIndicator.State.INDICATE);
                        }
                        DriverStationGamepadManager.this.uiIndicationRunning[i] = false;
                    }
                });
            }
        }
    }

    public synchronized void onGamepadOpenFailed(UsbGamepad usbGamepad, UsbGamepad.OpenResultCode openResultCode) {
    }

    public synchronized int getDefaultPositionForGamepad(int i, int i2, String str) {
        if (this.defaultGamepadManager.getPosition1Default() != null && this.defaultGamepadManager.getPosition1Default().equals(DefaultGamepadManager.getIdString(i, i2, str))) {
            return 0;
        }
        if (this.defaultGamepadManager.getPosition2Default() == null || !this.defaultGamepadManager.getPosition2Default().equals(DefaultGamepadManager.getIdString(i, i2, str))) {
            return -1;
        }
        return 1;
    }

    private void showToast(String str) {
        if (this.toastsAreBuggy) {
            Toast toast = this.prevToast;
            if (toast != null) {
                toast.cancel();
            }
            Toast makeText = Toast.makeText(this.context, str, 0);
            makeText.show();
            this.prevToast = makeText;
            return;
        }
        Toast.makeText(this.context, str, 0).show();
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    public synchronized List<Gamepad> getGamepadsForTransmission() {
        if (!this.enabled) {
            return new ArrayList();
        }
        ArrayList arrayList = new ArrayList(2);
        for (int i = 0; i < 2; i++) {
            if (this.gamepadPositionManager.captureGamepadStateIfAvailable(i, this.gamepadControlSurfaces[i])) {
                copyControlSurfaceToGamepad(this.gamepadControlSurfaces[i], this.robotCoreGamepads[i]);
                arrayList.add(this.robotCoreGamepads[i]);
            }
        }
        Iterator<GamepadUser> it = this.syntheticUsersToTransmit.iterator();
        while (it.hasNext()) {
            GamepadUser next = it.next();
            Iterator it2 = arrayList.iterator();
            while (true) {
                if (!it2.hasNext()) {
                    break;
                }
                Gamepad gamepad = (Gamepad) it2.next();
                if (gamepad.getUser() == next) {
                    arrayList.remove(gamepad);
                    break;
                }
            }
            Gamepad gamepad2 = new Gamepad();
            gamepad2.setGamepadId(-2);
            gamepad2.refreshTimestamp();
            gamepad2.setUser(next);
            arrayList.add(gamepad2);
        }
        this.syntheticUsersToTransmit.clear();
        return arrayList;
    }

    public synchronized void resetLedsForBindStatus() {
        UsbGamepad[] usbGamepadArr = this.gamepads;
        if (usbGamepadArr[0] != null) {
            usbGamepadArr[0].setLEDMode(UsbGamepad.LedMode.USER_1_CONNECTED);
        }
        UsbGamepad[] usbGamepadArr2 = this.gamepads;
        if (usbGamepadArr2[1] != null) {
            usbGamepadArr2[1].setLEDMode(UsbGamepad.LedMode.USER_2_CONNECTED);
        }
    }

    public synchronized void setLedColor(int i, int i2, int i3, int i4) {
        int i5 = i - 1;
        if (i5 <= 2 && i5 >= 0) {
            UsbGamepad[] usbGamepadArr = this.gamepads;
            if (usbGamepadArr[i5] != null) {
                usbGamepadArr[i5].setLedColor((byte) i2, (byte) i3, (byte) i4);
            }
        }
    }

    public synchronized void runLedEffect(Gamepad.LedEffect ledEffect) {
        int i = ledEffect.user - 1;
        if (i <= 2 && i >= 0) {
            UsbGamepad[] usbGamepadArr = this.gamepads;
            if (usbGamepadArr[i] != null) {
                usbGamepadArr[i].runLedEffect(ftcLedEffectToDriverLayerEffect(ledEffect));
            }
        }
    }

    private UsbGamepad.LedEffect ftcLedEffectToDriverLayerEffect(Gamepad.LedEffect ledEffect) {
        UsbGamepad.LedEffect.Builder builder = new UsbGamepad.LedEffect.Builder();
        Iterator<Gamepad.LedEffect.Step> it = ledEffect.steps.iterator();
        while (it.hasNext()) {
            Gamepad.LedEffect.Step next = it.next();
            builder.addStep((byte) next.f117r, (byte) next.f116g, (byte) next.f115b, next.duration);
        }
        builder.setRepeating(ledEffect.repeating);
        return builder.build();
    }

    public synchronized void runRumbleEffect(Gamepad.RumbleEffect rumbleEffect) {
        int i = rumbleEffect.user - 1;
        if (i <= 2 && i >= 0) {
            UsbGamepad[] usbGamepadArr = this.gamepads;
            if (usbGamepadArr[i] != null) {
                usbGamepadArr[i].runRumbleEffect(ftcRumbleEffectToDriverLayerEffect(rumbleEffect));
            }
        }
    }

    public synchronized void setRumblePowers(int i, int i2, int i3) {
        int i4 = i - 1;
        if (i4 <= 2 && i4 >= 0) {
            UsbGamepad[] usbGamepadArr = this.gamepads;
            if (usbGamepadArr[i4] != null) {
                usbGamepadArr[i4].setRumblePower((short) i2, (short) i3);
            }
        }
    }

    private UsbGamepad.RumbleEffect ftcRumbleEffectToDriverLayerEffect(Gamepad.RumbleEffect rumbleEffect) {
        UsbGamepad.RumbleEffect.Builder builder = new UsbGamepad.RumbleEffect.Builder();
        Iterator<Gamepad.RumbleEffect.Step> it = rumbleEffect.steps.iterator();
        while (it.hasNext()) {
            Gamepad.RumbleEffect.Step next = it.next();
            builder.addStep(next.large, next.small, next.duration);
        }
        return builder.build();
    }

    public synchronized void stopGamepadRumble() {
        for (int i = 0; i < 2; i++) {
            UsbGamepad[] usbGamepadArr = this.gamepads;
            if (usbGamepadArr[i] != null) {
                usbGamepadArr[i].setRumblePower(0, 0);
            }
        }
    }

    private static void copyControlSurfaceToGamepad(UsbGamepadControlSurfaces usbGamepadControlSurfaces, Gamepad gamepad) {
        gamepad.left_trigger = usbGamepadControlSurfaces.left_trigger;
        gamepad.right_trigger = usbGamepadControlSurfaces.right_trigger;
        gamepad.right_stick_button = usbGamepadControlSurfaces.right_stick_button;
        gamepad.left_stick_button = usbGamepadControlSurfaces.left_stick_button;
        gamepad.back = usbGamepadControlSurfaces.back;
        gamepad.start = usbGamepadControlSurfaces.start;
        gamepad.dpad_right = usbGamepadControlSurfaces.dpad_right;
        gamepad.dpad_left = usbGamepadControlSurfaces.dpad_left;
        gamepad.dpad_down = usbGamepadControlSurfaces.dpad_down;
        gamepad.dpad_up = usbGamepadControlSurfaces.dpad_up;
        gamepad.f114y = usbGamepadControlSurfaces.f168y;
        gamepad.f113x = usbGamepadControlSurfaces.f167x;
        gamepad.f110b = usbGamepadControlSurfaces.f166b;
        gamepad.f109a = usbGamepadControlSurfaces.f165a;
        gamepad.guide = usbGamepadControlSurfaces.guide;
        gamepad.touchpad = usbGamepadControlSurfaces.touchpad;
        gamepad.touchpad_finger_1 = usbGamepadControlSurfaces.touchpad_finger_1;
        gamepad.touchpad_finger_2 = usbGamepadControlSurfaces.touchpad_finger_2;
        if (usbGamepadControlSurfaces.type == UsbGamepadControlSurfaces.Type.SONY_PS4_COMPATIBLE) {
            gamepad.touchpad_finger_1_x = (usbGamepadControlSurfaces.touchpad_finger_1_x * 2.0f) - 1.0f;
            gamepad.touchpad_finger_1_y = -((usbGamepadControlSurfaces.touchpad_finger_1_y * 2.0f) - 1.0f);
            gamepad.touchpad_finger_2_x = (usbGamepadControlSurfaces.touchpad_finger_2_x * 2.0f) - 1.0f;
            gamepad.touchpad_finger_2_y = -((usbGamepadControlSurfaces.touchpad_finger_2_y * 2.0f) - 1.0f);
        } else {
            gamepad.touchpad_finger_1_x = 0.0f;
            gamepad.touchpad_finger_1_y = 0.0f;
            gamepad.touchpad_finger_2_x = 0.0f;
            gamepad.touchpad_finger_2_y = 0.0f;
        }
        gamepad.right_bumper = usbGamepadControlSurfaces.right_bumper;
        gamepad.left_bumper = usbGamepadControlSurfaces.left_bumper;
        gamepad.left_stick_x = usbGamepadControlSurfaces.left_stick_x;
        gamepad.left_stick_y = usbGamepadControlSurfaces.left_stick_y;
        gamepad.right_stick_x = usbGamepadControlSurfaces.right_stick_x;
        gamepad.right_stick_y = usbGamepadControlSurfaces.right_stick_y;
        gamepad.type = usbGamepadControlSurfaces.type == UsbGamepadControlSurfaces.Type.SONY_PS4_COMPATIBLE ? Gamepad.Type.SONY_PS4 : Gamepad.Type.XBOX_360;
    }

    public synchronized void handleGamepadEvent(MotionEvent motionEvent) {
        this.gamepadDeviceManager.onMotionEvent(motionEvent);
    }

    public synchronized void handleGamepadEvent(KeyEvent keyEvent) {
        this.gamepadDeviceManager.onKeyEvent(keyEvent);
    }

    public synchronized void handleNewIntent(Intent intent) {
        this.gamepadDeviceManager.onNewIntent(intent);
    }

    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences2, String str) {
        if (str.equals(this.prefKeyRumbleOnBind)) {
            this.rumbleOnBind = sharedPreferences2.getBoolean(this.prefKeyRumbleOnBind, RUMBLE_ON_BIND_DEFAULT);
        }
    }
}
