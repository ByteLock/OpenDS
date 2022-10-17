package com.qualcomm.robotcore.hardware;

import android.text.TextUtils;
import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.SerialNumber;
import org.firstinspires.ftc.robotcore.internal.hardware.CachedLynxModulesInfo;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.inspection.InspectionState;

public final class USBAccessibleLynxModule {
    private static final String FW_UNAVAILABLE = ("(" + AppUtil.getDefContext().getString(C0705R.string.lynxUnavailableFWVersionString) + ")");
    protected String firmwareVersionString = InspectionState.NO_VERSION;
    protected String formattedFirmwareVersionString = FW_UNAVAILABLE;
    protected int moduleAddress = 0;
    protected boolean moduleAddressChangeable = true;
    protected SerialNumber serialNumber = null;

    public USBAccessibleLynxModule(SerialNumber serialNumber2) {
        setSerialNumber(serialNumber2);
    }

    public USBAccessibleLynxModule(SerialNumber serialNumber2, boolean z) {
        setSerialNumber(serialNumber2);
        setModuleAddressChangeable(z);
    }

    public SerialNumber getSerialNumber() {
        return this.serialNumber;
    }

    public void setSerialNumber(SerialNumber serialNumber2) {
        this.serialNumber = serialNumber2;
    }

    public int getModuleAddress() {
        return this.moduleAddress;
    }

    public void setModuleAddress(int i) {
        this.moduleAddress = i;
    }

    public boolean isModuleAddressChangeable() {
        return this.moduleAddressChangeable;
    }

    public void setModuleAddressChangeable(boolean z) {
        this.moduleAddressChangeable = z;
    }

    public String getFirmwareVersionString() {
        return this.firmwareVersionString;
    }

    public String getFinishedFirmwareVersionString() {
        String firmwareVersionString2 = getFirmwareVersionString();
        if (TextUtils.isEmpty(firmwareVersionString2)) {
            return FW_UNAVAILABLE;
        }
        return CachedLynxModulesInfo.formatFirmwareVersion(firmwareVersionString2);
    }

    public void setFirmwareVersionString(String str) {
        this.firmwareVersionString = str;
        this.formattedFirmwareVersionString = getFinishedFirmwareVersionString();
    }
}
