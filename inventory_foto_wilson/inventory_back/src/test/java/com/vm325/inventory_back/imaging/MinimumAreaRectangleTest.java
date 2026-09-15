package com.vm325.inventory_back.imaging;

import georegression.struct.point.Point2D_F64;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinimumAreaRectangleTest {

    @Test
    void compute_rectanguloSinGirar_devuelveSuPropiaAreaYRelacionDeAspecto() {
        List<Point2D_F64> rectangle = List.of(
                new Point2D_F64(0, 0),
                new Point2D_F64(40, 0),
                new Point2D_F64(40, 20),
                new Point2D_F64(0, 20)
        );

        OrderedCorners result = MinimumAreaRectangle.compute(rectangle);

        assertEquals(800.0, result.areaSimple(), 1e-6);
        assertRatioMatches(2.0, result.aspectRatio());
    }

    @Test
    void compute_rectanguloGirado25Grados_devuelveLaMismaAreaYRelacionDeAspecto() {
        // Un rectángulo 40x20 (área 800, relación 2.0) girado un ángulo
        // arbitrario alrededor de su centro: el rectángulo de área mínima
        // que lo contiene debe ser el rectángulo mismo, sin importar el
        // ángulo — esta es la propiedad que hace confiable "rotating
        // calipers" para tarjetas fotografiadas con cualquier inclinación.
        double angle = Math.toRadians(25);
        double cx = 20, cy = 10;
        List<Point2D_F64> rotated = List.of(
                rotate(0, 0, cx, cy, angle),
                rotate(40, 0, cx, cy, angle),
                rotate(40, 20, cx, cy, angle),
                rotate(0, 20, cx, cy, angle)
        );

        OrderedCorners result = MinimumAreaRectangle.compute(rotated);

        assertEquals(800.0, result.areaSimple(), 1e-6);
        assertRatioMatches(2.0, result.aspectRatio());
    }

    private static void assertRatioMatches(double expected, double actual) {
        boolean matchesDirect = Math.abs(actual - expected) < 1e-6;
        boolean matchesReciprocal = Math.abs(actual - 1 / expected) < 1e-6;
        assertTrue(matchesDirect || matchesReciprocal,
                "esperaba " + expected + " o su recíproco, fue " + actual);
    }

    private static Point2D_F64 rotate(double x, double y, double cx, double cy, double angle) {
        double dx = x - cx;
        double dy = y - cy;
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Point2D_F64(cx + dx * cos - dy * sin, cy + dx * sin + dy * cos);
    }
}
