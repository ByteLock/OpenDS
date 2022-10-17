package com.qualcomm.hardware;

import android.content.Context;
import com.qualcomm.hardware.lynx.EmbeddedControlHubModule;
import com.qualcomm.hardware.lynx.LynxAnalogInputController;
import com.qualcomm.hardware.lynx.LynxDcMotorController;
import com.qualcomm.hardware.lynx.LynxDigitalChannelController;
import com.qualcomm.hardware.lynx.LynxModule;
import com.qualcomm.hardware.lynx.LynxNackException;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.hardware.lynx.LynxUsbDevice;
import com.qualcomm.hardware.lynx.LynxVoltageSensor;
import com.qualcomm.robotcore.eventloop.SyncdDevice;
import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.AnalogInputController;
import com.qualcomm.robotcore.hardware.ColorSensor;
import com.qualcomm.robotcore.hardware.DcMotorController;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.DigitalChannelController;
import com.qualcomm.robotcore.hardware.HardwareDevice;
import com.qualcomm.robotcore.hardware.HardwareMap;
import com.qualcomm.robotcore.hardware.OpticalDistanceSensor;
import com.qualcomm.robotcore.hardware.PWMOutputController;
import com.qualcomm.robotcore.hardware.ServoController;
import com.qualcomm.robotcore.hardware.ServoControllerEx;
import com.qualcomm.robotcore.hardware.configuration.BuiltInConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxConstants;
import com.qualcomm.robotcore.hardware.configuration.LynxI2cDeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxModuleConfiguration;
import com.qualcomm.robotcore.hardware.configuration.LynxUsbDeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.ReadXMLFileHandler;
import com.qualcomm.robotcore.hardware.configuration.ServoFlavor;
import com.qualcomm.robotcore.hardware.configuration.WebcamConfiguration;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.AnalogSensorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.DigitalIoDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.I2cDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.util.RobotLog;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;
import org.xmlpull.v1.XmlPullParser;

public class HardwareFactory {
    public static final String TAG = "HardwareFactory";
    private Context context;
    private XmlPullParser xmlPullParser = null;

    public HardwareFactory(Context context2) {
        this.context = context2;
    }

    public HardwareMap createHardwareMap(SyncdDevice.Manager manager) throws RobotCoreException, InterruptedException {
        HardwareMap hardwareMap;
        synchronized (HardwareDeviceManager.scanDevicesLock) {
            RobotLog.m60vv(TAG, "createHardwareMap()");
            EmbeddedControlHubModule.clear();
            hardwareMap = new HardwareMap(this.context);
            if (this.xmlPullParser != null) {
                HardwareDeviceManager hardwareDeviceManager = new HardwareDeviceManager(this.context, manager);
                for (ControllerConfiguration mapControllerConfiguration : new ReadXMLFileHandler(hardwareDeviceManager).parse(this.xmlPullParser)) {
                    mapControllerConfiguration(hardwareMap, hardwareDeviceManager, mapControllerConfiguration);
                }
            } else {
                RobotLog.m60vv(TAG, "no xml to parse: using empty map");
            }
        }
        return hardwareMap;
    }

    public void instantiateConfiguration(HardwareMap hardwareMap, ControllerConfiguration controllerConfiguration, SyncdDevice.Manager manager) throws RobotCoreException, InterruptedException {
        synchronized (HardwareDeviceManager.scanDevicesLock) {
            mapControllerConfiguration(hardwareMap, new HardwareDeviceManager(this.context, manager), controllerConfiguration);
        }
    }

    /* access modifiers changed from: protected */
    public void mapControllerConfiguration(HardwareMap hardwareMap, DeviceManager deviceManager, ControllerConfiguration controllerConfiguration) throws RobotCoreException, InterruptedException {
        ConfigurationType configurationType = controllerConfiguration.getConfigurationType();
        if (configurationType == BuiltInConfigurationType.LYNX_USB_DEVICE) {
            mapLynxUsbDevice(hardwareMap, deviceManager, (LynxUsbDeviceConfiguration) controllerConfiguration);
        } else if (configurationType == BuiltInConfigurationType.WEBCAM) {
            mapWebcam(hardwareMap, deviceManager, (WebcamConfiguration) controllerConfiguration);
        } else {
            RobotLog.m49ee(TAG, "unexpected controller configuration type: %s", configurationType);
        }
    }

    public void setXmlPullParser(XmlPullParser xmlPullParser2) {
        this.xmlPullParser = xmlPullParser2;
    }

    public XmlPullParser getXmlPullParser() {
        return this.xmlPullParser;
    }

