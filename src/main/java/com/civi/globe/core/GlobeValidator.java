package com.civi.globe.core;

import com.civi.globe.math.Vector3;
import java.util.HashSet;
import java.util.Set;

public final class GlobeValidator {

    private GlobeValidator() {
    }

    public static void validate(GlobeMesh mesh) {
        validateCounts(mesh);
        validateUniqueIds(mesh);
        validateConnectivity(mesh);
        validateNormalizedCenters(mesh);
    }

    public static void validateCounts(GlobeMesh mesh) {
        long pentagons = mesh.cells().stream().filter(cell -> cell.type() == CellType.PENTAGON).count();
        long hexagons = mesh.cells().stream().filter(cell -> cell.type() == CellType.HEXAGON).count();
        if (pentagons != mesh.pentagonCount()) {
            throw new IllegalStateException("Quantidade de pentágonos inválida.");
        }
        if (hexagons != mesh.hexagonCount()) {
            throw new IllegalStateException("Quantidade de hexágonos inválida.");
        }
    }

    public static void validateUniqueIds(GlobeMesh mesh) {
        Set<String> ids = new HashSet<>();
        for (Cell cell : mesh.cells()) {
            if (!ids.add(cell.id())) {
                throw new IllegalStateException("IDs duplicados encontrados.");
            }
        }
    }

    public static void validateConnectivity(GlobeMesh mesh) {
        for (Cell cell : mesh.cells()) {
            int minimum = cell.type() == CellType.PENTAGON ? 5 : 5;
            if (cell.neighborIds().size() < minimum) {
                throw new IllegalStateException("Conectividade insuficiente para a célula " + cell.id());
            }
        }
    }

    public static void validateNormalizedCenters(GlobeMesh mesh) {
        for (Cell cell : mesh.cells()) {
            Vector3 center = cell.center();
            double length = center.length();
            if (Math.abs(length - 1.0d) > 0.0001d) {
                throw new IllegalStateException("Centro fora da esfera para a célula " + cell.id());
            }
        }
    }
}
