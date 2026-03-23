package com.civi.globe.service;

import com.civi.globe.domain.Cell;
import com.civi.globe.domain.CellType;
import com.civi.globe.domain.GlobeMesh;

import java.util.HashSet;
import java.util.Set;

public final class GlobeValidator {

    public void validate(GlobeMesh mesh) {
        validateUniqueIds(mesh);
        validateCounts(mesh);
        validateCenters(mesh);
        validateNeighbors(mesh);
    }

    private void validateUniqueIds(GlobeMesh mesh) {
        Set<String> ids = new HashSet<>();
        for (Cell cell : mesh.cells()) {
            if (!ids.add(cell.id())) {
                throw new IllegalStateException("IDs duplicados encontrados na malha.");
            }
        }
    }

    private void validateCounts(GlobeMesh mesh) {
        long pentagons = mesh.cells().stream().filter(cell -> cell.type() == CellType.PENTAGON).count();
        long others = mesh.cells().stream().filter(cell -> cell.type() != CellType.PENTAGON).count();
        if (pentagons != mesh.pentagonCount()) {
            throw new IllegalStateException("Quantidade de pentágonos inválida.");
        }
        if (others != mesh.hexagonCount()) {
            throw new IllegalStateException("Quantidade de células não pentagonais inválida.");
        }
    }

    private void validateCenters(GlobeMesh mesh) {
        for (Cell cell : mesh.cells()) {
            double delta = Math.abs(cell.center().length() - 1.0d);
            if (delta > 0.0001d) {
                throw new IllegalStateException("Centro fora da superfície unitária: " + cell.id());
            }
        }
    }

    private void validateNeighbors(GlobeMesh mesh) {
        for (Cell cell : mesh.cells()) {
            int minimum = cell.type() == CellType.PENTAGON ? 5 : 5;
            if (cell.neighbors().size() < minimum) {
                throw new IllegalStateException("Conectividade insuficiente para: " + cell.id());
            }
        }
    }
}
