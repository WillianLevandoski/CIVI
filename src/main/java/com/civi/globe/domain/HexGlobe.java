package com.civi.globe.domain;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class HexGlobe {

    private static final double POLE_MARGIN = Math.toRadians(11.0d);

    private final List<HexTile> tiles;

    public HexGlobe(int latitudeBands, int baseColumns) {
        this.tiles = Collections.unmodifiableList(createTiles(latitudeBands, baseColumns));
    }

    public List<HexTile> tiles() {
        return tiles;
    }

    private List<HexTile> createTiles(int latitudeBands, int baseColumns) {
        List<HexTile> generatedTiles = new ArrayList<>();
        int safeLatitudeBands = Math.max(10, latitudeBands);
        double minLatitude = -Math.PI / 2.0d + POLE_MARGIN;
        double maxLatitude = Math.PI / 2.0d - POLE_MARGIN;
        double latitudeStep = (maxLatitude - minLatitude) / Math.max(1, safeLatitudeBands - 1);
        int tileId = 1;

        for (int row = 0; row < safeLatitudeBands; row++) {
            double latitude = minLatitude + (row * latitudeStep);
            double latitudeWeight = Math.max(0.18d, Math.cos(latitude));
            int columns = Math.max(5, (int) Math.round(baseColumns * latitudeWeight));
            double longitudeStep = (2.0d * Math.PI) / columns;
            double rowOffset = (row % 2 == 0) ? 0.0d : longitudeStep / 2.0d;

            for (int column = 0; column < columns; column++) {
                double longitude = (column * longitudeStep) + rowOffset;
                Vector3 center = sphericalToCartesian(latitude, longitude).normalize();
                String id = "HEX-%03d".formatted(tileId++);
                generatedTiles.add(new HexTile(id, row, column, latitude, longitude, latitudeStep, longitudeStep, center));
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
