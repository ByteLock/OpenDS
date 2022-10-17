package com.qualcomm.ftccommon.configuration;

import android.content.Context;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.system.Misc;

public class RobotConfigMap implements Serializable {
    Map<SerialNumber, ControllerConfiguration> map;

    public RobotConfigMap(Collection<ControllerConfiguration> collection) {
        this.map = new HashMap();
        for (ControllerConfiguration next : collection) {
            put(next.getSerialNumber(), next);
        }
    }

    public RobotConfigMap(Map<SerialNumber, ControllerConfiguration> map2) {
        this.map = new HashMap();
        this.map = new HashMap(map2);
    }

    public RobotConfigMap(RobotConfigMap robotConfigMap) {
        this(robotConfigMap.map);
    }

    public RobotConfigMap() {
        this.map = new HashMap();
    }

    public boolean contains(SerialNumber serialNumber) {
        return this.map.containsKey(serialNumber);
    }

    public ControllerConfiguration get(SerialNumber serialNumber) {
        return this.map.get(serialNumber);
    }

    public void put(SerialNumber serialNumber, ControllerConfiguration controllerConfiguration) {
        this.map.put(serialNumber, controllerConfiguration);
    }

    public boolean remove(SerialNumber serialNumber) {
        return this.map.remove(serialNumber) != null;
    }

    public int size() {
        return this.map.size();
    }

    public Collection<SerialNumber> serialNumbers() {
        return this.map.keySet();
    }

    public Collection<ControllerConfiguration> controllerConfigurations() {
        return this.map.values();
    }

    public void writeToLog(String str, String str2) {
        RobotLog.m61vv(str, "robotConfigMap: %s", str2);
        for (ControllerConfiguration next : controllerConfigurations()) {
            RobotLog.m61vv(str, "   serial=%s id=0x%08x name='%s' ", next.getSerialNumber(), Integer.valueOf(next.hashCode()), next.getName());
        }
    }

    public void writeToLog(String str, String str2, ControllerConfiguration controllerConfiguration) {
        writeToLog(str, str2);
        RobotLog.m61vv(str, "  :serial=%s id=0x%08x name='%s' ", controllerConfiguration.getSerialNumber(), Integer.valueOf(controllerConfiguration.hashCode()), controllerConfiguration.getName());
    }

    /* access modifiers changed from: package-private */
    public boolean allControllersAreBound() {
        for (ControllerConfiguration serialNumber : controllerConfigurations()) {
            if (serialNumber.getSerialNumber().isFake()) {
                return false;
            }
        }
        return true;
    }

    public void bindUnboundControllers(ScannedDevices scannedDevices) {
        List list;
        ScannedDevices scannedDevices2 = new ScannedDevices(scannedDevices);
        for (ControllerConfiguration serialNumber : controllerConfigurations()) {
            scannedDevices2.remove(serialNumber.getSerialNumber());
        }
        HashMap hashMap = new HashMap();
        for (Map.Entry next : scannedDevices2.entrySet()) {
            ConfigurationType fromUSBDeviceType = BuiltInConfigurationType.fromUSBDeviceType((DeviceManager.UsbDeviceType) next.getValue());
            if (fromUSBDeviceType != BuiltInConfigurationType.UNKNOWN) {
                List list2 = (List) hashMap.get(fromUSBDeviceType);
                if (list2 == null) {
                    list2 = new LinkedList();
                    hashMap.put(fromUSBDeviceType, list2);
                }
                list2.add((SerialNumber) next.getKey());
            }
        }
        for (ControllerConfiguration next2 : controllerConfigurations()) {
            if (next2.getSerialNumber().isFake() && (list = (List) hashMap.get(next2.getConfigurationType())) != null && !list.isEmpty()) {
                next2.setSerialNumber((SerialNumber) list.remove(0));
            }
        }
        ArrayList<ControllerConfiguration> arrayList = new ArrayList<>(controllerConfigurations());
        this.map.clear();
        for (ControllerConfiguration controllerConfiguration : arrayList) {
            put(controllerConfiguration.getSerialNumber(), controllerConfiguration);
        }
    }

    public void setSerialNumber(ControllerConfiguration controllerConfiguration, SerialNumber serialNumber) {
        remove(controllerConfiguration.getSerialNumber());
        controllerConfiguration.setSerialNumber(serialNumber);
        put(serialNumber, controllerConfiguration);
    }

    public void swapSerialNumbers(ControllerConfiguration controllerConfiguration, ControllerConfiguration controllerConfiguration2) {
        SerialNumber serialNumber = controllerConfiguration.getSerialNumber();
        controllerConfiguration.setSerialNumber(controllerConfiguration2.getSerialNumber());
        controllerConfiguration2.setSerialNumber(serialNumber);
        put(controllerConfiguration.getSerialNumber(), controllerConfiguration);
        put(controllerConfiguration2.getSerialNumber(), controllerConfiguration2);
        boolean isKnownToBeAttached = controllerConfiguration.isKnownToBeAttached();
        controllerConfiguration.setKnownToBeAttached(controllerConfiguration2.isKnownToBeAttached());
        controllerConfiguration2.setKnownToBeAttached(isKnownToBeAttached);
    }

    public boolean isSwappable(ControllerConfiguration controllerConfiguration, ScannedDevices scannedDevices, Context context) {
        return !getEligibleSwapTargets(controllerConfiguration, scannedDevices, context).isEmpty();
    }

    public List<ControllerConfiguration> getEligibleSwapTargets(ControllerConfiguration controllerConfiguration, ScannedDevices scannedDevices, Context context) {
        return new LinkedList();
    }

    /* access modifiers changed from: protected */
    public String generateName(Context context, ConfigurationType configurationType, List<ControllerConfiguration> list) {
        int i = 1;
        while (true) {
            String formatForUser = Misc.formatForUser("%s %d", configurationType.getDisplayName(ConfigurationType.DisplayNameFlavor.Normal), Integer.valueOf(i));
            if (!nameExists(formatForUser, list)) {
                return formatForUser;
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public boolean nameExists(String str, List<ControllerConfiguration> list) {
        for (ControllerConfiguration name : list) {
            if (name.getName().equalsIgnoreCase(str)) {
                return true;
            }
        }
        for (ControllerConfiguration name2 : controllerConfigurations()) {
            if (name2.getName().equalsIgnoreCase(str)) {
                return true;
            }
        }
        return false;
    }

    /* access modifiers changed from: protected */
    public boolean containsSerialNumber(List<ControllerConfiguration> list, SerialNumber serialNumber) {
        for (ControllerConfiguration serialNumber2 : list) {
            if (serialNumber2.getSerialNumber().equals((Object) serialNumber)) {
                return true;
            }
        }
        return false;
    }
}
