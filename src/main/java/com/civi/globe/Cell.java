package com.civi.globe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class Cell {
    public enum Type {
        HEX,
        PENT
    }

    private final String id;
    private final Type type;
    private final Vec3 center;
    private final List<Vec3> vertices;
    private final int primaryFace;
    private final List<Integer> supportingFaces;
    private final List<String> neighborIds;

    public Cell(String id, Type type, Vec3 center, List<Vec3> vertices, int primaryFace, List<Integer> supportingFaces, List<String> neighborIds) {
        this.id = id;
        this.type = type;
        this.center = center;
        this.vertices = List.copyOf(vertices);
        this.primaryFace = primaryFace;
        this.supportingFaces = List.copyOf(supportingFaces);
        this.neighborIds = Collections.unmodifiableList(new ArrayList<>(neighborIds));
    }

    public String id() {
        return id;
    }

    public Type type() {
        return type;
    }

    public Vec3 center() {
        return center;
    }

    public List<Vec3> vertices() {
        return vertices;
    }

    public int primaryFace() {
        return primaryFace;
    }

    public List<Integer> supportingFaces() {
        return supportingFaces;
    }

    public List<String> neighborIds() {
        return neighborIds;
    }
}
