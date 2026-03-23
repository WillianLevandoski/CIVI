package com.civi.globe.core;

import com.civi.globe.math.Vector3;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GoldbergMeshBuilder {

    private static final double PENTAGON_SPACING = 0.55d;
    private static final double HEXAGON_SPACING = 0.25d;
    private static final double PENTAGON_RADIUS = 0.20d;
    private static final double HEXAGON_RADIUS = 0.16d;

    private final IcosahedronBuilder icosahedronBuilder = new IcosahedronBuilder();

    public GlobeMesh build(int m, int n) {
        validateParameters(m, n);
        int t = GoldbergFormula.computeT(m, n);
        int pentagons = GoldbergFormula.computePentagons();
        int hexagons = GoldbergFormula.computeHexagons(t);

        List<SeedCell> seeds = new ArrayList<>();
        List<Vector3> pentagonCenters = icosahedronBuilder.createVertices();
        for (int index = 0; index < pentagonCenters.size(); index++) {
            seeds.add(new SeedCell("P-%02d".formatted(index + 1), CellType.PENTAGON, pentagonCenters.get(index)));
        }

        List<Vector3> hexagonCenters = generateHexagonCenters(hexagons, pentagonCenters);
        for (int index = 0; index < hexagonCenters.size(); index++) {
            seeds.add(new SeedCell("H-%03d".formatted(index + 1), CellType.HEXAGON, hexagonCenters.get(index)));
        }

        Map<String, Set<String>> neighbors = buildNeighborGraph(seeds);
        List<Cell> cells = seeds.stream()
                .map(seed -> toCell(seed, neighbors.getOrDefault(seed.id(), Set.of()), seeds))
                .toList();

        GlobeMesh mesh = new GlobeMesh(m, n, t, pentagons, hexagons, cells);
        GlobeValidator.validate(mesh);
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

    private List<Vector3> generateHexagonCenters(int hexagonCount, List<Vector3> pentagonCenters) {
        List<Vector3> centers = new ArrayList<>();
        int candidateIndex = 0;
        int attempts = Math.max(2000, hexagonCount * 200);
        while (centers.size() < hexagonCount && candidateIndex < attempts) {
            Vector3 candidate = fibonacciPoint(candidateIndex++, hexagonCount + pentagonCenters.size() + 32);
            double pentagonDistance = pentagonCenters.stream()
                    .mapToDouble(center -> center.distanceTo(candidate))
                    .min()
                    .orElse(0.0d);
            if (pentagonDistance < PENTAGON_SPACING) {
                continue;
            }
            boolean tooClose = centers.stream().anyMatch(center -> center.distanceTo(candidate) < HEXAGON_SPACING);
            if (tooClose) {
                continue;
            }
            centers.add(candidate);
        }
        if (centers.size() < hexagonCount) {
            throw new IllegalStateException("Não foi possível distribuir os hexágonos na esfera.");
        }
        return centers;
    }

    private Vector3 fibonacciPoint(int index, int total) {
        double goldenAngle = Math.PI * (3.0d - Math.sqrt(5.0d));
        double y = 1.0d - (2.0d * (index + 0.5d) / total);
        double radius = Math.sqrt(Math.max(0.0d, 1.0d - (y * y)));
        double theta = goldenAngle * index;
        return new Vector3(Math.cos(theta) * radius, y, Math.sin(theta) * radius).normalize();
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
        ensureMinimumConnectivity(graph, seeds);
        return graph;
    }

    private void ensureMinimumConnectivity(Map<String, Set<String>> graph, List<SeedCell> seeds) {
        Map<String, SeedCell> seedById = seeds.stream()
                .collect(LinkedHashMap::new, (map, seed) -> map.put(seed.id(), seed), Map::putAll);
        for (SeedCell seed : seeds) {
            int desired = desiredNeighborCount(seed.type());
            Set<String> current = graph.get(seed.id());
            if (current.size() >= desired) {
                continue;
            }
            List<SeedCell> nearest = seeds.stream()
                    .filter(other -> !other.id().equals(seed.id()))
                    .filter(other -> !current.contains(other.id()))
                    .sorted(Comparator.comparingDouble(other -> seed.center().distanceTo(other.center())))
                    .limit(desired - current.size())
                    .toList();
            for (SeedCell neighbor : nearest) {
                graph.get(seed.id()).add(neighbor.id());
                graph.get(neighbor.id()).add(seed.id());
            }
        }
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            entry.setValue(entry.getValue().stream()
                    .sorted(Comparator.comparingDouble(id -> seedById.get(entry.getKey()).center().distanceTo(seedById.get(id).center())))
                    .limit(Math.max(desiredNeighborCount(seedById.get(entry.getKey()).type()), entry.getValue().size()))
                    .collect(LinkedHashSet::new, LinkedHashSet::add, LinkedHashSet::addAll));
        }
    }

    private int desiredNeighborCount(CellType type) {
        if (type == CellType.PENTAGON) {
            return 5;
        }
        return 6;
    }

    private Cell toCell(SeedCell seed, Collection<String> neighborIds, List<SeedCell> allSeeds) {
        List<Vector3> orderedNeighbors = neighborIds.stream()
                .map(id -> findById(allSeeds, id).center())
                .sorted(Comparator.comparingDouble(point -> angleAround(seed.center(), point)))
                .toList();
        List<Vector3> vertices = buildPolygon(seed.center(), orderedNeighbors, seed.type());
        return new Cell(seed.id(), seed.type(), seed.center(), vertices, neighborIds.stream().sorted().toList());
    }

    private SeedCell findById(List<SeedCell> allSeeds, String id) {
        return allSeeds.stream().filter(seed -> seed.id().equals(id)).findFirst().orElseThrow();
    }

    private List<Vector3> buildPolygon(Vector3 center, List<Vector3> neighbors, CellType type) {
        int sides = type == CellType.PENTAGON ? 5 : 6;
        double radius = type == CellType.PENTAGON ? PENTAGON_RADIUS : HEXAGON_RADIUS;
        Vector3 reference = Math.abs(center.y()) > 0.9d ? new Vector3(1.0d, 0.0d, 0.0d) : new Vector3(0.0d, 1.0d, 0.0d);
        Vector3 tangentA = center.cross(reference).normalize();
        Vector3 tangentB = center.cross(tangentA).normalize();
        List<Vector3> vertices = new ArrayList<>();
        for (int index = 0; index < sides; index++) {
            double angle = (Math.PI * 2.0d * index) / sides;
            if (index < neighbors.size()) {
                angle = angleAround(center, neighbors.get(index));
            }
            Vector3 vertex = center
                    .add(tangentA.scale(Math.cos(angle) * radius))
                    .add(tangentB.scale(Math.sin(angle) * radius))
                    .normalize();
            vertices.add(vertex);
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
