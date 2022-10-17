package com.qualcomm.ftcdriverstation;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.Switch;
import android.widget.Toast;
import org.firstinspires.directgamepadaccess.core.GamepadManager;
import org.firstinspires.directgamepadaccess.core.UsbGamepad;
import org.firstinspires.directgamepadaccess.core.UsbGamepadControlSurfaces;
import org.firstinspires.directgamepadaccess.usb.DirectAccessGamepadManager;

public class ConfigureAdvancedFeaturesDefaultGamepadsActivity extends Activity implements GamepadManager.Callback {
    static final String USER_1_FORMAT_STRING = "User 1: %s";
    static final String USER_2_FORMAT_STRING = "User 2: %s";
    static final String USER_NONE = "NONE";
    Switch advancedFeaturesSwitch;
    DefaultGamepadManager defaultGamepadManager;
    ProgressDialog detectionDialog;
    int gamepadBeingLookedFor;
    LinearLayout layoutDefaultGamepads;
    LinearLayout layoutRumbleOnBind;
    Switch rumbleOnBindSwitch;
    /* access modifiers changed from: private */
    public SharedPreferences sharedPreferences;
    Button user1defaultBtn;
    Button user2defaultBtn;

    public void onGamepadConnected(UsbGamepad usbGamepad) {
    }

    public void onGamepadDisconnected(UsbGamepad usbGamepad) {
    }

