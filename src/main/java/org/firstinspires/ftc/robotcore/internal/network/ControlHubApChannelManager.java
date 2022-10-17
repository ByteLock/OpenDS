package org.firstinspires.ftc.robotcore.internal.network;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import com.qualcomm.robotcore.util.Intents;
import com.qualcomm.robotcore.util.RobotLog;
import java.util.EnumSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import org.firstinspires.ftc.robotcore.internal.hardware.android.AndroidBoard;
import org.firstinspires.ftc.robotcore.internal.system.AppUtil;
import org.firstinspires.ftc.robotcore.internal.system.SynchronousResultReceiver;

public final class ControlHubApChannelManager implements ApChannelManager {
    private static final boolean DEBUG = false;
    private static final ApChannel FACTORY_DEFAULT_AP_CHANNEL = ApChannel.AUTO_2_4_GHZ;
    private static final String TAG = "ControlHubApChannelManager";
    private final ChannelResultReceiver channelResultReceiver = new ChannelResultReceiver();
    private final Context context = AppUtil.getDefContext();
    private EnumSet<ApChannel> supportedChannels = null;

    public Set<ApChannel> getSupportedChannels() {
        if (this.supportedChannels == null) {
            EnumSet<ApChannel> copyOf = EnumSet.copyOf(ApChannel.ALL_2_4_GHZ_CHANNELS);
            copyOf.add(ApChannel.AUTO_2_4_GHZ);
            if (AndroidBoard.getInstance().supports5GhzAp()) {
                copyOf.addAll(ApChannel.NON_DFS_5_GHZ_CHANNELS);
                if (AndroidBoard.getInstance().supports5GhzAutoSelection()) {
                    copyOf.add(ApChannel.AUTO_5_GHZ);
                }
            }
            this.supportedChannels = copyOf;
        }
        return this.supportedChannels;
    }

    public synchronized ApChannel getCurrentChannel() {
        if (AndroidBoard.getInstance().supportsGetChannelInfoIntent()) {
            Intent intent = new Intent(Intents.ACTION_FTC_AP_GET_CURRENT_CHANNEL_INFO);
            intent.putExtra(Intents.EXTRA_RESULT_RECEIVER, AppUtil.wrapResultReceiverForIpc(this.channelResultReceiver));
            AppUtil.getDefContext().sendBroadcast(intent);
            try {
                return (ApChannel) this.channelResultReceiver.awaitResult(45, TimeUnit.MILLISECONDS);
            } catch (InterruptedException unused) {
                RobotLog.m48ee(TAG, "Thread interrupted while getting current channel from AP service");
                Thread.currentThread().interrupt();
            } catch (TimeoutException unused2) {
                RobotLog.m48ee(TAG, "Timeout while getting current channel from AP service");
            }
        }
        return ApChannel.UNKNOWN;
    }

    public void setChannel(ApChannel apChannel, boolean z) throws InvalidNetworkSettingException {
        if (!getSupportedChannels().contains(apChannel)) {
            throw new InvalidNetworkSettingException("This device does not support channel " + apChannel);
        } else if (!z) {
        } else {
            if (AndroidBoard.getInstance().supportsBulkNetworkSettings()) {
                setChannelViaBulkSettingsApi(apChannel);
            } else {
                setChannelViaLegacyApi(apChannel.channelNum);
            }
        }
    }

    private void setChannelViaBulkSettingsApi(ApChannel apChannel) throws InvalidNetworkSettingException {
        RobotLog.m60vv(TAG, "Setting channel via bulk Wi-Fi settings API: " + apChannel.getDisplayName());
        NetworkConnectionHandler.getInstance().getNetworkConnection().setNetworkSettings((String) null, (String) null, apChannel);
    }

    private void setChannelViaLegacyApi(int i) {
        RobotLog.m60vv(TAG, "Sending ap channel change intent");
        Intent intent = new Intent(Intents.ACTION_FTC_AP_CHANNEL_CHANGE);
        intent.putExtra(Intents.EXTRA_AP_PREF, i);
        this.context.sendBroadcast(intent);
    }

    public ApChannel resetChannel(boolean z) {
        ApChannel apChannel = FACTORY_DEFAULT_AP_CHANNEL;
        try {
            setChannel(apChannel, z);
        } catch (InvalidNetworkSettingException e) {
            RobotLog.m50ee(TAG, (Throwable) e, "Unable to reset channel to " + apChannel);
        }
        return apChannel;
    }

    private static class ChannelResultReceiver extends SynchronousResultReceiver<ApChannel> {
        public ChannelResultReceiver() {
            super(3, ControlHubApChannelManager.TAG, CallbackLooper.getDefault().getHandler());
        }

        /* access modifiers changed from: protected */
        public ApChannel provideResult(int i, Bundle bundle) {
            return ApChannel.fromBandAndChannel(bundle.getInt(Intents.BUNDLE_KEY_CURRENT_BAND), bundle.getInt(Intents.BUNDLE_KEY_CURRENT_CHANNEL));
        }
    }
}
