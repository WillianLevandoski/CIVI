package com.civi.globe.controller;

import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

public final class CameraController {

    private static final double MIN_Z = -1500.0d;
    private static final double MAX_Z = -420.0d;
    private static final double DEFAULT_Z = -880.0d;
    private static final double DEFAULT_ROTATE_X = -20.0d;
    private static final double DEFAULT_ROTATE_Y = -35.0d;
    private static final double MIN_ROTATE_X = -85.0d;
    private static final double MAX_ROTATE_X = 85.0d;

    private final Rotate rotateX = new Rotate(DEFAULT_ROTATE_X, Rotate.X_AXIS);
    private final Rotate rotateY = new Rotate(DEFAULT_ROTATE_Y, Rotate.Y_AXIS);
    private final PerspectiveCamera camera = new PerspectiveCamera(true);

    public CameraController() {
        camera.setNearClip(0.1d);
        camera.setFarClip(8000.0d);
        camera.setTranslateZ(DEFAULT_Z);
    }

    public PerspectiveCamera camera() {
        return camera;
    }

    public Rotate rotateX() {
        return rotateX;
    }

    public Rotate rotateY() {
        return rotateY;
    }

    public void rotateBy(double deltaX, double deltaY) {
        rotateY.setAngle(rotateY.getAngle() + deltaX);
        rotateX.setAngle(clamp(rotateX.getAngle() + deltaY, MIN_ROTATE_X, MAX_ROTATE_X));
    }

    public void zoomBy(double delta) {
        camera.setTranslateZ(clamp(camera.getTranslateZ() + delta, MIN_Z, MAX_Z));
    }

    public void reset() {
        rotateX.setAngle(DEFAULT_ROTATE_X);
        rotateY.setAngle(DEFAULT_ROTATE_Y);
        camera.setTranslateZ(DEFAULT_Z);
    }

    private double clamp(double value, double min, double max) {
        if (value < min) {
            return min;
        }
        if (value > max) {
            return max;
        }
        return value;
    }
}
