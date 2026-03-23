package com.civi.globe.ui;

import com.civi.globe.domain.Cell;
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

    public static final double GLOBE_RADIUS = 280.0d;

    private static final PhongMaterial FILL_MATERIAL = material(Color.rgb(8, 8, 8, 0.95d), Color.rgb(40, 40, 40));
    private static final PhongMaterial LINE_MATERIAL = material(Color.WHITE, Color.WHITE);
    private static final PhongMaterial SELECTED_FILL = material(Color.rgb(20, 60, 60, 0.95d), Color.rgb(140, 255, 255));
    private static final PhongMaterial SELECTED_LINE = material(Color.CYAN, Color.WHITE);

    public Node createCellNode(Cell cell) {
        TriangleMesh mesh = createMesh(cell);

        MeshView fill = new MeshView(mesh);
        fill.setDrawMode(DrawMode.FILL);
        fill.setCullFace(CullFace.NONE);
        fill.setMaterial(FILL_MATERIAL);
        fill.setUserData(cell);

        MeshView lines = new MeshView(mesh);
        lines.setDrawMode(DrawMode.LINE);
        lines.setCullFace(CullFace.NONE);
        lines.setMaterial(LINE_MATERIAL);
        lines.setMouseTransparent(true);

        Group node = new Group(fill, lines);
        node.setUserData(cell);
        return node;
    }

    public void applySelection(Node node, boolean selected) {
        if (!(node instanceof Group group)) {
            return;
        }
        if (!(group.getChildren().get(0) instanceof MeshView fill)) {
            return;
        }
        if (!(group.getChildren().get(1) instanceof MeshView lines)) {
            return;
        }
        fill.setMaterial(selected ? SELECTED_FILL : FILL_MATERIAL);
        lines.setMaterial(selected ? SELECTED_LINE : LINE_MATERIAL);
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

    private void addPoint(TriangleMesh mesh, Vector3 point) {
        mesh.getPoints().addAll(
                (float) (point.x() * GLOBE_RADIUS),
                (float) (point.y() * GLOBE_RADIUS),
                (float) (point.z() * GLOBE_RADIUS)
        );
    }

    private static PhongMaterial material(Color diffuse, Color specular) {
        PhongMaterial material = new PhongMaterial(diffuse);
        material.setSpecularColor(specular);
        return material;
    }
}
