package com.qualcomm.ftccommon.configuration;

import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.ListView;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.util.RobotLog;

public class EditSwapUsbDevices extends EditActivity {
    public static final String TAG = "EditSwapUsbDevices";
    public static final RequestCode requestCode = RequestCode.EDIT_SWAP_USB_DEVICES;
    protected ControllerConfiguration targetConfiguration;

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        RobotLog.m60vv(TAG, "onCreate()");
        setContentView(C0470R.layout.activity_swap_usb_devices);
        EditParameters fromIntent = EditParameters.fromIntent(this, getIntent());
        deserialize(fromIntent);
        this.targetConfiguration = (ControllerConfiguration) fromIntent.getConfiguration();
        ((TextView) findViewById(C0470R.C0472id.swapCaption)).setText(String.format(getString(C0470R.string.swapPrompt), new Object[]{this.targetConfiguration.getName()}));
        ((Button) findViewById(C0470R.C0472id.doneButton)).setVisibility(8);
        populateList();
    }

    /* access modifiers changed from: protected */
    public void populateList() {
        ListView listView = (ListView) findViewById(C0470R.C0472id.controllersList);
        listView.setAdapter(new DeviceInfoAdapter(this, 17367044, getRobotConfigMap().getEligibleSwapTargets(this.targetConfiguration, this.scannedDevices, this)));
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
                EditSwapUsbDevices editSwapUsbDevices = EditSwapUsbDevices.this;
                editSwapUsbDevices.finishOk(new EditParameters(editSwapUsbDevices, (ControllerConfiguration) adapterView.getItemAtPosition(i)));
            }
        });
    }

    public void onBackPressed() {
        RobotLog.m60vv(TAG, "onBackPressed()");
        doBackOrCancel();
    }

    public void onCancelButtonPressed(View view) {
        RobotLog.m60vv(TAG, "onCancelButtonPressed()");
        doBackOrCancel();
    }

    /* access modifiers changed from: protected */
    public void doBackOrCancel() {
        finishCancel();
    }
}
