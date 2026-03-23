package com.civi.globe.app;

import com.civi.globe.controller.GlobeSceneController;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

public final class GlobeApp extends Application {

    @Override
    public void start(Stage stage) {
        GlobeSceneController controller = new GlobeSceneController();
        Scene scene = controller.createScene(1440, 960);
        scene.setFill(Color.BLACK);

        stage.setTitle("CIVI - Goldberg Globe");
        stage.setScene(scene);
        stage.show();

        controller.initialize();
    }
}
