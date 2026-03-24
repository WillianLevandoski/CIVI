package com.civi.globe;

import javafx.application.Application;
import javafx.scene.AmbientLight;
import javafx.scene.Group;
import javafx.scene.PerspectiveCamera;
import javafx.scene.Scene;
import javafx.scene.SceneAntialiasing;
import javafx.scene.paint.Color;
import javafx.scene.PointLight;
import javafx.scene.transform.Rotate;
import javafx.stage.Stage;

public final class Main extends Application {
    private static final double SPHERE_RADIUS = 260.0;
    private static final int RESOLUTION = 10;

    @Override
    public void start(Stage stage) {
        GlobeMeshBuilder meshBuilder = new GlobeMeshBuilder();
        GlobeMesh globeMesh = meshBuilder.build(SPHERE_RADIUS, RESOLUTION);

        Group root3D = new Group();
        Group globeRoot = new Group(globeMesh.fillGroup(), globeMesh.edgeGroup());
        Rotate rotateX = new Rotate(-22.0, Rotate.X_AXIS);
        Rotate rotateY = new Rotate(-30.0, Rotate.Y_AXIS);
        Rotate rotateZ = new Rotate(0.0, Rotate.Z_AXIS);
        globeRoot.getTransforms().addAll(rotateX, rotateY, rotateZ);
        root3D.getChildren().add(globeRoot);

        AmbientLight ambientLight = new AmbientLight(Color.color(0.55, 0.55, 0.55));
        PointLight light = new PointLight(Color.WHITE);
        light.setTranslateZ(-600.0);
        light.setTranslateY(-250.0);
        root3D.getChildren().addAll(ambientLight, light);

        PerspectiveCamera camera = new PerspectiveCamera(true);
        camera.setNearClip(0.1);
        camera.setFarClip(5000.0);
        CameraController cameraController = new CameraController(camera, rotateX, rotateY, rotateZ, -900.0);

        Scene scene = new Scene(root3D, 1400, 900, true, SceneAntialiasing.BALANCED);
        scene.setFill(Color.BLACK);
        scene.setCamera(camera);

        InputController inputController = new InputController();
        inputController.attach(scene, cameraController);

        stage.setTitle("Icosahedral Hex Globe");
        stage.setScene(scene);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
