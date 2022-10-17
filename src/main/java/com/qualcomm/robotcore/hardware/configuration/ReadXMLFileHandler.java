package com.qualcomm.robotcore.hardware.configuration;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.util.GlobalWarningSource;
import com.qualcomm.robotcore.util.RobotLog;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import org.firstinspires.ftc.robotcore.internal.system.Assert;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlPullParser;
import org.xmlpull.v1.XmlPullParserException;
import org.xmlpull.v1.XmlPullParserFactory;

public class ReadXMLFileHandler extends ConfigurationUtility {
    public static final String TAG = "ReadXMLFileHandler";
    private static WarningManager warningManager;
    private DeviceManager deviceManager;
    private XmlPullParser parser;

    static {
        WarningManager warningManager2 = new WarningManager();
        warningManager = warningManager2;
        RobotLog.registerGlobalWarningSource(warningManager2);
    }

    public ReadXMLFileHandler() {
        this.deviceManager = null;
    }

    public ReadXMLFileHandler(DeviceManager deviceManager2) {
        this();
        this.deviceManager = deviceManager2;
    }

    public static XmlPullParser xmlPullParserFromReader(Reader reader) {
        XmlPullParser xmlPullParser = null;
        try {
            XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
            newInstance.setNamespaceAware(true);
            xmlPullParser = newInstance.newPullParser();
            xmlPullParser.setInput(reader);
            return xmlPullParser;
        } catch (XmlPullParserException e) {
            e.printStackTrace();
            return xmlPullParser;
        }
    }

    public List<ControllerConfiguration> parse(Reader reader) throws RobotCoreException {
        this.parser = xmlPullParserFromReader(reader);
        return parseDocument();
    }

    public List<ControllerConfiguration> parse(XmlPullParser xmlPullParser) throws RobotCoreException {
        this.parser = xmlPullParser;
        return parseDocument();
    }

    private List<ControllerConfiguration> parseDocument() throws RobotCoreException {
        warningManager.actuallyClearWarning();
        List<ControllerConfiguration> list = null;
        try {
            int eventType = this.parser.getEventType();
            while (eventType != 1) {
                if (eventType == 2) {
                    if (deform(this.parser.getName()) == BuiltInConfigurationType.ROBOT) {
                        list = parseRobot();
                    } else {
                        parseIgnoreElementChildren();
                    }
                }
                eventType = this.parser.next();
            }
        } catch (XmlPullParserException e) {
            RobotLog.m64w("XmlPullParserException");
            e.printStackTrace();
        } catch (IOException e2) {
            RobotLog.m64w("IOException");
            e2.printStackTrace();
        }
        if (list == null) {
            list = new ArrayList<>();
        }
        addEmbeddedLynxModuleIfNecessary(list);
        return list;
    }

    private List<ControllerConfiguration> parseRobot() throws XmlPullParserException, IOException, RobotCoreException {
        Assert.assertTrue(this.parser.getEventType() == 2 && deform(this.parser.getName()) == BuiltInConfigurationType.ROBOT);
        ArrayList arrayList = new ArrayList();
        int next = this.parser.next();
        while (next != 3) {
            if (next == 2) {
                ConfigurationType deform = deform(this.parser.getName());
                ControllerConfiguration controllerConfiguration = null;
                if (deform == BuiltInConfigurationType.LYNX_USB_DEVICE) {
                    controllerConfiguration = new LynxUsbDeviceConfiguration();
                } else if (deform == BuiltInConfigurationType.WEBCAM) {
                    controllerConfiguration = new WebcamConfiguration();
                } else {
                    parseIgnoreElementChildren();
                }
                if (controllerConfiguration != null) {
                    controllerConfiguration.deserialize(this.parser, this);
                    arrayList.add(controllerConfiguration);
                }
            }
            next = this.parser.next();
        }
        return arrayList;
    }

