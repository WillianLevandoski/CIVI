package com.civi.globe.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HexGlobe {

    private final List<HexTile> tiles;

    public HexGlobe(int latitudeBands, int baseColumns) {
        this.tiles = Collections.unmodifiableList(createTiles(latitudeBands, baseColumns));
    }

    public List<HexTile> tiles() {
        return tiles;
    }

    private List<HexTile> createTiles(int latitudeBands, int baseColumns) {
        List<HexTile> generatedTiles = new ArrayList<>();
        int safeLatitudeBands = Math.max(6, latitudeBands);
        int tileId = 1;

        for (int row = 0; row <= safeLatitudeBands; row++) {
            double verticalRatio = (double) row / safeLatitudeBands;
            double latitude = (-Math.PI / 2.0d) + (Math.PI * verticalRatio);
            double latitudeWeight = Math.max(0.35d, Math.cos(latitude));
            int columns = Math.max(6, (int) Math.round(baseColumns * latitudeWeight));
            double rowOffset = (row % 2 == 0) ? 0.0d : Math.PI / columns;

            for (int column = 0; column < columns; column++) {
                double longitude = ((2.0d * Math.PI * column) / columns) + rowOffset;
                Vector3 center = sphericalToCartesian(latitude, longitude).normalize();
                String id = "HEX-%03d".formatted(tileId++);
                generatedTiles.add(new HexTile(id, row, column, latitude, longitude, center));
            }
        }

        return generatedTiles;
    }

    private Vector3 sphericalToCartesian(double latitude, double longitude) {
        double cosLatitude = Math.cos(latitude);
        double x = cosLatitude * Math.cos(longitude);
        double y = Math.sin(latitude);
        double z = cosLatitude * Math.sin(longitude);
        return new Vector3(x, y, z);
    }
}
