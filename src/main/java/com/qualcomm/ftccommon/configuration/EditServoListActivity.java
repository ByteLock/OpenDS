package com.qualcomm.ftccommon.configuration;

import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;

public class EditServoListActivity extends EditPortListSpinnerActivity {
    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public ConfigurationType.DeviceFlavor getDeviceFlavorBeingConfigured() {
        return ConfigurationType.DeviceFlavor.SERVO;
    }

    public EditServoListActivity() {
        this.layoutMain = C0470R.layout.servo_list;
        this.idListParentLayout = C0470R.C0472id.item_list_parent;
        this.layoutItem = C0470R.layout.servo;
        this.idItemRowPort = C0470R.C0472id.row_port;
        this.idItemSpinner = C0470R.C0472id.choiceSpinner;
        this.idItemEditTextResult = C0470R.C0472id.editTextResult;
        this.idItemPortNumber = C0470R.C0472id.port_number;
    }
}