    private void mapMotor(HardwareMap hardwareMap, DeviceManager deviceManager, DeviceConfiguration deviceConfiguration, DcMotorController dcMotorController) {
        if (deviceConfiguration.isEnabled()) {
            hardwareMap.dcMotor.put(deviceConfiguration.getName(), deviceManager.createDcMotor(dcMotorController, deviceConfiguration.getPort(), (MotorConfigurationType) deviceConfiguration.getConfigurationType(), deviceConfiguration.getName()));
        }
    }

    private void mapServoDevice(HardwareMap hardwareMap, DeviceManager deviceManager, DeviceConfiguration deviceConfiguration, ServoController servoController) {
        HardwareDevice hardwareDevice;
        if (deviceConfiguration.isEnabled() && deviceConfiguration.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.SERVO)) {
            ServoConfigurationType servoConfigurationType = (ServoConfigurationType) deviceConfiguration.getConfigurationType();
            if (servoConfigurationType.getServoFlavor() == ServoFlavor.STANDARD) {
                hardwareDevice = deviceManager.createServo(servoController, deviceConfiguration.getPort(), deviceConfiguration.getName());
            } else if (servoConfigurationType.getServoFlavor() == ServoFlavor.CONTINUOUS) {
                hardwareDevice = deviceManager.createCRServo(servoController, deviceConfiguration.getPort(), deviceConfiguration.getName());
            } else {
                hardwareDevice = deviceManager.createCustomServoDevice(servoController, deviceConfiguration.getPort(), servoConfigurationType);
            }
            if (hardwareDevice != null) {
                addUserDeviceToMap(hardwareMap, deviceConfiguration, hardwareDevice);
            }
        }
    }

    private void mapLynxServoDevice(HardwareMap hardwareMap, DeviceManager deviceManager, DeviceConfiguration deviceConfiguration, ServoControllerEx servoControllerEx) {
        HardwareDevice hardwareDevice;
        if (deviceConfiguration.isEnabled() && deviceConfiguration.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.SERVO)) {
            ServoConfigurationType servoConfigurationType = (ServoConfigurationType) deviceConfiguration.getConfigurationType();
            if (servoConfigurationType.getServoFlavor() == ServoFlavor.STANDARD) {
                hardwareDevice = deviceManager.createServoEx(servoControllerEx, deviceConfiguration.getPort(), deviceConfiguration.getName(), servoConfigurationType);
            } else if (servoConfigurationType.getServoFlavor() == ServoFlavor.CONTINUOUS) {
                hardwareDevice = deviceManager.createCRServoEx(servoControllerEx, deviceConfiguration.getPort(), deviceConfiguration.getName(), servoConfigurationType);
            } else {
                hardwareDevice = deviceManager.createLynxCustomServoDevice(servoControllerEx, deviceConfiguration.getPort(), servoConfigurationType);
            }
            if (hardwareDevice != null) {
                addUserDeviceToMap(hardwareMap, deviceConfiguration, hardwareDevice);
            }
        }
    }

    private void buildLynxDevices(List<DeviceConfiguration> list, HardwareMap hardwareMap, DeviceManager deviceManager, AnalogInputController analogInputController) {
        for (DeviceConfiguration next : list) {
            if (next.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.ANALOG_SENSOR)) {
                mapAnalogSensor(hardwareMap, deviceManager, analogInputController, next);
            }
        }
    }

    private void buildLynxDevices(List<DeviceConfiguration> list, HardwareMap hardwareMap, DeviceManager deviceManager, DigitalChannelController digitalChannelController) {
        for (DeviceConfiguration next : list) {
            if (next.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.DIGITAL_IO)) {
                mapDigitalDevice(hardwareMap, deviceManager, digitalChannelController, next);
            }
        }
    }

    private void buildLynxI2cDevices(List<LynxI2cDeviceConfiguration> list, HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule) {
        for (LynxI2cDeviceConfiguration next : list) {
            ConfigurationType configurationType = next.getConfigurationType();
            if (configurationType == BuiltInConfigurationType.IR_SEEKER_V3) {
                mapIrSeekerV3Device(hardwareMap, deviceManager, lynxModule, next);
            } else if (configurationType == BuiltInConfigurationType.ADAFRUIT_COLOR_SENSOR) {
                mapAdafruitColorSensor(hardwareMap, deviceManager, lynxModule, next);
            } else if (configurationType == BuiltInConfigurationType.LYNX_COLOR_SENSOR) {
                mapLynxColorSensor(hardwareMap, deviceManager, lynxModule, next);
            } else if (configurationType == BuiltInConfigurationType.COLOR_SENSOR) {
                mapModernRoboticsColorSensor(hardwareMap, deviceManager, lynxModule, next);
            } else if (configurationType == BuiltInConfigurationType.GYRO) {
                mapModernRoboticsGyro(hardwareMap, deviceManager, lynxModule, next);
            } else if (configurationType != BuiltInConfigurationType.NOTHING) {
                if (!configurationType.isDeviceFlavor(ConfigurationType.DeviceFlavor.I2C)) {
                    RobotLog.m64w("Unexpected device type connected to I2c Controller while parsing XML: " + configurationType.toString());
                } else if (configurationType instanceof I2cDeviceConfigurationType) {
                    mapUserI2cDevice(hardwareMap, deviceManager, lynxModule, next);
                }
            }
        }
    }

    private void mapIrSeekerV3Device(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            hardwareMap.irSeekerSensor.put(deviceConfiguration.getName(), deviceManager.createMRI2cIrSeekerSensorV3(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName()));
        }
    }

    private void mapDigitalDevice(HardwareMap hardwareMap, DeviceManager deviceManager, DigitalChannelController digitalChannelController, DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            HardwareDevice hardwareDevice = null;
            if (deviceConfiguration.getConfigurationType() == BuiltInConfigurationType.TOUCH_SENSOR) {
                hardwareDevice = deviceManager.createMRDigitalTouchSensor(digitalChannelController, deviceConfiguration.getPort(), deviceConfiguration.getName());
            } else if (deviceConfiguration.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.DIGITAL_IO)) {
                hardwareDevice = deviceManager.createDigitalDevice(digitalChannelController, deviceConfiguration.getPort(), (DigitalIoDeviceConfigurationType) deviceConfiguration.getConfigurationType());
            }
            if (hardwareDevice != null) {
                addUserDeviceToMap(hardwareMap, deviceConfiguration, hardwareDevice);
            }
        }
    }

    private void mapAnalogSensor(HardwareMap hardwareMap, DeviceManager deviceManager, AnalogInputController analogInputController, DeviceConfiguration deviceConfiguration) {
        HardwareDevice createAnalogSensor;
        if (deviceConfiguration.isEnabled() && deviceConfiguration.getConfigurationType().isDeviceFlavor(ConfigurationType.DeviceFlavor.ANALOG_SENSOR) && (createAnalogSensor = deviceManager.createAnalogSensor(analogInputController, deviceConfiguration.getPort(), (AnalogSensorConfigurationType) deviceConfiguration.getConfigurationType())) != null) {
            addUserDeviceToMap(hardwareMap, deviceConfiguration, createAnalogSensor);
        }
    }

    private void mapPwmOutputDevice(HardwareMap hardwareMap, DeviceManager deviceManager, PWMOutputController pWMOutputController, DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            hardwareMap.pwmOutput.put(deviceConfiguration.getName(), deviceManager.createPwmOutputDevice(pWMOutputController, deviceConfiguration.getPort(), deviceConfiguration.getName()));
        }
    }

    private void mapI2cDeviceSynch(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        hardwareMap.i2cDeviceSynch.put(deviceConfiguration.getName(), deviceManager.createI2cDeviceSynch(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName()));
    }

    private void addUserDeviceToMap(HardwareMap hardwareMap, DeviceConfiguration deviceConfiguration, HardwareDevice hardwareDevice) {
        hardwareMap.put(deviceConfiguration.getName(), hardwareDevice);
        for (HardwareMap.DeviceMapping next : hardwareMap.allDeviceMappings) {
            if (next.getDeviceTypeClass().isInstance(hardwareDevice)) {
                maybeAddToMapping(next, deviceConfiguration.getName(), next.cast(hardwareDevice));
            }
        }
    }

    private <T extends HardwareDevice> void maybeAddToMapping(HardwareMap.DeviceMapping<T> deviceMapping, String str, T t) {
        if (!deviceMapping.contains(str)) {
            deviceMapping.putLocal(str, t);
        }
    }

    private void mapUserI2cDevice(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        HardwareDevice createUserI2cDevice;
        if (deviceConfiguration.isEnabled() && (createUserI2cDevice = deviceManager.createUserI2cDevice(lynxModule, deviceConfiguration.getI2cChannel(), (I2cDeviceConfigurationType) deviceConfiguration.getConfigurationType(), deviceConfiguration.getName())) != null) {
            addUserDeviceToMap(hardwareMap, deviceConfiguration, createUserI2cDevice);
        }
    }

    private void mapWebcam(HardwareMap hardwareMap, DeviceManager deviceManager, WebcamConfiguration webcamConfiguration) throws RobotCoreException, InterruptedException {
        if (webcamConfiguration.isEnabled()) {
            SerialNumber serialNumber = webcamConfiguration.getSerialNumber();
            if (webcamConfiguration.getAutoOpen()) {
                RobotLog.m49ee(TAG, "support for auto-opening webcams is not yet implemented: %s", serialNumber);
                return;
            }
            WebcamName createWebcamName = deviceManager.createWebcamName(serialNumber, webcamConfiguration.getName());
            if (createWebcamName != null) {
                hardwareMap.put(serialNumber, webcamConfiguration.getName(), createWebcamName);
            }
        }
    }

    private void mapLynxUsbDevice(HardwareMap hardwareMap, DeviceManager deviceManager, LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration) throws RobotCoreException, InterruptedException {
        HardwareMap hardwareMap2 = hardwareMap;
        DeviceManager deviceManager2 = deviceManager;
        if (lynxUsbDeviceConfiguration.isEnabled()) {
            SerialNumber serialNumber = lynxUsbDeviceConfiguration.getSerialNumber();
            LynxUsbDevice lynxUsbDevice = (LynxUsbDevice) deviceManager2.createLynxUsbDevice(serialNumber, lynxUsbDeviceConfiguration.getName());
            try {
                if (lynxUsbDeviceConfiguration.isSystemSynthetic()) {
                    lynxUsbDevice.setSystemSynthetic(true);
                }
                boolean z = !LynxConstants.isEmbeddedSerialNumber(serialNumber);
                Iterator<LynxUsbDevice> it = hardwareMap2.getAll(LynxUsbDevice.class).iterator();
                while (true) {
                    if (it.hasNext()) {
                        if (!LynxConstants.isEmbeddedSerialNumber(it.next().getSerialNumber())) {
                            z = false;
                            break;
                        }
                    } else {
                        break;
                    }
                }
                ArrayList<LynxModule> arrayList = new ArrayList<>();
                HashMap hashMap = new HashMap();
                int parentModuleAddress = lynxUsbDeviceConfiguration.getParentModuleAddress();
                for (DeviceConfiguration next : lynxUsbDeviceConfiguration.getModules()) {
                    int port = next.getPort();
                    hashMap.put(Integer.valueOf(port), next.getName());
                    LynxModule lynxModule = (LynxModule) deviceManager2.createLynxModule(lynxUsbDevice, port, parentModuleAddress == port, next.getName());
                    arrayList.add(lynxModule);
                    if (((LynxModuleConfiguration) next).isSystemSynthetic()) {
                        lynxModule.setSystemSynthetic(true);
                    }
                }
                HashMap hashMap2 = new HashMap();
                for (LynxModule lynxModule2 : arrayList) {
                    if (lynxModule2.isParent()) {
                        connectModule(lynxUsbDevice, lynxModule2, hashMap, hashMap2, z && hashMap2.isEmpty());
                    }
                }
                for (LynxModule lynxModule3 : arrayList) {
                    if (!lynxModule3.isParent()) {
                        connectModule(lynxUsbDevice, lynxModule3, hashMap, hashMap2, false);
                    }
                }
                HashMap hashMap3 = hashMap;
                mapLynxModuleComponents(hardwareMap, deviceManager, lynxUsbDeviceConfiguration, lynxUsbDevice, hashMap2);
                for (Map.Entry entry : hashMap2.entrySet()) {
                    int intValue = ((Integer) entry.getKey()).intValue();
                    LynxModule lynxModule4 = (LynxModule) entry.getValue();
                    hardwareMap2.put(lynxModule4.getModuleSerialNumber(), (String) hashMap3.get(Integer.valueOf(intValue)), lynxModule4);
                }
                hardwareMap2.put(serialNumber, lynxUsbDeviceConfiguration.getName(), lynxUsbDevice);
            } catch (LynxNackException e) {
                throw e.wrap();
            } catch (RobotCoreException | RuntimeException e2) {
                lynxUsbDevice.close();
                hardwareMap2.remove(serialNumber, lynxUsbDeviceConfiguration.getName(), lynxUsbDevice);
                throw e2;
            }
        }
    }

    private void connectModule(LynxUsbDevice lynxUsbDevice, LynxModule lynxModule, Map<Integer, String> map, Map<Integer, LynxModule> map2, boolean z) throws InterruptedException {
        try {
            LynxModule addConfiguredModule = lynxUsbDevice.addConfiguredModule(lynxModule);
            if (z) {
                addConfiguredModule.enablePhoneCharging(true);
            }
            map2.put(Integer.valueOf(addConfiguredModule.getModuleAddress()), addConfiguredModule);
        } catch (LynxNackException | RobotCoreException | RuntimeException unused) {
            lynxUsbDevice.noteMissingModule(lynxModule, map.get(Integer.valueOf(lynxModule.getModuleAddress())));
        }
    }

    private void mapLynxModuleComponents(HardwareMap hardwareMap, DeviceManager deviceManager, LynxUsbDeviceConfiguration lynxUsbDeviceConfiguration, LynxUsbDevice lynxUsbDevice, Map<Integer, LynxModule> map) throws LynxNackException, RobotCoreException, InterruptedException {
        for (DeviceConfiguration next : lynxUsbDeviceConfiguration.getModules()) {
            LynxModule lynxModule = map.get(Integer.valueOf(next.getPort()));
            if (lynxModule != null) {
                LynxModuleConfiguration lynxModuleConfiguration = (LynxModuleConfiguration) next;
                LynxDcMotorController lynxDcMotorController = new LynxDcMotorController(this.context, lynxModule);
                hardwareMap.dcMotorController.put(next.getName(), lynxDcMotorController);
                for (DeviceConfiguration next2 : lynxModuleConfiguration.getMotors()) {
                    if (next2.isEnabled()) {
                        hardwareMap.dcMotor.put(next2.getName(), deviceManager.createDcMotorEx(lynxDcMotorController, next2.getPort(), (MotorConfigurationType) next2.getConfigurationType(), next2.getName()));
                    }
                }
                LynxServoController lynxServoController = new LynxServoController(this.context, lynxModule);
                hardwareMap.servoController.put(next.getName(), lynxServoController);
                for (DeviceConfiguration mapLynxServoDevice : lynxModuleConfiguration.getServos()) {
                    mapLynxServoDevice(hardwareMap, deviceManager, mapLynxServoDevice, lynxServoController);
                }
                hardwareMap.voltageSensor.put(next.getName(), new LynxVoltageSensor(this.context, lynxModule));
                LynxAnalogInputController lynxAnalogInputController = new LynxAnalogInputController(this.context, lynxModule);
                hardwareMap.put(next.getName(), lynxAnalogInputController);
                buildLynxDevices(lynxModuleConfiguration.getAnalogInputs(), hardwareMap, deviceManager, (AnalogInputController) lynxAnalogInputController);
                LynxDigitalChannelController lynxDigitalChannelController = new LynxDigitalChannelController(this.context, lynxModule);
                hardwareMap.put(next.getName(), lynxDigitalChannelController);
                buildLynxDevices(lynxModuleConfiguration.getDigitalDevices(), hardwareMap, deviceManager, (DigitalChannelController) lynxDigitalChannelController);
                buildLynxI2cDevices(lynxModuleConfiguration.getI2cDevices(), hardwareMap, deviceManager, lynxModule);
            }
        }
    }

    private void mapAdafruitColorSensor(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        hardwareMap.colorSensor.put(deviceConfiguration.getName(), deviceManager.createAdafruitI2cColorSensor(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName()));
    }

    private void mapLynxColorSensor(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        ColorSensor createLynxColorRangeSensor = deviceManager.createLynxColorRangeSensor(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName());
        hardwareMap.colorSensor.put(deviceConfiguration.getName(), createLynxColorRangeSensor);
        hardwareMap.opticalDistanceSensor.put(deviceConfiguration.getName(), (OpticalDistanceSensor) createLynxColorRangeSensor);
    }

    private void mapModernRoboticsColorSensor(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            hardwareMap.colorSensor.put(deviceConfiguration.getName(), deviceManager.createModernRoboticsI2cColorSensor(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName()));
        }
    }

    private void mapModernRoboticsGyro(HardwareMap hardwareMap, DeviceManager deviceManager, LynxModule lynxModule, DeviceConfiguration deviceConfiguration) {
        if (deviceConfiguration.isEnabled()) {
            hardwareMap.gyroSensor.put(deviceConfiguration.getName(), deviceManager.createModernRoboticsI2cGyroSensor(lynxModule, deviceConfiguration.getI2cChannel(), deviceConfiguration.getName()));
        }
    }

    public static void noteSerialNumberType(Context context2, SerialNumber serialNumber, String str) {
        SerialNumber.noteSerialNumberType(serialNumber, str);
    }

    public static String getDeviceDisplayName(Context context2, SerialNumber serialNumber) {
        return SerialNumber.getDeviceDisplayName(serialNumber);
    }
}
