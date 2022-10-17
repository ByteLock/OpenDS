package com.qualcomm.ftccommon.configuration;

import android.content.Intent;
import android.view.View;
import com.qualcomm.ftccommon.C0470R;
import com.qualcomm.robotcore.hardware.DeviceManager;
import com.qualcomm.robotcore.hardware.ScannedDevices;
import com.qualcomm.robotcore.hardware.configuration.ConfigurationType;
import com.qualcomm.robotcore.hardware.configuration.ControllerConfiguration;
import com.qualcomm.robotcore.util.SerialNumber;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.firstinspires.ftc.robotcore.internal.p013ui.UILocation;

public class EditUSBDeviceActivity extends EditActivity {
    protected ScannedDevices extraUSBDevices = new ScannedDevices();

    /* access modifiers changed from: protected */
    public void refreshSerialNumber() {
    }

    public String getTag() {
        return getClass().getSimpleName();
    }

    /* access modifiers changed from: protected */
    public void deserialize(EditParameters editParameters) {
        super.deserialize(editParameters);
        determineExtraUSBDevices();
    }

    /* access modifiers changed from: protected */
    public void swapConfiguration() {
        if (getRobotConfigMap().isSwappable(this.controllerConfiguration, this.scannedDevices, this)) {
            EditParameters editParameters = new EditParameters(this, this.controllerConfiguration);
            editParameters.setRobotConfigMap(getRobotConfigMap());
            editParameters.setScannedDevices(this.scannedDevices);
            handleLaunchEdit(EditSwapUsbDevices.requestCode, EditSwapUsbDevices.class, editParameters);
        }
    }

    /* access modifiers changed from: protected */
    public boolean completeSwapConfiguration(int i, int i2, Intent intent) {
        if (i2 != -1 || RequestCode.fromValue(i) != EditSwapUsbDevices.requestCode) {
            return false;
        }
        SerialNumber serialNumber = ((ControllerConfiguration) EditParameters.fromIntent(this, intent).getConfiguration()).getSerialNumber();
        ControllerConfiguration controllerConfiguration = getRobotConfigMap().get(serialNumber);
        if (controllerConfiguration != null) {
            this.robotConfigMap.swapSerialNumbers(this.controllerConfiguration, controllerConfiguration);
        } else {
            this.robotConfigMap.setSerialNumber(this.controllerConfiguration, serialNumber);
            this.controllerConfiguration.setKnownToBeAttached(true);
        }
        determineExtraUSBDevices();
        refreshAfterSwap();
        return true;
    }

    /* access modifiers changed from: protected */
    public void fixConfiguration() {
        SerialNumber fixableCandidate = getFixableCandidate();
        if (fixableCandidate != null) {
            this.robotConfigMap.setSerialNumber(this.controllerConfiguration, fixableCandidate);
            this.controllerConfiguration.setKnownToBeAttached(true);
            determineExtraUSBDevices();
        } else {
            this.appUtil.showToast(UILocation.ONLY_LOCAL, String.format(getString(C0470R.string.fixFailNoneAvailable), new Object[]{this.controllerConfiguration.getName(), displayNameOfConfigurationType(ConfigurationType.DisplayNameFlavor.Normal, this.controllerConfiguration.getConfigurationType())}));
        }
        refreshAfterFix();
    }

    /* access modifiers changed from: protected */
    public SerialNumber getFixableCandidate() {
        if (this.controllerConfiguration.isKnownToBeAttached()) {
            return null;
        }
        DeviceManager.UsbDeviceType uSBDeviceType = this.controllerConfiguration.toUSBDeviceType();
        Iterator<Map.Entry<SerialNumber, DeviceManager.UsbDeviceType>> it = this.extraUSBDevices.entrySet().iterator();
        boolean z = false;
        SerialNumber serialNumber = null;
        boolean z2 = false;
        while (true) {
            if (!it.hasNext()) {
                z = z2;
                break;
            }
            Map.Entry next = it.next();
            if (next.getValue() == uSBDeviceType) {
                if (serialNumber != null) {
                    break;
                }
                serialNumber = (SerialNumber) next.getKey();
                z2 = true;
            }
        }
        if (z) {
            return serialNumber;
        }
        return null;
    }

    /* access modifiers changed from: protected */
    public boolean isFixable() {
        return getFixableCandidate() != null;
    }

    /* access modifiers changed from: protected */
    public boolean isSwappable() {
        List<ControllerConfiguration> eligibleSwapTargets = getRobotConfigMap().getEligibleSwapTargets(this.controllerConfiguration, this.scannedDevices, this);
        SerialNumber fixableCandidate = getFixableCandidate();
        if (eligibleSwapTargets.isEmpty() || (fixableCandidate != null && eligibleSwapTargets.size() == 1 && eligibleSwapTargets.get(0).getSerialNumber().equals((Object) fixableCandidate))) {
            return false;
        }
        return true;
    }

    /* access modifiers changed from: protected */
    public void refreshAfterFix() {
        showFixSwapButtons();
        this.currentCfgFile.markDirty();
        this.robotConfigFileManager.updateActiveConfigHeader(this.currentCfgFile);
    }

    /* access modifiers changed from: protected */
    public void refreshAfterSwap() {
        showFixSwapButtons();
        this.currentCfgFile.markDirty();
        this.robotConfigFileManager.updateActiveConfigHeader(this.currentCfgFile);
    }

    /* access modifiers changed from: protected */
    public void showFixSwapButtons() {
        showFixButton(isFixable());
        showSwapButton(isSwappable());
        refreshSerialNumber();
    }

    /* access modifiers changed from: protected */
    public void showFixButton(boolean z) {
        showButton(this.idFixButton, z);
    }

    /* access modifiers changed from: protected */
    public void showSwapButton(boolean z) {
        showButton(this.idSwapButton, z);
    }

    /* access modifiers changed from: protected */
    public void showButton(int i, boolean z) {
        View findViewById = findViewById(i);
        if (findViewById != null) {
            findViewById.setVisibility(z ? 0 : 8);
        }
    }

    /* access modifiers changed from: protected */
    public void determineExtraUSBDevices() {
        this.extraUSBDevices = new ScannedDevices(this.scannedDevices);
        for (SerialNumber remove : getRobotConfigMap().serialNumbers()) {
            this.extraUSBDevices.remove(remove);
        }
        for (ControllerConfiguration next : getRobotConfigMap().controllerConfigurations()) {
            next.setKnownToBeAttached(this.scannedDevices.containsKey(next.getSerialNumber()));
        }
    }
}
