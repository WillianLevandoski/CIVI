package com.civi.globe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javafx.scene.Group;

public final class GlobeMesh {
    private final List<Cell> cells;
    private final List<EdgeSegment> edgeSegments;
    private final Map<String, Cell> cellsById;
    private final Group fillGroup;
    private final Group edgeGroup;

    public GlobeMesh(List<Cell> cells, List<EdgeSegment> edgeSegments, Map<String, Cell> cellsById, Group fillGroup, Group edgeGroup) {
        this.cells = Collections.unmodifiableList(new ArrayList<>(cells));
        this.edgeSegments = Collections.unmodifiableList(new ArrayList<>(edgeSegments));
        this.cellsById = Map.copyOf(cellsById);
        this.fillGroup = fillGroup;
        this.edgeGroup = edgeGroup;
    }

    public List<Cell> cells() {
        return cells;
    }

    public List<EdgeSegment> edgeSegments() {
        return edgeSegments;
    }

    public Map<String, Cell> cellsById() {
        return cellsById;
    }

    public Group fillGroup() {
        return fillGroup;
    }

    public Group edgeGroup() {
        return edgeGroup;
    }

    public record EdgeSegment(Vec3 start, Vec3 end, String aKey, String bKey) {
    }
}
