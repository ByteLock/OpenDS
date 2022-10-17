package com.qualcomm.robotcore.hardware;

public class PWMOutputImplEx extends PWMOutputImpl implements PWMOutputEx {
    PWMOutputControllerEx controllerEx;

    public PWMOutputImplEx(PWMOutputController pWMOutputController, int i) {
        super(pWMOutputController, i);
        this.controllerEx = (PWMOutputControllerEx) pWMOutputController;
    }

    public void setPwmEnable() {
        this.controllerEx.setPwmEnable(this.port);
    }

    public void setPwmDisable() {
        this.controllerEx.setPwmDisable(this.port);
    }

    public boolean isPwmEnabled() {
        return this.controllerEx.isPwmEnabled(this.port);
    }
}
