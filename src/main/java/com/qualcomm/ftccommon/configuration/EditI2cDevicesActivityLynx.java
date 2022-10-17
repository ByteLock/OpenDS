package com.qualcomm.ftccommon.configuration;

import android.view.View;
import android.widget.Spinner;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.LynxI2cDeviceConfiguration;
import java.util.List;

public class EditI2cDevicesActivityLynx extends EditI2cDevicesActivityAbstract<LynxI2cDeviceConfiguration> {
    private int i2cBus;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public void deserialize(EditParameters editParameters) {
        super.deserialize(editParameters);
        this.i2cBus = editParameters.getI2cBus();
    }

    /* access modifiers changed from: protected */
    public void localizeSpinner(View view) {
        List<ConfigurationType> applicableConfigTypes = ConfigurationTypeManager.getInstance().getApplicableConfigTypes(ConfigurationType.DeviceFlavor.I2C, this.controlSystem, this.configuringControlHubParent, this.i2cBus);
        localizeConfigTypeSpinnerTypes(ConfigurationType.DisplayNameFlavor.Normal, (Spinner) view.findViewById(this.idItemSpinner), applicableConfigTypes);
    }
}
