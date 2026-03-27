package com.civi.globe;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.List;

final class HexCell {
    int id;
    final int[] ix = new int[6];
    final List<Integer> neighbors = new ArrayList<>();
    int a;
    int b;
    TerrainType terrainType = TerrainType.WATER;
    String terrainName = "Ocean";
    Color predefinedColor;
    boolean revealed;
}
