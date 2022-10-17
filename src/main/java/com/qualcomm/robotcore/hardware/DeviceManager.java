package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.exception.RobotCoreException;
import com.qualcomm.robotcore.hardware.configuration.DeviceConfiguration;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.AnalogSensorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.DigitalIoDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.I2cDeviceConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.MotorConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.ServoConfigurationType;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.external.hardware.camera.WebcamName;

public interface DeviceManager {
    ColorSensor createAdafruitI2cColorSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    HardwareDevice createAnalogSensor(AnalogInputController analogInputController, int i, AnalogSensorConfigurationType analogSensorConfigurationType);

    CRServo createCRServo(ServoController servoController, int i, String str);

    CRServo createCRServoEx(ServoControllerEx servoControllerEx, int i, String str, ServoConfigurationType servoConfigurationType);

    HardwareDevice createCustomServoDevice(ServoController servoController, int i, ServoConfigurationType servoConfigurationType);

    DcMotor createDcMotor(DcMotorController dcMotorController, int i, MotorConfigurationType motorConfigurationType, String str);

    DcMotor createDcMotorEx(DcMotorController dcMotorController, int i, MotorConfigurationType motorConfigurationType, String str);

    HardwareDevice createDigitalDevice(DigitalChannelController digitalChannelController, int i, DigitalIoDeviceConfigurationType digitalIoDeviceConfigurationType);

    I2cDeviceSynch createI2cDeviceSynch(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    LED createLED(DigitalChannelController digitalChannelController, int i, String str);

    ColorSensor createLynxColorRangeSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    HardwareDevice createLynxCustomServoDevice(ServoControllerEx servoControllerEx, int i, ServoConfigurationType servoConfigurationType);

    RobotCoreLynxModule createLynxModule(RobotCoreLynxUsbDevice robotCoreLynxUsbDevice, int i, boolean z, String str);

    RobotCoreLynxUsbDevice createLynxUsbDevice(SerialNumber serialNumber, String str) throws RobotCoreException, InterruptedException;

    TouchSensor createMRDigitalTouchSensor(DigitalChannelController digitalChannelController, int i, String str);

    IrSeekerSensor createMRI2cIrSeekerSensorV3(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    ColorSensor createModernRoboticsI2cColorSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    GyroSensor createModernRoboticsI2cGyroSensor(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, String str);

    PWMOutput createPwmOutputDevice(PWMOutputController pWMOutputController, int i, String str);

    Servo createServo(ServoController servoController, int i, String str);

    Servo createServoEx(ServoControllerEx servoControllerEx, int i, String str, ServoConfigurationType servoConfigurationType);

    HardwareDevice createUserI2cDevice(RobotCoreLynxModule robotCoreLynxModule, DeviceConfiguration.I2cChannel i2cChannel, I2cDeviceConfigurationType i2cDeviceConfigurationType, String str);

    WebcamName createWebcamName(SerialNumber serialNumber, String str) throws RobotCoreException, InterruptedException;

    ScannedDevices scanForUsbDevices() throws RobotCoreException;

    public enum UsbDeviceType {
        FTDI_USB_UNKNOWN_DEVICE,
        LYNX_USB_DEVICE,
        WEBCAM,
        UNKNOWN_DEVICE;

        public static UsbDeviceType from(String str) {
            for (UsbDeviceType usbDeviceType : values()) {
                if (usbDeviceType.toString().equals(str)) {
                    return usbDeviceType;
                }
            }
            return UNKNOWN_DEVICE;
        }
    }
}
