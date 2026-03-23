package com.civi.globe.core;

import com.civi.globe.math.Vector3;
import java.util.List;

public record Cell(
        String id,
        CellType type,
        Vector3 center,
        List<Vector3> vertices,
        List<String> neighborIds
) {
}
