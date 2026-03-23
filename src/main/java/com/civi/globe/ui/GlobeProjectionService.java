package com.civi.globe.ui;

import com.civi.globe.domain.HexTile;
import com.civi.globe.domain.Vector3;
import java.awt.Dimension;
import java.awt.Polygon;
import java.awt.geom.Point2D;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class GlobeProjectionService {

    private static final int HEX_SIDES = 6;
    private static final double HEX_RADIUS = 0.16d;

    public List<ProjectedTile> project(List<HexTile> tiles, CameraState cameraState, Dimension canvasSize) {
        List<ProjectedTile> projectedTiles = new ArrayList<>();
        double globeRadius = Math.min(canvasSize.getWidth(), canvasSize.getHeight()) * 0.32d * cameraState.zoom();
        double centerX = canvasSize.getWidth() / 2.0d;
        double centerY = canvasSize.getHeight() / 2.0d;

        for (HexTile tile : tiles) {
            Vector3 rotatedCenter = rotate(tile.center(), cameraState);
            if (rotatedCenter.z() <= -0.18d) {
                continue;
            }

            Polygon polygon = createHexPolygon(tile, cameraState, globeRadius, centerX, centerY);
            if (polygon.npoints == HEX_SIDES) {
                projectedTiles.add(new ProjectedTile(tile, polygon, rotatedCenter.z()));
            }
        }

        projectedTiles.sort(Comparator.comparingDouble(ProjectedTile::depth));
        return projectedTiles;
    }

    private Polygon createHexPolygon(HexTile tile, CameraState cameraState, double globeRadius, double centerX, double centerY) {
        Polygon polygon = new Polygon();
        for (int corner = 0; corner < HEX_SIDES; corner++) {
            double angle = Math.toRadians(60.0d * corner);
            double cornerLatitude = tile.latitude() + (HEX_RADIUS * Math.sin(angle));
            double poleLimit = Math.toRadians(89.0d);
            cornerLatitude = Math.max(-poleLimit, Math.min(poleLimit, cornerLatitude));

            double longitudinalScale = Math.max(0.25d, Math.cos(tile.latitude()));
            double cornerLongitude = tile.longitude() + ((HEX_RADIUS * Math.cos(angle)) / longitudinalScale);
            Vector3 rotatedCorner = rotate(sphericalToCartesian(cornerLatitude, cornerLongitude), cameraState);
            if (rotatedCorner.z() <= -0.20d) {
                return new Polygon();
            }

            Point2D point = projectToScreen(rotatedCorner, globeRadius, centerX, centerY);
            polygon.addPoint((int) Math.round(point.getX()), (int) Math.round(point.getY()));
        }
        return polygon;
    }

    private Point2D projectToScreen(Vector3 point, double globeRadius, double centerX, double centerY) {
        double perspective = 1.0d + (point.z() * 0.18d);
        double x = centerX + (point.x() * globeRadius * perspective);
        double y = centerY - (point.y() * globeRadius * perspective);
        return new Point2D.Double(x, y);
    }

    private Vector3 rotate(Vector3 vector, CameraState cameraState) {
        double sinYaw = Math.sin(cameraState.yaw());
        double cosYaw = Math.cos(cameraState.yaw());
        double yawX = (vector.x() * cosYaw) + (vector.z() * sinYaw);
        double yawZ = (-vector.x() * sinYaw) + (vector.z() * cosYaw);

        double sinPitch = Math.sin(cameraState.pitch());
        double cosPitch = Math.cos(cameraState.pitch());
        double pitchY = (vector.y() * cosPitch) - (yawZ * sinPitch);
        double pitchZ = (vector.y() * sinPitch) + (yawZ * cosPitch);

        return new Vector3(yawX, pitchY, pitchZ);
    }

    private Vector3 sphericalToCartesian(double latitude, double longitude) {
        double cosLatitude = Math.cos(latitude);
        return new Vector3(
                cosLatitude * Math.cos(longitude),
                Math.sin(latitude),
                cosLatitude * Math.sin(longitude)
        );
    }
}
