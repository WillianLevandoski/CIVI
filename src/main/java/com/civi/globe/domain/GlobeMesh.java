package com.civi.globe.domain;

import java.util.List;
import java.util.Map;

public record GlobeMesh(
        int m,
        int n,
        int t,
        int pentagonCount,
        int hexagonCount,
        List<Cell> cells,
        Map<String, Cell> cellsById
) {
}
