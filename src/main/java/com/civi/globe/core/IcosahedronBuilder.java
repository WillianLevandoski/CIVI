package com.civi.globe.core;

import com.civi.globe.math.Vector3;
import java.util.ArrayList;
import java.util.List;

public final class IcosahedronBuilder {

    public List<Vector3> createVertices() {
        double phi = (1.0d + Math.sqrt(5.0d)) / 2.0d;
        List<Vector3> vertices = List.of(
                new Vector3(-1, phi, 0),
                new Vector3(1, phi, 0),
                new Vector3(-1, -phi, 0),
                new Vector3(1, -phi, 0),
                new Vector3(0, -1, phi),
                new Vector3(0, 1, phi),
                new Vector3(0, -1, -phi),
                new Vector3(0, 1, -phi),
                new Vector3(phi, 0, -1),
                new Vector3(phi, 0, 1),
                new Vector3(-phi, 0, -1),
                new Vector3(-phi, 0, 1)
        );
        return vertices.stream().map(Vector3::normalize).toList();
    }

    public List<int[]> createFaces() {
        return List.of(
                new int[]{0, 11, 5}, new int[]{0, 5, 1}, new int[]{0, 1, 7}, new int[]{0, 7, 10}, new int[]{0, 10, 11},
                new int[]{1, 5, 9}, new int[]{5, 11, 4}, new int[]{11, 10, 2}, new int[]{10, 7, 6}, new int[]{7, 1, 8},
                new int[]{3, 9, 4}, new int[]{3, 4, 2}, new int[]{3, 2, 6}, new int[]{3, 6, 8}, new int[]{3, 8, 9},
                new int[]{4, 9, 5}, new int[]{2, 4, 11}, new int[]{6, 2, 10}, new int[]{8, 6, 7}, new int[]{9, 8, 1}
        );
    }

    public List<Vector3> subdivideFaceSamples(int m, int n) {
        int t = GoldbergFormula.computeT(m, n);
        int target = GoldbergFormula.computeVertices(t);
        List<Vector3> seeds = new ArrayList<>(createVertices());
        if (target <= seeds.size()) {
            return seeds;
        }
        int extra = target - seeds.size();
        for (int index = 0; index < extra; index++) {
            double ratio = (index + 0.5d) / extra;
            double y = 1.0d - (2.0d * ratio);
            double radius = Math.sqrt(Math.max(0.0d, 1.0d - (y * y)));
            double theta = Math.PI * (3.0d - Math.sqrt(5.0d)) * index;
            seeds.add(new Vector3(Math.cos(theta) * radius, y, Math.sin(theta) * radius).normalize());
        }
        return seeds;
    }
}
