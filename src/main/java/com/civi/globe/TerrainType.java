package com.civi.globe;

import javafx.scene.paint.Color;

import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;

enum TerrainType {
    WATER(0.65, 1, Color.DODGERBLUE),
    LAND(0.35, 2, Color.GOLD);

    private final double baseChance;
    private final int priority;
    private final Color displayColor;
    private final Map<TerrainType, Double> affinityWeights = new EnumMap<>(TerrainType.class);

    TerrainType(double baseChance, int priority, Color displayColor) {
        this.baseChance = baseChance;
        this.priority = priority;
        this.displayColor = displayColor;
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
