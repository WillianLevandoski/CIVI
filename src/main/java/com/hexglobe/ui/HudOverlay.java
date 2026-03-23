package com.hexglobe.ui;

import com.hexglobe.math.Vector3;
import com.hexglobe.model.Cell;
import javafx.geometry.Insets;
import javafx.scene.control.Label;
import javafx.scene.layout.VBox;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;

public class HudOverlay extends VBox {

    private final Label idLabel;
    private final Label typeLabel;
    private final Label sidesLabel;
    private final Label centerLabel;
    private final Label neighborsLabel;

    public HudOverlay() {
        setPadding(new Insets(10, 16, 10, 16));
        setSpacing(3);
        setStyle(
            "-fx-background-color: rgba(0,0,0,0.80);" +
            "-fx-border-color: #00ffff;" +
            "-fx-border-width: 1px;" +
            "-fx-border-radius: 2px;"
        );
        setMaxWidth(280);

        Label header = makeLabel("[ CELULA SELECIONADA ]", true);
        idLabel       = makeLabel("", false);
        typeLabel     = makeLabel("", false);
        sidesLabel    = makeLabel("", false);
        centerLabel   = makeLabel("", false);
        neighborsLabel = makeLabel("", false);

        getChildren().addAll(header, idLabel, typeLabel, sidesLabel, centerLabel, neighborsLabel);
        setVisible(false);
        setMouseTransparent(true);
    }

    private Label makeLabel(String text, boolean header) {
        Label label = new Label(text);
        label.setTextFill(header ? Color.CYAN : Color.color(0.82, 0.82, 0.82));
        label.setFont(Font.font("Monospace", header ? FontWeight.BOLD : FontWeight.NORMAL, 11));
        return label;
    }

    public void show(Cell cell) {
        Vector3 c = cell.getCenter();
        idLabel.setText(String.format("ID       : %d", cell.getId()));
        typeLabel.setText(String.format("TIPO     : %s", cell.getType().name()));
        sidesLabel.setText(String.format("LADOS    : %d", cell.getSideCount()));
        centerLabel.setText(String.format("CENTRO   : %.2f, %.2f, %.2f", c.x, c.y, c.z));
        neighborsLabel.setText(String.format("VIZINHOS : %d", cell.getNeighbors().size()));
        setVisible(true);
    }

    public void hide() {
        setVisible(false);
    }
}
