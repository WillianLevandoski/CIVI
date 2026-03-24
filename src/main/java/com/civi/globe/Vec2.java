package com.civi.globe;

public final class Vec2 {
    public static final double EPSILON = 1.0e-9;

    public final double x;
    public final double y;

    public Vec2(double x, double y) {
        this.x = x;
        this.y = y;
    }

    public Vec2 add(Vec2 other) {
        return new Vec2(x + other.x, y + other.y);
    }

    public Vec2 subtract(Vec2 other) {
        return new Vec2(x - other.x, y - other.y);
    }

    public Vec2 scale(double factor) {
        return new Vec2(x * factor, y * factor);
    }

    public double cross(Vec2 other) {
        return (x * other.y) - (y * other.x);
    }

    public double dot(Vec2 other) {
        return (x * other.x) + (y * other.y);
    }
}
