package com.qualcomm.ftccommon.configuration;

import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;

public class EditMotorListActivity extends EditPortListSpinnerActivity<DeviceConfiguration> {
    ConfigurationType unspecifiedMotorType = MotorConfigurationType.getUnspecifiedMotorType();

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public ConfigurationType.DeviceFlavor getDeviceFlavorBeingConfigured() {
        return ConfigurationType.DeviceFlavor.MOTOR;
    }

    public EditMotorListActivity() {
        this.layoutMain = C0470R.layout.motor_list;
        this.idListParentLayout = C0470R.C0472id.item_list_parent;
        this.layoutItem = C0470R.layout.motor;
        this.idItemRowPort = C0470R.C0472id.row_port;
        this.idItemSpinner = C0470R.C0472id.choiceMotorSpinner;
        this.idItemEditTextResult = C0470R.C0472id.editTextResult;
        this.idItemPortNumber = C0470R.C0472id.port_number;
    }

    /* access modifiers changed from: protected */
    public ConfigurationType getDefaultEnabledSelection() {
        ConfigurationType configurationType = this.unspecifiedMotorType;
        if (configurationType != null) {
            return configurationType;
        }
        return super.getDefaultEnabledSelection();
    }
}
