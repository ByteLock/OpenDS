package org.firstinspires.ftc.robotcore.internal.network;

import java.util.Set;

public interface ApChannelManager {
    ApChannel getCurrentChannel();

    Set<ApChannel> getSupportedChannels();

    ApChannel resetChannel(boolean z);

    void setChannel(ApChannel apChannel, boolean z) throws InvalidNetworkSettingException;
}
