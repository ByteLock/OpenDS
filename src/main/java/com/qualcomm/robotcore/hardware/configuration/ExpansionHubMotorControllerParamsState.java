package com.qualcomm.robotcore.hardware.configuration;

import com.google.gson.annotations.Expose;
import com.qualcomm.hardware.lynx.LynxServoController;
import com.qualcomm.robotcore.hardware.DcMotor;
import com.qualcomm.robotcore.hardware.MotorControlAlgorithm;
import com.qualcomm.robotcore.hardware.PIDFCoefficients;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFPositionParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFVelocityParams;
import java.io.Serializable;
import org.firstinspires.ftc.robotcore.system.Assert;
import org.firstinspires.ftc.robotcore.system.Misc;

public class ExpansionHubMotorControllerParamsState implements Serializable, Cloneable {
    @Expose
    public MotorControlAlgorithm algorithm;
    @Expose

    /* renamed from: d */
    public double f129d;
    @Expose

    /* renamed from: f */
    public double f130f;
    @Expose

    /* renamed from: i */
    public double f131i;
    @Expose
    public DcMotor.RunMode mode;
    @Expose

    /* renamed from: p */
    public double f132p;

    public ExpansionHubMotorControllerParamsState() {
        this.mode = null;
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        Assert.assertTrue(isDefault());
    }

    public ExpansionHubMotorControllerParamsState(DcMotor.RunMode runMode, PIDFCoefficients pIDFCoefficients) {
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.mode = runMode;
        this.f132p = pIDFCoefficients.f124p;
        this.f131i = pIDFCoefficients.f123i;
        this.f129d = pIDFCoefficients.f121d;
        this.f130f = pIDFCoefficients.f122f;
        this.algorithm = pIDFCoefficients.algorithm;
    }

    public ExpansionHubMotorControllerParamsState(ExpansionHubMotorControllerPositionParams expansionHubMotorControllerPositionParams) {
        this.mode = null;
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.mode = DcMotor.RunMode.RUN_TO_POSITION;
        this.f132p = expansionHubMotorControllerPositionParams.mo10131P();
        this.f131i = expansionHubMotorControllerPositionParams.mo10130I();
        this.f129d = expansionHubMotorControllerPositionParams.mo10129D();
        this.f130f = LynxServoController.apiPositionFirst;
        this.algorithm = MotorControlAlgorithm.LegacyPID;
    }

    public ExpansionHubMotorControllerParamsState(ExpansionHubPIDFPositionParams expansionHubPIDFPositionParams) {
        this.mode = null;
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.mode = DcMotor.RunMode.RUN_TO_POSITION;
        this.f132p = expansionHubPIDFPositionParams.mo10196P();
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.algorithm = expansionHubPIDFPositionParams.algorithm();
    }

    public ExpansionHubMotorControllerParamsState(ExpansionHubMotorControllerVelocityParams expansionHubMotorControllerVelocityParams) {
        this.mode = null;
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.mode = DcMotor.RunMode.RUN_USING_ENCODER;
        this.f132p = expansionHubMotorControllerVelocityParams.mo10134P();
        this.f131i = expansionHubMotorControllerVelocityParams.mo10133I();
        this.f129d = expansionHubMotorControllerVelocityParams.mo10132D();
        this.f130f = LynxServoController.apiPositionFirst;
        this.algorithm = MotorControlAlgorithm.LegacyPID;
    }

    public ExpansionHubMotorControllerParamsState(ExpansionHubPIDFVelocityParams expansionHubPIDFVelocityParams) {
        this.mode = null;
        this.f132p = LynxServoController.apiPositionFirst;
        this.f131i = LynxServoController.apiPositionFirst;
        this.f129d = LynxServoController.apiPositionFirst;
        this.f130f = LynxServoController.apiPositionFirst;
        this.mode = DcMotor.RunMode.RUN_USING_ENCODER;
        this.f132p = expansionHubPIDFVelocityParams.mo10201P();
        this.f131i = expansionHubPIDFVelocityParams.mo10200I();
        this.f129d = expansionHubPIDFVelocityParams.mo10198D();
        this.f130f = expansionHubPIDFVelocityParams.mo10199F();
        this.algorithm = expansionHubPIDFVelocityParams.algorithm();
    }

    public PIDFCoefficients getPidfCoefficients() {
        return new PIDFCoefficients(this.f132p, this.f131i, this.f129d, this.f130f, this.algorithm);
    }

    public ExpansionHubMotorControllerParamsState clone() {
        try {
            return (ExpansionHubMotorControllerParamsState) super.clone();
        } catch (CloneNotSupportedException unused) {
            throw new RuntimeException("internal error: Parameters not cloneable");
        }
    }

    public boolean isDefault() {
        return this.mode == null;
    }

    public boolean equals(Object obj) {
        if (!(obj instanceof ExpansionHubMotorControllerParamsState)) {
            return false;
        }
        ExpansionHubMotorControllerParamsState expansionHubMotorControllerParamsState = (ExpansionHubMotorControllerParamsState) obj;
        if (this.mode == expansionHubMotorControllerParamsState.mode && this.f132p == expansionHubMotorControllerParamsState.f132p && this.f131i == expansionHubMotorControllerParamsState.f131i && this.f129d == expansionHubMotorControllerParamsState.f129d && this.f130f == expansionHubMotorControllerParamsState.f130f && this.algorithm == expansionHubMotorControllerParamsState.algorithm) {
            return true;
        }
        return false;
    }

    public int hashCode() {
        return ((((this.mode.hashCode() ^ (hash(this.f132p) << 3)) ^ (hash(this.f131i) << 6)) ^ (hash(this.f129d) << 9)) ^ (hash(this.f130f) << 12)) ^ -860998516;
    }

    /* access modifiers changed from: protected */
    public int hash(double d) {
        return Double.valueOf(d).hashCode();
    }

    public String toString() {
        return Misc.formatForUser("mode=%s,p=%f,i=%f,d=%f,f=%f", this.mode, Double.valueOf(this.f132p), Double.valueOf(this.f131i), Double.valueOf(this.f129d), Double.valueOf(this.f130f));
    }
}
