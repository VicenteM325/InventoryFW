package com.vm325.inventory_back.imaging;

import georegression.struct.point.Point2D_F64;

import java.util.List;

/**
 * Rectángulo de área mínima que contiene una envolvente convexa dada
 * ("rotating calipers"): el rectángulo de área mínima que encierra un
 * conjunto convexo siempre tiene un lado colineal con alguno de los lados
 * de la envolvente, así que basta probar la orientación de cada lado.
 * <p>
 * Se usa en vez de exigir que el contorno detectado se ajuste exactamente a
 * un polígono de 4 vértices: cualquier forma razonablemente rectangular,
 * aunque su trazo tenga ruido, produce un buen rectángulo de 4 esquinas.
 */
final class MinimumAreaRectangle {

    private MinimumAreaRectangle() {
    }

    static OrderedCorners compute(List<Point2D_F64> hull) {
        int n = hull.size();
        double bestArea = Double.MAX_VALUE;
        double bestMinX = 0, bestMinY = 0, bestMaxX = 0, bestMaxY = 0, bestAngle = 0;

        for (int i = 0; i < n; i++) {
            Point2D_F64 a = hull.get(i);
            Point2D_F64 b = hull.get((i + 1) % n);
            double edgeAngle = Math.atan2(b.y - a.y, b.x - a.x);
            double cos = Math.cos(-edgeAngle);
            double sin = Math.sin(-edgeAngle);

            double minX = Double.MAX_VALUE, maxX = -Double.MAX_VALUE;
            double minY = Double.MAX_VALUE, maxY = -Double.MAX_VALUE;
            for (Point2D_F64 p : hull) {
                double rx = p.x * cos - p.y * sin;
                double ry = p.x * sin + p.y * cos;
                minX = Math.min(minX, rx);
                maxX = Math.max(maxX, rx);
                minY = Math.min(minY, ry);
                maxY = Math.max(maxY, ry);
            }

            double area = (maxX - minX) * (maxY - minY);
            if (area < bestArea) {
                bestArea = area;
                bestMinX = minX;
                bestMinY = minY;
                bestMaxX = maxX;
                bestMaxY = maxY;
                bestAngle = edgeAngle;
            }
        }

        double cos = Math.cos(bestAngle);
        double sin = Math.sin(bestAngle);
        List<Point2D_F64> corners = List.of(
                rotateBack(bestMinX, bestMinY, cos, sin),
                rotateBack(bestMaxX, bestMinY, cos, sin),
                rotateBack(bestMaxX, bestMaxY, cos, sin),
                rotateBack(bestMinX, bestMaxY, cos, sin)
        );
        return OrderedCorners.from(corners);
    }

    private static Point2D_F64 rotateBack(double x, double y, double cos, double sin) {
        return new Point2D_F64(x * cos - y * sin, x * sin + y * cos);
    }
}
