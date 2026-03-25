package com.civi.globe;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

enum TerrainType {
    WATER(0.60, 1, Color.DODGERBLUE, Color.DODGERBLUE),
    LAND(0.40, 2, Color.GOLD, Color.FORESTGREEN);

    private final double baseChance;
    private final int priority;
    private final Color displayColor;
    private final Color secondaryDisplayColor;
    private final Map<TerrainType, Double> affinityWeights = new HashMap<>();

    TerrainType(double baseChance, int priority, Color displayColor, Color secondaryDisplayColor) {
        this.baseChance = baseChance;
        this.priority = priority;
        this.displayColor = displayColor;
        this.secondaryDisplayColor = secondaryDisplayColor;
    }

    double getBaseChance() {
        return baseChance;
    }

    int getPriority() {
        return priority;
    }

    Color getDisplayColor() {
        return displayColor;
    }

    Color getSecondaryDisplayColor() {
        return secondaryDisplayColor;
    }

    void setAffinity(TerrainType other, double weight) {
        affinityWeights.put(other, weight);
    }

    double affinityTo(TerrainType other) {
        Double weight = affinityWeights.get(other);
        if (weight == null) {
            return 0.0;
        }
        return weight;
    }

    Map<TerrainType, Double> getAffinityWeights() {
        return Collections.unmodifiableMap(affinityWeights);
    }
}
