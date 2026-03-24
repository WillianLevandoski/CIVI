package com.civi.globe;

import javafx.scene.PerspectiveCamera;
import javafx.scene.transform.Rotate;

public final class CameraController {
    private final PerspectiveCamera camera;
    private final Rotate globeRotateX;
    private final Rotate globeRotateY;
    private final Rotate globeRotateZ;
    private final double defaultDistance;
    private double distance;

    public CameraController(PerspectiveCamera camera, Rotate globeRotateX, Rotate globeRotateY, Rotate globeRotateZ, double defaultDistance) {
        this.camera = camera;
        this.globeRotateX = globeRotateX;
        this.globeRotateY = globeRotateY;
        this.globeRotateZ = globeRotateZ;
        this.defaultDistance = defaultDistance;
        reset();
    }

    public void rotate(double deltaX, double deltaY) {
        globeRotateY.setAngle(globeRotateY.getAngle() + deltaX);
        globeRotateX.setAngle(clamp(globeRotateX.getAngle() - deltaY, -90.0, 90.0));
    }

    public void roll(double deltaZ) {
        globeRotateZ.setAngle(globeRotateZ.getAngle() + deltaZ);
    }

    public void zoom(double delta) {
        distance = clamp(distance + delta, -1800.0, -250.0);
        camera.setTranslateZ(distance);
    }

    public void reset() {
        globeRotateX.setAngle(-22.0);
        globeRotateY.setAngle(-30.0);
        globeRotateZ.setAngle(0.0);
        distance = defaultDistance;
        camera.setTranslateZ(distance);
    }

    private double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}
