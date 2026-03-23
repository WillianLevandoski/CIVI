package com.hexglobe.service;

import com.hexglobe.model.Cell;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class SelectionService {

    private Cell selectedCell;
    private final List<Consumer<Cell>> selectionListeners = new ArrayList<>();

    public void select(Cell cell) {
        selectedCell = cell;
        notifyListeners();
    }

    public void deselect() {
        selectedCell = null;
        notifyListeners();
    }

    public boolean isSelected(Cell cell) {
        return selectedCell != null && selectedCell.getId() == cell.getId();
    }

    public Cell getSelectedCell() {
        return selectedCell;
    }

    public void addSelectionListener(Consumer<Cell> listener) {
        selectionListeners.add(listener);
    }

    private void notifyListeners() {
        for (Consumer<Cell> listener : selectionListeners) {
            listener.accept(selectedCell);
        }
    }
}
