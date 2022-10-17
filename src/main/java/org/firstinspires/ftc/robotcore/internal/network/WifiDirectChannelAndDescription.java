package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.C0705R;
import java.util.ArrayList;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;

public class WifiDirectChannelAndDescription implements Comparable<WifiDirectChannelAndDescription> {
    protected int channel;
    protected String description;

    public static List<WifiDirectChannelAndDescription> load() {
        String[] stringArray = AppUtil.getDefContext().getResources().getStringArray(C0705R.array.wifi_direct_channels);
        ArrayList arrayList = new ArrayList();
        for (String wifiDirectChannelAndDescription : stringArray) {
            arrayList.add(new WifiDirectChannelAndDescription(wifiDirectChannelAndDescription));
        }
        return arrayList;
    }

    public static String getDescription(int i) {
        for (WifiDirectChannelAndDescription next : load()) {
            if (next.getChannel() == i) {
                return next.getDescription();
            }
        }
        return AppUtil.getDefContext().getString(C0705R.string.unknown_wifi_direct_channel);
    }

    public WifiDirectChannelAndDescription(String str) {
        String[] split = str.split("\\|");
        this.description = split[0];
        this.channel = Integer.parseInt(split[1]);
    }

    public int getChannel() {
        return this.channel;
    }

    public String getDescription() {
        return this.description;
    }

    public String toString() {
        return getDescription();
    }

    public int compareTo(WifiDirectChannelAndDescription wifiDirectChannelAndDescription) {
        return this.channel - wifiDirectChannelAndDescription.channel;
    }
}
