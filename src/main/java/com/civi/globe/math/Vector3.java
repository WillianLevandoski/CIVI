package com.civi.globe.math;

public record Vector3(double x, double y, double z) {

    public static Vector3 zero() {
        return new Vector3(0.0d, 0.0d, 0.0d);
    }

    public Vector3 add(Vector3 other) {
        return new Vector3(x + other.x, y + other.y, z + other.z);
    }

    public Vector3 subtract(Vector3 other) {
        return new Vector3(x - other.x, y - other.y, z - other.z);
    }

    public Vector3 scale(double factor) {
        return new Vector3(x * factor, y * factor, z * factor);
    }

    public double dot(Vector3 other) {
        return (x * other.x) + (y * other.y) + (z * other.z);
    }

    public Vector3 cross(Vector3 other) {
        return new Vector3(
                (y * other.z) - (z * other.y),
                (z * other.x) - (x * other.z),
                (x * other.y) - (y * other.x)
        );
    }

    public double length() {
        return Math.sqrt(dot(this));
    }

    public Vector3 normalize() {
        double length = length();
        if (length == 0.0d) {
            return this;
        }
        return scale(1.0d / length);
    }

    public double distanceTo(Vector3 other) {
        return subtract(other).length();
    }
}
