package com.civi.globe.ui;

import com.civi.globe.domain.Cell;
import com.civi.globe.domain.GlobeMesh;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GlobeRenderer {

    private final CellNodeFactory cellNodeFactory;

    public GlobeRenderer(CellNodeFactory cellNodeFactory) {
        this.cellNodeFactory = cellNodeFactory;
    }

    public RenderedGlobe render(GlobeMesh mesh) {
        Group root = new Group();
        root.getChildren().add(new AmbientLight(Color.WHITE));
        root.getChildren().add(createCoreSphere());

        Map<String, Node> cellNodes = new LinkedHashMap<>();
        for (Cell cell : mesh.cells()) {
            Node node = cellNodeFactory.createCellNode(cell);
            cellNodes.put(cell.id(), node);
            root.getChildren().add(node);
        }
        return new RenderedGlobe(root, cellNodes);
    }

    public CellNodeFactory cellNodeFactory() {
        return cellNodeFactory;
    }

    private Sphere createCoreSphere() {
        Sphere sphere = new Sphere(CellNodeFactory.GLOBE_RADIUS - 8.0d);
        PhongMaterial material = new PhongMaterial(Color.rgb(2, 2, 2));
        material.setSpecularColor(Color.rgb(18, 18, 18));
        sphere.setMaterial(material);
        sphere.setMouseTransparent(true);
        return sphere;
    }

    public record RenderedGlobe(Group root, Map<String, Node> cellNodes) {
    }
}
