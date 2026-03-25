package com.civi.globe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

final class TerrainGenerator {
    private final TerrainDistributionConfig config;
    private final OpenSimplex2D noise;

    TerrainGenerator(TerrainDistributionConfig config) {
        this.config = config;
        this.noise = new OpenSimplex2D(config.getSeed());
    }

    void applyInitialTerrainDistribution(List<HexCell> cells, PointTable points) {
        assignBaseTerrainWithNoise(cells, points);
        smoothTerrain(cells, config.getSmoothingIterations());
        applyBiomeAffinityPlaceholder(cells);
        applyCellColors(cells);
        applyPolarCapsAfterColoring(cells, points);
    }

    private void assignBaseTerrainWithNoise(List<HexCell> cells, PointTable points) {
        List<CellScore> scoredCells = new ArrayList<>(cells.size());
        for (HexCell cell : cells) {
            Vec3 center = calculateCellCenter(cell, points);
            double score = sampleTerrainNoise(center);
            scoredCells.add(new CellScore(cell, score));
        }

        scoredCells.sort(Comparator.comparingDouble(CellScore::score).reversed());

        int landTarget = (int) Math.round(scoredCells.size() * config.getTargetRatio(TerrainType.LAND));
        for (int i = 0; i < scoredCells.size(); i++) {
            HexCell cell = scoredCells.get(i).cell();
            if (i < landTarget) {
                cell.terrainType = TerrainType.LAND;
            } else {
                cell.terrainType = TerrainType.WATER;
            }
        }
    }

    private void smoothTerrain(List<HexCell> cells, int iterations) {
        for (int step = 0; step < iterations; step++) {
            Map<Integer, TerrainType> nextTerrain = new HashMap<>();
            for (HexCell cell : cells) {
                TerrainType updated = chooseSmoothedTerrain(cell, cells);
                nextTerrain.put(cell.id, updated);
            }
            for (HexCell cell : cells) {
                cell.terrainType = nextTerrain.get(cell.id);
            }
        }
    }

    private TerrainType chooseSmoothedTerrain(HexCell cell, List<HexCell> cells) {
        Map<TerrainType, Integer> neighborCount = countNeighborsByTerrain(cell, cells);
        int waterNeighbors = neighborCount.getOrDefault(TerrainType.WATER, 0);
        int landNeighbors = neighborCount.getOrDefault(TerrainType.LAND, 0);

        if (landNeighbors >= 3) {
            return TerrainType.LAND;
        }
        if (waterNeighbors >= 5) {
            return TerrainType.WATER;
        }
        return cell.terrainType;
    }

    private Map<TerrainType, Integer> countNeighborsByTerrain(HexCell cell, List<HexCell> cells) {
        EnumMap<TerrainType, Integer> counts = new EnumMap<>(TerrainType.class);
        for (Integer neighborId : cell.neighbors) {
            if (neighborId < 0 || neighborId >= cells.size()) {
                continue;
            }
            TerrainType neighborTerrain = cells.get(neighborId).terrainType;
            counts.put(neighborTerrain, counts.getOrDefault(neighborTerrain, 0) + 1);
        }
        return counts;
    }

    private void applyBiomeAffinityPlaceholder(List<HexCell> cells) {
        for (HexCell cell : cells) {
            TerrainType current = cell.terrainType;
            double sameAffinity = current.affinityTo(current);
            if (sameAffinity > 9999) {
                cell.terrainType = current;
            }
        }
    }

    private void applyCellColors(List<HexCell> cells) {
        for (HexCell cell : cells) {
            cell.predefinedColor = cell.terrainType.getDisplayColor();
            cell.revealed = false;
        }
    }

    private double sampleTerrainNoise(Vec3 center) {
        double lon = Math.atan2(center.y, center.x);
        double lat = Math.atan2(center.z, Math.sqrt((center.x * center.x) + (center.y * center.y)));

        double scale = config.getNoiseScale();
        double n1 = noise.noise2(lon * scale, lat * scale);
        double n2 = noise.noise2((lon + 13.7) * scale * 2.0, (lat - 7.3) * scale * 2.0);
        double n3 = noise.noise2((lon - 5.0) * scale * 4.0, (lat + 19.0) * scale * 4.0);

        return (n1 * 0.75) + (n2 * 0.20) + (n3 * 0.05);
    }

