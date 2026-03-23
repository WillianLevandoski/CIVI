package com.civi.globe.ui;

import com.civi.globe.domain.HexGlobe;
import com.civi.globe.domain.HexTile;
import java.awt.BasicStroke;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseWheelEvent;
import java.util.ArrayList;
import java.util.List;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

public final class GlobePanel extends JPanel {

    private final HexGlobe globe;
    private final CameraState cameraState;
    private final GlobeProjectionService projectionService;
    private final List<ProjectedTile> lastProjection;

    private String selectedTileId = "Nenhum hexágono selecionado";

    public GlobePanel(HexGlobe globe) {
        this.globe = globe;
        this.cameraState = new CameraState();
        this.projectionService = new GlobeProjectionService();
        this.lastProjection = new ArrayList<>();

        setBackground(Color.BLACK);
        setPreferredSize(new Dimension(1200, 800));
        setFocusable(true);
        configureInteractions();
    }

    @Override
    protected void paintComponent(Graphics graphics) {
        super.paintComponent(graphics);
        Graphics2D graphics2D = (Graphics2D) graphics.create();
        graphics2D.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        lastProjection.clear();
        lastProjection.addAll(projectionService.project(globe.tiles(), cameraState, getSize()));

        graphics2D.setColor(Color.BLACK);
        graphics2D.fillRect(0, 0, getWidth(), getHeight());
        graphics2D.setColor(Color.WHITE);
        graphics2D.setStroke(new BasicStroke(1.2f));

        for (ProjectedTile projectedTile : lastProjection) {
            Polygon polygon = projectedTile.polygon();
            graphics2D.setColor(Color.BLACK);
            graphics2D.fillPolygon(polygon);
            graphics2D.setColor(Color.WHITE);
            graphics2D.drawPolygon(polygon);
        }

        drawHud(graphics2D);
        graphics2D.dispose();
    }

    private void drawHud(Graphics2D graphics2D) {
        graphics2D.setColor(Color.WHITE);
        graphics2D.setFont(graphics2D.getFont().deriveFont(Font.BOLD, 16f));
        graphics2D.drawString("Controles: ← → ↑ ↓ | Zoom: roda do mouse, +, -", 18, 28);
        graphics2D.drawString("Clique em um hexágono para selecionar", 18, 52);
        graphics2D.drawString("Selecionado: " + selectedTileId, 18, 76);
        graphics2D.drawString("Total de hexágonos: " + globe.tiles().size(), 18, 100);
    }

    private void configureInteractions() {
        addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent event) {
                requestFocusInWindow();
                if (SwingUtilities.isLeftMouseButton(event)) {
                    selectTile(event.getX(), event.getY());
                }
            }
        });

        addMouseWheelListener(this::handleZoom);
        addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent event) {
                handleNavigation(event);
            }
        });
    }

    private void handleNavigation(KeyEvent event) {
        switch (event.getKeyCode()) {
            case KeyEvent.VK_LEFT -> cameraState.rotateLeft();
            case KeyEvent.VK_RIGHT -> cameraState.rotateRight();
            case KeyEvent.VK_UP -> cameraState.rotateUp();
            case KeyEvent.VK_DOWN -> cameraState.rotateDown();
            case KeyEvent.VK_ADD, KeyEvent.VK_EQUALS -> cameraState.zoomIn();
            case KeyEvent.VK_SUBTRACT, KeyEvent.VK_MINUS -> cameraState.zoomOut();
            default -> {
                return;
            }
        }
        repaint();
    }

    private void handleZoom(MouseWheelEvent event) {
        if (event.getPreciseWheelRotation() < 0) {
            cameraState.zoomIn();
        } else {
            cameraState.zoomOut();
        }
        repaint();
    }

    private void selectTile(int x, int y) {
        for (int index = lastProjection.size() - 1; index >= 0; index--) {
            ProjectedTile projectedTile = lastProjection.get(index);
            if (projectedTile.polygon().contains(x, y)) {
                HexTile tile = projectedTile.tile();
                selectedTileId = "%s (linha %d, coluna %d)".formatted(tile.id(), tile.row(), tile.column());
                repaint();
                return;
            }
        }
    }
}
