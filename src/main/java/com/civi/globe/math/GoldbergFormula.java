package com.civi.globe.math;

public final class GoldbergFormula {

    private GoldbergFormula() {
    }

    public static int calculateT(int m, int n) {
        return (m * m) + (m * n) + (n * n);
    }

    public static int pentagonCount() {
        return 12;
    }

    public static int hexagonCount(int t) {
        return 10 * (t - 1);
    }

    public static int faceCount(int t) {
        return (10 * t) + 2;
    }

    public static int vertexCount(int t) {
        return 20 * t;
    }

    public static int edgeCount(int t) {
        return 30 * t;
    }
}
