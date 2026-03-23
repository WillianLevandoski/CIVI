package com.civi.globe.ui;

public final class CameraState {

    private static final double ROTATION_STEP = Math.toRadians(8.0d);
    private static final double ZOOM_STEP = 1.15d;
    private static final double MIN_ZOOM = 0.55d;
    private static final double MAX_ZOOM = 3.5d;

    private double yaw;
    private double pitch;
    private double zoom = 1.0d;

    public void rotateLeft() {
        yaw -= ROTATION_STEP;
    }

    public void rotateRight() {
        yaw += ROTATION_STEP;
    }

    public void rotateUp() {
        pitch = clampPitch(pitch - ROTATION_STEP);
    }

    public void rotateDown() {
        pitch = clampPitch(pitch + ROTATION_STEP);
    }

    public void zoomIn() {
        zoom = Math.min(MAX_ZOOM, zoom * ZOOM_STEP);
    }

    public void zoomOut() {
        zoom = Math.max(MIN_ZOOM, zoom / ZOOM_STEP);
    }

    public double yaw() {
        return yaw;
    }

    public double pitch() {
        return pitch;
    }

    public double zoom() {
        return zoom;
    }

    private double clampPitch(double candidatePitch) {
        double limit = Math.toRadians(85.0d);
        return Math.max(-limit, Math.min(limit, candidatePitch));
    }
}
