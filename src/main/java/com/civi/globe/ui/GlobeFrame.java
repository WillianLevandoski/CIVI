package com.civi.globe.ui;

import com.civi.globe.domain.HexGlobe;
import java.awt.BorderLayout;
import javax.swing.JFrame;

public final class GlobeFrame extends JFrame {

    public GlobeFrame() {
        super("CIVI - Globo Hexagonal");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());
        add(new GlobePanel(new HexGlobe(16, 24)), BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
    }
}
