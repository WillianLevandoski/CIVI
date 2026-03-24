package com.civi.globe;

final class Vec3 {
    double x;
    double y;
    double z;

    Vec3(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    Vec3 copy() {
        return new Vec3(x, y, z);
    }
}