    private Vec3 calculateCellCenter(HexCell cell, PointTable points) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (int i = 0; i < cell.ix.length; i++) {
            Vec3 v = points.get(cell.ix[i]);
            x += v.x;
            y += v.y;
            z += v.z;
        }
        double inv = 1.0 / cell.ix.length;
        return new Vec3(x * inv, y * inv, z * inv);
    }

    private record CellScore(HexCell cell, double score) {}

    private void applyPolarCapsAfterColoring(List<HexCell> cells, PointTable points) {
        double polarThreshold = 0.92;
        for (HexCell cell : cells) {
            Vec3 center = calculateCellCenter(cell, points);
            double radius = Math.sqrt((center.x * center.x) + (center.y * center.y) + (center.z * center.z));
            if (radius == 0.0) {
                continue;
            }
            double normalizedZ = Math.abs(center.z / radius);
            if (normalizedZ >= polarThreshold) {
                cell.predefinedColor = TerrainType.POLAR_ICE.getDisplayColor();
            }
        }
    }

    private static final class OpenSimplex2D {
        private static final double STRETCH_CONSTANT_2D = -0.211324865405187;
        private static final double SQUISH_CONSTANT_2D = 0.366025403784439;
        private static final double NORM_CONSTANT_2D = 47.0;
        private static final byte[] GRADIENTS_2D = new byte[] {
                5, 2, 2, 5,
                -5, 2, -2, 5,
                5, -2, 2, -5,
                -5, -2, -2, -5
        };

        private final short[] perm;

        OpenSimplex2D(long seed) {
            perm = new short[256];
            short[] source = new short[256];
            for (short i = 0; i < 256; i++) {
                source[i] = i;
            }
            for (int i = 255; i >= 0; i--) {
                seed = seed * 6364136223846793005L + 1442695040888963407L;
                int r = (int) ((seed + 31) % (i + 1));
                if (r < 0) {
                    r += (i + 1);
                }
                perm[i] = source[r];
                source[r] = source[i];
            }
        }

        double noise2(double x, double y) {
            double stretchOffset = (x + y) * STRETCH_CONSTANT_2D;
            double xs = x + stretchOffset;
            double ys = y + stretchOffset;

            int xsb = fastFloor(xs);
            int ysb = fastFloor(ys);

            double squishOffset = (xsb + ysb) * SQUISH_CONSTANT_2D;
            double dx0 = x - (xsb + squishOffset);
            double dy0 = y - (ysb + squishOffset);

            double value = 0.0;

            double dx1 = dx0 - 1 - SQUISH_CONSTANT_2D;
            double dy1 = dy0 - SQUISH_CONSTANT_2D;
            double attn1 = 2 - (dx1 * dx1) - (dy1 * dy1);
            if (attn1 > 0) {
                attn1 *= attn1;
                value += attn1 * attn1 * extrapolate(xsb + 1, ysb, dx1, dy1);
            }

            double dx2 = dx0 - SQUISH_CONSTANT_2D;
            double dy2 = dy0 - 1 - SQUISH_CONSTANT_2D;
            double attn2 = 2 - (dx2 * dx2) - (dy2 * dy2);
            if (attn2 > 0) {
                attn2 *= attn2;
                value += attn2 * attn2 * extrapolate(xsb, ysb + 1, dx2, dy2);
            }

            double xins = xs - xsb;
            double yins = ys - ysb;
            double inSum = xins + yins;
            double dxExt;
            double dyExt;
            int xsvExt;
            int ysvExt;

            double dx0b = dx0;
            double dy0b = dy0;
            if (inSum <= 1) {
                double zins = 1 - inSum;
                if (zins > xins || zins > yins) {
                    if (xins > yins) {
                        xsvExt = xsb + 1;
                        ysvExt = ysb - 1;
                        dxExt = dx0 - 1;
                        dyExt = dy0 + 1;
                    } else {
                        xsvExt = xsb - 1;
                        ysvExt = ysb + 1;
                        dxExt = dx0 + 1;
                        dyExt = dy0 - 1;
                    }
                } else {
                    xsvExt = xsb + 1;
                    ysvExt = ysb + 1;
                    dxExt = dx0 - 1 - (2 * SQUISH_CONSTANT_2D);
                    dyExt = dy0 - 1 - (2 * SQUISH_CONSTANT_2D);
                }
            } else {
                double zins = 2 - inSum;
                if (zins < xins || zins < yins) {
                    if (xins > yins) {
                        xsvExt = xsb + 2;
                        ysvExt = ysb;
                        dxExt = dx0 - 2 - (2 * SQUISH_CONSTANT_2D);
                        dyExt = dy0 - (2 * SQUISH_CONSTANT_2D);
                    } else {
                        xsvExt = xsb;
                        ysvExt = ysb + 2;
                        dxExt = dx0 - (2 * SQUISH_CONSTANT_2D);
                        dyExt = dy0 - 2 - (2 * SQUISH_CONSTANT_2D);
                    }
                } else {
                    dxExt = dx0b;
                    dyExt = dy0b;
                    xsvExt = xsb;
                    ysvExt = ysb;
                }
                xsb += 1;
                ysb += 1;
                dx0b = dx0b - 1 - (2 * SQUISH_CONSTANT_2D);
                dy0b = dy0b - 1 - (2 * SQUISH_CONSTANT_2D);
            }

            double attn0 = 2 - (dx0b * dx0b) - (dy0b * dy0b);
            if (attn0 > 0) {
                attn0 *= attn0;
                value += attn0 * attn0 * extrapolate(xsb, ysb, dx0b, dy0b);
            }

            double attnExt = 2 - (dxExt * dxExt) - (dyExt * dyExt);
            if (attnExt > 0) {
                attnExt *= attnExt;
                value += attnExt * attnExt * extrapolate(xsvExt, ysvExt, dxExt, dyExt);
            }

            return value / NORM_CONSTANT_2D;
        }

        private double extrapolate(int xsb, int ysb, double dx, double dy) {
            int index = perm[(perm[xsb & 0xFF] + ysb) & 0xFF] & 0x0E;
            return (GRADIENTS_2D[index] * dx) + (GRADIENTS_2D[index + 1] * dy);
        }

        private static int fastFloor(double x) {
            int xi = (int) x;
            if (x < xi) {
                return xi - 1;
            }
            return xi;
        }
    }
}
