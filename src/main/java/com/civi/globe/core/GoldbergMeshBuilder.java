package com.civi.globe.core;

import com.civi.globe.math.Vector3;
import java.util.ArrayList;
import java.util.logging.Logger;
import java.util.Collection;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class GoldbergMeshBuilder {

    private static final Logger LOGGER = Logger.getLogger(GoldbergMeshBuilder.class.getName());

    private final IcosahedronBuilder icosahedronBuilder = new IcosahedronBuilder();

    public GlobeMesh build(int m, int n) {
        validateParameters(m, n);
        int t = GoldbergFormula.computeT(m, n);
        int pentagons = GoldbergFormula.computePentagons();
        int hexagons = GoldbergFormula.computeHexagons(t);
        LOGGER.info(() -> "Construindo malha Goldberg com m=%d, n=%d, T=%d, pentágonos=%d e hexágonos=%d."
                .formatted(m, n, t, pentagons, hexagons));

        List<SeedCell> seeds = new ArrayList<>();
        List<Vector3> pentagonCenters = icosahedronBuilder.createVertices();
        LOGGER.info(() -> "Vértices base do icosaedro gerados: " + pentagonCenters.size());
        for (int index = 0; index < pentagonCenters.size(); index++) {
            seeds.add(new SeedCell("P-%02d".formatted(index + 1), CellType.PENTAGON, pentagonCenters.get(index)));
        }

        List<Vector3> hexagonCenters = generateHexagonCenters(hexagons, pentagonCenters);
        LOGGER.info(() -> "Centros hexagonais gerados: " + hexagonCenters.size());
        for (int index = 0; index < hexagonCenters.size(); index++) {
            seeds.add(new SeedCell("H-%03d".formatted(index + 1), CellType.HEXAGON, hexagonCenters.get(index)));
        }

        LOGGER.info(() -> "Total de sementes para a malha: " + seeds.size());
        Map<String, Set<String>> neighbors = buildNeighborGraph(seeds);
        List<Cell> cells = seeds.stream()
                .map(seed -> toCell(seed, neighbors.getOrDefault(seed.id(), Set.of()), seeds))
                .toList();

        GlobeMesh mesh = new GlobeMesh(m, n, t, pentagons, hexagons, cells);
        GlobeValidator.validate(mesh);
        LOGGER.info(() -> "Validação concluída. Células finais: " + cells.size());
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
        LOGGER.info(() -> "Iniciando geração de " + hexagonCount + " centros hexagonais.");
        List<Vector3> centers = new ArrayList<>();
        int index = 0;
        while (centers.size() < hexagonCount) {
            Vector3 candidate = fibonacciPoint(index++);
            double minimumDistance = pentagonCenters.stream()
                    .mapToDouble(center -> center.distanceTo(candidate))
                    .min()
                    .orElse(0.0d);
            if (minimumDistance < 0.45d) {
                continue;
            }
            boolean tooCloseToExisting = centers.stream().anyMatch(center -> center.distanceTo(candidate) < 0.22d);
            if (tooCloseToExisting) {
                continue;
            }
            centers.add(candidate);
            if (centers.size() % 100 == 0 || centers.size() == hexagonCount) {
                LOGGER.info(() -> "Progresso da geração de hexágonos: " + centers.size() + "/" + hexagonCount);
            }
        }
        return centers;
    }

    private Vector3 fibonacciPoint(int index) {
        double offset = index + 0.5d;
        double phi = Math.acos(1.0d - (2.0d * offset / (offset + 200.0d)));
        double theta = Math.PI * (3.0d - Math.sqrt(5.0d)) * index;
        return new Vector3(
                Math.cos(theta) * Math.sin(phi),
                Math.cos(phi),
                Math.sin(theta) * Math.sin(phi)
        ).normalize();
    }

    private Map<String, Set<String>> buildNeighborGraph(List<SeedCell> seeds) {
        Map<String, Set<String>> graph = new LinkedHashMap<>();
        for (SeedCell seed : seeds) {
            graph.put(seed.id(), new LinkedHashSet<>());
        }
        for (SeedCell seed : seeds) {
            int desired = seed.type() == CellType.PENTAGON ? 5 : 6;
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
        rebalance(graph, seeds);
        return graph;
    }

    private void rebalance(Map<String, Set<String>> graph, List<SeedCell> seeds) {
        Map<String, SeedCell> byId = seeds.stream().collect(LinkedHashMap::new, (map, seed) -> map.put(seed.id(), seed), Map::putAll);
        for (SeedCell seed : seeds) {
            int desired = seed.type() == CellType.PENTAGON ? 5 : 6;
            Set<String> current = graph.get(seed.id());
            if (current.size() <= desired) {
                continue;
            }
            List<String> ordered = current.stream()
                    .sorted(Comparator.comparingDouble(id -> byId.get(id).center().distanceTo(seed.center())))
                    .toList();
            Set<String> trimmed = new LinkedHashSet<>(ordered.subList(0, desired));
            graph.put(seed.id(), trimmed);
        }
        for (Map.Entry<String, Set<String>> entry : graph.entrySet()) {
            for (String neighborId : entry.getValue()) {
                graph.computeIfAbsent(neighborId, ignored -> new LinkedHashSet<>()).add(entry.getKey());
            }
        }
    }

    private Cell toCell(SeedCell seed, Collection<String> neighborIds, List<SeedCell> allSeeds) {
        List<Vector3> orderedNeighbors = neighborIds.stream()
                .map(id -> findById(allSeeds, id).center())
                .sorted(Comparator.comparingDouble(point -> angleAround(seed.center(), point)))
                .toList();
        List<Vector3> vertices = buildPolygon(seed.center(), orderedNeighbors, seed.type() == CellType.PENTAGON ? 5 : 6);
        return new Cell(seed.id(), seed.type(), seed.center(), vertices, neighborIds.stream().sorted().toList());
    }

    private SeedCell findById(List<SeedCell> allSeeds, String id) {
        return allSeeds.stream().filter(seed -> seed.id().equals(id)).findFirst().orElseThrow();
    }

    private double angleAround(Vector3 center, Vector3 point) {
        Vector3 north = Math.abs(center.y()) > 0.9d ? new Vector3(1.0d, 0.0d, 0.0d) : new Vector3(0.0d, 1.0d, 0.0d);
        Vector3 tangentA = center.cross(north).normalize();
        Vector3 tangentB = center.cross(tangentA).normalize();
        Vector3 direction = point.subtract(center.scale(center.dot(point))).normalize();
        double x = direction.dot(tangentA);
        double y = direction.dot(tangentB);
        return Math.atan2(y, x);
    }

    private List<Vector3> buildPolygon(Vector3 center, List<Vector3> neighborDirections, int sides) {
        Vector3 north = Math.abs(center.y()) > 0.9d ? new Vector3(1.0d, 0.0d, 0.0d) : new Vector3(0.0d, 1.0d, 0.0d);
        Vector3 tangentA = center.cross(north).normalize();
        Vector3 tangentB = center.cross(tangentA).normalize();
        double radius = sides == 5 ? 0.22d : 0.18d;
        List<Vector3> vertices = new ArrayList<>();
        for (int index = 0; index < sides; index++) {
            double angle = (Math.PI * 2.0d * index) / sides;
            if (index < neighborDirections.size()) {
                angle = angleAround(center, neighborDirections.get(index));
            }
            Vector3 vertex = center
                    .add(tangentA.scale(Math.cos(angle) * radius))
                    .add(tangentB.scale(Math.sin(angle) * radius))
                    .normalize();
            vertices.add(vertex);
        }
        return vertices;
    }

    private record SeedCell(String id, CellType type, Vector3 center) {
    }
}
