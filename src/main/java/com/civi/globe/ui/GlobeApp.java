package com.civi.globe.ui;

import com.civi.globe.core.Cell;
import com.civi.globe.core.GoldbergFormula;
import com.civi.globe.core.GoldbergMeshBuilder;
import com.civi.globe.core.GlobeMesh;
import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.AmbientLight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;
import java.util.LinkedHashMap;
import java.util.Map;

public final class GlobeApp extends Application {

    private final GoldbergMeshBuilder meshBuilder = new GoldbergMeshBuilder();
    private final CellNodeFactory cellNodeFactory = new CellNodeFactory();
    private final Group root3D = new Group();
    private final Rotate rotateX = new Rotate(-20.0d, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20.0d, Rotate.Y_AXIS);
    private final Map<String, Node> cellNodes = new LinkedHashMap<>();
    private final Label selectionLabel = new Label("Seleção: nenhuma");
    private final Label statsLabel = new Label();
    private final TextField mField = new TextField("1");
    private final TextField nField = new TextField("1");

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
        layout.setStyle("-fx-background-color: #10141a;");

        Scene scene = new Scene(layout, 1400, 900, true);
        stage.setTitle("CIVI - Goldberg Globe");
        stage.setScene(scene);
        stage.show();

        loadMesh(1, 1);
    }

    private HBox buildToolbar() {
        Label formulaLabel = new Label("m e n definem GP(m,n)");
        formulaLabel.setTextFill(Color.WHITE);
        mField.setPrefWidth(70);
        nField.setPrefWidth(70);
        javafx.scene.control.Button generateButton = new javafx.scene.control.Button("Gerar");
        generateButton.setOnAction(event -> loadMesh(parseInput(mField.getText()), parseInput(nField.getText())));
        HBox toolbar = new HBox(10.0d, new Label("m"), mField, new Label("n"), nField, generateButton, formulaLabel);
        toolbar.setAlignment(Pos.CENTER_LEFT);
        toolbar.setPadding(new Insets(12.0d));
        toolbar.setStyle("-fx-background-color: #1b2430;");
        toolbar.getChildren().stream().filter(Label.class::isInstance).map(Label.class::cast).forEach(label -> label.setTextFill(Color.WHITE));
        return toolbar;
    }

    private VBox buildInfoPanel() {
        VBox panel = new VBox(10.0d, selectionLabel, statsLabel);
        VBox.setVgrow(statsLabel, Priority.ALWAYS);
        panel.setPadding(new Insets(16.0d));
        panel.setPrefWidth(320.0d);
        panel.setStyle("-fx-background-color: #18202a;");
        selectionLabel.setTextFill(Color.WHITE);
        statsLabel.setTextFill(Color.web("#d0d7de"));
        statsLabel.setWrapText(true);
        return panel;
    }

    private SubScene buildSubScene() {
        root3D.getTransforms().addAll(rotateX, rotateY);
        AmbientLight ambientLight = new AmbientLight(Color.color(0.7d, 0.7d, 0.7d));
        PointLight pointLight = new PointLight(Color.WHITE);
        pointLight.setTranslateZ(-900.0d);
        pointLight.setTranslateY(-300.0d);
        root3D.getChildren().addAll(ambientLight, pointLight);

        SubScene subScene = new SubScene(root3D, 1080, 900, true, javafx.scene.SceneAntialiasing.BALANCED);
        subScene.setFill(Color.web("#0b0f14"));
        camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1d);
        camera.setFarClip(5000.0d);
        camera.setTranslateZ(-900.0d);
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
            if (event.getButton() == MouseButton.PRIMARY && event.getPickResult().getIntersectedNode() != null) {
                selectNode(event.getPickResult().getIntersectedNode());
            }
        });
        subScene.setOnMouseDragged(event -> {
            if (event.getButton() != MouseButton.PRIMARY) {
                return;
            }
            rotateY.setAngle(anchorAngleY + (event.getSceneX() - anchorX) * 0.35d);
            rotateX.setAngle(anchorAngleX - (event.getSceneY() - anchorY) * 0.35d);
        });
        subScene.setOnScroll(event -> camera.setTranslateZ(camera.getTranslateZ() + event.getDeltaY()));
    }

    private void selectNode(Node node) {
        Object data = node.getUserData();
        if (!(data instanceof Cell cell)) {
            return;
        }
        if (selectedCell != null) {
            cellNodeFactory.applySelected(cellNodes.get(selectedCell.id()), selectedCell.type(), false);
        }
        selectedCell = cell;
        cellNodeFactory.applySelected(node, cell.type(), true);
        selectionLabel.setText("Seleção: %s | %s".formatted(cell.id(), cell.type()));
    }

    private void loadMesh(int m, int n) {
        GlobeMesh mesh;
        try {
            mesh = meshBuilder.build(m, n);
        } catch (IllegalArgumentException exception) {
            selectionLabel.setText("Erro: " + exception.getMessage());
            return;
        }
        root3D.getChildren().removeIf(node -> node.getUserData() instanceof Cell);
        cellNodes.clear();
        for (Cell cell : mesh.cells()) {
            Node node = cellNodeFactory.create(cell);
            cellNodes.put(cell.id(), node);
            root3D.getChildren().add(node);
        }
        selectedCell = null;
        selectionLabel.setText("Seleção: nenhuma");
        statsLabel.setText(buildStats(mesh));
    }

    private String buildStats(GlobeMesh mesh) {
        return "GP(%d,%d)\nT = %d\nPentágonos = %d\nHexágonos = %d\nFaces = %d\nVértices = %d\nArestas = %d\nCélulas geradas = %d"
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

    private int parseInput(String rawValue) {
        try {
            return Integer.parseInt(rawValue.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }
}
