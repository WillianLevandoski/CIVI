package com.hexglobe.model;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class GlobeMesh {

    private final List<Cell> cells;
    private final double radius;

    public GlobeMesh(double radius) {
        this.radius = radius;
        this.cells = new ArrayList<>();
    }

    public void addCell(Cell cell) {
        cells.add(cell);
    }

    public List<Cell> getCells() {
        return Collections.unmodifiableList(cells);
    }

    public int getCellCount() {
        return cells.size();
    }

    public double getRadius() {
        return radius;
    }

    public int getPentagonCount() {
        return (int) cells.stream().filter(c -> c.getType() == CellType.PENTAGON).count();
    }

    public int getHexagonCount() {
        return (int) cells.stream().filter(c -> c.getType() == CellType.HEXAGON).count();
    }
}
