package com.civi.globe.app;

import com.civi.globe.ui.GlobeFrame;
import javax.swing.SwingUtilities;

public final class Main {

    private Main() {
    }

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new GlobeFrame().setVisible(true));
    }
}
