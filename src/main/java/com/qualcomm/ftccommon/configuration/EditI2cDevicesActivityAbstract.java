package com.qualcomm.ftccommon.configuration;

import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;

public abstract class EditI2cDevicesActivityAbstract<ITEM_T extends DeviceConfiguration> extends EditPortListSpinnerActivity<ITEM_T> {
    public EditI2cDevicesActivityAbstract() {
        this.layoutMain = C0470R.layout.i2cs;
        this.idListParentLayout = C0470R.C0472id.item_list_parent;
        this.layoutItem = C0470R.layout.i2c_device;
        this.idItemRowPort = C0470R.C0472id.row_port_i2c;
        this.idItemSpinner = C0470R.C0472id.choiceSpinner;
        this.idItemEditTextResult = C0470R.C0472id.editTextResult;
        this.idItemPortNumber = C0470R.C0472id.port_number;
    }

    /* access modifiers changed from: protected */
    public ConfigurationType.DeviceFlavor getDeviceFlavorBeingConfigured() {
        return ConfigurationType.DeviceFlavor.I2C;
    }
}
