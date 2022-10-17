package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlSerializer;

public class LynxI2cDeviceConfiguration extends DeviceConfiguration {
    public static final String TAG = "LynxI2cDeviceConfiguration";
    public static final String XMLATTR_BUS = "bus";
    protected int bus = 0;

    public int getBus() {
        return this.bus;
    }

    public void setBus(int i) {
        this.bus = i;
    }

    public DeviceConfiguration.I2cChannel getI2cChannel() {
        return new DeviceConfiguration.I2cChannel(getBus());
    }

    public void serializeXmlAttributes(XmlSerializer xmlSerializer) {
        try {
            super.serializeXmlAttributes(xmlSerializer);
            xmlSerializer.attribute(InspectionState.NO_VERSION, XMLATTR_BUS, String.valueOf(getBus()));
        } catch (Exception e) {
            RobotLog.m50ee(TAG, (Throwable) e, "exception serializing");
            throw new RuntimeException(e);
        }
    }

    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        super.deserializeAttributes(xmlPullParser);
        String attributeValue = xmlPullParser.getAttributeValue((String) null, XMLATTR_BUS);
        setBus(attributeValue == null ? getPort() : Integer.parseInt(attributeValue));
    }
}
