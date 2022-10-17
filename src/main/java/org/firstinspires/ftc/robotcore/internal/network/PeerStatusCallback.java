package org.firstinspires.ftc.robotcore.internal.network;

public interface PeerStatusCallback {
    void onPeerConnected();

    void onPeerDisconnected();
}
