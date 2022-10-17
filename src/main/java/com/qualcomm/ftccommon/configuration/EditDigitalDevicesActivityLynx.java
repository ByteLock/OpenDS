package com.qualcomm.ftccommon.configuration;

import com.qualcomm.ftccommon.C0470R;

public class EditDigitalDevicesActivityLynx extends EditDigitalDevicesActivity {
    public String getTag() {
        return getClass().getSimpleName();
    }

    public EditDigitalDevicesActivityLynx() {
        this.layoutItem = C0470R.layout.digital_device_lynx;
    }
}
