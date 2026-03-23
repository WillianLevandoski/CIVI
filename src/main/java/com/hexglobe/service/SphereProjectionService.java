package com.hexglobe.service;

import com.hexglobe.math.Vector3;

public class SphereProjectionService {

    private final double radius;

    public SphereProjectionService(double radius) {
        this.radius = radius;
    }

    public Vector3 project(Vector3 point) {
        return point.normalize().scale(radius);
    }

    public Vector3 fromSpherical(double theta, double phi) {
        double x = radius * Math.sin(phi) * Math.cos(theta);
        double y = radius * Math.cos(phi);
        double z = radius * Math.sin(phi) * Math.sin(theta);
        return new Vector3(x, y, z);
    }

    public Vector3 midpointOnSphere(Vector3 a, Vector3 b) {
        return project(a.add(b));
    }

    public Vector3 centroidOnSphere(Vector3... points) {
        double sx = 0, sy = 0, sz = 0;
        for (Vector3 p : points) {
            sx += p.x;
            sy += p.y;
            sz += p.z;
        }
        return project(new Vector3(sx / points.length, sy / points.length, sz / points.length));
    }

    public double getRadius() {
        return radius;
    }
}
