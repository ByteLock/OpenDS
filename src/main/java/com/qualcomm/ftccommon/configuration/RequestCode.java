package com.qualcomm.ftccommon.configuration;

public enum RequestCode {
    NOTHING(0),
    EDIT_MOTOR_CONTROLLER(1),
    EDIT_SERVO_CONTROLLER(2),
    EDIT_LEGACY_MODULE(3),
    EDIT_DEVICE_INTERFACE_MODULE(4),
    EDIT_MATRIX_CONTROLLER(5),
    EDIT_PWM_PORT(6),
    EDIT_I2C_PORT(7),
    EDIT_ANALOG_INPUT(8),
    EDIT_DIGITAL(9),
    EDIT_ANALOG_OUTPUT(10),
    EDIT_LYNX_MODULE(11),
    EDIT_LYNX_USB_DEVICE(12),
    EDIT_I2C_BUS0(13),
    EDIT_I2C_BUS1(14),
    EDIT_I2C_BUS2(15),
    EDIT_I2C_BUS3(16),
    EDIT_MOTOR_LIST(17),
    EDIT_SERVO_LIST(18),
    EDIT_SWAP_USB_DEVICES(19),
    EDIT_FILE(20),
    NEW_FILE(21),
    AUTO_CONFIGURE(22),
    CONFIG_FROM_TEMPLATE(23),
    EDIT_USB_CAMERA(24);
    
    public final int value;

    private RequestCode(int i) {
        this.value = i;
    }

    public static RequestCode fromString(String str) {
        for (RequestCode requestCode : values()) {
            if (requestCode.toString().equals(str)) {
                return requestCode;
            }
        }
        return NOTHING;
    }

    public static RequestCode fromValue(int i) {
        for (RequestCode requestCode : values()) {
            if (requestCode.value == i) {
                return requestCode;
            }
        }
        return NOTHING;
    }
}
