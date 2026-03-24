package com.civi.globe;

public final class Vec3 {
    public static final double EPSILON = 1.0e-9;

    public final double x;
    public final double y;
    public final double z;

    public Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3 add(Vec3 other) {
        return new Vec3(x + other.x, y + other.y, z + other.z);
    }

    public Vec3 subtract(Vec3 other) {
        return new Vec3(x - other.x, y - other.y, z - other.z);
    }

    public Vec3 scale(double factor) {
        return new Vec3(x * factor, y * factor, z * factor);
    }

    public double dot(Vec3 other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    public Vec3 cross(Vec3 other) {
        return new Vec3(
            (y * other.z) - (z * other.y),
            (z * other.x) - (x * other.z),
            (x * other.y) - (y * other.x)
        );
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public Vec3 normalize() {
        double length = length();
        if (length < EPSILON) {
            return new Vec3(0.0, 0.0, 0.0);
        }
        return scale(1.0 / length);
    }

    public Vec3 withLength(double radius) {
        return normalize().scale(radius);
    }

    public double distance(Vec3 other) {
        return subtract(other).length();
    }
}
