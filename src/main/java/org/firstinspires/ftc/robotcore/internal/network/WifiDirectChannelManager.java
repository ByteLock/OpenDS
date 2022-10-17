package org.firstinspires.ftc.robotcore.internal.network;

import com.qualcomm.robotcore.C0705R;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.EnumSet;
import java.util.Set;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.PreferencesHelper;

public class WifiDirectChannelManager implements ApChannelManager {
    private static final String TAG = "WifiDirectChannelManager";
    private EnumSet<ApChannel> supportedChannels = null;

    public Set<ApChannel> getSupportedChannels() {
        if (this.supportedChannels == null) {
            EnumSet<ApChannel> copyOf = EnumSet.copyOf(ApChannel.ALL_2_4_GHZ_CHANNELS);
            copyOf.add(ApChannel.AUTO_2_4_GHZ);
            if (WifiUtil.is5GHzAvailable()) {
                copyOf.add(ApChannel.AUTO_5_GHZ);
                copyOf.addAll(ApChannel.NON_DFS_5_GHZ_CHANNELS);
            }
            this.supportedChannels = copyOf;
        }
        return this.supportedChannels;
    }

    public ApChannel getCurrentChannel() {
        int readInt = new PreferencesHelper(TAG).readInt(AppUtil.getDefContext().getString(C0705R.string.pref_wifip2p_channel), -1);
        if (readInt == 0) {
            return ApChannel.AUTO_2_4_GHZ;
        }
        return ApChannel.fromBandAndChannel(readInt > 14 ? 1 : 0, readInt);
    }

    public void setChannel(ApChannel apChannel, boolean z) throws InvalidNetworkSettingException {
        if (!getSupportedChannels().contains(apChannel)) {
            throw new InvalidNetworkSettingException("This device does not support channel " + apChannel);
        } else if (z) {
            new WifiDirectChannelChanger().changeToChannel(apChannel.channelNum);
        }
    }

    public ApChannel resetChannel(boolean z) {
        ApChannel apChannel = ApChannel.AUTO_2_4_GHZ;
        try {
            setChannel(apChannel, z);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Unable to reset channel to default");
        }
        return apChannel;
    }
}
