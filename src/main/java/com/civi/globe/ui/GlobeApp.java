package com.civi.globe.ui;

import com.civi.globe.core.Cell;
import com.civi.globe.core.GoldbergFormula;
import com.civi.globe.core.GoldbergMeshBuilder;
import com.civi.globe.core.GlobeMesh;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.SubScene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.paint.PhongMaterial;
import javafx.scene.shape.Sphere;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;

public final class GlobeApp extends Application {

    private static final int DEFAULT_M = 1;
    private static final int DEFAULT_N = 1;
    private static final double CAMERA_MIN_Z = -1400.0d;
    private static final double CAMERA_MAX_Z = -320.0d;
    private static final double ROTATION_STEP = 8.0d;
    private static final double ZOOM_STEP = 70.0d;

    private final GoldbergMeshBuilder meshBuilder = new GoldbergMeshBuilder();
    private final CellNodeFactory cellNodeFactory = new CellNodeFactory();
    private final Group root3D = new Group();
    private final Group globeGroup = new Group();
    private final Rotate rotateX = new Rotate(-20.0d, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-35.0d, Rotate.Y_AXIS);
    private final Map<String, Node> cellNodes = new LinkedHashMap<>();
    private final Label selectionLabel = new Label("Seleção: nenhuma");
    private final Label statsLabel = new Label();
    private final TextField mField = new TextField(String.valueOf(DEFAULT_M));
    private final TextField nField = new TextField(String.valueOf(DEFAULT_N));

    private Cell selectedCell;
    private double anchorX;
    private double anchorY;
    private double anchorAngleX;
    private double anchorAngleY;
    private PerspectiveCamera camera;

    @Override
    public void start(Stage stage) {
        BorderPane layout = new BorderPane();
        layout.setTop(buildToolbar());
        layout.setCenter(buildSubScene());
        layout.setRight(buildInfoPanel());
        layout.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(layout, 1440, 960, true);
        configureKeyboard(scene);

        stage.setTitle("CIVI - Goldberg Globe");
        stage.setScene(scene);
        stage.show();

        loadMesh(DEFAULT_M, DEFAULT_N);
    }

