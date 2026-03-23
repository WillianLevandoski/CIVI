package com.civi.globe.service;

import com.civi.globe.domain.Cell;
import com.civi.globe.domain.CellType;
import com.civi.globe.domain.GlobeMesh;
import com.civi.globe.math.GoldbergFormula;
import com.civi.globe.math.Vector3;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

public final class GlobeMeshBuilder {

    private static final double GOLDEN_RATIO = (1.0d + Math.sqrt(5.0d)) / 2.0d;
    private static final double BASE_RING_RADIUS = 0.145d;
    private static final double DEFORM_FACTOR = 0.16d;

    private final SphereProjectionService projectionService;
    private final GlobeValidator validator;

    public GlobeMeshBuilder(SphereProjectionService projectionService, GlobeValidator validator) {
        this.projectionService = projectionService;
        this.validator = validator;
    }

    public GlobeMesh build(int m, int n) {
        validateParameters(m, n);
        int t = GoldbergFormula.calculateT(m, n);
        int pentagons = GoldbergFormula.pentagonCount();
        int hexagons = GoldbergFormula.hexagonCount(t);

        List<SeedCell> seeds = new ArrayList<>();
        seeds.addAll(createPentagonSeeds());
        seeds.addAll(createHexagonSeeds(hexagons, seeds.stream().map(SeedCell::center).toList()));

        Map<String, Set<String>> neighbors = buildNeighborGraph(seeds);
        List<Cell> cells = seeds.stream()
                .map(seed -> toCell(seed, neighbors.get(seed.id()), seeds))
                .toList();
        Map<String, Cell> cellsById = cells.stream().collect(Collectors.toMap(Cell::id, cell -> cell, (left, right) -> left, LinkedHashMap::new));

        GlobeMesh mesh = new GlobeMesh(m, n, t, pentagons, hexagons, cells, cellsById);
        validator.validate(mesh);
        return mesh;
    }

    private void validateParameters(int m, int n) {
        if (m < 0 || n < 0) {
            throw new IllegalArgumentException("m e n devem ser maiores ou iguais a zero.");
        }
        if (m == 0 && n == 0) {
            throw new IllegalArgumentException("m e n não podem ser zero ao mesmo tempo.");
        }
    }

    private List<SeedCell> createPentagonSeeds() {
        List<Vector3> centers = List.of(
                new Vector3(-1, GOLDEN_RATIO, 0),
                new Vector3(1, GOLDEN_RATIO, 0),
                new Vector3(-1, -GOLDEN_RATIO, 0),
                new Vector3(1, -GOLDEN_RATIO, 0),
                new Vector3(0, -1, GOLDEN_RATIO),
                new Vector3(0, 1, GOLDEN_RATIO),
                new Vector3(0, -1, -GOLDEN_RATIO),
                new Vector3(0, 1, -GOLDEN_RATIO),
                new Vector3(GOLDEN_RATIO, 0, -1),
                new Vector3(GOLDEN_RATIO, 0, 1),
                new Vector3(-GOLDEN_RATIO, 0, -1),
                new Vector3(-GOLDEN_RATIO, 0, 1)
        );
        List<SeedCell> seeds = new ArrayList<>();
        for (int index = 0; index < centers.size(); index++) {
            seeds.add(new SeedCell("P-%02d".formatted(index + 1), CellType.PENTAGON, projectionService.projectToUnitSphere(centers.get(index))));
        }
        return seeds;
    }

    private List<SeedCell> createHexagonSeeds(int count, List<Vector3> pentagonCenters) {
        List<SeedCell> seeds = new ArrayList<>();
        int generated = 0;
        int candidateIndex = 0;
        while (generated < count && candidateIndex < Math.max(4000, count * 180)) {
            Vector3 candidate = fibonacciPoint(candidateIndex++, count + 64);
            if (isTooClose(candidate, pentagonCenters, 0.42d)) {
                continue;
            }
            if (isTooClose(candidate, seeds.stream().map(SeedCell::center).toList(), 0.20d)) {
                continue;
            }
            CellType type = isNearCurvatureZone(candidate, pentagonCenters) ? CellType.DEFORMED_HEXAGON : CellType.HEXAGON;
            seeds.add(new SeedCell("H-%03d".formatted(generated + 1), type, candidate));
            generated++;
        }
        if (generated != count) {
            throw new IllegalStateException("Não foi possível distribuir todas as células hexagonais na esfera.");
        }
        return seeds;
    }

    private boolean isTooClose(Vector3 candidate, List<Vector3> points, double minDistance) {
        for (Vector3 point : points) {
            if (point.distanceTo(candidate) < minDistance) {
                return true;
            }
        }
        return false;
    }

