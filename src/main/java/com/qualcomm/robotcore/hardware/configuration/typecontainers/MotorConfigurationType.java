package com.qualcomm.robotcore.hardware.configuration.typecontainers;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationTypeManager;
import com.qualcomm.robotcore.hardware.configuration.DistributorInfo;
import com.qualcomm.robotcore.hardware.configuration.DistributorInfoState;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerParamsState;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerPositionParams;
import com.qualcomm.robotcore.hardware.configuration.ExpansionHubMotorControllerVelocityParams;
import com.qualcomm.robotcore.hardware.configuration.MotorType;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFPositionParams;
import com.qualcomm.robotcore.hardware.configuration.annotations.ExpansionHubPIDFVelocityParams;
import com.qualcomm.robotcore.hardware.configuration.typecontainers.UserConfigurationType;
import com.qualcomm.robotcore.util.ClassUtil;
import org.firstinspires.ftc.robotcore.external.navigation.Rotation;

public final class MotorConfigurationType extends UserConfigurationType implements Cloneable {
    @Expose
    private double achieveableMaxRPMFraction;
    @Expose
    private DistributorInfoState distributorInfo = new DistributorInfoState();
    @Expose
    private double gearing;
    @Expose
    private ExpansionHubMotorControllerParamsState hubPositionParams = new ExpansionHubMotorControllerParamsState();
    @Expose
    private ExpansionHubMotorControllerParamsState hubVelocityParams = new ExpansionHubMotorControllerParamsState();
    @Expose
    private double maxRPM;
    @Expose
    private Rotation orientation;
    @Expose
    private double ticksPerRev;

    public double getTicksPerRev() {
        return this.ticksPerRev;
    }

    public double getAchieveableMaxTicksPerSecond() {
        return (getTicksPerRev() * (getMaxRPM() * getAchieveableMaxRPMFraction())) / 60.0d;
    }

    public int getAchieveableMaxTicksPerSecondRounded() {
        return (int) Math.round(getAchieveableMaxTicksPerSecond());
    }

    public void setTicksPerRev(double d) {
        this.ticksPerRev = d;
    }

    public double getGearing() {
        return this.gearing;
    }

    public void setGearing(double d) {
        this.gearing = d;
    }

    public double getMaxRPM() {
        return this.maxRPM;
    }

    public void setMaxRPM(double d) {
        this.maxRPM = d;
    }

    public double getAchieveableMaxRPMFraction() {
        return this.achieveableMaxRPMFraction;
    }

    public void setAchieveableMaxRPMFraction(double d) {
        this.achieveableMaxRPMFraction = d;
    }

    public Rotation getOrientation() {
        return this.orientation;
    }

    public void setOrientation(Rotation rotation) {
        this.orientation = rotation;
    }

    public boolean hasExpansionHubVelocityParams() {
        return !this.hubVelocityParams.isDefault();
    }

    public ExpansionHubMotorControllerParamsState getHubVelocityParams() {
        return this.hubVelocityParams;
    }

    public boolean hasExpansionHubPositionParams() {
        return !this.hubPositionParams.isDefault();
    }

    public ExpansionHubMotorControllerParamsState getHubPositionParams() {
        return this.hubPositionParams;
    }

    public DistributorInfoState getDistributorInfo() {
        return this.distributorInfo;
    }

    public static MotorConfigurationType getUnspecifiedMotorType() {
        return ConfigurationTypeManager.getInstance().getUnspecifiedMotorType();
    }

    public static MotorConfigurationType getMotorType(Class<?> cls) {
        return (MotorConfigurationType) ConfigurationTypeManager.getInstance().userTypeFromClass(ConfigurationType.DeviceFlavor.MOTOR, cls);
    }

    public MotorConfigurationType(Class cls, String str) {
        super(cls, ConfigurationType.DeviceFlavor.MOTOR, str);
    }

    public MotorConfigurationType() {
        super(ConfigurationType.DeviceFlavor.MOTOR);
    }

    public MotorConfigurationType clone() {
        try {
            MotorConfigurationType motorConfigurationType = (MotorConfigurationType) super.clone();
            motorConfigurationType.distributorInfo = this.distributorInfo.clone();
            motorConfigurationType.hubVelocityParams = this.hubVelocityParams.clone();
            motorConfigurationType.hubPositionParams = this.hubPositionParams.clone();
            return motorConfigurationType;
        } catch (CloneNotSupportedException unused) {
            throw new RuntimeException("internal error: Parameters not cloneable");
        }
    }

