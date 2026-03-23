package com.civi.globe.ui;

import com.civi.globe.domain.Cell;
import com.civi.globe.math.Vector3;
import javafx.geometry.Point3D;
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

import java.util.List;

public final class CellNodeFactory {

    public static final double GLOBE_RADIUS = 280.0d;

    private static final double EDGE_RADIUS = 0.75d;
    private static final PhongMaterial FILL_MATERIAL = material(Color.rgb(8, 8, 8, 0.94d), Color.rgb(24, 24, 24));
    private static final PhongMaterial EDGE_MATERIAL = material(Color.WHITE, Color.WHITE);
    private static final PhongMaterial SELECTED_FILL = material(Color.rgb(14, 36, 36, 0.96d), Color.rgb(112, 255, 255));
    private static final PhongMaterial SELECTED_EDGE = material(Color.CYAN, Color.WHITE);

    public Node createCellNode(Cell cell) {
        MeshView fill = createFillMesh(cell);
        Group edges = createEdgeGroup(cell.vertices());
        Group node = new Group(fill, edges);
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
        if (!(group.getChildren().get(1) instanceof Group edges)) {
            return;
        }
        fill.setMaterial(selected ? SELECTED_FILL : FILL_MATERIAL);
        edges.getChildren().forEach(edge -> {
            if (edge instanceof Cylinder cylinder) {
                cylinder.setMaterial(selected ? SELECTED_EDGE : EDGE_MATERIAL);
            }
        });
    }

    private MeshView createFillMesh(Cell cell) {
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

        MeshView fill = new MeshView(mesh);
        fill.setDrawMode(DrawMode.FILL);
        fill.setCullFace(CullFace.NONE);
        fill.setMaterial(FILL_MATERIAL);
        fill.setUserData(cell);
        return fill;
    }

    private Group createEdgeGroup(List<Vector3> vertices) {
        Group group = new Group();
        for (int index = 0; index < vertices.size(); index++) {
            Vector3 start = vertices.get(index);
            Vector3 end = vertices.get((index + 1) % vertices.size());
            group.getChildren().add(createEdge(start, end));
        }
        group.setMouseTransparent(true);
        return group;
    }

    private Cylinder createEdge(Vector3 start, Vector3 end) {
        Point3D startPoint = toPoint(start);
        Point3D endPoint = toPoint(end);
        Point3D delta = endPoint.subtract(startPoint);
        double length = delta.magnitude();

        Cylinder edge = new Cylinder(EDGE_RADIUS, length);
        edge.setMaterial(EDGE_MATERIAL);
        edge.setMouseTransparent(true);

        Point3D midpoint = startPoint.midpoint(endPoint);
        Point3D axis = new Point3D(0.0d, 1.0d, 0.0d).crossProduct(delta);
        double angle = Math.toDegrees(Math.acos(clamp(delta.normalize().dotProduct(0.0d, 1.0d, 0.0d))));

        edge.getTransforms().add(new Translate(midpoint.getX(), midpoint.getY(), midpoint.getZ()));
        if (axis.magnitude() > 0.0001d) {
            edge.getTransforms().add(new Rotate(angle, axis));
        } else if (delta.getY() < 0.0d) {
            edge.getTransforms().add(new Rotate(180.0d, Rotate.X_AXIS));
        }
        return edge;
    }

    private Point3D toPoint(Vector3 point) {
        return new Point3D(point.x() * GLOBE_RADIUS, point.y() * GLOBE_RADIUS, point.z() * GLOBE_RADIUS);
    }

    private void addPoint(TriangleMesh mesh, Vector3 point) {
        mesh.getPoints().addAll((float) (point.x() * GLOBE_RADIUS), (float) (point.y() * GLOBE_RADIUS), (float) (point.z() * GLOBE_RADIUS));
    }

    private double clamp(double value) {
        if (value < -1.0d) {
            return -1.0d;
        }
        if (value > 1.0d) {
            return 1.0d;
        }
        return value;
    }

    private static PhongMaterial material(Color diffuse, Color specular) {
        PhongMaterial material = new PhongMaterial(diffuse);
        material.setSpecularColor(specular);
        return material;
    }
}
