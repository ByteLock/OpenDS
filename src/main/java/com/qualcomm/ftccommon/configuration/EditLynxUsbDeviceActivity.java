package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxModuleConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxUsbDeviceConfiguration;
import java.util.List;

public class EditLynxUsbDeviceActivity extends EditUSBDeviceActivity {
    public static final RequestCode requestCode = RequestCode.EDIT_LYNX_USB_DEVICE;
    private AdapterView.OnItemClickListener editLaunchListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            EditLynxUsbDeviceActivity.this.handleLaunchEdit(EditLynxModuleActivity.requestCode, EditLynxModuleActivity.class, (DeviceConfiguration) EditLynxUsbDeviceActivity.this.lynxUsbDeviceConfiguration.getModules().get(EditLynxUsbDeviceActivity.this.listKeys[i].value));
        }
    };
    /* access modifiers changed from: private */
    public EditActivity.DisplayNameAndInteger[] listKeys;
    /* access modifiers changed from: private */
    public LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration;
    private EditText textLynxUsbDeviceName;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.lynx_usb_device);
        ((ListView) findViewById(C0470R.C0472id.lynxUsbDeviceModules)).setOnItemClickListener(this.editLaunchListener);
        this.textLynxUsbDeviceName = (EditText) findViewById(C0470R.C0472id.lynxUsbDeviceName);
        deserialize(EditParameters.fromIntent(this, getIntent()));
        this.lynxUsbDeviceConfiguration = (LynxUsbDeviceConfiguration) this.controllerConfiguration;
        this.textLynxUsbDeviceName.addTextChangedListener(new EditActivity.SetNameTextWatcher(this.controllerConfiguration));
        this.textLynxUsbDeviceName.setText(this.controllerConfiguration.getName());
        populateModules();
        showFixSwapButtons();
    }

    /* access modifiers changed from: protected */
    public void refreshSerialNumber() {
        ((TextView) findViewById(C0470R.C0472id.serialNumber)).setText(formatSerialNumber(this, this.controllerConfiguration));
    }

    /* access modifiers changed from: protected */
    public void populateModules() {
        ListView listView = (ListView) findViewById(C0470R.C0472id.lynxUsbDeviceModules);
        List<LynxModuleConfiguration> modules = this.lynxUsbDeviceConfiguration.getModules();
        this.listKeys = new EditActivity.DisplayNameAndInteger[modules.size()];
        int i = 0;
        while (true) {
            EditActivity.DisplayNameAndInteger[] displayNameAndIntegerArr = this.listKeys;
            if (i < displayNameAndIntegerArr.length) {
                displayNameAndIntegerArr[i] = new EditActivity.DisplayNameAndInteger(modules.get(i).getName(), i);
                i++;
            } else {
                listView.setAdapter(new ArrayAdapter(this, 17367043, this.listKeys));
                return;
            }
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        LynxModuleConfiguration lynxModuleConfiguration;
        logActivityResult(i, i2, intent);
        if (i2 == -1) {
            EditParameters fromIntent = EditParameters.fromIntent(this, intent);
            RequestCode fromValue = RequestCode.fromValue(i);
            if (fromValue == EditSwapUsbDevices.requestCode) {
                completeSwapConfiguration(i, i2, intent);
            } else if (fromValue == EditLynxModuleActivity.requestCode && (lynxModuleConfiguration = (LynxModuleConfiguration) fromIntent.getConfiguration()) != null) {
                int i3 = 0;
                while (true) {
                    if (i3 >= this.lynxUsbDeviceConfiguration.getModules().size()) {
                        break;
                    } else if (this.lynxUsbDeviceConfiguration.getModules().get(i3).getModuleAddress() == lynxModuleConfiguration.getModuleAddress()) {
                        this.lynxUsbDeviceConfiguration.getModules().set(i3, lynxModuleConfiguration);
                        break;
                    } else {
                        i3++;
                    }
                }
                populateModules();
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
        this.controllerConfiguration.setName(this.textLynxUsbDeviceName.getText().toString());
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
