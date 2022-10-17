package com.qualcomm.ftccommon.configuration;

import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;

public class EditPWMDevicesActivity extends EditPortListCheckboxActivity<DeviceConfiguration> {
    public static final RequestCode requestCode = RequestCode.EDIT_PWM_PORT;

    public String getTag() {
        return getClass().getSimpleName();
    }

    public EditPWMDevicesActivity() {
        this.layoutMain = C0470R.layout.pwms;
        this.idListParentLayout = C0470R.C0472id.item_list_parent;
        this.layoutItem = C0470R.layout.pwm_device;
        this.idItemRowPort = C0470R.C0472id.row_port;
        this.idItemCheckbox = C0470R.C0472id.checkbox_port;
        this.idItemEditTextResult = C0470R.C0472id.editTextResult;
        this.idItemPortNumber = C0470R.C0472id.port_number;
    }
}
