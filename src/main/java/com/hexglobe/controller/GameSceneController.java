package com.hexglobe.controller;

import com.hexglobe.model.GlobeMesh;
import com.hexglobe.render.GlobeRenderer;
import com.hexglobe.service.GlobeMeshBuilder;
import com.hexglobe.service.SelectionService;
import com.hexglobe.ui.HudOverlay;
import com.hexglobe.validation.GlobeValidator;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public class GameSceneController {

    // Radius in JavaFX units. Camera default distance = -500, so radius=120 fits well.
    private static final double GLOBE_RADIUS = 120.0;
    // GP(1,1): T=3 → 32 cells (12 pentagons + 20 hexagons). Increase to (2,0) or (2,2) for denser mesh.
    private static final int GP_M = 2;
    private static final int GP_N = 0;

    private final BorderPane root;
    private final double width;
    private final double height;

    private SelectionService selectionService;
    private CameraController cameraController;
    private HudOverlay hudOverlay;
    private SubScene subScene;

    public GameSceneController(BorderPane root, double width, double height) {
        this.root = root;
        this.width = width;
        this.height = height;
    }

    public void initialize(Scene scene) {
        selectionService = new SelectionService();
        cameraController = new CameraController();

        GlobeMesh mesh = buildAndValidateMesh();
        Group globeGroup = buildSceneGraph(mesh);

        subScene = createSubScene(globeGroup);
        hudOverlay = new HudOverlay();
        StackPane.setAlignment(hudOverlay, Pos.BOTTOM_LEFT);
        StackPane.setMargin(hudOverlay, new Insets(0, 0, 24, 24));

        StackPane overlay = new StackPane(subScene, hudOverlay);
        overlay.setStyle("-fx-background-color: black;");
        root.setCenter(overlay);

        subScene.widthProperty().bind(overlay.widthProperty());
        subScene.heightProperty().bind(overlay.heightProperty());

        InputController inputController = new InputController(cameraController);
        inputController.bindTo(scene, subScene);

        selectionService.addSelectionListener(selected -> {
            if (selected != null) {
                hudOverlay.show(selected);
            } else {
                hudOverlay.hide();
            }
        });
    }

    private GlobeMesh buildAndValidateMesh() {
        GlobeMeshBuilder builder = new GlobeMeshBuilder(GLOBE_RADIUS, GP_M, GP_N);
        GlobeMesh mesh = builder.build();

        GlobeValidator validator = new GlobeValidator();
        GlobeValidator.ValidationResult result = validator.validate(mesh);
        result.print();

        System.out.printf("Malha gerada: %d celulas (%d pentagons, %d hexagons)%n",
            mesh.getCellCount(), mesh.getPentagonCount(), mesh.getHexagonCount());

        return mesh;
    }

    private Group buildSceneGraph(GlobeMesh mesh) {
        GlobeRenderer renderer = new GlobeRenderer(selectionService);
        return renderer.render(mesh);
    }

    private SubScene createSubScene(Group globeGroup) {
        Group sceneRoot = new Group(globeGroup);
        SubScene scene = new SubScene(sceneRoot, width, height, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        scene.setCamera(cameraController.getCamera());
        return scene;
    }

    public void startRendering() {
        // Hook point for future AnimationTimer / game tick loop
    }
}
