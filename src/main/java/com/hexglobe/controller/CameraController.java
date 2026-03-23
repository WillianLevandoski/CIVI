package com.hexglobe.controller;

import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;
import javafx.scene.transform.Translate;

public class CameraController {

    private static final double DEFAULT_DISTANCE = -500;
    private static final double MIN_DISTANCE     = -900;
    private static final double MAX_DISTANCE     = -120;
    private static final double ROTATE_STEP      = 2.5;
    private static final double SCROLL_STEP      = 18.0;
    private static final double MOUSE_SENSITIVITY = 0.45;

    private final PerspectiveCamera camera;
    private final Rotate rotateX;
    private final Rotate rotateY;
    private final Translate translate;

    private double angleX = -20;
    private double angleY = 30;
    private double distance = DEFAULT_DISTANCE;

    public CameraController() {
        camera = new PerspectiveCamera(true);
        camera.setNearClip(1);
        camera.setFarClip(5000);
        camera.setFieldOfView(45);

        rotateX = new Rotate(angleX, Rotate.X_AXIS);
        rotateY = new Rotate(angleY, Rotate.Y_AXIS);
        translate = new Translate(0, 0, distance);

        camera.getTransforms().addAll(rotateY, rotateX, translate);
    }

    public void rotateByMouse(double dx, double dy) {
        angleY += dx * MOUSE_SENSITIVITY;
        angleX = clampX(angleX - dy * MOUSE_SENSITIVITY);
        applyTransforms();
    }

    public void rotateLeft()  { angleY -= ROTATE_STEP; applyTransforms(); }
    public void rotateRight() { angleY += ROTATE_STEP; applyTransforms(); }

    public void rotateUp() {
        angleX = clampX(angleX - ROTATE_STEP);
        applyTransforms();
    }

    public void rotateDown() {
        angleX = clampX(angleX + ROTATE_STEP);
        applyTransforms();
    }

    public void zoomIn()  { distance = clampDist(distance + SCROLL_STEP); applyTransforms(); }
    public void zoomOut() { distance = clampDist(distance - SCROLL_STEP); applyTransforms(); }

    public void zoom(double direction) {
        distance = clampDist(distance + direction * SCROLL_STEP);
        applyTransforms();
    }

    public void reset() {
        angleX = -20;
        angleY = 30;
        distance = DEFAULT_DISTANCE;
        applyTransforms();
    }

    private double clampX(double x)    { return Math.max(-89, Math.min(89, x)); }
    private double clampDist(double d) { return Math.max(MIN_DISTANCE, Math.min(MAX_DISTANCE, d)); }

    private void applyTransforms() {
        rotateX.setAngle(angleX);
        rotateY.setAngle(angleY);
        translate.setZ(distance);
    }

    public PerspectiveCamera getCamera() {
        return camera;
    }
}
