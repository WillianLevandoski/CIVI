package com.civi.globe.ui;

import com.civi.globe.core.Cell;
import com.civi.globe.core.CellType;
import com.civi.globe.math.Vector3;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.CullFace;
import javafx.scene.shape.DrawMode;
import javafx.scene.shape.MeshView;
import javafx.scene.shape.TriangleMesh;

public final class CellNodeFactory {

    public static final double RADIUS = 220.0d;

    private static final PhongMaterial BLACK_MATERIAL = material(Color.BLACK, Color.WHITE);
    private static final PhongMaterial WHITE_MATERIAL = material(Color.WHITE, Color.WHITE);

    public Node create(Cell cell) {
        TriangleMesh mesh = createMesh(cell);

        MeshView fillView = new MeshView(mesh);
        fillView.setCullFace(CullFace.NONE);
        fillView.setDrawMode(DrawMode.FILL);
        fillView.setMaterial(BLACK_MATERIAL);
        fillView.setUserData(cell);

        MeshView lineView = new MeshView(mesh);
        lineView.setCullFace(CullFace.NONE);
        lineView.setDrawMode(DrawMode.LINE);
        lineView.setMaterial(WHITE_MATERIAL);
        lineView.setMouseTransparent(true);

        Group group = new Group(fillView, lineView);
        group.setUserData(cell);
        return group;
    }

    public void applySelected(Node node, boolean selected) {
        if (!(node instanceof Group group)) {
            return;
        }
        if (group.getChildren().isEmpty() || !(group.getChildren().get(0) instanceof MeshView fillView)) {
            return;
        }
        fillView.setMaterial(selected ? WHITE_MATERIAL : BLACK_MATERIAL);
    }

    private TriangleMesh createMesh(Cell cell) {
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
        return mesh;
    }

    private void addPoint(TriangleMesh mesh, Vector3 vector) {
        mesh.getPoints().addAll(
                (float) (vector.x() * RADIUS),
                (float) (vector.y() * RADIUS),
                (float) (vector.z() * RADIUS)
        );
    }

    private static PhongMaterial material(Color diffuse, Color specular) {
        PhongMaterial material = new PhongMaterial(diffuse);
        material.setSpecularColor(specular);
        return material;
    }
}
