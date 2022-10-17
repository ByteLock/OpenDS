package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.IOException;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LynxUsbDeviceConfiguration extends ControllerConfiguration<LynxModuleConfiguration> {
    private static final boolean ASSUME_EMBEDDED_MODULE_ADDRESS = AppUtil.getInstance().isRobotController();
    public static final String XMLATTR_PARENT_MODULE_ADDRESS = "parentModuleAddress";
    int parentModuleAddress = 1;
    private int recordedParentModuleAddress = 1;

    public LynxUsbDeviceConfiguration() {
        super(InspectionState.NO_VERSION, new LinkedList(), SerialNumber.createFake(), BuiltInConfigurationType.LYNX_USB_DEVICE);
    }

    public LynxUsbDeviceConfiguration(String str, List<LynxModuleConfiguration> list, SerialNumber serialNumber) {
        super(str, new LinkedList(list), serialNumber, BuiltInConfigurationType.LYNX_USB_DEVICE);
        finishInitialization();
    }

    public void setSerialNumber(SerialNumber serialNumber) {
        super.setSerialNumber(serialNumber);
        for (LynxModuleConfiguration usbDeviceSerialNumber : getModules()) {
            usbDeviceSerialNumber.setUsbDeviceSerialNumber(serialNumber);
        }
    }

    public int getParentModuleAddress() {
        return this.parentModuleAddress;
    }

    public void setParentModuleAddress(int i) {
        this.parentModuleAddress = i;
    }

    public List<LynxModuleConfiguration> getModules() {
        return getDevices();
    }

    /* access modifiers changed from: protected */
    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        super.deserializeAttributes(xmlPullParser);
        String attributeValue = xmlPullParser.getAttributeValue((String) null, XMLATTR_PARENT_MODULE_ADDRESS);
        if (attributeValue != null && !attributeValue.isEmpty()) {
            this.recordedParentModuleAddress = Integer.parseInt(attributeValue);
        }
        if (!ASSUME_EMBEDDED_MODULE_ADDRESS || !getSerialNumber().isEmbedded()) {
            setParentModuleAddress(this.recordedParentModuleAddress);
        } else {
            setParentModuleAddress(173);
        }
    }

    /* access modifiers changed from: protected */
    public void deserializeChildElement(ConfigurationType configurationType, XmlPullParser xmlPullParser, ReadXMLFileHandler readXMLFileHandler) throws IOException, XmlPullParserException, RobotCoreException {
        super.deserializeChildElement(configurationType, xmlPullParser, readXMLFileHandler);
        if (configurationType == BuiltInConfigurationType.LYNX_MODULE) {
            LynxModuleConfiguration lynxModuleConfiguration = new LynxModuleConfiguration();
            lynxModuleConfiguration.deserialize(xmlPullParser, readXMLFileHandler);
            if (ASSUME_EMBEDDED_MODULE_ADDRESS && getSerialNumber().isEmbedded() && lynxModuleConfiguration.getModuleAddress() == this.recordedParentModuleAddress) {
                lynxModuleConfiguration.setModuleAddress(173);
            }
            lynxModuleConfiguration.setIsParent(lynxModuleConfiguration.getModuleAddress() == this.parentModuleAddress);
            getModules().add(lynxModuleConfiguration);
        }
    }

    /* access modifiers changed from: protected */
    public void onDeserializationComplete(ReadXMLFileHandler readXMLFileHandler) {
        finishInitialization();
        super.onDeserializationComplete(readXMLFileHandler);
    }

    private void finishInitialization() {
        Collections.sort(getModules(), new Comparator<DeviceConfiguration>() {
            public int compare(DeviceConfiguration deviceConfiguration, DeviceConfiguration deviceConfiguration2) {
                if (deviceConfiguration.getPort() == 173) {
                    return -1;
                }
                return deviceConfiguration.getPort() - deviceConfiguration2.getPort();
            }
        });
        int i = 0;
        for (LynxModuleConfiguration next : getModules()) {
            next.setUsbDeviceSerialNumber(getSerialNumber());
            if (next.isParent()) {
                setParentModuleAddress(next.getModuleAddress());
            }
            if (next.getModuleAddress() == 173) {
                i++;
                if (!getSerialNumber().isEmbedded() || i > 1) {
                    RobotLog.setGlobalErrorMsg("An Expansion Hub is configured with address 173, which is reserved for the Control Hub. You need to change the Expansion Hub's address, and make a new configuration file");
                }
            }
        }
    }
}
