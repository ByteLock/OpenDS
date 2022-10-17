package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.ControlSystem;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.annotations.DeviceProperties;
import com.qualcomm.robotcore.util.ClassUtil;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.Serializable;
import org.firstinspires.ftc.robotcore.internal.opmode.OnBotJavaDeterminer;
import org.firstinspires.inspection.InspectionState;

public abstract class UserConfigurationType implements ConfigurationType, Serializable {
    @Expose
    private boolean builtIn = false;
    @Expose
    private ControlSystem[] compatibleControlSystems = {ControlSystem.REV_HUB};
    @Expose
    protected String description;
    @Expose
    private final ConfigurationType.DeviceFlavor flavor;
    @Expose
    private boolean isDeprecated;
    @Expose
    private boolean isExternalLibraries;
    @Expose
    private boolean isOnBotJava;
    @Expose
    protected String name = InspectionState.NO_VERSION;
    /* access modifiers changed from: private */
    @Expose
    public String xmlTag;
    @Expose
    private String[] xmlTagAliases;

    public UserConfigurationType(Class cls, ConfigurationType.DeviceFlavor deviceFlavor, String str) {
        this.flavor = deviceFlavor;
        this.xmlTag = str;
        this.isOnBotJava = OnBotJavaDeterminer.isOnBotJava(cls);
        this.isExternalLibraries = OnBotJavaDeterminer.isExternalLibraries(cls);
        this.isDeprecated = cls.isAnnotationPresent(Deprecated.class);
    }

    protected UserConfigurationType(ConfigurationType.DeviceFlavor deviceFlavor) {
        this.flavor = deviceFlavor;
        this.xmlTag = InspectionState.NO_VERSION;
    }

    public void processAnnotation(DeviceProperties deviceProperties) {
        this.description = ClassUtil.decodeStringRes(deviceProperties.description());
        this.builtIn = deviceProperties.builtIn();
        this.compatibleControlSystems = deviceProperties.compatibleControlSystems();
        this.xmlTagAliases = deviceProperties.xmlTagAliases();
        if (!deviceProperties.name().isEmpty()) {
            this.name = ClassUtil.decodeStringRes(deviceProperties.name().trim());
        }
    }

    public void finishedAnnotations(Class cls) {
        if (this.name.isEmpty()) {
            this.name = cls.getSimpleName();
        }
        if (this.xmlTagAliases == null) {
            this.xmlTagAliases = new String[0];
        }
    }

    public boolean isCompatibleWith(ControlSystem controlSystem) {
        for (ControlSystem controlSystem2 : this.compatibleControlSystems) {
            if (controlSystem == controlSystem2) {
                return true;
            }
        }
        return false;
    }

    protected static class SerializationProxy implements Serializable {
        protected String xmlTag;

        public SerializationProxy(UserConfigurationType userConfigurationType) {
            this.xmlTag = userConfigurationType.xmlTag;
        }

        private Object readResolve() {
            return ConfigurationTypeManager.getInstance().configurationTypeFromTag(this.xmlTag);
        }
    }

    private Object writeReplace() {
        return new SerializationProxy(this);
    }

    private void readObject(ObjectInputStream objectInputStream) throws InvalidObjectException {
        throw new InvalidObjectException("proxy required");
    }

    public ConfigurationType.DeviceFlavor getDeviceFlavor() {
        return this.flavor;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public boolean isOnBotJava() {
        return this.isOnBotJava;
    }

    public boolean isExternalLibraries() {
        return this.isExternalLibraries;
    }

    public boolean isBuiltIn() {
        return this.builtIn;
    }

    public String getDisplayName(ConfigurationType.DisplayNameFlavor displayNameFlavor) {
        return this.name;
    }

    public String getXmlTag() {
        return this.xmlTag;
    }

    public String[] getXmlTagAliases() {
        return this.xmlTagAliases;
    }

    public DeviceManager.UsbDeviceType toUSBDeviceType() {
        return DeviceManager.UsbDeviceType.FTDI_USB_UNKNOWN_DEVICE;
    }

    public boolean isDeviceFlavor(ConfigurationType.DeviceFlavor deviceFlavor) {
        return this.flavor == deviceFlavor;
    }

    public boolean isDeprecated() {
        return this.isDeprecated;
    }
}
