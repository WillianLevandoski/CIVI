package com.civi.globe.ui;

import com.civi.globe.domain.HexTile;
import java.awt.Polygon;

public record ProjectedTile(HexTile tile, Polygon polygon, double depth) {
}
