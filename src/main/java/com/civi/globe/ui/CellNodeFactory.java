package com.civi.globe.ui;

import com.civi.globe.core.Cell;
import com.civi.globe.core.CellType;
import com.civi.globe.math.Vector3;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public final class CellNodeFactory {

    private static final double RADIUS = 220.0d;
    private static final PhongMaterial PENTAGON_MATERIAL = new PhongMaterial(Color.web("#f4b942"));
    private static final PhongMaterial HEXAGON_MATERIAL = new PhongMaterial(Color.web("#4fc3f7"));
    private static final PhongMaterial SELECTED_MATERIAL = new PhongMaterial(Color.web("#ef5350"));

    public Node create(Cell cell) {
        TriangleMesh mesh = new TriangleMesh();
        addPoint(mesh, cell.center());
        for (Vector3 vertex : cell.vertices()) {
            addPoint(mesh, vertex);
        }
        mesh.getTexCoords().addAll(0.0f, 0.0f);
        for (int index = 1; index < cell.vertices().size(); index++) {
            mesh.getFaces().addAll(0, 0, index, 0, index + 1, 0);
        }
        mesh.getFaces().addAll(0, 0, cell.vertices().size(), 0, 1, 0);

        MeshView view = new MeshView(mesh);
        view.setCullFace(CullFace.BACK);
        view.setDrawMode(DrawMode.FILL);
        view.setMaterial(defaultMaterial(cell.type()));
        view.setUserData(cell);
        return view;
    }

    public void applySelected(Node node, CellType type, boolean selected) {
        if (node instanceof MeshView meshView) {
            meshView.setMaterial(selected ? SELECTED_MATERIAL : defaultMaterial(type));
        }
    }

    private PhongMaterial defaultMaterial(CellType type) {
        if (type == CellType.PENTAGON) {
            return PENTAGON_MATERIAL;
        }
        return HEXAGON_MATERIAL;
    }

    private void addPoint(TriangleMesh mesh, Vector3 vector) {
        mesh.getPoints().addAll(
                (float) (vector.x() * RADIUS),
                (float) (vector.y() * RADIUS),
                (float) (vector.z() * RADIUS)
        );
    }
}
