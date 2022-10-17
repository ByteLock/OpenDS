package com.qualcomm.hardware.bosch;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class JustLoggingAccelerationIntegrator implements BNO055IMU.AccelerationIntegrator {
    Acceleration acceleration;
    BNO055IMU.Parameters parameters;

    public void initialize(BNO055IMU.Parameters parameters2, Position position, Velocity velocity) {
        this.parameters = parameters2;
    }

    public Position getPosition() {
        return new Position();
    }

    public Velocity getVelocity() {
        return new Velocity();
    }

    public Acceleration getAcceleration() {
        Acceleration acceleration2 = this.acceleration;
        return acceleration2 == null ? new Acceleration() : acceleration2;
    }

    public void update(Acceleration acceleration2) {
        if (acceleration2.acquisitionTime != 0) {
            Acceleration acceleration3 = this.acceleration;
            if (acceleration3 != null) {
                this.acceleration = acceleration2;
                if (this.parameters.loggingEnabled) {
                    RobotLog.m61vv(this.parameters.loggingTag, "dt=%.3fs accel=%s", Double.valueOf(((double) (this.acceleration.acquisitionTime - acceleration3.acquisitionTime)) * 1.0E-9d), this.acceleration);
                    return;
                }
                return;
            }
            this.acceleration = acceleration2;
        }
    }
}
