package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.WebcamConfiguration;

public class EditWebcamActivity extends EditUSBDeviceActivity {
    public static final RequestCode requestCode = RequestCode.EDIT_USB_CAMERA;
    private EditText textCameraName;
    private WebcamConfiguration webcamConfiguration;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.webcam_device);
        this.textCameraName = (EditText) findViewById(C0470R.C0472id.cameraName);
        deserialize(EditParameters.fromIntent(this, getIntent()));
        this.webcamConfiguration = (WebcamConfiguration) this.controllerConfiguration;
        this.textCameraName.addTextChangedListener(new EditActivity.SetNameTextWatcher(this.controllerConfiguration));
        this.textCameraName.setText(this.controllerConfiguration.getName());
        showFixSwapButtons();
    }

    /* access modifiers changed from: protected */
    public void refreshSerialNumber() {
        ((TextView) findViewById(C0470R.C0472id.serialNumber)).setText(formatSerialNumber(this, this.controllerConfiguration));
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        logActivityResult(i, i2, intent);
        if (i2 == -1) {
            EditParameters fromIntent = EditParameters.fromIntent(this, intent);
            RequestCode fromValue = RequestCode.fromValue(i);
            if (fromValue == EditSwapUsbDevices.requestCode) {
                completeSwapConfiguration(i, i2, intent);
            } else if (fromValue == requestCode) {
                WebcamConfiguration webcamConfiguration2 = (WebcamConfiguration) fromIntent.getConfiguration();
            }
            this.currentCfgFile.markDirty();
            this.robotConfigFileManager.setActiveConfig(this.currentCfgFile);
        }
    }

    public void onDoneButtonPressed(View view) {
        finishOk();
    }

    /* access modifiers changed from: protected */
    public void finishOk() {
        this.controllerConfiguration.setName(this.textCameraName.getText().toString());
        finishOk(new EditParameters((EditActivity) this, (DeviceConfiguration) this.controllerConfiguration, getRobotConfigMap()));
    }

    public void onCancelButtonPressed(View view) {
        finishCancel();
    }

    public void onFixButtonPressed(View view) {
        fixConfiguration();
    }

    public void onSwapButtonPressed(View view) {
        swapConfiguration();
    }
}
