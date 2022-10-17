package com.qualcomm.hardware.bosch;

import com.qualcomm.hardware.bosch.BNO055IMU;
import com.qualcomm.robotcore.util.RobotLog;
import org.firstinspires.ftc.robotcore.external.navigation.Acceleration;
import org.firstinspires.ftc.robotcore.external.navigation.NavUtil;
import org.firstinspires.ftc.robotcore.external.navigation.Position;
import org.firstinspires.ftc.robotcore.external.navigation.Velocity;

public class NaiveAccelerationIntegrator implements BNO055IMU.AccelerationIntegrator {
    Acceleration acceleration = null;
    BNO055IMU.Parameters parameters = null;
    Position position = new Position();
    Velocity velocity = new Velocity();

    public Position getPosition() {
        return this.position;
    }

    public Velocity getVelocity() {
        return this.velocity;
    }

    public Acceleration getAcceleration() {
        return this.acceleration;
    }

    NaiveAccelerationIntegrator() {
    }

    public void initialize(BNO055IMU.Parameters parameters2, Position position2, Velocity velocity2) {
        this.parameters = parameters2;
        if (position2 == null) {
            position2 = this.position;
        }
        this.position = position2;
        if (velocity2 == null) {
            velocity2 = this.velocity;
        }
        this.velocity = velocity2;
        this.acceleration = null;
    }

    public void update(Acceleration acceleration2) {
        if (acceleration2.acquisitionTime != 0) {
            Acceleration acceleration3 = this.acceleration;
            if (acceleration3 != null) {
                Velocity velocity2 = this.velocity;
                this.acceleration = acceleration2;
                if (acceleration3.acquisitionTime != 0) {
                    this.velocity = NavUtil.plus(this.velocity, NavUtil.meanIntegrate(this.acceleration, acceleration3));
                }
                if (velocity2.acquisitionTime != 0) {
                    this.position = NavUtil.plus(this.position, NavUtil.meanIntegrate(this.velocity, velocity2));
                }
                BNO055IMU.Parameters parameters2 = this.parameters;
                if (parameters2 != null && parameters2.loggingEnabled) {
                    RobotLog.m61vv(this.parameters.loggingTag, "dt=%.3fs accel=%s vel=%s pos=%s", Double.valueOf(((double) (this.acceleration.acquisitionTime - acceleration3.acquisitionTime)) * 1.0E-9d), this.acceleration, this.velocity, this.position);
                    return;
                }
                return;
            }
            this.acceleration = acceleration2;
        }
    }
}
