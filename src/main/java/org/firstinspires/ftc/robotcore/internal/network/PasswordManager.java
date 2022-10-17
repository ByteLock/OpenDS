package org.firstinspires.ftc.robotcore.internal.network;

public interface PasswordManager {
    String getPassword();

    boolean isDefault();

    String resetPassword(boolean z);

    void setPassword(String str, boolean z) throws InvalidNetworkSettingException;
}
