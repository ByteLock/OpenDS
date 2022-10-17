package com.qualcomm.ftccommon.configuration;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.Spinner;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationUtility;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.Utility;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.network.CallbackResult;
import org.firstinspires.ftc.robotcore.internal.network.NetworkConnectionHandler;
import org.firstinspires.ftc.robotcore.internal.p013ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public abstract class EditActivity extends ThemedActivity {
    public static final String TAG = "EditActivity";
    protected AppUtil appUtil = AppUtil.getInstance();
    protected ConfigurationUtility configurationUtility;
    /* access modifiers changed from: protected */
    public Context context;
    protected ControllerConfiguration controllerConfiguration;
    protected RobotConfigFile currentCfgFile;
    protected List<RobotConfigFile> extantRobotConfigurations = new LinkedList();
    protected boolean haveRobotConfigMapParameter = false;
    protected int idAddButton = C0470R.C0472id.addButton;
    protected int idFixButton = C0470R.C0472id.fixButton;
    protected int idSwapButton = C0470R.C0472id.swapButton;
    protected boolean remoteConfigure = AppUtil.getInstance().isDriverStation();
    protected RobotConfigFileManager robotConfigFileManager;
    protected RobotConfigMap robotConfigMap = new RobotConfigMap();
    protected ScannedDevices scannedDevices = new ScannedDevices();
    protected AdapterView.OnItemSelectedListener spinnerListener = new AdapterView.OnItemSelectedListener() {
        public void onNothingSelected(AdapterView<?> adapterView) {
        }

        public void onItemSelected(AdapterView<?> adapterView, View view, int i, long j) {
            ConfigurationTypeAndDisplayName configurationTypeAndDisplayName = (ConfigurationTypeAndDisplayName) adapterView.getItemAtPosition(i);
            View itemViewFromSpinnerItem = itemViewFromSpinnerItem(view);
            if (configurationTypeAndDisplayName.configurationType == BuiltInConfigurationType.NOTHING) {
                EditActivity.this.clearDevice(itemViewFromSpinnerItem);
            } else {
                EditActivity.this.changeDevice(itemViewFromSpinnerItem, configurationTypeAndDisplayName.configurationType);
            }
        }

        /* access modifiers changed from: protected */
        public View itemViewFromSpinnerItem(View view) {
            return (View) view.getParent().getParent().getParent();
        }
    };
    protected Utility utility;

    /* access modifiers changed from: protected */
    public void changeDevice(View view, ConfigurationType configurationType) {
    }

    /* access modifiers changed from: protected */
    public void clearDevice(View view) {
    }

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        this.context = this;
        PreferenceManager.setDefaultValues(this, C0470R.xml.app_settings, false);
        this.utility = new Utility(this);
        this.configurationUtility = new ConfigurationUtility();
        RobotConfigFileManager robotConfigFileManager2 = new RobotConfigFileManager(this);
        this.robotConfigFileManager = robotConfigFileManager2;
        this.currentCfgFile = robotConfigFileManager2.getActiveConfig();
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        this.robotConfigFileManager.updateActiveConfigHeader(this.currentCfgFile);
    }

    /* access modifiers changed from: protected */
    public void deserialize(EditParameters editParameters) {
        this.scannedDevices = editParameters.getScannedDevices();
        this.extantRobotConfigurations = editParameters.getExtantRobotConfigurations();
        this.controllerConfiguration = editParameters.getConfiguration() instanceof ControllerConfiguration ? (ControllerConfiguration) editParameters.getConfiguration() : null;
        if (editParameters.getCurrentCfgFile() != null) {
            this.currentCfgFile = editParameters.getCurrentCfgFile();
        }
        deserializeConfigMap(editParameters);
    }

    /* access modifiers changed from: protected */
    public void deserializeConfigMap(EditParameters editParameters) {
        ControllerConfiguration controllerConfiguration2;
        this.robotConfigMap = new RobotConfigMap(editParameters.getRobotConfigMap());
        this.haveRobotConfigMapParameter = editParameters.haveRobotConfigMapParameter();
        RobotConfigMap robotConfigMap2 = this.robotConfigMap;
        if (robotConfigMap2 != null && (controllerConfiguration2 = this.controllerConfiguration) != null && robotConfigMap2.contains(controllerConfiguration2.getSerialNumber())) {
            this.controllerConfiguration = this.robotConfigMap.get(this.controllerConfiguration.getSerialNumber());
        }
    }

    /* access modifiers changed from: protected */
    public RobotConfigMap getRobotConfigMap() {
        RobotConfigMap robotConfigMap2 = this.robotConfigMap;
        return robotConfigMap2 == null ? new RobotConfigMap() : robotConfigMap2;
    }

    /* access modifiers changed from: protected */
    public void handleLaunchEdit(RequestCode requestCode, Class cls, List<DeviceConfiguration> list) {
        handleLaunchEdit(requestCode, cls, new EditParameters(this, DeviceConfiguration.class, list));
    }

    /* access modifiers changed from: protected */
    public void handleLaunchEdit(RequestCode requestCode, Class cls, DeviceConfiguration deviceConfiguration) {
        handleLaunchEdit(requestCode, cls, new EditParameters(this, deviceConfiguration));
    }

    /* access modifiers changed from: protected */
    public void handleLaunchEdit(RequestCode requestCode, Class cls, EditParameters editParameters) {
        handleLaunchEdit(requestCode, cls, editParameters.toBundle());
    }

    private void handleLaunchEdit(RequestCode requestCode, Class cls, Bundle bundle) {
        Intent intent = new Intent(this.context, cls);
        intent.putExtras(bundle);
        setResult(-1, intent);
        RobotLog.m59v("%s: starting activity %s code=%d", getClass().getSimpleName(), intent.getComponent().getShortClassName(), Integer.valueOf(requestCode.value));
        startActivityForResult(intent, requestCode.value);
    }

    public static String formatSerialNumber(Context context2, ControllerConfiguration controllerConfiguration2) {
        String obj = controllerConfiguration2.getSerialNumber().toString();
        if (controllerConfiguration2.getSerialNumber().isFake() || controllerConfiguration2.isKnownToBeAttached()) {
            return obj;
        }
        return obj + context2.getString(C0470R.string.serialNumberNotAttached);
    }

    /* access modifiers changed from: protected */
    public void finishCancel() {
        RobotLog.m59v("%s: cancelling", getClass().getSimpleName());
        setResult(0, new Intent());
        finish();
    }

    /* access modifiers changed from: protected */
    public void finishOk(EditParameters editParameters) {
        RobotLog.m59v("%s: OK", getClass().getSimpleName());
        Intent intent = new Intent();
        editParameters.putIntent(intent);
        finishOk(intent);
    }

    /* access modifiers changed from: protected */
    public void finishOk() {
        finishOk(new Intent());
    }

    /* access modifiers changed from: protected */
    public void finishOk(Intent intent) {
        setResult(-1, intent);
        finish();
    }

    public void onBackPressed() {
        logBackPressed();
        finishOk();
    }

    /* access modifiers changed from: protected */
    public void logActivityResult(int i, int i2, Intent intent) {
        RobotLog.m59v("%s: activity result: code=%d result=%d", getClass().getSimpleName(), Integer.valueOf(i), Integer.valueOf(i2));
    }

    /* access modifiers changed from: protected */
    public void logBackPressed() {
        RobotLog.m59v("%s: backPressed received", getClass().getSimpleName());
    }

    protected class SetNameTextWatcher implements TextWatcher {
        private final DeviceConfiguration deviceConfiguration;

        public void beforeTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        public void onTextChanged(CharSequence charSequence, int i, int i2, int i3) {
        }

        protected SetNameTextWatcher(DeviceConfiguration deviceConfiguration2) {
            this.deviceConfiguration = deviceConfiguration2;
        }

        public void afterTextChanged(Editable editable) {
            this.deviceConfiguration.setName(editable.toString());
        }
    }

    protected static class DisplayNameAndInteger implements Comparable<DisplayNameAndInteger> {
        public final String displayName;
        public final int value;

        public DisplayNameAndInteger(String str, int i) {
            this.displayName = str;
            this.value = i;
        }

        public String toString() {
            return this.displayName;
        }

        public int compareTo(DisplayNameAndInteger displayNameAndInteger) {
            return this.displayName.compareTo(displayNameAndInteger.displayName);
        }
    }

    protected static class DisplayNameAndRequestCode implements Comparable<DisplayNameAndRequestCode> {
        public final String displayName;
        public final RequestCode requestCode;

        public DisplayNameAndRequestCode(String str) {
            String[] split = str.split("\\|");
            this.displayName = split[0];
            this.requestCode = RequestCode.fromString(split[1]);
        }

        public DisplayNameAndRequestCode(String str, RequestCode requestCode2) {
            this.displayName = str;
            this.requestCode = requestCode2;
        }

        public static DisplayNameAndRequestCode[] fromArray(String[] strArr) {
            int length = strArr.length;
            DisplayNameAndRequestCode[] displayNameAndRequestCodeArr = new DisplayNameAndRequestCode[length];
            for (int i = 0; i < length; i++) {
                displayNameAndRequestCodeArr[i] = new DisplayNameAndRequestCode(strArr[i]);
            }
            return displayNameAndRequestCodeArr;
        }

        public String toString() {
            return this.displayName;
        }

        public int compareTo(DisplayNameAndRequestCode displayNameAndRequestCode) {
            return this.displayName.compareTo(displayNameAndRequestCode.displayName);
        }
    }

    /* access modifiers changed from: protected */
    public void clearNameIfNecessary(EditText editText, DeviceConfiguration deviceConfiguration) {
        if (!deviceConfiguration.isEnabled()) {
            editText.setText(InspectionState.NO_VERSION);
            deviceConfiguration.setName(InspectionState.NO_VERSION);
            return;
        }
        editText.setText(deviceConfiguration.getName());
    }

    public String disabledDeviceName() {
        return getString(C0470R.string.noDeviceAttached);
    }

    public String nameOf(DeviceConfiguration deviceConfiguration) {
        return nameOf(deviceConfiguration.getName());
    }

    public String nameOf(String str) {
        return str.equals(DeviceConfiguration.DISABLED_DEVICE_NAME) ? getString(C0470R.string.noDeviceAttached) : str;
    }

    public String displayNameOfConfigurationType(ConfigurationType.DisplayNameFlavor displayNameFlavor, ConfigurationType configurationType) {
        return configurationType.getDisplayName(displayNameFlavor);
    }

    protected class ConfigurationTypeAndDisplayName {
        public final ConfigurationType configurationType;
        public final String displayName;
        public final ConfigurationType.DisplayNameFlavor flavor;

        public ConfigurationTypeAndDisplayName(ConfigurationType.DisplayNameFlavor displayNameFlavor, ConfigurationType configurationType2) {
            this.flavor = displayNameFlavor;
            this.configurationType = configurationType2;
            this.displayName = EditActivity.this.displayNameOfConfigurationType(displayNameFlavor, configurationType2);
        }

        public String toString() {
            return this.displayName;
        }
    }

    /* access modifiers changed from: protected */
    public void localizeConfigTypeSpinner(ConfigurationType.DisplayNameFlavor displayNameFlavor, Spinner spinner) {
        ArrayAdapter arrayAdapter = (ArrayAdapter) spinner.getAdapter();
        ArrayList arrayList = new ArrayList();
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            arrayList.add((String) arrayAdapter.getItem(i));
        }
        localizeConfigTypeSpinnerStrings(displayNameFlavor, spinner, arrayList);
    }

    /* access modifiers changed from: protected */
    public void localizeConfigTypeSpinnerStrings(ConfigurationType.DisplayNameFlavor displayNameFlavor, Spinner spinner, List<String> list) {
        LinkedList linkedList = new LinkedList();
        for (String fromString : list) {
            linkedList.add(BuiltInConfigurationType.fromString(fromString));
        }
        localizeConfigTypeSpinnerTypes(displayNameFlavor, spinner, linkedList);
    }

    /* access modifiers changed from: protected */
    public void localizeConfigTypeSpinnerTypes(ConfigurationType.DisplayNameFlavor displayNameFlavor, Spinner spinner, List<ConfigurationType> list) {
        ConfigurationTypeAndDisplayName[] configurationTypeAndDisplayNameArr = new ConfigurationTypeAndDisplayName[list.size()];
        for (int i = 0; i < list.size(); i++) {
            configurationTypeAndDisplayNameArr[i] = new ConfigurationTypeAndDisplayName(displayNameFlavor, list.get(i));
        }
        spinner.setAdapter(new ConfigurationTypeArrayAdapter(this, configurationTypeAndDisplayNameArr));
    }

    /* access modifiers changed from: protected */
    public int findPosition(Spinner spinner, ConfigurationType configurationType) {
        ArrayAdapter arrayAdapter = (ArrayAdapter) spinner.getAdapter();
        for (int i = 0; i < arrayAdapter.getCount(); i++) {
            if (((ConfigurationTypeAndDisplayName) arrayAdapter.getItem(i)).configurationType == configurationType) {
                return i;
            }
        }
        return -1;
    }

    /* access modifiers changed from: protected */
    public int findPosition(Spinner spinner, ConfigurationType configurationType, ConfigurationType configurationType2) {
        int findPosition = findPosition(spinner, configurationType);
        return findPosition < 0 ? findPosition(spinner, configurationType2) : findPosition;
    }

    /* access modifiers changed from: protected */
    public void handleSpinner(View view, int i, DeviceConfiguration deviceConfiguration) {
        handleSpinner(view, i, deviceConfiguration, false);
    }

    /* access modifiers changed from: protected */
    public void handleSpinner(View view, int i, DeviceConfiguration deviceConfiguration, boolean z) {
        Spinner spinner = (Spinner) view.findViewById(i);
        if (z || deviceConfiguration.isEnabled()) {
            spinner.setSelection(findPosition(spinner, deviceConfiguration.getSpinnerChoiceType(), getDefaultEnabledSelection()));
        } else {
            spinner.setSelection(findPosition(spinner, BuiltInConfigurationType.NOTHING));
        }
        spinner.setOnItemSelectedListener(this.spinnerListener);
    }

    /* access modifiers changed from: protected */
    public ConfigurationType getDefaultEnabledSelection() {
        return BuiltInConfigurationType.NOTHING;
    }

    /* access modifiers changed from: protected */
    public void sendOrInject(Command command) {
        if (this.remoteConfigure) {
            NetworkConnectionHandler.getInstance().sendCommand(command);
        } else {
            NetworkConnectionHandler.getInstance().injectReceivedCommand(command);
        }
    }

    /* access modifiers changed from: protected */
    public CallbackResult handleCommandNotifyActiveConfig(String str) {
        RobotLog.m61vv(TAG, "%s.handleCommandRequestActiveConfigResp(%s)", getClass().getSimpleName(), str);
        this.robotConfigFileManager.setActiveConfigAndUpdateUI(this.robotConfigFileManager.getConfigFromString(str));
        return CallbackResult.HANDLED_CONTINUE;
    }
}
