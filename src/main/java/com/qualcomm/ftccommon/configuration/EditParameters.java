package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.os.Bundle;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.system.Assert;

public class EditParameters<ITEM_T extends DeviceConfiguration> implements Serializable {
    private DeviceConfiguration configuration;
    private boolean configuringControlHubParent;
    private ControlSystem controlSystem;
    private RobotConfigFile currentCfgFile;
    private List<ITEM_T> currentItems;
    private List<RobotConfigFile> extantRobotConfigurations;
    private boolean growable;
    private boolean haveRobotConfigMapParameter;
    private int i2cBus;
    private int initialPortNumber;
    private boolean isConfigDirty;
    private Class<ITEM_T> itemClass;
    private int maxItemCount;
    private RobotConfigMap robotConfigMap;
    private ScannedDevices scannedDevices;

    public EditParameters(EditActivity editActivity, DeviceConfiguration deviceConfiguration) {
        this(editActivity);
        this.configuration = deviceConfiguration;
    }

    public EditParameters(EditActivity editActivity, DeviceConfiguration deviceConfiguration, RobotConfigMap robotConfigMap2) {
        this(editActivity);
        this.configuration = deviceConfiguration;
        this.robotConfigMap = robotConfigMap2;
        this.haveRobotConfigMapParameter = true;
    }

    public EditParameters(EditActivity editActivity, Class<ITEM_T> cls, List<ITEM_T> list) {
        this(editActivity);
        setItems(cls, list);
    }

    public EditParameters(EditActivity editActivity, DeviceConfiguration deviceConfiguration, Class<ITEM_T> cls, List<ITEM_T> list) {
        this(editActivity);
        this.configuration = deviceConfiguration;
        setItems(cls, list);
    }

    public EditParameters(EditActivity editActivity, Class<ITEM_T> cls, List<ITEM_T> list, int i) {
        this(editActivity);
        setItems(cls, list);
        this.maxItemCount = i;
    }

    private void setItems(Class<ITEM_T> cls, List<ITEM_T> list) {
        this.itemClass = cls;
        this.currentItems = list;
        for (ITEM_T isInstance : list) {
            Assert.assertTrue(cls.isInstance(isInstance));
        }
    }

    public EditParameters(EditActivity editActivity) {
        this.isConfigDirty = false;
        this.configuration = null;
        this.currentItems = null;
        this.itemClass = null;
        this.initialPortNumber = 0;
        this.maxItemCount = 0;
        this.i2cBus = 0;
        this.growable = false;
        this.scannedDevices = new ScannedDevices();
        this.robotConfigMap = new RobotConfigMap();
        this.haveRobotConfigMapParameter = false;
        this.extantRobotConfigurations = new ArrayList();
        this.controlSystem = null;
        this.configuringControlHubParent = false;
        this.currentCfgFile = null;
        this.isConfigDirty = editActivity.currentCfgFile.isDirty();
    }

    public EditParameters() {
        this.isConfigDirty = false;
        this.configuration = null;
        this.currentItems = null;
        this.itemClass = null;
        this.initialPortNumber = 0;
        this.maxItemCount = 0;
        this.i2cBus = 0;
        this.growable = false;
        this.scannedDevices = new ScannedDevices();
        this.robotConfigMap = new RobotConfigMap();
        this.haveRobotConfigMapParameter = false;
        this.extantRobotConfigurations = new ArrayList();
        this.controlSystem = null;
        this.configuringControlHubParent = false;
        this.currentCfgFile = null;
    }

    public DeviceConfiguration getConfiguration() {
        return this.configuration;
    }

    public List<ITEM_T> getCurrentItems() {
        List<ITEM_T> list = this.currentItems;
        return list == null ? new LinkedList() : list;
    }

    public Class<ITEM_T> getItemClass() {
        Assert.assertNotNull(this.itemClass);
        return this.itemClass;
    }

    public int getMaxItemCount() {
        List<ITEM_T> list = this.currentItems;
        if (list == null) {
            return this.maxItemCount;
        }
        return Math.max(this.maxItemCount, list.size());
    }

    public boolean isGrowable() {
        return this.growable;
    }

    public void setGrowable(boolean z) {
        this.growable = z;
    }

    public ScannedDevices getScannedDevices() {
        return this.scannedDevices;
    }

    public void setScannedDevices(ScannedDevices scannedDevices2) {
        this.scannedDevices = scannedDevices2;
    }

    public void setInitialPortNumber(int i) {
        this.initialPortNumber = i;
    }

    public int getInitialPortNumber() {
        return this.initialPortNumber;
    }

    public int getI2cBus() {
        return this.i2cBus;
    }

    public void setI2cBus(int i) {
        this.i2cBus = i;
    }

    public RobotConfigMap getRobotConfigMap() {
        return this.robotConfigMap;
    }

    public void setRobotConfigMap(RobotConfigMap robotConfigMap2) {
        this.robotConfigMap = robotConfigMap2;
        this.haveRobotConfigMapParameter = true;
    }

    public boolean haveRobotConfigMapParameter() {
        return this.haveRobotConfigMapParameter;
    }

    public List<RobotConfigFile> getExtantRobotConfigurations() {
        return this.extantRobotConfigurations;
    }

    public void setExtantRobotConfigurations(List<RobotConfigFile> list) {
        this.extantRobotConfigurations = list;
    }

    public ControlSystem getControlSystem() {
        return this.controlSystem;
    }

    public void setControlSystem(ControlSystem controlSystem2) {
        this.controlSystem = controlSystem2;
    }

    public boolean configuringControlHubParent() {
        return this.configuringControlHubParent;
    }

    public void setConfiguringControlHubParent(boolean z) {
        this.configuringControlHubParent = z;
    }

