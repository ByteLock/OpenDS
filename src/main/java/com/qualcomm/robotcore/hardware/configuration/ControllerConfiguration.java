package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.xmlpull.v1.XmlPullParser;

public abstract class ControllerConfiguration<ITEM_T extends DeviceConfiguration> extends DeviceConfiguration implements Serializable {
    public static final String XMLATTR_SERIAL_NUMBER = "serialNumber";
    private List<ITEM_T> devices;
    private boolean isSystemSynthetic;
    private boolean knownToBeAttached;
    private SerialNumber serialNumber;

    public ControllerConfiguration(String str, SerialNumber serialNumber2, ConfigurationType configurationType) {
        this(str, new ArrayList(), serialNumber2, configurationType);
    }

    public ControllerConfiguration(String str, List<ITEM_T> list, SerialNumber serialNumber2, ConfigurationType configurationType) {
        super(configurationType);
        this.knownToBeAttached = false;
        this.isSystemSynthetic = false;
        super.setName(str);
        this.devices = list;
        this.serialNumber = serialNumber2;
    }

    public static ControllerConfiguration forType(String str, SerialNumber serialNumber2, ConfigurationType configurationType) {
        if (configurationType == BuiltInConfigurationType.LYNX_USB_DEVICE) {
            return new LynxUsbDeviceConfiguration(str, new LinkedList(), serialNumber2);
        }
        if (configurationType == BuiltInConfigurationType.LYNX_MODULE) {
            return new LynxModuleConfiguration(str);
        }
        if (configurationType == BuiltInConfigurationType.WEBCAM) {
            return new WebcamConfiguration(str, serialNumber2);
        }
        return null;
    }

    public List<ITEM_T> getDevices() {
        return this.devices;
    }

    public ConfigurationType getConfigurationType() {
        return super.getConfigurationType();
    }

    public void setSerialNumber(SerialNumber serialNumber2) {
        this.serialNumber = serialNumber2;
    }

    public SerialNumber getSerialNumber() {
        return this.serialNumber;
    }

    public boolean isKnownToBeAttached() {
        return this.knownToBeAttached;
    }

    public void setKnownToBeAttached(boolean z) {
        this.knownToBeAttached = z;
    }

    public boolean isSystemSynthetic() {
        return this.isSystemSynthetic;
    }

    public void setSystemSynthetic(boolean z) {
        this.isSystemSynthetic = z;
    }

    public void setDevices(List<ITEM_T> list) {
        this.devices = list;
    }

    public DeviceManager.UsbDeviceType toUSBDeviceType() {
        return getConfigurationType().toUSBDeviceType();
    }

    /* access modifiers changed from: protected */
    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        super.deserializeAttributes(xmlPullParser);
        String attributeValue = xmlPullParser.getAttributeValue((String) null, "serialNumber");
        if (attributeValue != null) {
            setSerialNumber(SerialNumber.fromString(attributeValue));
            setPort(-1);
            return;
        }
        setSerialNumber(SerialNumber.createFake());
    }
}
