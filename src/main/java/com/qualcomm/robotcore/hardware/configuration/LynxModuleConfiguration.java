package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.util.SerialNumber;
import java.io.IOException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.usb.LynxModuleSerialNumber;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;

public class LynxModuleConfiguration extends ControllerConfiguration<DeviceConfiguration> {
    public static final String TAG = "LynxModuleConfiguration";
    private List<DeviceConfiguration> analogInputs;
    private List<DeviceConfiguration> digitalDevices;
    private List<LynxI2cDeviceConfiguration> i2cDevices;
    private boolean isParent;
    private List<DeviceConfiguration> motors;
    private List<DeviceConfiguration> pwmOutputs;
    private List<DeviceConfiguration> servos;
    private SerialNumber usbDeviceSerialNumber;

    public LynxModuleConfiguration() {
        this(InspectionState.NO_VERSION);
    }

    public LynxModuleConfiguration(String str) {
        super(str, new ArrayList(), SerialNumber.createFake(), BuiltInConfigurationType.LYNX_MODULE);
        this.isParent = false;
        this.motors = new LinkedList();
        this.servos = new LinkedList();
        this.pwmOutputs = new LinkedList();
        this.digitalDevices = new LinkedList();
        this.analogInputs = new LinkedList();
        this.i2cDevices = new LinkedList();
        this.usbDeviceSerialNumber = SerialNumber.createFake();
        this.servos = ConfigurationUtility.buildEmptyServos(0, 6);
        this.motors = ConfigurationUtility.buildEmptyMotors(0, 4);
        this.pwmOutputs = ConfigurationUtility.buildEmptyDevices(0, 4, BuiltInConfigurationType.NOTHING);
        this.analogInputs = ConfigurationUtility.buildEmptyDevices(0, 4, BuiltInConfigurationType.NOTHING);
        this.digitalDevices = ConfigurationUtility.buildEmptyDevices(0, 8, BuiltInConfigurationType.NOTHING);
        this.i2cDevices = new LinkedList();
    }

    public void setPort(int i) {
        super.setPort(i);
        setSerialNumber(new LynxModuleSerialNumber(this.usbDeviceSerialNumber, i));
    }

    public void setModuleAddress(int i) {
        setPort(i);
    }

    public int getModuleAddress() {
        return getPort();
    }

    public void setIsParent(boolean z) {
        this.isParent = z;
    }

    public boolean isParent() {
        return this.isParent;
    }

    public void setUsbDeviceSerialNumber(SerialNumber serialNumber) {
        this.usbDeviceSerialNumber = serialNumber;
        setSerialNumber(new LynxModuleSerialNumber(serialNumber, getModuleAddress()));
    }

    public void setSerialNumber(SerialNumber serialNumber) {
        super.setSerialNumber(serialNumber);
    }

    public SerialNumber getUsbDeviceSerialNumber() {
        return this.usbDeviceSerialNumber;
    }

    public SerialNumber getModuleSerialNumber() {
        return getSerialNumber();
    }

    public List<DeviceConfiguration> getServos() {
        return this.servos;
    }

    public void setServos(List<DeviceConfiguration> list) {
        this.servos = list;
    }

    public List<DeviceConfiguration> getMotors() {
        return this.motors;
    }

    public void setMotors(List<DeviceConfiguration> list) {
        this.motors = list;
    }

    public List<DeviceConfiguration> getAnalogInputs() {
        return this.analogInputs;
    }

    public void setAnalogInputs(List<DeviceConfiguration> list) {
        this.analogInputs = list;
    }

    public List<DeviceConfiguration> getPwmOutputs() {
        return this.pwmOutputs;
    }

    public void setPwmOutputs(List<DeviceConfiguration> list) {
        this.pwmOutputs = list;
    }

    public List<LynxI2cDeviceConfiguration> getI2cDevices() {
        return this.i2cDevices;
    }

    public void setI2cDevices(List<LynxI2cDeviceConfiguration> list) {
        this.i2cDevices = new LinkedList();
        for (LynxI2cDeviceConfiguration next : list) {
            if (next.isEnabled() && next.getPort() >= 0 && next.getPort() < 4) {
                this.i2cDevices.add(next);
            }
        }
    }

    public List<LynxI2cDeviceConfiguration> getI2cDevices(int i) {
        LinkedList linkedList = new LinkedList();
        for (LynxI2cDeviceConfiguration next : this.i2cDevices) {
            if (next.getBus() == i) {
                linkedList.add(next);
            }
        }
        return linkedList;
    }

    public void setI2cDevices(int i, List<LynxI2cDeviceConfiguration> list) {
        LinkedList linkedList = new LinkedList();
        for (LynxI2cDeviceConfiguration next : this.i2cDevices) {
            if (next.getBus() != i) {
                linkedList.add(next);
            }
        }
        for (LynxI2cDeviceConfiguration next2 : list) {
            if (next2.isEnabled()) {
                next2.setBus(i);
                linkedList.add(next2);
            }
        }
        this.i2cDevices = linkedList;
    }

    public List<DeviceConfiguration> getDigitalDevices() {
        return this.digitalDevices;
    }

    public void setDigitalDevices(List<DeviceConfiguration> list) {
        this.digitalDevices = list;
    }

    /* access modifiers changed from: protected */
    public void deserializeChildElement(ConfigurationType configurationType, XmlPullParser xmlPullParser, ReadXMLFileHandler readXMLFileHandler) throws IOException, XmlPullParserException, RobotCoreException {
        super.deserializeChildElement(configurationType, xmlPullParser, readXMLFileHandler);
        if (configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.SERVO)) {
            DeviceConfiguration deviceConfiguration = new DeviceConfiguration();
            deviceConfiguration.deserialize(xmlPullParser, readXMLFileHandler);
            getServos().set(deviceConfiguration.getPort() + 0, deviceConfiguration);
        } else if (configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.MOTOR)) {
            DeviceConfiguration deviceConfiguration2 = new DeviceConfiguration();
            deviceConfiguration2.deserialize(xmlPullParser, readXMLFileHandler);
            getMotors().set(deviceConfiguration2.getPort() + 0, deviceConfiguration2);
        } else if (configurationType == BuiltInConfigurationType.PULSE_WIDTH_DEVICE) {
            DeviceConfiguration deviceConfiguration3 = new DeviceConfiguration();
            deviceConfiguration3.deserialize(xmlPullParser, readXMLFileHandler);
            getPwmOutputs().set(deviceConfiguration3.getPort(), deviceConfiguration3);
        } else if (configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.ANALOG_SENSOR)) {
            DeviceConfiguration deviceConfiguration4 = new DeviceConfiguration();
            deviceConfiguration4.deserialize(xmlPullParser, readXMLFileHandler);
            getAnalogInputs().set(deviceConfiguration4.getPort(), deviceConfiguration4);
        } else if (configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.DIGITAL_IO)) {
            DeviceConfiguration deviceConfiguration5 = new DeviceConfiguration();
            deviceConfiguration5.deserialize(xmlPullParser, readXMLFileHandler);
            getDigitalDevices().set(deviceConfiguration5.getPort(), deviceConfiguration5);
        } else if (configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.I2C)) {
            LynxI2cDeviceConfiguration lynxI2cDeviceConfiguration = new LynxI2cDeviceConfiguration();
            lynxI2cDeviceConfiguration.deserialize(xmlPullParser, readXMLFileHandler);
            getI2cDevices().add(lynxI2cDeviceConfiguration);
        }
    }

    /* access modifiers changed from: protected */
    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        super.deserializeAttributes(xmlPullParser);
        setModuleAddress(getPort());
    }
}
