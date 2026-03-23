package com.hexglobe.model;

import com.hexglobe.math.Vector3;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class Cell {

    private final int id;
    private final CellType type;
    private final Vector3 center;
    private final List<Vector3> vertices;
    private final List<Cell> neighbors;

    public Cell(int id, CellType type, Vector3 center, List<Vector3> vertices) {
        this.id = id;
        this.type = type;
        this.center = center;
        this.vertices = Collections.unmodifiableList(new ArrayList<>(vertices));
        this.neighbors = new ArrayList<>();
    }

    public void addNeighbor(Cell neighbor) {
        if (!neighbors.contains(neighbor)) {
            neighbors.add(neighbor);
        }
    }

    public int getId() {
        return id;
    }

    public CellType getType() {
        return type;
    }

    public Vector3 getCenter() {
        return center;
    }

    public List<Vector3> getVertices() {
        return vertices;
    }

    public List<Cell> getNeighbors() {
        return Collections.unmodifiableList(neighbors);
    }

    public int getSideCount() {
        return vertices.size();
    }

    @Override
    public String toString() {
        return String.format("Cell{id=%d, type=%s, sides=%d, center=%s}", id, type, getSideCount(), center);
    }
}
