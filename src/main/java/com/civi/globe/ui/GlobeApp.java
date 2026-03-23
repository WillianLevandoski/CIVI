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
import javafx.scene.PointLight;
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
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

import java.util.LinkedHashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

public final class GlobeApp extends Application {

    private static final Logger LOGGER = Logger.getLogger(GlobeApp.class.getName());
    private static final int DEFAULT_M = 1;
    private static final int DEFAULT_N = 1;

    private final GoldbergMeshBuilder meshBuilder = new GoldbergMeshBuilder();
    private final CellNodeFactory cellNodeFactory = new CellNodeFactory();
    private final Group root3D = new Group();
    private final Rotate rotateX = new Rotate(-20.0d, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(-20.0d, Rotate.Y_AXIS);
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
        LOGGER.info(() -> "Inicializando a aplicação CIVI com valores padrão GP(%d,%d).".formatted(DEFAULT_M, DEFAULT_N));
        BorderPane layout = new BorderPane();
        layout.setTop(buildToolbar());
        layout.setCenter(buildSubScene());
        layout.setRight(buildInfoPanel());
        layout.setStyle("-fx-background-color: #10141a;");

        Scene scene = new Scene(layout, 1400, 900, true);
        stage.setTitle("CIVI - Goldberg Globe");
        stage.setScene(scene);
        stage.show();

        LOGGER.info("Janela principal exibida. Iniciando geração automática da malha padrão.");
        loadMesh(DEFAULT_M, DEFAULT_N);
    }

    private HBox buildToolbar() {
        Label formulaLabel = new Label("m e n definem GP(m,n)");
        formulaLabel.setTextFill(Color.WHITE);
        mField.setPrefWidth(70);
        nField.setPrefWidth(70);
        javafx.scene.control.Button generateButton = new javafx.scene.control.Button("Gerar");
        generateButton.setOnAction(event -> {
            int parsedM = parseInput(mField.getText(), "m");
            int parsedN = parseInput(nField.getText(), "n");
            LOGGER.info(() -> "Botão Gerar acionado com m=%d e n=%d.".formatted(parsedM, parsedN));
            loadMesh(parsedM, parsedN);
        });
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
        LOGGER.info("Configurando SubScene 3D, câmera e iluminação.");
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
            LOGGER.fine(() -> "Clique ignorado porque o nó não representa uma célula: " + node.getClass().getSimpleName());
            return;
        }
        if (selectedCell != null) {
            cellNodeFactory.applySelected(cellNodes.get(selectedCell.id()), selectedCell.type(), false);
        }
        selectedCell = cell;
        cellNodeFactory.applySelected(node, cell.type(), true);
        selectionLabel.setText("Seleção: %s | %s".formatted(cell.id(), cell.type()));
        LOGGER.info(() -> "Célula selecionada: %s (%s).".formatted(cell.id(), cell.type()));
    }

    private void loadMesh(int m, int n) {
        long startedAt = System.nanoTime();
        LOGGER.info(() -> "Iniciando loadMesh para GP(%d,%d).".formatted(m, n));
        GlobeMesh mesh;
        try {
            mesh = meshBuilder.build(m, n);
        } catch (IllegalArgumentException exception) {
            LOGGER.log(Level.WARNING, "Falha ao gerar malha para m=" + m + " e n=" + n, exception);
            selectionLabel.setText("Erro: " + exception.getMessage());
            return;
        }

        LOGGER.info(() -> "Malha construída com sucesso: T=%d, células=%d. Preparando nós 3D.".formatted(mesh.t(), mesh.cells().size()));
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
        long elapsedMs = (System.nanoTime() - startedAt) / 1_000_000L;
        LOGGER.info(() -> "loadMesh finalizado para GP(%d,%d) em %d ms. Nós renderizados=%d."
                .formatted(m, n, elapsedMs, cellNodes.size()));
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

    private int parseInput(String rawValue, String fieldName) {
        try {
            int parsed = Integer.parseInt(rawValue.trim());
            LOGGER.fine(() -> "Campo " + fieldName + " interpretado como " + parsed + ".");
            return parsed;
        } catch (NumberFormatException exception) {
            LOGGER.log(Level.WARNING, "Valor inválido no campo " + fieldName + ": '" + rawValue + "'. Usando 0.", exception);
            return 0;
        }
    }
}
