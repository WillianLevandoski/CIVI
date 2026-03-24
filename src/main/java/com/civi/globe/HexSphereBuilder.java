package com.civi.globe;

import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

final class HexSphereBuilder {
    private static final double DEG = Math.PI / 180.0;
    private static final double PI2 = Math.PI * 2.0;

    final PointTable points = new PointTable();
    final List<HexCell> cells = new ArrayList<>();

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
        h.color = Color.rgb(0, 64, 128);

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

        h.color = Color.rgb(0, 64, 64);
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
            h.color = ph.color;
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

        h.color = Color.rgb(0, 128, 64);
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

        h.color = Color.rgb(64, 128, 0);
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
        h0.color = Color.rgb(128, 0, 0);
        h0.a = 0;
        h0.b = n - 1;
        h1.color = h0.color;
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

        h.color = Color.rgb(96, 0, 96);
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

            int left = sectorBase - (n - 2);
            if (left < 0) left += na;

            int right = sectorBase + (n - 2);
            if (right >= na) right -= na;

            ii[0] = ab[left][b0 - 1];
            ii[1] = ab[left][b0 + 1];
            ii[2] = ab[right][b0 - 1];
            ii[3] = ab[right][b0 + 1];

            if (ii[0] < 0 || ii[1] < 0 || ii[2] < 0 || ii[3] < 0) {
                continue;
            }

            h.color = Color.rgb(128, 128, 0);
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

            h.color = Color.rgb(160, 64, 0);
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

        assignIdsAndNeighbors();
    }
    
    private static HexCell copyCell(HexCell src) {
        HexCell dst = new HexCell();
        dst.id = src.id;
        dst.a = src.a;
        dst.b = src.b;
        dst.color = src.color;
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
}
