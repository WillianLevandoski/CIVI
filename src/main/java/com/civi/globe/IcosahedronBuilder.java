package com.civi.globe;

import java.util.List;

public final class IcosahedronBuilder {
    public record Face(int a, int b, int c) {
    }

    public record IcosahedronData(List<Vec3> vertices, List<Face> faces) {
    }

    public IcosahedronData build(double radius) {
        double s = 2.0 / Math.sqrt(5.0);
        double c = 1.0 / Math.sqrt(5.0);
        Vec3[] points = new Vec3[12];
        points[0] = new Vec3(0.0, 0.0, 1.0).withLength(radius);
        for (int i = 0; i < 5; i++) {
            double angle = i * Math.PI * 2.0 / 5.0;
            points[i + 1] = new Vec3(s * Math.cos(angle), s * Math.sin(angle), c).withLength(radius);
        }
        for (int i = 0; i < 6; i++) {
            Vec3 p = points[i];
            points[i + 6] = new Vec3(-p.x, p.y, -p.z).withLength(radius);
        }
        List<Face> faces = List.of(
            new Face(0, 1, 2), new Face(0, 2, 3), new Face(0, 3, 4), new Face(0, 4, 5), new Face(0, 5, 1),
            new Face(6, 7, 8), new Face(6, 8, 9), new Face(6, 9, 10), new Face(6, 10, 11), new Face(6, 11, 7),
            new Face(1, 2, 11), new Face(2, 3, 10), new Face(3, 4, 9), new Face(4, 5, 8), new Face(5, 1, 7),
            new Face(1, 11, 7), new Face(2, 10, 11), new Face(3, 9, 10), new Face(4, 8, 9), new Face(5, 7, 8)
        );
        return new IcosahedronData(List.of(points), faces);
    }
}
