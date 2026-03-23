package com.civi.globe.controller;

import com.civi.globe.service.SelectionService;
import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;

public final class InputController {

    private static final double ROTATION_FACTOR = 0.35d;
    private static final double ROTATION_STEP = 7.0d;
    private static final double ZOOM_STEP = 70.0d;
    private static final double CLICK_THRESHOLD = 5.0d;

    private final CameraController cameraController;
    private final SelectionService selectionService;

    private double anchorX;
    private double anchorY;
    private boolean dragging;

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
            dragging = false;
        });
        subScene.setOnMouseDragged(event -> {
            double deltaX = event.getSceneX() - anchorX;
            double deltaY = anchorY - event.getSceneY();
            if (Math.abs(deltaX) > CLICK_THRESHOLD || Math.abs(deltaY) > CLICK_THRESHOLD) {
                dragging = true;
            }
            cameraController.rotateBy(deltaX * ROTATION_FACTOR, deltaY * ROTATION_FACTOR);
            anchorX = event.getSceneX();
            anchorY = event.getSceneY();
        });
        subScene.setOnMouseReleased(event -> {
            if (!dragging) {
                selectionService.select(event.getPickResult().getIntersectedNode());
            }
        });
        subScene.setOnScroll(event -> cameraController.zoomBy(event.getDeltaY() > 0 ? -ZOOM_STEP : ZOOM_STEP));
    }

    private void bindKeyboard(Scene scene) {
        scene.setOnKeyPressed(event -> {
            KeyCode code = event.getCode();
            if (code == KeyCode.LEFT) {
                cameraController.rotateBy(-ROTATION_STEP, 0.0d);
            }
            if (code == KeyCode.RIGHT) {
                cameraController.rotateBy(ROTATION_STEP, 0.0d);
            }
            if (code == KeyCode.UP) {
                cameraController.rotateBy(0.0d, -ROTATION_STEP);
            }
            if (code == KeyCode.DOWN) {
                cameraController.rotateBy(0.0d, ROTATION_STEP);
            }
            if (code == KeyCode.PLUS || code == KeyCode.EQUALS || code == KeyCode.ADD) {
                cameraController.zoomBy(-ZOOM_STEP);
            }
            if (code == KeyCode.MINUS || code == KeyCode.SUBTRACT) {
                cameraController.zoomBy(ZOOM_STEP);
            }
            if (code == KeyCode.R) {
                cameraController.reset();
            }
        });
    }
}
