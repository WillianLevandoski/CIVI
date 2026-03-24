package com.civi.globe;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.Timer;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.Polygon;
import java.awt.RenderingHints;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Main {
    private double animX = 0.0;
    private double animY = 0.0;
    private double dAnimX = 0.0;
    private double dAnimY = 0.0;

    private static final int FPS_DELAY_MS = 16;
    private final HexSphereBuilder mesh = new HexSphereBuilder();
    private final JPanel panel;

    private Main() {
        mesh.build(10, 1.5);
        panel = new JPanel() {
            @Override
            protected void paintComponent(Graphics g) {
                super.paintComponent(g);
                draw((Graphics2D) g, getWidth(), getHeight());
            }
        };
        panel.setFocusable(true);

        panel.addKeyListener(new KeyAdapter() {
            @Override
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_DOWN) animX += 2.0;
                if (e.getKeyCode() == KeyEvent.VK_UP) animX -= 2.0;
                if (e.getKeyCode() == KeyEvent.VK_RIGHT) animY += 2.0;
                if (e.getKeyCode() == KeyEvent.VK_LEFT) animY -= 2.0;
            }
        });
    }

    private void show() {
        JFrame frame = new JFrame("Hex Sphere (Java port)");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setLocationRelativeTo(null);
        frame.setContentPane(panel);
        frame.setVisible(true);
        panel.requestFocusInWindow();

        Timer timer = new Timer(FPS_DELAY_MS, e -> {
            animX = (animX + dAnimX) % 360.0;
            animY = (animY + dAnimY) % 360.0;
            panel.repaint();
        });
        timer.start();
    }

    private void draw(Graphics2D g, int w, int h) {
        g.setColor(new Color(12, 12, 12));
        g.fillRect(0, 0, w, h);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);

        List<Face2D> faces = new ArrayList<>();
        for (HexCell c : mesh.cells) {
            double[] xs = new double[6];
            double[] ys = new double[6];
            double zAcc = 0.0;
            for (int i = 0; i < 6; i++) {
                Vec3 p = mesh.points.get(c.ix[i]);
                Vec3 pr = rotate(p, animX, animY);
                zAcc += pr.z;
                double depth = pr.z + 5.0;
                double k = 360.0 / depth;
                xs[i] = (pr.x * k) + (w * 0.5);
                ys[i] = (-pr.y * k) + (h * 0.5);
            }
            faces.add(new Face2D(xs, ys, zAcc / 6.0, c.color));
        }

        faces.sort(Comparator.comparingDouble(a -> a.z));
        for (Face2D f : faces) {
            Polygon poly = new Polygon();
            for (int i = 0; i < 6; i++) {
                poly.addPoint((int) Math.round(f.x[i]), (int) Math.round(f.y[i]));
            }
            g.setColor(f.color);
            g.fillPolygon(poly);
            g.setColor(Color.WHITE);
            g.drawPolygon(poly);
        }
    }

    private static Vec3 rotate(Vec3 p, double ax, double ay) {
        double rx = Math.toRadians(ax);
        double ry = Math.toRadians(ay);

        double cy = Math.cos(ry);
        double sy = Math.sin(ry);
        double x1 = (p.x * cy) + (p.z * sy);
        double z1 = (-p.x * sy) + (p.z * cy);

        double cx = Math.cos(rx);
        double sx = Math.sin(rx);
        double y2 = (p.y * cx) - (z1 * sx);
        double z2 = (p.y * sx) + (z1 * cx);
        return new Vec3(x1, y2, z2);
    }

    private record Face2D(double[] x, double[] y, double z, Color color) {}

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> new Main().show());
    }
}
