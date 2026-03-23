package com.civi.globe.ui;

import com.civi.globe.domain.Cell;
import com.civi.globe.domain.GlobeMesh;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;

public final class HudOverlay {

    private final Label statsLabel = createLabel();
    private final Label selectionLabel = createLabel();
    private final VBox container = new VBox(10.0d);

    public HudOverlay() {
        Label controls = createLabel();
        controls.setText("Drag: rotate | Scroll: zoom | Setas: rotação | +/-: zoom | R: reset");
        selectionLabel.setText("Seleção: nenhuma");
        container.getChildren().addAll(controls, statsLabel, selectionLabel);
        container.setAlignment(Pos.TOP_LEFT);
        container.setPadding(new Insets(14.0d));
        container.setMaxWidth(420.0d);
        container.setStyle("-fx-background-color: rgba(0,0,0,0.72); -fx-border-color: white; -fx-border-width: 1;");
    }

    public StackPane build() {
        StackPane wrapper = new StackPane(container);
        StackPane.setAlignment(container, Pos.TOP_LEFT);
        wrapper.setPickOnBounds(false);
        return wrapper;
    }

    public void updateStats(GlobeMesh mesh) {
        statsLabel.setText(
                "GP(%d,%d) | T=%d | Pentágonos=%d | Hex/Deformados=%d | Células=%d".formatted(
                        mesh.m(),
                        mesh.n(),
                        mesh.t(),
                        mesh.pentagonCount(),
                        mesh.hexagonCount(),
                        mesh.cells().size()
                )
        );
    }

    public void showSelection(Cell cell) {
        selectionLabel.setText(
                "ID: %s\nTipo: %s\nLados: %d\nCentro: %s".formatted(
                        cell.id(),
                        cell.type(),
                        cell.sideCount(),
                        cell.center().format(3)
                )
        );
    }

    public void clearSelection() {
        selectionLabel.setText("Seleção: nenhuma");
    }

    private Label createLabel() {
        Label label = new Label();
        label.setTextFill(Color.WHITE);
        label.setWrapText(true);
        return label;
    }
}
