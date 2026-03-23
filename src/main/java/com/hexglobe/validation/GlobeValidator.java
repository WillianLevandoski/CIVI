package com.hexglobe.validation;

import com.hexglobe.model.Cell;
import com.hexglobe.model.GlobeMesh;

import java.util.HashSet;
import java.util.Set;

public class GlobeValidator {

    public ValidationResult validate(GlobeMesh mesh) {
        ValidationResult result = new ValidationResult();

        checkUniqueIds(mesh, result);
        checkMinimumCellCount(mesh, result);
        checkPentagonCount(mesh, result);
        checkNeighborConnectivity(mesh, result);
        checkCentersProjected(mesh, result);

        return result;
    }

    private void checkUniqueIds(GlobeMesh mesh, ValidationResult result) {
        Set<Integer> ids = new HashSet<>();
        for (Cell cell : mesh.getCells()) {
            if (!ids.add(cell.getId())) {
                result.addError("ID duplicado detectado: " + cell.getId());
            }
        }
    }

    private void checkMinimumCellCount(GlobeMesh mesh, ValidationResult result) {
        if (mesh.getCellCount() < 32) {
            result.addWarning("Malha com menos de 32 células: " + mesh.getCellCount());
        }
    }

    private void checkPentagonCount(GlobeMesh mesh, ValidationResult result) {
        int pentagons = mesh.getPentagonCount();
        if (pentagons != 12) {
            result.addWarning("Poliedro de Goldberg deve ter 12 pentágonos. Encontrado: " + pentagons);
        }
    }

    private void checkNeighborConnectivity(GlobeMesh mesh, ValidationResult result) {
        for (Cell cell : mesh.getCells()) {
            if (cell.getNeighbors().isEmpty()) {
                result.addWarning("Célula sem vizinhos: " + cell.getId());
            }
        }
    }

    private void checkCentersProjected(GlobeMesh mesh, ValidationResult result) {
        double radius = mesh.getRadius();
        for (Cell cell : mesh.getCells()) {
            double dist = cell.getCenter().length();
            double diff = Math.abs(dist - radius);
            if (diff > 0.01) {
                result.addWarning(String.format(
                    "Centro da célula %d fora da esfera: dist=%.4f, radius=%.4f",
                    cell.getId(), dist, radius));
            }
        }
    }

    public static class ValidationResult {
        private final java.util.List<String> errors = new java.util.ArrayList<>();
        private final java.util.List<String> warnings = new java.util.ArrayList<>();

        public void addError(String msg) {
            errors.add("[ERRO] " + msg);
        }

        public void addWarning(String msg) {
            warnings.add("[AVISO] " + msg);
        }

        public boolean isValid() {
            return errors.isEmpty();
        }

        public void print() {
            for (String e : errors) {
                System.err.println(e);
            }
            for (String w : warnings) {
                System.out.println(w);
            }
            if (errors.isEmpty() && warnings.isEmpty()) {
                System.out.println("[OK] Malha válida.");
            }
        }
    }
}
