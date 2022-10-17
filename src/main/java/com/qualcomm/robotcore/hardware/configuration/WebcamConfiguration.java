package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.util.SerialNumber;
import java.util.LinkedList;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;

public class WebcamConfiguration extends ControllerConfiguration<DeviceConfiguration> {
    public static final String XMLATTR_AUTO_OPEN_CAMERA = "autoOpen";
    protected boolean autoOpen;

    public WebcamConfiguration() {
        this(InspectionState.NO_VERSION, SerialNumber.createFake());
    }

    public WebcamConfiguration(String str, SerialNumber serialNumber) {
        this(str, serialNumber, false);
    }

    public WebcamConfiguration(String str, SerialNumber serialNumber, boolean z) {
        super(str, new LinkedList(), serialNumber, BuiltInConfigurationType.WEBCAM);
        this.autoOpen = z;
    }

    public boolean getAutoOpen() {
        return this.autoOpen;
    }

    private void setAutoOpen(boolean z) {
        this.autoOpen = z;
    }

    /* access modifiers changed from: protected */
    public void deserializeAttributes(XmlPullParser xmlPullParser) {
        super.deserializeAttributes(xmlPullParser);
        String attributeValue = xmlPullParser.getAttributeValue((String) null, XMLATTR_AUTO_OPEN_CAMERA);
        if (attributeValue != null && !attributeValue.isEmpty()) {
            setAutoOpen(Boolean.parseBoolean(attributeValue));
        }
    }
}
