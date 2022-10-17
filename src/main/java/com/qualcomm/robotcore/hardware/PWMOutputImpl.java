package com.qualcomm.robotcore.hardware;

import com.qualcomm.robotcore.C0705R;
import org.firstinspires.ftc.robotcore.system.AppUtil;

public class PWMOutputImpl implements PWMOutput {
    protected PWMOutputController controller;
    protected int port;

    public void close() {
    }

    public int getVersion() {
        return 1;
    }

    public void resetDeviceConfigurationForOpMode() {
    }

    public PWMOutputImpl(PWMOutputController pWMOutputController, int i) {
        this.controller = pWMOutputController;
        this.port = i;
    }

    public void setPulseWidthOutputTime(int i) {
        this.controller.setPulseWidthOutputTime(this.port, i);
    }

    public int getPulseWidthOutputTime() {
        return this.controller.getPulseWidthOutputTime(this.port);
    }

    public void setPulseWidthPeriod(int i) {
        this.controller.setPulseWidthPeriod(this.port, i);
    }

    public int getPulseWidthPeriod() {
        return this.controller.getPulseWidthPeriod(this.port);
    }

    public HardwareDevice.Manufacturer getManufacturer() {
        return this.controller.getManufacturer();
    }

    public String getDeviceName() {
        return AppUtil.getDefContext().getString(C0705R.string.configTypePulseWidthDevice);
    }

    public String getConnectionInfo() {
        return this.controller.getConnectionInfo() + "; port " + this.port;
    }
}
