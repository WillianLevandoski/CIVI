package com.civi.globe.controller;

import com.civi.globe.service.SelectionService;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;

public final class InputController {

    private static final double ROTATION_FACTOR = 0.35d;
    private static final double ROTATION_STEP = 7.0d;
    private static final double ZOOM_STEP = 70.0d;

    private final CameraController cameraController;
    private final SelectionService selectionService;

    private double anchorX;
    private double anchorY;

    public InputController(CameraController cameraController, SelectionService selectionService) {
        this.cameraController = cameraController;
        this.selectionService = selectionService;
    }

    public void bind(Scene scene, SubScene subScene) {
        bindMouse(subScene);
        bindKeyboard(scene);
    }

    private void bindMouse(SubScene subScene) {
        subScene.setOnMousePressed(event -> {
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
            if (event.getButton() == MouseButton.PRIMARY) {
                selectionService.select(event.getPickResult().getIntersectedNode());
            }
        });
        subScene.setOnMouseDragged(event -> {
            if (event.isPrimaryButtonDown()) {
                double deltaX = (event.getSceneX() - anchorX) * ROTATION_FACTOR;
                double deltaY = (anchorY - event.getSceneY()) * ROTATION_FACTOR;
                cameraController.rotateBy(deltaX, deltaY);
                anchorX = event.getSceneX();
                anchorY = event.getSceneY();
            }
        });
        subScene.setOnScroll(event -> cameraController.zoomBy(event.getDeltaY() > 0 ? -ZOOM_STEP : ZOOM_STEP));
    }

    private void bindKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            if (event.getCode() == KeyCode.LEFT) {
                cameraController.rotateBy(-ROTATION_STEP, 0.0d);
            }
            if (event.getCode() == KeyCode.RIGHT) {
                cameraController.rotateBy(ROTATION_STEP, 0.0d);
            }
            if (event.getCode() == KeyCode.UP) {
                cameraController.rotateBy(0.0d, -ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.DOWN) {
                cameraController.rotateBy(0.0d, ROTATION_STEP);
            }
            if (event.getCode() == KeyCode.PLUS || event.getCode() == KeyCode.EQUALS) {
                cameraController.zoomBy(-ZOOM_STEP);
            }
            if (event.getCode() == KeyCode.MINUS) {
                cameraController.zoomBy(ZOOM_STEP);
            }
            if (event.getCode() == KeyCode.R) {
                cameraController.reset();
            }
        });
    }
}
