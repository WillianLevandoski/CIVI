package com.civi.globe.service;

import com.civi.globe.domain.Cell;
import com.civi.globe.ui.CellNodeFactory;
import com.civi.globe.ui.HudOverlay;
import javafx.scene.Node;

import java.util.Map;

public final class SelectionService {

    private final CellNodeFactory cellNodeFactory;
    private final HudOverlay hudOverlay;
    private Map<String, Node> cellNodes = Map.of();
    private Cell selectedCell;

    public SelectionService(CellNodeFactory cellNodeFactory, HudOverlay hudOverlay) {
        this.cellNodeFactory = cellNodeFactory;
        this.hudOverlay = hudOverlay;
    }

    public void bind(Map<String, Node> cellNodes) {
        clear();
        this.cellNodes = cellNodes;
    }

    public void select(Node target) {
        Cell cell = resolveCell(target);
        if (cell == null) {
            return;
        }
        if (selectedCell != null) {
            Node current = cellNodes.get(selectedCell.id());
            if (current != null) {
                cellNodeFactory.applySelection(current, false);
            }
        }
        selectedCell = cell;
        Node selectedNode = cellNodes.get(cell.id());
        if (selectedNode != null) {
            cellNodeFactory.applySelection(selectedNode, true);
        }
        hudOverlay.showSelection(cell);
    }

    public void clear() {
        if (selectedCell != null) {
            Node current = cellNodes.get(selectedCell.id());
            if (current != null) {
                cellNodeFactory.applySelection(current, false);
            }
        }
        selectedCell = null;
        hudOverlay.clearSelection();
    }

    private Cell resolveCell(Node node) {
        Node current = node;
        while (current != null) {
            if (current.getUserData() instanceof Cell cell) {
                return cell;
            }
            current = current.getParent();
        }
        return null;
    }
}