    private HBox buildToolbar() {
        Label formulaLabel = new Label("Use GP(m,n) e navegue com mouse, setas e +/-");
        styleLabel(formulaLabel);
        Label mLabel = new Label("m");
        Label nLabel = new Label("n");
        styleLabel(mLabel);
        styleLabel(nLabel);
        mField.setPrefWidth(70.0d);
        nField.setPrefWidth(70.0d);
        Button generateButton = new Button("Gerar");
        generateButton.setOnAction(event -> loadMesh(parseInput(mField.getText(), "m"), parseInput(nField.getText(), "n")));
        HBox toolbar = new HBox(10.0d, mLabel, mField, nLabel, nField, generateButton, formulaLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12.0d));
        toolbar.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 0 0 1 0;");
        return toolbar;
    }

    private VBox buildInfoPanel() {
        Label controlsLabel = new Label("Controles\nMouse: rotacionar e clicar\nSetas: girar\n+ / =: zoom in\n-: zoom out");
        styleLabel(controlsLabel);
        statsLabel.setWrapText(true);
        styleLabel(statsLabel);
        styleLabel(selectionLabel);
        VBox panel = new VBox(14.0d, controlsLabel, selectionLabel, statsLabel);
        VBox.setVgrow(statsLabel, Priority.ALWAYS);
        panel.setPadding(new Insets(16.0d));
        panel.setPrefWidth(330.0d);
        panel.setStyle("-fx-background-color: black; -fx-border-color: white; -fx-border-width: 0 0 0 1;");
        return panel;
    }

    private SubScene buildSubScene() {
        root3D.getChildren().clear();
        root3D.getTransforms().setAll(rotateX, rotateY);
        globeGroup.getChildren().clear();
        root3D.getChildren().add(globeGroup);

        AmbientLight ambientLight = new AmbientLight(Color.WHITE);
        root3D.getChildren().add(ambientLight);

        Sphere baseSphere = new Sphere(CellNodeFactory.RADIUS - 4.0d);
        PhongMaterial sphereMaterial = new PhongMaterial(Color.BLACK);
        sphereMaterial.setSpecularColor(Color.gray(0.2d));
        baseSphere.setMaterial(sphereMaterial);
        globeGroup.getChildren().add(baseSphere);

        SubScene subScene = new SubScene(root3D, 1100.0d, 960.0d, true, SceneAntialiasing.BALANCED);
        subScene.setFill(Color.BLACK);
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1d);
        camera.setFarClip(5000.0d);
        camera.setTranslateZ(-850.0d);
        subScene.setCamera(camera);
        configureMouse(subScene);
        return subScene;
    }

    private void configureMouse(SubScene subScene) {
        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            anchorAngleX = rotateX.getAngle();
            anchorAngleY = rotateY.getAngle();
            if (event.getButton() == MouseButton.PRIMARY) {
                selectNode(event.getPickResult().getIntersectedNode());
            }
        });
        subScene.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            rotateY.setAngle(anchorAngleY + ((event.getSceneX() - anchorX) * 0.35d));
            rotateX.setAngle(anchorAngleX - ((event.getSceneY() - anchorY) * 0.35d));
        });
        subScene.setOnScroll(event -> adjustZoom(event.getDeltaY() > 0 ? -ZOOM_STEP : ZOOM_STEP));
    }

    private void configureKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.UP) {
                rotateX.setAngle(rotateX.getAngle() - ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.DOWN) {
                rotateX.setAngle(rotateX.getAngle() + ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.LEFT) {
                rotateY.setAngle(rotateY.getAngle() - ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.RIGHT) {
                rotateY.setAngle(rotateY.getAngle() + ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                adjustZoom(-ZOOM_STEP);
            }
            if (event.getCode() == KeyCode.MINUS) {
                adjustZoom(ZOOM_STEP);
            }
        });
    }

    private void adjustZoom(double delta) {
        double nextValue = camera.getTranslateZ() + delta;
        if (nextValue < CAMERA_MIN_Z) {
            nextValue = CAMERA_MIN_Z;
        }
        if (nextValue > CAMERA_MAX_Z) {
            nextValue = CAMERA_MAX_Z;
        }
        camera.setTranslateZ(nextValue);
    }

    private void selectNode(Node node) {
        Cell cell = findCell(node);
        if (cell == null) {
            return;
        }
        if (selectedCell != null) {
            cellNodeFactory.applySelected(cellNodes.get(selectedCell.id()), false);
        }
        selectedCell = cell;
        cellNodeFactory.applySelected(cellNodes.get(cell.id()), true);
        selectionLabel.setText("Seleção\nID: %s\nTipo: %s\nVizinhos: %d".formatted(cell.id(), cell.type(), cell.neighborIds().size()));
    }

    private Cell findCell(Node node) {
        Node current = node;
        while (current != null) {
            if (current.getUserData() instanceof Cell cell) {
                return cell;
            }
            current = current.getParent();
        }
        return null;
    }

    private void loadMesh(int m, int n) {
        try {
            GlobeMesh mesh = meshBuilder.build(m, n);
            globeGroup.getChildren().removeIf(node -> node.getUserData() instanceof Cell);
            cellNodes.clear();
            for (Cell cell : mesh.cells()) {
                Node cellNode = cellNodeFactory.create(cell);
                cellNodes.put(cell.id(), cellNode);
                globeGroup.getChildren().add(cellNode);
            }
            selectedCell = null;
            selectionLabel.setText("Seleção: nenhuma");
            statsLabel.setText(buildStats(mesh));
        } catch (RuntimeException exception) {
            selectionLabel.setText("Erro: " + exception.getMessage());
        }
    }

    private String buildStats(GlobeMesh mesh) {
        return "GP(%d,%d)\nT = %d\nPentágonos = %d\nHexágonos = %d\nFaces = %d\nVértices = %d\nArestas = %d\nCélulas = %d"
                .formatted(
                        mesh.m(),
                        mesh.n(),
                        mesh.t(),
                        mesh.pentagonCount(),
                        mesh.hexagonCount(),
                        GoldbergFormula.computeFaces(mesh.t()),
                        GoldbergFormula.computeVertices(mesh.t()),
                        GoldbergFormula.computeEdges(mesh.t()),
                        mesh.cells().size()
                );
    }

    private int parseInput(String value, String fieldName) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("Valor inválido para %s.".formatted(fieldName));
        }
    }

    private void styleLabel(Label label) {
        label.setTextFill(Color.WHITE);
    }
}
