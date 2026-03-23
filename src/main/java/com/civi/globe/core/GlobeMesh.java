package com.civi.globe.core;

import java.util.List;

public record GlobeMesh(
        int m,
        int n,
        int t,
        int pentagonCount,
        int hexagonCount,
        List<Cell> cells
) {
}
