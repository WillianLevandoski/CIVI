package com.civi.globe;

import javafx.animation.AnimationTimer;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.canvas.Canvas;
import javafx.scene.canvas.GraphicsContext;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.image.Image;
import javafx.scene.image.PixelReader;
import javafx.scene.image.PixelWriter;
import javafx.scene.image.WritableImage;
import javafx.scene.input.MouseEvent;
import javafx.scene.input.KeyCode;
import javafx.geometry.Pos;
import javafx.scene.layout.BorderPane;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.stage.Stage;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Main extends Application {
    private static final int BASE_SUBDIVISIONS = 10; // configuração atual (1x)
    private static final int GLOBE_DETAIL_MULTIPLIER = 2; // altere aqui para aumentar/reduzir densidade
    private static final double KEYBOARD_ROTATION_SPEED = 0.5;
    private static final double MOUSE_DRAG_SENSITIVITY = 0.50;
    private static final double ZOOM_SCROLL_STEP = 0.50;
    private static final double MIN_ZOOM = 2.50;
    private static final double MAX_ZOOM = 20.2;
    private static final double MINIMAP_ZOOM = 1.08;

    private double animX = 0.0;
    private double animY = 0.0;
    private double dAnimX = 0.0;
    private double dAnimY = 0.0;
    private double zoom = 1.0;

    private double lastMouseX = 0.0;
    private double lastMouseY = 0.0;
    private boolean dragging = false;

    private boolean upPressed = false;
    private boolean downPressed = false;
    private boolean leftPressed = false;
    private boolean rightPressed = false;

    private final HexSphereBuilder mesh = new HexSphereBuilder();
    private final List<Face2D> renderedFaces = new ArrayList<>();
    private final Label selectedInfoLabel = new Label("Clique em um hexágono para ver o ID e os vizinhos.");
    private final Canvas minimapCanvas = new Canvas(256, 140);
    private final Image hexTexture = loadTexture("/textures/hex-tile.png");
    private boolean showColoredGrid = false;
    private Integer selectedCellId;

    @Override
    public void start(Stage stage) {
        mesh.build(BASE_SUBDIVISIONS * GLOBE_DETAIL_MULTIPLIER, 1.5);

        Canvas canvas = new Canvas(1100, 800);
        StackPane root = new StackPane(canvas);
        BorderPane layout = new BorderPane();
        layout.setCenter(root);

        selectedInfoLabel.setWrapText(true);
        selectedInfoLabel.setTextFill(Color.WHITE);
        selectedInfoLabel.setMinWidth(280);
        selectedInfoLabel.setStyle("-fx-padding: 12; -fx-font-size: 14;");
        minimapCanvas.setWidth(256);
        minimapCanvas.setHeight(140);
        minimapCanvas.setOnMouseClicked(this::handleMinimapClick);

        Button toggleGridButton = new Button("Mostrar grade");
        toggleGridButton.setMaxWidth(Double.MAX_VALUE);
        toggleGridButton.setOnAction(e -> {
            showColoredGrid = !showColoredGrid;
            toggleGridButton.setText(showColoredGrid ? "Esconder grade" : "Mostrar grade");
        });

        Button paintAllButton = new Button("Mostrar tudo");
        paintAllButton.setMaxWidth(Double.MAX_VALUE);
        paintAllButton.setOnAction(e -> {
            int revealed = revealAllHiddenCells();
            selectedInfoLabel.setText("Células reveladas: " + revealed + ".");
        });

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox rightPane = new VBox(minimapCanvas, selectedInfoLabel, spacer, toggleGridButton, paintAllButton);
        rightPane.setAlignment(Pos.TOP_LEFT);
        rightPane.setMinWidth(280);
        rightPane.setStyle("-fx-padding: 12; -fx-spacing: 10; -fx-background-color: #1c1c1c; -fx-border-color: #303030; -fx-border-width: 0 0 0 1;");
        layout.setRight(rightPane);

        Scene scene = new Scene(layout, 1400, 800, Color.rgb(18, 18, 18));

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

        canvas.setOnScroll(e -> {
            double direction = e.getDeltaY() > 0 ? 1.0 : -1.0;
            zoom += direction * ZOOM_SCROLL_STEP;
            zoom = clamp(zoom, MIN_ZOOM, MAX_ZOOM);
        });

        canvas.setOnMouseClicked(e -> {
            HexCell clickedCell = findCellAt(e.getX(), e.getY());
            if (clickedCell == null) {
                selectedInfoLabel.setText("Nenhum hexágono selecionado.");
                selectedCellId = null;
                drawMinimap();
                return;
            }

            selectedCellId = clickedCell.id;
            clickedCell.revealed = true;
            for (Integer neighborId : clickedCell.neighbors) {
                if (neighborId >= 0 && neighborId < mesh.cells.size()) {
                    mesh.cells.get(neighborId).revealed = true;
                }
            }

            String neighbors = clickedCell.neighbors.stream()
                    .sorted()
                    .map(String::valueOf)
                    .collect(Collectors.joining(", "));
            selectedInfoLabel.setText(
                    "ID: " + clickedCell.id
                            + "\nVizinhos: " + neighbors
                            + "\nCélula clicada e vizinhos revelados: " + (clickedCell.neighbors.size() + 1)
            );
            drawMinimap();
        });

        new AnimationTimer() {
            @Override
            public void handle(long now) {
                animX = (animX + dAnimX) % 360.0;
                animY = (animY + dAnimY) % 360.0;
                draw(canvas.getGraphicsContext2D(), canvas.getWidth(), canvas.getHeight());
                drawMinimap();
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
                double k = (360.0 * zoom) / depth;
                xs[i] = (pr.x * k) + (w * 0.5);
                ys[i] = (-pr.y * k) + (h * 0.5);
            }
            faces.add(new Face2D(xs, ys, zAcc / 6.0, c));
        }

        faces.sort(Comparator.comparingDouble(a -> a.z));
        renderedFaces.clear();
        renderedFaces.addAll(faces);
        for (Face2D f : faces) {
            if (!f.cell.revealed) {
                if (hexTexture != null) {
                    drawTexturedPolygon(g, f.x, f.y, hexTexture);
                } else {
                    g.setFill(Color.BLACK);
                    g.fillPolygon(f.x, f.y, 6);
                }
            } else {
                g.setFill(f.cell.predefinedColor);
                g.fillPolygon(f.x, f.y, 6);
            }
            if (showColoredGrid) {
                g.setStroke(Color.WHITE);
                g.setLineWidth(0.0);
                g.strokePolygon(f.x, f.y, 6);
            }
        }
    }

    private int revealAllHiddenCells() {
        int revealedCount = 0;
        for (HexCell cell : mesh.cells) {
            if (cell.revealed) {
                continue;
            }
            cell.revealed = true;
            revealedCount++;
        }
        drawMinimap();
        return revealedCount;
    }

    private HexCell findCellAt(double x, double y) {
        for (int i = renderedFaces.size() - 1; i >= 0; i--) {
            Face2D face = renderedFaces.get(i);
            if (pointInPolygon(x, y, face.x, face.y)) {
                return face.cell;
            }
        }
        return null;
    }

    private static boolean pointInPolygon(double x, double y, double[] px, double[] py) {
        boolean inside = false;
        for (int i = 0, j = px.length - 1; i < px.length; j = i++) {
            boolean intersects = ((py[i] > y) != (py[j] > y))
                    && (x < ((px[j] - px[i]) * (y - py[i]) / (py[j] - py[i]) + px[i]));
            if (intersects) {
                inside = !inside;
            }
        }
        return inside;
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

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Image loadTexture(String resourcePath) {
        var stream = Main.class.getResourceAsStream(resourcePath);
        if (stream == null) {
            return null;
        }

        Image image = new Image(stream);
        return makeMagentaTransparent(image);
    }

    private static Image makeMagentaTransparent(Image source) {
        int width = (int) source.getWidth();
        int height = (int) source.getHeight();
        WritableImage out = new WritableImage(width, height);
        PixelReader reader = source.getPixelReader();
        PixelWriter writer = out.getPixelWriter();

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                Color pixel = reader.getColor(x, y);
                if (isMagenta(pixel)) {
                    writer.setColor(x, y, Color.TRANSPARENT);
                } else {
                    writer.setColor(x, y, pixel);
                }
            }
        }

        return out;
    }

    private static boolean isMagenta(Color pixel) {
        return pixel.getRed() > 0.70 && pixel.getBlue() > 0.70 && pixel.getGreen() < 0.35;
    }

    private static void drawTexturedPolygon(GraphicsContext g, double[] x, double[] y, Image image) {
        double minX = x[0];
        double maxX = x[0];
        double minY = y[0];
        double maxY = y[0];

        for (int i = 1; i < x.length; i++) {
            minX = Math.min(minX, x[i]);
            maxX = Math.max(maxX, x[i]);
            minY = Math.min(minY, y[i]);
            maxY = Math.max(maxY, y[i]);
        }

        g.save();
        g.beginPath();
        g.moveTo(x[0], y[0]);
        for (int i = 1; i < x.length; i++) {
            g.lineTo(x[i], y[i]);
        }
        g.closePath();
        g.clip();
        g.drawImage(image, minX, minY, maxX - minX, maxY - minY);
        g.restore();
    }

    private void drawMinimap() {
        GraphicsContext g = minimapCanvas.getGraphicsContext2D();
        double w = minimapCanvas.getWidth();
        double h = minimapCanvas.getHeight();

        g.setFill(Color.rgb(14, 14, 14));
        g.fillRect(0, 0, w, h);

        g.setStroke(Color.rgb(50, 50, 50));
        g.setLineWidth(1.0);
        g.strokeRect(0.5, 0.5, w - 1.0, h - 1.0);

        g.setStroke(Color.rgb(40, 40, 40));
        g.strokeLine(0, h / 2.0, w, h / 2.0);
        g.strokeLine(w / 4.0, 0, w / 4.0, h);
        g.strokeLine(w / 2.0, 0, w / 2.0, h);
        g.strokeLine((w * 3.0) / 4.0, 0, (w * 3.0) / 4.0, h);

        for (HexCell cell : mesh.cells) {
            Vec3 projected = projectCellToEquirectangular(cell, w, h);
            Color fill = cell.revealed ? cell.predefinedColor : Color.rgb(22, 22, 22);
            g.setFill(fill);
            g.fillOval(projected.x - 1.4, projected.y - 1.4, 2.8, 2.8);
        }

        if (selectedCellId != null && selectedCellId >= 0 && selectedCellId < mesh.cells.size()) {
            HexCell selectedCell = mesh.cells.get(selectedCellId);
            Vec3 selectedPoint = projectCellToEquirectangular(selectedCell, w, h);

            g.setFill(Color.rgb(255, 210, 0));
            g.fillOval(selectedPoint.x - 4.5, selectedPoint.y - 4.5, 9.0, 9.0);
            g.setStroke(Color.BLACK);
            g.setLineWidth(1.2);
            g.strokeOval(selectedPoint.x - 4.5, selectedPoint.y - 4.5, 9.0, 9.0);
        }
    }

    private void handleMinimapClick(MouseEvent event) {
        HexCell nearest = findNearestCellOnMap(event.getX(), event.getY(), minimapCanvas.getWidth(), minimapCanvas.getHeight());
        if (nearest == null) {
            return;
        }

        selectedCellId = nearest.id;
        nearest.revealed = true;
        String neighbors = nearest.neighbors.stream()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(", "));
        selectedInfoLabel.setText(
                "ID: " + nearest.id
                        + "\nVizinhos: " + neighbors
                        + "\nSelecionado pelo mapa plano."
        );
        drawMinimap();
    }

    private HexCell findNearestCellOnMap(double x, double y, double w, double h) {
        HexCell nearest = null;
        double minDistanceSquared = Double.MAX_VALUE;
        for (HexCell cell : mesh.cells) {
            Vec3 mapPoint = projectCellToEquirectangular(cell, w, h);
            double dx = mapPoint.x - x;
            double dy = mapPoint.y - y;
            double distanceSquared = (dx * dx) + (dy * dy);
            if (distanceSquared < minDistanceSquared) {
                minDistanceSquared = distanceSquared;
                nearest = cell;
            }
        }
        return nearest;
    }

    private Vec3 projectCellToEquirectangular(HexCell cell, double width, double height) {
        Vec3 center = averageCellCenter(cell);
        double length = Math.sqrt((center.x * center.x) + (center.y * center.y) + (center.z * center.z));
        if (length > 0) {
            center = new Vec3(center.x / length, center.y / length, center.z / length);
        }

        double longitude = Math.atan2(center.z, center.x);
        double latitude = Math.asin(clamp(center.y, -1.0, 1.0));

        double mapX = ((longitude + Math.PI) / (2.0 * Math.PI)) * width;
        double mapY = ((Math.PI / 2.0 - latitude) / Math.PI) * height;
        return new Vec3(mapX, mapY, 0.0);
    }

    private Vec3 averageCellCenter(HexCell cell) {
        double x = 0.0;
        double y = 0.0;
        double z = 0.0;
        for (int index : cell.ix) {
            Vec3 point = mesh.points.get(index);
            x += point.x;
            y += point.y;
            z += point.z;
        }
        return new Vec3(x / cell.ix.length, y / cell.ix.length, z / cell.ix.length);
    }

    private record Face2D(double[] x, double[] y, double z, HexCell cell) {}

    public static void main(String[] args) {
        launch(args);
    }
}
