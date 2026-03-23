package com.hexglobe.render;

import com.hexglobe.model.Cell;
import com.hexglobe.model.GlobeMesh;
import com.hexglobe.service.SelectionService;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PointLight;
import javafx.scene.paint.Color;

public class GlobeRenderer {

    private final CellNodeFactory cellNodeFactory;

    public GlobeRenderer(SelectionService selectionService) {
        this.cellNodeFactory = new CellNodeFactory(selectionService);
    }

    public Group render(GlobeMesh mesh) {
        Group globeGroup = new Group();

        for (Cell cell : mesh.getCells()) {
            Group cellNode = cellNodeFactory.createCellNode(cell);
            globeGroup.getChildren().add(cellNode);
        }

        globeGroup.getChildren().addAll(buildLights());

        return globeGroup;
    }

    private Group buildLights() {
        Group lights = new Group();

        // Ambient base so dark faces are still slightly visible
        AmbientLight ambient = new AmbientLight(Color.color(0.15, 0.15, 0.18));

        // Primary cool-white front light
        PointLight primary = new PointLight(Color.color(0.9, 0.92, 1.0));
        primary.setTranslateX(250);
        primary.setTranslateY(-200);
        primary.setTranslateZ(350);

        // Subtle blue fill from the opposite side
        PointLight fill = new PointLight(Color.color(0.1, 0.15, 0.25));
        fill.setTranslateX(-300);
        fill.setTranslateY(150);
        fill.setTranslateZ(-250);

        lights.getChildren().addAll(ambient, primary, fill);
        return lights;
    }
}
