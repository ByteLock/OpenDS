package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlSerializer;

public class DeviceConfiguration implements Serializable, Comparable<DeviceConfiguration> {
    public static final String DISABLED_DEVICE_NAME = "NO$DEVICE$ATTACHED";
    public static final String TAG = "DeviceConfiguration";
    public static final String XMLATTR_NAME = "name";
    public static final String XMLATTR_PORT = "port";
    private boolean enabled;
    protected String name;
    private int port;
    private ConfigurationType type;

    /* access modifiers changed from: protected */
    public void deserializeChildElement(ConfigurationType configurationType, XmlPullParser xmlPullParser, ReadXMLFileHandler readXMLFileHandler) throws IOException, XmlPullParserException, RobotCoreException {
    }

    public DeviceConfiguration(int i, ConfigurationType configurationType, String str, boolean z) {
        BuiltInConfigurationType builtInConfigurationType = BuiltInConfigurationType.NOTHING;
        this.port = i;
        this.type = configurationType;
        this.name = str;
        this.enabled = z;
    }

    public DeviceConfiguration() {
        this(0);
    }

    public DeviceConfiguration(int i) {
        this(i, BuiltInConfigurationType.NOTHING, DISABLED_DEVICE_NAME, false);
    }

    public DeviceConfiguration(ConfigurationType configurationType) {
        this(0, configurationType, InspectionState.NO_VERSION, false);
    }

    public DeviceConfiguration(int i, ConfigurationType configurationType) {
        this(i, configurationType, DISABLED_DEVICE_NAME, false);
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean z) {
        this.enabled = z;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String str) {
        this.name = str;
    }

    public void setConfigurationType(ConfigurationType configurationType) {
        this.type = configurationType;
    }

    public static void sortByName(List<? extends DeviceConfiguration> list) {
        Collections.sort(list, new Comparator<DeviceConfiguration>() {
            public int compare(DeviceConfiguration deviceConfiguration, DeviceConfiguration deviceConfiguration2) {
                return deviceConfiguration.getName().compareToIgnoreCase(deviceConfiguration2.getName());
            }
        });
    }

    public ConfigurationType getConfigurationType() {
        return this.type;
    }

    public ConfigurationType getSpinnerChoiceType() {
        return getConfigurationType();
    }

    public int getPort() {
        return this.port;
    }

    public void setPort(int i) {
        this.port = i;
    }

    public I2cChannel getI2cChannel() {
        return new I2cChannel(getPort());
    }

    public static class I2cChannel {
        public final int channel;

        public I2cChannel(int i) {
            this.channel = i;
        }

        public String toString() {
            return "channel=" + this.channel;
        }
    }

    public int compareTo(DeviceConfiguration deviceConfiguration) {
        return getPort() - deviceConfiguration.getPort();
    }

    public void serializeXmlAttributes(XmlSerializer xmlSerializer) {
        try {
            xmlSerializer.attribute(InspectionState.NO_VERSION, "name", getName());
            xmlSerializer.attribute(InspectionState.NO_VERSION, XMLATTR_PORT, String.valueOf(getPort()));
        } catch (Exception e) {
            RobotLog.m50ee(TAG, (Throwable) e, "exception serializing");
            throw new RuntimeException(e);
        }
    }

    public final void deserialize(XmlPullParser xmlPullParser, ReadXMLFileHandler readXMLFileHandler) throws IOException, XmlPullParserException, RobotCoreException {
        String name2 = xmlPullParser.getName();
        deserializeAttributes(xmlPullParser);
        setConfigurationType(ReadXMLFileHandler.deform(name2));
        setEnabled(true);
        int next = xmlPullParser.next();
        String name3 = xmlPullParser.getName();
        ConfigurationType deform = ReadXMLFileHandler.deform(name3);
        while (next != 1) {
            if (next != 3 || name3 == null || !name3.equals(name2)) {
                if (next == 2) {
                    deserializeChildElement(deform, xmlPullParser, readXMLFileHandler);
                }
                next = xmlPullParser.next();
                name3 = xmlPullParser.getName();
                deform = ReadXMLFileHandler.deform(name3);
            } else {
                onDeserializationComplete(readXMLFileHandler);
                return;
            }
        }
        RobotLog.logAndThrow("Reached the end of the XML file while processing a device.");
    }

    /* access modifiers changed from: protected */
    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        int i;
        setName(xmlPullParser.getAttributeValue((String) null, "name"));
        String attributeValue = xmlPullParser.getAttributeValue((String) null, XMLATTR_PORT);
        if (attributeValue == null) {
            i = -1;
        } else {
            i = Integer.parseInt(attributeValue);
        }
        setPort(i);
    }

    /* access modifiers changed from: protected */
    public void onDeserializationComplete(ReadXMLFileHandler readXMLFileHandler) {
        readXMLFileHandler.onDeviceParsed(this);
    }
}
