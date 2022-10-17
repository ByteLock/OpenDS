package com.qualcomm.ftccommon.configuration;

import android.view.View;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import java.util.List;

public abstract class EditPortListSpinnerActivity<ITEM_T extends DeviceConfiguration> extends EditPortListActivity<ITEM_T> {
    protected boolean configuringControlHubParent;
    protected ControlSystem controlSystem;
    protected int idItemSpinner;

    /* access modifiers changed from: protected */
    public abstract ConfigurationType.DeviceFlavor getDeviceFlavorBeingConfigured();

    protected EditPortListSpinnerActivity() {
    }

    /* access modifiers changed from: protected */
    public void deserialize(EditParameters editParameters) {
        super.deserialize(editParameters);
        this.controlSystem = editParameters.getControlSystem();
        this.configuringControlHubParent = editParameters.configuringControlHubParent();
    }

    /* access modifiers changed from: protected */
    public View createItemViewForPort(int i) {
        View createItemViewForPort = super.createItemViewForPort(i);
        localizeSpinner(createItemViewForPort);
        return createItemViewForPort;
    }

    /* access modifiers changed from: protected */
    public void localizeSpinner(View view) {
        List<ConfigurationType> applicableConfigTypes = ConfigurationTypeManager.getInstance().getApplicableConfigTypes(getDeviceFlavorBeingConfigured(), this.controlSystem, this.configuringControlHubParent);
        localizeConfigTypeSpinnerTypes(ConfigurationType.DisplayNameFlavor.Normal, (Spinner) view.findViewById(this.idItemSpinner), applicableConfigTypes);
    }

    /* access modifiers changed from: protected */
    public void addViewListenersOnIndex(int i) {
        View findViewByIndex = findViewByIndex(i);
        DeviceConfiguration findConfigByIndex = findConfigByIndex(i);
        addNameTextChangeWatcherOnIndex(i);
        handleDisabledDevice(findViewByIndex, findConfigByIndex);
        handleSpinner(findViewByIndex, this.idItemSpinner, findConfigByIndex);
    }

    private void handleDisabledDevice(View view, DeviceConfiguration deviceConfiguration) {
        EditText editText = (EditText) view.findViewById(this.idItemEditTextResult);
        if (deviceConfiguration.isEnabled()) {
            editText.setText(deviceConfiguration.getName());
            editText.setEnabled(true);
            return;
        }
        editText.setText(disabledDeviceName());
        editText.setEnabled(false);
    }

    /* access modifiers changed from: protected */
    public void clearDevice(View view) {
        int parseInt = Integer.parseInt(((TextView) view.findViewById(this.idItemPortNumber)).getText().toString());
        EditText editText = (EditText) view.findViewById(this.idItemEditTextResult);
        editText.setEnabled(false);
        editText.setText(disabledDeviceName());
        findConfigByPort(parseInt).setEnabled(false);
    }

    /* access modifiers changed from: protected */
    public void changeDevice(View view, ConfigurationType configurationType) {
        int parseInt = Integer.parseInt(((TextView) view.findViewById(this.idItemPortNumber)).getText().toString());
        EditText editText = (EditText) view.findViewById(this.idItemEditTextResult);
        editText.setEnabled(true);
        DeviceConfiguration findConfigByPort = findConfigByPort(parseInt);
        clearNameIfNecessary(editText, findConfigByPort);
        findConfigByPort.setConfigurationType(configurationType);
        findConfigByPort.setEnabled(true);
    }
}