    private boolean isNearCurvatureZone(Vector3 candidate, List<Vector3> pentagonCenters) {
        for (Vector3 pentagon : pentagonCenters) {
            if (pentagon.distanceTo(candidate) < 0.63d) {
                return true;
            }
        }
        return false;
    }

    private Vector3 fibonacciPoint(int index, int total) {
        double goldenAngle = Math.PI * (3.0d - Math.sqrt(5.0d));
        double y = 1.0d - (2.0d * (index + 0.5d) / total);
        double radius = Math.sqrt(Math.max(0.0d, 1.0d - (y * y)));
        double theta = goldenAngle * index;
        return projectionService.projectToUnitSphere(new Vector3(Math.cos(theta) * radius, y, Math.sin(theta) * radius));
    }

    private Map<String, Set<String>> buildNeighborGraph(List<SeedCell> seeds) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (SeedCell seed : seeds) {
            graph.put(seed.id(), new LinkedHashSet<>());
        }
        for (SeedCell seed : seeds) {
            int desired = desiredNeighborCount(seed.type());
            List<SeedCell> nearest = seeds.stream()
                    .filter(other -> !other.id().equals(seed.id()))
                    .sorted(Comparator.comparingDouble(other -> seed.center().distanceTo(other.center())))
                    .limit(desired)
                    .toList();
            for (SeedCell neighbor : nearest) {
                graph.get(seed.id()).add(neighbor.id());
                graph.get(neighbor.id()).add(seed.id());
            }
        }
        return graph;
    }

    private int desiredNeighborCount(CellType type) {
        if (type == CellType.PENTAGON) {
            return 5;
        }
        return 6;
    }

    private Cell toCell(SeedCell seed, Collection<String> neighborIds, List<SeedCell> allSeeds) {
        List<Vector3> orderedNeighbors = neighborIds.stream()
                .map(id -> findSeed(id, allSeeds))
                .sorted(Comparator.comparingDouble(other -> angleAround(seed.center(), other.center())))
                .map(SeedCell::center)
                .toList();
        List<Vector3> vertices = buildVertices(seed, orderedNeighbors);
        List<String> neighbors = neighborIds.stream().sorted().toList();
        return new Cell(seed.id(), seed.type(), seed.center(), vertices, neighbors);
    }

    private SeedCell findSeed(String id, List<SeedCell> seeds) {
        for (SeedCell seed : seeds) {
            if (seed.id().equals(id)) {
                return seed;
            }
        }
        throw new IllegalArgumentException("Seed não encontrada: " + id);
    }

    private List<Vector3> buildVertices(SeedCell seed, List<Vector3> orderedNeighbors) {
        int sides = seed.type() == CellType.PENTAGON ? 5 : 6;
        Vector3 center = seed.center();
        Vector3 reference = Math.abs(center.y()) > 0.9d ? new Vector3(1.0d, 0.0d, 0.0d) : new Vector3(0.0d, 1.0d, 0.0d);
        Vector3 tangentA = center.cross(reference).normalize();
        Vector3 tangentB = center.cross(tangentA).normalize();
        double baseRadius = seed.type() == CellType.PENTAGON ? BASE_RING_RADIUS * 0.95d : BASE_RING_RADIUS;

        List<Vector3> vertices = new ArrayList<>();
        for (int index = 0; index < sides; index++) {
            double angle = (Math.PI * 2.0d * index) / sides;
            double radialX = baseRadius;
            double radialY = baseRadius;
            if (!orderedNeighbors.isEmpty()) {
                angle = angleAround(center, orderedNeighbors.get(index % orderedNeighbors.size()));
            }
            if (seed.type() == CellType.DEFORMED_HEXAGON) {
                double wave = Math.sin((index * Math.PI * 2.0d) / sides);
                radialX = baseRadius * (1.0d + (DEFORM_FACTOR * 0.55d * wave));
                radialY = baseRadius * (1.0d - (DEFORM_FACTOR * 0.45d * wave));
            }
            vertices.add(projectionService.tangentOffset(center, tangentA, tangentB, angle, radialX, radialY));
        }
        return vertices;
    }

    private double angleAround(Vector3 center, Vector3 point) {
        Vector3 reference = Math.abs(center.y()) > 0.9d ? new Vector3(1.0d, 0.0d, 0.0d) : new Vector3(0.0d, 1.0d, 0.0d);
        Vector3 tangentA = center.cross(reference).normalize();
        Vector3 tangentB = center.cross(tangentA).normalize();
        Vector3 projected = point.subtract(center.scale(center.dot(point))).normalize();
        return Math.atan2(projected.dot(tangentB), projected.dot(tangentA));
    }

    private record SeedCell(String id, CellType type, Vector3 center) {
    }
}
