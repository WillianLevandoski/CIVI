package com.civi.globe.domain;

public record Vector3(double x, double y, double z) {

    public Vector3 normalize() {
        double length = Math.sqrt((x * x) + (y * y) + (z * z));
        if (length == 0.0d) {
            return this;
        }
        return new Vector3(x / length, y / length, z / length);
    }
}
