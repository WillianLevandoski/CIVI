package com.civi.globe.service;

import com.civi.globe.math.Vector3;

public final class SphereProjectionService {

    public Vector3 normalize(Vector3 vector) {
        return vector.normalize();
    }

    public Vector3 projectToSphere(Vector3 vector, double radius) {
        return normalize(vector).scale(radius);
    }

    public Vector3 projectToUnitSphere(Vector3 vector) {
        return normalize(vector);
    }

    public Vector3 tangentOffset(Vector3 center, Vector3 tangentA, Vector3 tangentB, double angle, double radiusX, double radiusY) {
        Vector3 offset = tangentA.scale(Math.cos(angle) * radiusX)
                .add(tangentB.scale(Math.sin(angle) * radiusY));
        return projectToUnitSphere(center.add(offset));
    }
}
