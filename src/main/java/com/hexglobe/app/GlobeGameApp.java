package com.hexglobe.app;

import com.hexglobe.controller.GameSceneController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.layout.BorderPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public class GlobeGameApp extends Application {

    private static final int WIDTH = 1280;
    private static final int HEIGHT = 800;

    @Override
    public void start(Stage primaryStage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: black;");

        Scene scene = new Scene(root, WIDTH, HEIGHT, true);
        scene.setFill(Color.BLACK);

        GameSceneController controller = new GameSceneController(root, WIDTH, HEIGHT);
        controller.initialize(scene);

        primaryStage.setTitle("HexGlobe — Base de Jogo");
        primaryStage.setScene(scene);
        primaryStage.setResizable(true);
        primaryStage.show();

        controller.startRendering();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
