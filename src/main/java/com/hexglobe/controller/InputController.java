package com.hexglobe.controller;

import javafx.scene.Scene;
import javafx.scene.SubScene;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.ScrollEvent;

public class InputController {

    private final CameraController cameraController;
    private double lastMouseX;
    private double lastMouseY;

    public InputController(CameraController cameraController) {
        this.cameraController = cameraController;
    }

    public void bindTo(Scene scene, SubScene subScene) {
        subScene.addEventHandler(MouseEvent.MOUSE_PRESSED,  this::onMousePressed);
        subScene.addEventHandler(MouseEvent.MOUSE_DRAGGED,  this::onMouseDragged);
        subScene.addEventHandler(ScrollEvent.SCROLL,        this::onScroll);
        scene.addEventHandler(KeyEvent.KEY_PRESSED,         this::onKeyPressed);
    }

    private void onMousePressed(MouseEvent e) {
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
    }

    private void onMouseDragged(MouseEvent e) {
        double dx = e.getSceneX() - lastMouseX;
        double dy = e.getSceneY() - lastMouseY;
        cameraController.rotateByMouse(dx, dy);
        lastMouseX = e.getSceneX();
        lastMouseY = e.getSceneY();
    }

    private void onScroll(ScrollEvent e) {
        cameraController.zoom(e.getDeltaY() > 0 ? 1 : -1);
    }

    private void onKeyPressed(KeyEvent e) {
        KeyCode code = e.getCode();
        switch (code) {
            case LEFT   -> cameraController.rotateLeft();
            case RIGHT  -> cameraController.rotateRight();
            case UP     -> cameraController.rotateUp();
            case DOWN   -> cameraController.rotateDown();
            case PLUS, EQUALS, ADD -> cameraController.zoomIn();
            case MINUS, SUBTRACT   -> cameraController.zoomOut();
            case R      -> cameraController.reset();
            default     -> { }
        }
    }
}
