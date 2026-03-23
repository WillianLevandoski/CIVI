package com.civi.globe.domain;

public record HexTile(
        String id,
        int row,
        int column,
        double latitude,
        double longitude,
        Vector3 center
) {
}
