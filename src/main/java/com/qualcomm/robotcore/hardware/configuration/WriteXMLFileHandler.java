package com.qualcomm.robotcore.hardware.configuration;

import android.util.Xml;
import com.qualcomm.ftccommon.configuration.RobotConfigResFilter;
import com.qualcomm.robotcore.exception.DuplicateNameException;
import com.qualcomm.robotcore.exception.RobotCoreException;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import org.firstinspires.ftc.robotcore.external.function.ThrowingRunnable;
import org.firstinspires.inspection.InspectionState;
import org.xmlpull.v1.XmlSerializer;

public class WriteXMLFileHandler {
    private List<String> duplicates = new ArrayList();
    private int indent = 0;
    private String[] indentation = {"    ", "        ", "            "};
    private HashSet<String> names = new HashSet<>();
    /* access modifiers changed from: private */
    public XmlSerializer serializer = Xml.newSerializer();

    public String toXml(Collection<ControllerConfiguration> collection) {
        return toXml(collection, (String) null, (String) null);
    }

    public String toXml(Collection<ControllerConfiguration> collection, String str, String str2) {
        this.duplicates = new ArrayList();
        this.names = new HashSet<>();
        StringWriter stringWriter = new StringWriter();
        try {
            this.serializer.setOutput(stringWriter);
            this.serializer.startDocument("UTF-8", true);
            this.serializer.ignorableWhitespace("\n");
            this.serializer.startTag(InspectionState.NO_VERSION, RobotConfigResFilter.robotConfigRootTag);
            if (str != null) {
                this.serializer.attribute(InspectionState.NO_VERSION, str, str2);
            }
            this.serializer.ignorableWhitespace("\n");
            for (ControllerConfiguration next : collection) {
                ConfigurationType configurationType = next.getConfigurationType();
                if (configurationType == BuiltInConfigurationType.LYNX_USB_DEVICE) {
                    writeLynxUSBDevice((LynxUsbDeviceConfiguration) next);
                } else if (configurationType == BuiltInConfigurationType.WEBCAM) {
                    writeWebcam((WebcamConfiguration) next);
                }
            }
            this.serializer.endTag(InspectionState.NO_VERSION, RobotConfigResFilter.robotConfigRootTag);
            this.serializer.ignorableWhitespace("\n");
            this.serializer.endDocument();
            return stringWriter.toString();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private void checkForDuplicates(DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            String name = deviceConfiguration.getName();
            if (this.names.contains(name)) {
                this.duplicates.add(name);
            } else {
                this.names.add(name);
            }
        }
    }

    private void writeWebcam(final WebcamConfiguration webcamConfiguration) throws IOException {
        writeUsbController(webcamConfiguration, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                if (webcamConfiguration.getAutoOpen()) {
                    WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, WebcamConfiguration.XMLATTR_AUTO_OPEN_CAMERA, String.valueOf(webcamConfiguration.getAutoOpen()));
                }
            }
        }, (ThrowingRunnable<IOException>) null);
    }

    private void writeLynxUSBDevice(final LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration) throws IOException {
        writeUsbController(lynxUsbDeviceConfiguration, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, LynxUsbDeviceConfiguration.XMLATTR_PARENT_MODULE_ADDRESS, Integer.toString(lynxUsbDeviceConfiguration.getParentModuleAddress()));
            }
        }, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                for (DeviceConfiguration deviceConfiguration : lynxUsbDeviceConfiguration.getDevices()) {
                    if (deviceConfiguration.getConfigurationType() == BuiltInConfigurationType.LYNX_MODULE) {
                        WriteXMLFileHandler.this.writeController((LynxModuleConfiguration) deviceConfiguration, false);
                    } else {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(deviceConfiguration);
                    }
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public <CONTROLLER_T extends ControllerConfiguration<? extends DeviceConfiguration>> void writeController(final CONTROLLER_T controller_t, final boolean z) throws IOException {
        writeNamedController(controller_t, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                if (z) {
                    WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, "serialNumber", controller_t.getSerialNumber().getString());
                } else {
                    WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, DeviceConfiguration.XMLATTR_PORT, String.valueOf(controller_t.getPort()));
                }
            }
        }, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                if (controller_t.getConfigurationType() == BuiltInConfigurationType.LYNX_MODULE) {
                    LynxModuleConfiguration lynxModuleConfiguration = (LynxModuleConfiguration) controller_t;
                    for (DeviceConfiguration access$200 : lynxModuleConfiguration.getMotors()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$200);
                    }
                    for (DeviceConfiguration access$2002 : lynxModuleConfiguration.getServos()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2002);
                    }
                    for (DeviceConfiguration access$2003 : lynxModuleConfiguration.getAnalogInputs()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2003);
                    }
                    for (DeviceConfiguration access$2004 : lynxModuleConfiguration.getPwmOutputs()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2004);
                    }
                    for (DeviceConfiguration access$2005 : lynxModuleConfiguration.getDigitalDevices()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2005);
                    }
                    for (LynxI2cDeviceConfiguration access$2006 : lynxModuleConfiguration.getI2cDevices()) {
                        WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2006);
                    }
                    return;
                }
                for (DeviceConfiguration access$2007 : controller_t.getDevices()) {
                    WriteXMLFileHandler.this.writeDeviceNameAndPort(access$2007);
                }
            }
        });
    }

    /* access modifiers changed from: private */
    public void writeDeviceNameAndPort(final DeviceConfiguration deviceConfiguration) throws IOException {
        if (deviceConfiguration.isEnabled()) {
            writeDevice(deviceConfiguration, new ThrowingRunnable<IOException>() {
                public void run() throws IOException {
                    deviceConfiguration.serializeXmlAttributes(WriteXMLFileHandler.this.serializer);
                }
            }, (ThrowingRunnable<IOException>) null);
        }
    }

    private void writeUsbController(final ControllerConfiguration controllerConfiguration, final ThrowingRunnable<IOException> throwingRunnable, ThrowingRunnable<IOException> throwingRunnable2) throws IOException {
        writeNamedController(controllerConfiguration, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, "serialNumber", controllerConfiguration.getSerialNumber().getString());
                ThrowingRunnable throwingRunnable = throwingRunnable;
                if (throwingRunnable != null) {
                    throwingRunnable.run();
                }
            }
        }, throwingRunnable2);
    }

    private void writeNamedController(final ControllerConfiguration controllerConfiguration, final ThrowingRunnable<IOException> throwingRunnable, ThrowingRunnable<IOException> throwingRunnable2) throws IOException {
        writeDevice(controllerConfiguration, new ThrowingRunnable<IOException>() {
            public void run() throws IOException {
                WriteXMLFileHandler.this.serializer.attribute(InspectionState.NO_VERSION, "name", controllerConfiguration.getName());
                ThrowingRunnable throwingRunnable = throwingRunnable;
                if (throwingRunnable != null) {
                    throwingRunnable.run();
                }
            }
        }, throwingRunnable2);
    }

    private void writeDevice(DeviceConfiguration deviceConfiguration, ThrowingRunnable<IOException> throwingRunnable, ThrowingRunnable<IOException> throwingRunnable2) throws IOException {
        this.serializer.ignorableWhitespace(this.indentation[this.indent]);
        this.serializer.startTag(InspectionState.NO_VERSION, conform(deviceConfiguration.getConfigurationType()));
        checkForDuplicates(deviceConfiguration);
        if (throwingRunnable != null) {
            throwingRunnable.run();
        }
        if (throwingRunnable2 != null) {
            this.serializer.ignorableWhitespace("\n");
            this.indent++;
            throwingRunnable2.run();
            int i = this.indent - 1;
            this.indent = i;
            this.serializer.ignorableWhitespace(this.indentation[i]);
        }
        this.serializer.endTag(InspectionState.NO_VERSION, conform(deviceConfiguration.getConfigurationType()));
        this.serializer.ignorableWhitespace("\n");
    }

    public void writeToFile(String str, File file, String str2) throws RobotCoreException, IOException {
        if (this.duplicates.size() <= 0) {
            boolean z = true;
            if (!file.exists()) {
                z = file.mkdir();
            }
            if (z) {
                File file2 = new File(file, str2);
                FileOutputStream fileOutputStream = null;
                try {
                    FileOutputStream fileOutputStream2 = new FileOutputStream(file2);
                    try {
                        fileOutputStream2.write(str.getBytes());
                        try {
                            fileOutputStream2.close();
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                    } catch (Exception e2) {
                        e = e2;
                        fileOutputStream = fileOutputStream2;
                        try {
                            e.printStackTrace();
                            fileOutputStream.close();
                        } catch (Throwable th) {
                            th = th;
                            try {
                                fileOutputStream.close();
                            } catch (IOException e3) {
                                e3.printStackTrace();
                            }
                            throw th;
                        }
                    } catch (Throwable th2) {
                        th = th2;
                        fileOutputStream = fileOutputStream2;
                        fileOutputStream.close();
                        throw th;
                    }
                } catch (Exception e4) {
                    e = e4;
                    e.printStackTrace();
                    fileOutputStream.close();
                }
            } else {
                throw new RobotCoreException("Unable to create directory");
            }
        } else {
            throw new DuplicateNameException("Duplicate names: " + this.duplicates);
        }
    }

    private String conform(ConfigurationType configurationType) {
        return configurationType.getXmlTag();
    }
}
