package com.civi.globe;

import javafx.scene.Scene;

public final class InputController {
    private double lastX;
    private double lastY;

    public void attach(Scene scene, CameraController cameraController) {
        scene.setOnMousePressed(event -> {
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });
        scene.setOnMouseDragged(event -> {
            double dx = event.getSceneX() - lastX;
            double dy = event.getSceneY() - lastY;
            cameraController.rotate(dx * 0.35, dy * 0.35);
            lastX = event.getSceneX();
            lastY = event.getSceneY();
        });
        scene.setOnScroll(event -> cameraController.zoom(event.getDeltaY() * -0.8));
        scene.setOnKeyPressed(event -> {
            switch (event.getCode()) {
                case W, UP -> cameraController.rotate(0.0, 4.0);
                case S, DOWN -> cameraController.rotate(0.0, -4.0);
                case A, LEFT -> cameraController.rotate(-4.0, 0.0);
                case D, RIGHT -> cameraController.rotate(4.0, 0.0);
                case Q -> cameraController.roll(-4.0);
                case E -> cameraController.roll(4.0);
                case PLUS, EQUALS -> cameraController.zoom(-40.0);
                case MINUS -> cameraController.zoom(40.0);
                case R -> cameraController.reset();
                default -> {
                }
            }
        });
    }
}
