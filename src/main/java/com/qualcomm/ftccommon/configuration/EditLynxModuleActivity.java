package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.ftccommon.CommandList;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxI2cDeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxModuleConfiguration;
import com.qualcomm.robotcore.robocol.Command;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.List;
import org.firstinspires.ftc.robotcore.system.Assert;

public class EditLynxModuleActivity extends EditActivity {
    public static final RequestCode requestCode = RequestCode.EDIT_LYNX_MODULE;
    private AdapterView.OnItemClickListener editLaunchListener = new AdapterView.OnItemClickListener() {
        public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
            EditActivity.DisplayNameAndRequestCode displayNameAndRequestCode = EditLynxModuleActivity.this.listKeys[i];
            switch (C05052.$SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode[displayNameAndRequestCode.requestCode.ordinal()]) {
                case 1:
                    EditLynxModuleActivity.this.editMotors(displayNameAndRequestCode);
                    return;
                case 2:
                    EditLynxModuleActivity editLynxModuleActivity = EditLynxModuleActivity.this;
                    editLynxModuleActivity.editServos(displayNameAndRequestCode, 0, EditServoListActivity.class, editLynxModuleActivity.lynxModuleConfiguration.getServos());
                    return;
                case 3:
                    EditLynxModuleActivity.this.editI2cBus(displayNameAndRequestCode, 0);
                    return;
                case 4:
                    EditLynxModuleActivity.this.editI2cBus(displayNameAndRequestCode, 1);
                    return;
                case 5:
                    EditLynxModuleActivity.this.editI2cBus(displayNameAndRequestCode, 2);
                    return;
                case 6:
                    EditLynxModuleActivity.this.editI2cBus(displayNameAndRequestCode, 3);
                    return;
                case 7:
                    EditLynxModuleActivity editLynxModuleActivity2 = EditLynxModuleActivity.this;
                    editLynxModuleActivity2.editSimple(displayNameAndRequestCode, 0, EditDigitalDevicesActivityLynx.class, editLynxModuleActivity2.lynxModuleConfiguration.getDigitalDevices());
                    return;
                case 8:
                    EditLynxModuleActivity editLynxModuleActivity3 = EditLynxModuleActivity.this;
                    editLynxModuleActivity3.editSimple(displayNameAndRequestCode, 0, EditAnalogInputDevicesActivity.class, editLynxModuleActivity3.lynxModuleConfiguration.getAnalogInputs());
                    return;
                default:
                    return;
            }
        }
    };
    /* access modifiers changed from: private */
    public EditActivity.DisplayNameAndRequestCode[] listKeys;
    /* access modifiers changed from: private */
    public LynxModuleConfiguration lynxModuleConfiguration;
    private EditText lynx_module_name;

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.lynx_module);
        this.listKeys = EditActivity.DisplayNameAndRequestCode.fromArray(getResources().getStringArray(C0470R.array.lynx_module_options_array));
        ListView listView = (ListView) findViewById(C0470R.C0472id.lynx_module_devices);
        listView.setAdapter(new ArrayAdapter(this, 17367043, this.listKeys));
        listView.setOnItemClickListener(this.editLaunchListener);
        this.lynx_module_name = (EditText) findViewById(C0470R.C0472id.lynx_module_name);
        deserialize(EditParameters.fromIntent(this, getIntent()));
        LynxModuleConfiguration lynxModuleConfiguration2 = (LynxModuleConfiguration) this.controllerConfiguration;
        this.lynxModuleConfiguration = lynxModuleConfiguration2;
        this.lynx_module_name.addTextChangedListener(new EditActivity.SetNameTextWatcher(lynxModuleConfiguration2));
        this.lynx_module_name.setText(this.lynxModuleConfiguration.getName());
        RobotLog.m61vv(EditActivity.TAG, "lynxModuleConfiguration.getSerialNumber()=%s", this.lynxModuleConfiguration.getSerialNumber());
        visuallyIdentify();
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
        visuallyUnidentify();
    }

    /* access modifiers changed from: protected */
    public void visuallyIdentify() {
        sendIdentify(true);
    }

    /* access modifiers changed from: protected */
    public void visuallyUnidentify() {
        sendIdentify(false);
    }

    /* access modifiers changed from: protected */
    public void sendIdentify(boolean z) {
        sendOrInject(new Command(CommandList.CmdVisuallyIdentify.Command, new CommandList.CmdVisuallyIdentify(this.lynxModuleConfiguration.getModuleSerialNumber(), z).serialize()));
    }

    /* renamed from: com.qualcomm.ftccommon.configuration.EditLynxModuleActivity$2 */
    static /* synthetic */ class C05052 {
        static final /* synthetic */ int[] $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode;

        /* JADX WARNING: Can't wrap try/catch for region: R(18:0|1|2|3|4|5|6|7|8|9|10|11|12|13|14|15|16|18) */
        /* JADX WARNING: Code restructure failed: missing block: B:19:?, code lost:
            return;
         */
        /* JADX WARNING: Failed to process nested try/catch */
        /* JADX WARNING: Missing exception handler attribute for start block: B:11:0x003e */
        /* JADX WARNING: Missing exception handler attribute for start block: B:13:0x0049 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:15:0x0054 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:3:0x0012 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:5:0x001d */
        /* JADX WARNING: Missing exception handler attribute for start block: B:7:0x0028 */
        /* JADX WARNING: Missing exception handler attribute for start block: B:9:0x0033 */
        static {
            /*
                com.qualcomm.ftccommon.configuration.RequestCode[] r0 = com.qualcomm.ftccommon.configuration.RequestCode.values()
                int r0 = r0.length
                int[] r0 = new int[r0]
                $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode = r0
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_MOTOR_LIST     // Catch:{ NoSuchFieldError -> 0x0012 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0012 }
                r2 = 1
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0012 }
            L_0x0012:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x001d }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_SERVO_LIST     // Catch:{ NoSuchFieldError -> 0x001d }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x001d }
                r2 = 2
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x001d }
            L_0x001d:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x0028 }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_I2C_BUS0     // Catch:{ NoSuchFieldError -> 0x0028 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0028 }
                r2 = 3
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0028 }
            L_0x0028:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x0033 }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_I2C_BUS1     // Catch:{ NoSuchFieldError -> 0x0033 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0033 }
                r2 = 4
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0033 }
            L_0x0033:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x003e }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_I2C_BUS2     // Catch:{ NoSuchFieldError -> 0x003e }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x003e }
                r2 = 5
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x003e }
            L_0x003e:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x0049 }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_I2C_BUS3     // Catch:{ NoSuchFieldError -> 0x0049 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0049 }
                r2 = 6
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0049 }
            L_0x0049:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x0054 }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_DIGITAL     // Catch:{ NoSuchFieldError -> 0x0054 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0054 }
                r2 = 7
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0054 }
            L_0x0054:
                int[] r0 = $SwitchMap$com$qualcomm$ftccommon$configuration$RequestCode     // Catch:{ NoSuchFieldError -> 0x0060 }
                com.qualcomm.ftccommon.configuration.RequestCode r1 = com.qualcomm.ftccommon.configuration.RequestCode.EDIT_ANALOG_INPUT     // Catch:{ NoSuchFieldError -> 0x0060 }
                int r1 = r1.ordinal()     // Catch:{ NoSuchFieldError -> 0x0060 }
                r2 = 8
                r0[r1] = r2     // Catch:{ NoSuchFieldError -> 0x0060 }
            L_0x0060:
                return
            */
            throw new UnsupportedOperationException("Method not decompiled: com.qualcomm.ftccommon.configuration.EditLynxModuleActivity.C05052.<clinit>():void");
        }
    }

    /* access modifiers changed from: package-private */
    public <ITEM_T extends DeviceConfiguration> EditParameters initParameters(int i, Class<ITEM_T> cls, List<ITEM_T> list) {
        EditParameters editParameters = new EditParameters((EditActivity) this, cls, list);
        editParameters.setInitialPortNumber(i);
        editParameters.setControlSystem(ControlSystem.REV_HUB);
        editParameters.setConfiguringControlHubParent(this.lynxModuleConfiguration.getUsbDeviceSerialNumber().isEmbedded() && (this.lynxModuleConfiguration.isParent() || this.lynxModuleConfiguration.getModuleAddress() == 173));
        return editParameters;
    }

    /* access modifiers changed from: private */
    public void editSimple(EditActivity.DisplayNameAndRequestCode displayNameAndRequestCode, int i, Class cls, List<DeviceConfiguration> list) {
        handleLaunchEdit(displayNameAndRequestCode.requestCode, cls, initParameters(i, DeviceConfiguration.class, list));
    }

    /* access modifiers changed from: private */
    public void editServos(EditActivity.DisplayNameAndRequestCode displayNameAndRequestCode, int i, Class cls, List<DeviceConfiguration> list) {
        handleLaunchEdit(displayNameAndRequestCode.requestCode, cls, initParameters(i, DeviceConfiguration.class, list));
    }

    /* access modifiers changed from: private */
    public void editMotors(EditActivity.DisplayNameAndRequestCode displayNameAndRequestCode) {
        boolean z = true;
        Assert.assertTrue(this.lynxModuleConfiguration.getMotors().size() == 4);
        if (this.lynxModuleConfiguration.getMotors().get(0).getPort() != 0) {
            z = false;
        }
        Assert.assertTrue(z);
        handleLaunchEdit(displayNameAndRequestCode.requestCode, EditMotorListActivity.class, initParameters(0, DeviceConfiguration.class, this.lynxModuleConfiguration.getMotors()));
    }

    /* access modifiers changed from: private */
    public void editI2cBus(EditActivity.DisplayNameAndRequestCode displayNameAndRequestCode, int i) {
        EditParameters initParameters = initParameters(0, LynxI2cDeviceConfiguration.class, this.lynxModuleConfiguration.getI2cDevices(i));
        initParameters.setI2cBus(i);
        initParameters.setGrowable(true);
        handleLaunchEdit(displayNameAndRequestCode.requestCode, EditI2cDevicesActivityLynx.class, initParameters);
    }

    /* access modifiers changed from: protected */
    public void onActivityResult(int i, int i2, Intent intent) {
        logActivityResult(i, i2, intent);
        RequestCode fromValue = RequestCode.fromValue(i);
        if (i2 == -1) {
            boolean z = true;
            if (fromValue == RequestCode.EDIT_MOTOR_LIST) {
                this.lynxModuleConfiguration.setMotors(EditParameters.fromIntent(this, intent).getCurrentItems());
                Assert.assertTrue(this.lynxModuleConfiguration.getMotors().size() == 4);
                if (this.lynxModuleConfiguration.getMotors().get(0).getPort() != 0) {
                    z = false;
                }
                Assert.assertTrue(z);
            } else if (fromValue == RequestCode.EDIT_SERVO_LIST) {
                this.lynxModuleConfiguration.setServos(EditParameters.fromIntent(this, intent).getCurrentItems());
            } else if (fromValue == RequestCode.EDIT_ANALOG_INPUT) {
                this.lynxModuleConfiguration.setAnalogInputs(EditParameters.fromIntent(this, intent).getCurrentItems());
            } else if (fromValue == RequestCode.EDIT_DIGITAL) {
                this.lynxModuleConfiguration.setDigitalDevices(EditParameters.fromIntent(this, intent).getCurrentItems());
            } else {
                EditParameters fromIntent = EditParameters.fromIntent(this, intent);
                if (fromValue == RequestCode.EDIT_I2C_BUS0) {
                    this.lynxModuleConfiguration.setI2cDevices(0, fromIntent.getCurrentItems());
                } else if (fromValue == RequestCode.EDIT_I2C_BUS1) {
                    this.lynxModuleConfiguration.setI2cDevices(1, fromIntent.getCurrentItems());
                } else if (fromValue == RequestCode.EDIT_I2C_BUS2) {
                    this.lynxModuleConfiguration.setI2cDevices(2, fromIntent.getCurrentItems());
                } else if (fromValue == RequestCode.EDIT_I2C_BUS3) {
                    this.lynxModuleConfiguration.setI2cDevices(3, fromIntent.getCurrentItems());
                }
            }
            this.currentCfgFile.markDirty();
            this.robotConfigFileManager.setActiveConfig(this.currentCfgFile);
        }
    }

    public void onDoneButtonPressed(View view) {
        finishOk();
    }

    /* access modifiers changed from: protected */
    public void finishOk() {
        this.controllerConfiguration.setName(this.lynx_module_name.getText().toString());
        finishOk(new EditParameters(this, this.controllerConfiguration));
    }

    public void onCancelButtonPressed(View view) {
        finishCancel();
    }
}
