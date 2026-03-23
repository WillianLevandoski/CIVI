package com.civi.globe.core;

public final class GoldbergFormula {

    private GoldbergFormula() {
    }

    public static int computeT(int m, int n) {
        return (m * m) + (m * n) + (n * n);
    }

    public static int computePentagons() {
        return 12;
    }

    public static int computeHexagons(int t) {
        return 10 * (t - 1);
    }

    public static int computeFaces(int t) {
        return (10 * t) + 2;
    }

    public static int computeVertices(int t) {
        return 20 * t;
    }

    public static int computeEdges(int t) {
        return 30 * t;
    }
}