    public void onGamepadOpenFailed(UsbGamepad usbGamepad, UsbGamepad.OpenResultCode openResultCode) {
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0648R.layout.activity_configure_default_gamepads);
        this.sharedPreferences = PreferenceManager.getDefaultSharedPreferences(this);
        this.layoutDefaultGamepads = (LinearLayout) findViewById(C0648R.C0650id.layoutDefaultGamepads);
        this.advancedFeaturesSwitch = (Switch) findViewById(C0648R.C0650id.switchAdvancedFeatures);
        this.rumbleOnBindSwitch = (Switch) findViewById(C0648R.C0650id.switchRumbleOnBind);
        this.layoutRumbleOnBind = (LinearLayout) findViewById(C0648R.C0650id.layoutRumbleOnBind);
        if (FtcDriverStationActivity.usingUserspaceDriver) {
            this.advancedFeaturesSwitch.setChecked(true);
        } else {
            this.advancedFeaturesSwitch.setChecked(false);
            disableAllChildren(this.layoutDefaultGamepads);
            disableAllChildren(this.layoutRumbleOnBind);
        }
        if (this.sharedPreferences.getBoolean(getResources().getString(C0648R.string.pref_key_rumble_on_bind), DriverStationGamepadManager.RUMBLE_ON_BIND_DEFAULT)) {
            this.rumbleOnBindSwitch.setChecked(true);
        } else {
            this.rumbleOnBindSwitch.setChecked(false);
        }
        final String string = getResources().getString(C0648R.string.pref_key_advanced_gamepad_features);
        this.advancedFeaturesSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.sharedPreferences.edit().putBoolean(string, z).apply();
            }
        });
        this.rumbleOnBindSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            public void onCheckedChanged(CompoundButton compoundButton, boolean z) {
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.sharedPreferences.edit().putBoolean(ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.getResources().getString(C0648R.string.pref_key_rumble_on_bind), z).apply();
            }
        });
        this.user1defaultBtn = (Button) findViewById(C0648R.C0650id.user1_default_btn);
        this.user2defaultBtn = (Button) findViewById(C0648R.C0650id.user2_default_btn);
        this.defaultGamepadManager = DefaultGamepadManager.getInstance(this);
        if (FtcDriverStationActivity.usingUserspaceDriver) {
            DirectAccessGamepadManager.getInstance(this).registerCallback(this);
        }
        if (this.defaultGamepadManager.getPosition1Default() == null) {
            this.user1defaultBtn.setText(String.format(USER_1_FORMAT_STRING, new Object[]{USER_NONE}));
        } else {
            this.user1defaultBtn.setText(String.format(USER_1_FORMAT_STRING, new Object[]{this.defaultGamepadManager.getPosition1Default()}));
        }
        if (this.defaultGamepadManager.getPosition2Default() == null) {
            this.user2defaultBtn.setText(String.format(USER_2_FORMAT_STRING, new Object[]{USER_NONE}));
        } else {
            this.user2defaultBtn.setText(String.format(USER_2_FORMAT_STRING, new Object[]{this.defaultGamepadManager.getPosition2Default()}));
        }
        this.user1defaultBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.defaultGamepadManager.clearPosition1Default();
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.user1defaultBtn.setText(String.format(ConfigureAdvancedFeaturesDefaultGamepadsActivity.USER_1_FORMAT_STRING, new Object[]{ConfigureAdvancedFeaturesDefaultGamepadsActivity.USER_NONE}));
                return true;
            }
        });
        this.user2defaultBtn.setOnLongClickListener(new View.OnLongClickListener() {
            public boolean onLongClick(View view) {
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.defaultGamepadManager.clearPosition2Default();
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.user2defaultBtn.setText(String.format(ConfigureAdvancedFeaturesDefaultGamepadsActivity.USER_2_FORMAT_STRING, new Object[]{ConfigureAdvancedFeaturesDefaultGamepadsActivity.USER_NONE}));
                return true;
            }
        });
    }

    public void onBackPressed() {
        if (FtcDriverStationActivity.usingUserspaceDriver != this.advancedFeaturesSwitch.isChecked()) {
            new AlertDialog.Builder(this).setTitle(C0648R.string.title_advanced_gamepad_features_changed).setMessage(C0648R.string.explanation_advanced_gamepad_features_changed).setPositiveButton("OK", new DialogInterface.OnClickListener() {
                public void onClick(DialogInterface dialogInterface, int i) {
                    ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.finish();
                }
            }).show();
        } else {
            finish();
        }
    }

    public void onDestroy() {
        super.onDestroy();
        if (FtcDriverStationActivity.usingUserspaceDriver) {
            DirectAccessGamepadManager.getInstance(this).unregisterCallback(this);
        }
    }

    public void onClickUser1DefaultBtn(View view) {
        this.gamepadBeingLookedFor = 1;
        showDetectionDialog();
    }

    public void onClickUser2DefaultBtn(View view) {
        this.gamepadBeingLookedFor = 2;
        showDetectionDialog();
    }

    private void showDetectionDialog() {
        ProgressDialog progressDialog = new ProgressDialog(this);
        this.detectionDialog = progressDialog;
        progressDialog.setTitle("Gamepad identification");
        this.detectionDialog.setMessage("Please press any key on the gamepad.");
        this.detectionDialog.setCancelable(false);
        this.detectionDialog.setProgressStyle(0);
        this.detectionDialog.setButton(-2, "Abort", new DialogInterface.OnClickListener() {
            public void onClick(DialogInterface dialogInterface, int i) {
                Toast.makeText(ConfigureAdvancedFeaturesDefaultGamepadsActivity.this, "Aborted gamepad detection", 0).show();
            }
        });
        this.detectionDialog.show();
    }

    /* access modifiers changed from: package-private */
    public void handleGamepadIdentified(UsbGamepad usbGamepad) {
        ProgressDialog progressDialog = this.detectionDialog;
        if (progressDialog != null && progressDialog.isShowing()) {
            this.detectionDialog.dismiss();
            int i = this.gamepadBeingLookedFor;
            if (i == 1) {
                String position2Default = this.defaultGamepadManager.getPosition2Default();
                if (position2Default == null || !position2Default.equals(DefaultGamepadManager.getIdString(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber()))) {
                    this.defaultGamepadManager.setPosition1Default(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber());
                    this.user1defaultBtn.setText(String.format(USER_1_FORMAT_STRING, new Object[]{DefaultGamepadManager.getIdString(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber())}));
                    return;
                }
                Toast.makeText(this, "Already assigned to another slot!", 0).show();
            } else if (i == 2) {
                String position1Default = this.defaultGamepadManager.getPosition1Default();
                if (position1Default == null || !position1Default.equals(DefaultGamepadManager.getIdString(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber()))) {
                    this.defaultGamepadManager.setPosition2Default(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber());
                    this.user2defaultBtn.setText(String.format(USER_2_FORMAT_STRING, new Object[]{DefaultGamepadManager.getIdString(usbGamepad.getVid(), usbGamepad.getPid(), usbGamepad.getSerialNumber())}));
                    return;
                }
                Toast.makeText(this, "Already assigned to another slot!", 0).show();
            }
        }
    }

    public void onGamepadControlSurfaceUpdate(final UsbGamepad usbGamepad, UsbGamepadControlSurfaces usbGamepadControlSurfaces) {
        runOnUiThread(new Runnable() {
            public void run() {
                ConfigureAdvancedFeaturesDefaultGamepadsActivity.this.handleGamepadIdentified(usbGamepad);
            }
        });
    }

    static void disableAllChildren(ViewGroup viewGroup) {
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            viewGroup.getChildAt(i).setEnabled(false);
        }
    }
}
