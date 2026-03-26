package com.civi.globe;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HexSphereBuilder {
    private static final double DEG = Math.PI / 180.0;
    private static final double PI2 = Math.PI * 2.0;

    final PointTable points = new PointTable();
    final List<HexCell> cells = new ArrayList<>();
    private final TerrainGenerator terrainGenerator = new TerrainGenerator(TerrainDistributionConfig.defaultConfig());

    void build(int n, double r) {
        cells.clear();
        points.clear();

        double c = Math.cos(60.0 * DEG);
        double s = Math.sin(60.0 * DEG);
        double sy = r / (n + n - 2);
        double sz = sy / s;
        double sx = sz * c;
        double sz2 = 0.5 * sz;

        int na = 5 * (n - 2);
        int nb = n;
        int b0 = n;

        HexCell h = new HexCell();

        for (int b = 1; b < n - 1; b++) {
            for (int a = 1; a < b; a++) {
                double px = ((double) a) * (sx + sz);
                double py = ((double) (b - (a >> 1))) * (sy * 2.0);
                double pz = 0.0;
                if ((a & 1) != 0) py -= sy;
                h.ix[0] = points.add(px + sz2 + sx, py, pz);
                h.ix[1] = points.add(px + sz2, py + sy, pz);
                h.ix[2] = points.add(px - sz2, py + sy, pz);
                h.ix[3] = points.add(px - sz2 - sx, py, pz);
                h.ix[4] = points.add(px - sz2, py - sy, pz);
                h.ix[5] = points.add(px + sz2, py - sy, pz);
                h.a = a;
                h.b = n - 1 - b;
                cells.add(copyCell(h));
            }
        }
        int firstTriangleCount = cells.size();

        for (int i = 0; i < points.size(); i++) {
            Vec3 q = points.get(i);
            double ang = Math.atan2(q.y, q.x);
            double len = len(q.x, q.y, q.z);
            ang -= 60.0 * DEG;
            while (ang > +60.0 * DEG) ang -= PI2;
            while (ang < -60.0 * DEG) ang += PI2;
            len *= Math.cos(ang) / Math.cos(30.0 * DEG);
            ang *= 72.0 / 60.0;
            q.x = len * Math.cos(ang);
            q.y = len * Math.sin(ang);
        }

        for (int sector = 1; sector < 5; sector++) {
            double ang = sector * 72.0 * DEG;
            for (int i = 0; i < firstTriangleCount; i++) {
                HexCell ph = cells.get(i);
                for (int j = 0; j < 6; j++) {
                    Vec3 p = points.get(ph.ix[j]).copy();
                    rotate2d(-ang, p);
                    h.ix[j] = points.add(p.x, p.y, p.z);
                }
                h.a = ph.a + (sector * (n - 2));
                h.b = ph.b;
                cells.add(copyCell(h));
            }
        }
        for (int i = 0; i < points.size(); i++) {
            Vec3 q = points.get(i);
            double ang = len(q.x, q.y, q.z) * 0.5 * Math.PI / r;
            q.z = r * Math.cos(ang);
            double ll = Math.abs(r * Math.sin(ang) / Math.sqrt((q.x * q.x) + (q.y * q.y)));
            q.x *= ll;
            q.y *= ll;
        }

        int half = cells.size();
        for (int i = 0; i < half; i++) {
            HexCell ph = cells.get(i);
            for (int j = 0; j < 6; j++) {
                Vec3 p = points.get(ph.ix[j]).copy();
                p.z = -p.z;
                h.ix[j] = points.add(p.x, p.y, p.z);
            }
            h.a = ph.a;
            h.b = -ph.b;
            h.predefinedColor = ph.predefinedColor;
            cells.add(copyCell(h));
        }

        int[][] ab = new int[na][nb + nb + 1];
        for (int a = 0; a < na; a++) {
            for (int b = -nb; b <= nb; b++) ab[a][b0 + b] = -1;
        }
        for (int i = 0; i < cells.size(); i++) {
            HexCell ph = cells.get(i);
            ab[ph.a][b0 + ph.b] = i;
        }

        for (int a = 0; a < na; a++) {
            h.a = a;
            h.b = 0;
            int a0 = a;
            int a1 = (a + 1) % na;
            int i0 = ab[a0][b0 + 1];
            int i1 = ab[a1][b0 + 1];
            int j0 = ab[a0][b0 - 1];
            int j1 = ab[a1][b0 - 1];
            if ((i0 >= 0) && (i1 >= 0) && (j0 >= 0) && (j1 >= 0)) {
                h.ix[0] = cells.get(i1).ix[1];
                h.ix[1] = cells.get(i0).ix[0];
                h.ix[2] = cells.get(i0).ix[1];
                h.ix[3] = cells.get(j0).ix[1];
                h.ix[4] = cells.get(j0).ix[0];
                h.ix[5] = cells.get(j1).ix[1];
                cells.add(copyCell(h));
                ab[h.a][b0] = cells.size() - 1;
            }
        }

        for (int a = 0; a < na; a += n - 2) {
            for (int b = 1; b < n - 3; b++) {
                for (int sign : new int[]{1, -1}) {
                    int bb = sign * b;
                    h.a = a;
                    h.b = bb;
                    int a0 = a - b;
                    if (a0 < 0) a0 += na;
                    int i0 = ab[a0][b0 + bb];
                    a0--;
                    if (a0 < 0) a0 += na;
                    int i1 = ab[a0][b0 + bb + sign];
                    int a1 = a + 1;
                    if (a1 >= na) a1 -= na;
                    int j0 = ab[a1][b0 + bb];
                    int j1 = ab[a1][b0 + bb + sign];
                    if ((i0 >= 0) && (i1 >= 0) && (j0 >= 0) && (j1 >= 0)) {
                        h.ix[0] = cells.get(i0).ix[5];
                        h.ix[1] = cells.get(i0).ix[4];
                        h.ix[2] = cells.get(i1).ix[5];
                        h.ix[3] = cells.get(j1).ix[3];
                        h.ix[4] = cells.get(j0).ix[4];
                        h.ix[5] = cells.get(j0).ix[3];
                        cells.add(copyCell(h));
                    }
                }
            }
        }

        HexCell h0 = new HexCell();
        HexCell h1 = new HexCell();
        h0.a = 0;
        h0.b = n - 1;
        h1.a = h0.a;
        h1.b = -h0.b;
        double pz = Math.sqrt((r * r) - (sz * sz));
        for (double ang = 0.0, a = 0; a < 5; a++, ang += 72.0 * DEG) {
            double px = 2.0 * sz * Math.cos(ang);
            double py = 2.0 * sz * Math.sin(ang);
            h0.ix[(int) a] = points.add(px, py, +pz);
            h1.ix[(int) a] = points.add(px, py, -pz);
        }
        h0.ix[5] = h0.ix[4];
        h1.ix[5] = h1.ix[4];
        cells.add(h0);
        cells.add(h1);

        int[] ii = new int[5];
        HexCell[] poles = new HexCell[]{h0, h1};
        int hb = n - 2;
        for (int i = 0; i < 2; i++, hb = -hb) {
            HexCell pole = poles[i];
            int b = (i == 0) ? n - 3 : -(n - 3);
            h.b = hb;
            int a = 1;
            for (int k = 0; k < 5; k++, a += n - 2) {
                int aa = a % na;
                ii[k] = ab[aa][b0 + b];
            }
            for (int j = 0; j < 5; j++) {
                h.a = ((4 + j) % 5) * (n - 2) + 1;
                h.ix[0] = pole.ix[(5 - j) % 5];
                h.ix[1] = pole.ix[(6 - j) % 5];
                h.ix[2] = cells.get(ii[(j + 4) % 5]).ix[4];
                h.ix[3] = cells.get(ii[(j + 4) % 5]).ix[5];
                h.ix[4] = cells.get(ii[j]).ix[3];
                h.ix[5] = cells.get(ii[j]).ix[4];
                cells.add(copyCell(h));
            }
        }

        for (int sectorBase = 0; sectorBase < na; sectorBase += (n - 2)) {
            double ang = 36.0 * DEG - ((sectorBase / (n - 2)) * 72.0 * DEG);

            double px = r * Math.cos(ang);
            double py = r * Math.sin(ang);
            double z = sz;

            int i0 = points.add(px, py, +z);
            int i1 = points.add(px, py, -z);

            int left = sectorBase - 1;
            if (left < 0) left += na;

            int right = sectorBase + 1;
            if (right >= na) right -= na;

            ii[0] = ab[left][b0 - 1];
            ii[1] = ab[left][b0 + 1];
            ii[2] = ab[right][b0 - 1];
            ii[3] = ab[right][b0 + 1];

            if (ii[0] < 0 || ii[1] < 0 || ii[2] < 0 || ii[3] < 0) {
                continue;
            }

            h.a = sectorBase;
            h.b = 0;
            h.ix[0] = cells.get(ii[0]).ix[0];
            h.ix[1] = cells.get(ii[0]).ix[1];
            h.ix[2] = cells.get(ii[1]).ix[1];
            h.ix[3] = cells.get(ii[1]).ix[0];
            h.ix[4] = i0;
            h.ix[5] = i1;
            cells.add(copyCell(h));

            h.ix[0] = cells.get(ii[2]).ix[2];
            h.ix[1] = cells.get(ii[2]).ix[1];
            h.ix[2] = cells.get(ii[3]).ix[1];
            h.ix[3] = cells.get(ii[3]).ix[2];
            h.ix[4] = i0;
            h.ix[5] = i1;
            cells.add(copyCell(h));

            h.ix[0] = cells.get(ii[0]).ix[0];
            h.ix[1] = cells.get(ii[0]).ix[5];
            h.ix[2] = cells.get(ii[2]).ix[3];
            h.ix[3] = cells.get(ii[2]).ix[2];
            h.ix[4] = i1;
            h.ix[5] = i1;
            cells.add(copyCell(h));

            h.ix[0] = cells.get(ii[1]).ix[0];
            h.ix[1] = cells.get(ii[1]).ix[5];
            h.ix[2] = cells.get(ii[3]).ix[3];
            h.ix[3] = cells.get(ii[3]).ix[2];
            h.ix[4] = i0;
            h.ix[5] = i0;
            cells.add(copyCell(h));
        }

        enforceHexOnlyCells();
        assignIdsAndNeighbors();
        applyInitialTerrainDistribution();
    }

    void applyInitialTerrainDistribution() {
        terrainGenerator.applyInitialTerrainDistribution(cells, points);
    }

    private void enforceHexOnlyCells() {
        for (HexCell cell : cells) {
            int guard = 0;
            while (uniqueVertexIndexes(cell).size() < 6 && guard < 6) {
                boolean changed = false;
                for (int i = 0; i < 6; i++) {
                    int next = (i + 1) % 6;
                    if (cell.ix[i] == cell.ix[next]) {
                        int after = (i + 2) % 6;
                        Vec3 a = points.get(cell.ix[next]);
                        Vec3 b = points.get(cell.ix[after]);
                        int midpoint = points.add(
                                (a.x + b.x) * 0.5,
                                (a.y + b.y) * 0.5,
                                (a.z + b.z) * 0.5
                        );
                        cell.ix[next] = midpoint;
                        changed = true;
                        break;
                    }
                }
                if (!changed) {
                    break;
                }
                guard++;
            }
        }
    }
    
    private static HexCell copyCell(HexCell src) {
        HexCell dst = new HexCell();
        dst.id = src.id;
        dst.a = src.a;
        dst.b = src.b;
        dst.predefinedColor = src.predefinedColor;
        System.arraycopy(src.ix, 0, dst.ix, 0, src.ix.length);
        return dst;
    }

    private void assignIdsAndNeighbors() {
        for (int i = 0; i < cells.size(); i++) {
            HexCell cell = cells.get(i);
            cell.id = i;
            cell.neighbors.clear();
        }

        for (int i = 0; i < cells.size(); i++) {
            HexCell current = cells.get(i);
            Set<Integer> currentVertices = uniqueVertexIndexes(current);
            for (int j = i + 1; j < cells.size(); j++) {
                HexCell other = cells.get(j);
                int shared = 0;
                for (int idx : uniqueVertexIndexes(other)) {
                    if (currentVertices.contains(idx)) {
                        shared++;
                    }
                }
                if (shared >= 2) {
                    current.neighbors.add(other.id);
                    other.neighbors.add(current.id);
                }
            }
        }
    }

    private static Set<Integer> uniqueVertexIndexes(HexCell cell) {
        Set<Integer> vertices = new HashSet<>();
        for (int ix : cell.ix) {
            vertices.add(ix);
        }
        return vertices;
    }

    private static void rotate2d(double ang, Vec3 p) {
        double x = (p.x * Math.cos(ang)) - (p.y * Math.sin(ang));
        double y = (p.x * Math.sin(ang)) + (p.y * Math.cos(ang));
        p.x = x;
        p.y = y;
    }

    private static double len(double x, double y, double z) {
        return Math.sqrt((x * x) + (y * y) + (z * z));
    }

    List<SphericalTriangle> subdivideHexIntoEquilateralTriangles(HexCell hex, int n, Vec3 sphereCenter, double sphereRadius) {
        List<SphericalTriangle> triangles = new ArrayList<>();
        if (n < 1) {
            return triangles;
        }

        Vec3[] hexVertices = new Vec3[6];
        for (int i = 0; i < 6; i++) {
            hexVertices[i] = points.get(hex.ix[i]);
        }

        Vec3 center = new Vec3(0.0, 0.0, 0.0);
        for (Vec3 vertex : hexVertices) {
            center.x += vertex.x;
            center.y += vertex.y;
            center.z += vertex.z;
        }
        center.x /= 6.0;
        center.y /= 6.0;
        center.z /= 6.0;

        Vec3 edge01 = subtract(hexVertices[1], hexVertices[0]);
        Vec3 edge02 = subtract(hexVertices[2], hexVertices[0]);
        Vec3 u = normalize(edge01);
        Vec3 normal = normalize(cross(edge01, edge02));
        Vec3 v = normalize(cross(normal, u));
        if (length(u) == 0.0 || length(v) == 0.0) {
            return triangles;
        }

        double[] localX = new double[6];
        double[] localY = new double[6];
        for (int i = 0; i < 6; i++) {
            Vec3 rel = subtract(hexVertices[i], center);
            localX[i] = dot(rel, u);
            localY[i] = dot(rel, v);
        }

        double l = 0.0;
        int edgeCount = 0;
        for (int i = 0; i < 6; i++) {
            int next = (i + 1) % 6;
            double dx = localX[next] - localX[i];
            double dy = localY[next] - localY[i];
            double edgeLen = Math.sqrt((dx * dx) + (dy * dy));
            if (edgeLen > 1.0e-9) {
                l += edgeLen;
                edgeCount++;
            }
        }
        if (edgeCount == 0) {
            return triangles;
        }
        l /= edgeCount;
        double s = l / n;

        double basisAx = s;
        double basisAy = 0.0;
        double basisBx = s * 0.5;
        double basisBy = s * Math.sqrt(3.0) * 0.5;
        double det = (basisAx * basisBy) - (basisAy * basisBx);
        if (Math.abs(det) < 1.0e-12) {
            return triangles;
        }

        double minA = Double.POSITIVE_INFINITY;
        double maxA = Double.NEGATIVE_INFINITY;
        double minB = Double.POSITIVE_INFINITY;
        double maxB = Double.NEGATIVE_INFINITY;
        for (int i = 0; i < 6; i++) {
            double[] ab = toLattice(localX[i], localY[i], basisAx, basisAy, basisBx, basisBy, det);
            minA = Math.min(minA, ab[0]);
            maxA = Math.max(maxA, ab[0]);
            minB = Math.min(minB, ab[1]);
            maxB = Math.max(maxB, ab[1]);
        }

        int aStart = (int) Math.floor(minA) - 2;
        int aEnd = (int) Math.ceil(maxA) + 2;
        int bStart = (int) Math.floor(minB) - 2;
        int bEnd = (int) Math.ceil(maxB) + 2;
        Vec3[][] lattice = new Vec3[aEnd - aStart + 1][bEnd - bStart + 1];
        boolean[][] inside = new boolean[aEnd - aStart + 1][bEnd - bStart + 1];
        double eps = s * 1.0e-3;

        for (int ai = aStart; ai <= aEnd; ai++) {
            for (int bi = bStart; bi <= bEnd; bi++) {
                double px = (ai * basisAx) + (bi * basisBx);
                double py = (ai * basisAy) + (bi * basisBy);
                if (!pointInPolygon(px, py, localX, localY, eps)) {
                    continue;
                }
                Vec3 point = add(add(center, scale(u, px)), scale(v, py));
                lattice[ai - aStart][bi - bStart] = projectToSphere(point, sphereCenter, sphereRadius);
                inside[ai - aStart][bi - bStart] = true;
            }
        }

        for (int ai = aStart; ai < aEnd; ai++) {
            for (int bi = bStart; bi < bEnd; bi++) {
                int ia = ai - aStart;
                int ib = bi - bStart;
                if (inside[ia][ib] && inside[ia + 1][ib] && inside[ia][ib + 1]) {
                    triangles.add(new SphericalTriangle(lattice[ia][ib], lattice[ia + 1][ib], lattice[ia][ib + 1]));
                }
                if (inside[ia + 1][ib + 1] && inside[ia + 1][ib] && inside[ia][ib + 1]) {
                    triangles.add(new SphericalTriangle(lattice[ia + 1][ib + 1], lattice[ia + 1][ib], lattice[ia][ib + 1]));
                }
            }
        }
        return triangles;
    }

    record SphericalTriangle(Vec3 a, Vec3 b, Vec3 c) {}

    private static Vec3 projectToSphere(Vec3 point, Vec3 sphereCenter, double sphereRadius) {
        Vec3 delta = subtract(point, sphereCenter);
        Vec3 direction = normalize(delta);
        return add(sphereCenter, scale(direction, sphereRadius));
    }

    private static boolean pointInPolygon(double x, double y, double[] px, double[] py, double epsilon) {
        boolean inside = false;
        for (int i = 0, j = px.length - 1; i < px.length; j = i++) {
            double xi = px[i];
            double yi = py[i];
            double xj = px[j];
            double yj = py[j];
            double edgeDx = xj - xi;
            double edgeDy = yj - yi;
            double cross = ((x - xi) * edgeDy) - ((y - yi) * edgeDx);
            if (Math.abs(cross) <= epsilon) {
                double dot = ((x - xi) * (x - xj)) + ((y - yi) * (y - yj));
                if (dot <= epsilon) {
                    return true;
                }
            }
            boolean intersects = ((yi > y) != (yj > y)) && (x < ((edgeDx * (y - yi)) / (yj - yi) + xi));
            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
    }

    private static double[] toLattice(double x, double y, double ax, double ay, double bx, double by, double det) {
        double a = ((x * by) - (y * bx)) / det;
        double b = ((ax * y) - (ay * x)) / det;
        return new double[]{a, b};
    }

    private static Vec3 add(Vec3 a, Vec3 b) {
        return new Vec3(a.x + b.x, a.y + b.y, a.z + b.z);
    }

    private static Vec3 subtract(Vec3 a, Vec3 b) {
        return new Vec3(a.x - b.x, a.y - b.y, a.z - b.z);
    }

    private static Vec3 scale(Vec3 v, double factor) {
        return new Vec3(v.x * factor, v.y * factor, v.z * factor);
    }

    private static double dot(Vec3 a, Vec3 b) {
        return (a.x * b.x) + (a.y * b.y) + (a.z * b.z);
    }

    private static Vec3 cross(Vec3 a, Vec3 b) {
        return new Vec3((a.y * b.z) - (a.z * b.y), (a.z * b.x) - (a.x * b.z), (a.x * b.y) - (a.y * b.x));
    }

    private static Vec3 normalize(Vec3 v) {
        double len = length(v);
        if (len == 0.0) {
            return new Vec3(0.0, 0.0, 0.0);
        }
        return new Vec3(v.x / len, v.y / len, v.z / len);
    }

    private static double length(Vec3 v) {
        return Math.sqrt((v.x * v.x) + (v.y * v.y) + (v.z * v.z));
    }
}
