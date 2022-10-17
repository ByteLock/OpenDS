package com.qualcomm.ftccommon.configuration;

import android.os.Bundle;

public class FtcNewFileActivity extends FtcConfigurationActivity {
    public static final RequestCode requestCode = RequestCode.NEW_FILE;

    /* access modifiers changed from: protected */
    public void ensureConfigFileIsFresh() {
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        dirtyCheckThenSingletonUSBScanAndUpdateUI(false);
    }
}
