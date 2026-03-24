package com.civi.globe;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class PointTable {
    private static final double QUANT = 1e9;
    private final List<Vec3> points = new ArrayList<>();
    private final Map<String, Integer> index = new HashMap<>();

    int add(double x, double y, double z) {
        String key = key(x, y, z);
        Integer existing = index.get(key);
        if (existing != null) {
            return existing;
        }
        int ix = points.size();
        points.add(new Vec3(x, y, z));
        index.put(key, ix);
        return ix;
    }

    Vec3 get(int ix) {
        return points.get(ix);
    }

    int size() {
        return points.size();
    }

    private static String key(double x, double y, double z) {
        long qx = Math.round(x * QUANT);
        long qy = Math.round(y * QUANT);
        long qz = Math.round(z * QUANT);
        return qx + ":" + qy + ":" + qz;
    }
}
