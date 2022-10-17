package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.configuration.EditActivity;
import com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

public abstract class EditPortListActivity<ITEM_T extends DeviceConfiguration> extends EditUSBDeviceActivity {
    public static final String TAG = "EditPortListActivity";
    protected EditText editTextBannerControllerName;
    protected int idBannerParent = C0470R.C0472id.bannerParent;
    protected int idControllerName = C0470R.C0472id.controller_name;
    protected int idControllerSerialNumber = C0470R.C0472id.serialNumber;
    protected int idItemEditTextResult;
    protected int idItemPortNumber;
    protected int idItemRowPort;
    protected int idListParentLayout;
    protected int initialPortNumber;
    protected Class<ITEM_T> itemClass;
    protected List<ITEM_T> itemList = new ArrayList();
    protected ArrayList<View> itemViews = new ArrayList<>();
    protected int layoutControllerNameBanner = 0;
    protected int layoutItem;
    protected int layoutMain;
    protected TextView textViewSerialNumber;

    /* access modifiers changed from: protected */
    public abstract void addViewListenersOnIndex(int i);

    protected EditPortListActivity() {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(this.layoutMain);
        EditParameters fromIntent = EditParameters.fromIntent(this, getIntent());
        deserialize(fromIntent);
        this.initialPortNumber = fromIntent.getInitialPortNumber();
        showButton(this.idAddButton, fromIntent.isGrowable());
        if (this.layoutControllerNameBanner != 0) {
            LinearLayout linearLayout = (LinearLayout) findViewById(this.idBannerParent);
            View inflate = getLayoutInflater().inflate(this.layoutControllerNameBanner, linearLayout, false);
            linearLayout.addView(inflate);
            this.editTextBannerControllerName = (EditText) inflate.findViewById(this.idControllerName);
            this.textViewSerialNumber = (TextView) inflate.findViewById(this.idControllerSerialNumber);
            this.editTextBannerControllerName.setText(this.controllerConfiguration.getName());
            showFixSwapButtons();
        }
        createListViews(fromIntent);
        addViewListeners();
    }

    /* access modifiers changed from: protected */
    public void refreshSerialNumber() {
        this.textViewSerialNumber.setText(formatSerialNumber(this, this.controllerConfiguration));
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
    }

    /* access modifiers changed from: protected */
    public void createListViews(EditParameters<ITEM_T> editParameters) {
        if (editParameters != null) {
            this.itemList = editParameters.getCurrentItems();
            this.itemClass = editParameters.getItemClass();
            Collections.sort(this.itemList);
            for (int i = 0; i < this.itemList.size(); i++) {
                this.itemViews.add(createItemViewForPort(findConfigByIndex(i).getPort()));
            }
        }
    }

    /* access modifiers changed from: protected */
    public void addViewListeners() {
        for (int i = 0; i < this.itemList.size(); i++) {
            addViewListenersOnIndex(i);
        }
    }

    /* access modifiers changed from: protected */
    public View createItemViewForPort(int i) {
        LinearLayout linearLayout = (LinearLayout) findViewById(this.idListParentLayout);
        View inflate = getLayoutInflater().inflate(this.layoutItem, linearLayout, false);
        linearLayout.addView(inflate);
        View findViewById = inflate.findViewById(this.idItemRowPort);
        TextView textView = (TextView) findViewById.findViewById(this.idItemPortNumber);
        if (textView != null) {
            textView.setText(String.format(Locale.getDefault(), "%d", new Object[]{Integer.valueOf(i)}));
        }
        return findViewById;
    }

    /* access modifiers changed from: protected */
    public void addNameTextChangeWatcherOnIndex(int i) {
        ((EditText) findViewByIndex(i).findViewById(this.idItemEditTextResult)).addTextChangedListener(new EditActivity.SetNameTextWatcher(findConfigByIndex(i)));
    }

    public void onAddButtonPressed(View view) {
        addNewItem();
    }

    /* access modifiers changed from: protected */
    public void addNewItem() {
        int i;
        try {
            if (this.itemList.isEmpty()) {
                i = this.initialPortNumber;
            } else {
                List<ITEM_T> list = this.itemList;
                i = ((DeviceConfiguration) list.get(list.size() - 1)).getPort() + 1;
            }
            int size = this.itemList.size();
            DeviceConfiguration deviceConfiguration = (DeviceConfiguration) this.itemClass.newInstance();
            deviceConfiguration.setPort(i);
            deviceConfiguration.setConfigurationType(BuiltInConfigurationType.NOTHING);
            this.itemList.add(deviceConfiguration);
            this.itemViews.add(createItemViewForPort(i));
            addViewListenersOnIndex(size);
        } catch (IllegalAccessException | InstantiationException e) {
            RobotLog.m50ee(TAG, e, "exception thrown during addNewItem(); ignoring add");
        }
    }

    public void onFixButtonPressed(View view) {
        fixConfiguration();
    }

    public void onSwapButtonPressed(View view) {
        swapConfiguration();
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        if (i2 == -1) {
            completeSwapConfiguration(i, i2, intent);
            this.currentCfgFile.markDirty();
            this.robotConfigFileManager.updateActiveConfigHeader(this.currentCfgFile);
        }
    }

    /* access modifiers changed from: protected */
    public View findViewByIndex(int i) {
        return this.itemViews.get(i);
    }

    /* access modifiers changed from: protected */
    public DeviceConfiguration findConfigByIndex(int i) {
        return (DeviceConfiguration) this.itemList.get(i);
    }

    /* access modifiers changed from: protected */
    public DeviceConfiguration findConfigByPort(int i) {
        for (ITEM_T item_t : this.itemList) {
            if (item_t.getPort() == i) {
                return item_t;
            }
        }
        return null;
    }

    public void onDoneButtonPressed(View view) {
        finishOk();
    }

    public void onCancelButtonPressed(View view) {
        finishCancel();
    }

    /* access modifiers changed from: protected */
    public void finishOk() {
        if (this.controllerConfiguration != null) {
            this.controllerConfiguration.setName(this.editTextBannerControllerName.getText().toString());
            finishOk(new EditParameters((EditActivity) this, (DeviceConfiguration) this.controllerConfiguration, getRobotConfigMap()));
            return;
        }
        finishOk(new EditParameters((EditActivity) this, this.itemClass, this.itemList));
    }
}
