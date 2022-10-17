package com.qualcomm.robotcore.hardware.configuration;

import com.google.gson.annotations.Expose;
import com.qualcomm.robotcore.util.ClassUtil;
import java.io.Serializable;
import org.firstinspires.inspection.InspectionState;

public class DistributorInfoState implements Serializable, Cloneable {
    @Expose
    private String distributor = InspectionState.NO_VERSION;
    @Expose
    private String model = InspectionState.NO_VERSION;
    @Expose
    private String url = InspectionState.NO_VERSION;

    public static DistributorInfoState from(DistributorInfo distributorInfo) {
        DistributorInfoState distributorInfoState = new DistributorInfoState();
        distributorInfoState.setDistributor(ClassUtil.decodeStringRes(distributorInfo.distributor()));
        distributorInfoState.setModel(ClassUtil.decodeStringRes(distributorInfo.model()));
        distributorInfoState.setUrl(ClassUtil.decodeStringRes(distributorInfo.url()));
        return distributorInfoState;
    }

    public DistributorInfoState clone() {
        try {
            return (DistributorInfoState) super.clone();
        } catch (CloneNotSupportedException unused) {
            throw new RuntimeException("internal error: Parameters not cloneable");
        }
    }

    public String getDistributor() {
        return this.distributor;
    }

    public void setDistributor(String str) {
        this.distributor = str.trim();
    }

    public String getModel() {
        return this.model;
    }

    public void setModel(String str) {
        this.model = str.trim();
    }

    public String getUrl() {
        return this.url;
    }

    public void setUrl(String str) {
        this.url = str.trim();
    }
}
