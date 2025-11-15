package com.logisticscraft.occlusionculling.util;

public class Vec3d {
    public double x;
    public double y;
    public double z;

    public Vec3d(double x, double y, double z) {
        set(x, y, z);
    }

    public void set(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }
}
