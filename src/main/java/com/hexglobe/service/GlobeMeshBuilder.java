package com.hexglobe.service;

import com.hexglobe.math.GoldbergFormula;
import com.hexglobe.math.Vector3;
import com.hexglobe.model.Cell;
import com.hexglobe.model.CellType;
import com.hexglobe.model.GlobeMesh;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class GlobeMeshBuilder {
    private static final double CELL_VERTEX_INSET = 0.78;

    private final SphereProjectionService projection;
    private final GoldbergFormula formula;
    private final double radius;

    public GlobeMeshBuilder(double radius, int m, int n) {
        this.radius = radius;
        this.projection = new SphereProjectionService(radius);
        this.formula = new GoldbergFormula(m, n);
    }

    public GlobeMesh build() {
        GlobeMesh mesh = new GlobeMesh(radius);

        List<Vector3> icoVertices = buildIcosahedronVertices();
        List<int[]> icoFaces = buildIcosahedronFaces();

        List<Vector3> subVerts = new ArrayList<>(icoVertices);
        List<int[]> subFaces = new ArrayList<>(icoFaces);

        int subdivisions = formula.getM() + formula.getN();
        for (int s = 0; s < subdivisions; s++) {
            subdivide(subVerts, subFaces);
        }

        List<Cell> cells = buildDualCells(subVerts, subFaces);
        for (Cell cell : cells) {
            mesh.addCell(cell);
        }

        connectNeighbors(cells, subFaces, subVerts);

        System.out.println(formula);
        return mesh;
    }

    private List<Vector3> buildIcosahedronVertices() {
        double phi = (1.0 + Math.sqrt(5.0)) / 2.0;
        List<Vector3> verts = new ArrayList<>();
        addNorm(verts, -1,  phi,  0);
        addNorm(verts,  1,  phi,  0);
        addNorm(verts, -1, -phi,  0);
        addNorm(verts,  1, -phi,  0);
        addNorm(verts,  0, -1,  phi);
        addNorm(verts,  0,  1,  phi);
        addNorm(verts,  0, -1, -phi);
        addNorm(verts,  0,  1, -phi);
        addNorm(verts,  phi, 0, -1);
        addNorm(verts,  phi, 0,  1);
        addNorm(verts, -phi, 0, -1);
        addNorm(verts, -phi, 0,  1);
        return verts;
    }

    private void addNorm(List<Vector3> list, double x, double y, double z) {
        list.add(projection.project(new Vector3(x, y, z)));
    }

    private List<int[]> buildIcosahedronFaces() {
        List<int[]> faces = new ArrayList<>();
        faces.add(new int[]{0, 11, 5});
        faces.add(new int[]{0, 5, 1});
        faces.add(new int[]{0, 1, 7});
        faces.add(new int[]{0, 7, 10});
        faces.add(new int[]{0, 10, 11});
        faces.add(new int[]{1, 5, 9});
        faces.add(new int[]{5, 11, 4});
        faces.add(new int[]{11, 10, 2});
        faces.add(new int[]{10, 7, 6});
        faces.add(new int[]{7, 1, 8});
        faces.add(new int[]{3, 9, 4});
        faces.add(new int[]{3, 4, 2});
        faces.add(new int[]{3, 2, 6});
        faces.add(new int[]{3, 6, 8});
        faces.add(new int[]{3, 8, 9});
        faces.add(new int[]{4, 9, 5});
        faces.add(new int[]{2, 4, 11});
        faces.add(new int[]{6, 2, 10});
        faces.add(new int[]{8, 6, 7});
        faces.add(new int[]{9, 8, 1});
        return faces;
    }

    private void subdivide(List<Vector3> verts, List<int[]> faces) {
        Map<Long, Integer> cache = new HashMap<>();
        List<int[]> newFaces = new ArrayList<>();

        for (int[] face : faces) {
            int a = midpoint(face[0], face[1], verts, cache);
            int b = midpoint(face[1], face[2], verts, cache);
            int c = midpoint(face[2], face[0], verts, cache);
            newFaces.add(new int[]{face[0], a, c});
            newFaces.add(new int[]{face[1], b, a});
            newFaces.add(new int[]{face[2], c, b});
            newFaces.add(new int[]{a, b, c});
        }

        faces.clear();
        faces.addAll(newFaces);
    }

    private int midpoint(int i1, int i2, List<Vector3> verts, Map<Long, Integer> cache) {
        long key = (long) Math.min(i1, i2) * 1_000_000L + Math.max(i1, i2);
        if (cache.containsKey(key)) {
            return cache.get(key);
        }
        Vector3 mid = projection.midpointOnSphere(verts.get(i1), verts.get(i2));
        int idx = verts.size();
        verts.add(mid);
        cache.put(key, idx);
        return idx;
    }

    private List<Cell> buildDualCells(List<Vector3> verts, List<int[]> faces) {
        Map<Integer, List<Integer>> vertToFaces = new HashMap<>();
        for (int fi = 0; fi < faces.size(); fi++) {
            for (int vi : faces.get(fi)) {
                vertToFaces.computeIfAbsent(vi, k -> new ArrayList<>()).add(fi);
            }
        }

        List<Vector3> centroids = computeFaceCentroids(verts, faces);

        List<Cell> cells = new ArrayList<>();
        int cellId = 0;

        for (Map.Entry<Integer, List<Integer>> entry : vertToFaces.entrySet()) {
            int vi = entry.getKey();
            List<Integer> faceIndices = entry.getValue();

            if (faceIndices.size() < 3) {
                continue;
            }

            Vector3 center = verts.get(vi);
            List<Vector3> ring = new ArrayList<>();
            for (int fi : faceIndices) {
                ring.add(insetTowardsCenter(center, centroids.get(fi)));
            }

            sortAroundVertex(ring, center);

            CellType type = faceIndices.size() == 5 ? CellType.PENTAGON : CellType.HEXAGON;
            cells.add(new Cell(cellId++, type, center, ring));
        }

        return cells;
    }

    private List<Vector3> computeFaceCentroids(List<Vector3> verts, List<int[]> faces) {
        List<Vector3> centroids = new ArrayList<>();
        for (int[] face : faces) {
            centroids.add(projection.centroidOnSphere(
                verts.get(face[0]),
                verts.get(face[1]),
                verts.get(face[2])
            ));
        }
        return centroids;
    }

    private void sortAroundVertex(List<Vector3> ring, Vector3 center) {
        if (ring.size() < 3) {
            return;
        }
        Vector3 normal = center.normalize();
        Vector3 first = ring.get(0).add(center.scale(-1));
        Vector3 proj = first.add(normal.scale(-normal.dot(first)));
        double pLen = proj.length();
        if (pLen < 1e-9) {
            return;
        }
        Vector3 tangent = proj.scale(1.0 / pLen);
        Vector3 bitangent = normal.cross(tangent).normalize();

        ring.sort((a, b) -> {
            Vector3 localA = a.add(center.scale(-1));
            Vector3 localB = b.add(center.scale(-1));
            double angA = Math.atan2(localA.dot(bitangent), localA.dot(tangent));
            double angB = Math.atan2(localB.dot(bitangent), localB.dot(tangent));
            return Double.compare(angA, angB);
        });
    }

    private Vector3 insetTowardsCenter(Vector3 center, Vector3 vertex) {
        return projection.project(center.lerp(vertex, CELL_VERTEX_INSET));
    }

    private void connectNeighbors(List<Cell> cells, List<int[]> faces, List<Vector3> verts) {
        Map<Integer, Cell> vertToCell = buildVertToCellMap(cells, verts);

        for (int[] face : faces) {
            Cell c0 = vertToCell.get(face[0]);
            Cell c1 = vertToCell.get(face[1]);
            Cell c2 = vertToCell.get(face[2]);
            linkIfPresent(c0, c1);
            linkIfPresent(c1, c2);
            linkIfPresent(c0, c2);
        }
    }

    private Map<Integer, Cell> buildVertToCellMap(List<Cell> cells, List<Vector3> verts) {
        Map<Integer, Cell> map = new HashMap<>();
        for (Cell cell : cells) {
            Vector3 center = cell.getCenter();
            for (int i = 0; i < verts.size(); i++) {
                Vector3 v = verts.get(i);
                if (samePoint(v, center)) {
                    map.put(i, cell);
                    break;
                }
            }
        }
        return map;
    }

    private boolean samePoint(Vector3 a, Vector3 b) {
        return Math.abs(a.x - b.x) < 1e-6
            && Math.abs(a.y - b.y) < 1e-6
            && Math.abs(a.z - b.z) < 1e-6;
    }

    private void linkIfPresent(Cell a, Cell b) {
        if (a != null && b != null) {
            a.addNeighbor(b);
            b.addNeighbor(a);
        }
    }

    public GoldbergFormula getFormula() {
        return formula;
    }
}
