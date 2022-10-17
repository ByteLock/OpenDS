package com.qualcomm.ftccommon;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckedTextView;
import android.widget.FrameLayout;
import android.widget.ListView;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.Arrays;
import org.firstinspires.ftc.robotcore.network.WifiDirectChannelAndDescription;
import org.firstinspires.ftc.robotcore.network.WifiDirectChannelChanger;
import org.firstinspires.ftc.robotcore.network.WifiUtil;
import org.firstinspires.ftc.robotcore.ui.ThemedActivity;
import org.firstinspires.ftc.robotcore.ui.UILocation;
import org.firstinspires.ftc.robotcore.system.AppUtil;
import org.firstinspires.ftc.robotcore.system.PreferencesHelper;
import org.firstinspires.inspection.C1275R;

public class FtcWifiDirectChannelSelectorActivity extends ThemedActivity implements AdapterView.OnItemClickListener {
    public static final String TAG = "FtcWifiDirectChannelSelectorActivity";
    private final int INDEX_AUTO_AND_2_4_ITEMS = 12;
    private WifiDirectChannelChanger configurer = null;
    private PreferencesHelper preferencesHelper = new PreferencesHelper(TAG);
    private boolean remoteConfigure = AppUtil.getInstance().isDriverStation();

    public String getTag() {
        return TAG;
    }

    /* access modifiers changed from: protected */
    public FrameLayout getBackBar() {
        return (FrameLayout) findViewById(C1275R.C1277id.backbar);
    }

    /* access modifiers changed from: protected */
    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(C0470R.layout.activity_ftc_wifi_channel_selector);
        ListView listView = (ListView) findViewById(C0470R.C0472id.channelPickList);
        loadAdapter(listView);
        listView.setOnItemClickListener(this);
        listView.setChoiceMode(1);
        if (!this.remoteConfigure) {
            this.configurer = new WifiDirectChannelChanger();
        }
    }

    /* access modifiers changed from: protected */
    public void onStart() {
        super.onStart();
        int readInt = this.preferencesHelper.readInt(getString(C0470R.string.pref_wifip2p_channel), -1);
        if (readInt == -1) {
            RobotLog.m61vv(TAG, "pref_wifip2p_channel: No preferred channel defined. Will use a default value of %d", 0);
            readInt = 0;
        } else {
            RobotLog.m61vv(TAG, "pref_wifip2p_channel: Found existing preferred channel (%d).", Integer.valueOf(readInt));
        }
        ListView listView = (ListView) findViewById(C0470R.C0472id.channelPickList);
        ArrayAdapter<WifiDirectChannelAndDescription> adapter = getAdapter(listView);
        int i = 0;
        while (i < adapter.getCount()) {
            WifiDirectChannelAndDescription item = adapter.getItem(i);
            if (readInt == item.getChannel()) {
                listView.setItemChecked(i, true);
                RobotLog.m61vv(TAG, "preferred channel matches ListView index %d (%d).", Integer.valueOf(i), Integer.valueOf(item.getChannel()));
                i = adapter.getCount();
            }
            i++;
        }
    }

    /* access modifiers changed from: protected */
    public void onDestroy() {
        super.onDestroy();
    }

    /* access modifiers changed from: protected */
    public ArrayAdapter<WifiDirectChannelAndDescription> getAdapter(AdapterView<?> adapterView) {
        return (ArrayAdapter) adapterView.getAdapter();
    }

    /* access modifiers changed from: protected */
    public void loadAdapter(ListView listView) {
        WifiDirectChannelAndDescription[] wifiDirectChannelAndDescriptionArr = (WifiDirectChannelAndDescription[]) WifiDirectChannelAndDescription.load().toArray(new WifiDirectChannelAndDescription[0]);
        Arrays.sort(wifiDirectChannelAndDescriptionArr);
        if (!WifiUtil.is5GHzAvailable()) {
            wifiDirectChannelAndDescriptionArr = (WifiDirectChannelAndDescription[]) Arrays.copyOf(wifiDirectChannelAndDescriptionArr, 12);
            RobotLog.m60vv(TAG, "5GHz radio not available.");
        } else {
            RobotLog.m60vv(TAG, "5GHz radio is available.");
        }
        listView.setAdapter(new WifiChannelItemAdapter(this, 17367049, wifiDirectChannelAndDescriptionArr));
    }

    protected class WifiChannelItemAdapter extends ArrayAdapter<WifiDirectChannelAndDescription> {
        int checkmark;

        public WifiChannelItemAdapter(Context context, int i, WifiDirectChannelAndDescription[] wifiDirectChannelAndDescriptionArr) {
            super(context, i, wifiDirectChannelAndDescriptionArr);
            TypedValue typedValue = new TypedValue();
            FtcWifiDirectChannelSelectorActivity.this.getTheme().resolveAttribute(16843289, typedValue, true);
            this.checkmark = typedValue.resourceId;
        }

        public View getView(int i, View view, ViewGroup viewGroup) {
            View view2 = super.getView(i, view, viewGroup);
            ((CheckedTextView) view2).setCheckMarkDrawable(this.checkmark);
            return view2;
        }
    }

    public void onItemClick(AdapterView<?> adapterView, View view, int i, long j) {
        WifiDirectChannelChanger wifiDirectChannelChanger = this.configurer;
        if (wifiDirectChannelChanger == null || !wifiDirectChannelChanger.isBusy()) {
            WifiDirectChannelAndDescription item = getAdapter(adapterView).getItem(i);
            ((CheckedTextView) view).setChecked(true);
            if (!this.remoteConfigure) {
                this.configurer.changeToChannel(item.getChannel());
            } else if (this.preferencesHelper.writePrefIfDifferent(getString(C0470R.string.pref_wifip2p_channel), Integer.valueOf(item.getChannel()))) {
                AppUtil.getInstance().showToast(UILocation.ONLY_LOCAL, getString(C0470R.string.toastWifiP2pChannelChangeRequestedDS, new Object[]{item.getDescription()}));
            }
        }
    }

    public void onWifiSettingsClicked(View view) {
        RobotLog.m60vv(TAG, "launch Wi-Fi settings");
        startActivity(new Intent("android.net.wifi.PICK_WIFI_NETWORK"));
    }
}
