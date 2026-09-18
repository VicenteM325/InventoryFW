package com.vm325.inventory_back.imaging;

import georegression.struct.point.Point2D_F64;

import java.util.ArrayList;
import java.util.List;

/**
 * Envolvente convexa (algoritmo de Andrew, "monotone chain") y área de un
 * polígono simple (fórmula del shoelace), sin dependencias nuevas. Usado
 * para reducir el contorno crudo de una figura detectada a su forma convexa
 * antes de ajustarle un rectángulo de área mínima.
 */
final class ConvexHullUtil {

    private ConvexHullUtil() {
    }

    static List<Point2D_F64> convexHull(List<Point2D_F64> points) {
        List<Point2D_F64> sorted = new ArrayList<>(points);
        sorted.sort((a, b) -> {
            int cmp = Double.compare(a.x, b.x);
            return cmp != 0 ? cmp : Double.compare(a.y, b.y);
        });
        int n = sorted.size();
        if (n < 3) {
            return sorted;
        }

        List<Point2D_F64> hull = new ArrayList<>();
        for (Point2D_F64 p : sorted) {
            while (hull.size() >= 2 && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }

        int lowerSize = hull.size() + 1;
        for (int i = n - 2; i >= 0; i--) {
            Point2D_F64 p = sorted.get(i);
            while (hull.size() >= lowerSize && cross(hull.get(hull.size() - 2), hull.get(hull.size() - 1), p) <= 0) {
                hull.remove(hull.size() - 1);
            }
            hull.add(p);
        }
        hull.remove(hull.size() - 1); // el último punto repite el primero
        return hull;
    }

    static double area(List<Point2D_F64> polygon) {
        double sum = 0;
        int n = polygon.size();
        for (int i = 0; i < n; i++) {
            Point2D_F64 a = polygon.get(i);
            Point2D_F64 b = polygon.get((i + 1) % n);
            sum += a.x * b.y - b.x * a.y;
        }
        return Math.abs(sum) / 2.0;
    }

    private static double cross(Point2D_F64 o, Point2D_F64 a, Point2D_F64 b) {
        return (a.x - o.x) * (b.y - o.y) - (a.y - o.y) * (b.x - o.x);
    }
}
