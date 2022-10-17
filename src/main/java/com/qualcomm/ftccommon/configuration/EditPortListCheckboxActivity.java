package com.qualcomm.ftccommon.configuration;

import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import org.firstinspires.inspection.InspectionState;

public abstract class EditPortListCheckboxActivity<ITEM_T extends DeviceConfiguration> extends EditPortListActivity<ITEM_T> {
    protected int idItemCheckbox;

    protected EditPortListCheckboxActivity() {
    }

    /* access modifiers changed from: protected */
    public void addViewListenersOnIndex(int i) {
        addCheckBoxListenerOnIndex(i);
        addNameTextChangeWatcherOnIndex(i);
        handleDisabledDeviceByIndex(i);
    }

    /* access modifiers changed from: protected */
    public void handleDisabledDeviceByIndex(int i) {
        View findViewByIndex = findViewByIndex(i);
        CheckBox checkBox = (CheckBox) findViewByIndex.findViewById(this.idItemCheckbox);
        DeviceConfiguration deviceConfiguration = (DeviceConfiguration) this.itemList.get(i);
        if (deviceConfiguration.isEnabled()) {
            checkBox.setChecked(true);
            ((EditText) findViewByIndex.findViewById(this.idItemEditTextResult)).setText(deviceConfiguration.getName());
            return;
        }
        checkBox.setChecked(true);
        checkBox.performClick();
    }

    /* access modifiers changed from: protected */
    public void addCheckBoxListenerOnIndex(int i) {
        View findViewByIndex = findViewByIndex(i);
        final EditText editText = (EditText) findViewByIndex.findViewById(this.idItemEditTextResult);
        final DeviceConfiguration deviceConfiguration = (DeviceConfiguration) this.itemList.get(i);
        ((CheckBox) findViewByIndex.findViewById(this.idItemCheckbox)).setOnClickListener(new View.OnClickListener() {
            public void onClick(View view) {
                if (((CheckBox) view).isChecked()) {
                    editText.setEnabled(true);
                    editText.setText(InspectionState.NO_VERSION);
                    deviceConfiguration.setEnabled(true);
                    deviceConfiguration.setName(InspectionState.NO_VERSION);
                    return;
                }
                editText.setEnabled(false);
                editText.setText(EditPortListCheckboxActivity.this.disabledDeviceName());
                deviceConfiguration.setEnabled(false);
                deviceConfiguration.setName(EditPortListCheckboxActivity.this.disabledDeviceName());
            }
        });
    }
}
