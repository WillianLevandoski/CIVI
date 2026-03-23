package com.hexglobe.math;

public class GoldbergFormula {

    private final int m;
    private final int n;
    private final int T;

    public GoldbergFormula(int m, int n) {
        this.m = m;
        this.n = n;
        this.T = m * m + m * n + n * n;
    }

    public int getT() {
        return T;
    }

    public int getM() {
        return m;
    }

    public int getN() {
        return n;
    }

    public int expectedHexagonCount() {
        return 10 * T + 2 - 12;
    }

    public int expectedPentagonCount() {
        return 12;
    }

    public int expectedTotalFaceCount() {
        return 10 * T + 2;
    }

    public int expectedVertexCount() {
        return 10 * T * 2;
    }

    public int expectedEdgeCount() {
        return 30 * T;
    }

    @Override
    public String toString() {
        return String.format("Goldberg GP(%d,%d): T=%d, faces=%d (hex=%d, pent=%d)",
            m, n, T, expectedTotalFaceCount(), expectedHexagonCount(), expectedPentagonCount());
    }
}
