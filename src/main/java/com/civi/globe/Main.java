package com.civi.globe;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.StackPane;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main extends Application {
    private static final double KEYBOARD_ROTATION_SPEED = 1.2;
    private static final double MOUSE_DRAG_SENSITIVITY = 0.22;

    private double animX = 0.0;
    private double animY = 0.0;
    private double dAnimX = 0.0;
    private double dAnimY = 0.0;

    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private boolean dragging = false;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private final HexSphereBuilder mesh = new HexSphereBuilder();

    @Override
    public void start(Stage stage) {
        mesh.build(10, 1.5);

        Canvas canvas = new Canvas(1100, 800);
        StackPane root = new StackPane(canvas);
        Scene scene = new Scene(root, 1100, 800, Color.rgb(18, 18, 18));

        canvas.widthProperty().bind(root.widthProperty());
        canvas.heightProperty().bind(root.heightProperty());

        scene.setOnKeyPressed(e -> updateKeyboardRotation(e.getCode(), true));
        scene.setOnKeyReleased(e -> updateKeyboardRotation(e.getCode(), false));

        canvas.setOnMousePressed(e -> {
            dragging = true;
            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        canvas.setOnMouseReleased(e -> dragging = false);

        canvas.setOnMouseDragged(e -> {
            if (!dragging) {
                return;
            }

            double dx = e.getX() - lastMouseX;
            double dy = e.getY() - lastMouseY;

            animY += dx * MOUSE_DRAG_SENSITIVITY;
            animX -= dy * MOUSE_DRAG_SENSITIVITY;

            lastMouseX = e.getX();
            lastMouseY = e.getY();
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                animX = (animX + dAnimX) % 360.0;
                animY = (animY + dAnimY) % 360.0;
                draw(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
            }
        }.start();

        stage.setTitle("Hex Sphere (Java port)");
        stage.setScene(scene);
        stage.show();
        scene.getRoot().requestFocus();
    }

    private void updateKeyboardRotation(KeyCode keyCode, boolean pressed) {
        if (keyCode == KeyCode.UP) upPressed = pressed;
        if (keyCode == KeyCode.DOWN) downPressed = pressed;
        if (keyCode == KeyCode.LEFT) leftPressed = pressed;
        if (keyCode == KeyCode.RIGHT) rightPressed = pressed;

        dAnimX = 0.0;
        dAnimY = 0.0;

        if (upPressed && !downPressed) dAnimX = -KEYBOARD_ROTATION_SPEED;
        if (downPressed && !upPressed) dAnimX = KEYBOARD_ROTATION_SPEED;
        if (leftPressed && !rightPressed) dAnimY = -KEYBOARD_ROTATION_SPEED;
        if (rightPressed && !leftPressed) dAnimY = KEYBOARD_ROTATION_SPEED;
    }

    private void draw(GraphicsContext g, double w, double h) {
        g.setFill(Color.rgb(12, 12, 12));
        g.fillRect(0, 0, w, h);

        List<Face2D> faces = new ArrayList<>();
        for (HexCell c : mesh.cells) {
            double[] xs = new double[6];
            double[] ys = new double[6];
            double zAcc = 0.0;
            for (int i = 0; i < 6; i++) {
                Vec3 p = mesh.points.get(c.ix[i]);
                Vec3 pr = rotate(p, animX, animY);
                zAcc += pr.z;
                double depth = pr.z + 5.0;
                double k = 360.0 / depth;
                xs[i] = (pr.x * k) + (w * 0.5);
                ys[i] = (-pr.y * k) + (h * 0.5);
            }
            faces.add(new Face2D(xs, ys, zAcc / 6.0, c.color));
        }

        faces.sort(Comparator.comparingDouble(a -> a.z));
        for (Face2D f : faces) {
            g.setFill(f.color);
            g.fillPolygon(f.x, f.y, 6);
            g.setStroke(Color.WHITE);
            g.setLineWidth(1.1);
            g.strokePolygon(f.x, f.y, 6);
        }
    }

    private static Vec3 rotate(Vec3 p, double ax, double ay) {
        double rx = Math.toRadians(ax);
        double ry = Math.toRadians(ay);

        double cy = Math.cos(ry);
        double sy = Math.sin(ry);
        double x1 = (p.x * cy) + (p.z * sy);
        double z1 = (-p.x * sy) + (p.z * cy);

        double cx = Math.cos(rx);
        double sx = Math.sin(rx);
        double y2 = (p.y * cx) - (z1 * sx);
        double z2 = (p.y * sx) + (z1 * cx);
        return new Vec3(x1, y2, z2);
    }

    private record Face2D(double[] x, double[] y, double z, Color color) {}

    public static void main(String[] args) {
        launch(args);
    }
}
