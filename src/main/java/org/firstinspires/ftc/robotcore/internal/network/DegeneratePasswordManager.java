package org.firstinspires.ftc.robotcore.internal.network;

import org.firstinspires.inspection.InspectionState;

public class DegeneratePasswordManager implements PasswordManager {
    public String getPassword() {
        return null;
    }

    public boolean isDefault() {
        return false;
    }

    public String resetPassword(boolean z) {
        return InspectionState.NO_VERSION;
    }

    public void setPassword(String str, boolean z) {
    }
}
