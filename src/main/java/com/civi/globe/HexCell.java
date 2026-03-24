package com.civi.globe;

import java.awt.Color;

/**
 * Célula da malha: normalmente hexágono; para pentágonos o índice final é duplicado.
 */
final class HexCell {
    final int[] ix = new int[6];
    int a;
    int b;
    Color color;
}
