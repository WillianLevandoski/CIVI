package com.hexglobe.render;

import com.hexglobe.math.Vector3;
import com.hexglobe.model.Cell;
import com.hexglobe.service.SelectionService;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Cylinder;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

import java.util.List;

public class CellNodeFactory {

    private static final Color FILL_DEFAULT    = Color.color(0.04, 0.04, 0.07, 0.95);
    private static final Color FILL_SELECTED   = Color.color(0.00, 0.25, 0.30, 0.95);
    private static final Color EDGE_DEFAULT    = Color.WHITE;
    private static final Color EDGE_SELECTED   = Color.CYAN;
    private static final double EDGE_RADIUS    = 0.35;

    private final SelectionService selectionService;

    public CellNodeFactory(SelectionService selectionService) {
        this.selectionService = selectionService;
    }

    public Group createCellNode(Cell cell) {
        Group group = new Group();

        MeshView face = buildFaceMesh(cell);
        face.setOnMouseClicked(e -> {
            selectionService.select(cell);
            e.consume();
        });

        Group edges = buildEdgeRing(cell, EDGE_DEFAULT);

        group.getChildren().addAll(face, edges);
        group.setUserData(cell);

        selectionService.addSelectionListener(selected -> {
            boolean active = selected != null && selected.getId() == cell.getId();
            refreshAppearance(face, edges, active);
        });

        return group;
    }

    private MeshView buildFaceMesh(Cell cell) {
        List<Vector3> verts = cell.getVertices();
        Vector3 center = cell.getCenter();
        int n = verts.size();

        TriangleMesh mesh = new TriangleMesh();

        // Vertex 0 = center; vertices 1..n = polygon ring
        mesh.getPoints().addAll((float) center.x, (float) center.y, (float) center.z);
        for (Vector3 v : verts) {
            mesh.getPoints().addAll((float) v.x, (float) v.y, (float) v.z);
        }

        // Minimal UV set (unused visually but required by JavaFX)
        mesh.getTexCoords().addAll(0.5f, 0.5f, 1.0f, 0.5f, 0.5f, 1.0f);

        for (int i = 0; i < n; i++) {
            int b = i + 1;
            int c = (i + 1) % n + 1;
            mesh.getFaces().addAll(0, 0, b, 1, c, 2);
        }

        MeshView view = new MeshView(mesh);
        view.setMaterial(buildMaterial(FILL_DEFAULT));
        return view;
    }

    private Group buildEdgeRing(Cell cell, Color color) {
        Group edgeGroup = new Group();
        List<Vector3> verts = cell.getVertices();
        int n = verts.size();

        for (int i = 0; i < n; i++) {
            Vector3 a = verts.get(i);
            Vector3 b = verts.get((i + 1) % n);
            edgeGroup.getChildren().add(buildEdgeCylinder(a, b, color));
        }

        return edgeGroup;
    }

    private Cylinder buildEdgeCylinder(Vector3 a, Vector3 b, Color color) {
        double dx = b.x - a.x;
        double dy = b.y - a.y;
        double dz = b.z - a.z;
        double length = Math.sqrt(dx * dx + dy * dy + dz * dz);

        if (length < 1e-9) {
            return new Cylinder(EDGE_RADIUS, EDGE_RADIUS);
        }

        Cylinder cyl = new Cylinder(EDGE_RADIUS, length);
        cyl.setMaterial(buildMaterial(color));

        double mx = (a.x + b.x) / 2.0;
        double my = (a.y + b.y) / 2.0;
        double mz = (a.z + b.z) / 2.0;
        cyl.getTransforms().add(new Translate(mx, my, mz));

        Vector3 dir = new Vector3(dx / length, dy / length, dz / length);
        Vector3 yAxis = new Vector3(0, 1, 0);
        Vector3 cross = yAxis.cross(dir);
        double crossLen = cross.length();

        if (crossLen > 1e-6) {
            double cosA = Math.max(-1.0, Math.min(1.0, yAxis.dot(dir)));
            double angle = Math.toDegrees(Math.acos(cosA));
            cyl.getTransforms().add(new Rotate(angle, cross.x, cross.y, cross.z));
        } else if (dir.y < 0) {
            cyl.getTransforms().add(new Rotate(180, 1, 0, 0));
        }

        return cyl;
    }

    private PhongMaterial buildMaterial(Color color) {
        PhongMaterial mat = new PhongMaterial();
        mat.setDiffuseColor(color);
        mat.setSpecularColor(Color.color(1, 1, 1, 0.08));
        mat.setSpecularPower(30);
        return mat;
    }

    private void refreshAppearance(MeshView face, Group edges, boolean selected) {
        face.setMaterial(buildMaterial(selected ? FILL_SELECTED : FILL_DEFAULT));
        Color edgeColor = selected ? EDGE_SELECTED : EDGE_DEFAULT;
        for (Node node : edges.getChildren()) {
            if (node instanceof Cylinder cyl) {
                cyl.setMaterial(buildMaterial(edgeColor));
            }
        }
    }
}
