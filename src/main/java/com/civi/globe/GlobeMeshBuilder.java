package com.civi.globe;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public final class GlobeMeshBuilder {
    // This implementation follows the practical Stack Overflow approach: 2D triangular hex cutout -> icosahedron face -> barycentric + double slerp mapping.
    private final IcosahedronBuilder icosahedronBuilder = new IcosahedronBuilder();
    private final HexGridTriangleBuilder patchBuilder = new HexGridTriangleBuilder();
    private final SphericalMapper mapper = new SphericalMapper();

    public GlobeMesh build(double radius, int resolution) {
        IcosahedronBuilder.IcosahedronData ico = icosahedronBuilder.build(radius);
        List<HexGridTriangleBuilder.PatchCell2D> patchCells = patchBuilder.build(resolution);
        Map<String, CellAccumulator> accumulators = new LinkedHashMap<>();
        for (int faceIndex = 0; faceIndex < ico.faces().size(); faceIndex++) {
            final int currentFaceIndex = faceIndex;
            IcosahedronBuilder.Face face = ico.faces().get(faceIndex);
            Vec3 s1 = ico.vertices().get(face.a());
            Vec3 s2 = ico.vertices().get(face.b());
            Vec3 s3 = ico.vertices().get(face.c());
            for (HexGridTriangleBuilder.PatchCell2D patchCell : patchCells) {
                Vec3 center = mapper.mapPoint(patchCell.center(), s1, s2, s3, radius);
                String key = key(center);
                CellAccumulator accumulator = accumulators.computeIfAbsent(key, ignored -> new CellAccumulator(key, center, currentFaceIndex));
                accumulator.faces.add(faceIndex);
                for (Vec2 v2 : patchCell.vertices2D()) {
                    Vec3 vertex = mapper.mapPoint(v2, s1, s2, s3, radius * 1.0008);
                    accumulator.vertices.putIfAbsent(key(vertex), vertex);
                }
            }
        }
        List<Cell> cells = new ArrayList<>();
        Map<String, Cell> cellsById = new LinkedHashMap<>();
        for (CellAccumulator accumulator : accumulators.values()) {
            List<Vec3> polygon = orderVertices(accumulator.center, accumulator.vertices.values().stream().toList());
            if (polygon.size() < 5) {
                continue;
            }
            Cell.Type type = polygon.size() == 5 ? Cell.Type.PENT : Cell.Type.HEX;
            Cell cell = new Cell(
                accumulator.id,
                type,
                accumulator.center,
                polygon,
                accumulator.primaryFace,
                accumulator.faces.stream().sorted().toList(),
                List.of()
            );
            cells.add(cell);
            cellsById.put(cell.id(), cell);
        }
        Map<String, Set<String>> neighbors = buildNeighbors(cells);
        cells = cells.stream()
            .sorted(Comparator.comparing(Cell::id))
            .map(cell -> new Cell(cell.id(), cell.type(), cell.center(), cell.vertices(), cell.primaryFace(), cell.supportingFaces(), neighbors.getOrDefault(cell.id(), Set.of()).stream().sorted().toList()))
            .toList();
        cellsById = cells.stream().collect(LinkedHashMap::new, (map, cell) -> map.put(cell.id(), cell), Map::putAll);
        Group fillGroup = new Group();
        Group edgeGroup = new Group();
        List<GlobeMesh.EdgeSegment> edges = buildRenderableNodes(cells, fillGroup, edgeGroup);
        return new GlobeMesh(cells, edges, cellsById, fillGroup, edgeGroup);
    }

    private List<GlobeMesh.EdgeSegment> buildRenderableNodes(List<Cell> cells, Group fillGroup, Group edgeGroup) {
        PhongMaterial fillMaterial = new PhongMaterial(Color.rgb(5, 5, 5));
        PhongMaterial lineMaterial = new PhongMaterial(Color.WHITE);
        Map<String, GlobeMesh.EdgeSegment> uniqueEdges = new LinkedHashMap<>();
        for (Cell cell : cells) {
            MeshView meshView = new MeshView(createCellMesh(cell));
            meshView.setMaterial(fillMaterial);
            meshView.setUserData(cell.id());
            meshView.setCullFace(CullFace.NONE);
            meshView.setDrawMode(DrawMode.FILL);
            fillGroup.getChildren().add(meshView);
            List<Vec3> vertices = cell.vertices();
            for (int i = 0; i < vertices.size(); i++) {
                Vec3 start = vertices.get(i);
                Vec3 end = vertices.get((i + 1) % vertices.size());
                String aKey = key(start);
                String bKey = key(end);
                String edgeKey = aKey.compareTo(bKey) <= 0 ? aKey + "|" + bKey : bKey + "|" + aKey;
                if (!uniqueEdges.containsKey(edgeKey)) {
                    uniqueEdges.put(edgeKey, new GlobeMesh.EdgeSegment(start, end, aKey, bKey));
                    edgeGroup.getChildren().add(createEdgeCylinder(start, end, lineMaterial));
                }
            }
        }
        return new ArrayList<>(uniqueEdges.values());
    }

    private TriangleMesh createCellMesh(Cell cell) {
        TriangleMesh mesh = new TriangleMesh();
        mesh.getTexCoords().addAll(0.0f, 0.0f);
        Vec3 center = cell.center();
        mesh.getPoints().addAll((float) center.x, (float) center.y, (float) center.z);
        for (Vec3 vertex : cell.vertices()) {
            mesh.getPoints().addAll((float) vertex.x, (float) vertex.y, (float) vertex.z);
        }
        for (int i = 0; i < cell.vertices().size(); i++) {
            int next = ((i + 1) % cell.vertices().size()) + 1;
            int current = i + 1;
            mesh.getFaces().addAll(0, 0, current, 0, next, 0);
            mesh.getFaces().addAll(0, 0, next, 0, current, 0);
        }
        return mesh;
    }

    private Node createEdgeCylinder(Vec3 start, Vec3 end, PhongMaterial material) {
        Vec3 diff = end.subtract(start);
        double height = diff.length();
        Cylinder cylinder = new Cylinder(0.7, height, 10);
        cylinder.setMaterial(material);
        Vec3 mid = start.add(end).scale(0.5);
        Vec3 yAxis = new Vec3(0.0, 1.0, 0.0);
        Vec3 direction = diff.normalize();
        Vec3 axis = yAxis.cross(direction);
        double angle = Math.toDegrees(Math.acos(Math.max(-1.0, Math.min(1.0, yAxis.dot(direction)))));
        if (axis.length() < 1.0e-8) {
            axis = new Vec3(1.0, 0.0, 0.0);
        }
        cylinder.getTransforms().addAll(
            new Translate(mid.x, mid.y, mid.z),
            new Rotate(angle, axis.x, axis.y, axis.z)
        );
        return cylinder;
    }

    private Map<String, Set<String>> buildNeighbors(List<Cell> cells) {
        Map<String, Set<String>> neighbors = new HashMap<>();
        Map<String, List<String>> edgeOwners = new HashMap<>();
        for (Cell cell : cells) {
            List<Vec3> vertices = cell.vertices();
            for (int i = 0; i < vertices.size(); i++) {
                String a = key(vertices.get(i));
                String b = key(vertices.get((i + 1) % vertices.size()));
                String edgeKey = a.compareTo(b) <= 0 ? a + "|" + b : b + "|" + a;
                edgeOwners.computeIfAbsent(edgeKey, ignored -> new ArrayList<>()).add(cell.id());
            }
        }
        for (List<String> owners : edgeOwners.values()) {
            if (owners.size() == 2) {
                neighbors.computeIfAbsent(owners.get(0), ignored -> new TreeSet<>()).add(owners.get(1));
                neighbors.computeIfAbsent(owners.get(1), ignored -> new TreeSet<>()).add(owners.get(0));
            }
        }
        return neighbors;
    }

    private List<Vec3> orderVertices(Vec3 center, List<Vec3> vertices) {
        Vec3 normal = center.normalize();
        Vec3 reference = Math.abs(normal.z) < 0.9 ? new Vec3(0.0, 0.0, 1.0) : new Vec3(1.0, 0.0, 0.0);
        Vec3 tangent = normal.cross(reference).normalize();
        Vec3 bitangent = normal.cross(tangent).normalize();
        return vertices.stream()
            .distinct()
            .sorted(Comparator.comparingDouble(v -> Math.atan2(v.subtract(center).dot(bitangent), v.subtract(center).dot(tangent))))
            .toList();
    }

    private String key(Vec3 point) {
        return String.format(java.util.Locale.US, "%.6f:%.6f:%.6f", point.x, point.y, point.z);
    }

    private static final class CellAccumulator {
        private final String id;
        private final Vec3 center;
        private final int primaryFace;
        private final Set<Integer> faces = new LinkedHashSet<>();
        private final Map<String, Vec3> vertices = new LinkedHashMap<>();

        private CellAccumulator(String id, Vec3 center, int primaryFace) {
            this.id = id;
            this.center = center;
            this.primaryFace = primaryFace;
        }
    }
}
