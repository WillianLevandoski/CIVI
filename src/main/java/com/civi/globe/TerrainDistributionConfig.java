package com.civi.globe;

import java.util.EnumMap;
import java.util.Map;
import java.util.concurrent.ThreadLocalRandom;

final class TerrainDistributionConfig {
    private final long seed;
    private final int smoothingIterations;
    private final Map<TerrainType, Double> targetRatios;
    private final double noiseScale;

    TerrainDistributionConfig(long seed, int smoothingIterations, Map<TerrainType, Double> targetRatios, double noiseScale) {
        this.seed = seed;
        this.smoothingIterations = smoothingIterations;
        this.targetRatios = new EnumMap<>(targetRatios);
        this.noiseScale = noiseScale;
    }

    static TerrainDistributionConfig defaultConfig() {
        EnumMap<TerrainType, Double> ratios = new EnumMap<>(TerrainType.class);
        ratios.put(TerrainType.WATER, TerrainType.WATER.getBaseChance());
        ratios.put(TerrainType.LAND, TerrainType.LAND.getBaseChance());
        return new TerrainDistributionConfig(
                ThreadLocalRandom.current().nextLong(),
                3,
                ratios,
                2.6
        );
    }

    long getSeed() {
        return seed;
    }

    int getSmoothingIterations() {
        return smoothingIterations;
    }

    double getTargetRatio(TerrainType terrainType) {
        Double ratio = targetRatios.get(terrainType);
        if (ratio == null) {
            return 0.0;
        }
        return ratio;
    }

    double getNoiseScale() {
        return noiseScale;
    }
}