    public boolean processAnnotation(Object obj) {
        if (obj == null) {
            return false;
        }
        if (obj instanceof ExpansionHubPIDFVelocityParams) {
            return processAnnotation((ExpansionHubPIDFVelocityParams) obj);
        }
        if (obj instanceof ExpansionHubMotorControllerVelocityParams) {
            return processAnnotation((ExpansionHubMotorControllerVelocityParams) obj);
        }
        if (obj instanceof ExpansionHubPIDFPositionParams) {
            return processAnnotation((ExpansionHubPIDFPositionParams) obj);
        }
        if (obj instanceof ExpansionHubMotorControllerPositionParams) {
            return processAnnotation((ExpansionHubMotorControllerPositionParams) obj);
        }
        if (obj instanceof DistributorInfo) {
            return processAnnotation((DistributorInfo) obj);
        }
        return false;
    }

    public boolean processAnnotation(MotorType motorType) {
        if (motorType == null) {
            return false;
        }
        if (this.name.isEmpty()) {
            this.name = ClassUtil.decodeStringRes(motorType.name().trim());
        }
        this.ticksPerRev = motorType.ticksPerRev();
        this.gearing = motorType.gearing();
        this.maxRPM = motorType.maxRPM();
        this.achieveableMaxRPMFraction = motorType.achieveableMaxRPMFraction();
        this.orientation = motorType.orientation();
        return true;
    }

    public boolean processAnnotation(com.qualcomm.robotcore.hardware.configuration.annotations.MotorType motorType) {
        if (motorType == null) {
            return false;
        }
        this.ticksPerRev = motorType.ticksPerRev();
        this.gearing = motorType.gearing();
        this.maxRPM = motorType.maxRPM();
        this.achieveableMaxRPMFraction = motorType.achieveableMaxRPMFraction();
        this.orientation = motorType.orientation();
        return true;
    }

    public boolean processAnnotation(ExpansionHubPIDFVelocityParams expansionHubPIDFVelocityParams) {
        if (expansionHubPIDFVelocityParams == null) {
            return false;
        }
        this.hubVelocityParams = new ExpansionHubMotorControllerParamsState(expansionHubPIDFVelocityParams);
        return true;
    }

    public boolean processAnnotation(ExpansionHubMotorControllerVelocityParams expansionHubMotorControllerVelocityParams) {
        if (expansionHubMotorControllerVelocityParams == null) {
            return false;
        }
        this.hubVelocityParams = new ExpansionHubMotorControllerParamsState(expansionHubMotorControllerVelocityParams);
        return true;
    }

    public boolean processAnnotation(ExpansionHubPIDFPositionParams expansionHubPIDFPositionParams) {
        if (expansionHubPIDFPositionParams == null) {
            return false;
        }
        this.hubPositionParams = new ExpansionHubMotorControllerParamsState(expansionHubPIDFPositionParams);
        return true;
    }

    public boolean processAnnotation(ExpansionHubMotorControllerPositionParams expansionHubMotorControllerPositionParams) {
        if (expansionHubMotorControllerPositionParams == null) {
            return false;
        }
        this.hubPositionParams = new ExpansionHubMotorControllerParamsState(expansionHubMotorControllerPositionParams);
        return true;
    }

    public boolean processAnnotation(DistributorInfo distributorInfo2) {
        if (distributorInfo2 == null) {
            return false;
        }
        if (this.name.isEmpty()) {
            String decodeStringRes = ClassUtil.decodeStringRes(distributorInfo2.distributor().trim());
            String decodeStringRes2 = ClassUtil.decodeStringRes(distributorInfo2.model().trim());
            if (!decodeStringRes.isEmpty() && !decodeStringRes2.isEmpty()) {
                this.name = decodeStringRes + " " + decodeStringRes2;
            }
        }
        this.distributorInfo = DistributorInfoState.from(distributorInfo2);
        return true;
    }

    public void finishedAnnotations(Class cls) {
        if (this.name.isEmpty()) {
            this.name = cls.getSimpleName();
        }
    }

    private Object writeReplace() {
        return new UserConfigurationType.SerializationProxy(this);
    }
}
