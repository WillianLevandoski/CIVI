package com.civi.globe.controller;

import com.civi.globe.domain.GlobeMesh;
import com.civi.globe.service.GlobeMeshBuilder;
import com.civi.globe.service.GlobeValidator;
import com.civi.globe.service.SelectionService;
import com.civi.globe.service.SphereProjectionService;
import com.civi.globe.ui.CellNodeFactory;
import com.civi.globe.ui.GlobeRenderer;
import com.civi.globe.ui.HudOverlay;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;

public final class GlobeSceneController {

    private static final int DEFAULT_M = 2;
    private static final int DEFAULT_N = 1;

    private final SphereProjectionService projectionService = new SphereProjectionService();
    private final GlobeValidator globeValidator = new GlobeValidator();
    private final GlobeMeshBuilder meshBuilder = new GlobeMeshBuilder(projectionService, globeValidator);
    private final CellNodeFactory cellNodeFactory = new CellNodeFactory();
    private final GlobeRenderer globeRenderer = new GlobeRenderer(cellNodeFactory);
    private final HudOverlay hudOverlay = new HudOverlay();
    private final SelectionService selectionService = new SelectionService(cellNodeFactory, hudOverlay);
    private final CameraController cameraController = new CameraController();
    private final InputController inputController = new InputController(cameraController, selectionService);

    private final Group globePivot = new Group();
    private SubScene subScene;
    private Scene scene;

    public Scene createScene(double width, double height) {
        BorderPane layout = new BorderPane();
        layout.setStyle("-fx-background-color: black;");

        globePivot.getTransforms().setAll(cameraController.rotateX(), cameraController.rotateY());
        subScene = new SubScene(globePivot, width, height, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        subScene.setCamera(cameraController.camera());

        StackPane viewport = new StackPane(subScene, hudOverlay.build());
        viewport.setAlignment(Pos.TOP_LEFT);
        viewport.setPadding(new Insets(12.0d));
        viewport.setStyle("-fx-background-color: black;");
        subScene.widthProperty().bind(viewport.widthProperty());
        subScene.heightProperty().bind(viewport.heightProperty());

        layout.setCenter(viewport);
        scene = new Scene(layout, width, height, true);
        scene.setFill(Color.BLACK);
        inputController.bind(scene, subScene);
        return scene;
    }

    public void initialize() {
        loadDefaultMesh();
    }

    private void loadDefaultMesh() {
        GlobeMesh mesh = meshBuilder.build(DEFAULT_M, DEFAULT_N);
        GlobeRenderer.RenderedGlobe rendered = globeRenderer.render(mesh);
        globePivot.getChildren().setAll(rendered.root());
        selectionService.bind(rendered.cellNodes());
        hudOverlay.updateStats(mesh);
        cameraController.reset();
    }
}
