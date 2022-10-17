package org.firstinspires.ftc.robotcore.internal.network;

import android.net.wifi.p2p.WifiP2pGroup;
import com.google.gson.reflect.TypeToken;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import org.firstinspires.ftc.robotcore.internal.collections.SimpleGson;

public class WifiDirectGroupName implements Comparable<WifiDirectGroupName> {
    protected String name;

    public WifiDirectGroupName(WifiP2pGroup wifiP2pGroup) {
        this.name = wifiP2pGroup.getNetworkName();
    }

    public WifiDirectGroupName(String str) {
        this.name = str;
    }

    public static List<WifiDirectGroupName> namesFromGroups(Collection<WifiP2pGroup> collection) {
        ArrayList arrayList = new ArrayList();
        for (WifiP2pGroup wifiDirectGroupName : collection) {
            arrayList.add(new WifiDirectGroupName(wifiDirectGroupName));
        }
        return arrayList;
    }

    public String toString() {
        return this.name;
    }

    public int compareTo(WifiDirectGroupName wifiDirectGroupName) {
        return toString().compareTo(wifiDirectGroupName.toString());
    }

    public static String serializeNames(Collection<WifiP2pGroup> collection) {
        return serializeNames(namesFromGroups(collection));
    }

    public static String serializeNames(List<WifiDirectGroupName> list) {
        return SimpleGson.getInstance().toJson((Object) list);
    }

    public static List<WifiDirectGroupName> deserializeNames(String str) {
        return (List) SimpleGson.getInstance().fromJson(str, new TypeToken<ArrayList<WifiDirectGroupName>>() {
        }.getType());
    }
}
