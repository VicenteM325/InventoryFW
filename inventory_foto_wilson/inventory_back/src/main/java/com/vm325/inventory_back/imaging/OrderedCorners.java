package com.vm325.inventory_back.imaging;

import georegression.struct.point.Point2D_F64;

import java.util.List;

/**
 * Las 4 esquinas de un cuadrilátero ordenadas como TL/TR/BR/BL, usando el
 * truco clásico de suma/diferencia de coordenadas — funciona sin importar
 * el orden ni el sentido (horario/antihorario) en que se hayan encontrado
 * los puntos. Tipo compartido entre las distintas estrategias de detección
 * de {@link CardDetectionServiceImpl}, para no duplicar esta lógica.
 */
record OrderedCorners(Point2D_F64 topLeft, Point2D_F64 topRight, Point2D_F64 bottomRight, Point2D_F64 bottomLeft) {

    static OrderedCorners from(List<Point2D_F64> fourPoints) {
        Point2D_F64 tl = fourPoints.get(0);
        Point2D_F64 br = fourPoints.get(0);
        Point2D_F64 tr = fourPoints.get(0);
        Point2D_F64 bl = fourPoints.get(0);
        double minSum = Double.MAX_VALUE, maxSum = -Double.MAX_VALUE;
        double minDiff = Double.MAX_VALUE, maxDiff = -Double.MAX_VALUE;

        for (Point2D_F64 p : fourPoints) {
            double sum = p.x + p.y;
            double diff = p.y - p.x;
            if (sum < minSum) {
                minSum = sum;
                tl = p;
            }
            if (sum > maxSum) {
                maxSum = sum;
                br = p;
            }
            if (diff < minDiff) {
                minDiff = diff;
                tr = p;
            }
            if (diff > maxDiff) {
                maxDiff = diff;
                bl = p;
            }
        }
        return new OrderedCorners(tl, tr, br, bl);
    }

    double aspectRatio() {
        double topWidth = distance(topLeft, topRight);
        double bottomWidth = distance(bottomLeft, bottomRight);
        double leftHeight = distance(topLeft, bottomLeft);
        double rightHeight = distance(topRight, bottomRight);
        double width = (topWidth + bottomWidth) / 2.0;
        double height = (leftHeight + rightHeight) / 2.0;
        return width / height;
    }

    double areaSimple() {
        return ConvexHullUtil.area(List.of(topLeft, topRight, bottomRight, bottomLeft));
    }

    private static double distance(Point2D_F64 a, Point2D_F64 b) {
        double dx = a.x - b.x;
        double dy = a.y - b.y;
        return Math.sqrt(dx * dx + dy * dy);
    }
}
