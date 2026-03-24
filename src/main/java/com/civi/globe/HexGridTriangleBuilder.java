package com.civi.globe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HexGridTriangleBuilder {
    public record PatchCell2D(String localId, Vec2 center, List<Vec2> vertices2D, int i, int j, int k) {
    }

    private final Vec2 a = new Vec2(-0.5, 0.0);
    private final Vec2 b = new Vec2(0.5, 0.0);
    private final Vec2 c = new Vec2(0.0, Math.sqrt(3.0) / 2.0);

    public List<PatchCell2D> build(int resolution) {
        if (resolution < 1) {
            throw new IllegalArgumentException("resolution must be >= 1");
        }
        double step = 1.0 / resolution;
        double hexSide = step / Math.sqrt(3.0);
        List<PatchCell2D> cells = new ArrayList<>();
        for (int i = 0; i <= resolution; i++) {
            for (int j = 0; j <= resolution - i; j++) {
                int k = resolution - i - j;
                Vec2 center = baryToPoint(i / (double) resolution, j / (double) resolution, k / (double) resolution);
                List<Vec2> hex = createHex(center, hexSide);
                List<Vec2> clipped = clipPolygonToTriangle(hex);
                if (clipped.size() >= 3) {
                    cells.add(new PatchCell2D(i + ":" + j + ":" + k, center, clipped, i, j, k));
                }
            }
        }
        return cells;
    }

    private Vec2 baryToPoint(double l1, double l2, double l3) {
        return a.scale(l1).add(b.scale(l2)).add(c.scale(l3));
    }

    private List<Vec2> createHex(Vec2 center, double side) {
        double dx = Math.sqrt(3.0) * 0.5 * side;
        double dy = 0.5 * side;
        return List.of(
            center.add(new Vec2(0.0, side)),
            center.add(new Vec2(dx, dy)),
            center.add(new Vec2(dx, -dy)),
            center.add(new Vec2(0.0, -side)),
            center.add(new Vec2(-dx, -dy)),
            center.add(new Vec2(-dx, dy))
        );
    }

    private List<Vec2> clipPolygonToTriangle(List<Vec2> polygon) {
        List<Vec2> output = new ArrayList<>(polygon);
        output = clipAgainstEdge(output, a, b, c);
        output = clipAgainstEdge(output, b, c, a);
        output = clipAgainstEdge(output, c, a, b);
        return sortPolygon(output);
    }

    private List<Vec2> clipAgainstEdge(List<Vec2> input, Vec2 edgeStart, Vec2 edgeEnd, Vec2 insidePoint) {
        List<Vec2> output = new ArrayList<>();
        if (input.isEmpty()) {
            return output;
        }
        Vec2 previous = input.get(input.size() - 1);
        boolean previousInside = isInside(previous, edgeStart, edgeEnd, insidePoint);
        for (Vec2 current : input) {
            boolean currentInside = isInside(current, edgeStart, edgeEnd, insidePoint);
            if (currentInside) {
                if (!previousInside) {
                    output.add(intersection(previous, current, edgeStart, edgeEnd));
                }
                output.add(current);
            } else if (previousInside) {
                output.add(intersection(previous, current, edgeStart, edgeEnd));
            }
            previous = current;
            previousInside = currentInside;
        }
        return output;
    }

    private boolean isInside(Vec2 point, Vec2 edgeStart, Vec2 edgeEnd, Vec2 insidePoint) {
        double ref = edgeEnd.subtract(edgeStart).cross(insidePoint.subtract(edgeStart));
        double test = edgeEnd.subtract(edgeStart).cross(point.subtract(edgeStart));
        return ref >= 0.0 ? test >= -1.0e-9 : test <= 1.0e-9;
    }

    private Vec2 intersection(Vec2 p0, Vec2 p1, Vec2 e0, Vec2 e1) {
        Vec2 r = p1.subtract(p0);
        Vec2 s = e1.subtract(e0);
        double denominator = r.cross(s);
        if (Math.abs(denominator) < 1.0e-10) {
            return p0;
        }
        double t = e0.subtract(p0).cross(s) / denominator;
        return p0.add(r.scale(t));
    }

    private List<Vec2> sortPolygon(List<Vec2> polygon) {
        if (polygon.size() <= 2) {
            return polygon;
        }
        Vec2 centroid = new Vec2(0.0, 0.0);
        for (Vec2 point : polygon) {
            centroid = centroid.add(point);
        }
        centroid = centroid.scale(1.0 / polygon.size());
        Vec2 center = centroid;
        return polygon.stream()
            .distinct()
            .sorted(Comparator.comparingDouble(p -> Math.atan2(p.y - center.y, p.x - center.x)))
            .toList();
    }
}