    private void addEmbeddedLynxModuleIfNecessary(List<ControllerConfiguration> list) {
        if (LynxConstants.isRevControlHub()) {
            for (ControllerConfiguration serialNumber : list) {
                if (LynxConstants.isEmbeddedSerialNumber(serialNumber.getSerialNumber())) {
                    RobotLog.m60vv(TAG, "embedded lynx USB device is already present");
                    return;
                }
            }
            RobotLog.m60vv(TAG, "auto-configuring embedded lynx USB device");
            list.add(buildNewEmbeddedLynxUsbDevice(this.deviceManager));
        }
    }

    public List<ControllerConfiguration> parse(InputStream inputStream) throws RobotCoreException {
        this.parser = null;
        try {
            XmlPullParserFactory newInstance = XmlPullParserFactory.newInstance();
            newInstance.setNamespaceAware(true);
            XmlPullParser newPullParser = newInstance.newPullParser();
            this.parser = newPullParser;
            newPullParser.setInput(inputStream, (String) null);
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        return parseDocument();
    }

    public void onDeviceParsed(DeviceConfiguration deviceConfiguration) {
        noteExistingName(deviceConfiguration.getConfigurationType(), deviceConfiguration.getName());
        handleDeprecation(deviceConfiguration);
        if (deviceConfiguration instanceof LynxModuleConfiguration) {
            LynxModuleConfiguration lynxModuleConfiguration = (LynxModuleConfiguration) deviceConfiguration;
            if (lynxModuleConfiguration.getModuleAddress() > 10 && lynxModuleConfiguration.getModuleAddress() != 173) {
                warningManager.addWarning(String.format(Locale.ENGLISH, "A module is configured with address %d. Addresses higher than %d are reserved for system use", new Object[]{Integer.valueOf(lynxModuleConfiguration.getModuleAddress()), 10}));
            }
        }
    }

    private void handleDeprecation(DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.getConfigurationType().isDeprecated()) {
            warningManager.addWarning(String.format("%s is a deprecated configuration type and may be removed in a future release", new Object[]{deviceConfiguration.getConfigurationType().getDisplayName(ConfigurationType.DisplayNameFlavor.Normal)}));
        }
    }

    private void parseIgnoreElementChildren() throws IOException, XmlPullParserException {
        Assert.assertTrue(this.parser.getEventType() == 2);
        int next = this.parser.next();
        while (next != 3 && next != 1) {
            if (next == 2) {
                parseIgnoreElementChildren();
            }
            next = this.parser.next();
        }
    }

    public static ConfigurationType deform(String str) {
        if (str != null) {
            return ConfigurationTypeManager.getInstance().configurationTypeFromTag(str);
        }
        return null;
    }

    private static class WarningManager implements GlobalWarningSource {
        private String warningMessage;
        private int warningMessageSuppressionCount;

        public boolean shouldTriggerWarningSound() {
            return false;
        }

        private WarningManager() {
            this.warningMessageSuppressionCount = 0;
            this.warningMessage = InspectionState.NO_VERSION;
        }

        /* access modifiers changed from: private */
        public synchronized void actuallyClearWarning() {
            clearGlobalWarning();
            this.warningMessage = InspectionState.NO_VERSION;
        }

        /* access modifiers changed from: private */
        public synchronized void addWarning(String str) {
            if (this.warningMessage.isEmpty()) {
                this.warningMessage = str;
            } else {
                this.warningMessage += String.format("; %s", new Object[]{str});
            }
        }

        public synchronized String getGlobalWarning() {
            return this.warningMessageSuppressionCount > 0 ? InspectionState.NO_VERSION : this.warningMessage;
        }

        public synchronized void suppressGlobalWarning(boolean z) {
            if (z) {
                this.warningMessageSuppressionCount++;
            } else {
                this.warningMessageSuppressionCount--;
            }
        }

        public synchronized void setGlobalWarning(String str) {
        }

        public synchronized void clearGlobalWarning() {
            this.warningMessageSuppressionCount = 0;
        }
    }
}