    public RobotConfigFile getCurrentCfgFile() {
        return this.currentCfgFile;
    }

    public void setCurrentCfgFile(RobotConfigFile robotConfigFile) {
        this.currentCfgFile = robotConfigFile;
    }

    public void putIntent(Intent intent) {
        intent.putExtras(toBundle());
    }

    public Bundle toBundle() {
        Bundle bundle = new Bundle();
        DeviceConfiguration deviceConfiguration = this.configuration;
        if (deviceConfiguration != null) {
            bundle.putSerializable("configuration", deviceConfiguration);
        }
        ScannedDevices scannedDevices2 = this.scannedDevices;
        if (scannedDevices2 != null && scannedDevices2.size() > 0) {
            bundle.putString("scannedDevices", this.scannedDevices.toSerializationString());
        }
        RobotConfigMap robotConfigMap2 = this.robotConfigMap;
        if (robotConfigMap2 != null && robotConfigMap2.size() > 0) {
            bundle.putSerializable("robotConfigMap", this.robotConfigMap);
        }
        List<RobotConfigFile> list = this.extantRobotConfigurations;
        if (list != null && list.size() > 0) {
            bundle.putString("extantRobotConfigurations", RobotConfigFileManager.serializeXMLConfigList(this.extantRobotConfigurations));
        }
        ControlSystem controlSystem2 = this.controlSystem;
        if (controlSystem2 != null) {
            bundle.putSerializable("controlSystem", controlSystem2);
        }
        bundle.putBoolean("configuringControlHubParent", this.configuringControlHubParent);
        RobotConfigFile robotConfigFile = this.currentCfgFile;
        if (robotConfigFile != null) {
            bundle.putString("currentCfgFile", RobotConfigFileManager.serializeConfig(robotConfigFile));
        }
        bundle.putBoolean("haveRobotConfigMap", this.haveRobotConfigMapParameter);
        bundle.putInt("initialPortNumber", this.initialPortNumber);
        bundle.putInt("maxItemCount", this.maxItemCount);
        bundle.putInt("i2cBus", this.i2cBus);
        bundle.putBoolean("growable", this.growable);
        bundle.putBoolean("isConfigDirty", this.isConfigDirty);
        Class<ITEM_T> cls = this.itemClass;
        if (cls != null) {
            bundle.putString("itemClass", cls.getCanonicalName());
        }
        if (this.currentItems != null) {
            for (int i = 0; i < this.currentItems.size(); i++) {
                bundle.putSerializable(String.valueOf(i), (Serializable) this.currentItems.get(i));
            }
        }
        return bundle;
    }

    public static <RESULT_ITEM extends DeviceConfiguration> EditParameters<RESULT_ITEM> fromIntent(EditActivity editActivity, Intent intent) {
        return fromBundle(editActivity, intent.getExtras());
    }

    public static <RESULT_ITEM extends DeviceConfiguration> EditParameters<RESULT_ITEM> fromBundle(EditActivity editActivity, Bundle bundle) {
        EditParameters<RESULT_ITEM> editParameters = new EditParameters<>();
        if (bundle == null) {
            return editParameters;
        }
        for (String str : bundle.keySet()) {
            if (str.equals("configuration")) {
                editParameters.configuration = (DeviceConfiguration) bundle.getSerializable(str);
            } else if (str.equals("scannedDevices")) {
                editParameters.scannedDevices = ScannedDevices.fromSerializationString(bundle.getString(str));
            } else if (str.equals("robotConfigMap")) {
                editParameters.robotConfigMap = (RobotConfigMap) bundle.getSerializable(str);
            } else if (str.equals("haveRobotConfigMap")) {
                editParameters.haveRobotConfigMapParameter = bundle.getBoolean(str);
            } else if (str.equals("extantRobotConfigurations")) {
                editParameters.extantRobotConfigurations = RobotConfigFileManager.deserializeXMLConfigList(bundle.getString(str));
            } else if (str.equals("controlSystem")) {
                editParameters.controlSystem = (ControlSystem) bundle.getSerializable(str);
            } else if (str.equals("configuringControlHubParent")) {
                editParameters.configuringControlHubParent = bundle.getBoolean(str);
            } else if (str.equals("currentCfgFile")) {
                editParameters.currentCfgFile = RobotConfigFileManager.deserializeConfig(bundle.getString(str));
            } else if (str.equals("initialPortNumber")) {
                editParameters.initialPortNumber = bundle.getInt(str);
            } else if (str.equals("i2cBus")) {
                editParameters.i2cBus = bundle.getInt(str);
            } else if (str.equals("maxItemCount")) {
                editParameters.maxItemCount = bundle.getInt(str);
            } else if (str.equals("growable")) {
                editParameters.growable = bundle.getBoolean(str);
            } else if (str.equals("isConfigDirty")) {
                editParameters.isConfigDirty = bundle.getBoolean(str);
            } else if (str.equals("itemClass")) {
                try {
                    editParameters.itemClass = Class.forName(bundle.getString(str));
                } catch (ClassNotFoundException unused) {
                    editParameters.itemClass = null;
                }
            } else {
                try {
                    int parseInt = Integer.parseInt(str);
                    DeviceConfiguration deviceConfiguration = (DeviceConfiguration) bundle.getSerializable(str);
                    if (editParameters.currentItems == null) {
                        editParameters.currentItems = new ArrayList();
                    }
                    editParameters.currentItems.add(parseInt, deviceConfiguration);
                } catch (NumberFormatException unused2) {
                }
            }
        }
        if (editParameters.isConfigDirty) {
            editActivity.currentCfgFile.markDirty();
        }
        return editParameters;
    }
}
